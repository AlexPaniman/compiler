import execute.VirtualMachine;
import execute.VirtualMachineException;
import executor.NativeExecutor;
import executor.VoidFunction;
import guru.nidi.graphviz.engine.Engine;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.Rasterizer;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import lexer.Lexer;
import lexer.LexerException;
import nodes.INode;
import parse.ParseException;
import parse.Parser;
import java.util.stream.IntStream;

import static lexer.Token.NUM;
import static lexer.Token.STR;
import static lexer.Token.VAR;

public class MainController {
    @FXML private Text console;
    @FXML private TextArea program;

    @FXML private CheckBox showGraph;
    @FXML private CheckBox showTokens;
    @FXML private CheckBox showByteCode;

    @FXML private ProgressBar progressBar;

    private String complete(int i) {
        return IntStream
                .range(0, /*length = */ 4 - String.valueOf(i).length())
                .mapToObj(n -> (Object) n)
                .reduce("", (acc, id) -> acc + " ")
                .toString() + i;
    }

    @FXML public void execute() throws LexerException, ParseException, VirtualMachineException {
        progressBar.setProgress(0.0);
        Parser parser = new Parser(new Lexer(program.getText()));
        INode main = parser.parse();
        progressBar.setProgress(0.33);
        if (showGraph.isSelected()) {
            Image image = SwingFXUtils.toFXImage(
                    Graphviz
                            .fromString(main.resultViz())
                            .engine(Engine.DOT)
                            .scale(3)
                            .render(Format.PNG)
                            .toImage(),
                    null
            );
            ImageView imageView = new ImageView(image);

            Group root = new Group();

            double x = image.getHeight() / 400;
            double y = image.getWidth() / 600;

            imageView.setFitWidth(image.getWidth() / Math.max(x, y));
            imageView.setFitHeight(image.getHeight() / Math.max(x, y));

            root.getChildren().add(imageView);

            Stage graph = new Stage();

            Scene scene = new Scene(root, imageView.getFitWidth(), imageView.getFitHeight());

            scene.onMouseDraggedProperty().setValue(
                    new EventHandler<MouseEvent>() {
                        private double lastX;
                        private double lastY;

                        private long nano = System.nanoTime();

                        @Override
                        public void handle(MouseEvent event) {
                            if (System.nanoTime() - nano < 1e8) {
                                imageView.setX(imageView.getX() + event.getX() - lastX);
                                imageView.setY(imageView.getY() + event.getY() - lastY);
                            }
                            lastX = event.getX();
                            lastY = event.getY();
                            nano = System.nanoTime();
                        }
                    }
            );

            scene.onScrollProperty().setValue(event -> {
                double height = imageView.getFitHeight();
                double width = imageView.getFitWidth();
                double delta = .9;

                if (event.getTextDeltaY() > 0)
                    delta = 1 / delta;

                double x2 = scene.getWidth() / 2;
                double y2 = scene.getHeight() / 2;

                double x1 = imageView.getX();
                double y1 = imageView.getY();

                imageView.setFitHeight(height * delta);
                imageView.setFitWidth(width * delta);

                x1 -= delta * (x2 - x1) + x1 - x2;
                y1 -= delta * (y2 - y1) + y1 - y2;

                imageView.setX(x1);
                imageView.setY(y1);
            });

            scene.onKeyPressedProperty().setValue(event -> {
                if (event.getCode() == KeyCode.F11)
                    graph.setFullScreen(true);
            });

            graph.setTitle("AST (Abstract Syntax Tree)");
            graph.getIcons().add(new Image(getClass().getResource("icon.png").toString()));
            graph.setResizable(true);
            graph.setScene(scene);
            graph.show();
        }

        compile.Compiler compiler = new compile.Compiler();
        compiler.compileProgram(main);

        NativeExecutor executor = new NativeExecutor(compiler.nativeFunctions())
                .func("println", 1, (VoidFunction) arr -> Platform.runLater(() ->
                        console.setText(console.getText() + arr[0].toString() + "\n")))
                .func("print", 1, (VoidFunction) arr -> Platform.runLater(() ->
                        console.setText(console.getText() + arr[0].toString())));

        StringBuilder builder = new StringBuilder();

        if (showTokens.isSelected()) {
            Lexer lexer = new Lexer(program.getText());

            builder.append("TOKENS:\n");
            int count = 0;
            while (lexer.nextToken()) {
                if (lexer.token() == NUM || lexer.token() == VAR || lexer.token() == STR)
                    builder
                            .append("\t")
                            .append(complete(count))
                            .append(":= ")
                            .append(lexer.token())
                            .append("(")
                            .append(lexer.value())
                            .append(")\n");
                else
                    builder
                            .append("\t")
                            .append(complete(count))
                            .append(":= ")
                            .append(lexer.token())
                            .append("\n");
                count++;
            }
            builder.append("\n");
            progressBar.setProgress(0.66);
        }

        Object[] operands = compiler.program();
        if (showByteCode.isSelected()) {
            builder.append("BYTE-CODE:\n");
            for (int i = 0; i < operands.length; i++) {
                Object obj = operands[i];
                Class c = operands.length > i + 1 ? operands[i + 1].getClass() : null;
                if (c == String.class || c == Integer.class || c == Double.class) {
                    builder
                            .append("\t")
                            .append(complete(i))
                            .append(":= ")
                            .append(obj)
                            .append(" ")
                            .append(operands[++i])
                            .append("\n");
                } else
                    builder
                            .append("\t")
                            .append(complete(i))
                            .append(":= ")
                            .append(obj)
                            .append("\n");
            }
            builder.append("\n");
            progressBar.setProgress(80.0);
        }
        VirtualMachine virtualMachine = new VirtualMachine(executor, operands);

        builder.append("CONSOLE:\n");

        virtualMachine.executeAll();

        console.setText(builder.toString());
        progressBar.setProgress(100.0);
    }
}
