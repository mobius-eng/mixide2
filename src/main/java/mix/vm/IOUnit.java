package mix.vm;

import java.io.*;

public class IOUnit
{
	protected InputStream in;
	protected PrintStream out;
	protected RandomAccessFile file;
	protected int blockSize;
	protected long position;
	
	public IOUnit()
	{
		in = null;
		out = null;
		file = null;
		blockSize = 0;
		position = 0;
	}
	
	public void setBlockSize(int size)
	{
		if(size>=0)
			blockSize = size;
	}
	
	public void setFile(File f)
	{
		try
		{
			file = new RandomAccessFile(f, "rw");
		}
		catch(FileNotFoundException e)
		{
		}
	}
	
	public void setInput(InputStream is)
	{
		in = is;
	}
	
	public void setOutput(PrintStream ps)
	{
		out = ps;
	}
	
	public int getBlockSize()
	{
		return blockSize;
	}
	
	public boolean isBusy()
	{
		return !isReady();
	}
	
	public boolean isReady()
	{
		if(in==null && out==null && file==null)
			return false;
		// For the time being, we'll assume an attached unit is always ready.
		return true;
	}

	/** Read a block of data from the I/O device. If the device is attached to
	 *  an InputStream it means the data being read is alphanumeric; therefore,
	 *  characters are silently translated into the internal MIX characted
	 *  code. Otherwise, the data is treated as being numeric.
	 */	
	public MixWord read()
	{
		int value = 0;
		if(file!=null)
		{
			try
			{
				file.seek(position);
				value = file.readInt();
				position += 4;
			}
			catch(IOException ioe)
			{
				// Let's pretend nothing happened...
			}
		}
		else if(in!=null)
			for(int i = 0; i<MixVM.REGISTER_WIDTH; i++)
			{
				try
				{
					// Convert character into internal MIX representation:
					int n = MixChar.number((char) in.read()).intValue();
					// Pack 5 characters into a 30-bit (5-byte) integer value:
					value = (value << MixByte.N_BITS) | (n & MixByte.MAX_VALUE);
				}
				catch(IOException e)
				{
					// Let's pretend nothing happened...
				}
			}
		return new MixWord(MixVM.REGISTER_WIDTH, value);
	}
	
	public void write(MixWord w)
	{
            if(file!=null)
            {
                try
                {
                    file.seek(position);
                    file.write(w.intValue());
                    position += 4;
                }
                catch(IOException ioe)
                {
                    // Let's pretend nothing happened...
                }
            }
            else if(out!=null)
            {
                for(int i = 1; i<=MixVM.REGISTER_WIDTH; i++)
                {
                    char c = MixChar.character(w.getByte(i));
                    out.print(c);
                }
            }
	}
	
	public void newline()
	{
            if(out!=null)
                out.println();
            
	}
	
	public void pagebreak()
	{
		if(out==null) return;
		for(int i = 0; i<blockSize*MixVM.REGISTER_WIDTH; i++)
			out.print("-");
		out.println();
	}
	
	/** Position the device relative to the current position.
	 *  A value of zero, however, means skip to the beginning of the file.
	 */
	public void seek(int offset)
	{
		if(file==null)
			return;
		if(offset==0)
			position = 0;
		else
		{
			position += offset;
			if(position<0)
				position = 0;
			try
			{
				if(position>file.length())
					position = file.length();
			}
			catch(IOException e)
			{
				position = 0;
			}
		}
	}
}
