import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.transform.TransformerConfigurationException;


@SuppressWarnings("serial")
public class MainFrame extends JFrame implements KeyListener, ActionListener, Runnable {

	private CategoryPanel categoryPanel;
	private FileSetterGetter fileSetterGetter;
	private XmlReaderWriter xmlReadWrite;
	private Utils utils;
	private JPanel fullPanel;
	private JPanel allCategoryPanel;
	private JPanel checkboxPanel;
	private JPanel categoryListPanel;
	private JPanel categoryItemsPanel;
	private Box categoryListBox;	
	private HashMap<Integer, CategoryPanel> categoryMap;
	private JLabel filePathLabel;
	private JLabel savedPathLabel;
	private JLabel exampleLabel;
	private JTextField separatorStart;
	private JTextField separatorEnd;
	private JTextField separatorMid;
	private JCheckBox allCheckbox;
	private JCheckBox lessThanCheckBox;
	private JCheckBox equalCheckBox;
	private JComboBox<String> levelCombo;
	private JButton saveFileBtn;
	private JProgressBar parsingProgress;
	
	private static MainFrame mainFrame;
	private static final int categoriWidth = 326;
	private static final int categoriHight = 40;
	private static int categoryKey = 0;
	
	private static final String TITLE = "Log Filter";
	private static final String FILE_NAME = "File Name";
	private static final String APPLIES_ALL = " Applies to all";
	private static final String OPEN = "Open";
	private static final String SAVE_PATH = "Saved Path";	
	private static final String level[] = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
	
	private static final String ACT_FILE_OPEN = "fileOpen";
	private static final String ACT_GET_PATH = "getPath";
	private static final String ACT_UNCHECK_ALL = "uncheckAllCheckbox";
	private static final String ACT_APPLIES = "appliesAll";
	private static final String ACT_ADD_CTGR = "createCategory";
	private static final String ACT_SAVE = "searchNSave";
	
	private static String destPath = System.getProperty("user.dir");
	private static String searchExample ="Ex.) ";
	
	public MainFrame(String title) {
		// TODO Auto-generated constructor stub
		super(title);
		xmlReadWrite = new XmlReaderWriter();
		utils = new Utils();
	}

	public void showGUI() {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 정상적으로 종료하기 위한 메소드
		getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.X_AXIS));
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		setIconImage(toolkit.getImage("LogFilterIcon.png")); // 아이콘 추가
		
		fullPanel = new JPanel();
		getContentPane().add(fullPanel);
		fullPanel.setLayout(null);
		
		filePathLabel = new JLabel(FILE_NAME);
		filePathLabel.setBounds(131, 10, 468, 25);
		fileSetterGetter = new FileSetterGetter();
		
		/* File open button */
		JButton btnOpen = new JButton(OPEN);
		btnOpen.setActionCommand(ACT_FILE_OPEN);
		btnOpen.addActionListener(this);
		btnOpen.setBounds(12, 10, 107, 23);
		fullPanel.add(btnOpen);
		fullPanel.add(filePathLabel);
		
		setDropTarget(new DropTarget() {

			public synchronized void drop(DropTargetDropEvent evt) {
				try {

					evt.acceptDrop(DnDConstants.ACTION_COPY);
					@SuppressWarnings("unchecked")
					List<File> droppedFiles = (List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
					String fileName = droppedFiles.get(0).getName();
					String extensionName = null;
					/* Drag and Drop을 통해 받아온 파일의 확장자를 비교해서 txt, log파일이 아니라면 팝업을 날리고, 맞다면 File객체로 만들어 둔다. */
					if (fileName.lastIndexOf('.') >= 0){
						extensionName = fileName.substring(fileName.lastIndexOf('.')+1);
					}
					if (null != extensionName && (extensionName.equals("txt") || extensionName.equals("log") || extensionName.equals("TXT") || extensionName.equals("LOG"))){
						setFile(droppedFiles.get(0));
						checkSaveFileBtn();
					} else {
						JOptionPane.showMessageDialog(null,
			                    "Only '.txt', '.log' file can be read!", "Message",
			                    JOptionPane.INFORMATION_MESSAGE);
					}
					
				} catch (Exception ex){
					ex.printStackTrace();
				}
			}
		});
		
		savedPathLabel = new JLabel(destPath);
		savedPathLabel.setBounds(131, 43, 468, 15);
		fullPanel.add(savedPathLabel);
		
		/* Path 설정 버튼, 누르면 Path 만 받아오도록 설정함 */
		JButton savedPathButton = new JButton(SAVE_PATH);
		savedPathButton.addActionListener(this);
		savedPathButton.setActionCommand(ACT_GET_PATH);
		savedPathButton.setBounds(12, 39, 107, 23);
		fullPanel.add(savedPathButton);
		
		JLabel categoryStartLbl = new JLabel("Category Separator - Begining");
		categoryStartLbl.setBounds(12, 95, 174, 15);
		fullPanel.add(categoryStartLbl);
		
		/* 시작, 중간, 끝 구분자에 KeyListener를 달아서 글씨가 써지거나 지워질 때, 바로 예시문에 나타나도록 함.
		 * 구분자 TextField 에 key가 눌려지면, keyReleased() 함수가 호출됨.  */
		separatorStart = new JTextField();
		separatorStart.setDocument(new JTextFieldLimit(1)); // 1글자만 입력되도록 함.
		separatorStart.setBounds(187, 92, 35, 21);
		separatorStart.addKeyListener(this);
		fullPanel.add(separatorStart);
		separatorStart.setColumns(10);
		
		JLabel endLbl = new JLabel("End");
		endLbl.setBounds(234, 95, 35, 15);
		fullPanel.add(endLbl);
		
		separatorEnd = new JTextField();
		separatorEnd.setDocument(new JTextFieldLimit(1));
		separatorEnd.setColumns(10);
		separatorEnd.setBounds(260, 92, 35, 21);
		separatorEnd.addKeyListener(this);
		fullPanel.add(separatorEnd);
		
		
		JLabel middleLbl = new JLabel("Middle");
		middleLbl.setBounds(307, 95, 46, 15);
		fullPanel.add(middleLbl);
		
		separatorMid = new JTextField();
		separatorMid.setDocument(new JTextFieldLimit(1));
		separatorMid.setColumns(10);
		separatorMid.setBounds(351, 92, 35, 21);
		separatorMid.addKeyListener(this);
		fullPanel.add(separatorMid);
		
		/* categoryListPanel - All category 및 하위 category들을 감싸고 있는 Panel */
		categoryListPanel = new JPanel();
		categoryListPanel.setBounds(12, 123, 587, 458);
		categoryListPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED),"Category"));
		fullPanel.add(categoryListPanel);
		categoryListPanel.setLayout(new BorderLayout(0, 0));
		
		/* allCategoryPanel - allCheckbox, lessThan/equal Checkbox, levelCombobox를 담고 있다. */
		allCategoryPanel = new JPanel();
		FlowLayout flowLayout_2 = (FlowLayout) allCategoryPanel.getLayout();
		flowLayout_2.setVgap(0);
		flowLayout_2.setHgap(10);
		allCategoryPanel.setSize(279, 25);

		categoryListPanel.add("North", allCategoryPanel);
		
		/* allCheckbox, 체크되면 모든 카테고리가 활성화 되며, allCategory안에 있는 내용들이 모두 적용된다. 체크 해제를 누르면 모두 비활성화. */
		allCheckbox = new JCheckBox(APPLIES_ALL);
		allCheckbox.setFont(new Font("굴림", Font.BOLD, 14));
		allCheckbox.addActionListener(this);
		allCheckbox.setActionCommand(ACT_APPLIES);
		allCategoryPanel.add(allCheckbox);
		allCategoryPanel.add(Box.createRigidArea(new Dimension(250, 0))); // 공간을 둘때 사용됨.
		
		checkboxPanel = new JPanel();
		checkboxPanel.setLayout(new BoxLayout(checkboxPanel, BoxLayout.Y_AXIS));
		allCategoryPanel.add(checkboxPanel);
		
		// allCategory 안에 있는 "<" checkbox, 이것을 누르면, allCheckbox가 체크해제 되도록 하였다. allCategory안 다른 checkbox도 마찬가지이다.
		lessThanCheckBox = new JCheckBox("<");
		lessThanCheckBox.addActionListener(this);
		lessThanCheckBox.setActionCommand(ACT_UNCHECK_ALL);
		checkboxPanel.add(lessThanCheckBox);
		
		equalCheckBox = new JCheckBox("=");
		equalCheckBox.setActionCommand(ACT_UNCHECK_ALL);
		equalCheckBox.addActionListener(this);
		checkboxPanel.add(equalCheckBox);
		
		levelCombo = new JComboBox<String>(level);
		levelCombo.setActionCommand(ACT_UNCHECK_ALL);
		levelCombo.addActionListener(this);
		allCategoryPanel.add(levelCombo);
		
		/* '+' Button을 누를 때마다 하나의 카테고리가 생성됨. 카테고리 하나는 클래스 객체 하나. */
		JButton addCategoryBtn = new JButton("+");
		addCategoryBtn.setActionCommand(ACT_ADD_CTGR);
		addCategoryBtn.addActionListener(this);
		allCategoryPanel.add(addCategoryBtn);
		
		categoryItemsPanel = new JPanel(); // Category들을 감싸고 있는 Panel
		FlowLayout flowLayout = (FlowLayout) categoryItemsPanel.getLayout();
		flowLayout.setVgap(3);
		categoryItemsPanel.setSize(categoriWidth, 279);
		
		JScrollPane categoryScrollPane = new JScrollPane(categoryItemsPanel); // Scroll을 적용
		categoryListPanel.add(categoryScrollPane);
		
		categoryListBox = Box.createVerticalBox(); // Category들을 감싸는 Box추가, categoryItemsPanel 안에 포함됨.
		categoryListBox.setSize(categoriWidth, 279);
		
		categoryMap = new HashMap<Integer, CategoryPanel>();
		fullPanel.add(categoryListPanel);
		
		System.out.println("categoryKey : "+categoryKey+", categoryMap.size() : "+categoryMap.size());
		
		/* Progress Bar */
		parsingProgress = new JProgressBar(); // Progress Bar 생성, Parsing하고, Save될 때 만 보이게 된다.
		parsingProgress.setBounds(22, 591, 468, 15);
		parsingProgress.setStringPainted(true);
		parsingProgress.setVisible(false);
		fullPanel.add(parsingProgress);
		
		/* Save Button */
		saveFileBtn = new JButton("Save File");
		saveFileBtn.setActionCommand(ACT_SAVE);
		saveFileBtn.addActionListener(this);
		saveFileBtn.setBounds(502, 591, 97, 28);
		fullPanel.add(saveFileBtn);
		
		exampleLabel = new JLabel(searchExample); // 검색할 String 형식 예시문구.
		exampleLabel.setBounds(22, 589, 288, 15);
		fullPanel.add(exampleLabel);

		setXmlValues();	// xml파일에서 읽어온 값들을 가져와서 각 Component에 Setting 해준다. 
		showSearchExample(); // 예시문구를 보여줄지 판단하여 보여준다.
		checkSaveFileBtn(); // Save Button을 특정조건에서 활성화 시켜주는 Method.
		setSize(627, 674);
		setVisible(true);
		
		/* 윈도우 상태에 따라 호출되는 WindowListener 이다. */
		addWindowListener(new WindowListener() {

			@Override
			public void windowActivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowClosed(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowClosing(WindowEvent e) {
				// TODO Auto-generated method stub
				try {
					/* 각 Component들로 부터 값들을 받아오고 */
					xmlReadWrite.setOpenFile(fileSetterGetter.getPath());
					xmlReadWrite.setSavedPath(savedPathLabel.getText());
					xmlReadWrite.setSeparatorB(separatorStart.getText());
					xmlReadWrite.setSeparatorE(separatorEnd.getText());
					xmlReadWrite.setSeparatorM(separatorMid.getText());
					
					System.out.println("finalize() OpenFile-" + fileSetterGetter.getPath() + ", savedPath-" + savedPathLabel.getText()
							+ ", separatorB-"+separatorStart.getText()+", separatorE-"+separatorEnd.getText() + ", separatorM-"+separatorMid.getText());
					
					/* all Category는 "enable|lessThan|equal|level"(T|F|T|4) 형태로 만들어 Setting한다. */
					String allCategoryValues = boolToString(allCheckbox.isSelected()) + "|"
							+ boolToString(lessThanCheckBox.isSelected()) + "|"
							+ boolToString(equalCheckBox.isSelected()) + "|"
							+ levelCombo.getSelectedItem();
					xmlReadWrite.setAllCateValues(allCategoryValues);
					
					System.out.println("finalize() allCategoryValues-"+allCategoryValues);
					
					/* Map<Integer, CategoryPanel>으로 저장되어 있는 것을 Category들 값만 빼내서 ArrayList<String>로 저장한다. */
					ArrayList<String> categoryArray = new ArrayList<String>(); 
					Collection<CategoryPanel> collection = categoryMap.values();
					Iterator<CategoryPanel> iterator = collection.iterator();
					while (iterator.hasNext()) {
						CategoryPanel categoryPanel = iterator.next();
						// category 값들은 이런 "enable(bool)|name(string)|lessThan(bool)|equal(bool)|level(string)"(T|ENGN|T|F|5) 형태의 스트링으로 저장된다.
						String categoryValues = boolToString(categoryPanel.enableChk.isSelected()) + "|"
								+ categoryPanel.categoryName.getText() + "|"
								+ boolToString(categoryPanel.lessThanChk.isSelected()) + "|"
								+ boolToString(categoryPanel.equalChk.isSelected()) + "|"
								+ categoryPanel.levelCombo.getSelectedItem();
						categoryArray.add(categoryValues); // 한 카테고리당 하나의 스트링으로 저장.
						System.out.println("finalize() categoryValues-"+categoryValues);
					}
					xmlReadWrite.setCategoryArray(categoryArray);
					xmlReadWrite.setWriteXml();
				} catch (TransformerConfigurationException exception) {
					// TODO Auto-generated catch block
					exception.printStackTrace();
					dispose(); // 위 Try문 안에서 Exeption 발생하면 printing 후 그냥 끝낸다.
				}
				dispose(); // 다 저장 했으면 창 닫음.
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowIconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowOpened(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}			
		});
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		JFileChooser fileChooser;
		/* Open button을 눌렀을 때 실행되는 Action. */
		if (e.getActionCommand() == ACT_FILE_OPEN) { 
			fileChooser = new JFileChooser(fileSetterGetter.getPath());
			FileNameExtensionFilter fileFilter = new FileNameExtensionFilter("Text file", "txt", "log");
			fileChooser.setFileFilter(fileFilter);
			int result = fileChooser.showDialog(this, null);
			if(result == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				setFile(file);
				checkSaveFileBtn();
			}
		/* Saved Path button을 눌렀을 때 실행되는 Action */
		} else if (e.getActionCommand() == ACT_GET_PATH) {
			fileChooser = new JFileChooser(destPath);
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fileChooser.showDialog(this, null);
			File file = fileChooser.getSelectedFile();
			if (file.exists()) {
				destPath = file.getPath();
				savedPathLabel.setText(file.getPath());
			}
			
		} else if (e.getActionCommand() == ACT_UNCHECK_ALL) {
			
			unselectAllCheckbox();
			
		} else if (e.getActionCommand() == ACT_APPLIES) {
			if (allCheckbox.isSelected()) { // allCheckbox를 선택했을 때,
				if (categoryMap.size() != 0) { // 일단 추가된 카테고리가 있는지 확인한다. 없으면 카테고리를 추가하라고 Dialog를 날린다.
					// 값들을 Parameter로 받아와 모든 카테고리에 적용하고, 모두 체크한다.
					utils.allSelect(categoryMap, equalCheckBox.isSelected(), lessThanCheckBox.isSelected(), levelCombo.getSelectedItem()); 
					allCheckbox.setSelected(true);
				} else {
					JOptionPane.showMessageDialog(this, "There is no category to select. please add category.");
					allCheckbox.setSelected(false);
				}
			} else {
				Collection<CategoryPanel> collection = categoryMap.values();
				Iterator<CategoryPanel> iterator = collection.iterator();
				while (iterator.hasNext()){
					iterator.next().enableChk.setSelected(false);
				}
			}
		} else if (e.getActionCommand() == ACT_ADD_CTGR) {
			if(categoryMap.size() <= 19){ // 카테고리 갯수가 20개를 넘으면 만들수 없다고 Dialog 나옴. 
				createCategory(); // 카테고리 생성 Method. 
				allCheckbox.setSelected(false); // allCheckbox 체크해제 되도록 함.
				checkSaveFileBtn(); // Save Button 활성화 시킬지 말지 결정해주는 Method.
				showSearchExample(); // Search 예제를 보여주는 Method.
			} else {
				JOptionPane.showMessageDialog(this, "Categories can not be more than 20.");
			}
			setVisible(true);  // 이 Method를 쓰지 않으면, Frame 화면이 갱신되지 않는다.
		} else if (e.getActionCommand() == ACT_SAVE) {
			boolean check = utils.settingSearchWords(categoryMap, separatorStart.getText(), separatorEnd.getText(), separatorMid.getText());
			if (check) { // 위 함수에서, category 들에 이름이 없거나 체크된 것이 없을 때, false를 리턴한다. 
				disableAllComponents(); // Progress Bar를 제외한 모든 컴포넌트들을 비활성화 시키고,
				utils.parseAndSave(fileSetterGetter.getFile(), destPath, parsingProgress, mainFrame); // 검색과 파일을 생성해서 저장한다.
			} else {
				JOptionPane.showMessageDialog(this, "Can not create file!");
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		showSearchExample();
	}
	
	/* 카테고리 생성 Method. 카테고리당 Key값을 부여해준다. 그리고 Map을 이용해 Key, CategoryPanel객체를 관리한다. 
	 * 아래는 '+' Button을 눌렀을때 호출되는 메소드 */
	public void createCategory() {
		categoryKey ++; // Key 값 생성. 1 부터 시작한다.
		categoryPanel = new CategoryPanel(this, categoryKey); // categoryPanel 객체 생성할때 Key값을 부여한다.
		categoryPanel.setSize(categoriWidth, categoriHight);
		categoryPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
		categoryMap.put(categoryKey, categoryPanel); // categoryMap에 Key값과 categoryPanel객체를 넣어준다.
		System.out.println("Press Button categoryKey : "+categoryKey+", categoryMap.size : "+categoryMap.size());
		categoryListBox.add(categoryPanel); // categoryPanel객체는 categoryBox에 추가시키고
		categoryItemsPanel.add(categoryListBox); // categoryBox는 categoryItemsPanel에 추가시킨다.
	}
	
	/* 위 Method와 같은 기능이지만, 아래 Method는 xml을 파싱하여 category를 만들때 호출되는  Method 이다. */
	public void createCategory(ArrayList<String> categoryArray) {
		for (int i = 0 ; i < categoryArray.size() ; i++) {
			categoryKey ++;
			String[] categoryValues = categoryArray.get(i).split("\\|"); // CategoryArray에 저장된 String을 '|'기준으로 쪼개서 배열로 저장한다. 
			if (categoryValues.length == 5) { // '|'을 기준으로 자른 String이  5개가 되지 않으면 카테고리를 만들지 않는다.
				categoryPanel = new CategoryPanel(this, categoryKey);
				categoryPanel.setSize(categoriWidth, categoriHight);
				categoryPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
				
				/* 위에서 생성한 categoryPanel 객체에다 값들을 setting. 나머지 부분 위에 있는 Method와 동일. */
				categoryPanel.enableChk.setSelected(stringToBool(categoryValues[0]));
				categoryPanel.categoryName.setText(categoryValues[1]);
				categoryPanel.lessThanChk.setSelected(stringToBool(categoryValues[2]));
				categoryPanel.equalChk.setSelected(stringToBool(categoryValues[3]));
				categoryPanel.levelCombo.setSelectedItem(categoryValues[4]);
				
				categoryMap.put(categoryKey, categoryPanel);
				categoryListBox.add(categoryPanel);
				categoryItemsPanel.add(categoryListBox);
				System.out.println("Press Button categoryKey : "+categoryKey+", categoryMap.size : "+categoryMap.size());
			}
		}
	}
	
	/*모든 컴포넌트들을 비활성화 시킴 - Progress bar 생성시 호출함.*/
	public void disableAllComponents(){
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		parsingProgress.setVisible(true);
		
		Collection<CategoryPanel> collection = categoryMap.values();
		Iterator<CategoryPanel> iterator = collection.iterator();
		exampleLabel.setVisible(false);
		while (iterator.hasNext()){
			CategoryPanel categoryPanel = iterator.next();
			for (Component c : categoryPanel.checkboxPanel.getComponents()) {
				c.setEnabled(false);
			}
			for (Component c : categoryPanel.getComponents()) {
				c.setEnabled(false);
			}
		}
		for (Component c : checkboxPanel.getComponents()) {
			c.setEnabled(false);
		}
		for (Component c : fullPanel.getComponents()) {
			c.setEnabled(false);
		}
		for (Component c : allCategoryPanel.getComponents()) {
			c.setEnabled(false);
		}
	}
	
	/*모든 컴포넌트들을 활성화 시킴 - Progress bar 끝난 후 호출함.*/
	public void enableAllComponents(){
		setCursor(null);
		parsingProgress.setVisible(false);
		
		for (Component c : fullPanel.getComponents()) {
			c.setEnabled(true);
		}
		for (Component c : allCategoryPanel.getComponents()) {
			c.setEnabled(true);
		}
		for (Component c : checkboxPanel.getComponents()) {
			c.setEnabled(true);
		}
		exampleLabel.setVisible(true);
		Collection<CategoryPanel> collection = categoryMap.values();
		Iterator<CategoryPanel> iterator = collection.iterator();
		
		while (iterator.hasNext()) {
			CategoryPanel categoryPanel = iterator.next();
			for (Component c : categoryPanel.getComponents()) {
				c.setEnabled(true);
			}
			for (Component c : categoryPanel.checkboxPanel.getComponents()) {
				c.setEnabled(true);
			}
		}
	}
	
	/* xml파일에서 값들을 가져와 각 component들에다 setting해주는 Method. */
	public void setXmlValues() {
		if (xmlReadWrite.parseXml()) { // xml 파일이 없으면 false, 있으면 값들을 set해주고 true 반환. 
			if (null != xmlReadWrite.getOpenFile()) { // Open할 파일명이 null인지 확인.
				setFile(new File(xmlReadWrite.getOpenFile())); // file setting.(유효한 파일인지는 호출한 함수 안에서 확인 한다.)
			}
			if ((new File(xmlReadWrite.getSavedPath()).exists())) { // 저장경로가 존재하는지 확인.
				destPath = xmlReadWrite.getSavedPath(); // 저장경로 Setting
			}
			savedPathLabel.setText(destPath);
			separatorStart.setText(xmlReadWrite.getSeparatorB());
			separatorEnd.setText(xmlReadWrite.getSeparatorE());
			separatorMid.setText(xmlReadWrite.getSeparatorM());
			if (null != xmlReadWrite.getAllCateValues()) { // AllCategory 값들이 null인지 확인.
				String[] allCateValArray = xmlReadWrite.getAllCateValues().split("\\|"); // '|'기준으로 쪼개서 String배열로 만듬.
				if ( allCateValArray.length == 4) { // 배열길이가 4가 아니면 값 setting하지 않는다.
					lessThanCheckBox.setSelected(stringToBool(allCateValArray[1]));
					equalCheckBox.setSelected(stringToBool(allCateValArray[2]));
					levelCombo.setSelectedItem(allCateValArray[3]);
					allCheckbox.setSelected(stringToBool(allCateValArray[0]));
				}
			}
			// 받아온 categoryArray값 들이 null이 아닌지 확인 후에, category들을 생성한다.
			if (null != xmlReadWrite.getCategoryArray()){
				createCategory(xmlReadWrite.getCategoryArray());
			}
		}
	}
	
	
	/* category 를 삭제 할 때 호출되는 Method, CategoryPanel안에 '-'버튼을 누르면 아래의 Method가 호출된다. 
	 * categoryPanel 객체를 생성할때 부여되었던 Key값을 Parameter로 넘긴다. Key값을 통해 삭제가 된다. */
	public void removeCategori(int receivedKey){
		categoryListBox.remove(categoryMap.get(receivedKey)); // Box안에 있던 categoryPanel을 삭제
		categoryMap.remove(receivedKey); // Map안에서도 지운다.
		this.setVisible(true); // Frame 갱신
		showSearchExample(); // 예시문구 변경될 것이 있는지 확인.
		System.out.println("categoryMap.size : "+categoryMap.size()+", categoryNum : "+receivedKey);
	}
	
	/* Save Button을 Open할 파일 존재여부, cateogy유무에 따라 활성화 시켜주는 Method. */
	public void checkSaveFileBtn() {
		if ((fileSetterGetter.settingFile == null) || (categoryMap.size() == 0)) {
			saveFileBtn.setEnabled(false);
		} else {
			saveFileBtn.setEnabled(true);
		}
	}
	
	/* File의 존재여부를 판단해 File을 Setting 해주는 Method. */
	public void setFile(File file) {
		if (file.exists()) {
			fileSetterGetter.setFile(file);
			setTitle(TITLE+" - "+fileSetterGetter.getPath());
			filePathLabel.setText(fileSetterGetter.getName());
		}
	}
	
	/* 예시구문을 조건에 따라 보여주는 Method */
	public void showSearchExample() {
		String testString = "TEST";
		String testNum = "0";
		if (categoryMap.size() > 0) { // category가 있을 때 보여준다.
			exampleLabel.setVisible(true);
			/* 중간구분자가 유/무에 따라서 예시문구가 다르다. Search방식도 다르다. 
			 * 중간 구분자가 있을 때 - 시작구분자 + 카테고리 이름 + 중간구분자 + 아무글자 + 중간구분자 + 레벨 + 마지막구분자 [ENGN:*:0] 
			 * 중간 구분자가 없을 때 - 시작구분자 + 카테고리 이름 + 레벨 + 마지막 구분자 [FLW3]*/
			if (0 < separatorMid.getText().length()) {
				exampleLabel.setText(searchExample + separatorStart.getText() + testString + separatorMid.getText() + "*"
						+ separatorMid.getText() + testNum + separatorEnd.getText());
			} else {
				exampleLabel.setText(searchExample + separatorStart.getText() + testString + testNum + separatorEnd.getText());
			}
		} else {
			exampleLabel.setVisible(false);
		}
	}
	
	/* String T/F 를 boolean 으로 변경하는 Method */
	public boolean stringToBool(String trueFalse) {
		return ("T".equals(trueFalse));
	}
	
	/* boolean을 String T/F 로 변경하는 Method */
	public String boolToString(boolean trueFalse) {
		return (trueFalse ? "T" : "F");
	}
	
	public void unselectAllCheckbox() {
		if (allCheckbox.isSelected()) {
			allCheckbox.setSelected(false);
		}
	}
	
//	public MainFrame getCompnentFrame() {
//		return mainFrame;
//	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		showGUI();
	}
	
	public static void main(String[] args) {
		mainFrame = new MainFrame(TITLE);
		SwingUtilities.invokeLater(mainFrame);
	}

}
