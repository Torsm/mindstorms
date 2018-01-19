package de.thkoeln.mindstorms.bots.localization;

import de.thkoeln.mindstorms.server.controlling.EV3Controller;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * MonteCarloLocalization
 */
public class MonteCarloLocalization implements Runnable {
    public final static int CAPACITY = 1000;
    private final static double TRAVEL_DISTANCE = 5;

    private final EV3Controller ctr;
    private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    private final ParticleListener listener;
    private final TreeMap<Double, Double> particleDistanceMap;

    private List<Particle> particles;
    private int direction = 1;


    public MonteCarloLocalization(EV3Controller ctr, ParticleListener listener, TreeMap<Double, Double> particleDistanceMap) {
        this.ctr = ctr;
        this.listener = listener;
        this.particleDistanceMap = particleDistanceMap;
    }

    public void start(double y) {
        final double belief = 1 / (double) MonteCarloLocalization.CAPACITY;
        particles = IntStream.range(0, CAPACITY).mapToObj(i -> new Particle(400 * Math.random(), y, belief)).collect(Collectors.toList());
        direction = ctr.getDirection().await();
        ctr.rotateFrontDistanceSensorMotor(90 - direction * ctr.getSensorPosition().await().intValue());
        ctr.clearScreen();

        service.scheduleWithFixedDelay(this, 1, 1, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        service.shutdown();
    }

    @Override
    public void run() {
        ctr.travel(TRAVEL_DISTANCE * 10).await();
        if (correct()) {
            ctr.readFrontDistanceSensor().onComplete(result -> {
                double dist = result.doubleValue() > 50 ? 70 : 20;

                final double sum = particles.parallelStream()
                        .peek(particle -> {
                            particle.adjustX(TRAVEL_DISTANCE * direction);

                            Map.Entry<Double, Double> entry = particleDistanceMap.floorEntry(particle.getX());
                            double particleDist = particle.getY() - (entry == null ? 70 : entry.getValue());
                            double val = Math.min(dist, particleDist) / Math.max(dist, particleDist);

                            particle.adjustBelief(val);
                        })
                        .mapToDouble(Particle::getBelief)
                        .sum();

                particles.parallelStream().forEach(particle -> particle.adjustBelief(1/sum));

                newGen();

                final double sum2 = particles.parallelStream().mapToDouble(Particle::getBelief).sum();
                particles.parallelStream().forEach(particle -> particle.adjustBelief(1/sum2));
                listener.onNewGeneration(particles);
            }).await();
        }
    }

    private double mutate(double val) {
        double mutation = 0.1;
        return val + val * ((Math.random() * mutation) - mutation/2.0);
    }

    private boolean correct() {
        boolean b = true;
        if (ctr.readColorSensor().await() < 0.02) {
            ctr.rotate(90).await();
            ctr.travel(30).await();
            if (ctr.readColorSensor().await() < 0.02) {
                ctr.travel(-60).await();
                if (ctr.readColorSensor().await() < 0.02) {
                    b = false;
                    final CountDownLatch latch = new CountDownLatch(2);
                    ctr.setDirection(direction *= -1);
                    ctr.rotateFrontDistanceSensorMotor(180 * direction).onComplete(result -> latch.countDown());
                    ctr.travel(30).await();
                    ctr.rotate(87).await();
                    ctr.travel(50).onComplete(result -> latch.countDown());
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    ctr.rotate(-98).await();
                }
            } else {
                ctr.rotate(-82).await();
            }
        }
        return b;
    }

    private void newGen() {
        ArrayList<Particle> newGen = new ArrayList<>(CAPACITY);
        particles.sort(Comparator.comparingDouble(Particle::getBelief).reversed());
        int pos = 0;
        double sp = 2;
        while (newGen.size() < CAPACITY) {
            double indP = 1.0 / (double) particles.size() * (sp - (2.0 * sp - 2.0) * (double) (pos - 1) / (double)(particles.size() - 1));
            if (Math.random() < indP) {
                Particle particle = particles.get(pos);
                newGen.add(new Particle(mutate(particle.getX()), particle.getY(), particle.getBelief()));
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


    public interface ParticleListener {
        void onNewGeneration(List<Particle> particles);
    }
}
