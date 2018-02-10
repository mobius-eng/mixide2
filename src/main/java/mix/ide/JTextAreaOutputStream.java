package mix.ide;

import java.io.*;
import javax.swing.*;

/**
 * This class allows to write to a JTextArea as if it were
 * an OutputStream.
 */
public class JTextAreaOutputStream extends OutputStream
{
    protected JTextArea textArea;

    /**
     * Create a new output stream linked to the given 
     * target JTextArea.
     */
    public JTextAreaOutputStream(JTextArea target)
    {
        super();
        textArea = target;
    }

    /**
     * Implements the abstract write() method of the parent class
     * to append a character to the target JTextArea.
     */
    public void write(int b)
    {	
        textArea.append(Character.toString((char) b));
        // On a "new line", refresh the text area so that the line just
        // written becomes immediately visible:
        if((char) b=='\n')
        {
            textArea.setCaretPosition(textArea.getText().length());
            textArea.paintImmediately(textArea.getVisibleRect());
        }
    }
}
