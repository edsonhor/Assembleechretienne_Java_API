package Entities;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

import DAL.Idatamapper;
import DAL.MysqlParameter;
import DAL.MysqlParameter.ParameterDirection;

public class UserCredential implements Idatamapper<UserCredential> {

	private String username;
	private String password;
	private String email;
	private String password_hash;
	private String password_salt;

	public UserCredential(String username, String email, String password_salt, String password_hash) {
		this.username = username;
		this.email = email;
		this.password_hash = password_hash;
		this.password_salt = password_salt;
	}

	public UserCredential(String email, String password) {
		this.email = email;
		this.setPassword(password);
	}

	public UserCredential() {
	}

	public ArrayList<UserCredential> MapDataToLocalEntity(ResultSet resultSet) {
		ArrayList<UserCredential> list_of_user = new ArrayList<UserCredential>();
		try {
			while (resultSet.next()) {
				list_of_user.add(new UserCredential(resultSet.getString("username"), resultSet.getString("email"),
						resultSet.getString("password_salt"), resultSet.getString("password_hash")));
			}
			return list_of_user;
		} catch (SQLException e) {
			e.printStackTrace(); // will need to send this info to elasticSeach
		}

		return list_of_user;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword_hash() {
		return password_hash;
	}

	public void setPassword_hash(String password_hash) {
		this.password_hash = password_hash;
	}

	public String getPassword_salt() {
		return password_salt;
	}

	public void setPassword_salt(String password_salt) {
		this.password_salt = password_salt;
	}

	public ArrayList<MysqlParameter> getUserCredentialParam(String user_email) {
		ArrayList<MysqlParameter> param = new ArrayList<MysqlParameter>();
		param.add(new MysqlParameter("p_email", Types.VARCHAR, user_email, ParameterDirection.INPUT));
		return param;
	}

	public ArrayList<MysqlParameter> setUserCredentialParam(String username, String email, String password_salt,
			String password_hash) {
		ArrayList<MysqlParameter> param = new ArrayList<MysqlParameter>();
		param.add(new MysqlParameter("p_email", Types.VARCHAR, email, ParameterDirection.INPUT));
		param.add(new MysqlParameter("p_password_salt", Types.VARCHAR, password_salt, ParameterDirection.INPUT));
		param.add(new MysqlParameter("p_password_hash", Types.VARCHAR, password_hash, ParameterDirection.INPUT));
		param.add(new MysqlParameter("p_username", Types.VARCHAR, username, ParameterDirection.INPUT));
		return param;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

}
