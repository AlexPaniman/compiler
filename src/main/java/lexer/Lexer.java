package lexer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.Map;

import static lexer.Token.*;

public class Lexer {
    private int index;
    private char[] program;

    private Token token;
    private String value;

    private static Map<String, String> seq;

    static {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        Type map = new TypeToken<Map<String, String>>() {
        }.getType();
        try {
            seq = gson.fromJson(
                    Files
                            .lines(new File("src/main/resources/sequence.json").toPath())
                            .reduce("", (acc, line) -> acc + line + "\n"),
                    map
            );
        } catch (IOException io) {
            io.printStackTrace();
        }
    }


    public Lexer(String program) {
        this.program = program.toCharArray();
        this.index = 0;
    }


    private boolean str() throws LexerException {
        if (program[index] != '\'')
            return false;
        StringBuilder string = new StringBuilder();
        for (index++; index < program.length; index++) {
            if (program[index] == '\'') {
                token = STR;
                value = string.toString();
                index++;
                return true;
            }
            if (program[index++] == '\\')
                switch (program[index]) {
                    case '\\':
                        string.append("\\");
                        break;
                    case 'n':
                        string.append("\n");
                        break;
                    case 'r':
                        string.append("\r");
                        break;
                    case 't':
                        string.append("\t");
                        break;
                    case 'b':
                        string.append("\b");
                        break;
                    case 'f':
                        string.append("\f");
                        break;
                    case '\'':
                        string.append("\'");
                        break;
                    case '\"':
                        string.append("\"");
                        break;
                    default:
                        throw new LexerException("Unexpected symbol after '\\':" + program[index]);
                }
            else
                string.append(program[index]);
        }
        throw new LexerException("Unclosed string literal");
    }

    private boolean num() {
        StringBuilder number = new StringBuilder();
        for (; ; index++)
            if (Character.isDigit(program[index]) || (number.length() > 0 && program[index] == '.'))
                number.append(program[index]);
            else
                break;
        if (number.length() == 0)
            return false;
        token = NUM;
        value = number.toString();
        return true;
    }

    private boolean var() {
        int start = index;
        StringBuilder variable = new StringBuilder();
        for (; ; index++)
            if (String.valueOf(program[index]).matches("[a-zA-Z]"))
                variable.append(program[index]);
            else
                break;
        if (variable.length() == 0) {
            index = start;
            return false;
        }
        token = VAR;
        value = variable.toString();
        return true;
    }

    private boolean seq(String compare) {
        int start = index;
        for (int j = 0; j < compare.length(); j++, index++)
            if (program[index] != compare.charAt(j)) {
                index = start;
                return false;
            }
        return true;
    }

    private boolean anySeq() {
        for (Map.Entry<String, String> entry : seq.entrySet())
            if (seq(entry.getKey())) {
                token = Token.valueOf(entry.getValue());
                value = entry.getKey();
                return true;
            }
        return false;
    }


    private void skip() {
        while (program[index] == '\t' || program[index] == '\n' || program[index] == ' ') {
            index++;
            if (index >= program.length)
                break;
        }
    }

    private boolean ended() {
        if (index >= program.length) {
            token = null;
            value = null;
            return true;
        }
        return false;
    }


    public boolean nextToken() throws LexerException {
        if (ended())
            return false;
        skip();
        if (ended())
            return false;
        if (anySeq())
            return true;
        if (var())
            return true;
        if (num())
            return true;
        if (str())
            return true;
        throw new LexerException("Unexpected symbol: " + program[index]);
    }


    public Token token() {
        return token;
    }

    public String value() {
        return value;
    }
}
