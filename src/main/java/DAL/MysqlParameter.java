package DAL;

import java.sql.Types;

public class MysqlParameter {

	private String name; // parameter name should match database colon name
	private int type; // java.sql.Types;
	private String value;
	private ParameterDirection parameterdirection;

	public MysqlParameter(String name, int type, String value, ParameterDirection parameterdirection) {
		this.name = name;
		this.type = type;
		this.value = value;
		this.parameterdirection = parameterdirection;
	}

	public void SetName(String newname) {
		this.name = newname;
	}

	public void SetType(Types newtype) {
		this.type = Integer.parseInt(newtype.toString());
	}

	public void SetValue(String newvalue) {
		this.value = newvalue;
	}

	public String Getvalue() {
		return this.value;
	}

	public int Gettype() {
		return this.type;
	}

	public String GetName() {
		return this.name;
	}

	public ParameterDirection GetParameterDirection() {
		return this.parameterdirection;
	}

	public void SetParameterDirection(ParameterDirection direction) {
		this.parameterdirection = direction;
	}

	public enum ParameterDirection {
		INPUT, OUTPUT, INPUTOUTPUT,
	}

}
