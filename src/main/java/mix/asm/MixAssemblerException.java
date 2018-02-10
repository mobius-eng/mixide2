package mix.asm;

import java.io.StreamTokenizer;

/** Assembler exception to report compilation errors.
 */
public class MixAssemblerException extends Exception
{
	/** Create a new compilation error to report.
	 */
	public MixAssemblerException(String message, StreamTokenizer st)
	{
		super(message + " at line " + st.lineno() +
			":\n\tToken type: " + getPrintableType(st.ttype) +
			"\n\tToken numeric value: " + st.nval +
			"\n\tToken string value: " + st.sval + ".");
	}
	
	// TODO: aggiungere un metodo statico che traduca il tipo di un token
	// in una stringa comprensibile.
	/**
	 */
	private static String getPrintableType(int ttype)
	{
		String name = "unknown";
		
		switch(ttype)
		{
			case StreamTokenizer.TT_WORD:
				name = "word";
				break;
			case StreamTokenizer.TT_NUMBER:
				name = "number";
				break;
			case StreamTokenizer.TT_EOL:
				name = "line terminator";
				break;
			case StreamTokenizer.TT_EOF:
				name = "file terminator";
				break;
			default:
				name = "symbol '" + (char) ttype + "', code = " + ttype;
		}
		return name;
	}
}
