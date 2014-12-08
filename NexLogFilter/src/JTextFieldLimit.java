import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/* Text box에 지정된 숫자만큼만 문자 입력되도록 만들어주는 클래스 */
@SuppressWarnings("serial")
public class JTextFieldLimit extends PlainDocument {
	
	private int limit;
    // optional uppercase conversion
	private boolean toUppercase = false;
    
    JTextFieldLimit(int limit) {
        super();
        this.limit = limit;
    }
    
    JTextFieldLimit(int limit, boolean upper) {
        super();
        this.limit = limit;
        this.toUppercase = upper;
    }
    
    public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
        if (str == null) {
            return;
        }
        
        if ( (getLength() + str.length()) <= limit) {
            if (toUppercase) {
                str = str.toUpperCase();
            }
            super.insertString(offset, str, attr);
        }
    }
}
