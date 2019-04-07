package nodes;

import lexer.Token;

public class UnaryOperator extends INode {
    private Token operator;
    private INode operand;

    public UnaryOperator(Token operator, INode operand) {
        this.operator = operator;
        this.operand = operand;
    }

    public Token operator() {
        return operator;
    }

    public INode operand() {
        return operand;
    }

    public String graphViz() {
        String graphViz = "";
        graphViz += operand.graphViz();
        graphViz += id() + " [label = \"" + operator.toString().toLowerCase() + "\", shape = triangle, color = black, style = filled, fillcolor = yellow];\n";
        graphViz += id() + " -- " + operand.id()  + ";\n";
        return graphViz;
    }
}
