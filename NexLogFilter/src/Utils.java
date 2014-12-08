import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;


public class Utils implements PropertyChangeListener {
	public HashMap<Integer, CategoryPanel> categoryMap;
	public ArrayList<String> searchArray = null;
	public JProgressBar parsingProgress;
	private MainFrame mainFrame;
	
	/* allSelect - 모든 카테고리들을 Enable 시키고, All category의 내용을 전부 적용시킨다. categoryMap과 All category의 설정값들을 받는다. */
	public void allSelect (HashMap<Integer, CategoryPanel> paramMap, boolean equalsChkbox, boolean lessthanChkbox, Object selectLevel) {
		Collection<CategoryPanel> collection = paramMap.values();
		Iterator<CategoryPanel> iterator = collection.iterator();
		while(iterator.hasNext()){
			CategoryPanel categoryPanel = iterator.next();
			categoryPanel.enableChk.setSelected(true);
			categoryPanel.equalChk.setSelected(equalsChkbox);
			categoryPanel.lessThanChk.setSelected(lessthanChkbox);
			categoryPanel.levelCombo.setSelectedItem(selectLevel);
		}
	}
	
	/* settingSearchWords Category에 있는 내용들을 실제 파일에서 찾으려는 String으로 만들고, ArrayList로 묶는다. */
	public boolean settingSearchWords (HashMap<Integer, CategoryPanel> paramMap, String separatorStart, String separatorEnd, String separatorMid) {
		searchArray = new ArrayList<String>();
		Collection<CategoryPanel> collection = paramMap.values();
		Iterator<CategoryPanel> iterator = collection.iterator();
		int checkSelected = 0;
		while(iterator.hasNext()){
			CategoryPanel categoryPanel = (CategoryPanel) iterator.next();
			if (categoryPanel.enableChk.isSelected() && categoryPanel.categoryName.getText().trim().length() != 0) { // Enable Checkbox가 체크 되었고, 카테고리명이 들어있는지 확인.
				System.out.println("Utils 48 : categoryPanel.categoryName.getText().trim().length() : "+categoryPanel.categoryName.getText().trim().length());
				String searchString;
				if (separatorMid.trim().length() == 0) { 
					searchString = separatorStart + categoryPanel.categoryName.getText(); // 중간구분자가 없다면, "[FLW" 까지 String으로 만들고,
				} else {
					searchString = separatorStart + categoryPanel.categoryName.getText() + separatorMid + "-" + separatorMid; // " 있다면, [ENGN:-:" 까지 String으로 만든다,
				}
				int parseInt = Integer.parseInt((String) categoryPanel.levelCombo.getSelectedItem()); // level값을 int형으로 변환한다음,
				if (categoryPanel.lessThanChk.isSelected() && parseInt > 0) { // lessthan 이 체크 되어 있고, level이 0 이상일 경우,
					int j = 0;
					do { // 0부터 level값-1 만큼 돌면서,
						searchString += Integer.toString(j) + separatorEnd;// "[ENGN:-:1]" 또는 "[FLW1]" 돌고있는 값과 마지막 구분자를 만들었던 String에 붙여서 , 
						searchArray.add(searchString); // ArrayList<String>에 추가한다.
						System.out.println("Utils 61 : "+searchString);
						// String에서 마지막 구분자 길이 + level길이(1) 만큼 자른다."[ENGN:-:", "[FLW"
						searchString = searchString.substring(0, searchString.length()-separatorEnd.length()-1); 
						j++;
					} while (j < parseInt); // 반복한다.
				}
				if (categoryPanel.equalChk.isSelected()) { // equal Combobox가 체크되어있는 지 확인한 후, 
					searchString += Integer.toString(parseInt) + separatorEnd; // 위에서 만들었던 level값을 가져와 마지막 구분자와 함께 붙여준다.
					searchArray.add(searchString); // List에 추가.
					System.out.println("Utils 70 : "+searchString);
				}
			} else {
				checkSelected++; // Category enable Checkbox가 체크 해제 되어있거나, 카테고리명이 없는것을 확인하여,   
				System.out.println("checkSelected:"+checkSelected);
				if (checkSelected == collection.size() ){ // 모든카테고리가 다 그렇다면, False를 리턴한다.
					return false;
				}
			}
		}
		System.out.println("Utils:80 collection.size()"+collection.size());
		return true;
	}
	
	/* 파일을 검색하고 저장할때 가장 처음 호출되는 메소드.
	 * 검색할 파일, 저장경로, Progress bar를 Parameter로 받는다.
	 * 파일을 저장하면서, Progress bar를 생성하기 위해서는,
	 * Background에서 돌아가는 Class를 만들고, 처리진행에 따른 Progress Bar를  보여줘야 한다. 
	 */
	public void parseAndSave (File file, String destPath, JProgressBar progressBar, MainFrame mainFrame) {
		parsingProgress = progressBar;
		ParseTask parseTask = new ParseTask(file, destPath);
		this.mainFrame = mainFrame;
		parseTask.addPropertyChangeListener(this);
		parseTask.execute();
	}

	/* Progress event 를 받기위한 Method. */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// TODO Auto-generated method stub
		if ("progress" == evt.getPropertyName()) {
			int progress = (Integer) evt.getNewValue();
			parsingProgress.setValue(progress);
		}
		
	}
	
	/* SwingWorker를 상속받은 클래스, Thread 처럼 백그라운드에서 돌지만, UI단으로 Message를 띄워줄수 있다. 
	 * Background에서 스트링을 Search하면서 현재 진행률을 알려준다.
	 * Search / File Write 까지 다 끝나고 난 후엔 done Method가 호출되면서 끝난다. 
	 */
	class ParseTask extends SwingWorker<Void, Void> {
		
		File readFile = null;
		String destPath = null;
		
		ParseTask(File file, String path){
			readFile = file;
			destPath = path;
		}

		/* 실제 Background에서 도는 Method */
		@Override
		protected Void doInBackground() throws Exception {
			// TODO Auto-generated method stub
			setProgress(0); // Progress 0부터 시작
			int progress;
			long fileLength = readFile.length(); // 파일 전체크기를 Byte단위로 받아온다.
			try {
				/* ProgressInputStream - FilterInputStream을 상속받아 만든 클래스, 파일을 읽을때, 이 클래스 안에 read함수가 호출되는데,
				 * read 할 때마다 byte단위로 counting하여, 전체 크기와 현재까지 counting한 값을 나눠 현재진행률을 알수 있다.*/
				ProgressInputStream pis = new ProgressInputStream(new FileInputStream(readFile), fileLength);
				BufferedReader bufferReader = new BufferedReader(new InputStreamReader(pis));
				BufferedWriter bufferWriter = new BufferedWriter(new FileWriter(destPath+"\\Parsing_"+readFile.getName().split("\\.")[0]+"_"+System.currentTimeMillis()+".txt"));
				String readLine = null;
				System.out.println("START : "+System.currentTimeMillis());
				boolean isMiddleSep = searchArray.get(0).contains("-"); // '-'가 있는 것은 중간구분자 가 있는 것이고, 없는것은 중간구분자가 없는 것이다.
				while ((readLine = bufferReader.readLine()) != null) { // BufferedReader로 1 line씩 읽는다.
					progress = (int)(pis.getProgress() * 100.0); // 현재 몇 퍼센트 read했는지 받아옴.
					for (int i = 0 ; i < searchArray.size() ; i++) { // readLine에 ArrayList로 저장한 String들과 비교함. ArrayList갯수만큼 반복.
						if (isMiddleSep) { // 중간 구분자가 있는지 체크,
							String splitSearch[] = searchArray.get(i).split("-"); // "[ENGN:-:0]"이런 형식으로 되어있는 String을 "-" 기준으로 자름.
							if (readLine.contains(splitSearch[0]) && readLine.contains(splitSearch[1])) { // "[ENGN:", ":0]"이 포함되어 있는지 확인하여 write함., 
								bufferWriter.write(readLine+"\n");
							}
						} else { // 중간 구분자가 없는 것은 하나의 String으로 찾으면 됨."[FLW3]"
							if (readLine.contains(searchArray.get(i))) {
								bufferWriter.write(readLine+"\n");
							}
						}						
					}
					setProgress(progress); // 한 line읽고나서 progress Set해 줌.
				}
				bufferWriter.flush();
				bufferReader.close();
				bufferWriter.close();
				System.out.println("END : "+System.currentTimeMillis());
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		/* 작업이 완료된 후에 호출되는 Method */
		@Override
		protected void done() {
			// TODO Auto-generated method stub
			if (!isCancelled()){ // 실패하지 않았다면, Success했다고 Dialog로 알려줌.
				JOptionPane.showMessageDialog(null,
	                    "File created successfully!", "Message",
	                    JOptionPane.INFORMATION_MESSAGE);
			}
			setProgress(0); // 다시 progress는 0
//			FilterFrame filterFrame = new FilterFrame();
//			filterFrame.enableAllComponents(); // 모든 컴포넌트들 enable시킴.
//			filterFrame.showSearchExample(); // 예시문구도 보여줌.
			
			mainFrame.enableAllComponents(); // 모든 컴포넌트들 enable시킴.
			mainFrame.showSearchExample(); // 예시문구도 보여줌.
			super.done();
		}
	}
}
