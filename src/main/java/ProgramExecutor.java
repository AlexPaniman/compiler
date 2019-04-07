import compile.Compiler;
import execute.VirtualMachine;
import executor.NativeExecutor;
import executor.VoidFunction;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.stage.Stage;
import lexer.Lexer;
import nodes.INode;
import parse.Parser;

import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.util.stream.IntStream;

import static execute.Operand.*;
import static lexer.Token.NUM;
import static lexer.Token.STR;
import static lexer.Token.VAR;

public class ProgramExecutor extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    private String complete(int i) {
        return IntStream
                .range(0, /*length = */ 4 - String.valueOf(i).length())
                .mapToObj(n -> (Object) n)
                .reduce("", (acc, id) -> acc + " ")
                .toString() + i;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        String program = Files
                .lines(new File("src/main/resources/script.txt").toPath())
                .reduce("", (acc, line) -> acc + line + "\n");

        Lexer lexer = new Lexer(program);
        System.out.println("TOKENS:");
        int count = 0;
        while (lexer.nextToken()) {
            if (lexer.token() == NUM || lexer.token() == VAR || lexer.token() == STR)
                System.out.println("\t" + complete(count) + ":= " + lexer.token() + "(" + lexer.value() + ")");
            else
                System.out.println("\t" + complete(count) + ":= " + lexer.token());
            count++;
        }
        lexer = new Lexer(program);
        INode main = new Parser(lexer).parse();

        Files.write(
                new File("src/main/resources/graph.gv").toPath(),
                main.resultViz().getBytes()
        );

        Process process = Runtime
                .getRuntime()
                .exec("dot -Tpng src/main/resources/graph.gv -o src/main/resources/graph.png");
        while (process.isAlive())
            Thread.sleep(1);

        Image image = new Image(getClass().getResource("graph.png").toString());
        ImageView imageView = new ImageView(image);

        Group root = new Group();

        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();

        double x = image.getHeight() / (dimension.getHeight() - 20);
        double y = image.getWidth() / (dimension.getWidth() - 20);
        double res = Math.max(x, y);

        imageView.setFitWidth(image.getWidth() / res);
        imageView.setFitHeight(image.getHeight() / res);

        root.getChildren().add(imageView);

        primaryStage.setTitle("AST (Abstract Syntax Tree)");
        primaryStage.getIcons().add(new Image(getClass().getResource("icon.png").toString()));
        primaryStage.setResizable(false);

        imageView.onMouseDraggedProperty().setValue(
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

        Scene scene = new Scene(root, dimension.getWidth() - 200, dimension.getHeight() - 200);

        imageView.onScrollProperty().setValue(event -> {
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
                primaryStage.setFullScreen(true);
        });

        primaryStage.setResizable(true);
        primaryStage.setScene(scene);
        primaryStage.show();

        System.out.println("\nBYTE-CODE:");

        Compiler compiler = new Compiler();
        compiler.compileProgram(main);

        NativeExecutor executor = new NativeExecutor(compiler.nativeFunctions())
                .func("print", 1, (VoidFunction)  arr -> System.out.println(arr[0]));

        Object[] operands = compiler.program();
        for (int i = 0; i < operands.length; i++) {
            Object obj = operands[i];
            if (obj == PUSH || obj == FETCH || obj == JZ || obj == JNZ || obj == JMP || obj == STORE || obj == INVOKE || obj == NATIVE) {
                System.out.println("\t" + complete(i) + ":= " + obj + " " + operands[++i]);
            } else
                System.out.println("\t" + complete(i) + ":= " + obj);
        }
        VirtualMachine virtualMachine = new VirtualMachine(executor, operands);

        System.out.println("\nCONSOLE:");
        virtualMachine.executeAll();

        System.out.println("\n" + virtualMachine.status());
    }
}
