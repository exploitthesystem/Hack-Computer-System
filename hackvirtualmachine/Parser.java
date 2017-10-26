/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hackvirtualmachine;

import java.io.*;

/**
 *
 * @author Marko L.
 */
public class Parser {
    private final String ARITH = "C_ARITHMETIC";
    private final String PSH = "C_PUSH", POP = "C_POP";
    private final String LBL = "C_LABEL";
    private final String GT = "C_GOTO", IF = "C_IF";
    private final String FCT = "C_FUNCTION";
    private final String RET = "C_RETURN";
    private final String CALL = "C_CALL";
    
    private BufferedReader buffreader;
    private String[] line;
    
    public Parser(String fileName) throws IOException {
        buffreader = new BufferedReader(new FileReader(fileName));
    }
    
    public boolean advance() throws IOException {
        while (true) {
            String str = buffreader.readLine();
            if (str == null)
                return false;
            
            str = str.replaceAll("^\\s*", "");
            str = str.replaceAll("//.*", "");
            
            if (str.length() == 0)
                continue;
            
            line = str.split("\\b\\s+");
            
            return true;
        }
    }
    
    public String commandType() {
        if (line[0].contains("push"))
            return PSH;
        if (line[0].contains("pop"))
            return POP;
        if (line[0].contains("label"))
            return LBL;
        if (line[0].contains("if-goto")) // if-goto must precede goto. If it doesn't,
            return IF;                   // it will always be overlooked. 
        if (line[0].contains("goto"))
            return GT;
        if (line[0].contains("function"))
            return FCT;
        if (line[0].contains("return"))
            return RET;
        if (line[0].contains("call"))
            return CALL;
        return ARITH;
    }
    
    public String arg1() {
        if (commandType().equals(ARITH))
            return line[0];
        return line[1];
    }
    
    public int arg2() {
        return Integer.parseInt(line[2]);
    }
    
    public void close() throws IOException {
        buffreader.close();
    }
}