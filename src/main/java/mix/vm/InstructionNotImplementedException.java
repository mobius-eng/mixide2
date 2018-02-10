package mix.vm;


class InstructionNotImplementedException extends Exception
{
	public InstructionNotImplementedException(Instruction instruction, String mnemonic)
	{
		super("" + instruction + " (" + mnemonic + ")"); 
	}
}
