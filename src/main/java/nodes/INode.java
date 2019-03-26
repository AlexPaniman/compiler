package nodes;

public abstract class INode {
    static int counter = 0;
    private final int id = counter ++;

    public abstract String graphViz();

    public int id() {
        return id;
    }

    public String resultViz() {
        return "graph A\n{" +
                    graphViz() +
                "}";
    }
}
