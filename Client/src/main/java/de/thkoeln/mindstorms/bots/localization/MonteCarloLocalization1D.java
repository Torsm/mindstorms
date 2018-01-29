package de.thkoeln.mindstorms.bots.localization;

import de.thkoeln.mindstorms.concurrency.ObservableRequest;
import de.thkoeln.mindstorms.server.controlling.EV3Controller;
import de.thkoeln.mindstorms.util.LloydsAlgorithm;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * MonteCarloLocalization1D
 */


//TODO Partikel müssen zufällig eine richtung haben, vorne oder hinten da der bot später auch zufällig zu einer seite guckt (?)
public class MonteCarloLocalization1D implements Runnable, LocalizationService {
    private final static int CAPACITY = 1000;
    private final static int WIDTH = 400;
    private final static double TRAVEL_DISTANCE = 5;

    private final EV3Controller ctr;
    private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    private final ParticleListener listener;
    private final TreeMap<Double, Double> particleDistanceMap;

    private List<Particle> particles;
    private int direction = 1;


    public MonteCarloLocalization1D(EV3Controller ctr, ParticleListener listener, TreeMap<Double, Double> particleDistanceMap) {
        this.ctr = ctr;
        this.listener = listener;
        this.particleDistanceMap = particleDistanceMap;
    }

    public void start(double y) {
        final double belief = 1 / (double) CAPACITY;
        direction = ctr.getDirection().await();
        particles = IntStream.range(0, CAPACITY).mapToObj(i -> new Particle(i / (double) CAPACITY * (double) WIDTH, y, (direction - 1) * -90, belief)).collect(Collectors.toList());
        ctr.rotateFrontDistanceSensorMotor(90 - direction * ctr.getSensorPosition().await().intValue());
        ctr.clearScreen();

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
        ctr.travel(TRAVEL_DISTANCE * 10).await();
        if (correct()) {
            final ObservableRequest<double[][]> clusterRequest = new ObservableRequest<>((Method) null);
            ctr.readFrontDistanceSensor().onComplete(result -> {
                double dist = result.doubleValue() > 0.5 ? 70 : 20;

                final double sum = particles.parallelStream()
                        .peek(particle -> {
                            particle.move(TRAVEL_DISTANCE);

                            Map.Entry<Double, Double> entry = particleDistanceMap.floorEntry(particle.getX());
                            double particleDist = particle.getY() - (entry == null ? 0 : entry.getValue());
                            double val = Math.min(dist, particleDist) / Math.max(dist, particleDist);

                            particle.adjustBelief(val);
                        })
                        .mapToDouble(Particle::getBelief)
                        .sum();

                double[][] positions = particles.stream().map(particle -> new double[]{particle.getX(), particle.getY(), 0.0}).collect(Collectors.toList()).toArray(new double[0][0]);
                LloydsAlgorithm lloydsAlgorithm = new LloydsAlgorithm(positions);
                clusterRequest.complete(lloydsAlgorithm.getClusterPoints(2));

                particles.parallelStream().forEach(particle -> particle.adjustBelief(1/sum));

                newGen();

                final double sum2 = particles.parallelStream().mapToDouble(Particle::getBelief).sum();
                particles.parallelStream().forEach(particle -> particle.adjustBelief(1/sum2));
                listener.redraw(particles);
            });

            double[][] clusters = clusterRequest.get();
            if (clusters.length == 2) {
                double x1 = clusters[0][0];
                double x2 = clusters[1][0];
                if (Math.abs(x1 - x2) < 15) {
                    stop();
                    ctr.drawString("AYY", 0, 0);
                }
            }
        } else {
            particles.parallelStream().forEach(particle -> particle.setAngle((direction - 1) * -90));
            listener.redraw(particles);
        }
    }

    private double mutate(double val) {
        if (ThreadLocalRandom.current().nextDouble() > 0.1)
            return val;
        double mutation = 5;
        return ThreadLocalRandom.current().nextDouble(val - mutation, val + mutation);
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
                    ctr.rotate(-95).await();
                }
            } else {
                ctr.rotate(-85).await();
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
                newGen.add(new Particle(mutate(particle.getX()), particle.getY(), particle.getAngle(), 1 / (double) CAPACITY));
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

}
