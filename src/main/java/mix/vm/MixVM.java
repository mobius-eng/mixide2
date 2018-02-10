package mix.vm;

import java.io.*;
import mix.ide.MemoryView;
import mix.ide.RegisterView;
import mix.ide.TerminalView;

/**
 * The MIX virtual machine.
 *
 * @author Andrea G. B. Tettamanzi
 */
public class MixVM
{
	public static final int N_INDEX_REGISTERS = 6;
	public static final int REGISTER_WIDTH = 5;
	public static final int ADDRESS_WIDTH = 2;
	public static final int MEMORY_SIZE = 4000;
	public static final int N_IO_UNITS = 21;
	
	/** The Accumulator register.
	 */
	protected MixWord rA;
	
	/** The Extension register.
	 */
	protected MixWord rX;
	
	/** The Jump address register.
	 */
	protected MixWord rJ;
	
	/** The Location Pointer (i.e., the program counter).
	 */
	protected MixWord lp;
	
	/** The six Index registers I1 to I6.
	 */
	protected MixWord[] rI;
	protected MixWord[] memory;
	
	/** The overflow toggle. */
	boolean overflow;
	
	/** The comparison indicator.
	 *  Negative means L, zero means E, positive means G.
	 */
	protected int comparison;
	
	/** The 21 I/O units (InputStream & PrintStream).
	 */
	protected IOUnit[] u;
	
	/** Halt flag. */
	protected boolean halted;
        
        /** The internal clock. */
        protected long clock;
	
	/** View of the memory contents. */
	protected MemoryView view;
		
	/** View of the registers. */
	protected RegisterView regView;
		
	/** Creates an instance of the MIX virtual machine.
	 *
	 */
	public MixVM()
	{
		rA = new MixWord(REGISTER_WIDTH);
		rX = new MixWord(REGISTER_WIDTH);
		rJ = new MixWord(ADDRESS_WIDTH);
        lp = new MixWord(ADDRESS_WIDTH);
		rI = new MixWord[N_INDEX_REGISTERS];
		for(int i = 0; i<N_INDEX_REGISTERS; i++)
			rI[i] = new MixWord(ADDRESS_WIDTH);
		memory = new MixWord[MEMORY_SIZE];
		for(int i = 0; i<MEMORY_SIZE; i++)
			memory[i] = new MixWord(REGISTER_WIDTH);
		overflow = false;
		comparison = 0;
		
		// Set up the I/O units:
		u = new IOUnit[N_IO_UNITS];
		for(int i = 0; i<N_IO_UNITS; i++)
			u[i] = new IOUnit();
		// Units 0 through 15 have 100-word blocks:
		for(int i = 0; i<=15; i++)
			u[i].setBlockSize(100);
		// Unit 16 is the 80-column card reader - in this emulation, the default input
		u[16].setBlockSize(16);
		u[16].setInput(System.in);
		// Unit 17 is the 80-column card punch - in this emulation, the output console
		u[17].setBlockSize(16);
		u[17].setOutput(System.out);
		// Unit 18 is the line printer - in this emulation, the output console
		u[18].setBlockSize(24);
		u[18].setOutput(System.out);
		// Unit 19 is the typewriter terminal - in this emulation, it will be
		// the terminal view, but the error console by default
		u[19].setBlockSize(14);
		u[19].setOutput(System.err);
		// Unit 20 is the paper tape - in this emulation, the error console
		u[20].setBlockSize(14);
		u[20].setOutput(System.err);
		halted = true;
                resetClock();
		view = null;
	}
	
	/** Set the terminal view
	 */
	public void setTerminal(TerminalView term)
	{
            u[19].setOutput(term.getPrintStream());
	}
	
	/** Set the view for visualizing a snapshot of the memory contents.
	 *  When a view is set, every change in the contents of the memory
	 *  is notified to the view, so that the snapshot on the screen is
	 *  kept up to date.
	 */
	public void setView(MemoryView v)
	{
            view = v;
            if(view!=null)
                for(int i = 0; i<MEMORY_SIZE; i++)
                    view.update(i, memory[i]);
	}
        
        /** Set the view for visualizing a snapshot of the registers.
	 *  When a view is set, every change in the contents of the registers
	 *  is notified to the view, so that the snapshot on the screen is
	 *  kept up to date.
         */
        public void setRegisterView(RegisterView rv)
        {
            regView = rv;
        }

	/** Return the new content for a register of a given width,
	 *  and set the OV toggle if appropriate.
	 */	
	protected MixWord setRegister(int width, int v)
	{
		int sign = 1;
		if(v<0)
		{
			sign = -1;
			v = -v;
		}
		overflow = (v >> (width*MixByte.N_BITS))!=0;
		return new MixWord(width, sign*v);
	}
	
	/** Check whether OV is on. If it is on, it is turned off,
	 *  otherwise nothing happens.
	 */
	public boolean overflow()
	{
		boolean ov = overflow;
		overflow = false;
		return ov;
	}

	/** Sets the overflow toggle if the parameter is <CODE>"on"</CODE>.
         *
	 *  @author Stefano Marino
	 */	
        public void setOV(String ov)
        {
            overflow = ov.equalsIgnoreCase("on");
        }

	/** Get the status of the OV toggle.
	*/
	public boolean getOV() { return overflow; }
	
	/** Get the status of the comparison flag
	*/
	public int getCmp() { return comparison; }
	
	/** Check the comparison flags.
	 */
	public boolean cmpL() { return comparison<0; }
	public boolean cmpE() { return comparison==0; }
	public boolean cmpG() { return comparison>0; }
	public boolean cmpGE() { return comparison>=0; }
	public boolean cmpNE() { return comparison!=0; }
	public boolean cmpLE() { return comparison<=0; }
	
	/** Set the comparison flags.
	 */
	public void compare(int v) { comparison = v; }
	
	public MixWord getAccumulator()	{ return rA; }
	
	public void setAccumulator(int v) { rA = setRegister(REGISTER_WIDTH, v); }
	
	public MixWord getExtension() {	return rX; }
	
	public void setExtension(int v)	{ rX = setRegister(REGISTER_WIDTH, v); }
	
	public MixWord getExtendedAccumulator()	{ return new MixWord(rA, rX); }
	
        /** Enters the given long value into the extended accumulator and
         *  sets the overflow toggle if appropriate. 
         */
	public void setExtendedAccumulator(long v)
	{
		int sign = 1;
		if(v<0)
		{
			sign = -1;
			v = -v;
		}
		rX = new MixWord(REGISTER_WIDTH, (int) (sign*(v & MixWord.MAX_VALUE)));
		v >>= REGISTER_WIDTH*MixByte.N_BITS;
		rA = new MixWord(REGISTER_WIDTH, (int) (sign*v));
		overflow = (v >> (REGISTER_WIDTH*MixByte.N_BITS))!=0;
	}

	/** Sets the double register rAX with the content of the given
	 *  (10-byte) word, without affecting the signs.
	 */
	public void setExtendedAccumulator(MixWord w)
	{
		for(int i = 1; i<=REGISTER_WIDTH; i++)
		{
			rA.setByte(i, w.getByte(i));
			rX.setByte(i, w.getByte(REGISTER_WIDTH + i));
		}
	}
	
	/** Sets the <CODE>rA</CODE> register with the given a floating-point value,
	 *  and set the <CODE>OV</CODE> toggle if the magnitude of the provided value is too
         *  big to be represented.
         *  The base 64 floating-point representation used by the MIX machine is as
         *  follows:
         *  <ul>
         *  <li>the sign field (0:0) holds the sign of the floating-point number;</li>
         *  <li>the (1:1) field holds the exponent in "excess 32" representation;</li>
         *  <li>the (2:5) field holds (the fractional part of) the mantissa, <VAR>f</VAR>.</li> 
         *  </ul>
         *  <p>The convention adopted by Knuth is that <VAR>f</VAR> &lt; 1.
         *  To be normalized, a floating-point number must have  <VAR>f</VAR> >= 1/64.</p>
         *
         *  <p>There are two special cases:</p>
         *  <ul>
         *  <li>zero is represented as <code>&plusmn; 33 00 00 00 00</code>;</li>
         *  <li>a number whose magnitude is too big to be represented switches the
         *      <CODE>OV</CODE> toggle on and defaults to <code>&plusmn; 63 63 63 63 63</code>.</li>
         *  </ul>
         *
         *  @since version 1.4
	 */	
	protected MixWord setAccumulator(double v)
	{
            rA.setSign(v<0 ? -1 : 1);
            v = Math.abs(v);
            // Special case, zero
            if(v==0.0)
            {
                rA.setByte(1, new MixByte(33));
                for(int i = 0; i<4; i++)
                    rA.setByte(REGISTER_WIDTH - i, new MixByte());
                return rA;
            }
	    overflow = v>=Math.pow(64.0, 31.0);
            // Special case, overflow:
            if(overflow)
            {
                for(int i = 1; i<=REGISTER_WIDTH; i++)
                    rA.setByte(i, new MixByte(MixByte.MAX_VALUE));
                return rA;
            }
            /*
            // Determine, by dichotomic search, the exponent p:
            int pmax = 30, pmin = -33;
            do
            {
                int p = (pmax + pmin)/2;
                double m = v*Math.pow(64.0, (double) -p);
                if(m<1.0)
                    pmax = p - 1;
                else if(m>=64.0)
                    pmin = p + 1;
                else
                    pmin = pmax = p;
            }
            while(pmin<pmax);
             */
            int p = (int) Math.floor(Math.log(v)/Math.log(64.0));
            /*
            if(p!=pmax)
                throw new RuntimeException("p = " + p + ", pmax = " + pmax);
             */
            // Excess 33 representation of the exponent:
            rA.setByte(1, new MixByte(33 + p));
            // Calculate the mantissa
            int mantissa = (int) Math.floor(v*Math.pow(64.0, (double)(3 - p)));
            for(int i = 0; i<4; i++)
            {
                rA.setByte(REGISTER_WIDTH - i, new MixByte(mantissa & MixByte.MAX_VALUE));
                mantissa >>= MixByte.N_BITS;
            }
            return rA;
	}
	
	public MixWord getIndexRegister(int i)
	{
		if(i>0 && i<=N_INDEX_REGISTERS)
			return rI[i - 1];
		else
			return new MixWord(ADDRESS_WIDTH);
	}
	
	public void setIndexRegister(int i, int v)
	{
		if(i>0 && i<=N_INDEX_REGISTERS)
			rI[i - 1] = setRegister(ADDRESS_WIDTH, v);
	}
	
	public MixWord getJumpRegister() { return rJ; }
	
	public void setJumpRegister(int v) { rJ = setRegister(ADDRESS_WIDTH, v); }
	
	/** Copy the location pointer to the jump register to prepare for a jump.
	 */
	public void setJumpRegister() { rJ = lp; }

	public MixWord getLocationPtr() { return lp; }
	
	public void setLocationPtr(int addr)
	{
		lp = new MixWord(ADDRESS_WIDTH, addr);
		if(view!=null)
			view.setLocationPointer(addr);
	}
        
        public void setMemoryValue(int addr, MixWord value) {
            memory[addr] = value;
        }
	
	/** Write an integer value to a memory word.
	 */
	public void write(int address, int value)
	{
            write(address, new MixWord(REGISTER_WIDTH, value));
	}
	
	/** Write a memory word.
	 */
	public void write(int address, MixWord value)
	{
            memory[address] = value;
            if(view!=null)
            {
                view.update(address, memory[address]);
                view.refresh();
            }
	}
	
	/** Write a sub-part of a memory word.
	 *  On a <EM>write</EM> operation, the field specification has the
	 *  opposite meaning from the <EM>read</EM> operation: the number
	 *  of bytes in the field is taken from the right-hand of the register
	 *  and shifted <EM>left</EM> if necessary to be inserted into the
	 *  specified field of the destination word.
	 */
	public void write(int address, FieldSpecification fspec, MixWord value)
	{
            memory[address].subWord(fspec, value);
            if(view!=null)
            {
                view.update(address, memory[address]);
                view.refresh();
            }
	}
	
	/** Read the content of a memory word.
	 */
	public MixWord read(int address)
	{
		return memory[address];
	}
	
	/** Attach an I/O unit for both input and output.
	 */
	public void attach(int unit, File f)
	{
		u[unit].setFile(f);
	}
	
	/** Attach an I/O unit to an input stream.
	 */
	public void attachInput(int unit, InputStream is)
	{
		u[unit].setInput(is);
	}
	
	/** Attach an I/O unit to an output stream.
	 */
	public void attachOutput(int unit, PrintStream ps)
	{
		u[unit].setOutput(ps);
	}
	
	/** Get a reference to an I/O unit.
	 */
	public IOUnit unit(int n)
        {
            if(n>=0 && n<N_IO_UNITS)
                return u[n];
            else
                return null;
        }
	
	/** Set the starting location for program execution.
	 */
	public void start(int address)
	{
		setLocationPtr(address);
		halted = false;
	}
	
	/** Perform an execution step.
	 *  Fetch the next instruction, decode it, and execute it.
	 */
	public void step() throws Exception
	{
		if(halted)
			throw new Exception("MIX halted!");
		Instruction instr = new Instruction(memory[lp.increment()]);
		instr.execute(this);
		if(view!=null)
			view.setLocationPointer(lp.intValue());
                if(regView!=null)
                    regView.update(this);
	}
	
	/** Check whether the VM is halted.
	 */
	public boolean isHalted()
	{
            return halted;
	}
	
	/** Halt the VM.
	 */
	public void halt()
	{
            halted = true;
	}
        
        /**
         * Returns the number of cycles elapsed according to the internal clock.
         */
        public long clock()
        {
            return clock;
        }
        
        /**
         * Resets the internal clock.
         */
        public void resetClock()
        {
            clock = 0;
        }
        
        /**
         * Advance the internal clock of the specified number of cycles.
         */
        public void tick(int cycles)
        {
            clock += cycles;
        }
	
	/** Run the VM.
	 */
	public void run() throws Exception
	{
            while(!isHalted())
                step();
	}
	
	/** Dump the content of the memory to a file.
	 */
	public void dump(PrintStream ps)
	{
		for(int i = 0; i<MEMORY_SIZE; i++)
			ps.println("" + i + ":\t" + memory[i]);
	}
	
	/** Load the content of the memory from a file.
	 *  The file must be a dump file produced by the {@link #dump dump()} method
	 *  of this class or by the MIXAL assembler.
	 *
	 *  @return the last address read.
	 */
	public int load(InputStream is) throws IOException
	{
            StreamTokenizer st = new StreamTokenizer(new BufferedReader(new InputStreamReader(is)));

            int address = 0;
            int sign = 1;
            MixWord word = null;
            int state = 0;
            while(st.nextToken()!=st.TT_EOF)
            {
                switch(state)
                {
                    case 0:
                            if(st.ttype!=st.TT_NUMBER)
                                    throw new IOException("Address expected at line " + st.lineno());
                            address = (int) st.nval;
                            break;
                    case 1:
                            if(st.ttype!=':')
                                    throw new IOException("':' expected at line " + st.lineno());
                            break;
                    case 2:
                            if(st.ttype!='+' && st.ttype!='-')
                                    throw new IOException("Sign expected at line " + st.lineno());
                            sign = st.ttype=='+' ? 1 : -1;
                            word = new MixWord(REGISTER_WIDTH);
                            word.setSign(sign);
                            break;
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                            if(st.ttype!=st.TT_NUMBER)
                                    throw new IOException("Byte expected at line " + st.lineno());
                            word.setByte(state - 2, new MixByte((int) st.nval));
                            break;
                }
                if(++state==8)
                {
                    write(address, word);
                    state = 0;
                }
            }
            return address;
	}
}
