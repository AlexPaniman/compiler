package nodes;

public class Break extends INode {
    @Override
    public String graphViz() {
        return id() + " [label = \"break\", shape = hexagon, color = red];\n";
    }
}
