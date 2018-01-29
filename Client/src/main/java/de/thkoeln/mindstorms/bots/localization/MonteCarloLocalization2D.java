package de.thkoeln.mindstorms.bots.localization;

import de.thkoeln.mindstorms.concurrency.ObservableRequest;
import de.thkoeln.mindstorms.server.controlling.EV3Controller;
import lejos.robotics.geometry.Line;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

            double direction = ThreadLocalRandom.current().nextInt(0, 4);

            return new Particle(x, y, direction * 90, belief);
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
        final double sensorData[] = new double[4];
        ObservableRequest<Float> sensorRequest = ctr.readBackDistanceSensor();
        sensorData[0] = ctr.readFrontDistanceSensor().await();
        ctr.rotateFrontDistanceSensorMotor(-90).await();
        sensorData[1] = ctr.readFrontDistanceSensor().await();
        ctr.rotateFrontDistanceSensorMotor(180).await();
        sensorData[2] = ctr.readFrontDistanceSensor().await();
        ctr.rotateFrontDistanceSensorMotor(-90);
        sensorData[3] = sensorRequest.get();

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

            int direction = (int) p.getAngle() / 90;

            double directions[] = new double[4];
            directions[0] = Objects.requireNonNull(e).x1 - p.getX();
            directions[1] = Objects.requireNonNull(s).y1 - p.getY();
            directions[2] = p.getX() - Objects.requireNonNull(w).x1;
            directions[3] = p.getY() - Objects.requireNonNull(n).y1;

            for (int i = 0; i < 4; i++){
                double partDistance = directions[direction + i % 4];
                double botDistance = sensorData[i];

                double factor = Math.min(partDistance, botDistance) / Math.max(partDistance, botDistance);
                p.adjustBelief(factor);
            }
        });
    }
}
