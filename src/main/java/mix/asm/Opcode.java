package mix.asm;

import mix.vm.*;
import java.util.*;

/**
 * This class encapsulates the concept of opcode, including assembler
 * directives.
 *
 * @author Andrea G. B. Tettamanzi
 */
public class Opcode
{
	/** A conventional instruction code for "unknown opcode".
	 */
	public static int UNKNOWN = -1;
	
	/** Lookup table of symbolic names of operations and assembler directives.
	 */
	protected static MixalSymbolTable symbol = new MixalSymbolTable();

	/** Reverse lookup table of opcodes, for disassembly.
	 */
	protected static Properties codes = reverse(symbol);

	/** The symbolic opcode as passed to the constructor. */
	protected String OP;
	
	/** The operation code field of the instruction:
	 */
	protected int C;

	/** The default value of the operation variant.
	 */	
	protected int F;

	/** Reverses the provided symbol table, making it possible
	 *  to associate a mnemonic to every numerical opcode.
	 *
	 *  @author Stefano Marino, Andrea G. B. Tettamanzi
	 */	
	private static Properties reverse(MixalSymbolTable s)
	{
		if(s==null) return null;
		Properties rev = new Properties();
		for(Iterator i = s.keySet().iterator(); i.hasNext();)
		{
			 String mnemonic = (String) i.next();
			 if(!mnemonic.endsWith("_F"))
			 {
 				String pair = "" + s.getProperty(mnemonic).trim();
 				if(s.containsKey(mnemonic + "_F"))
 					pair += " " + s.getProperty(mnemonic + "_F").trim();
				rev.setProperty(pair, mnemonic);
			 }
		}
		// rev.list(System.out);
		return rev;
	}
		
	/** Disassemble an instruction.
	 *
	 *  @author Stefano Marino, Andrea G. B. Tettamanzi
	 */	
	public static String decode(MixWord mw)
	{
		Opcode oc = new Opcode(mw);
		String ret = oc.mnemonic();
		if(ret!=null)
		{
			while(ret.length()<5)
				ret += " ";
			int addr = mw.sign()*(mw.getByte(1).intValue()*64+mw.getByte(2).intValue());
			if(addr!=0)
				ret += addr;
			if(mw.getByte(3).intValue()>0)
				ret += (addr==0 ? "0" : "") + "," + mw.getByte(3).intValue();
			if(mw.getByte(4).intValue()!=Integer.parseInt(symbol.getProperty(oc.mnemonic() + "_F", "5")))
				ret += "(" + mw.getByte(4).intValue()/8 + ":" + mw.getByte(4).intValue()%8 + ")";
			return "  " + ret;
		}
		return "";
	}
    
	/** Look up the numerical operation code of the given symbolic opcode.
	 */
	public static int code(String w)
	{
		return Integer.parseInt(symbol.getProperty(w, "-1"));
	}
	
	/** Create a new opcode instance based on its symbolic name.
	 *  If the word token is not a recognized opcode or assembler directive,
	 *  the opcode will assume the UNKNOWN code
	 */
	public Opcode(String w)
	{
		OP = w;
		// Look up the numerical operation code, default is UNKNOWN
		C = Integer.parseInt(symbol.getProperty(OP, "-1"));
		// Look up the default value of the variant field, default is (0:5)
		F = Integer.parseInt(symbol.getProperty(OP + "_F", "5"));
	}
	
	/** Create a new opcode instance based on a Mix word.
	 */
	public Opcode(MixWord mw)
	{
            C = mw.getByte(5).intValue();
            F = mw.getByte(4).intValue();
            // Look up the mnemonic:
            // Simplest case: mnemonic with explicit default F
            if(codes.containsKey("" + C + " " + F))
                OP = codes.getProperty("" + C + " " + F);
            // Normal case: mnemonic with implicit default F:
            else if(codes.containsKey("" + C))
                OP = codes.getProperty("" + C);
            // Most complicated case: mnemonic with nondefault F
            else
            {
                OP = "???";
                for(int f = 0; f<6; f++)
                    if(codes.containsKey("" + C + " " + f))
                    {
                        OP = codes.getProperty("" + C + " " + f);
                        break;
                    }
            }
	}
		
	/** Check whether the opcode is unknown.
	 */
	public boolean isUnknown()
	{
            return C==UNKNOWN;
	}
	
	/** Return the mnemonic of the opcode.
	 */
	public String mnemonic()
	{
            return OP;
	}
	
	/** Return the code field of the opcode.
	 */
	public int code()
	{
            return C;
	}
	
	/** Return the default value of the variant field of the opcode.
	 */
	public int defaultVariant()
	{
            return F;
	}
}
