package DAL;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import DAL.MysqlParameter.ParameterDirection;

public class MysqlDataAccessHelper {

	private static final Logger logger = LogManager.getLogger(MysqlDataAccessHelper.class);

	private DataSource datasource;

	private MysqlDataAccessHelper() {
		Context initCtx;
		try {
			initCtx = new InitialContext();
			Context envCtx = (Context) initCtx.lookup("java:comp/env");
			DataSource datasource = (DataSource) envCtx.lookup("jdbc/assembleechretienne");
			this.datasource = datasource;

		} catch (NamingException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

	// Singleton Public method, which is the only method allowed to return an
	// instance of the singleton
	// (the instance here is the database connection statement)
	public static MysqlDataAccessHelper getInstance() {
		return MysqlDataAcessHelperSingleton.INSTANCE;
	}

	/*
	 * Private inner class responsible for instantiating the single instance of
	 * the singleton
	 */
	private static class MysqlDataAcessHelperSingleton {
		private static final MysqlDataAccessHelper INSTANCE = new MysqlDataAccessHelper();
	}

	public <T> ArrayList<T> ExecuteStoreProcedureWithOutputParameter(String StoreProcedureName,
			ArrayList<MysqlParameter> parameters, Idatamapper<T> datamapper) {
		int number_of_parameters = parameters.size();
		String prepareCallInputParam = PrepareCallInputString(number_of_parameters, StoreProcedureName);
		CallableStatement cStmt = null;
		ResultSet resut = null;
		Connection conn = null;
		try {
			conn = datasource.getConnection();
			cStmt = conn.prepareCall(prepareCallInputParam);
			for (MysqlParameter param : parameters) {
				if (param.GetParameterDirection() == ParameterDirection.INPUT) {
					cStmt.setObject(param.GetName(), param.Getvalue(), param.Gettype());
				} else {
					cStmt.registerOutParameter(param.GetName(), param.Gettype());
				}
			}

			resut = cStmt.executeQuery();
			ArrayList<T> res = datamapper.MapDataToLocalEntity(resut);
			closeConnection(resut, cStmt, conn);
			return res;
		}

		catch (SQLException e)

		{
			closeConnection(resut, cStmt, conn);
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	public String ExecuteStoreProcedure_No_Output_Parameter(String StoreProcedureName,
			ArrayList<MysqlParameter> parameters) {
		int number_of_parameters = parameters.size();
		String prepareCallInputParam = PrepareCallInputString(number_of_parameters, StoreProcedureName);
		CallableStatement cStmt = null;

		Connection conn = null;
		StringBuilder returnmessage = new StringBuilder(100);
		try {
			conn = datasource.getConnection();
			cStmt = conn.prepareCall(prepareCallInputParam);
			for (MysqlParameter param : parameters) {
				if (param.GetParameterDirection() == ParameterDirection.INPUT) {
					cStmt.setObject(param.GetName(), param.Getvalue(), param.Gettype());
				} else {
					cStmt.registerOutParameter(param.GetName(), param.Gettype());
				}
			}

			if (!cStmt.execute()) {
				return returnmessage.append("valid").toString(); // if
																	// everything
																	// went well
			}
		} catch (SQLException e) {
			logger.error(e.getMessage());
			returnmessage = returnmessage.append("invalid:" + e.getErrorCode());
			e.printStackTrace();
		} finally {
			closeConnection(null, cStmt, conn);
		}

		return returnmessage.toString();
	}

	public void closeConnection(ResultSet resultset, CallableStatement statement, Connection conn) {

		try {
			if (resultset != null) {
				resultset.close();
				resultset = null;
			}
			statement.close();
			statement = null;

			conn.close();
			conn = null;

		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}

		finally {

			if (resultset != null) {
				try {
					resultset.close();
				} catch (SQLException e) {
					logger.error(e.getMessage());
					e.printStackTrace();
				}
			}
			resultset = null;
		}
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}
			statement = null;
		}
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}
			conn = null;
		}

	}

	public String Preparestatement_Parentheses(int NumberOfInputParameters) {
		if (NumberOfInputParameters <= 0) {
			return "()";
		}
		StringBuilder s = new StringBuilder("(?");
		while (NumberOfInputParameters > 1) {
			s.append(",?");
			NumberOfInputParameters--;
		}
		s.append(")");
		return s.toString();
	}

	public String PrepareCallInputString(int number_of_parameters, String StoreProcedureName) {
		StringBuilder prepareStatement = new StringBuilder("{call ");
		prepareStatement.append(StoreProcedureName);
		prepareStatement.append(Preparestatement_Parentheses(number_of_parameters));
		prepareStatement.append("}");
		return prepareStatement.toString();
	}

}
