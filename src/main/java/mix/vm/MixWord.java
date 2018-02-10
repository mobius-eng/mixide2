package mix.vm;

/** This class implements a MIX word, made up of
 *  a number of bytes plus sign.
 */
public class MixWord
{
	/** Sign of the word, according to the convention
	 *  <code>false</code> = positive, <code>true</code> =negative.
	 */
	protected boolean sign;
	protected MixByte[] part;
        
        /** The maximum absolute value that fits in a MIX word. */
	public static final int MAX_VALUE = 0x3FFFFFFF;
	
	/**
	 *
	 *  @author Stefano Marino
	 */
    public MixWord(String[] s)
    {
    	sign = (!s[0].trim().equals("+")) ? true : false;
    	part = new mix.vm.MixByte[5];
        for(int j = 0; j < 5; j++)
            part[j] = new MixByte(Integer.parseInt(s[j+1]));
    }
    
	public MixWord(int n_bytes)
	{
		sign = false;
		part = new MixByte[n_bytes];
		for(int i = 0; i<n_bytes; i++)
			part[i] = new MixByte();
	}
	
	public MixWord(int n_bytes, int v)
	{
		if(sign = v<0)
			v = -v;
		int mask = MixByte.MAX_VALUE;
		part = new MixByte[n_bytes];
		for(int i = 0; i<n_bytes; i++)
		{
			part[n_bytes - i - 1] = new MixByte((v & mask) >> (MixByte.N_BITS*i));
			mask <<= MixByte.N_BITS;
		}
	}
	
	/** Create a word by joining two smaller words.
	 */
	public MixWord(MixWord wa, MixWord wx)
	{
		sign = wa.sign;
		part = new MixByte[wa.size() + wx.size()];
		for(int i = 0; i<wa.size(); i++)
			part[i] = wa.part[i];
		for(int i = 0; i<wx.size(); i++)
			part[wa.size() + i] = wx.part[i];
	}
	
	/** Return the number of bytes of which the word consists of.
	 */
	public int size()
	{
            return part.length;
	}
	
        /**
         * Returns the sign of this word as an integer: -1 for negative
         * or +1 for positive.
         * 
         * @return -1 or +1, depending on the sign of this word
         */
	public int sign()
	{
            return sign ? -1 : 1;
	}
	
	public void setSign(int s)
	{
		sign = s<0;
	}
	
	/** Return a single byte of the word.
	 */
	public MixByte getByte(int pos)
	{
		// 2-byte words have bytes 4 and 5,
		// 5-byte words have bytes 1 through 5,
		// whereas 10-byte words (e.g., rAX) have bytes 1 to 10
		int i = part.length<=5 ? pos + part.length - 6 : pos - 1;
		if(i<0 || i>=part.length)
			return new MixByte();
		return part[i];
	}
	
	/** Set a single byte of the word.
	 */
	public void setByte(int pos, MixByte b)
	{
		// 2-byte words have bytes 4 and 5,
		// 5-byte words have bytes 1 through 5,
		// whereas 10-byte words (e.g., rAX) have bytes 1 to 10
		int i = part.length<=5 ? pos + part.length - 6 : pos - 1;
		if(i>=0 && i<part.length)
			part[i] = b;
	}
	
	/** Return a sub-word, according to a field specification.
	 *  If the field specification includes position zero, then the sign
	 *  of the word is considered, otherwise the returned subword is
	 *  always positive. Two-byte words have bytes 4 and 5, and the
	 *  other bytes are assumed to be zero. 
	 */
	public MixWord subWord(FieldSpecification fspec)
	{
		int L = fspec.first();
		int R = fspec.last();
		
		// Number of bytes of the extracted subword:
		int n_bytes = R - L;
		if(L>0)
			n_bytes++;
		
		// Create a new word of the right size to get the subword:
		MixWord sw = new MixWord(n_bytes);
		
		// If L = 0, consider the sign:
		if(L==0)
		{
			sw.sign = sign;
			L = 1;
		}
		
		// Adjust L and R to take the various cases into account:
		// - for a register (5-byte) word, L and R stay unchanged;
		// - for an address (2-byte) word, L and R are decreased by 3, so that
		//   e.g. byte 4 now is byte 1 of the part[] array:
		// - for an extended (10-byte) word, no change is needed.
		if(part.length<=5)
		{
			L += part.length - 5;
			R += part.length - 5;
		}
		
		// Now copy the bytes, reminding that Java arrays start from zero;
		// also, bytes whose index falls outside of the original word are
		// taken as being zero:
		for(int i = 0; i<n_bytes; i++)
		{
			int j = L + i - 1;
			if(j>=0 && j<part.length)
	 			sw.part[i] = part[j];
	 	}
		return sw;
	}
	
	/** Assigns a value to the specified sub-word.
	 *  On an <EM>assignment</EM> operation, the field specification has the
	 *  opposite meaning from the <EM>get</EM> operation: the number
	 *  of bytes in the field is taken from the right-hand of the register
	 *  and shifted <EM>left</EM> if necessary to be inserted into the
	 *  specified field of the destination word.
	 */
	public void subWord(FieldSpecification fspec, MixWord source)
	{
		int L = fspec.first();
		int R = fspec.last();
		int n_bytes = R - L;
		if(L>0)
			n_bytes++;

		if(L==0)
		{
			sign = source.sign;
			L = 1;
		}
		else
		{
			L += part.length - 5;
			R += part.length - 5;
		}
		for(int i = L - 1; i<R; i++)
		{
			int j = i + 7 - n_bytes - L;
 			part[i] = source.getByte(j);
 		}
	}
	
	/** Return the integer value of the word.
	 */
	public int intValue()
	{
		int value = 0;
	
		for(int i = 0; i<part.length; i++)
			value |= part[part.length - i - 1].intValue() << (MixByte.N_BITS*i);
			
		return sign()*value;
	}
	
	/**
         * Returns the long value of this word.
         * 
         * @return the value of this word as a <code>long</code>
	 */
	public long longValue()
	{
            long value = 0;

            for(int i = 0; i<part.length; i++)
                value |= ((long) part[part.length - i - 1].intValue()) << (MixByte.N_BITS*i);

            return sign()*value;
	}
	
	/** Return the double-precision floating-point value of the word.
         *  The base 64 floating-point representation used by the MIX machine is as
         *  follows:
         *  <ul>
         *  <li>the sign field (0:0) holds the sign of the floating-point number;</li>
         *  <li>the (1:1) field holds the exponent in "excess 32" representation;</li>
         *  <li>the (2:5) field holds (the fractional part of) the mantissa, <VAR>f</VAR>.</li> 
         *  </ul>
         *  The convention adopted by Knuth is that <VAR>f</VAR> &lt; 1.
         *  To be normalized, a floating-point number must have  <VAR>f</VAR> >= 1/64.
         *
         *  @since version 1.4
	 */
	public double doubleValue()
	{
		double mantissa = 0;
	
		for(int i = 1; i<part.length; i++)
			mantissa = mantissa*64.0 + ((double) part[i].intValue());
                if(sign)
                    mantissa *= -1.0;
			
		return mantissa*Math.pow(64.0, ((double) part[0].intValue()) - 36.0);
	}
	
	/** Return the integer value of the word and increment.
	 */
	public int increment()
	{
		int v = intValue();
		for(int i = part.length - 1; i>=0; i--)
			if(!part[i].inc())
				break;
		return v;
	}
	
	
	/** Shift left the word in-place by the specified number of bytes.
	 */
	public void shiftLeft(int n)
	{
		int i;

		if(n<=0)
			return;
		for(i = 0; i<part.length - n; i++)
			part[i] = part[i + n];
		for(; i<part.length; part[i++] = new MixByte());
	}
	
	/** Shift left the word in-place by the specified number of bits.
	 */
	public void shiftLeftBinary(int n)
	{
		if(n<=0)
			return;
                long v = longValue() << n;
		long mask = MixByte.MAX_VALUE;
		for(int i = 0; i<part.length; i++)
		{
			part[part.length - i - 1] = new MixByte((int)((v & mask) >> (MixByte.N_BITS*i)));
			mask <<= MixByte.N_BITS;
		}
	}
	
	/** Shift right the word in-place by the specified number of bytes.
	 */
	public void shiftRight(int n)
	{
		int i;

		if(n<=0)
			return;
		for(i = part.length - 1; i>=n; i--)
			part[i] = part[i - n];
		for(; i>=0; part[i--] = new MixByte());
	}
	
	/** Shift right the word in-place by the specified number of bits.
	 */
	public void shiftRightBinary(int n)
	{
		if(n<=0)
			return;
                long v = longValue() >> n;
		long mask = MixByte.MAX_VALUE;
		for(int i = 0; i<part.length; i++)
		{
			part[part.length - i - 1] = new MixByte((int)((v & mask) >> (MixByte.N_BITS*i)));
			mask <<= MixByte.N_BITS;
		}
	}
	
	/** Rotate left the word in-place by the specified number of bytes.
	 */
	public void rotateLeft(int n)
	{
		n = n % part.length;
		if(n==0) return;
		
		int i;
		MixByte[] buffer = new MixByte[n];
		for(i = 0; i<part.length - n; i++)
		{
			if(i<n) buffer[i] = part[i];
			part[i] = part[i + n];
		}
		for(int j = 0; i<part.length; part[i++] = buffer[j++]);
	}
	
	/** Rotate right the word in-place by the specified number of bytes.
	 */
	public void rotateRight(int n)
	{
		n = n % part.length;
		if(n==0) return;
		
		int i;
		MixByte[] buffer = new MixByte[n];
		for(i = part.length - 1; i>=n; i--)
		{
			if(i>=part.length - n) buffer[i - part.length + n] = part[i];
			part[i] = part[i - n];
		}
		for(int j = n - 1; i>=0; part[i--] = buffer[j--]);
	}
	
	public String toString()
	{
		String s = sign ? "-" : "+";
		for(int i = 0; i<part.length; i++)
			s += " " + part[i];
		return s;
	}
}
