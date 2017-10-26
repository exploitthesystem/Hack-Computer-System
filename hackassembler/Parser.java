/*
 * This is the parser for the Hack machine assembler. It receives a standard  
 * text file as a buffered input stream and manipulates each line, parsing
 * in order to extract A and C instructions for further processing.
 */

package hackassembler;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Marko L.
 */
public class Parser {
    private final char A_COMMAND = '0';
    private final char C_COMMAND = '1';
    private final char L_COMMAND = '2';
    
    private BufferedReader buffreader;
    private String line;
    
    public Parser(String fileName) {
        try {
            buffreader = new BufferedReader(new FileReader(fileName));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public boolean advance() throws IOException {         
        while(true) {
          line = buffreader.readLine();
          if (line == null)
              return false;
          
          line = line.replaceAll("\\s", "");
          line = line.replaceAll("//.*", "");
          if (line.length() == 0)
              continue;
          return true;
        }
    }
    
    public char commandType() {
        if (line.charAt(0) == '@')
            return A_COMMAND;
        if (line.charAt(0) == '(')
            return L_COMMAND;
        return C_COMMAND;
    }
    
    public String symbol() {
        String s = line.replace("(", "");
        s = s.replace(")", "");
        s = s.replace("@", "");
        
        return s;
    }
    
    public String dest() {
        if (line.indexOf('=') == -1)
            return "";
        String s = line.replaceAll("=.*", "");
        String dest = "";
        if (s.indexOf('A') != -1)
            dest = "A";
        if (s.indexOf('M') != -1)
            dest += "M";
        if (s.indexOf('D') != -1)
            dest += "D";
        
        return dest;
    }
    
    public String comp() {
        String s = line.replaceAll(".*=", "");
        s = s.replaceAll(";.*", "");
        
        return s;
    }
    
    public String jump() {
        if (line.indexOf(';') == -1)
            return "";
        String s = line.replaceAll(".*;", "");
        
        return s;
    }
    
    public void close() throws IOException {
        buffreader.close();
    }
}