package de.thkoeln.mindstorms.bots.ui;

import de.thkoeln.mindstorms.bots.UserControlledBot;
import de.thkoeln.mindstorms.bots.localization.MonteCarloLocalization;
import de.thkoeln.mindstorms.bots.localization.Particle;
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
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Controller
 */
@Disabled
public class Controller implements Initializable, MonteCarloLocalization.ParticleListener {
    public Button chooseFileButton;
    public Canvas canvas;
    public Button two;

    private EV3Controller ctr;
    private List<Line> lines;
    private TreeMap<Double, Double> map;
    private MonteCarloLocalization monteCarloLocalization;


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
        if (monteCarloLocalization != null)
            monteCarloLocalization.stop();

        monteCarloLocalization = new MonteCarloLocalization(ctr, this, map);
        monteCarloLocalization.start(lines.get(lines.size()-1).y1);
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
