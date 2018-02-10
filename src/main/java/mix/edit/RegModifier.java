package mix.edit;

import java.awt.event.*;
import java.awt.*;

import javax.swing.*;

import mix.vm.*;
import mix.ide.*;

public class RegModifier extends JFrame implements ActionListener{
	private Container  jp = null;
	
	private JLabel  lov = null;
	private JComboBox  jov = null;

	private JLabel  lcmp = null;
	private JComboBox  jcmp = null;
 	
	private JLabel  lRa = null;
	private JTextField jRa = null;

	private JLabel  lRx = null;
	private JTextField jRx = null;

	private JTextField jRj = null;
	private JLabel  lRj = null;

	private JLabel[]  lI = null;
	private JTextField[] jI=null;
	
	private MixVM mm =null;
	private RegisterView rw = null;
	
	private JButton jDone, jCancel,jReset;
	
	void init(){
		setTitle("Register Modifier");
		jp = getContentPane();
		SpringLayout layout = new SpringLayout();
        jp.setLayout(layout);		
        lov = new JLabel("OV");
		jp.add(lov);
		jov = new JComboBox(new Object[]{"ON","OFF"});
		jp.add(jov);
		
		lcmp = new JLabel("CMP");
		jp.add(lcmp);
		jcmp = new JComboBox(new Object[]{"<","=",">"});
		jp.add(jcmp);

		lRa = new JLabel("A");
		jp.add(lRa);
		jRa = new JTextField(); 
		jRa.setColumns(11);
		jp.add(jRa);

		lRx = new JLabel("X");
		jp.add(lRx);
		jRx = new JTextField(); 
		jRx.setColumns(11);
		jp.add(jRx);

		lI = new JLabel[6];
		jI = new JTextField[6];
		for(int j=0;j<6;j++){
			lI[j]= new JLabel("I"+(j+1));
			jp.add(lI[j]);
			jI[j]=new JTextField();
			jI[j].setColumns(4);
			jp.add(jI[j]);
		}
		lRj = new JLabel("J");
		jp.add(lRj);
		jRj = new JTextField();
		jRj.setColumns(4);
		jp.add(jRj);

		JPanel bott = new JPanel();
		SpringLayout buttonlay = new SpringLayout();
		bott.setLayout(buttonlay);
		jDone = new JButton("Done");
		jDone.addActionListener(this);
		bott.add(jDone);
		jReset = new JButton("Reset");
		jReset.addActionListener(this);
		bott.add(jReset);
		jCancel = new JButton("Cancel");
		jCancel.addActionListener(this);
		bott.add(jCancel);
		jp.add(new JLabel(""));
		jp.add(bott);
        SpringUtilities.makeCompactGrid(bott,
                1, 3, //rows, cols
                1, 1,        //initX, initY
                1, 1);       //xPad, yPad
		
        SpringUtilities.makeCompactGrid(jp,
                12, 2, //rows, cols
                1, 1,        //initX, initY
                1, 1);       //xPad, yPad
		pack();
		setSize(250,300);
		setLocation(rw.getLocation());
		
		refresh();
	}
	public void refresh(){
		jov.setSelectedIndex(mm.getOV()?0:1);
		jcmp.setSelectedIndex((mm.getCmp()==0)?1:((mm.getCmp()<0)?0:2));
		jRa.setText(""+mm.getAccumulator().intValue());
		jRx.setText(""+mm.getExtension().intValue());
		for(int j=1;j<=6;j++){
			jI[j-1].setText(""+mm.getIndexRegister(j).intValue());
		}		
		jRj.setText(""+mm.getJumpRegister().intValue());
	}
	
	public RegModifier(RegisterView rw,MixVM mm){
		super();
		this.rw=rw;
		this.mm=mm;
		init();
	}
        
        public void setVM(MixVM newmix) {
            mm = newmix;
        }
	
	public void update(boolean reset){
		if(!reset){
			mm.setOV(jov.getSelectedIndex()==0?"ON":"OFF");
			mm.compare(jcmp.getSelectedIndex()-1);
			mm.setAccumulator(Integer.parseInt(jRa.getText().replaceFirst("\\+","")));
			mm.setExtension(Integer.parseInt(jRx.getText().replaceFirst("\\+","")));
			mm.setJumpRegister(Integer.parseInt(jRj.getText().replaceFirst("\\+","")));
			for (int i=1;i<=6;i++){
				mm.setIndexRegister(i,Integer.parseInt(jI[i-1].getText().replaceFirst("\\+","")));
			}
		}else{
			mm.setOV("ON");
			mm.compare(0);
			mm.setAccumulator(0);
			mm.setExtension(0);
			mm.setJumpRegister(0);
			for (int i=1;i<=6;i++)
				mm.setIndexRegister(i,0);
		}
			
		rw.update(mm);
	
	}

	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equalsIgnoreCase("done"))
			update(false);
		if(e.getActionCommand().equalsIgnoreCase("reset"))
			update(true);
		this.setVisible(false);
	}

}
