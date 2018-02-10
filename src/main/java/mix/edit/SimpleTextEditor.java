package mix.edit;

import java.io.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;


import mix.ide.MixalSourceFileFilter;

public class SimpleTextEditor extends JFrame{
	
	private  MixalSourceFileFilter 	mixalSourceFiles = new MixalSourceFileFilter();

	private javax.swing.JPanel jContentPane = null;

	private javax.swing.JPanel jPanel = null;

	private javax.swing.JButton jButton = null;

	private javax.swing.JButton jButton0 = null;

	private javax.swing.JButton jButton1 = null;

	private javax.swing.JButton jButton2 = null;

	private javax.swing.JScrollPane jScrollPane = null;

	private javax.swing.JTextArea jTextArea = null;

	private javax.swing.JFileChooser jFileChooser = null; //  @jve:visual-info  decl-index=0 visual-constraint="582,36"

	private boolean hasChanged = false;

	private File file=null;
	
	private static final String title = "MIX Editor";

	public SimpleTextEditor(File f) {
		super();
		initialize();
		loadFile(f);
	}
	public SimpleTextEditor() {
		super();
		initialize();
	}

	private static void main(String[] args) {
		SimpleTextEditor ste = new SimpleTextEditor();
		ste.setVisible(true);
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new javax.swing.JPanel();
			jContentPane.setLayout(new java.awt.BorderLayout());
			jContentPane.add(getJPanel(), java.awt.BorderLayout.SOUTH);
			jContentPane.add(getJScrollPane(), java.awt.BorderLayout.CENTER);
			jContentPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(
					5, 5, 5, 5));
		}
		return jContentPane;
	}

	/**
	 * This method initializes the components of the editor window.
	 */
	private void initialize() {
		this.setContentPane(getJContentPane());
		this.setSize(580, 568);
		this.setTitle(title+((file==null)?"":" "+file.getName()));
		this
				.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				doExit();
			}
		});

	}

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getJPanel() {
		if (jPanel == null) {
			jPanel = new javax.swing.JPanel();
			jPanel.add(getJButton(), null);
			jPanel.add(getJButton0(), null);
			jPanel.add(getJButton1(), null);
			jPanel.add(getJButton2(), null);
		}
		return jPanel;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private javax.swing.JButton getJButton() {
		if (jButton == null) {
			jButton = new javax.swing.JButton();
			jButton.setText("Load File");
			jButton.setMnemonic(java.awt.event.KeyEvent.VK_L);
			jButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					loadFile();
				}
			});
		}
		return jButton;
	}

	/**
	 * This method initializes jButton1
	 * 
	 * @return javax.swing.JButton
	 */
	private javax.swing.JButton getJButton0() {
		if (jButton0 == null) {
			jButton0 = new javax.swing.JButton();
			jButton0.setText("Save");
			jButton0.setMnemonic(java.awt.event.KeyEvent.VK_S);
			jButton0.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					saveFile();
				}
			});
		}
		return jButton0;
	}
	private javax.swing.JButton getJButton1() {
		if (jButton1 == null) {
			jButton1 = new javax.swing.JButton();
			jButton1.setText("SaveAs");
			jButton1.setMnemonic(java.awt.event.KeyEvent.VK_A);
			jButton1.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					saveAsFile();
				}
			});
		}
		return jButton1;
	}

	/**
	 * This method initializes jButton2
	 * 
	 * @return javax.swing.JButton
	 */
	private javax.swing.JButton getJButton2() {
		if (jButton2 == null) {
			jButton2 = new javax.swing.JButton();
			jButton2.setText("Close");
			jButton2.setMnemonic(java.awt.event.KeyEvent.VK_C);
			jButton2.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					doExit();
				}
			});
		}
		return jButton2;
	}

	/**
	 * This method initializes jScrollPane
	 * 
	 * @return javax.swing.JScrollPane
	 */
	private javax.swing.JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new javax.swing.JScrollPane();
			jScrollPane.setViewportView(getJTextArea());
		}
		return jScrollPane;
	}

	/**
	 * This method initializes jTextArea
	 * 
	 * @return javax.swing.JTextArea
	 */
	private javax.swing.JTextArea getJTextArea() {
		if (jTextArea == null) {
			jTextArea = new javax.swing.JTextArea();
			jTextArea.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 10));
			jTextArea.addKeyListener(new java.awt.event.KeyAdapter() {
				public void keyTyped(java.awt.event.KeyEvent e) {
					if (!hasChanged) {
						setTitle(title + " *");
						hasChanged = true;
					}
				}
			});
		}
		return jTextArea;
	}

	/**
	 * This method initializes jFileChooser
	 * 
	 * @return javax.swing.JFileChooser
	 */
	private javax.swing.JFileChooser getJFileChooser() {
		if (jFileChooser == null) {
			jFileChooser = new javax.swing.JFileChooser();
			jFileChooser.setMultiSelectionEnabled(false);
			jFileChooser.setFileFilter(mixalSourceFiles);
		}
		return jFileChooser;
	}

	private void loadFile(File f){
		try {
			this.file = f;
			BufferedReader br = new BufferedReader(new FileReader(f));
			getJTextArea().read(br, null);
			br.close();
			setTitle(title+((file==null)?"":" "+file.getName()));
			hasChanged = false;
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	private void loadFile() {
		int state = getJFileChooser().showOpenDialog(this);
		if (state == JFileChooser.APPROVE_OPTION) {
			File f = getJFileChooser().getSelectedFile();
			loadFile(f);
		}
	}

	private void saveFile() {
		if(this.file==null)saveAsFile();
		else{
			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(this.file));
				getJTextArea().write(bw);
				bw.close();
				setTitle(title+((file==null)?"":" "+file.getName()));
				hasChanged = false;
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
		private void saveAsFile() {
		int state;
		File f=this.file;
			state = getJFileChooser().showSaveDialog(this);
			if (state == JFileChooser.APPROVE_OPTION) {
				f = getJFileChooser().getSelectedFile();
				try {
					BufferedWriter bw = new BufferedWriter(new FileWriter(f));
					getJTextArea().write(bw);
					bw.close();
					setTitle(title);
					hasChanged = false;
					this.file=f;
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
	}

	private void doExit() {
		if (hasChanged) {
			int state = JOptionPane.showConfirmDialog(this,
					"File has been changed. Save before exit?");
			if (state == JOptionPane.YES_OPTION) {
				saveFile();
				
			} else if (state == JOptionPane.CANCEL_OPTION) {
				return;
			}
		}
		this.setVisible(false);
	}
} //  @jve:visual-info  decl-index=0 visual-constraint="20,27"
