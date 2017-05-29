package jpa.model.msg;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import jpa.model.BaseModel;

@Entity
@Table(name="message_folder", uniqueConstraints=@UniqueConstraint(columnNames = {"folderName"}))
public class MessageFolder extends BaseModel implements Serializable {
	private static final long serialVersionUID = 1129960153818231522L;

	@Column(nullable=false, length=26)
	private String folderName = "";
	@Column(nullable=true, length=255)
	private String description = null;
	
	public String getFolderName() {
		return folderName;
	}
	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

}
