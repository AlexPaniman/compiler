package compile;

import execute.Operand;
import lexer.Token;
import parse.Node;

import java.util.*;

import static execute.Operand.*;
import static lexer.Token.*;

public class Compiler {
    private List<Object> program;
    private Map<String, Integer> variables;
    private Map<String, Integer> functions;
    private int count;

    public Compiler() {
        this.program = new ArrayList<>();
        this.variables = new HashMap<>();
        this.functions = new HashMap<>();
        this.count = 0;
    }

    private void gen(Object... ops) {
        program.addAll(Arrays.asList(ops));
    }

    private void binary(Operand operand, Node node) {
        compile(node.nodes()[0]);
        compile(node.nodes()[1]);
        gen(operand);
    }

    private void compile(Node node) {
        Token token = node.token();
        if (token == null)
            for (Node n : node.nodes())
                compile(n);
        else if (token == VAR)
            gen(FETCH, variables.get(node.value().toString()));
        else if (token == NUM || token == STR)
            gen(PUSH, node.value());
        else if (token == Token.XOR)
            binary(Operand.XOR, node);
        else if (token == Token.EQL)
            binary(Operand.EQL, node);
        else if (token == Token.AND)
            binary(Operand.AND, node);
        else if (token == Token.SUM)
            binary(Operand.ADD, node);
        else if (token == Token.SUB)
            binary(Operand.SUB, node);
        else if (token == Token.MUL)
            binary(Operand.MUL, node);
        else if (token == Token.DIV)
            binary(Operand.DIV, node);
        else if (token == Token.MOD)
            binary(Operand.MOD, node);
        else if (token == Token.POW)
            binary(Operand.POW, node);
        else if (token == Token.LT)
            binary(Operand.LT, node);
        else if (token == Token.BT)
            binary(Operand.BT, node);
        else if (token == Token.LE)
            binary(Operand.LE, node);
        else if (token == Token.BE)
            binary(Operand.BE, node);
        else if (token == Token.OR)
            binary(Operand.OR, node);
        else if (token == ASSIGN) {
            compile(node.nodes()[1]);
            gen(STORE, variables.computeIfAbsent((String) node.nodes()[0].value(), n -> count++));
        } else if (token == FOR) {
            compile(node.nodes()[0]);
            int loop = program.size();
            compile(node.nodes()[1]);
            gen(JZ, 0);
            int index = program.size() - 1;
            compile(node.nodes()[2]);
            compile(node.nodes()[3]);
            gen(JMP, loop);
            program.set(index, program.size());
        } else if (token == WHILE) {
            int loop = program.size();
            compile(node.nodes()[0]);
            gen(JZ, 0);
            int index = program.size() - 1;
            compile(node.nodes()[1]);
            gen(JMP, loop);
            program.set(index, program.size());
        } else if (token == DO_WHILE) {
            compile(node.nodes()[0]);
            int loop = program.size();
            compile(node.nodes()[0]);
            gen(JZ, 0);
            int index = program.size() - 1;
            compile(node.nodes()[1]);
            gen(JMP, loop);
            program.set(index, program.size());
        } else if (token == IF) {
            int len = node.nodes().length;
            compile(node.nodes()[0]);
            gen(JZ, 0);
            int index = program.size() - 1;
            if (len == 2) {
                compile(node.nodes()[1]);
                program.set(index, program.size());
            } else {
                compile(node.nodes()[1]);
                gen(JMP, 0);
                program.set(index, program.size());
                index = program.size() - 1;
                compile(node.nodes()[2]);
                program.set(index, program.size());
            }
        } else if (token == LAMBDA) {
            gen(JMP, 0);
            functions.put(node.value().toString(), program.size());
            int index = program.size() - 1;
            for (Node n : node.nodes()[0].nodes())
                gen(STORE, variables.computeIfAbsent((String) n.value(), nodeC -> count++));
            if (node.nodes()[1].nodes().length == 1 && node.nodes()[1].nodes()[0].token() != RETURN) {
                compile(node.nodes()[1]);
                gen(RET);
            } else
                compile(node.nodes()[1]);
            program.set(index, program.size());
        } else if (token == RETURN) {
            compile(node.nodes()[0]);
            gen(RET);
        } else if (token == CALL) {
            Node[] nodes = node.nodes();
            for (int i = nodes.length - 1; i >= 0; i--)
                compile(nodes[i]);
            gen(INVOKE, functions.get(node.value().toString()));
        }
    }

    public Object[] compileProgram(Node main) {
        compile(main);
        gen(RET);
        return program.toArray();
    }
}
