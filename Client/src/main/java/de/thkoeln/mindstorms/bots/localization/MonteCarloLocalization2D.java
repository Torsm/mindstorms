package de.thkoeln.mindstorms.bots.localization;

import de.thkoeln.mindstorms.server.controlling.EV3Controller;
import lejos.robotics.geometry.Line;
import lejos.robotics.geometry.Point;

import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.thkoeln.mindstorms.bots.localization.MonteCarloLocalization1D.mutateValue;

/**
 * MonteCarloLocalization2D
 */
public class MonteCarloLocalization2D implements Runnable, LocalizationService {
    private final static int CAPACITY = 3000;
    private final static double TRAVEL_DISTANCE = 10;

    private final EV3Controller ctr;
    private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    private final ParticleListener listener;
    private final List<Line> lines;

    private List<Particle> particles;


    public MonteCarloLocalization2D(EV3Controller ctr, ParticleListener listener, List<Line> lines) {
        this.ctr = ctr;
        this.listener = listener;
        this.lines = lines;
    }

    public void start() {
        final double belief = 1 / (double) CAPACITY;
        particles = IntStream.range(0, CAPACITY).mapToObj(i -> {
            boolean b = ThreadLocalRandom.current().nextDouble() < 2.0 / 11.0;

            float x = (float) ThreadLocalRandom.current().nextDouble(0, b ? 100 : 150);
            float y = (float) ThreadLocalRandom.current().nextDouble(b ? 150 : 0, b ? 200 : 150);

            double angle = ThreadLocalRandom.current().nextDouble(0, 360);

            return new Particle(x, y, angle, belief);
        }).collect(Collectors.toList());

        listener.redraw(particles);
        service.scheduleWithFixedDelay(this, 1, 1, TimeUnit.MILLISECONDS);
        ctr.setMotorSpeed(100);
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
        ctr.turnSensorTo(-90).await();
        final List<float[]> list = new ArrayList<>();

        int count = 10;
        int step = 180 / count;
        for (int i = 0; i < count; i++) {
            list.add(ctr.getCurrentAngleData().await());
            ctr.rotateSensorMotor(step).await();
        }
        list.add(ctr.getCurrentAngleData().await());

        list.stream().min(Comparator.comparingDouble(data -> data[1])).ifPresent(data -> {
            float angle = data[0];
            ctr.turnSensorTo(0);
            ctr.rotate(angle).await();

            double distance = Arrays.stream(ctr.read3().await()).min().orElse(0) * 1000;
            System.out.println(angle + " -> " + distance);

            particles.forEach(particle -> {
                particle.rotate(angle);

                Point target = new Point((float) (particle.getX() + Math.cos(Math.toRadians(angle)) * 250.0), (float) (particle.getY() + Math.sin(Math.toRadians(angle)) * 250.0));
                final Line viewport = new Line((float) particle.getX(), (float) particle.getY(), target.x, target.y);
                lines.stream()
                        .map(line -> line.intersectsAt(viewport))
                        .filter(Objects::nonNull)
                        .mapToDouble(collision -> new Line((float) particle.getX(), (float) particle.getY(), collision.x, collision.y).length() * 10)
                        .min()
                        .ifPresent(particleDistance -> {
                            double val = Math.min(distance, particleDistance) / Math.max(distance, particleDistance);
                            particle.adjustBelief(val);
                        });
            });

            if (distance > 250 && distance < 1000) {
                ctr.travel(TRAVEL_DISTANCE * 10).await();
                particles.forEach(p -> p.move(TRAVEL_DISTANCE));
            } else {
                rotate();
            }

            resample();

            listener.redraw(particles);
        });
    }

    private void resample() {
        ArrayList<Particle> newGen = new ArrayList<>(CAPACITY);
        particles.removeIf(p -> p.getX() < 0 || p.getY() < 0 || p.getX() > 150 || p.getY() > 200 || (p.getX() > 100 && p.getY() > 150));
        particles.sort(Comparator.comparingDouble(Particle::getBelief).reversed());
        int pos = 0;
        double sp = 2;
        while (newGen.size() < CAPACITY) {
            double indP = 1.0 / (double) particles.size() * (sp - (2.0 * sp - 2.0) * (double) (pos - 1) / (double)(particles.size() - 1));
            if (ThreadLocalRandom.current().nextDouble() < indP) {
                Particle p = particles.get(pos);
                newGen.add(mutate(p));
                pos = -1;
            }
            if (pos < particles.size()) {
                pos++;
            } else {
                pos = 0;
            }
        }

        particles = newGen;
    }

    private Particle mutate(Particle p) {
        return new Particle(mutateValue(p.getX(), 0.1, 5), mutateValue(p.getY(), 0.1, 5), (mutateValue(p.getAngle(), 0.1, 5) + 360) % 360, 1 / (double) CAPACITY);
    }

    private void rotate() {
        final double angle = ThreadLocalRandom.current().nextDouble() * 360 - 180;
        ctr.rotate(angle).await();
        particles.forEach(particle -> particle.rotate(angle));
    }
}
