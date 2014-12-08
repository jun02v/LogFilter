import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;
 
public class XmlReaderWriter {
	
	public static File settingFile;
	public String openFile = null;
	public String savedPath = null;
	public String separatorB = null;
	public String separatorE = null;
	public String separatorM = null;
	public String allCateValues = null;
	public ArrayList<String> categoryArray;
	
	public static final String LOG_FILTER = "logFilter";
	public static final String OPEN_FILE = "openFile";
	public static final String SAVED_PATH = "savedPath";
	public static final String SEPARATOR_BIGIN = "separatorB";
	public static final String SEPARATOR_END = "separatorE";
	public static final String SEPARATOR_MIDDLE = "separatorM";
	public static final String ALL_CATEGORY = "allCategory";
	public static final String CATEGORY = "category";
	public static final String NAME = "name";
	public static final String ENABLE = "enable";
	public static final String LESS_THAN = "lessThan";
	public static final String EQUAL = "equal";
	public static final String LEVEL = "level";
	
	/* 생성자, 가장먼저 log_filter.xml을 file객체로 생성하게 되어 있다. */
	XmlReaderWriter() {
		settingFile = new File("log_filter.xml");
		System.out.println("File exists - "+settingFile.exists());
	}
	
	/* xml파일을 Parsing하는 Method. */
	public boolean parseXml() {
		if (!settingFile.exists()) { // 가장먼저 log_filter.xml이 존재하는지 확인한다.
			return false;
		} else {
			try {
				DocumentBuilderFactory docBuildFact = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuild = docBuildFact.newDocumentBuilder();
				Document doc = docBuild.parse(settingFile);
				doc.getDocumentElement().normalize();

				//System.out.println("Root element : " + doc.getDocumentElement().getNodeName());
				
				/* 각각의  태그에 맞는 값들을 가져온다.*/
				openFile = getNodeValue(doc, OPEN_FILE);
				savedPath = (null != getNodeValue(doc, "savedPath") ? getNodeValue(doc, "savedPath") : System.getProperty("user.dir"));
				
				separatorB = getNodeValue(doc, "separatorB");
				if(null != getNodeValue(doc, "separatorB")){
					separatorB = separatorB.trim().substring(0, 1);
				}
				separatorE = getNodeValue(doc, "separatorE");
				if(null != getNodeValue(doc, "separatorE")){
					separatorE = separatorE.trim().substring(0, 1);
				}
				separatorM = getNodeValue(doc, "separatorM");
				if(null != getNodeValue(doc, "separatorM")){
					separatorM = separatorM.trim().substring(0, 1);
				}
				
				System.out.println("openFile-"+openFile);
				System.out.println("savedPath-"+savedPath);
				System.out.println("separatorB-"+separatorB);
				System.out.println("separatorE-"+separatorE);
				System.out.println("separatorD-"+separatorM);
				
				int allCateNodeLength = doc.getElementsByTagName("allCategory").getLength();
				Node allCategoryNode = doc.getElementsByTagName("allCategory").item(0);
				
				if ((allCateNodeLength > 0) && (allCategoryNode.getNodeType() == Node.ELEMENT_NODE)) {
					Element allCategoryElement = (Element)allCategoryNode;
					allCateValues = allCategoryElement.getAttribute("enable")+"|";
					allCateValues += allCategoryElement.getAttribute("lessThan")+"|";
					allCateValues += allCategoryElement.getAttribute("equal")+"|";
					allCateValues += allCategoryElement.getAttribute("level");
					System.out.println("All category values = "+allCateValues);
				}			
				
				NodeList categoryList = doc.getElementsByTagName("category");
				int cateNodeLength = doc.getElementsByTagName("category").getLength();
				categoryArray = new ArrayList<String>();
 
				if (cateNodeLength > 0) {
					for (int i = 0; i < categoryList.getLength(); i++) { 	
						Node categoryNode = categoryList.item(i);
						
						if (categoryNode.getNodeType() == Node.ELEMENT_NODE) {
							String categoryValues = null;
							Element categoryElmnt = (Element) categoryNode;						
							categoryValues = categoryElmnt.getAttribute("enable")+"|";
							categoryValues += categoryElmnt.getAttribute("name")+"|";
							categoryValues += categoryElmnt.getAttribute("lessThan")+"|";
							categoryValues += categoryElmnt.getAttribute("equal")+"|";
							categoryValues += categoryElmnt.getAttribute("level");
							categoryArray.add(categoryValues);
							System.out.println("category"+i+" values = "+categoryValues);
						}
					}
				}
	 
			} catch (SAXParseException e) {

				e.printStackTrace();
				return false;
				
			} catch (DOMException e) {
				
				e.printStackTrace();
				return false;
				
			} catch (NullPointerException e) {
				
				e.printStackTrace();
				return false;
				
			} catch (Exception e) {
				
				e.printStackTrace();
				return false;
				
			}
			return true;
		}
	}
	
	/* Document객체와, tag명을 받아서 value값을 return하는 Method */
	public String getNodeValue(Document doc, String tag) {
		if (doc.getElementsByTagName(tag).getLength() > 0) {
			Node tagNode = doc.getElementsByTagName(tag).item(0).getFirstChild();			
			if (tagNode == null) {
				return null;
			} else {
				return tagNode.getTextContent();
			}
		}
		return null;
	}
	
	/* xml에다 값을 입력하는 Method, getter Method들에서 값들을 받아와서 써준다. */
	public void setWriteXml() throws TransformerConfigurationException {
		
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement(LOG_FILTER);
			doc.appendChild(rootElement);
			Element openFileElement = doc.createElement(OPEN_FILE);
			openFileElement.setTextContent(getOpenFile());
			rootElement.appendChild(openFileElement);
			
			Element savedPathElement = doc.createElement("savedPath");
			savedPathElement.setTextContent(getSavedPath());
			rootElement.appendChild(savedPathElement);
			
			Element separatorBElement = doc.createElement("separatorB");
			separatorBElement.setTextContent(getSeparatorB());
			rootElement.appendChild(separatorBElement);
			
			Element separatorEElement = doc.createElement("separatorE");
			separatorEElement.setTextContent(getSeparatorE());
			rootElement.appendChild(separatorEElement);
			
			Element separatorMElement = doc.createElement("separatorM");
			separatorMElement.setTextContent(getSeparatorM());
			rootElement.appendChild(separatorMElement);
			
			Element allCategoryElement = doc.createElement(ALL_CATEGORY);
			String[] allCatgoryValues = getAllCateValues().split("\\|");
			allCategoryElement.setAttribute(ENABLE, allCatgoryValues[0]);
			allCategoryElement.setAttribute(LESS_THAN, allCatgoryValues[1]);
			allCategoryElement.setAttribute(EQUAL, allCatgoryValues[2]);
			allCategoryElement.setAttribute(LEVEL, allCatgoryValues[3]);
			
			rootElement.appendChild(allCategoryElement);
			
			ArrayList<String> categoryArray = getCategoryArray();			 
			if (categoryArray.size() > 0) {
				for (int i = 0; i < categoryArray.size(); i++) {
					String categoryValueSet = categoryArray.get(i);
					String[] categoryValues = categoryValueSet.split("\\|");					
					Element categoryElement = doc.createElement(CATEGORY);
					categoryElement.setAttribute(ENABLE, categoryValues[0]);
					categoryElement.setAttribute(NAME, categoryValues[1]);
					categoryElement.setAttribute(LESS_THAN, categoryValues[2]);
					categoryElement.setAttribute(EQUAL, categoryValues[3]);
					categoryElement.setAttribute(LEVEL, categoryValues[4]);
					rootElement.appendChild(categoryElement);
				}
			}
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.VERSION, "1.0");
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(settingFile);
			
			transformer.transform(source, result);
			System.out.println("File Saved~!");
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/* Setter / Getter Method. */
	public String getOpenFile() {
		return openFile;
	}
	
	public String getSavedPath() {
		return savedPath;
	}
	
	public String getSeparatorB() {
		return separatorB;
	}
	
	public String getSeparatorE() {
		return separatorE;
	}
	
	public String getSeparatorM() {
		return separatorM;
	}
	
	public String getAllCateValues() {
		return allCateValues;
	}
	
	public ArrayList<String> getCategoryArray() {
		return categoryArray;
	}

	public void setOpenFile(String openFile) {
		this.openFile = openFile;
	}

	public void setSavedPath(String savedPath) {
		this.savedPath = savedPath;
	}

	public void setSeparatorB(String separatorB) {
		this.separatorB = separatorB;
	}

	public void setSeparatorE(String separatorE) {
		this.separatorE = separatorE;
	}
	
	public void setSeparatorM(String separatorM) {
		this.separatorM = separatorM;
	}

	public void setAllCateValues(String allCateValues) {
		this.allCateValues = allCateValues;
	}

	public void setCategoryArray(ArrayList<String> categoryArray) {
		this.categoryArray = categoryArray;
	}
	
	
}