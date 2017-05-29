package jpa.data.preload;

/*
 * define standard message folders
 */
public enum FolderEnum {

	All("All Messages"),
	Inbox("Messages received. Default folder"),
	Sent("Messages sent"),
	Draft("Messages drafted"),
	Trash("Messages deleted"),
	Archive("Messages archived"),
	Closed("Messages closed"),
	Spam("Messages marked as Spam");
	
	private String description;
	private FolderEnum(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}

	public static FolderEnum getByName(String name) {
		for (FolderEnum folder : FolderEnum.values()) {
			if (folder.name().equalsIgnoreCase(name)) {
				return folder;
			}
		}
		throw new IllegalArgumentException("Invalid enum name (" + name + ") received.");
	}

}
