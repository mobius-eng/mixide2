/*
 * Process.java
 *
 * Created on May 1, 2008, 9:19 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package mix.ide;

import mix.vm.MixVM;

/**
 * A thread used to run the MIX machine as a separate process.
 *
 * @author Andrea G. B. Tettamanzi
 */
public class Process extends Thread
{
    protected MixVM mix;
    
    /** Creates a new process to run the given MIX virtual machine. */
    public Process(MixVM vm)
    {
        mix = vm;
    }
    
    /** Runs the MIX virtual machine in a separate thread. */
    public void run()
    {
        try
        {
            mix.run();
        }
        catch(Exception ex)
        {
            MixIDE.runtimeError(ex);
        }
    }
}
