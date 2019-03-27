package nodes;

import com.google.common.base.Objects;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lexer.Token;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class BinaryOperator extends INode {
    private Token operator;
    private INode first;
    private INode second;

    private static Map<String, String> seq;

    static {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        Type map = new TypeToken<Map<String, String>>() {
        }.getType();
        try {
            seq = gson.fromJson(
                    Files
                            .lines(new File("src/main/resources/sequence.json").toPath())
                            .reduce("", (acc, line) -> acc + line + "\n"),
                    map
            );
            Map<String, String> temp = new HashMap<>();
            for (Map.Entry<String, String> entry: seq.entrySet())
                temp.put(entry.getValue(), entry.getKey());
            seq = temp;
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    public BinaryOperator(Token operator, INode first, INode second) {
        this.operator = operator;
        this.first = first;
        this.second = second;
    }

    public Token operator() {
        return operator;
    }

    public INode first() {
        return first;
    }

    public INode second() {
        return second;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(operator, first, second);
    }

    public String graphViz() {
        String graphViz = "";
        graphViz += first.graphViz();
        graphViz += second.graphViz();
        graphViz += id() + " [label = \"" + seq.get(operator.toString()) + "\", shape = triangle, color = black, style = filled, fillcolor = yellow];\n";
        graphViz += id() + " -- " + first.id()  + ";\n";
        graphViz += id() + " -- " + second.id() + ";\n";
        return graphViz;
    }
}
