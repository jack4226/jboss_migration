package jpa.service.external;

public interface TargetTextProc {

	static final String LF = System.getProperty("line.separator", "\n");
	
	public String process();
}
