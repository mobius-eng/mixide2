package mix.ide;

import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.*;

/** A filter for the file chooser to visualize MIX dump (image) files only.
 *  A MIX dump file is recognized by the extension ".dmp".
 */
public class MixDumpFileFilter extends MixIDEFileFilter
{
    /** Accept all directories and all MIX dump files.
     */
    public boolean accept(File f)
    {
        if(f.isDirectory())
            return true;

        String extension = getExtension(f);
        if(extension!=null)
            return extension.equals("dmp");

        return false;
    }

    /** The description of this filter
     */
    public String getDescription()
    {
        return "MIX dump files";
    }
}
