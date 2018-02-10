package mix.ide;
 
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import javax.help.*;

import mix.vm.*;
import mix.asm.*;
import mix.edit.*;

/**
 * The main window and class of the MIX Integrated Development Environment.
 *
 * @author Andrea G. B. Tettamanzi
 */
public class MixIDE extends JFrame
{
    /** A reference to the one and only instance of this class. */
    public static MixIDE mainFrame;
    
    /** A thread for executing the MIX machine. */
    protected Process executionThread;
    
    /** The currently open MIXAL source file. */
    protected File srcFile;
    
    /** The default file to which a MIXAL source file is to be assembled. */
    protected File destFile;
    
    /** The version identifier. */
    private static final String version = "2.0.0";
    
    /** The menu bar. */
    protected JMenuBar menuBar;

    /** The text area containing a snapshot of the MIX memory. */
    protected MemoryView memoryView;

    /** The text area containing a snapshot of the MIX registers. */
    protected RegisterView registerView;

    /** The text area to show the output directed to the MIX terminal. */
    protected TerminalView terminalView;

    /** The MIX virtual machine. */
    protected MixVM mix;

    /** The start address for executing the program. */
    protected int startAddress;

    /** File chooser dialog. */
    private JFileChooser chooser;

    /** File filter for MIXAL source files */
    private MixIDEFileFilter mixalSourceFiles = new MixalSourceFileFilter();

    /** File filter for MIX dump files */
    private MixIDEFileFilter mixDumpFiles = new MixDumpFileFilter();

    /** The help set for this application. */
    public HelpSet hs = null;

    /** The whole help system is centered around an object of class HelpBroker
     *  which acts as a middleman between the application and the help viewer,
     *  which can be completely ignored.
     */
    public HelpBroker hb;

    /** URL of the help set to be loaded by the HelpBroker when the
     *  application starts. It must point to a file "MixIDE.hs"
     *  in the "help" directory under the project root.
     */
    private static final String HSNAME = "help/MixIDE.hs";

    private SimpleTextEditor editor; 
    private JFrame logjframe;
    private JScrollPane logjscrollpane;
    private RowModifier rowMod;
    private RegModifier regMod;
    private LinePositioner lineP;
	
    /**
     * Creates and initializes the main IDE frame with its components.
     * This constructor is protected because this class is a singleton
     * and the only one authorized to create one (and the only) instance
     * of itself.
     */
    protected MixIDE()
    {
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                if(editor!=null)
                    editor.dispatchEvent(e);
                dispose();
                System.exit(0);
            }
        });
        initComponents();
        reset();
    }

    /**
     * Resets the MIX virtual machine and the views.
     *
     * @author Stefano Marino
     */	
    private void reset()
    {
        // Create a new clean MIX machine:
        mix = new MixVM();
		
        mix.setView(memoryView);
        mix.setRegisterView(registerView);
        if(regMod != null)
            regMod.setVM(mix);
        
        // Connect the MIX machine to its terminal:
        mix.setTerminal(terminalView);

        if(registerView!=null)
            registerView.update(mix);
        	
        startAddress = 0;        
        lineP = null;        
    }
    
    /** Initializes the menus and the text area inside the frame, as well
     *  as the help subsystem. 
     */
    private void initComponents()
    {
        // Load the help set...
        try
        {
            // Perhaps instead of taking the helpset name from a string constant,
            // one could obtain it from a property file:
            hs = new HelpSet(null, ClassLoader.getSystemResource(HSNAME));
        }
        catch(Exception e)
        {
            System.out.println("Help set " + HSNAME + " not found.");
            dispose();
            System.exit(1);
        }
        catch(NoClassDefFoundError ncdferr)
        {
            int option = JOptionPane.showConfirmDialog(this,
                "MixIDE Could not find the JavaHelp library: on-line help will not be available.\n" +
                "You can solve the problem by placing the file 'jh.jar' in the same directory as MixIDE-n.n.jar,\n" +
                "or in the 'lib' directory of your Java Runtime Environment.\n\n" +
                "Do you want to continue anyway?",
                "MIX IDE v. " + version + " Warning",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            if(option!=JOptionPane.YES_OPTION)
            {
                dispose();
                System.exit(2);
            }
        }			
        // ... and create the corresponding HelpBroker, which the application
        // will invoke to visualize its contents:
        if(hs!=null)
            hb = hs.createHelpBroker();

        menuBar = new JMenuBar();

        // Set up the "MIX" menu with all its menu items:
        JMenu vm = new JMenu("MIX");
        vm.setMnemonic(java.awt.event.KeyEvent.VK_M);

        JMenuItem vm_load = new JMenuItem("Load...");
        vm_load.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK));
        vm_load.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt) { load(false); }
        });
        vm.add(vm_load); 

        JMenuItem vm_run = new JMenuItem("Run");
        vm_run.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
        vm_run.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt) { run(); }
        });
        vm.add(vm_run); 

        JMenuItem vm_halt = new JMenuItem("Stop");
        vm_halt.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        vm_halt.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt) { stop(); }
        });
        vm.add(vm_halt); 

        JMenuItem vm_step = new JMenuItem("Step");
        vm_step.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0));
        vm_step.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt) { step(); }
        });
        vm.add(vm_step);

        JMenuItem vm_over = new JMenuItem("Step over");
        vm_over.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, 0));
        vm_over.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt) { over(); }
        });
        vm.add(vm_over);		

        JMenuItem vm_reset = new JMenuItem("Reset...");
        vm_reset.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.ALT_DOWN_MASK));
        vm_reset.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt) { reset(); }
        });
        vm.add(vm_reset);

        JMenuItem vm_dump = new JMenuItem("Dump...");
        vm_dump.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK));
        vm_dump.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt) { dump(); }
        });
        vm.add(vm_dump);

        menuBar.add(vm);
		
        // Set up the "I/O" menu with all its menu items:
        JMenu io = new JMenu("I/O");
        io.setMnemonic((int) 'I');
		
        JMenuItem io_attach = new JMenuItem("Tape 0...");
        io_attach.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt) { attach(0); }
        });
        io.add(io_attach);

        io_attach = new JMenuItem("Tape 1...");
        io_attach.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt) { attach(1); }
        });
        io.add(io_attach);

        io_attach = new JMenuItem("Tape 2...");
        io_attach.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt) { attach(2); }
        });
        io.add(io_attach);

        io_attach = new JMenuItem("Tape 3...");
        io_attach.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt) { attach(3); }
        });
        io.add(io_attach);

        io_attach = new JMenuItem("Tape 4...");
        io_attach.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt) { attach(4); }
        });
        io.add(io_attach);

        io_attach = new JMenuItem("Tape 5...");
        io_attach.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt) { attach(5); }
        });
        io.add(io_attach);

        io_attach = new JMenuItem("Tape 6...");
        io_attach.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt) { attach(6); }
        });
        io.add(io_attach);

        io_attach = new JMenuItem("Tape 7...");
        io_attach.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt) { attach(7); }
        });
        io.add(io_attach);

        io_attach = new JMenuItem("Disk 8...");
        io_attach.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt) { attach(8); }
        });
        io.add(io_attach);

        io_attach = new JMenuItem("Disk 9...");
        io_attach.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt) { attach(9); }
        });
        io.add(io_attach);

        io_attach = new JMenuItem("Disk 10...");
        io_attach.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt) { attach(10); }
        });
        io.add(io_attach);

        io_attach = new JMenuItem("Disk 11...");
        io_attach.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt) { attach(11); }
        });
        io.add(io_attach);

        io_attach = new JMenuItem("Disk 12...");
        io_attach.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt) { attach(12); }
        });
        io.add(io_attach);

        io_attach = new JMenuItem("Disk 13...");
        io_attach.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt) { attach(13); }
        });
        io.add(io_attach);

        io_attach = new JMenuItem("Disk 14...");
        io_attach.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt) { attach(14); }
        });
        io.add(io_attach);

        io_attach = new JMenuItem("Disk 15...");
        io_attach.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt) { attach(15); }
        });
        io.add(io_attach);

        io_attach = new JMenuItem("Paper tape 20...");
        io_attach.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt) { attach(20); }
        });
        io.add(io_attach);

        menuBar.add(io);

        // Set up the "Assembler" menu with all its menu items:
        JMenu asm = new JMenu("Assembler");
        asm.setMnemonic((int) 'A');
		
        JMenuItem asm_compile = new JMenuItem("Compile...");
        asm_compile.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt) { compile(false); }
        });
        asm.add(asm_compile);

        menuBar.add(asm);

        // Set up the "Tools" menu with all its menu items:
        JMenu tools = new JMenu("Tools");
        tools.setMnemonic((int)'T');

        JMenuItem reload = new JMenuItem("Recompile and reload");
        reload.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
        reload.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent actionevent)
            {
                compile(true);
                load(true);
            }
        });

        JMenuItem edit = new JMenuItem("Edit Source");
        edit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK));
        edit.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent actionevent)
            {
            	edit();
            }
        });

        JMenuItem modword = new JMenuItem("Modify Word");
        modword.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK));
        modword.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent actionevent)
            {
            	modify();
            }
        });

        JMenuItem modreg = new JMenuItem("Modify Registers");
        modreg.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_DOWN_MASK));
        modreg.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent actionevent)
            {
            	modifyReg();
            }
        });

        JMenuItem linepos = new JMenuItem("Set Location Pointer");
        linepos.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK));
        linepos.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent actionevent)
            {
            	setPosition();
            }
        });
        
        tools.add(reload);
        tools.add(edit);
        tools.add(modword);
        tools.add(modreg);
        tools.add(linepos);
        menuBar.add(tools);
		
        // Set up the "Info" menu with all its menu items:
        JMenu info = new JMenu("Info");

        // Help topics:
        JMenuItem info_help = new JMenuItem("Help topics...");
        if(hs!=null)
                info_help.addActionListener(new CSH.DisplayHelpFromSource(hb));
        else
                info_help.setEnabled(false);
        info.add(info_help);

        JMenuItem info_about = new JMenuItem("About...");
        info_about.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt) { about(); }
        });
        info.add(info_about);

        menuBar.add(info);

        // Done setting up menus; set up menu bar: 
        setJMenuBar(menuBar);

        // Create a file chooser once and for all:
        chooser = new JFileChooser();
        chooser.addChoosableFileFilter(mixDumpFiles);
        chooser.addChoosableFileFilter(mixalSourceFiles);

        // Set up the three views:
        // Memory View...
        memoryView = new MemoryView(this);
        JScrollPane scrollPane = new JScrollPane(memoryView,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        memoryView.setEditable(false);
        getContentPane().add(scrollPane);

        // ... Register View...
        registerView = new RegisterView();
        registerView.setEditable(false);
        JDialog regs = new JDialog(this, "MIX Registers");
        regs.setSize(400, 200);
        regs.setLocation(512, 0);
        regs.getContentPane().add(registerView);
        regs.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        regs.setVisible(true);

        // ... and Terminal View.
        terminalView = new TerminalView();
        JFrame term = new JFrame("MIX Terminal");
        JScrollPane termsp = new JScrollPane(terminalView,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        terminalView.setEditable(false);
        term.setSize(400, 400);
        term.setLocation(512, 200);
        term.getContentPane().add(termsp);
        term.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        term.setVisible(true);

        // Managing context-sensitive help is easy.
        // Example: enable help requests by means of F1 when
        // the user in is the main frame, by calling the topic
        // with identifier "memview" in help set hs:
        if(hs!=null)
                hb.enableHelpKey(this, "memview", hs);

    	// Add a mouse listener to the memory and register view, so that
    	// the user can double-click on memory words or register to modify
    	// them (author: Stefano Marino)
        memoryView.addMouseListener(new MouseAdapter()
        {
            public void mouseClicked(MouseEvent e)
            {
                if(e.getClickCount()>1)
                        modify();	// Modify Word
     	    }       	
     	});
     	
        registerView.addMouseListener(new MouseAdapter()
        {
            public void mouseClicked(MouseEvent e)
            {
                if(e.getClickCount()>1)
                        modifyReg();	// Modify Registers
     	    }
     	});
    }
	
    /** Displays a dialog window allowing the user to set the location pointer.
     * 
     *  @author Stefano Marino
     */
    private void setPosition()
    {
        if(lineP==null)
            lineP = new LinePositioner(memoryView); 
        else
            lineP.update(memoryView);
        lineP.setVisible(true);
    }
    
    /** Open the Modify Registers dialog to allow the user to
     *  modify the registers.
     *
     *  @author Stefano Marino
     */
    protected void modifyReg()
    {
        if(regMod==null)
                regMod = new RegModifier(registerView,mix); 
        else
                regMod.refresh();
        regMod.setVisible(true);
    }

    /** Open the Modify dialog to allow the user to edit a memory word.
     *  After closing the dialog, the memory view gets updated.
     *
     *  @author Stefano Marino
     */
    protected void modify()
    {
        if(rowMod==null)
            rowMod = new RowModifier(memoryView); 
    	else
            rowMod.update(memoryView);
    	rowMod.setVisible(true);
    }
    
    /** Open the MIXAL source editor.
     *
     *  @author Stefano Marino
     */
    protected void edit()
    {
    	if(editor==null)
    	{
            if(srcFile!=null)
                editor = new SimpleTextEditor(srcFile);
            else	
                editor = new SimpleTextEditor();
    	}
    	editor.setVisible(true);	
    }
    
    /** Show the "About..." message box with version information and credits.
     */
    protected void about()
    {
    	JOptionPane.showMessageDialog(this,
            "MIX IDE version " + version +"\n" +
            "(c) 2005-2008 by Andrea G. B. Tettamanzi\n\n" +
            "Thanks to Stefano Marino for the editing extensions\n\n" +
            "This software is provided \"as is\" for instructional purposes only.\n" +
            "Materiale didattico del corso 'Programmazione degli Elaboratori',\n" +
            "A.A. 2004/2005 - A.A. 2007/2008, Corsi di Laurea della sede di Crema,\n" +
            "Università degli Studi di Milano.");
    }

    /** Compile a MIXAL assembly program into a memory configuration.
     *  For greater flexibility and modularity, the MIX memory configuration
     *  is not loaded into the MIX virtual machine, but stored into a
     *  file, which later can be loaded by the user and executed.
     */
    protected void compile(boolean recompile)
    {
        MixAssembler asm = null;

        // Have the user choose a MIXAL source file
        if(!recompile || srcFile==null || destFile==null)
        {
            chooser.setFileFilter(mixalSourceFiles);
            int returnVal = chooser.showOpenDialog(this);
            if(returnVal!=JFileChooser.APPROVE_OPTION)
                return;
        }

        String fileName = "";
        InputStream in = null;
        try
        {
            File sourceFile = (srcFile==null || !recompile) ? chooser.getSelectedFile() : srcFile;
            fileName = sourceFile.getName();
            System.out.println("Compiling file " + fileName);
            in = new FileInputStream(sourceFile);
            srcFile = sourceFile;

            // Create a new assembler session:
            asm = new MixAssembler(in);	
        }
        catch(FileNotFoundException e)
        {
            JOptionPane.showMessageDialog(this,
                "Source file not found.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Have the user choose the dump file
        if(!recompile || srcFile==null || destFile==null)
        {
            String proposedName = MixIDEFileFilter.getRootName(chooser.getSelectedFile()) + ".dmp";
            chooser.setSelectedFile(new File(proposedName));
            chooser.setFileFilter(mixDumpFiles);
            int returnVal = chooser.showSaveDialog(this);
            if(returnVal != JFileChooser.APPROVE_OPTION)
                return;
        }

        // Open a new console window to show assembler messages:
        TerminalView asmView = new TerminalView();
        JFrame term = new JFrame("Assembler Log for " + fileName);
        JScrollPane termsp = new JScrollPane(asmView,
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        asmView.setEditable(false);
        term.setSize(400, 600);
        term.setLocation(100, 100);
        term.getContentPane().add(termsp);
        term.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        term.setVisible(true);
        asm.setLogStream(asmView.getPrintStream());

        PrintStream out = null;
        try
        {
            File dumpFile = (destFile==null || !recompile) ? chooser.getSelectedFile() : destFile;
            destFile = dumpFile;
            fileName = dumpFile.getName();
            System.out.println("Compiling to file " + fileName);
            out = new PrintStream(new FileOutputStream(dumpFile));

            asmView.requestFocus();
            asm.compile(out);
        }
        catch(IOException ioe)
        {
            JOptionPane.showMessageDialog(this,
                ioe.toString(),
                "I/O Error",
                JOptionPane.ERROR_MESSAGE);
            try
            {
                if(out!=null) out.close();
                if(in!=null) in.close();
            }
            catch(Exception e) {}
        }
        catch(MixAssemblerException mae)
        {
            JOptionPane.showMessageDialog(this,
                mae.toString(),
                "Assembler Error",
                JOptionPane.ERROR_MESSAGE);
            try
            {
                if(out!=null) out.close();
                if(in!=null) in.close();
            }
            catch(Exception e) {}
        }
    }
	
    /** Load a memory configuration (a program) into the machine.
     */
    protected void load(boolean reload)
    {
        if(!reload || destFile==null)
        {
            // Have the user choose a program file
            chooser.setFileFilter(mixDumpFiles);
            int returnVal = chooser.showOpenDialog(this);
            if(returnVal != JFileChooser.APPROVE_OPTION)
                return;
        }

        try
        {
            File imageFile = (!reload || destFile==null) ? chooser.getSelectedFile() : destFile;
            destFile = imageFile;
            String fileName = imageFile.getName();
            System.out.println("Loading file " + fileName);
            InputStream in = new FileInputStream(imageFile);
            // Disconnect the MIX machine from the memory view:
            mix.setView(null);
            startAddress = mix.load(in);
            if(startAddress==3999)
                    startAddress = 0;
            mix.setView(memoryView);    // Reconnect to the memory view
            memoryView.setLocationPointer(startAddress);
            memoryView.setCurrentAddress(startAddress);
            mix.halt();
        }
        catch(FileNotFoundException e)
        {
            JOptionPane.showMessageDialog(this,
                    "Program file not found.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        catch(IOException ioe)
        {
            JOptionPane.showMessageDialog(this,
                    ioe.toString(),
                    "I/O Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Attach a MIX I/O unit to a file for input and output.
     */
    protected void attach(int unit)
    {
            // Have the user choose a file
            chooser.setFileFilter(chooser.getAcceptAllFileFilter());
        int returnVal = chooser.showOpenDialog(this);
        if(returnVal != JFileChooser.APPROVE_OPTION)
            return;
            mix.attach(unit, chooser.getSelectedFile());
    }
	
    /**
     * Start execution of the MIX machine.
     * The MIX machine is executed in a separate thread, so that the
     * user can interact with the IDE while the MIX machine is running.
     */
    protected void run()
    {
        if(executionThread!=null)
            if(executionThread.isAlive())
                return;
        // Execute the program:
        if(mix.isHalted())
            mix.start(startAddress);
        executionThread = new Process(mix);
        executionThread.start();
    }
        
    /**
     * Stop execution of the MIX machine.
     */
    protected void stop()
    {
        mix.halt();
        memoryView.setCurrentAddress(mix.getLocationPtr().intValue());
    }

    /**
     * Reports a runtime error.
     */
    public static void runtimeError(Exception e)
    {
        JOptionPane.showMessageDialog(mainFrame,
            e.toString(),
            "Runtime Error",
            JOptionPane.ERROR_MESSAGE);
        // This is for debug purposes: if the user did not launch the IDE from
        // a command prompt, she won't even notice that a stack trace is being
        // printed.
        e.printStackTrace();
    }
	
    /** Execute one instruction.
     */
    protected void step()
    {
            if(mix.isHalted())
                mix.start(startAddress);
            try
            {
                if(executionThread!=null)
                    if(executionThread.isAlive())
                        return;
                mix.step();
                memoryView.setCurrentAddress(mix.getLocationPtr().intValue());
            }
            catch(Exception e)
            {
                runtimeError(e);
            }
    }
	
    /** Execute all instructions up to the next address.
     *  This method provides a kind of "step over" functionality.
     */
    protected void over()
    {
        int breakpoint;

        if(mix.isHalted())
            mix.start(startAddress);
        breakpoint = (mix.getLocationPtr().intValue() + 1) % mix.MEMORY_SIZE;
        try
        {
            if(executionThread!=null)
                if(executionThread.isAlive())
                    return;
            while(mix.getLocationPtr().intValue()!=breakpoint)
                mix.step();
            memoryView.setCurrentAddress(breakpoint);
        }
        // catch(RuntimeException e) {throw e;}
        catch(Exception e)
        {
            runtimeError(e);
        }
    }
	
    /** Dump the memory configuration to a file.
     */
    protected void dump()
    {
            // Have the user choose the dump file
            chooser.setFileFilter(mixDumpFiles);
        int returnVal = chooser.showSaveDialog(this);
        if(returnVal != JFileChooser.APPROVE_OPTION)
            return;

            try
            {
                    File dumpFile = chooser.getSelectedFile();
                    String fileName = dumpFile.getName();
                    System.out.println("Dumping to file " + fileName);
                    PrintStream out = new PrintStream(new FileOutputStream(dumpFile));
                    mix.dump(out);
            }
            catch(IOException ioe)
            {
                    JOptionPane.showMessageDialog(this,
                            ioe.toString() + " while dumping.",
                            "I/O Error",
                            JOptionPane.ERROR_MESSAGE);
            }
    }

    /** Get the MIX virtual machine.
     *
     *  @author Stefano Marino
     */	
    public MixVM getMixVM()
    {
    	return this.mix;
    }
    
    /** Set the start address for program execution.
     *
     *  @author Stefano Marino
     */	
    public void setStartAddress(int s)
    {
    	startAddress = s;
    }
    
    /**
     * Starts the MIX IDE application.
     */
    public static void main(String args[])
    {
        System.out.println("Starting MIX IDE v. " + version + "...");

        // Create the one and only instance of the IDE:
        mainFrame = new MixIDE();
        mainFrame.setSize(512, 600);
        mainFrame.setTitle("MIX IDE v. " + version);
        mainFrame.setVisible(true);
        System.out.println("started.");
    }    
}
