/*
 * This is the driver program for the Hack assembler. It reads a text file
 * named Prog.asm, containing a Hack assembly program, and produces as output
 * the translated Hack machine code.
 * Modified: 06/10/14
 */

package hackassembler;

import java.io.IOException;

/**
 *
 * @author Marko L.
 */
public class HackAssembler {

    /**
     * @param args the command line arguments
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("No input file detected.");
            return;
        }
        
        int ROMAddress = 0, RAMAddress = 16;
        String instruction;
        SymbolTable table = new SymbolTable();
        
        java.io.File file = new java.io.File(args[0].replaceAll("\\..*", ".hack"));
        try (java.io.PrintWriter output = new java.io.PrintWriter(file)) {
            Parser parser = new Parser(args[0]);
            while (parser.advance()) {
                if (parser.commandType() == '2') {
                    table.addEntry(parser.symbol(), ROMAddress);
                    continue;
                }
                ROMAddress++;
            }
            parser = new Parser(args[0]);
            while (parser.advance()) {
                if (parser.commandType() == '0') { // A-COMMAND
                    String s = parser.symbol();
                    if (s.charAt(0) < '0' || s.charAt(0) > '9') {
                        Integer addr;
                        if (table.contains(s))
                            addr = table.getAddress(s);
                        else {
                            addr = RAMAddress;
                            table.addEntry(s, addr);
                            RAMAddress++;
                        }
                        instruction = String.format("%16s", Integer.toBinaryString(addr)).replace(' ', '0');
                    }
                    else
                        instruction = String.format("%16s", Integer.toBinaryString(Integer.parseInt(s))).replace(' ', '0');
                }
                else if (parser.commandType() == '1') // C-COMMAND
                    instruction = "111" + Code.comp(parser.comp()) + Code.dest(parser.dest()) + Code.jump(parser.jump());
                else // Skip L-COMMAND
                   continue;
                output.println(instruction);
            }
            parser.close();
            output.close();
        }
        catch (IOException | NumberFormatException e) {
        }
    }
}