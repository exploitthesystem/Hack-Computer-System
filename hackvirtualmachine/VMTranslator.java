/*
 * Main program of the Hack virtual machine, designed to translate .vm files from 
 * the Jack compiler and produce a single Hack.asm assembly file.
 * Modified: 06/14/14
 */

package hackvirtualmachine;

import java.io.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * VMTranslator.java
 * 
 * Main program entry point.
 * @author Marko L.
 */
public class VMTranslator {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Args out of bounds. Must provide one file or directory.");
            return;
        }
        java.io.File file = new File(args[0].replaceAll("\\..*", ".asm"));
        
        String[] vmFiles;
        if (file.isDirectory()) {
            vmFiles = file.list(new FilenameFilter(){
                @Override
                public boolean accept(File directory, String fileName) {
                    return fileName.endsWith(".vm");
                }
            });
        }
        else
            vmFiles = new String[] {args[0]};
        
        try {
            translator(vmFiles, file);
        } catch (IOException ex) {
            Logger.getLogger(VMTranslator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static void translator(String[] files, File input) throws IOException {
        Parser parser;
        CodeWriter writer = new CodeWriter(input);
        
        for (String file : files) {
            parser = new Parser(file);
            writer.setFileName(file);
            while (parser.advance()) {
                if ("C_ARITHMETIC".equals(parser.commandType()))
                    writer.writeArithmetic(parser.arg1());
                if ("C_PUSH".equals(parser.commandType()) || "C_POP".equals(parser.commandType()))
                    writer.writePushPop(parser.commandType(), parser.arg1(), parser.arg2());
                if ("C_LABEL".equals(parser.commandType()))
                    writer.writeLabel(parser.arg1());
                if ("C_GOTO".equals(parser.commandType()))
                    writer.writeGoto(parser.arg1());
                if ("C_IF".equals(parser.commandType()))
                    writer.writeIf(parser.arg1());
                if ("C_CALL".equals(parser.commandType()))
                    writer.writeCall(parser.arg1(), parser.arg2());
                if ("C_RETURN".equals(parser.commandType()))
                    writer.writeReturn();
            }
            parser.close();
        }
        writer.close();
    }
}