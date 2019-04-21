package nodes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lexer.Lexer;
import lexer.Token;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class BinaryOperator extends INode {
    private Token operator;
    private INode first;
    private INode second;

    private Map<String, String> seq;

    public BinaryOperator(Token operator, INode first, INode second) {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        Type map = new TypeToken<Map<String, String>>() {}.getType();
        try {
            Reader is = new InputStreamReader(getClass().getResourceAsStream("/sequence.json"));
            StringBuilder builder = new StringBuilder();
            for (int c; (c = is.read()) != -1; )
                builder.append((char) c);
            seq = gson.fromJson(builder.toString(), map);
            Map<String, String> temp = new HashMap<>();
            for (Map.Entry<String, String> entry: seq.entrySet())
                temp.put(entry.getValue(), entry.getKey());
            seq = temp;
        } catch (IOException io) {
            io.printStackTrace();
        }
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
