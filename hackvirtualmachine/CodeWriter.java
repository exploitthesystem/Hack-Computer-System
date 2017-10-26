package hackvirtualmachine;

import java.io.*;

/**
 *
 * @author Marko L.
 */
public class CodeWriter implements java.lang.AutoCloseable {
    PrintWriter output;
    String fileName;
    int eqCtr, gtCtr, ltCtr, retCtr;
    
    public CodeWriter(File fileName) throws FileNotFoundException {
        output = new PrintWriter(fileName);
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName.replaceAll("\\..*", "");
    }
    
    public void writeArithmetic(String command) {
        String cmd = command.toLowerCase();
        String eol = System.getProperty("line.separator");  
        String stackPopCode = "@SP" + eol + "AM=M-1" + eol + "D=M" + eol + "A=A-1";
        String temp = null;
        int tempCtr = 1;
        
        if ("add".equals(cmd) || "sub".equals(cmd) || "and".equals(cmd) || "or".equals(cmd)) {
            switch(cmd) {
                case "add": temp = "M=D+M"; break;
                case "sub": temp = "M=M-D"; break;
                case "and": temp = "M=D&M"; break;
                case "or": temp = "M=D|M"; break;
                default: System.err.println("Invalid arithmetic command in: add, sub, and, or");
            }
            output.println(stackPopCode);
            output.println(temp);
        }
        if ("eq".equals(cmd) || "gt".equals(cmd) || "lt".equals(cmd)) {
            switch(cmd) {
                case "eq": temp = "D;JEQ"; tempCtr += eqCtr; eqCtr++; break;
                case "gt": temp = "D;JGT"; tempCtr += gtCtr; gtCtr++; break;
                case "lt": temp = "D;JLT"; tempCtr += ltCtr; ltCtr++; break;
                default: System.err.println("Invalid arithmetic argument in jump: eq, gt, lt");
            }
            output.println(stackPopCode);
            output.println("D=M-D");
            output.println("M=-1");
            output.println("@" + cmd + "." + tempCtr);
            output.println(temp);
            output.println("@SP");
            output.println("A=M-1");
            output.println("M=0");
            output.println("(" + cmd + "." + tempCtr + ")");
        }
        if ("neg".equals(cmd)) {
            output.println("@SP");
            output.println("A=M-1");
            output.println("M=-M");
        }
        if ("not".equals(cmd)) {
            output.println("@SP");
            output.println("A=M-1");
            output.println("M=!M");
        }
    }
    
    public void writePushPop(String command, String segment, int index) {
        String memoryCode = null;
        String eol = System.getProperty("line.separator");         
        
        if ("C_PUSH".equals(command)) { // push command code.
            String stackPushCode = "@SP" + eol + "M=M+1" + eol + "A=M-1" + eol + "M=D"; // This code applies to all push commands.
            String seg;                                                                 
            
            switch (segment) {
                case "this": seg = "THIS"; break;
                case "that": seg = "THAT"; break;
                case "argument": seg = "ARG"; break;
                default: seg = "LCL"; break;
            }
            
            switch(segment) {
                case "constant": memoryCode = "@" + index + eol + "D=A"; break;
                case "static": memoryCode = "@" + fileName + "." + index + eol + "D=M"; break;
                case "this": 
                case "that": 
                case "argument":
                case "local": memoryCode = "@" + index + eol + "D=A" + eol + "@" + seg + eol + "A=D+M" + eol + "D=M";
                              break;
                case "temp": memoryCode = "@" + index + eol + "D=A" + eol + "@5" + eol + "A=D+A" + eol + "D=M";
                             break;
                case "pointer": memoryCode = "@" + index + eol + "D=A" + eol + "@3" + eol + "A=D+A" + eol + "D=M";
                             break;
            }
            output.println(memoryCode);
            output.println(stackPushCode);
        } 
        if ("C_POP".equals(command)) { // pop command code.
            String stackPopCode = "@R13" + eol + "M=D" + eol + "@SP" + eol + "AM=M-1" + eol + "D=M" + eol 
                + "@R13" + eol + "A=M" + eol + "M=D";
            switch(segment) {
                case "static": memoryCode = "@SP" + eol + "AM=M-1" + eol + "D=M" + eol + "@" + fileName + "." + index 
                        + eol + "M=D";
                               output.println(memoryCode);
                               return;
                case "this": memoryCode = "@" + index + eol + "D=A" + eol + "@THIS" + eol + "D=D+M"; break;
                case "that": memoryCode = "@" + index + eol + "D=A" + eol + "@THAT" + eol + "D=D+M"; break;
                case "argument": memoryCode = "@" + index + eol + "D=A" + eol + "@ARG" + eol + "D=D+M"; break;
                case "local": memoryCode = "@" + index + eol + "D=A" + eol + "@LCL" + eol + "D=D+M"; break;
                case "pointer": memoryCode = "@" + index + eol + "D=A" + eol + "@R3" + eol + "D=D+A"; break;
                case "temp": memoryCode = "@" + index + eol + "D=A" + eol + "@R5" + eol + "D=D+A"; break;
            }
            output.println(memoryCode);
            output.println(stackPopCode);
        }
    }
    
    public void writeInit() {
        output.println("@256");
        output.println("D=A");
        output.println("@SP");
        output.println("M=D");
        writeCall("Sys.init", 0);
    }
    
    public void writeLabel(String label) {
        output.println("(" + label + ")");
    }
    
    public void writeGoto(String label) {
        output.println("@" + label);
        output.println("0;JMP");
    }
    
    public void writeIf(String label) {
        output.println("@SP");
        output.println("AM=M-1");
        output.println("D=M");
        output.println("@" + label);
        output.println("D;JNE");
    }
    
    public void writeCall(String functionName, int numArgs) {
        String eol = System.getProperty("line.separator"); 
        String stackPushCode = "@SP" + eol + "M=M+1" + eol + "A=M-1" + eol + "M=D";
        
        // Saves return address.
        output.println("@retAddr" + retCtr);
        output.println("D=A");
        output.println(stackPushCode);
        // Saves the LCL of the calling function.
        output.println("@LCL");
        output.println("D=M");
        output.println(stackPushCode);
        // Saves the ARG of the calling function.
        output.println("@ARG"); 
        output.println("D=M");
        output.println(stackPushCode); 
        // Saves THIS.
        output.println("@THIS");
        output.println("D=M");
        output.println(stackPushCode); 
        // Saves THAT.
        output.println("@THAT");
        output.println("D=M");
        output.println(stackPushCode); 
        // repositions ARG for called function.
        output.println("@" + numArgs); 
        output.println("D=A");
        output.println("@5");
        output.println("D=D+A");
        output.println("@SP");
        output.println("D=M-D");
        output.println("@ARG");
        output.println("M=D");
        // repositions LCL for called function.
        output.println("@SP"); 
        output.println("D=M");
        output.println("@LCL");
        output.println("M=D");
        // goto g, transfers control to called function.
        output.println("@" + functionName);
        output.println("0;JMP");
        // Symbol for return address.
        output.println("(retAddr" + functionName + "_" + retCtr + ")");
        retCtr++;
    }
    
    public void writeReturn() {
        // frame = LCL
        output.println("@LCL");
        output.println("D=M");
        output.println("@R13");
        output.println("M=D");
        // retAddr = *(frame-5)
        output.println("@5");
        output.println("A=D-A");
        output.println("D=M");
        output.println("@R14");
        output.println("M=D");
        // *ARG = pop
        output.println("@SP");
        output.println("AM=M-1");
        output.println("D=M");
        output.println("@ARG");
        output.println("A=M");
        output.println("M=D");
        // SP = ARG + 1
        output.println("D=A+1");
        output.println("@SP");
        output.println("M=D");
        // THAT = *(frame - 1)
        output.println("@R13");
        output.println("AM=M-1");
        output.println("D=M");
        output.println("@THAT");
        output.println("M=D");
        // THIS = *(frame - 2);
        output.println("@R13");
        output.println("AM=M-1");
        output.println("D=M");
        output.println("@THIS");
        output.println("M=D");
        // ARG = *(frame - 3)
        output.println("@R13");
        output.println("AM=M-1");
        output.println("D=M");
        output.println("@ARG");
        output.println("M=D");
        // LCL = *(frame - 4)
        output.println("@R13");
        output.println("AM=M-1");
        output.println("D=M");
        output.println("@LCL");
        output.println("M=D");
        // goto retAddr
        output.println("@R14");
        output.println("A=M");
        output.println("0;JMP");
    }
    
    public void writeFunction(String functionName, int numLocals) {
        output.println("(" + functionName + ")");
        output.println("@SP");
        output.println("A=M");
        
        for (int i = 0; i < numLocals; i++) {
            output.println("M=0");
            output.println("A=A+1");
        }
    }
    
    @Override
    public void close() throws IOException {
        output.close();
    }
}