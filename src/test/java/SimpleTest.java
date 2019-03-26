/*
import compile.Compiler;
import execute.VirtualMachine;
import lexer.Lexer;
import lexer.LexerException;
import nodes.INode;
import org.junit.Test;
import parse.ParseException;
import parse.Parser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static execute.Operand.*;
import static lexer.Token.NUM;
import static lexer.Token.STR;
import static lexer.Token.VAR;
*/
public class SimpleTest {
    /*@Test public void recursion() {
        VirtualMachine vm = new VirtualMachine(null, new Object[]{
                JMP, 36,
                STORE, 1,

                FETCH, 1,
                PUSH, 1,
                EQL,

                FETCH, 1,
                PUSH, 2,
                EQL,

                OR,

                JZ, 20,

                PUSH, 1,
                RET,

                FETCH, 1,
                PUSH, 1,
                SUB,
                INVOKE, 2,

                FETCH, 1,
                PUSH, 2,
                SUB,
                INVOKE, 2,

                ADD,

                RET,

                PUSH, 10,
                INVOKE, 2,
                RET
        });
        vm.execute();
    }

    @Test
    public void programExecution() throws IOException, LexerException, ParseException {
        String program = Files
                .lines(new File("src/main/resources/script.txt").toPath())
                .reduce("", (acc, line) -> acc + line + "\n");
        Lexer lexer = new Lexer(program);
        while (lexer.nextToken())
            if (lexer.token() == NUM || lexer.token() == VAR || lexer.token() == STR)
                System.out.println(lexer.token() + "(" + lexer.value() + ")");
            else
                System.out.println(lexer.token());
        System.out.println();
        lexer = new Lexer(program);
        INode main = new Parser(lexer).parse();

        System.out.println(main.graphViz());

        Compiler compiler = new Compiler();
        Object[] operands = compiler.compileProgram(main);
        for (int i = 0; i < operands.length; i++) {
            Object obj = operands[i];
            if (obj == PUSH || obj == FETCH || obj == JZ || obj == JNZ || obj == JMP || obj == STORE || obj == INVOKE) {
                System.out.println("<" + i + "> " + obj + " " + operands[++ i]);
            }
            else
                System.out.println("<" + i + "> " + obj);
        }
        System.out.println();
    }*/
}
