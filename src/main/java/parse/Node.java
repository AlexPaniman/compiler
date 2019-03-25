package parse;

import com.google.common.base.Objects;
import lexer.Token;

import static lexer.Token.IF;

public class Node {
    static Node EMPTY = new Node(null, "EMPTY");

    private Token token;
    private Object value;
    private Node[] nodes;

    Node(Token token, Object value, Node... nodes) {
        this.token = token;
        this.value = value;
        this.nodes = nodes;
    }

    public Node[] nodes() {
        return nodes;
    }

    public Object value() {
        return value;
    }

    public Token token() {
        return token;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(token, value, nodes);
    }

    public String graphViz() {
        StringBuilder builder = new StringBuilder();
        builder.append(hashCode()).append("[label = \"").append(token == null? "":token.toString().toLowerCase());
        if (value == "PROGRAM")
            builder.append("PROGRAM\", shape = polygon, sides = 7, peripheries = 2, color = green3, style = filled]\n");
        else if (value != null && token == null)
            builder.append(value.toString().toLowerCase()).append("\", shape = octagon, color = black, fillcolor = purple, style = filled]\n");
        else if (value != null)
            builder.append(token == null? "":"(").append(value.toString().toLowerCase()).append(token == null? "":")").append("\"]\n");
        else
            builder.append("\", shape = square, color = blue4, style = filled, fillcolor = green]\n");
        for (Node node: nodes)
            builder.append(node.graphViz());
        for (Node node: nodes)
            builder.append(hashCode()).append(" -> ").append(node.hashCode()).append("\n");
        return builder.toString();
    }
}
