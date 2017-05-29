package jpa.msgui.vo;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/*
 * A Boolean is defined as TINYINT in MySQL, and it's returned as Java Class
 * java.lang.Boolean if the configuration property tinyInt1isBit is set to true
 * (the default) and the storage size is 1, or java.lang.Integer if not.
 * 
 * So for MySQL there is no need to use this adapter if the configuration 
 * property for tinyInt1isBit is kept to default.
 * 
 * There is no need to use this adapter For PostgreSQL or Derby since they both
 * have native Boolean data type.
 * 
 * This adapter will probably only be useful to Oracle database.
 */
public class BooleanAdapter extends XmlAdapter<Integer, Boolean> {

	@Override
	public Boolean unmarshal(Integer v) throws Exception {
		return (v == null ? null : v == 1);
	}

	@Override
	public Integer marshal(Boolean v) throws Exception {
		return (v == null ? null : v ? 1 : 0);
	}

}
