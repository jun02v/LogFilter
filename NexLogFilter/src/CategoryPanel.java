import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

import java.awt.Font;


@SuppressWarnings("serial")
/* 추가되는 카테고리를 하나의 클래스로 만듬. */
public class CategoryPanel extends JPanel implements ActionListener {
	FlowLayout categoryLayout;
	JCheckBox enableChk;
	JTextField categoryName;
	TextField textLevel;
	//static FilterFrame filterFrame;
	private MainFrame mainFrame;  
	JButton removeButton;
	int categoryKey;
	JComboBox<String> levelCombo;
	JPanel checkboxPanel;
	JCheckBox lessThanChk;
	JCheckBox equalChk;

	/* 생성할때 Key값을 받아옴, 삭제할 때 쓰인다. */
	public CategoryPanel(MainFrame mainFrame, int receiveKey) {
		// TODO Auto-generated constructor stub
		categoryKey = receiveKey;
		//filterFrame = new FilterFrame();
		this.mainFrame = mainFrame; 
		categoryLayout = (FlowLayout)this.getLayout();
		categoryLayout.setVgap(0);
		categoryLayout.setHgap(10);
		categoryLayout.setAlignment(FlowLayout.LEFT);
		setBorder(new EtchedBorder(EtchedBorder.LOWERED));
		
		/* enable checkbox, 체크해제 되면 AllCheckbox도 체크해제 시킨다. */
		enableChk = new JCheckBox("");
		enableChk.addActionListener(this);
		enableChk.setSelected(true);
		add(enableChk);
		categoryName = new JTextField();
		categoryName.setColumns(30);
		add(categoryName);
		add(Box.createRigidArea(new Dimension(15, 0)));

		checkboxPanel = new JPanel();
		add(checkboxPanel);
		checkboxPanel.setLayout(new BoxLayout(checkboxPanel, BoxLayout.Y_AXIS));
		
		lessThanChk = new JCheckBox("<");
		lessThanChk.setSelected(true);
		checkboxPanel.add(lessThanChk);
		
		equalChk = new JCheckBox("=");
		equalChk.setSelected(true);
		checkboxPanel.add(equalChk);

		String level[] = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
		
		levelCombo = new JComboBox<String>(level);
		add(levelCombo);
		
		/* 삭제버튼을 누르면 FilterFrame에 removeCategory 함수를 호출하면서 key값을 parameter로 넘김. */
		removeButton = new JButton("-");
		removeButton.setFont(new Font("굴림", Font.PLAIN, 13));
		removeButton.addActionListener(this);
		removeButton.setActionCommand("remove");;
		
		add(removeButton);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if (e.getActionCommand() == "remove") {
			mainFrame.removeCategori(categoryKey);
		}
		if (e.getActionCommand() == "uncheckAllCheckbox") {
			mainFrame.unselectAllCheckbox();
		}
		
	}
}