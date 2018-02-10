package mix.ide;

import javax.swing.text.*;
import javax.swing.event.*;
import mix.vm.*;

/** This class provides a document model that enables the IDE to visualize
 *  a snapshot of the MIX memory contents.
 *  A snapshot of memory contents can be thought of a document containing
 *  one line per memory location (therefore 4000 lines). Each line is 30
 *  character long and has the following format:
 *  <BLOCKQUOTE>
 *    <CODE>
 *      aaaa: s bb cc dd ee ff "BCDEF"
 *    </CODE>
 *  </BLOCKQUOTE>
 *  where <CODE>aaaa</CODE> is the location address (0000 to 3999),
 *  <CODE>s</CODE> is a + or - sign, <CODE>bb</CODE> to <CODE>ff</CODE>
 *  are the bytes that make up the memory word, and <CODE>"BCDEF"</CODE>
 *  is the character-based representation of that memory word.
 */
public class MemoryModel implements Document
{
	/** The number of character per line. Each line is 30
	 *  character long and has the following format:
	 *  <BLOCKQUOTE>
	 *    <CODE>
	 *      aaaa: s bb cc dd ee ff "BCDEF"
	 *    </CODE>
	 *  </BLOCKQUOTE>
	 *  where <CODE>aaaa</CODE> is the location address (0000 to 3999),
	 *  <CODE>s</CODE> is a + or - sign, <CODE>bb</CODE> to <CODE>ff</CODE>
	 *  are the bytes that make up the memory word, and <CODE>"BCDEF"</CODE>
	 *  is the character-based representation of that memory word.
	 */
	public final int LINE_SIZE = 30;
	
	/** The MIX virtual machine whose memory this model gives access to.
	 */
	protected MixVM vm;
	
	/** Create a new memory model which will provide an updated snapshot
	 *  ov the contents of the given MIX virtual machine.
	 */
	public MemoryModel(MixVM vm)
	{
		this.vm = vm;
	}
	
	public int getLength ()
	{
		return MixVM.MEMORY_SIZE*LINE_SIZE;
	}

	public void addDocumentListener (DocumentListener listener)
	{
		// TODO: Add your code here
	}


	public void removeDocumentListener (DocumentListener listener)
	{
		// TODO: Add your code here
	}


	public void addUndoableEditListener (UndoableEditListener listener)
	{
		// TODO: Add your code here
	}


	public void removeUndoableEditListener (UndoableEditListener listener)
	{
		// TODO: Add your code here
	}


	public Object getProperty (Object key)
	{
            // TODO: Add your code here
            return null;
	}


	public void putProperty (Object key, Object value)
	{
		// TODO: Add your code here
	}


	public void remove (int offs, int len)
	{
		// TODO: Add your code here
	}


	public void insertString (int offset, String str, AttributeSet a)
	{
		// TODO: Add your code here
	}


	public String getText (int offset, int length)
	{
            // TODO: Add your code here
            return null;
	}


	public void getText (int offset, int length, Segment txt)
	{
		// TODO: Add your code here
	}


	public Position getStartPosition ()
	{
            // TODO: Add your code here
            return null;
	}


	public Position getEndPosition ()
	{
            // TODO: Add your code here
            return null;
	}


	public Position createPosition (int offs)
	{
            // TODO: Add your code here
            return null;
	}


	public Element[] getRootElements ()
	{
            // TODO: Add your code here
            return null;
	}


	public Element getDefaultRootElement ()
	{
            // TODO: Add your code here
            return null;
	}


	public void render (Runnable r)
	{
            // TODO: Add your code here
	}
}
