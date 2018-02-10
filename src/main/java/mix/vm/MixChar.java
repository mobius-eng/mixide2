package mix.vm;



public class MixChar
{
	/** The table of MIX character codes.
	 *  The assignment of character 0 to 55 is based on Knuth;
	 *  the assignment of the remaining characters, 56 to 63,
	 *  left unspecified by Knuth, has been dictated by the
	 *  requirements of the examples developed for the classes. 
	 */
	private static final char[] table =
	{
		' ', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I',
		'\u0394', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
		'\u03A3', '\u03A0', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'.', ',', '(', ')', '+', '-', '*', '/', '=', '$',
		'<', '>', '@', ';', ':', '\'', '!', '?', '#', '%',
		'\\', '|', '"', '~'
	};
	
	public static char character(MixByte b)
	{
		return table[b.intValue()];
	}
	
	public static MixByte number(char c)
	{
		int i;
		for(i = 0; i<table.length; i++)
			if(table[i]==c) break;
		return new MixByte(i);
	}
}
