package de.thkoeln.mindstorms.bots.ui;

import de.thkoeln.mindstorms.bots.UserControlledBot;
import de.thkoeln.mindstorms.bots.localization.MonteCarloLocalization;
import de.thkoeln.mindstorms.bots.localization.Particle;
import de.thkoeln.mindstorms.client.MindstormsClient;
import de.thkoeln.mindstorms.client.environment.properties.Disabled;
import de.thkoeln.mindstorms.server.controlling.EV3Controller;
import javafx.application.Platform;
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
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Controller
 */
@Disabled
public class Controller implements MonteCarloLocalization.ParticleListener {
    public Canvas canvas;

    private EV3Controller ctr;
    private List<Line> lines;
    private TreeMap<Double, Double> map;

    private MonteCarloLocalization monteCarloLocalization;

    private void draw() {
        final GraphicsContext graphics = canvas.getGraphicsContext2D();
        graphics.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        graphics.setFill(Color.BLACK);
        if (lines != null) {
            lines.forEach(line -> graphics.strokeLine(line.x1 * 2, line.y1 * 2, line.x2 * 2, line.y2 * 2));
        }
    }

    public void runTwo() {
        loadMap("/image/street.svg");

        if (monteCarloLocalization != null)
            monteCarloLocalization.stop();

        monteCarloLocalization = new MonteCarloLocalization(ctr, this, map);
        monteCarloLocalization.start(lines.get(lines.size()-1).y1);
    }

    public void runThree() {
        loadMap("/image/room.svg");


    }

    private void loadMap(String resource) {
        try {
            InputStream stream = MindstormsClient.class.getResourceAsStream(resource);
            lines = Arrays.asList(new SVGMapLoader(stream).readLineMap().flip().getLines());
            map = new TreeMap<>(lines.stream().filter(line -> line.y1 == line.y2 && line.y1 != 70).collect(Collectors.toMap(Line::getX1, Line::getY1)));

            draw();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }

    public void setController(int id) {
        ctr = UserControlledBot.getController(id);
    }

    @Override
    public void onNewGeneration(List<Particle> particles) {
        Platform.runLater(() -> {
            draw();
            final GraphicsContext graphics = canvas.getGraphicsContext2D();
            graphics.setFill(Color.RED);
            particles.forEach(particle -> graphics.fillOval(particle.getX() * 2, particle.getY() * 2 - 1, 3, 3));
        });
    }
}
