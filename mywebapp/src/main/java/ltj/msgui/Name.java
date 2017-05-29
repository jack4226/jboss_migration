package ltj.msgui;

public class Name {
	private String first;
	private String last;
	private boolean editable;
	private boolean markedForDeletion = false;

	public Name(String first, String last) {
		this.first = first;
		this.last = last;
	}

	public void setFirst(String newValue) {
		first = newValue;
	}

	public String getFirst() {
		return first;
	}

	public void setLast(String newValue) {
		last = newValue;
	}

	public String getLast() {
		return last;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	public boolean isEditable() {
		return editable;
	}

	public boolean isMarkedForDeletion() {
		return markedForDeletion;
	}

	public void setMarkedForDeletion(boolean markedForDeletion) {
		this.markedForDeletion = markedForDeletion;
	}
}
