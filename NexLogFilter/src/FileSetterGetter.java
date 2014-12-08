import java.io.File;


public class FileSetterGetter {
	File settingFile = null;
	
	public void setFile(File file) {
		settingFile = file;
	}
	
	public File getFile() {
		return settingFile;
	}
	
	public String getName() {
		return null == settingFile ? null : settingFile.getName();
	}
	
	public String getPath() {
		return null == settingFile ? null : settingFile.getPath();
	}
}
