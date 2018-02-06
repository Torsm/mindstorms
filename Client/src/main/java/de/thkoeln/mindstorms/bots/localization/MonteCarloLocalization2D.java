package de.thkoeln.mindstorms.bots.localization;

import de.thkoeln.mindstorms.server.controlling.EV3Controller;
import lejos.robotics.geometry.Line;
import lejos.robotics.geometry.Point;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.clustering.DoublePoint;

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

    public static int loopCount, stepCount, solveCount;

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
    public void awaitTermination() {
        try {
            service.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
        loopCount++;
        Stream.Builder<float[]> builder = Stream.builder();
        ctr.turnSensorTo(0).await();
        builder.add(ctr.getCurrentAngleData().await());
        ctr.turnSensorTo(-15).await();
        builder.add(ctr.getCurrentAngleData().await());
        ctr.turnSensorTo(15).await();
        builder.add(ctr.getCurrentAngleData().await());

        builder.build().max(Comparator.comparingDouble(data -> data[1])).ifPresent(data -> {
            float angle = data[0];
            ctr.turnSensorTo(0);
            ctr.rotate(angle).await();

            double distance = Arrays.stream(ctr.read3().await()).min().orElse(0) * 1000;

            particles.forEach(particle -> {
                particle.rotate(angle);

                double particleDistance = particle.measureDistance(lines);
                double val = Math.min(distance, particleDistance) / Math.max(distance, particleDistance);
                particle.adjustBelief(val);
            });

            if (distance > 250) {
                ctr.travel(TRAVEL_DISTANCE * 10).await();
                stepCount++;
                particles.forEach(p -> p.move(TRAVEL_DISTANCE));
                rotate(30);
            } else {
                rotate(180);
            }

            resample();
            listener.redraw(particles);

            DBSCANClusterer<DoublePoint> clusterer = new DBSCANClusterer<>(15, (int) (CAPACITY * 0.95));
            List<DoublePoint> points = particles.stream().map(particle -> new DoublePoint(new double[]{particle.getX(), particle.getY()})).collect(Collectors.toList());
            List<Cluster<DoublePoint>> cluster = clusterer.cluster(points);
            if (cluster.size() == 1) {
                stop();
                solveCount++;
            }
        });
    }

    private void resample() {
        ArrayList<Particle> newGen = new ArrayList<>(CAPACITY);
        particles.removeIf(p -> p.getX() < 0 || p.getY() < 0 || p.getX() > 150 || p.getY() > 200 || (p.getX() > 100 && p.getY() > 150));
        particles.sort(Comparator.comparingDouble(Particle::getBelief).reversed());
        double avg = particles.stream().mapToDouble(Particle::getBelief).average().orElse(0);
        particles.removeIf(particle -> particle.getBelief() < avg * 0.7);
        while(particles.size() < CAPACITY){
            boolean b = ThreadLocalRandom.current().nextDouble() < 2.0 / 11.0;

            float x = (float) ThreadLocalRandom.current().nextDouble(0, b ? 100 : 150);
            float y = (float) ThreadLocalRandom.current().nextDouble(b ? 150 : 0, b ? 200 : 150);

            double angle = ThreadLocalRandom.current().nextDouble(0, 360);

            particles.add(new Particle(x, y, angle, 1 / (double) CAPACITY));
        }

        int pos = 0;
        double sp = 1.4;
        while (newGen.size() < CAPACITY) {
            double indP = 1.0 / (double) particles.size() * (sp - (2.0 * sp - 2.0) * (double) (pos - 1) / (double)(particles.size() - 1));
            if (ThreadLocalRandom.current().nextDouble() < indP) {
                Particle p = particles.get(pos);
                newGen.add(mutate(p));
                pos = -1;
            }
            if (pos < particles.size()-1) {
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

    private void rotate(int deg) {
        final double angle = ThreadLocalRandom.current().nextDouble() * deg * 2 - deg;
        ctr.rotate(angle).await();
        particles.forEach(particle -> particle.rotate(angle));
    }
}
