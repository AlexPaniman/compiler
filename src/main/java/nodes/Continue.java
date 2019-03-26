package nodes;

public class Continue extends INode {

    public Continue() {}

    @Override
    public String graphViz() {
        return id() + " [label = \"continue\", shape = hexagon, color = red];\n";
    }
}
