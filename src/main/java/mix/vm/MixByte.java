package mix.vm;

/** This class implements the basic unit of information
 *  of the MIX architecture, the 6-bit byte.
 */
public class MixByte
{
	public static final int MAX_VALUE = 63;
	public static final int N_BITS = 6;
	protected byte value;
	
	public MixByte()
	{
		value = 0;
	}
	
	public MixByte(int v)
	{
		value = (byte) (v & MAX_VALUE);
	}
	
	public byte byteValue()
	{
		return value;
	}

	public int intValue()
	{
		return value;
	}
	
	public boolean inc()
	{
		value++;
		if(value>MAX_VALUE)
		{
			value = 0;
			return true;
		}
		return false;
	}

	public String toString()
	{
		return "" + value;
	}
}
