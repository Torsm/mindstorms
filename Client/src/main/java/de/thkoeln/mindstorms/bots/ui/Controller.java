package de.thkoeln.mindstorms.bots.ui;

import de.thkoeln.mindstorms.bots.UserControlledBot;
import de.thkoeln.mindstorms.bots.localization.Particle;
import de.thkoeln.mindstorms.client.environment.Disabled;
import de.thkoeln.mindstorms.server.controlling.EV3Controller;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import lejos.robotics.geometry.Line;
import lejos.robotics.mapping.SVGMapLoader;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Controller
 */
@Disabled
public class Controller implements Initializable {
    public Button chooseFileButton;
    public Canvas canvas;
    public Button two;
    public Button three;
    public Button four;
    public Button extra;

    private EV3Controller ctr;
    private List<Line> lines;
    private TreeMap<Double, Double> map;
    private List<Particle> particles;
    private int direction = 1;

    private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void chooseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image Files", "*.svg"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                lines = Arrays.asList(new SVGMapLoader(new FileInputStream(selectedFile)).readLineMap().flip().getLines());
                map = new TreeMap<>();
                lines.stream().filter(line -> line.y1 == line.y2).forEach(line -> map.put((double) line.x1, (double) line.y1));

                final int particleCount = 1000;
                final float y = lines.get(lines.size()-1).y1;
                final double believe = 1 / (double) particleCount;
                particles = IntStream.range(0, particleCount).mapToObj(i -> new Particle(400 * Math.random(), y, believe)).collect(Collectors.toList());

                draw();
            } catch (FileNotFoundException | XMLStreamException e) {
                e.printStackTrace();
            }
        }
    }

    private void draw() {
        final GraphicsContext graphics = canvas.getGraphicsContext2D();
        graphics.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        graphics.setFill(Color.BLACK);
        if (lines != null) {
            lines.forEach(line -> graphics.strokeLine(line.x1 * 2, line.y1 * 2, line.x2 * 2, line.y2 * 2));
        }
    }

    public void runTwo() {
        ctr.getSensorPosition().onComplete(result -> ctr.rotateFrontDistanceSensorMotor(90 - result.intValue()));
        ctr.clearScreen();

        service.scheduleWithFixedDelay(() -> {
            try {
                ctr.travel(100).get();
                if (correct() && particles != null) {
                    ctr.readFrontDistanceSensor().onComplete(result -> {
                        double dist = result.doubleValue() * 100.0;
                        draw();
                        final GraphicsContext graphics = canvas.getGraphicsContext2D();
                        particles.parallelStream().forEach(particle -> {
                            double particleDist = particle.getY() - map.floorEntry(particle.getX()).getValue();
                            double val = Math.min(dist, particleDist) / Math.max(dist, particleDist);
                            particle.adjustBelieve(val);
                            particle.adjustX(10 * direction);
                            synchronized (graphics) {
                                graphics.setFill(new Color(1 - particle.getBelieve(), particle.getBelieve(), 0, 1));
                                graphics.fillOval(particle.getX() * 2, particle.getY() * 2, 2, 2);
                            }
                        });

                        final double sum = particles.parallelStream().mapToDouble(Particle::getBelieve).sum();
                        particles.parallelStream().forEach(particle -> particle.adjustBelieve(1/sum));
                        particles.removeIf(particle -> particle.getX() < 0 || particle.getX() > 400);

                        System.out.println(particles.stream().mapToDouble(Particle::getBelieve).sum());

                        newGen();
                    }).get();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 1, 1, TimeUnit.MILLISECONDS);
    }

    private void newGen() {
        ArrayList<Particle> newGen = new ArrayList<>();
        particles.sort(Comparator.comparingDouble(Particle::getBelieve).reversed());
        int pos = 0;
        double sp = 2;
        while (newGen.size() < 1000) {
            double indP = 1.0 / (double) particles.size() * (sp - (2.0 * sp - 2.0) * (double) (pos - 1) / (double)(particles.size() - 1));
            if (Math.random() < indP) {
                newGen.add(particles.get(pos));
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

    private boolean correct() throws InterruptedException {
        boolean b = true;
        if (ctr.readColorSensor().get() < 0.02) {
            ctr.rotate(90).get();
            ctr.travel(30).get();
            if (ctr.readColorSensor().get() < 0.02) {
                ctr.travel(-60).get();
                if (ctr.readColorSensor().get() < 0.02) {
                    b = false;
                    final CountDownLatch latch = new CountDownLatch(2);
                    ctr.rotateFrontDistanceSensorMotor(180 * (direction *= -1)).onComplete(result -> latch.countDown());
                    ctr.travel(30).get();
                    ctr.rotate(87).get();
                    ctr.travel(50).onComplete(result -> latch.countDown());
                    latch.await();
                } else {
                    ctr.rotate(-90).get();
                }
            } else {
                ctr.rotate(-90).get();
            }
        }
        return b;
    }

    public void setController(int id) {
        ctr = UserControlledBot.getController(id);
    }

    public void runThree() throws InterruptedException {
        System.out.println(ctr.readColorSensor().get());
    }
}
