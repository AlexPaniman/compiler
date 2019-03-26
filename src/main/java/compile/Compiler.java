package compile;

import execute.Operand;
import executor.NativeExecutor;
import lexer.Token;
import nodes.*;

import java.util.*;

import static execute.Operand.*;

public class Compiler {
    private List<Object> program;
    private Map<String, Integer> variables;
    private Map<String, Integer> functions;
    private NativeExecutor nativeExecutor;
    private int count;

    public Compiler(NativeExecutor nativeExecutor) {
        this.program = new ArrayList<>();
        this.variables = new HashMap<>();
        this.functions = new HashMap<>();
        this.nativeExecutor = nativeExecutor;
        this.count = 0;
    }

    private void gen(Object... ops) {
        program.addAll(Arrays.asList(ops));
    }

    private void binary(Operand operand, BinaryOperator node) throws CompilerException {
        compile(node.first());
        compile(node.second());
        gen(operand);
    }

    private void math(BinaryOperator node) throws CompilerException {
        Token token = node.operator();
        if (token == Token.XOR)
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
    }

    private void compile(INode node) throws CompilerException {
        if (node instanceof Program)
            compile(((Program) node).program());
        if (node instanceof BinaryOperator)
            math((BinaryOperator) node);
        else if (node instanceof Block)
            for (INode n : ((Block) node).nodes())
                compile(n);
        else if (node instanceof Variable)
            gen(FETCH, variables.get(((Variable) node).name()));
        else if (node instanceof Constant)
            gen(PUSH, ((Constant) node).value());
        else if (node instanceof Assign) {
            Assign assign = (Assign) node;
            compile(assign.expression());
            gen(STORE, variables.computeIfAbsent(assign.variable(), n -> count++));
        } else if (node instanceof For) {
            For forLoop = (For) node;
            compile(forLoop.initializer());
            int loop = program.size();
            compile(forLoop.condition());
            gen(JZ, 0);
            int index = program.size() - 1;
            compile(forLoop.body());
            compile(forLoop.iterator());
            gen(JMP, loop);
            program.set(index, program.size());
        } else if (node instanceof While) {
            While whileLoop = (While) node;
            int loop = program.size();
            compile(whileLoop.condition());
            gen(JZ, 0);
            int index = program.size() - 1;
            compile(whileLoop.body());
            gen(JMP, loop);
            program.set(index, program.size());
        } else if (node instanceof DoWhile) {
            DoWhile doWhile = (DoWhile) node;
            compile(doWhile.condition());
            int loop = program.size();
            compile(doWhile.body());
            gen(JZ, 0);
            int index = program.size() - 1;
            compile(doWhile.body());
            gen(JMP, loop);
            program.set(index, program.size());
        } else if (node instanceof ShortIf) {
            ShortIf shortIf = (ShortIf) node;
            compile(shortIf.condition());
            gen(JZ, 0);
            int index = program.size() - 1;
            compile(shortIf.thenNode());
            program.set(index, program.size());
        } else if (node instanceof ExtendedIf) {
            ExtendedIf extendedIf = (ExtendedIf) node;
            compile(extendedIf.condition());
            gen(JZ, 0);
            int index = program.size() - 1;
            compile(extendedIf.thenNode());
            gen(JMP, 0);
            program.set(index, program.size());
            index = program.size() - 1;
            compile(extendedIf.elseNode());
            program.set(index, program.size());
        } else if (node instanceof DefineFunction) {
            DefineFunction defineFunc = (DefineFunction) node;
            gen(JMP, 0);
            functions.put(defineFunc.name(), program.size());
            int index = program.size() - 1;
            for (INode n : defineFunc.variables())
                gen(STORE, variables.computeIfAbsent(((Variable) n).name(), nodeC -> count++));
            if (defineFunc.body() instanceof Block)
                compile(defineFunc.body());
            else {
                compile(defineFunc.body());
                gen(RET);
            }
            program.set(index, program.size());
        } else if (node instanceof Return) {
            Return ret = (Return) node;
            if (ret.expression() != null)
                compile(ret.expression());
            else
                gen((Object) null);
            gen(RET);
        } else if (node instanceof FunctionCall) {
            FunctionCall call = (FunctionCall) node;
            INode[] nodes = call.variables();
            for (int i = nodes.length - 1; i >= 0; i--)
                compile(nodes[i]);
            if (functions.containsKey(call.name()))
                gen(INVOKE, functions.get(call.name()));
            else if (nativeExecutor != null) try {
                gen(NATIVE, nativeExecutor.get(call.name()));
            } catch (NoSuchMethodException exc) {
                throw new CompilerException("Can't find or access " + call.name() + " function!");
            } else throw new CompilerException("Can't find or access " + call.name() + " function!");
            if (!call.useReturn())
                gen(POP);
        }
    }

    public Object[] compileProgram(INode main) throws CompilerException {
        compile(main);
        gen(RET);
        return program.toArray();
    }
}
