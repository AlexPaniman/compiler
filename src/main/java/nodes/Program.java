package nodes;

public class Program extends INode {
    private INode program;

    public Program(INode program) {
        this.program = program;
    }

    public INode program() {
        return program;
    }

    @Override
    public String graphViz() {
        String graphViz = "";
        graphViz += program.graphViz();
        graphViz += id() + " [label = \"program\", shape = tripleoctagon];\n";
        graphViz += id() + " -- " + program.id() + ";\n";
        return graphViz;
    }
}
