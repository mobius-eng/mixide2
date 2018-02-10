package mix.edit;

import javax.swing.*;
import mix.vm.*;
import mix.ide.*;
/**
 * An example which shows off a functional simple text editor.  Includes a variety of events.
 */
public class RowModifier extends JFrame {
	
	private JPanel jContentPane = null;
	private JPanel jPanel = null;
	private JPanel jPanel2 = null;
	private JButton jButton0 = null;
	private JButton jButton1 = null;
	private JButton jButton2 = null;
	private JLabel jlinenum = null;
	private JTextField jmnemo = null;
	private JTextField jsign = null;
	private JTextField jb0 = null;
	private JTextField jb1 = null;
	private JTextField jb2 = null;
	private JTextField jb3 = null;
	private JTextField jb4 = null;
	private boolean hasChanged = false;
	private int line;
	private MixWord tword;
	private String[] strword;
	private MemoryView mview;

	private static final String title = "MIX Word ";


	public RowModifier(MemoryView mview) {
		super();
		this.mview = mview;
		line = mview.getSelectionStart()/mview.T_SIZE;
		try
                {
                    String content = mview.getText(mview.T_SIZE*line + 6, 16);
                    strword = content.split(" ");
                    tword = new MixWord(strword);
		}
                catch(Exception be)
                {
                    be.printStackTrace();
                }
		initialize();
	}

	public void update(){
		update(this.mview);
	}
	
	public void update(MemoryView mview){
		this.mview = mview;
		line=mview.getSelectionStart()/mview.T_SIZE;
		try
                {
                    String content = mview.getText(mview.T_SIZE*line + 6, 16);
                    strword = content.split(" ");
                    tword = new MixWord(strword);
                    jlinenum.setText(lfiller('0',""+line,4));		
                    jsign.setText(strword[0]);
                    jb0.setText(strword[1]);
                    jb1.setText(strword[2]);
                    jb2.setText(strword[3]);
                    jb3.setText(strword[4]);
                    jb4.setText(strword[5]);
                    jmnemo.setText(mix.asm.Opcode.decode(tword));		
		}
                catch(Exception be)
                {
                    be.printStackTrace();
		}
	}

	private javax.swing.JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new javax.swing.JPanel();
			jContentPane.setLayout(new java.awt.BorderLayout());
			jContentPane.add(getJPanel(), java.awt.BorderLayout.SOUTH);
			jContentPane.add(getJPanel2(), java.awt.BorderLayout.CENTER);
			jContentPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(
					5, 5, 5, 5));
		}
		return jContentPane;
	}

	private void initialize() {
		this.setContentPane(getJContentPane());
		this.setSize(450, 100);
		this.setTitle(title+ " line:"+line);
		this
				.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				doExit();
			}
		});

	}

	private JPanel getJPanel() {
		if (jPanel == null) {
			jPanel = new javax.swing.JPanel();
			jPanel.add(getJButton0(), null);
			jPanel.add(getJButton1(), null);
			jPanel.add(getJButton2(), null);
		}
		return jPanel;
	}
	
	private JPanel getJPanel2() {
		if (jPanel2 == null) {
			jPanel2 = new javax.swing.JPanel();
			jPanel2.add(getJlinenum(), null);
			jPanel2.add(getJsign(), null);
			jPanel2.add(getJb0(), null);
			jPanel2.add(getJb1(), null);
			jPanel2.add(getJb2(), null);
			jPanel2.add(getJb3(), null);
			jPanel2.add(getJb4(), null);
			jPanel2.add(getJmnemo(), null);
		}
		return jPanel2;
	}

	private JTextField getJsign() {
		if (jsign == null) {
			jsign = new JTextField(1) ;
			jsign.setText(strword[0]);
			jsign.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 10));
			jsign.setMinimumSize(jsign.getSize());
			jsign.addKeyListener(new java.awt.event.KeyAdapter() {
				public void keyReleased(java.awt.event.KeyEvent e) {
					hasChanged = true;
					char c = e.getKeyChar();
					if(jsign.getText().length()>1)
						if(c!='+'&&c!='-')jsign.setText(strword[0]);
						else jsign.setText(""+c);
					tword=new MixWord(getTword());
				}
			});
		}
		return jsign;
	}

	private JTextField getJb0() {
		if (jb0 == null) {
			jb0 = new JTextField(2);
			jb0.setText(strword[1]);
			jb0.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 10));
			jb0.setMinimumSize(jb0.getSize());
			jb0.addKeyListener(new java.awt.event.KeyAdapter() {
				public void keyReleased(java.awt.event.KeyEvent e) {
					hasChanged = true;
					if(jb0.getText().length()>2)
						jb0.setText(jb0.getText().substring(jb0.getText().length()-2,jb0.getText().length()));
					tword=new MixWord(getTword());
				}
			});
		}
		return jb0;
	}
	private JTextField getJb1() {
		if (jb1 == null) {
			jb1 = new JTextField(2) ;
			jb1.setText(strword[2]);
			jb1.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 10));
			jb1.setMinimumSize(jb1.getSize());
			jb1.addKeyListener(new java.awt.event.KeyAdapter() {
				public void keyReleased(java.awt.event.KeyEvent e) {
					hasChanged = true;
					jb1.setText(jb1.getText().substring(jb1.getText().length()-2,jb1.getText().length()));
					tword=new MixWord(getTword());
				}
			});
		}
		return jb1;
	}
	private JTextField getJb2() {
		if (jb2 == null) {
			jb2 = new JTextField(2) ;
			jb2.setText(strword[3]);
			jb2.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 10));
			jb2.setMinimumSize(jb2.getSize());
			jb2.addKeyListener(new java.awt.event.KeyAdapter() {
				public void keyReleased(java.awt.event.KeyEvent e) {
					hasChanged = true;
					jb2.setText(jb2.getText().substring(jb2.getText().length()-2,jb2.getText().length()));
					tword=new MixWord(getTword());
				}
			});
		}
		return jb2;
	}
	private JTextField getJb3() {
		if (jb3 == null) {
			jb3 = new JTextField(2) ;
			jb3.setText(strword[4]);
			jb3.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 10));
			jb3.setMinimumSize(jb3.getSize());
			jb3.addKeyListener(new java.awt.event.KeyAdapter() {
				public void keyReleased(java.awt.event.KeyEvent e) {
					hasChanged = true;
					jb3.setText(jb3.getText().substring(jb3.getText().length()-2,jb3.getText().length()));
					tword=new MixWord(getTword());
					refreshMnemo();
				}
			});
		}
		return jb3;
	}
	private JTextField getJb4() {
		if (jb4 == null) {
			jb4 = new JTextField(2) ;
			jb4.setText(strword[5]);
			jb4.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 10));
			jb4.setMinimumSize(jb4.getSize());
			jb4.addKeyListener(new java.awt.event.KeyAdapter() {
				public void keyReleased(java.awt.event.KeyEvent e) {
					hasChanged = true;
					jb4.setText(jb4.getText().substring(jb4.getText().length()-2,jb4.getText().length()));
					tword=new MixWord(getTword());
					refreshMnemo();
				}
			});
		}
		return jb4;
	}

	private JTextField getJmnemo() {
			if (jmnemo == null) {
				jmnemo = new JTextField(mix.asm.Opcode.decode(tword));
				jmnemo.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 10));
				jmnemo.setEditable(false);
			}
			return jmnemo;
	}
	
	private JLabel getJlinenum() {
		if (jlinenum == null) {
			jlinenum = new JLabel(lfiller('0',""+line,4)) ;
			jlinenum.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 10));
		}
		return jlinenum;
	}

	private javax.swing.JButton getJButton0() {
		if (jButton0 == null) {
			jButton0 = new javax.swing.JButton();
			jButton0.setText("Reset");
			jButton0.setMnemonic(java.awt.event.KeyEvent.VK_R);
			jButton0.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					doReset();
				}
			});
		}
		return jButton0;
	}
	private javax.swing.JButton getJButton1() {
		if (jButton1 == null) {
			jButton1 = new javax.swing.JButton();
			jButton1.setText("Modify");
			jButton1.setMnemonic(java.awt.event.KeyEvent.VK_M);
			jButton1.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					doModify();
				}
			});
		}
		return jButton1;
	}

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

	private String[] getTword(){
		String[] x =  new String[]{
(jsign.getText().length()==0)?strword[0]:jsign.getText().substring(jsign.getText().length()-1),
(jb0.getText().length()==0)?strword[1]:(jb0.getText().length()==1)?"0"+jb0.getText():jb0.getText().substring(jb0.getText().length()-2),
(jb1.getText().length()==0)?strword[2]:(jb1.getText().length()==1)?"0"+jb1.getText():jb1.getText().substring(jb1.getText().length()-2),
(jb2.getText().length()==0)?strword[3]:(jb2.getText().length()==1)?"0"+jb2.getText():jb2.getText().substring(jb2.getText().length()-2),
(jb3.getText().length()==0)?strword[4]:(jb3.getText().length()==1)?"0"+jb3.getText():jb3.getText().substring(jb3.getText().length()-2),
(jb4.getText().length()==0)?strword[5]:(jb4.getText().length()==1)?"0"+jb4.getText():jb4.getText().substring(jb4.getText().length()-2)
		};
	/*
	for(int i = 0;i<x.length;i++)
		System.out.print("#"+x[i]+"#,");
	System.out.println();
	*/
		return x;
	}

	private void refreshMnemo() {
		jmnemo.setText(mix.asm.Opcode.decode(tword));
	}

	private void doReset() {
		if(this.hasChanged)update(this.mview);
	}

	private void doModify() {
		mview.update(line,tword);
		update(mview);
	}

	private void doExit() {
		this.setVisible(false);
	}
	
	private String lfiller(char c, String s, int l){
		String ret="";
		int f=(s==null)?l:(s.length()>l)?0:l-s.length();
		for(int i=0;i<f;i++)ret+=c;
		return ret+=s.substring(0,l-ret.length());
	}
}  
