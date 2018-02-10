package mix.edit;

import javax.swing.*;

import mix.vm.*;
import mix.ide.*;

/** A dialog allowing the user to set the location pointer of the MIX machine.
 *  <CODE>LinePositioner</CODE> is a misnomer, probably caused by an erroneous
 *  interpretation by the author of the meaning of the acronym <CODE>LP</CODE>.
 *
 *  @author Stefano Marino
 */
public class LinePositioner extends JFrame {
	private JPanel jContentPane = null;
	private JPanel jPanel = null;
	private JPanel jPanel2 = null;
	private JButton jButton0 = null;
	private JButton jButton1 = null;
	private JTextField jline = null;
	private MemoryView mview;
	private int line;

	private static final String title = "Set Location Pointer";


	public LinePositioner(MemoryView mview) {
		super();
		this.mview = mview;
		line=mview.getSelectionStart()/mview.T_SIZE;
		initialize();
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
	this.setSize(240, 100);
	this.setTitle(title);
	this.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
	this.addWindowListener(new java.awt.event.WindowAdapter() {
		public void windowClosing(java.awt.event.WindowEvent e) {
			closeMe();
		}
	});

}

private JPanel getJPanel() {
	if (jPanel == null) {
		jPanel = new javax.swing.JPanel();
		jPanel.add(getJButton0(), null);
		jPanel.add(getJButton1(), null);
	}
	return jPanel;
}

private JPanel getJPanel2() {
	if (jPanel2 == null) {
		jPanel2 = new javax.swing.JPanel();
		jPanel2.add(getJline(), null);
	}
	return jPanel2;
}

	private JTextField getJline() {
		if (jline == null) {
			jline = new JTextField(4) ;
			jline.setText(lfiller('0',""+line,4));
			jline.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 10));
			jline.setMinimumSize(jline.getSize());
			jline.addKeyListener(new java.awt.event.KeyAdapter() {
				public void keyReleased(java.awt.event.KeyEvent e) {
					try{
						jline.setText(jline.getText().substring(jline.getText().length()-4,jline.getText().length()));
						update(mview);
					}catch(Exception ee){}
				}
			});
		}
		return jline;
	}
	private String lfiller(char c, String s, int l){
		String ret="";
		int f=(s==null)?l:(s.length()>l)?0:l-s.length();
		for(int i=0;i<f;i++)ret+=c;
		return ret+=s.substring(0,l-ret.length());
	}

	private javax.swing.JButton getJButton0() {
		if (jButton0 == null) {
			jButton0 = new javax.swing.JButton();
			jButton0.setText("OK");
			jButton0.setMnemonic(java.awt.event.KeyEvent.VK_ENTER);
			jButton0.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					posit();
				}
			});
		}
		return jButton0;
	}
	private javax.swing.JButton getJButton1() {
		if (jButton1 == null) {
			jButton1 = new javax.swing.JButton();
			jButton1.setText("Close");
			jButton1.setMnemonic(java.awt.event.KeyEvent.VK_ESCAPE);
			jButton1.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					closeMe();
				}
			});
		}
		return jButton1;
	}

	private void closeMe(){this.setVisible(false);}

	private void posit(){
		mview.setLocationPointer(line);
		mview.getMixIDE().setStartAddress(line);
		mview.getMixVM().start(line);
	}
	
	public void update(MemoryView memw){
		try{
			line = Integer.parseInt(jline.getText());
			if(line>3999)line=3999;
		}catch(Exception e){
			line = memw.getSelectionStart()/mview.T_SIZE;
		}
		jline.setText(lfiller('0',""+line,4));
	}

}
