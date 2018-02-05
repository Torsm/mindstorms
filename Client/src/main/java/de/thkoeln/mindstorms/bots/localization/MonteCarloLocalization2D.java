package de.thkoeln.mindstorms.bots.localization;

import de.thkoeln.mindstorms.server.controlling.EV3Controller;
import lejos.robotics.geometry.Line;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * MonteCarloLocalization2D
 */
public class MonteCarloLocalization2D implements Runnable, LocalizationService {
    private final static int CAPACITY = 3000;
    private final static double TRAVEL_DISTANCE = 5;

    private final EV3Controller ctr;
    private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    private final ParticleListener listener;
    private final List<Line> lines;

    private List<Particle> particles;
    private int direction = 0;


    public MonteCarloLocalization2D(EV3Controller ctr, ParticleListener listener, List<Line> lines) {
        this.ctr = ctr;
        this.listener = listener;
        this.lines = lines;
    }

    public void start() {
        final double belief = 1 / (double) CAPACITY;
        direction = ctr.getDirection().await();
        particles = IntStream.range(0, CAPACITY).mapToObj(i -> {
            boolean b = ThreadLocalRandom.current().nextDouble() < 2.0 / 11.0;

            double x = ThreadLocalRandom.current().nextDouble(0, b ? 100 : 150);
            double y = ThreadLocalRandom.current().nextDouble(b ? 150 : 0, b ? 200 : 150);

            double angle = ThreadLocalRandom.current().nextDouble(0, 359);

            return new Particle(x, y, angle, belief);
        }).collect(Collectors.toList());

        listener.redraw(particles);
        service.scheduleWithFixedDelay(this, 1, 1, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        service.shutdown();
    }

    @Override
    public void run() {
        try {
            loop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loop() throws InterruptedException {
        particles.forEach(p -> {
                    Line n = null, e = null, s = null, w = null;
                    for (Line line : lines) {
                        if (p.isRelevant(line)) {
                            if (line.y1 == line.y2) {
                                if (n == null || n.y1 < line.y1 && line.y1 <= p.getY()) {
                                    n = line;
                                } else if (s == null || s.y1 > line.y1 && line.y1 >= p.getY()) {
                                    s = line;
                                }
                            } else {
                                if (e == null || e.x1 > line.x1 && line.x1 >= p.getX()) {
                                    e = line;
                                } else if (w == null || w.x1 < line.x1 && line.x1 <= p.getX()) {
                                    w = line;
                                }
                            }
                        }
                    }
                });

        ctr.setMotorSpeed(360).await();
        ctr.turnSensorTo(-90).await();

        ctr.setMotorSpeed(25).await();
        final AtomicBoolean rotating = new AtomicBoolean(true);
        ctr.turnSensorTo(90).onComplete(result -> rotating.set(false));

        Stream.Builder<float[]> builder = Stream.builder();

        while (rotating.get()) {
            float[] data = ctr.getCurrentAngleData().await();
            builder.add(data);
        }

        builder.build().max(Comparator.comparingDouble(data -> data[1])).ifPresent(data -> {
            float angle = data[0];
            ctr.setMotorSpeed(360).await();
            ctr.turnSensorTo(0);
            ctr.rotate(angle).await();

            double distance = Arrays.stream(ctr.read3().await()).min().orElse(0);
            System.out.println(angle + " -> " + distance);
            if (distance > 0.25 && distance < 1) {
                ctr.travel(100).await();
            } else {
                rotate();
            }
        });
    }

    private void rotate() {
        double angle = ThreadLocalRandom.current().nextDouble() * 360 - 180;
        ctr.rotate(angle).await();
    }
}
