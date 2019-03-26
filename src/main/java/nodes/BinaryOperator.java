package nodes;

import com.google.common.base.Objects;
import lexer.Token;

public class BinaryOperator extends INode {
    private Token operator;
    private INode first;
    private INode second;

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
        graphViz += id() + " [label = \"" + operator.toString().toLowerCase() + "\", shape = triangle, color = black, style = filled, fillcolor = yellow];\n";
        graphViz += id() + " -- " + first.id()  + ";\n";
        graphViz += id() + " -- " + second.id() + ";\n";
        return graphViz;
    }
}
