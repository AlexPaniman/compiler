package nodes;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DefineFunction extends INode {
    private String name;
    private INode[] variables;
    private INode body;

    public DefineFunction(String name, INode[] variables, INode body) {
        this.name = name;
        this.variables = variables;
        this.body = body;
    }

    public String name() {
        return name;
    }

    public INode[] variables() {
        return variables;
    }

    public INode body() {
        return body;
    }

    @Override
    public String graphViz() {
        StringBuilder graphViz = new StringBuilder();
        List<String> vars = Arrays
                .stream(variables)
                .map(node -> (Variable) node)
                .map(Variable::name)
                .collect(Collectors.toList());
        String label = "define[" + name + "(" + String.join(", ", vars) + ")]";
        graphViz.append(id()).append(" [label = \"").append(label).append("\", shape = octane, style = filled, color = black, fillcolor = green3];\n");
        graphViz.append(body.graphViz());
        graphViz.append(id()).append(" -- ").append(body.id()).append(";\n");
        return graphViz.toString();
    }
}
