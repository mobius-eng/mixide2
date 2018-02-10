package mix.asm;

import java.io.*;
import java.util.*;
import mix.vm.*;

/**
 * <p>An assembler session for the MIXAL assembly language.
 * The specifications for the MIXAL assembler are given by means of
 * 13 rules on pages 153-156 of Donald Knuth's
 * <I>The Art of Computer Programming</I>, vol. 1,
 * 3rd Edition, Addison Wesley, Reading, Massachussets, 1997.</p>
 * 
 * <p>This assembler implementation differs from Knuth's specification
 * in an important way: while Knuth's assembler requires that a
 * MIXAL program be formatted according to a rigid scheme where
 * the various field of a program line begin at specified columns,
 * this assembler allows considerably more formatting freedom and
 * allows the programmer to use longer labels. However, this freedom
 * comes at a cost, namely that the assembler has one less clue to
 * decide whether a given token is a label or a mnemonic; therefore,
 * if MIXAL mnemonics like <code>NUM</code> are used as labels,
 * funny errors may occur.</p>
 *
 *  @author Andrea G. B. Tettamanzi
 */
public class MixAssembler
{
	/** State identifiers. */
	private static final int NEWLINE = 0, OPERATION = 1, ARGUMENTS = 2, SKIPTOEOL = 100;
	
	/** Directive and special operation identifiers. */
	private static final int
                NOP = 0,
		HLT = 5,
		EQU = 64,
		ORIG = 65,
		CON = 66,
		ALF = 67,
		END = 68;
		
	/** The source program. */
	protected StreamTokenizer source;
	
	/** The destination memory dump file. */
	protected PrintStream dest;
	
	/** The print stream to which log messages are to be directed. */
	protected PrintStream log;
	
	/** A temporary MIX Virtual Machine to hold the assembled program.
	 */
	MixVM vm;
	
	/**
         * Assembler's state.
         * The initial state of an assembler session is <CODE>NEWLINE</CODE>.
         */
	int state = NEWLINE;
	
	/** Location counter. Rule 10 says:
	 *  <BLOCKQUOTE>
	 *  The assembly process makes use of a value denoted by (*) (called the
	 *  <EM>location counter</EM>), which is initially zero. The value of (*)
	 *  should always be a nonnegative number that can fit in two bytes.
	 *  </BLOCKQUOTE>
	 */
	int address = 0;
	
	/**
         * Start address of the program.
         * A negative value means that the start address has not been assigned.
         */
	int entryPoint = -1;
	
	/** Counter for creating internal symbols. */
	int internalCnt = 0;
	
	/** Map of defined symbols. Rule 3 says:
	 *  <BLOCKQUOTE>
	 *  Each appearance of a symbol in a <CODE>MIXAL</code> program is said to be
	 *  either a "defined symbol" or a "future reference". A <EM>defined symbol</EM>
	 *  is a symbol that has appeared in the <CODE>LOC</CODE> field of a preceeding
	 *  line of this <CODE>MIXAL</CODE> program.
	 *  </BLOCKQUOTE>
	 *  <P>Each defined symbol is a key of the map, to which corresponds its value.</P>
	 */
	Map<String, Integer> definedSymbols;
	
	/** Map of future references.  Rule 3 says:
	 *  <BLOCKQUOTE>
	 *  Each appearance of a symbol in a <CODE>MIXAL</code> program is said to be
	 *  either a "defined symbol" or a "future reference". [...] A <EM>future
	 *  reference</EM> is a symbol that has not yet been defined [...].
	 *  </BLOCKQUOTE>
	 *  <P>Future references are handled as follows: each future reference is a key
	 *  of this map, to which a <CODE>Collection</CODE> of addresses is associated.
	 *  Since future references can occur in A-parts, decribing the address field
	 *  of a MIX instruction, those addresses point to all memory word whose
	 *  address field is to be replaced by the eventual value of the symbol.</P>
	 */
	Map<String, Collection<Integer>> futureReferences;
	
	/** Map of literal constants introduced throughout the program.
	 *  The keys of this map are the expressions enclosed between '='
	 *  found in the program; the associated values are MixWord objects
	 *  which contain the result of evaluating the expressions.
	 */
	Map<String, MixWord> literals;
	
	/** Create a new assembler session on the given MIXAL source program.
	 */
	public MixAssembler(InputStream is)
	{
		// Initialize the source tokenizer:
		source = new StreamTokenizer(new BufferedReader(new InputStreamReader(is)));
		source.whitespaceChars(0, 32);
		source.eolIsSignificant(true);
		source.quoteChar('"');
		source.ordinaryChar('\'');
		source.ordinaryChar('+');
		source.ordinaryChar('-');
		source.ordinaryChar('*');
		source.ordinaryChar('/');
		source.ordinaryChar(':');
		source.ordinaryChar(',');
		source.ordinaryChar('=');
		source.ordinaryChar('(');
		source.ordinaryChar(')');
		
		// Initialize the symbol maps:
		definedSymbols = new HashMap<String, Integer>();
		futureReferences = new HashMap<String, Collection<Integer>>();
		literals = new HashMap<String, MixWord>();

		// Initialize the temporary virtual machine:		
		vm = new MixVM();
		
		// Initialize the print stream for log messages:
		log = System.out;	// by default, the standard output
	}
	
	/** Set the terminal view
	 */
	public void setLogStream(PrintStream ps)
	{
		log = ps;
	}

	/** Switch to a new state.
	 */
	private void go(int nextState)
	{
		state = nextState;
	}
	
	/** Check that the current token is <CODE>c</CODE>, and throw
	 *  an exception otherwise.
	 */
	private void expect(char c)
	throws IOException, MixAssemblerException
	{
            if(source.ttype!=c)
                throw new MixAssemblerException("'" + c + "' expected", source);
            if(source.nextToken()==source.TT_EOF)
                throw new MixAssemblerException("Unexpected end of file", source);
	}
	
	/** Define a new symbol. Rule 10 says:
	 *  <BLOCKQUOTE>
	 *  [...] When the location field of a line is not blank, it must contain
	 *  a symbol that has not been previously defined. The equivalent of that symbol
	 *  is then defined to be the current value of.
	 *  </BLOCKQUOTE>
	 *
	 *  <P>If the symbol is a future reference, its definition triggers its
	 *  substitution in the address field of all instructions pointed to by
	 *  the addresses to which the future reference is mapped. After that, the
	 *  symbol is removed from the map of future references. 
	 */
	private void define(String symbol, int value)
	throws MixAssemblerException
	{
            if(definedSymbols.containsKey(symbol) && !(symbol.length()==1 && symbol.charAt(0)>'0' && symbol.charAt(0)<='9'))
                    throw new MixAssemblerException("Symbol " + symbol + " is already defined", source);
            definedSymbols.put(symbol, new Integer(value));

            if(futureReferences.containsKey(symbol))
            {
                // Write value in the address field of all instructions pointed to
                // by the Collection of addresses associated with symbol, and remove
                // it from the set of future references:
                Collection uses = (Collection) futureReferences.remove(symbol);
                Iterator it = uses.iterator();
                while(it.hasNext())
                {
                    vm.write(((Integer) it.next()).intValue(),
                        new FieldSpecification(0, 2),
                        new MixWord(vm.ADDRESS_WIDTH, value));
                }
            }
	}
	
	/** Look up a symbol to determine its value. If the symbol is not defined,
	 *  a <CODE>SymbolNotFoundException</CODE> is thrown.
 	 */
	private int lookup(String symbol)
	throws MixAssemblerException
	{
            if(!definedSymbols.containsKey(symbol))
                throw new SymbolNotFoundException(symbol, source);
            return ((Integer) definedSymbols.get(symbol)).intValue();
	}
	 
	/** Treat the given symbol as a future reference.
	 *  A pointer to the instruction being assembled is
	 *  inserted in the collection associated with it in the map of
	 *  future references.
	 */
	private void future(String symbol)
	{
            Collection<Integer> uses = null;
            if(futureReferences.containsKey(symbol))
                uses = futureReferences.get(symbol);
            else
                uses = new LinkedList<Integer>();
            uses.add(new Integer(address));
            futureReferences.put(symbol, uses);
            log.println("Future reference: " + symbol);
	}
	
	private int parseAtomicExpression()
	throws IOException, MixAssemblerException
	{
            int sign = 1;
            if(source.ttype=='-')
            {
                sign = -1;
                source.nextToken();
            }
            else if(source.ttype=='+')
                source.nextToken();

            if(source.ttype==source.TT_NUMBER)
                return sign*(int) source.nval;
            if(source.ttype==source.TT_WORD)
                return sign*lookup(source.sval);
            if(source.ttype=='*')
                return sign*address;
            throw new MixAssemblerException("Atomic expression expected", source);	
	}	
	
	/** Parse an expression. Rule 5 says:
	 *  <BLOCKQUOTE>
	 *  An <EM>expression</EM> is either
	 *  <OL>
	 *  <LI>an atomic expression [i.e., a number, a defined symbol, or an asterisk], or</LI>
	 *  <LI>a plus or minus sign followed by an atomic expression, or</LI>
	 *  <LI>an expression followed by a binary operator followed by an atomic expression.</LI>
	 *  </OL>
	 *  </BLOCKQUOTE>
	 */
	private int parseExpression()
	throws IOException, MixAssemblerException
	{
		// Initial value of the expression:
		int value = parseAtomicExpression();
		
		while(source.nextToken()!=source.TT_EOF)
		{
			// Binary operator:
			switch(source.ttype)
			{
                            case '+':
                                source.nextToken();
                                value += parseAtomicExpression();
                                break;
                            case '-':
                                source.nextToken();
                                value -= parseAtomicExpression();
                                break;
                            case '*':
                                source.nextToken();
                                value *= parseAtomicExpression();
                                break;
                            case ':':
                                source.nextToken();
                                value = value*8 + parseAtomicExpression();
                                break;
                            case '/':
                                if(source.nextToken()=='/') // "//"
                                {
                                        source.nextToken();
                                        long tmp = value<<(6*vm.REGISTER_WIDTH);
                                        value = (int) tmp/parseAtomicExpression();
                                }
                                else
                                        value /= parseAtomicExpression();
                                break;
                            case StreamTokenizer.TT_WORD:
                                String localLabel = "" + (int) value;
                                if(localLabel.length()==1 && localLabel.charAt(0)!='0')
                                {
                                    if(source.sval.compareTo("B")==0)
                                    {
                                        // Backward reference to a local label:
                                        value = lookup(localLabel);
                                        break;
                                    }
                                    if(source.sval.compareTo("F")==0)
                                    {
                                        // Forward reference to a local label:
                                        /* IPSE DIXIT: 
                                         * "There's a nice trick for implementing local labels
                                         * in one pass by temporarily linking future references
                                         * backwards and then patching the instructions when the
                                         * actual location becomes known. (I use it, for example,
                                         * in the MMIXAL assembler.)"
                                         * -- Don Knuth, personal communication, April 21, 2008
                                         */
                                        future(localLabel);
                                        source.nextToken();
                                        if(source.ttype=='+' || source.ttype=='-' || source.ttype=='*' || source.ttype=='/')
                                                throw new MixAssemblerException("Future reference " + localLabel +
                                                        "F cannot be part of an expression", source);
                                        return 0;
                                    }
                                }
                                // otherwise, just fall through to the default...
                            default:
                                log.println("Expression value = " + value);
                                return value;
			}
		}
		return value;
	}	
		
	/** Parse an A-part. Rule 6 says:
	 *  <BLOCKQUOTE>
	 *  An <EM>A-part</EM> (which is used to describe the address field of a
	 *  MIX instruction) is either
	 *  <OL>
	 *  <LI>vacuous (denoting the value zero), or</LI>
	 *  <LI>an expression, or</LI>
	 *  <LI>a future reference (denoting the eventual equivalent of the symbol;
	 *      see rule 13), or</LI>
	 *  <LI>a literal constant (denoting a reference to an internally created
	 *      symbol; see rule 12)</LI>
	 *  </OL>
	 *  </BLOCKQUOTE>
	 */
	private int parseAddressPart()
	throws IOException, MixAssemblerException
	{
		if(source.ttype==',' || source.ttype=='(') // vacuous
			return 0;
		if(source.ttype=='=') // literal constant
		{
			source.nextToken();
			int w = parseExpression();
			expect('=');
			// create an internal symbol for this literal:
			String symbol = "@" + (++internalCnt);
			future(symbol);
			literals.put(symbol, new MixWord(vm.REGISTER_WIDTH, w));
			return 0;
		}
		if(source.ttype==source.TT_WORD && !definedSymbols.containsKey(source.sval))
		{	// future reference
			String symbol = source.sval;
			future(symbol);
			source.nextToken();
			if(source.ttype=='+' || source.ttype=='-' || source.ttype=='*' || source.ttype=='/')
				throw new MixAssemblerException("Future reference " + symbol +
					" cannot be part of an expression", source);
			return 0;
		}
		// expression
		return parseExpression();
	}
	
	/** Parse an index part. Rule 7 says:
	 *  <BLOCKQUOTE>
	 *  An <EM>index part</EM> (which is used to describe the index field of
	 *  a MIX instruction) is either
	 *  <OL>
	 *  <LI>vacuous (denoting the value zero), or</LI>
	 *  <LI>a comma followed by an expression (denoting the value of that
	 *      expression.</LI>
	 *  </OL>
	 *  </BLOCKQUOTE>
	 */
	private int parseIndexPart()
	throws IOException, MixAssemblerException
	{
		if(source.ttype==',')
		{
			source.nextToken();
			return parseExpression();
		}
		// vacuous:
		return 0;
	}
	
	/** Parse an F-part. Rule 8 says:
	 *  <BLOCKQUOTE>
	 *  An <EM>F-part</EM>, which is used to describe the F-field of a MIX
	 *  instruction, is either
	 *  <OL>
	 *  <LI>vacuous (denoting the normal F-setting [...]), or</LI>
	 *  <LI>a left parenthesis followed by an expression followed by a right
	 *      parenthesis (denoting the value of that expression.</LI>
	 *  </OL>
	 *  </BLOCKQUOTE>
	 */
	private int parseFieldPart(int normal)
	throws IOException, MixAssemblerException
	{
		if(source.ttype=='(')
		{
			source.nextToken();
			int expr = parseExpression();
			expect(')');
			return expr;
		}
		// vacuous:
		return normal;
	}
	
	/** Parse a W-part. Rule 9 says:
	 *  <BLOCKQUOTE>
	 *  A <EM>W-value</EM> (which is used to describe a <EM>full-word</EM>
	 *  MIX constant) is either
	 *  <OL>
	 *  <LI>an expression followed by an F-part (in which case a vacuous
	 *      F-part denotes (0:5)), or</LI>
	 *  <LI>a W-value followed by a W-value of the form 1.</LI>
	 *  </OL>
	 *  </BLOCKQUOTE>
	 *  Multiple W-values (W-values separated by commas) are overwritten.
	 */
	private int parseWordValue()
	throws IOException, MixAssemblerException
	{
		MixWord wval = new MixWord(vm.REGISTER_WIDTH);
		
		while(source.ttype!=source.TT_EOF)
		{
			int expr = parseExpression();
			int f = parseFieldPart(5);
			wval.subWord(new FieldSpecification(f), new MixWord(vm.REGISTER_WIDTH, expr));
			if(source.ttype!=',')
				break;
			source.nextToken();
		}
		return wval.intValue();
	}
	
	/** Parse an alphanumeric value, consisting of a quote-enclosed string
	 *  of five characters.
	 */
	private String parseStringValue()
	throws MixAssemblerException
	{
		if(source.ttype!='"')
			throw new MixAssemblerException("Alphanumeric constant expected", source);
		if(source.sval.length()!=5)
			throw new MixAssemblerException("Alphanumeric constants must be 5-character long", source);
		return source.sval;
	}
	
	
	/** Return the current line number.
	 */
	public int lineno()
	{
		if(source!=null)
			return source.lineno();
		return -1;
	}
	
	/** Compile the source program into the given memory dump file.
	 */
	public void compile(PrintStream ps) throws IOException, MixAssemblerException
	{
            Opcode op = null;
            String loc = null;
            boolean ended = false; // will be set to true upon encountering the END directive

            // Set ps as the destination memory dump file:
            dest = ps;

            while(source.nextToken()!=source.TT_EOF)
            {
                switch(state)
                {
                    case NEWLINE: // we are at the beginning of a new line:
                        if(source.ttype=='*') // this line is a comment
                            go(SKIPTOEOL);
                        else if(source.ttype==source.TT_WORD)
                        {
                            op = new Opcode(source.sval);
                            if(ended)
                                throw new MixAssemblerException("No instruction, directive or label definition allowed after the END directive.",
                                        source);
                            if(op.isUnknown()) // this is a symbol definition
                            {
                                    loc = source.sval;
                                    go(OPERATION);
                            }
                            else
                            {
                                    loc = null;
                                    go(ARGUMENTS);
                            }
                        }
                        else if(source.ttype==source.TT_NUMBER)
                        {
                            // this should be a local label definition, of the form [1-9]H:
                            String localLabel = "" + (int) source.nval;
                            if(source.nextToken()!=source.TT_WORD || localLabel.length()>1 || localLabel.charAt(0)=='0')
                                throw new MixAssemblerException("Symbol, directive, or opcode expected",
                                        source);
                            if(source.sval.compareTo("H")!=0)
                                throw new MixAssemblerException("Symbol, directive, or opcode expected",
                                        source);
                            // OK, this is definitely a local label definition!
                            loc = localLabel;
                            go(OPERATION);
                        }
                        else if(source.ttype!=source.TT_EOL)
                            throw new MixAssemblerException("Symbol, directive, or opcode expected",
                                    source);
                        break;
                    case OPERATION: // we expect to read an operation symbol:
                        if(source.ttype!=source.TT_WORD)
                                throw new MixAssemblerException("Directive or opcode expected", source);
                        op = new Opcode(source.sval);
                        if(op.isUnknown())
                                throw new MixAssemblerException("Directive or opcode unknown", source);
                        /*
                        Rule 11 says: After processing the LOC field as described in rule 10,
                        the assembly process depends on the value of the OP field.
                        */
                        go(ARGUMENTS);
                        break;
                    case ARGUMENTS: // we expect to read the arguments of an instruction:
                        log.println("LOC = " + loc);
                        log.println("OP = " + op.mnemonic());
                        // Rule 11 says: [...]	There are six possibilities for OP:
                        switch(op.code())
                        {
                                case EQU:
                                        /*
                                        b) OP is "EQU". The ADDRESS should be a W-value (see rule 9).
                                           If the LOC field is nonblank, the equivalent of the symbol
                                           appearing there is set equal to the value specified in
                                           ADDRESS. This rule takes precedence over rule 10.
                                           The value of (*) is unchanged. [...]
                                        */
                                        int W = parseWordValue();
                                        if(loc!=null)
                                                define(loc, W);
                                        break;
                                case ORIG:
                                        /*
                                        c) OP is "ORIG". The ADDRESS should be a W-value (see rule 9);
                                           the location counter, (*), is set to this value. [...]
                                        */
                                        int newAddress = parseWordValue();
                                        // N.B.: parseWordValue must be done before defining the symbol,
                                        // otherwise the new symbol definition would be used instead of the
                                        // old. This would be problematic especially with local labels!
                                        if(loc!=null)
                                                define(loc, address);
                                        address = newAddress;
                                        break;
                                case CON:
                                        /*
                                        d) OP is "CON". The ADDRESS should be a W-value (see rule 9);
                                           the effect is to assemble a word, having this value,
                                           into the location specified by (*), and to advance (*) by 1.
                                        */
                                        if(loc!=null)
                                                define(loc, address);
                                        vm.write(address++, parseWordValue());
                                        break;
                                case ALF:
                                        /*
                                        e) OP is "ALF". The effect is to assemble the word of
                                           character codes formed by the first five characters of the
                                           address field, otherwise behaving like CON.
                                        N.B.: here we depart from Knuth's syntax in that the five
                                        characters must be enclosed between quotes.
                                        */
                                        String s = parseStringValue();
                                        MixWord alf = new MixWord(vm.REGISTER_WIDTH);
                                        for(int i = 0; i<5; i++)
                                                alf.setByte(i + 1, MixChar.number(s.charAt(i)));
                                        if(loc!=null)
                                                define(loc, address);
                                        vm.write(address++, alf);
                                        break;
                                case END:
                                        entryPoint = parseWordValue();
                                        ended = true;
                                        break;
                                case NOP: // NOP does not have arguments! 
                                case HLT: // HLT, NUM, and CHAR (same opcode) do not have arguments!
                                        vm.write(address, op.code());
                                        vm.write(address,
                                                new FieldSpecification(4, 4),
                                                new MixWord(vm.REGISTER_WIDTH, parseFieldPart(op.defaultVariant())));
                                        if(loc!=null)
                                                define(loc, address);
                                        address++;
                                        break;
                                default:
                                        /*
                                        a) OP is a symbolic MIX operator [...]. In this case the
                                           ADDRESS should be an A-part (rule 6), followed by an
                                           index part (rule 7), followed by an F-part (rule 8).
                                           We thereby obtain four values: C, F, A, and I.
                                           The effect is to assemble the word determined by the
                                           sequence

                                             LDA C
                                             STA WORD
                                             LDA F
                                             STA WORD(4:4)
                                             LDA I
                                             STA WORD(3:3)
                                             LDA A
                                             STA WORD(0:2)

                                           into the location specified by (*), and to advance (*) by 1.
                                        */
                                        vm.write(address, op.code());
                                        vm.write(address,
                                                new FieldSpecification(0, 2),
                                                new MixWord(vm.ADDRESS_WIDTH, parseAddressPart()));
                                        vm.write(address,
                                                new FieldSpecification(3, 3),
                                                new MixWord(vm.REGISTER_WIDTH, parseIndexPart()));
                                        vm.write(address,
                                                new FieldSpecification(4, 4),
                                                new MixWord(vm.REGISTER_WIDTH, parseFieldPart(op.defaultVariant())));
                                        if(loc!=null)
                                                define(loc, address);
                                        address++;
                        }
                        if(source.ttype==source.TT_EOL)
                                go(NEWLINE);
                        else
                                go(SKIPTOEOL);
                        break;
                    case SKIPTOEOL: // we are skipping comments to the end of line:
                        if(source.ttype==source.TT_EOL)
                                go(NEWLINE);
                        break;
                    default:
                        throw new MixAssemblerException("Undefined state.", source);
                }
            }
            if(entryPoint<0)
                    throw new MixAssemblerException("Unexpected end of file.", source);

            // All future references that are left at this stage must be allocated,
            // as prescribed by rule 13.
            Iterator it = futureReferences.keySet().iterator();
            while(it.hasNext())
            {
                    String symbol = (String) it.next();
                    if(literals.containsKey(symbol))
                            vm.write(address, (MixWord) literals.get(symbol));
                    else
                            vm.write(address, 0);
                    // N.B.: cannot use "define(symbol, address++);", because
                    // this would modify the collection underlying the it
                    // iterator, causing a ConcurrentModificationException!
                    definedSymbols.put(symbol, new Integer(address));
                    // Write value in the address field of all instructions pointed to
                    // by the Collection of addresses associated with symbol, and remove
                    // it from the set of future references:
                    Collection uses = (Collection) futureReferences.get(symbol);
                    Iterator use = uses.iterator();
                    while(use.hasNext())
                    {
                            vm.write(((Integer) use.next()).intValue(),
                                    new FieldSpecification(0, 2),
                                    new MixWord(vm.ADDRESS_WIDTH, address));
                    }
                    address++;
            }

            // Print the symbol table for debugging purposes:
            it = definedSymbols.keySet().iterator();
            log.println("DEFINED SYMBOLS:");
            while(it.hasNext())
            {
                    String symbol = (String) it.next();
                    log.println(symbol + " = " + lookup(symbol));
            }

            // Dump the assembled program to the destination file.
            vm.dump(dest);
            dest.println(entryPoint);
	}
}
