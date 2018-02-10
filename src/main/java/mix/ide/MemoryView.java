package mix.ide;

import java.awt.*;
import javax.swing.*;
import mix.edit.RowModifier;
import mix.vm.*;

/** This class provides a text area that enables the IDE to visualize
 *  a snapshot of the MIX memory contents.
 *  A snapshot of memory contents can be thought of a document containing
 *  one line per memory location (therefore 4000 lines). Each line is 30
 *  character long and has the following format:
 *  <BLOCKQUOTE>
 *    <CODE>
 *      aaaa: s bb cc dd ee ff "BCDEF" instr x.xxxxx
 *    </CODE>
 *  </BLOCKQUOTE>
 *  where <CODE>aaaa</CODE> is the location address (0000 to 3999),
 *  <CODE>s</CODE> is a + or - sign, <CODE>bb</CODE> to <CODE>ff</CODE>
 *  are the bytes that make up the memory word, <CODE>"BCDEF"</CODE>
 *  is the character-based representation of that memory word,
 *  and <CODE>instr</CODE> is the disassembled MIXAL instruction
 *  contained in the memory word.
 */
public class MemoryView extends JTextArea
{
    /** The number of character per line. Each line is 30
     *  character long and has the following format:
     *  <BLOCKQUOTE>
     *    <CODE>
     *      aaaa: s bb cc dd ee ff "BCDEF" instr x.xxxxx
     *    </CODE>
     *  </BLOCKQUOTE>
     *  where <CODE>aaaa</CODE> is the location address (0000 to 3999),
     *  <CODE>s</CODE> is a + or - sign, <CODE>bb</CODE> to <CODE>ff</CODE>
     *  are the bytes that make up the memory word, and <CODE>"BCDEF"</CODE>
     *  is the character-based representation of that memory word,
     *  and <CODE>instr</CODE> is the disassembled MIXAL instruction
     *  contained in the memory word.
     */
    public final int LINE_SIZE = 31;

    /** The number of characters for the disassembled MIXAL instruction
     *  contained in the memory word, which is appended at the end of
     *  each line.
     */
    public final int COMMENT_SIZE = 23;
    
    /** The number of characters for the floating-point representation of
     *  the memory word, which is appended past the disassembled MIXAL instruction.
     */
    public final int FP_SIZE = 15;
    
    /** Total size of a line. */
    public final int T_SIZE = LINE_SIZE + COMMENT_SIZE + FP_SIZE;
    
    /** The filler character. */
    public final char FILLER =' ';
    
    protected int location;
	
    private MixIDE mixIde;
	
    /** Create a new view of the memory contents of the given MIX virtual
     *  machine.
     */
    public MemoryView(MixIDE ide)
    {
            super();
            mixIde = ide;
            setFont(new Font("Monospaced", Font.PLAIN, 12));
            MixWord zero = new MixWord(MixVM.REGISTER_WIDTH, 0);
            for(int i = 0; i<MixVM.MEMORY_SIZE; i++)
                    append(line(i, zero));
            location = 0;
    }
	
    public MixIDE getMixIDE()
    {
    	return mixIde;
    }

    public MixVM getMixVM()
    {
    	return mixIde.getMixVM();
    }

	/** Format a line. Each line is 30 character long and has the following format:
	 *  <BLOCKQUOTE>
	 *    <CODE>
	 *      aaaa: s bb cc dd ee ff "BCDEF" istr x.xxx
	 *    </CODE>
	 *  </BLOCKQUOTE>
	 *  where <CODE>aaaa</CODE> is the location address (0000 to 3999),
	 *  <CODE>s</CODE> is a + or - sign, <CODE>bb</CODE> to <CODE>ff</CODE>
	 *  are the bytes that make up the memory word, and <CODE>"BCDEF"</CODE>
	 *  is the character-based representation of that memory word.
	 */
	private String line(int address, MixWord word)
	{
		String str = "";
		
		if(address<1000)
			str += "0";
		if(address<100)
			str += "0";
		if(address<10)
			str += "0";
		str += address + ": ";
		str += word.sign()<0 ? "-" : "+";
		for(int i = 0; i<word.size(); i++)
		{
			int b = word.getByte(i + 1).intValue();
			str += " ";
			if(b<10)
				str += "0";
			str += b;
		}
		str += " \"";
		for(int i = 0; i<word.size(); i++)
			str += MixChar.character(word.getByte(i + 1));
		str += "\"" + comment(mix.asm.Opcode.decode(word), COMMENT_SIZE, FILLER);
                str += comment(String.format("%e", word.doubleValue()), FP_SIZE, FILLER) + "\n";
		
		return str;
	}
	
	/** Update the content of a memory word.
	 */
	public void update(int address, MixWord value)
	{
            replaceRange(line(address, value), address*T_SIZE, (address + 1)*T_SIZE);
            getMixVM().setMemoryValue(address, value);
	}
        
        /**
         * Set the caret position to the line corresponding the given address,
         * so that the view scrolls to make that line visible.
         */
        public void setCurrentAddress(int address)
        {
            setCaretPosition(address*T_SIZE);
        }
        
        /**
         * Refresh the view on the screen.
         */
        public void refresh()
        {
            paintImmediately(getVisibleRect());
        }
	
	/** Update the location pointer.
	 */
	public void setLocationPointer(int address)
	{
            if(address<0 || address>=MixVM.MEMORY_SIZE)
                return;

            replaceRange(" ", location*T_SIZE + 5, location*T_SIZE + 6);
            location = address;
            replaceRange("*", location*T_SIZE + 5, location*T_SIZE + 6);
            // setCaretPosition(location*T_SIZE);
	}

    /** Formats the disassembled instruction at the end of a line.
     *
     *  @author Stefano Marino
     */
    public static String comment(String comm, int size, char filler)
    {
    	StringBuffer ret = new StringBuffer(size);	
    	int s = 0;
    	if(comm!=null)
    	{
    		s = (comm.length()<=size) ? comm.length() : size;
    		ret.append(comm.substring(0,s));
    	}
    	for(int i = 0; i<size - s; i++)
    		ret.append(filler);
    	return ret.toString();
    }
}
