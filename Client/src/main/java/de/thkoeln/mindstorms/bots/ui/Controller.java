package de.thkoeln.mindstorms.bots.ui;

import de.thkoeln.mindstorms.bots.UserControlledBot;
import de.thkoeln.mindstorms.bots.localization.*;
import de.thkoeln.mindstorms.client.MindstormsClient;
import de.thkoeln.mindstorms.client.environment.dummy.EV3DummyController;
import de.thkoeln.mindstorms.server.controlling.EV3Controller;
import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import lejos.robotics.geometry.Line;
import lejos.robotics.mapping.SVGMapLoader;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static de.thkoeln.mindstorms.bots.localization.MonteCarloLocalization2D.loopCount;
import static de.thkoeln.mindstorms.bots.localization.MonteCarloLocalization2D.solveCount;
import static de.thkoeln.mindstorms.bots.localization.MonteCarloLocalization2D.stepCount;

/**
 * Controller
 */
public class Controller implements ParticleListener {
    public static final double SCALE = 3;
    public Canvas canvas;

    private EV3Controller ctr;
    private List<Line> lines;

    private LocalizationService service;

    private void drawMap() {
        final GraphicsContext graphics = canvas.getGraphicsContext2D();
        graphics.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        graphics.setLineWidth(1);
        graphics.setStroke(Color.BLACK);
        if (lines != null) {
            lines.forEach(line -> graphics.strokeLine(line.x1 * SCALE, line.y1 * SCALE, line.x2 * SCALE, line.y2 * SCALE));
        }
    }

    public void runTwo() {
        lines = loadMap("/image/street.svg");
        drawMap();
        TreeMap<Double, Double> map = new TreeMap<>(lines.stream().filter(line -> line.y1 == line.y2 && line.y1 != 70).collect(Collectors.toMap(Line::getX1, Line::getY1)));

        if (service != null)
            service.stop();

        MonteCarloLocalization1D monteCarloLocalization1D = new MonteCarloLocalization1D(ctr, this, map);
        this.service = monteCarloLocalization1D;
        monteCarloLocalization1D.start(lines.get(lines.size()-1).y1);
    }

    public void runThree() {
        lines = loadMap("/image/room.svg");
        drawMap();

        if (service != null)
            service.stop();

        MonteCarloLocalization2D monteCarloLocalization2D = new MonteCarloLocalization2D(ctr, this, lines);
        this.service = monteCarloLocalization2D;
        monteCarloLocalization2D.start();
    }

    public static List<Line> loadMap(String resource) {
        try {
            InputStream stream = MindstormsClient.class.getResourceAsStream(resource);
            return Arrays.asList(new SVGMapLoader(stream).readLineMap().flip().getLines());
        } catch (XMLStreamException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setController(int id) {
        ctr = UserControlledBot.getController(id);

        if (ctr instanceof EV3DummyController)  {
            new Thread(() -> {
                while (true) {
                    runThree();
                    service.awaitTermination();

                    if (solveCount > 0) {
                        System.out.println("Times located: " + solveCount);
                        System.out.println("Average loops: " + loopCount / solveCount);
                        System.out.println("Average steps: " + stepCount / solveCount);
                    }
                }
            }).start();
        }
    }

    @Override
    public void redraw(List<Particle> particles) {
        Platform.runLater(() -> {
            drawMap();
            final GraphicsContext graphics = canvas.getGraphicsContext2D();
            particles.forEach(particle -> particle.draw(graphics));
            if (ctr instanceof EV3DummyController) {
                ((EV3DummyController) ctr).getBot().draw(graphics);
            }
        });
    }
}
