package mix.ide;

import java.awt.*;
import javax.swing.*;
import mix.vm.*;

/**
 * This class provides a text area that enables the IDE to visualize
 * a snapshot of the contents of the MIX registers, indicators, and
 * the internal clock.
 * 
 * <p>This is a quick-and-dirty implementation. It would be nice to
 * design and implement a graphical representation of the internal
 * status of the MIX machine.</p>
 */
public class RegisterView extends JTextArea
{
    public RegisterView()
    {
        super();
        setFont(new Font("Monospaced", Font.PLAIN, 12));
        MixWord zero = new MixWord(MixVM.REGISTER_WIDTH, 0);
        append("OV = OFF, CMP = '=', clock = 0\n");
        append("A = " + zero + ", X = " + zero + "\n");
        zero = new MixWord(MixVM.ADDRESS_WIDTH, 0);
        for(int i = 1; i<=6; i++)
                append("I" + i + " = " + zero + " = 0\n");
        append("J" + " = " + zero + " = 0\n");
    }

    public void update(MixVM mix)
    {
        String text = "OV = " + (mix.getOV() ? "ON" : "OFF") +
                    ", CMP = '";

        int cmp = mix.getCmp();              
        if(cmp<0)
                text += "<";
        else if(cmp==0)
                text += "=";
        else
                text += ">";

        text += "', clock = " + mix.clock() + "\n";

        text += "A = " + mix.getAccumulator() +
                    ", X = " + mix.getExtension() + "\n";
        for(int i = 1; i<=6; i++)
                text += "I" + i + " = " + mix.getIndexRegister(i) +
                        " = " + mix.getIndexRegister(i).intValue() + "\n";
        text += "J = " + mix.getJumpRegister() +
                " = " + mix.getJumpRegister().intValue() + "\n";
        setText(text);
        paintImmediately(getVisibleRect());
    }

}
