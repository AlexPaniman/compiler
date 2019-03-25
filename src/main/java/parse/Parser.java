package parse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lexer.Lexer;
import lexer.LexerException;
import lexer.Token;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import static lexer.Token.*;

public class Parser {
    private Lexer lexer;
    private static Map<String, Integer> priority;

    static {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        Type map = new TypeToken<Map<String, Integer>>() {
        }.getType();
        try {
            priority = gson.fromJson(
                    Files
                            .lines(new File("src/main/resources/priority.json").toPath())
                            .reduce("", (acc, line) -> acc + line + "\n"),
                    map
            );
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    public Parser(Lexer lexer) throws LexerException {
        this.lexer = lexer;
        this.lexer.nextToken();
    }


    private Node term() throws LexerException, ParseException {
        if (lexer.token() == VAR) {
            Object value = lexer.value();
            lexer.nextToken();
            if (lexer.token() == LRB) {
                List<Node> exp = new ArrayList<>();
                while (lexer.token() != RRB) {
                    lexer.nextToken();
                    exp.add(expr());
                }
                lexer.nextToken();
                return new Node(CALL, value, exp.toArray(new Node[0]));
            }
            return new Node(VAR, value);
        }
        if (lexer.token() == STR) {
            Object value = lexer.value();
            lexer.nextToken();
            return new Node(STR, value);
        }
        if (lexer.token() == NUM) {
            String number = lexer.value();
            lexer.nextToken();
            try {
                return new Node(NUM, Integer.valueOf(number));
            } catch (NumberFormatException exc) {
                return new Node(NUM, Double.valueOf(number));
            }
        }
        if (lexer.token() == LRB) {
            return parentExpr();
        }
        return null;
    }

    private Node expr() throws LexerException, ParseException {
        Stack<Object> expression = new Stack<>();
        Stack<Object> operators = new Stack<>();
        while (true) {
            Node term = term();
            if (term == null)
                return Node.EMPTY;
            expression.push(term);

            Integer exprPriority = priority.get(lexer.token().toString());
            if (exprPriority == null)
                break;

            Integer stackPriority = operators.empty() ? -1 : priority.get(operators.peek().toString());

            if (exprPriority <= stackPriority) do
                expression.push(operators.pop());
            while (!operators.empty() && Parser.priority.get(operators.peek().toString()) >= exprPriority);

            operators.push(lexer.token());

            lexer.nextToken();
        }
        while (!operators.empty())
            expression.push(operators.pop());
        Stack<Object> execute = new Stack<>();
        for (Object obj : expression) {
            if (obj.getClass() == Node.class)
                execute.push(obj);
            else {
                Node second = (Node) execute.pop();
                Node first = (Node) execute.pop();
                execute.push(new Node((Token) obj, null, first, second));
            }
        }
        return (Node) execute.pop();
    }

    private Node parentExpr() throws ParseException, LexerException {
        if (lexer.token() != LRB)
            throw new ParseException("( expected!");
        lexer.nextToken();
        Node expr = expr();
        if (lexer.token() != RRB)
            throw new ParseException(") expected!");
        lexer.nextToken();
        return expr;
    }

    private Node statement() throws LexerException, ParseException {
        if (lexer.token() == null)
            return null;
        if (lexer.token() == IF) {
            lexer.nextToken();
            Node condition = parentExpr();
            Node thenStmt = statement();
            if (lexer.token() == ELSE) {
                lexer.nextToken();
                return new Node(IF, null, condition, thenStmt, statement());
            } else {
                return new Node(IF, null, condition, thenStmt);
            }
        }
        if (lexer.token() == WHILE || lexer.token() == DO_WHILE) {
            Token token = lexer.token();
            lexer.nextToken();
            Node condition = parentExpr();
            Node stmt = statement();
            return new Node(token, null, condition, stmt);
        }
        if (lexer.token() == FOR) {
            lexer.nextToken();
            lexer.nextToken();
            Node init = statement();
            Node condition = expr();
            lexer.nextToken();
            Node iterator = statement();
            Node stmt = statement();
            return new Node(FOR, null, init, condition, iterator, stmt);
        }
        if (lexer.token() == LCB) {
            lexer.nextToken();
            List<Node> nodes = new ArrayList<>();
            while (lexer.token() != RCB)
                nodes.add(statement());
            lexer.nextToken();
            return new Node(null, "BLOCK", nodes.toArray(new Node[0]));
        }
        if (lexer.token() == VAR) {
            String var = lexer.value();
            lexer.nextToken();
            if (lexer.token() == ASSIGN) {
                lexer.nextToken();
                Node expr = expr();
                lexer.nextToken();
                return new Node(ASSIGN, null, new Node(VAR, var), expr);
            } else {
                if (lexer.token() != LRB)
                    throw new ParseException("( expected");
                List<Node> vars = new ArrayList<>();
                for (; lexer.token() != RRB; ) {
                    lexer.nextToken();
                    vars.add(expr());
                }
                lexer.nextToken();
                if (lexer.token() != LAMBDA) {
                    lexer.nextToken();
                    return new Node(CALL, var, vars.toArray(new Node[0]));
                }
                lexer.nextToken();
                Node body;
                if (lexer.token() == LCB)
                    body = new Node(null, "BODY", statement());
                else {
                    body = new Node(null, "BODY", expr());
                    if (lexer.token() == SEMICOLON)
                        lexer.nextToken();
                    else
                        throw new ParseException("; expected");
                }
                return new Node(LAMBDA, var, new Node(null, "VARS", vars.toArray(new Node[0])), body);
            }
        }
        if (lexer.token() == RETURN) {
            lexer.nextToken();
            Node expr = expr();
            lexer.nextToken();
            return new Node(RETURN, null, expr);
        }
        if (lexer.token() == CONTINUE) {
            lexer.nextToken();
            lexer.nextToken();
            return new Node(CONTINUE, null);
        }
        if (lexer.token() == BREAK) {
            lexer.nextToken();
            lexer.nextToken();
            return new Node(BREAK, null);
        }
        return null;
    }


    public Node parse() throws LexerException, ParseException {
        List<Node> nodes = new ArrayList<>();
        Node node;
        while ((node = statement()) != null)
            nodes.add(node);
        return new Node(null, "PROGRAM", nodes.toArray(new Node[0]));
    }
}
