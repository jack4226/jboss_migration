package jpa.msgui.bean;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@javax.inject.Named("uploadForm")
@javax.enterprise.context.RequestScoped
public class FileUploadForm implements java.io.Serializable {
	private static final long serialVersionUID = -841769703445641393L;
	private String fromPage;
	// Init
    private File file1;
    private File file2;
    private File file3;
    private File file4;
    private File file5;
    private File file6;
    private File file7;
    private File file8;
    private File file9;
    private File file10;
    private final Map<String, String> errors = new LinkedHashMap<String, String>();
    private final Map<String, String> messages  = new LinkedHashMap<String, String>();
    private final Map<String, String> pathes = new HashMap<String, String>();

    public String getFromPage() {
		return fromPage;
	}

	public void setFromPage(String fromPage) {
		this.fromPage = fromPage;
	}

	// Helpers
    public Map<String, String> getErrors() {
        return errors;
    }

    public Map<String, String> getMessages() {
        return messages;
    }

    public Map<String, String> getPathes() {
        return pathes;
    }

    public void setError(String fieldName, String message) {
        errors.put(fieldName, message);
    }

    public void setMessage(String fieldName, String message) {
        messages.put(fieldName, message);
    }

    public void setPathes(String fieldName, String filePath) {
    	pathes.put(fieldName, filePath);
    }
    
    public boolean hasErrors() {
        return errors.size() > 0;
    }
    
    public boolean hasMessages() {
    	return messages.size() > 0;
    }
    
    public void clear() {
    	errors.clear();
    	messages.clear();
    	pathes.clear();
    }
    
    public void setFile(int seq, File file) {
    	if (seq == 1) setFile1(file);
    	else if (seq == 2) setFile2(file);
    	else if (seq == 3) setFile3(file);
    	else if (seq == 4) setFile4(file);
    	else if (seq == 5) setFile5(file);
    	else if (seq == 6) setFile6(file);
    	else if (seq == 7) setFile7(file);
    	else if (seq == 8) setFile8(file);
    	else if (seq == 9) setFile9(file);
    	else if (seq == 10) setFile10(file);
    }

    // Getters
    public File getFile1() {
        return file1;
    }

    public void setFile1(File file) {
        this.file1 = file;
    }

	public File getFile2() {
		return file2;
	}

	public void setFile2(File file2) {
		this.file2 = file2;
	}

	public File getFile3() {
		return file3;
	}

	public void setFile3(File file3) {
		this.file3 = file3;
	}

	public File getFile4() {
		return file4;
	}

	public void setFile4(File file4) {
		this.file4 = file4;
	}

	public File getFile5() {
		return file5;
	}

	public void setFile5(File file5) {
		this.file5 = file5;
	}

	public File getFile6() {
		return file6;
	}

	public void setFile6(File file6) {
		this.file6 = file6;
	}

	public File getFile7() {
		return file7;
	}

	public void setFile7(File file7) {
		this.file7 = file7;
	}

	public File getFile8() {
		return file8;
	}

	public void setFile8(File file8) {
		this.file8 = file8;
	}

	public File getFile9() {
		return file9;
	}

	public void setFile9(File file9) {
		this.file9 = file9;
	}

	public File getFile10() {
		return file10;
	}

	public void setFile10(File file10) {
		this.file10 = file10;
	}
}
