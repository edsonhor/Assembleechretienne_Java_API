// package FunctionalTest;
//
// import static org.junit.Assert.*;
//
// import java.sql.Connection;
// import java.sql.DriverManager;
// import java.sql.SQLException;
// import java.sql.Types;
// import java.util.ArrayList;
//
//
// import org.junit.Before;
// import org.junit.Test;
//
// import DAL.MysqlDataAccessHelper;
// import DAL.MysqlParameter;
// import DAL.MysqlParameter.ParameterDirection;
// import Entities.UserCredential;
//
// public class MysqlDataAccessHelperFuntionalTest {
//
//
// @Before
// public void setUp() throws Exception {
// // Class.forName("com.mysql.jdbc.Driver").newInstance();
// }
//
//
// @Test
// public void testExecuteStoreProcedureWithOutputParameter() {
// UserCredential usedata= new UserCredential();
// MysqlDataAccessHelper unit_uder_test= MysqlDataAccessHelper.getInstance();
// unit_uder_test.ExecuteStoreProcedureWithOutputParameter("GET_USER_CREDIDENTIAL",getUserCredential(),usedata);
// }
//
// @Test
// public void testExecuteStoreProcedure_No_Output_Parameter() {
// MysqlDataAccessHelper unit_uder_test= MysqlDataAccessHelper.getInstance();
// boolean
// t=unit_uder_test.ExecuteStoreProcedure_No_Output_Parameter("ONBOARDING_USER",setUserCredentialParam("username","email","This
// is passord Salt","This is password hash"));
// assertEquals(true,t);
// }
//
// private ArrayList<MysqlParameter> getUserCredential(){
// ArrayList<MysqlParameter> param= new ArrayList<MysqlParameter>();
// param.add(new MysqlParameter("p_email",
// Types.VARCHAR,"edson.philippe@ufl.edu", ParameterDirection.INPUT));
// return param;
// }
//
// private ArrayList<MysqlParameter> setUserCredentialParam(String
// username,String email, String password_salt, String password_hash){
// ArrayList<MysqlParameter> param= new ArrayList<MysqlParameter>();
// param.add(new MysqlParameter("p_email", Types.VARCHAR,email,
// ParameterDirection.INPUT));
// param.add(new MysqlParameter("p_password_salt", Types.VARCHAR,password_salt,
// ParameterDirection.INPUT));
// param.add(new MysqlParameter("p_password_hash", Types.VARCHAR,password_hash,
// ParameterDirection.INPUT));
// param.add(new MysqlParameter("p_username", Types.VARCHAR,username,
// ParameterDirection.INPUT));
// return param;
// }
// }
