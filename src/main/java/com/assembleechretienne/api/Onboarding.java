package com.assembleechretienne.api;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import java.sql.Types;
import java.util.ArrayList;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import DAL.MysqlDataAccessHelper;
import DAL.MysqlParameter;
import DAL.MysqlParameter.ParameterDirection;
import Entities.UserCredential;
import HelperClasses.MailService;
import HelperClasses.PasswordHash;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;



/*THe onboarding class handle all registration related event*/
@Path("onboarding")
public class Onboarding {

	// static final Logger logger = Logger.getLogger(Onboarding.class);
	private static final Logger logger = LogManager.getLogger(Onboarding.class);
	private static JedisPool pool = new JedisPool(new JedisPoolConfig(), "localhost");
	// givien an email, thie return all the credential information about the
	// user

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{email}")
	public UserCredential getUserCredential(@PathParam("email") String email) {

		UserCredential userdata = new UserCredential();
		MysqlDataAccessHelper datahelper = MysqlDataAccessHelper.getInstance();
		ArrayList<MysqlParameter> param = new ArrayList<MysqlParameter>();
		param.add(new MysqlParameter("p_email", Types.VARCHAR, email, ParameterDirection.INPUT));
		
		
		
		ArrayList<UserCredential> user= datahelper.ExecuteStoreProcedureWithOutputParameter("GET_USER_CREDIDENTIAL", param, userdata);

		try (Jedis jedis = pool.getResource()) {
			  jedis.set(user.get(0).getEmail(), user.get(0).getUsername());
			  return user.get(0);
			}
		catch(Exception e){
			logger.error(" Error in user credential"+ e.getMessage());
		}
		
		return null;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response onboardingNewUser(UserCredential user) {
		if (user.getEmail().isEmpty() || user.getPassword().isEmpty()) {
			throw new WebApplicationException(400);
		}

		logger.info("Incoming Request to register: Username " + user.getEmail());

		String[] params;
		String salt;
		String hash;
		String email;
		String username;

		try {
			String inputparam = PasswordHash.createHash(user.getPassword());

			// Decode the hash into its parameters
			params = inputparam.split(":");
			salt = params[PasswordHash.SALT_INDEX];
			hash = params[PasswordHash.PBKDF2_INDEX];
			email = user.getEmail();
			username = user.getUsername();

			MysqlDataAccessHelper datahelper = MysqlDataAccessHelper.getInstance();
			String status = datahelper.ExecuteStoreProcedure_No_Output_Parameter("ONBOARDING_USER",
					user.setUserCredentialParam(username, email, salt, hash));

			if (status.equals("valid")) {
				String validationUrl="http://www.assembleechretienne.com/validate_email/"+hash+":"+salt;
				String body="<h1>Bienvenue Dans la communaut&#233 en ligne de l'egise assemblee chretienne. Veillez valider votre email en cliquant sur cette hyperlink <a href="+validationUrl+">"+"clickez!</a><//h1>";
				
				MailService.sendEmail(email, "S'il vous plaiz, verifiez votre email", body);

				logger.info("Incoming Request to register: Username " + user.getEmail() + " is successfull");
				return Response.ok("{\"onboarding\":\"successful\"}").build();
			}
			if (status.equals("invalid:1062")) {
				logger.info("Incoming Request to register: Username " + user.getEmail() + " who is already a member");
				return Response.ok("{\"onboarding\":\"unsuccessful\", \"message\":\"user already registered\"}")
						.build();
			}

		} catch (NoSuchAlgorithmException e) {
			logger.error("Incoming Request to register: Username " + user.getEmail() + "is unssucessful. " + "Reason :"
					+ e.getMessage());
		} catch (InvalidKeySpecException e) {
			logger.error("Incoming Request to register: Username " + user.getEmail() + "is unssucessful. " + "Reason :"
					+ e.getMessage());
		}

		catch (Exception e) {
			logger.error("Incoming Request to register: Username: " + user.getEmail() + " is unssucessful. "
					+ "Reason :" + e.getMessage());
		}

		return Response.ok("{\"onboarding\":\"unsuccessful\"}").build();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/validate")
	public Response AutenticateUser(UserCredential user) {

		
		if (user.getEmail().isEmpty() || user.getPassword().isEmpty()) {
			throw new WebApplicationException(400);
		}

		try {

			UserCredential usedata = new UserCredential();
			MysqlDataAccessHelper datahelper = MysqlDataAccessHelper.getInstance();
			ArrayList<UserCredential> data = datahelper.ExecuteStoreProcedureWithOutputParameter(
					"GET_USER_CREDIDENTIAL", usedata.getUserCredentialParam(user.getEmail()), usedata);
			if (data.isEmpty()) {
				logger.info("User: " + user.getEmail() + " autentication fail");
				return Response.ok("{\"authentication\":\"false\", \"message\":\"invalid username or password\"}")
						.build();
			}

			String hash = PasswordHash.PBKDF2_ITERATIONS + ":" + data.get(0).getPassword_salt() + ":"
					+ data.get(0).getPassword_hash();
			boolean validatePassword = PasswordHash.validatePassword(user.getPassword(), hash);

			if (validatePassword) {
				logger.info("User: " + user.getEmail() +" "+ data.get(0).getUsername()+ " autentication pass");
				return Response.ok("{\"authentication\":\"true\",\"username\":"+ "\""+ data.get(0).getUsername()+"\""+"}").build();
			} 
			
			else 
			{
				logger.info("User: " + user.getEmail() + "autentication fail");
				return Response.ok("{\"authentication\":\"false\", \"message\":\"invalid username or password\"}")
						.build();
			}
		}

		catch (NoSuchAlgorithmException e) {
			logger.error(e.getMessage());
			return Response.ok(e.getMessage()).build();
		} catch (InvalidKeySpecException e) {
			logger.error(e.getMessage());
			return Response.ok(e.getMessage()).build();
		}
	}
}
