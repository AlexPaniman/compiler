import compile.Compiler;
import execute.VirtualMachine;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import lexer.Lexer;
import parse.Node;
import javafx.scene.image.Image;
import parse.Parser;

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

    private String repeat(int num) {
        return IntStream.range(0, num).mapToObj(n -> (Object) n).reduce("", (acc, id) -> acc + " ").toString();
    }

    private int len(int num) {
        return String.valueOf(num).length();
    }

    private String complete(int num, int i) {
        return repeat(num - len(i)) + i;
    }

    private String complete(Object[] arr, int i) {
        return repeat(len(arr.length) - len(i)) + i;
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
                System.out.println("\t" + complete(3, count) + ":= " + lexer.token() + "(" + lexer.value() + ")");
            else
                System.out.println("\t" + complete(3, count) + ":= " + lexer.token());
            count ++;
        }
        lexer = new Lexer(program);
        Node main = new Parser(lexer).parse();

        Files.write(
                new File("src/main/resources/graph.dot").toPath(),
                "digraph G {\n".concat(main.graphViz()).concat("}").getBytes()
        );

        Process process = Runtime.getRuntime().exec("dot -Tpng src/main/resources/graph.dot -o src/main/resources/graph.png");
        while (process.isAlive())
            Thread.sleep(1);

        Image image = new Image(new File("src/main/resources/graph.png").toURI().toURL().toString());
        ImageView imageView = new ImageView(image);

        Group root = new Group();

        Scene scene = new Scene(root, image.getWidth(), image.getHeight());

        root.getChildren().add(imageView);

        primaryStage.setTitle("AST (Abstract Syntax Tree)");
        primaryStage.setResizable(false);

        primaryStage.setScene(scene);
        primaryStage.show();

        System.out.println("\nBYTE-CODE:");
        Compiler compiler = new Compiler();
        Object[] operands = compiler.compileProgram(main);
        for (int i = 0; i < operands.length; i++) {
            Object obj = operands[i];
            if (obj == PUSH || obj == FETCH || obj == JZ || obj == JNZ || obj == JMP || obj == STORE || obj == INVOKE) {
                System.out.println("\t" + complete(operands, i) + ":= " + obj + " " + operands[++ i]);
            }
            else
                System.out.println("\t" + complete(operands, i) + ":= " + obj);
        }
        VirtualMachine vm = new VirtualMachine(operands);
        vm.execute();
        System.out.println("\n" + vm.status());
    }
}
