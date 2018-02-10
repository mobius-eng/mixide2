package mix.asm;

import java.util.*;
import java.io.*;
import javax.swing.*;

class MixalSymbolTable extends Properties
{
	public MixalSymbolTable()
	{
		super();
		try
		{
			// load(new FileInputStream("opcodes.txt"));
			load(ClassLoader.getSystemResourceAsStream("opcodes.txt"));
		}
		catch(Exception e)
		{
			JOptionPane.showMessageDialog(null,
				e.toString(),
				"Opcode table not found: the Assembler will not work.",
				JOptionPane.ERROR_MESSAGE);
			throw new Error("Fatal error: opcode table not found.");
		}
		System.out.println("Opcode table loaded.");
	}
}
