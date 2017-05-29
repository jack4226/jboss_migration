package jpa.service.external;

public interface VariableResolver {

	static final String LF = System.getProperty("line.separator", "\n");
	
	public String process(int addrId);
}
