package mix.ide;

import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.*;

/** A filter for the file chooser to visualize MIXAL source files only.
 *  A MIXAL source file is recognized by the extension ".mix".
 */
public class MixalSourceFileFilter extends MixIDEFileFilter
{
    /** Accept all directories and all mix files.
     */
    public boolean accept(File f)
    {
        if(f.isDirectory())
            return true;

        String extension = getExtension(f);
        if(extension!=null)
            return extension.equals("mix");

        return false;
    }

    /** The description of this filter
     */
    public String getDescription()
    {
        return "MIXAL source files";
    }
}
