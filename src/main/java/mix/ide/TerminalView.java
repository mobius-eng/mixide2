package mix.ide;

import java.io.*;
import java.awt.*;
import javax.swing.*;
import mix.vm.*;

/** This class provides a text area that enables the IDE to visualize
 *  the output directed to the MIX terminal.
 */
public class TerminalView extends JTextArea
{
	/** Create a new terminal.
	 */
	public TerminalView()
	{
            super();
            setFont(new Font("Monospaced", Font.PLAIN, 12));
	}
	
	public PrintStream getPrintStream()
	{
            return new PrintStream(new JTextAreaOutputStream(this));
	}	
}
