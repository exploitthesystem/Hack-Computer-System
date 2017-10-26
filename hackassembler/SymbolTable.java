package hackassembler;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Marko L.
 */
public class SymbolTable {
    private final Map<String, Integer> symbols = new HashMap<>();
    
    public SymbolTable() {
      symbols.put("SP", 0);
      symbols.put("LCL", 1);
      symbols.put("ARG", 2);
      symbols.put("THIS", 3);
      symbols.put("THAT", 4);
      symbols.put("SCREEN", 16384);
      symbols.put("KBD", 24576);
      symbols.put("R0", 0);
      symbols.put("R1", 1);
      symbols.put("R2", 2);
      symbols.put("R3", 3);
      symbols.put("R4", 4);
      symbols.put("R5", 5);
      symbols.put("R6", 6);
      symbols.put("R7", 7);
      symbols.put("R8", 8);
      symbols.put("R9", 9);
      symbols.put("R10", 10);
      symbols.put("R11", 11);
      symbols.put("R12", 12);
      symbols.put("R13", 13);
      symbols.put("R14", 14);
      symbols.put("R15", 15);
    }
    // Adds the pair (symbol, address) to the table.
    public void addEntry(String symbol, int address) {
        symbols.put(symbol, address);
    }
    // Does the symbol table contain the given symbol?
    public boolean contains(String symbol) {
        return symbols.containsKey(symbol);
    }
    // Returns the address associated with the symbol (key-value pair).
    public int getAddress(String symbol) {
        return symbols.get(symbol);
    }
}