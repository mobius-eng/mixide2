package mix.ide;

import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.*;

/** Abstract base class for file filters used by the MixIDE environment.
 */
public abstract class MixIDEFileFilter extends FileFilter
{
    /** Get the extension of a file name.
     */
    protected static String getExtension(File f)
    {
        String ext = null;
        String fileName = f.getName();
        int i = fileName.lastIndexOf('.');

        if (i>0 &&  i<fileName.length() - 1)
            ext = fileName.substring(i + 1).toLowerCase();
        return ext;
    }
    
    /** Remove the extension from a file name.
     */
    public static String getRootName(File f)
    {
        String name = null;
        String fileName = f.getName();
        int i = fileName.lastIndexOf('.');

        if (i>0 &&  i<fileName.length() - 1)
            name = fileName.substring(0, i);
        return name;
    }
}
