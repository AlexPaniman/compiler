package parse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lexer.Lexer;
import lexer.LexerException;
import lexer.Token;
import nodes.*;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import static lexer.Token.*;

public class Parser {
    private Lexer lexer;
    private Map<String, Integer> priority;

    public Parser(Lexer lexer) throws LexerException {
        this.lexer = lexer;
        this.lexer.nextToken();
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        Type map = new TypeToken<Map<String, Integer>>() {
        }.getType();
        try {
            Reader is = new InputStreamReader(getClass().getResourceAsStream("/priority.json"));
            StringBuilder builder = new StringBuilder();
            for (int c; (c = is.read()) != -1; )
                builder.append((char) c);
            priority = gson.fromJson(builder.toString(), map);
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    private void test(Token test, String message) throws ParseException {
        if (lexer.token() != test)
            throw new ParseException(message);
    }

    private void next(Token test, String message) throws LexerException, ParseException {
        test(test, message);
        lexer.nextToken();
    }

    //Parse term
    private INode term() throws LexerException, ParseException {
        //Not operator
        if (lexer.token() == NOT) {
            lexer.nextToken();
            return new UnaryOperator(NOT, term());
        }
        //Function or Variable
        if (lexer.token() == VAR) {
            //Get function or variable name
            String name = lexer.value();
            lexer.nextToken();
            if (lexer.token() == LRB) {
                //Function
                List<INode> variables = new ArrayList<>();
                while (lexer.token() != RRB) {
                    lexer.nextToken();
                    INode expr = expr();
                    if (expr != null)
                        variables.add(expr);
                }
                lexer.nextToken();
                return new FunctionCall(name, variables.toArray(new INode[0]), true);
            }
            //Variable
            return new Variable(name);
        }
        //String literal
        if (lexer.token() == STR) {
            String value = lexer.value();
            lexer.nextToken();
            return new Constant(value);
        }
        //Number literal
        if (lexer.token() == NUM) {
            String number = lexer.value();
            lexer.nextToken();
            try {
                return new Constant(Integer.valueOf(number));
            } catch (NumberFormatException exc) {
                return new Constant(Double.valueOf(number));
            }
        }
        //Expression in brackets
        if (lexer.token() == LRB) {
            return parentExpr();
        }
        return null;
    }

    //Calculate expression
    private INode expr() throws LexerException, ParseException {
        Stack<Object> expression = new Stack<>();
        Stack<Object> operators = new Stack<>();
        while (true) {
            INode term = term();
            //Test for empty expression
            if (term == null)
                return null;
            expression.push(term);

            Integer exprPriority = priority.get(lexer.token().toString());
            if (exprPriority == null)
                break;

            Integer stackPriority = operators.empty() ? -1 : priority.get(operators.peek().toString());

            if (exprPriority <= stackPriority) do
                expression.push(operators.pop());
            while (!operators.empty() && priority.get(operators.peek().toString()) >= exprPriority);

            operators.push(lexer.token());

            lexer.nextToken();
        }
        while (!operators.empty())
            expression.push(operators.pop());
        Stack<Object> execute = new Stack<>();
        for (Object obj : expression) {
            if (obj instanceof INode)
                execute.push(obj);
            else {
                INode second = (INode) execute.pop();
                INode first = (INode) execute.pop();
                execute.push(new BinaryOperator((Token) obj, first, second));
            }
        }
        return (INode) execute.pop();
    }

    //Parse expression in brackets
    private INode parentExpr() throws ParseException, LexerException {
        next(LRB, "( expected!");
        INode expr = expr();
        next(RRB, ") expected!");
        return expr;
    }

    //Parse statement
    private INode statement() throws LexerException, ParseException {
        if (lexer.token() == null)
            return null;
        if (lexer.token() == IF) {
            lexer.nextToken();
            INode condition = parentExpr();
            INode thenStmt = statement();
            if (lexer.token() == ELSE) {
                lexer.nextToken();
                return new ExtendedIf(condition, thenStmt, statement());
            } else
                return new ShortIf(condition, thenStmt);
        }
        if (lexer.token() == WHILE || lexer.token() == DO_WHILE) {
            Token token = lexer.token();
            lexer.nextToken();
            INode condition = parentExpr();
            INode body = statement();
            if (token == WHILE)
                return new While(condition, body);
            return new DoWhile(condition, body);
        }
        if (lexer.token() == FOR) {
            lexer.nextToken();
            next(LRB, "( expected");
            INode init = statement();
            INode condition = expr();
            next(SEMICOLON, "; expected");
            INode iterator = statement();
            INode body = statement();
            return new For(init, condition, iterator, body);
        }
        //Code block
        if (lexer.token() == LCB) {
            lexer.nextToken();
            List<INode> nodes = new ArrayList<>();
            while (lexer.token() != RCB)
                nodes.add(statement());
            lexer.nextToken();
            return new Block(nodes.toArray(new INode[0]));
        }
        //Variable assign or function call
        if (lexer.token() == VAR) {
            String var = lexer.value();
            lexer.nextToken();
            if (lexer.token() == ASSIGN) {
                lexer.nextToken();
                INode expr = expr();
                lexer.nextToken();
                return new Assign(var, expr);
            } else {
                test(LRB, "( expected");
                List<INode> vars = new ArrayList<>();
                //Parse function arguments
                for (; lexer.token() != RRB; ) {
                    lexer.nextToken();
                    INode expr = expr();
                    if (expr != null)
                        vars.add(expr);
                }
                lexer.nextToken();
                if (lexer.token() != LAMBDA) {
                    lexer.nextToken();
                    return new FunctionCall(var, vars.toArray(new INode[0]), false);
                }
                lexer.nextToken();
                INode body;
                if (lexer.token() == LCB)
                    //Long function. In this function return expected
                    body = statement();
                else {
                    //Short function with expression body
                    body = expr();
                    next(SEMICOLON, "; expected");
                }
                return new DefineFunction(var, vars.toArray(new INode[0]), body);
            }
        }
        if (lexer.token() == RETURN) {
            lexer.nextToken();
            INode expr = expr();
            next(SEMICOLON, "; expected");
            return new Return(expr);
        }
        if (lexer.token() == CONTINUE) {
            lexer.nextToken();
            next(SEMICOLON, "; expected");
            return new Continue();
        }
        if (lexer.token() == BREAK) {
            lexer.nextToken();
            next(SEMICOLON, "; expected");
            return new Continue();
        }
        return null;
    }

    //Parse program
    public INode parse() throws LexerException, ParseException {
        List<INode> nodes = new ArrayList<>();
        INode node;
        while ((node = statement()) != null)
            nodes.add(node);
        return new Program(new Block(nodes.toArray(new INode[0])));
    }
}
