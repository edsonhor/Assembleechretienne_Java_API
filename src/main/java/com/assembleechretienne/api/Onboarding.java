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
				
				String body="<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\r\n\t\t<html xmlns=\"http://www.w3.org/1999/xhtml\">\r\n\t\t<head>\r\n\t\t\t<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\r\n\t\t\t<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\r\n\t\t\t<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge,chrome=1\">\r\n\t\t\t<meta name=\"format-detection\" content=\"telephone=no\" /> <!-- disable auto telephone linking in iOS -->\r\n\t\t\t<title>Respmail is a response HTML email designed to work on all major email platforms and smartphones</title>\r\n\t\t\t<style type=\"text/css\">\r\n\t\t\t\t RESET STYLES \r\n\t\t\t\thtml { background-color:#E1E1E1; margin:0; padding:0; }\r\n\t\t\t\tbody, #bodyTable, #bodyCell, #bodyCell{height:100% !important; margin:0; padding:0; width:100% !important;font-family:Helvetica, Arial, \"Lucida Grande\", sans-serif;}\r\n\t\t\t\ttable{border-collapse:collapse;}\r\n\t\t\t\ttable[id=bodyTable] {width:100%!important;margin:auto;max-width:850px!important;color:#7A7A7A;font-weight:normal;}\r\n\t\t\t\timg, a img{border:0; outline:none; text-decoration:none;height:auto; line-height:100%;}\r\n\t\t\t\ta {text-decoration:none !important;border-bottom: 1px solid;}\r\n\t\t\t\th1, h2, h3, h4, h5, h6{color:#5F5F5F; font-weight:normal; font-family:Helvetica; font-size:20px; line-height:125%; text-align:Left; letter-spacing:normal;margin-top:0;margin-right:0;margin-bottom:10px;margin-left:0;padding-top:0;padding-bottom:0;padding-left:0;padding-right:0;}\r\n\t\t\t\t CLIENT-SPECIFIC STYLES \r\n\t\t\t\t.ReadMsgBody{width:100%;} .ExternalClass{width:100%;}  Force Hotmail/Outlook.com to display emails at full width. \r\n\t\t\t\t.ExternalClass, .ExternalClass p, .ExternalClass span, .ExternalClass font, .ExternalClass td, .ExternalClass div{line-height:100%;}  Force Hotmail/Outlook.com to display line heights normally. \r\n\t\t\t\ttable, td{mso-table-lspace:0pt; mso-table-rspace:0pt;}  Remove spacing between tables in Outlook 2007 and up. \r\n\t\t\t\t#outlook a{padding:0;}  Force Outlook 2007 and up to provide a \"view in browser\" message. \r\n\t\t\t\timg{-ms-interpolation-mode: bicubic;display:block;outline:none; text-decoration:none;}  Force IE to smoothly render resized images. \r\n\t\t\t\tbody, table, td, p, a, li, blockquote{-ms-text-size-adjust:100%; -webkit-text-size-adjust:100%; font-weight:normal!important;}  Prevent Windows- and Webkit-based mobile platforms from changing declared text sizes. \r\n\t\t\t\t.ExternalClass td[class=\"ecxflexibleContainerBox\"] h3 {padding-top: 10px !important;}  Force hotmail to push 2-grid sub headers down \r\n\t\t\t\t /\\/\\/\\/\\/\\/\\/\\/\\/ TEMPLATE STYLES /\\/\\/\\/\\/\\/\\/\\/\\/ \r\n\t\t\t\t ========== Page Styles ========== \r\n\t\t\t\th1{display:block;font-size:26px;font-style:normal;font-weight:normal;line-height:100%;}\r\n\t\t\t\th2{display:block;font-size:20px;font-style:normal;font-weight:normal;line-height:120%;}\r\n\t\t\t\th3{display:block;font-size:17px;font-style:normal;font-weight:normal;line-height:110%;}\r\n\t\t\t\th4{display:block;font-size:18px;font-style:italic;font-weight:normal;line-height:100%;}\r\n\t\t\t\t.flexibleImage{height:auto;}\r\n\t\t\t\t.linkRemoveBorder{border-bottom:0 !important;}\r\n\t\t\t\ttable[class=flexibleContainerCellDivider] {padding-bottom:0 !important;padding-top:0 !important;}\r\n\t\t\t\tbody, #bodyTable{background-color:#E1E1E1;}\r\n\t\t\t\t#emailHeader{background-color:#E1E1E1;}\r\n\t\t\t\t#emailBody{background-color:#FFFFFF;}\r\n\t\t\t\t#emailFooter{background-color:#E1E1E1;}\r\n\t\t\t\t.nestedContainer{background-color:#F8F8F8; border:1px solid #CCCCCC;}\r\n\t\t\t\t.emailButton{background-color:#205478; border-collapse:separate;}\r\n\t\t\t\t.buttonContent{color:#FFFFFF; font-family:Helvetica; font-size:18px; font-weight:bold; line-height:100%; padding:15px; text-align:center;}\r\n\t\t\t\t.buttonContent a{color:#FFFFFF; display:block; text-decoration:none!important; border:0!important;}\r\n\t\t\t\t.emailCalendar{background-color:#FFFFFF; border:1px solid #CCCCCC;}\r\n\t\t\t\t.emailCalendarMonth{background-color:#205478; color:#FFFFFF; font-family:Helvetica, Arial, sans-serif; font-size:16px; font-weight:bold; padding-top:10px; padding-bottom:10px; text-align:center;}\r\n\t\t\t\t.emailCalendarDay{color:#205478; font-family:Helvetica, Arial, sans-serif; font-size:60px; font-weight:bold; line-height:100%; padding-top:20px; padding-bottom:20px; text-align:center;}\r\n\t\t\t\t.imageContentText {margin-top: 10px;line-height:0;}\r\n\t\t\t\t.imageContentText a {line-height:0;}\r\n\t\t\t\t#invisibleIntroduction {display:none !important;}  Removing the introduction text from the view \r\n\t\t\t\tFRAMEWORK HACKS & OVERRIDES \r\n\t\t\t\tspan[class=ios-color-hack] a {color:#275100!important;text-decoration:none!important;}  Remove all link colors in IOS (below are duplicates based on the color preference) \r\n\t\t\t\tspan[class=ios-color-hack2] a {color:#205478!important;text-decoration:none!important;}\r\n\t\t\t\tspan[class=ios-color-hack3] a {color:#8B8B8B!important;text-decoration:none!important;}\r\n\t\t\t\t A nice and clean way to target phone numbers you want clickable and avoid a mobile phone from linking other numbers that look like, but are not phone numbers.  Use these two blocks of code to \"unstyle\" any numbers that may be linked.  The second block gives you a class to apply with a span tag to the numbers you would like linked and styled.\r\n\t\t\t\tInspired by Campaign Monitor's article on using phone numbers in email: http://www.campaignmonitor.com/blog/post/3571/using-phone-numbers-in-html-email/.\r\n\t\t\t\t\r\n\t\t\t\t.a[href^=\"tel\"], a[href^=\"sms\"] {text-decoration:none!important;color:#606060!important;pointer-events:none!important;cursor:default!important;}\r\n\t\t\t\t.mobile_link a[href^=\"tel\"], .mobile_link a[href^=\"sms\"] {text-decoration:none!important;color:#606060!important;pointer-events:auto!important;cursor:default!important;}\r\n\t\t\t\t MOBILE STYLES \r\n\t\t\t\t@media only screen and (max-width: 480px){\r\n\t\t\t\t\t////// CLIENT-SPECIFIC STYLES //////\r\n\t\t\t\t\tbody{width:100% !important; min-width:100% !important;}  Force iOS Mail to render the email at full width. \r\n\t\t\t\t\t FRAMEWORK STYLES \r\n\t\t\t\t\t\r\n\t\t\t\t\tCSS selectors are written in attribute\r\n\t\t\t\t\tselector format to prevent Yahoo Mail\r\n\t\t\t\t\tfrom rendering media query styles on\r\n\t\t\t\t\tdesktop.\r\n\t\t\t\t\t\r\n\t\t\t\t\ttd[class=\"textContent\"], td[class=\"flexibleContainerCell\"] { width: 100%; padding-left: 10px !important; padding-right: 10px !important; }\r\n\t\t\t\t\ttable[id=\"emailHeader\"],\r\n\t\t\t\t\ttable[id=\"emailBody\"],\r\n\t\t\t\t\ttable[id=\"emailFooter\"],\r\n\t\t\t\t\ttable[class=\"flexibleContainer\"],\r\n\t\t\t\t\ttd[class=\"flexibleContainerCell\"] {width:100% !important;}\r\n\t\t\t\t\ttd[class=\"flexibleContainerBox\"], td[class=\"flexibleContainerBox\"] table {display: block;width: 100%;text-align: left;}\r\n\t\t\t\t\t\r\n\t\t\t\t\tThe following style rule makes any\r\n\t\t\t\t\timage classed with 'flexibleImage'\r\n\t\t\t\t\tfluid when the query activates.\r\n\t\t\t\t\tMake sure you add an inline max-width\r\n\t\t\t\t\tto those images to prevent them\r\n\t\t\t\t\tfrom blowing out.\r\n\t\t\t\t\t\r\n\t\t\t\t\ttd[class=\"imageContent\"] img {height:auto !important; width:100% !important; max-width:100% !important; }\r\n\t\t\t\t\timg[class=\"flexibleImage\"]{height:auto !important; width:100% !important;max-width:100% !important;}\r\n\t\t\t\t\timg[class=\"flexibleImageSmall\"]{height:auto !important; width:auto !important;}\r\n\t\t\t\t\t\r\n\t\t\t\t\tCreate top space for every second element in a block\r\n\t\t\t\t\t\r\n\t\t\t\t\ttable[class=\"flexibleContainerBoxNext\"]{padding-top: 10px !important;}\r\n\t\t\t\t\t\r\n\t\t\t\t\tMake buttons in the email span the\r\n\t\t\t\t\tfull width of their container, allowing\r\n\t\t\t\t\tfor left- or right-handed ease of use.\r\n\t\t\t\t\t\r\n\t\t\t\t\ttable[class=\"emailButton\"]{width:100% !important;}\r\n\t\t\t\t\ttd[class=\"buttonContent\"]{padding:0 !important;}\r\n\t\t\t\t\ttd[class=\"buttonContent\"] a{padding:15px !important;}\r\n\t\t\t\t}\r\n\t\t\t\t  CONDITIONS FOR ANDROID DEVICES ONLY\r\n\t\t\t\t*   http://developer.android.com/guide/webapps/targeting.html\r\n\t\t\t\t*   http://pugetworks.com/2011/04/css-media-queries-for-targeting-different-mobile-devices/ ;\r\n\t\t\t\t=====================================================\r\n\t\t\t\t@media only screen and (-webkit-device-pixel-ratio:.75){\r\n\t\t\t\t\t Put CSS for low density (ldpi) Android layouts in here \r\n\t\t\t\t}\r\n\t\t\t\t@media only screen and (-webkit-device-pixel-ratio:1){\r\n\t\t\t\t\t Put CSS for medium density (mdpi) Android layouts in here \r\n\t\t\t\t}\r\n\t\t\t\t@media only screen and (-webkit-device-pixel-ratio:1.5){\r\n\t\t\t\t\t Put CSS for high density (hdpi) Android layouts in here \r\n\t\t\t\t}\r\n\t\t\t\t end Android targeting \r\n\t\t\t\t CONDITIONS FOR IOS DEVICES ONLY\r\n\t\t\t\t=====================================================\r\n\t\t\t\t@media only screen and (min-device-width : 320px) and (max-device-width:568px) {\r\n\t\t\t\t}\r\n\t\t\t\t end IOS targeting \r\n\t\t\t</style>\r\n\t\t\t<!--\r\n\t\t\t\tOutlook Conditional CSS\r\n\t\t\t\tThese two style blocks target Outlook 2007 & 2010 specifically, forcing\r\n\t\t\t\tcolumns into a single vertical stack as on mobile clients. This is\r\n\t\t\t\tprimarily done to avoid the 'page break bug' and is optional.\r\n\t\t\t\tMore information here:\r\n\t\t\t\thttp://templates.mailchimp.com/development/css/outlook-conditional-css\r\n\t\t\t-->\r\n\t\t\t<!--[if mso 12]>\r\n\t\t\t\t<style type=\"text/css\">\r\n\t\t\t\t\t.flexibleContainer{display:block !important; width:100% !important;}\r\n\t\t\t\t</style>\r\n\t\t\t<![endif]-->\r\n\t\t\t<!--[if mso 14]>\r\n\t\t\t\t<style type=\"text/css\">\r\n\t\t\t\t\t.flexibleContainer{display:block !important; width:100% !important;}\r\n\t\t\t\t</style>\r\n\t\t\t<![endif]-->\r\n\t\t</head>\r\n\t\t<body bgcolor=\"#E1E1E1\" leftmargin=\"0\" marginwidth=\"0\" topmargin=\"0\" marginheight=\"0\" offset=\"0\">\r\n\r\n\t\t\t<!-- CENTER THE EMAIL // -->\r\n\t\t\t<!--\r\n\t\t\t1.  The center tag should normally put all the\r\n\t\t\t\tcontent in the middle of the email page.\r\n\t\t\t\tI added \"table-layout: fixed;\" style to force\r\n\t\t\t\tyahoomail which by default put the content left.\r\n\t\t\t2.  For hotmail and yahoomail, the contents of\r\n\t\t\t\tthe email starts from this center, so we try to\r\n\t\t\t\tapply necessary styling e.g. background-color.\r\n\t\t\t-->\r\n\t\t\t<center style=\"background-color:#E1E1E1;\">\r\n\t\t\t\t<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" height=\"100%\" width=\"100%\" id=\"bodyTable\" style=\"table-layout: fixed;max-width:100% !important;width: 100% !important;min-width: 100% !important;\">\r\n\t\t\t\t\t<tr>\r\n\t\t\t\t\t\t<td align=\"center\" valign=\"top\" id=\"bodyCell\">\r\n\r\n\t\t\t\t\t\t\t<!-- EMAIL HEADER // -->\r\n\t\t\t\t\t\t\t<!--\r\n\t\t\t\t\t\t\t\tThe table \"emailBody\" is the email's container.\r\n\t\t\t\t\t\t\t\tIts width can be set to 100% for a color band\r\n\t\t\t\t\t\t\t\tthat spans the width of the page.\r\n\t\t\t\t\t\t\t-->\r\n\t\t\t\t\t\t\t<table bgcolor=\"#E1E1E1\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"850\" id=\"emailHeader\">\r\n\r\n\t\t\t\t\t\t\t\t<!-- HEADER ROW // -->\r\n\t\t\t\t\t\t\t\t<tr>\r\n\t\t\t\t\t\t\t\t\t<td align=\"center\" valign=\"top\">\r\n\t\t\t\t\t\t\t\t\t\t<!-- CENTERING TABLE // -->\r\n\t\t\t\t\t\t\t\t\t\t<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">\r\n\t\t\t\t\t\t\t\t\t\t\t<tr>\r\n\t\t\t\t\t\t\t\t\t\t\t\t<td align=\"center\" valign=\"top\">\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t<!-- FLEXIBLE CONTAINER // -->\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t<table border=\"0\" cellpadding=\"10\" cellspacing=\"0\" width=\"850\" class=\"flexibleContainer\">\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t<tr>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<td valign=\"top\" width=\"850\" class=\"flexibleContainerCell\">\r\n\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<!-- CONTENT TABLE // -->\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<table align=\"left\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<tr>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<!--\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\tThe \"invisibleIntroduction\" is the text used for short preview\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\tof the email before the user opens it (50 characters max). Sometimes,\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\tyou do not want to show this message depending on your design but this\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\ttext is highly recommended.\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\tYou do not have to worry if it is hidden, the next <td> will automatically\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\tcenter and apply to the width 100% and also shrink to 50% if the first <td>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\tis visible.\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t-->\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<td align=\"left\" valign=\"middle\" id=\"invisibleIntroduction\" class=\"flexibleContainerBox\" style=\"display:none !important; mso-hide:all;\">\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"max-width:100%;\">\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</table>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</td>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<td align=\"right\" valign=\"middle\" class=\"flexibleContainerBox\">\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"max-width:100%;\">\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<tr>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<td align=\"left\" class=\"textContent\">\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<!-- CONTENT // -->\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</td>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</tr>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</table>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</td>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</tr>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</table>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</td>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t</tr>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t</table>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t<!-- // FLEXIBLE CONTAINER -->\r\n\t\t\t\t\t\t\t\t\t\t\t\t</td>\r\n\t\t\t\t\t\t\t\t\t\t\t</tr>\r\n\t\t\t\t\t\t\t\t\t\t</table>\r\n\t\t\t\t\t\t\t\t\t\t<!-- // CENTERING TABLE -->\r\n\t\t\t\t\t\t\t\t\t</td>\r\n\t\t\t\t\t\t\t\t</tr>\r\n\t\t\t\t\t\t\t\t<!-- // END -->\r\n\r\n\t\t\t\t\t\t\t</table>\r\n\t\t\t\t\t\t\t<!-- // END -->\r\n\r\n\t\t\t\t\t\t\t<!-- EMAIL BODY // -->\r\n\t\t\t\t\t\t\t<!--\r\n\t\t\t\t\t\t\t\tThe table \"emailBody\" is the email's container.\r\n\t\t\t\t\t\t\t\tIts width can be set to 100% for a color band\r\n\t\t\t\t\t\t\t\tthat spans the width of the page.\r\n\t\t\t\t\t\t\t-->\r\n\t\t\t\t\t\t\t<table bgcolor=\"#FFFFFF\"  border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"850\" id=\"emailBody\">\r\n\r\n\t\t\t\t\t\t\t\t<!-- MODULE ROW // -->\r\n\t\t\t\t\t\t\t\t<!--\r\n\t\t\t\t\t\t\t\t\tTo move or duplicate any of the design patterns\r\n\t\t\t\t\t\t\t\t\tin this email, simply move or copy the entire\r\n\t\t\t\t\t\t\t\t\tMODULE ROW section for each content block.\r\n\t\t\t\t\t\t\t\t-->\r\n\t\t\t\t\t\t\t\t<tr>\r\n\t\t\t\t\t\t\t\t\t<td align=\"center\" valign=\"top\">\r\n\t\t\t\t\t\t\t\t\t\t<!-- CENTERING TABLE // -->\r\n\t\t\t\t\t\t\t\t\t\t<!--\r\n\t\t\t\t\t\t\t\t\t\t\tThe centering table keeps the content\r\n\t\t\t\t\t\t\t\t\t\t\ttables centered in the emailBody table,\r\n\t\t\t\t\t\t\t\t\t\t\tin case its width is set to 100%.\r\n\t\t\t\t\t\t\t\t\t\t-->\r\n\t\t\t\t\t\t\t\t\t\t<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"color:#FFFFFF;\" bgcolor=\"#F96302\">\r\n\t\t\t\t\t\t\t\t\t\t\t<tr>\r\n\t\t\t\t\t\t\t\t\t\t\t\t<td align=\"center\" valign=\"top\">\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t<!-- FLEXIBLE CONTAINER // -->\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t<!--\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\tThe flexible container has a set width\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\tthat gets overridden by the media query.\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\tMost content tables within can then be\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\tgiven 100% widths.\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t-->\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"850\" class=\"flexibleContainer\">\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t<tr>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<td align=\"center\" valign=\"top\" width=\"850\" class=\"flexibleContainerCell\">\r\n\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<!-- CONTENT TABLE // -->\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<!--\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\tThe content table is the first element\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\tthat's entirely separate from the structural\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\tframework of the email.\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t-->\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<table border=\"0\" cellpadding=\"30\" cellspacing=\"0\" width=\"100%\">\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<tr>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<td align=\"center\" valign=\"top\" class=\"textContent\">\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<h1 style=\"color:#FFFFFF;line-height:100%;font-family:Helvetica,Arial,sans-serif;font-size:35px;font-weight:normal;margin-bottom:5px;text-align:center;\">Bienvenue A Assemblee Chretienne Par La Foi</h1>\r\n\t\t\t\t\t\t\t\t\t\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</td>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</tr>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</table>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<!-- // CONTENT TABLE -->\r\n\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</td>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t</tr>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t</table>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t<!-- // FLEXIBLE CONTAINER -->\r\n\t\t\t\t\t\t\t\t\t\t\t\t</td>\r\n\t\t\t\t\t\t\t\t\t\t\t</tr>\r\n\t\t\t\t\t\t\t\t\t\t</table>\r\n\t\t\t\t\t\t\t\t\t\t<!-- // CENTERING TABLE -->\r\n\t\t\t\t\t\t\t\t\t</td>\r\n\t\t\t\t\t\t\t\t</tr>\r\n\t\t\t\t\t\t\t\t<!-- // MODULE ROW -->\r\n\t\t\t\t\t\t\t\t\r\n\t\t\t\t\t\t\r\n\r\n\r\n\t\t\t\t\t\t\t\t<!-- MODULE ROW // -->\r\n\t\t\t\t\t\t\t\t<!--  The \"mc:hideable\" is a feature for MailChimp which allows\r\n\t\t\t\t\t\t\t\t\tyou to disable certain row. It works perfectly for our row structure.\r\n\t\t\t\t\t\t\t\t\thttp://kb.mailchimp.com/article/template-language-creating-editable-content-areas/\r\n\t\t\t\t\t\t\t\t-->\r\n\t\t\t\t\t\t\t\t<tr mc:hideable>\r\n\t\t\t\t\t\t\t\t\t<td align=\"center\" valign=\"top\">\r\n\t\t\t\t\t\t\t\t\t\t<!-- CENTERING TABLE // -->\r\n\t\t\t\t\t\t\t\t\t\t<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">\r\n\t\t\t\t\t\t\t\t\t\t\t<tr>\r\n\t\t\t\t\t\t\t\t\t\t\t\t<td align=\"center\" valign=\"top\">\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t<!-- FLEXIBLE CONTAINER // -->\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t<table border=\"0\" cellpadding=\"30\" cellspacing=\"0\" width=\"850\" class=\"flexibleContainer\">\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t<tr>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<td valign=\"top\" width=\"850\" class=\"flexibleContainerCell\">\r\n\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<!-- CONTENT TABLE // -->\r\n\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<!-- // CONTENT TABLE -->\r\n\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</td>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t</tr>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t</table>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t<!-- // FLEXIBLE CONTAINER -->\r\n\t\t\t\t\t\t\t\t\t\t\t\t</td>\r\n\t\t\t\t\t\t\t\t\t\t\t</tr>\r\n\t\t\t\t\t\t\t\t\t\t</table>\r\n\t\t\t\t\t\t\t\t\t\t<!-- // CENTERING TABLE -->\r\n\t\t\t\t\t\t\t\t\t</td>\r\n\t\t\t\t\t\t\t\t</tr>\r\n\t\t\t\t\t\t\t\t<!-- // MODULE ROW -->\r\n\t\t\t\t\t\t\t\t<!-- MODULE ROW // -->\r\n\t\t\t\t\t\t\t\t<tr>\r\n\t\t\t\t\t\t\t\t\t<td align=\"center\" valign=\"top\">\r\n\t\t\t\t\t\t\t\t\t\t<!-- CENTERING TABLE // -->\r\n\t\t\t\t\t\t\t\t\t\t<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">\r\n\t\t\t\t\t\t\t\t\t\t\t<tr>\r\n\t\t\t\t\t\t\t\t\t\t\t\t<td align=\"center\" valign=\"top\">\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t<!-- FLEXIBLE CONTAINER // -->\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"850\" class=\"flexibleContainer\">\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t<tr>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<td align=\"center\" valign=\"top\" width=\"850\" class=\"flexibleContainerCell\">\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<table border=\"0\" cellpadding=\"30\" cellspacing=\"0\" width=\"100%\">\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<tr>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<td align=\"center\" valign=\"top\">\r\n\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<!-- CONTENT TABLE // -->\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<tr>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<td valign=\"top\" class=\"textContent\">\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<h3 style=\"color:#5F5F5F;line-height:125%;font-family:Helvetica,Arial,sans-serif;font-size:20px;font-weight:normal;margin-top:0;margin-bottom:3px;text-align:left;\">Hello "+username+"</h3>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<div style=\"text-align:left;font-family:Helvetica,Arial,sans-serif;font-size:15px;margin-bottom:0;margin-top:3px;color:#5F5F5F;line-height:135%;\">Merci D'avoir cree un account, Clicker sur Verifiez pour valider votre account.</div>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</td>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</tr>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</table>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<!-- // CONTENT TABLE -->\r\n\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</td>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</tr>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</table>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</td>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t</tr>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t</table>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t<!-- // FLEXIBLE CONTAINER -->\r\n\t\t\t\t\t\t\t\t\t\t\t\t</td>\r\n\t\t\t\t\t\t\t\t\t\t\t</tr>\r\n\t\t\t\t\t\t\t\t\t\t</table>\r\n\t\t\t\t\t\t\t\t\t\t<!-- // CENTERING TABLE -->\r\n\t\t\t\t\t\t\t\t\t</td>\r\n\t\t\t\t\t\t\t\t</tr>\r\n\t\t\t\t\t\t\t\t<!-- // MODULE ROW -->\r\n\t\t\t\t\t\t\t\t\r\n\t\t\t\t\t\t\t\t<!-- MODULE ROW // -->\r\n\t\t\t\t\t\t\t\t<tr>\r\n\t\t\t\t\t\t\t\t\t<td align=\"center\" valign=\"top\">\r\n\t\t\t\t\t\t\t\t\t\t<!-- CENTERING TABLE // -->\r\n\t\t\t\t\t\t\t\t\t\t<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">\r\n\t\t\t\t\t\t\t\t\t\t\t<tr style=\"padding-top:0;\">\r\n\t\t\t\t\t\t\t\t\t\t\t\t<td align=\"center\" valign=\"top\">\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t<!-- FLEXIBLE CONTAINER // -->\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t<table border=\"0\" cellpadding=\"30\" cellspacing=\"0\" width=\"850\" class=\"flexibleContainer\">\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t<tr>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<td style=\"padding-top:0;\" align=\"center\" valign=\"top\" width=\"850\" class=\"flexibleContainerCell\">\r\n\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<!-- CONTENT TABLE // -->\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"50%\" class=\"emailButton\" style=\"background-color: #F96302;\">\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<tr>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<td align=\"center\" valign=\"middle\" class=\"buttonContent\" style=\"padding-top:15px;padding-bottom:15px;padding-right:15px;padding-left:15px;\">\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<a style=\"color:#FFFFFF;text-decoration:none;font-family:Helvetica,Arial,sans-serif;font-size:20px;line-height:135%;\" href=\""+validationUrl+"\" target=\"_blank\">Verifiez</a>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</td>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</tr>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</table>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<!-- // CONTENT TABLE -->\r\n\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</td>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t</tr>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t</table>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t<!-- // FLEXIBLE CONTAINER -->\r\n\t\t\t\t\t\t\t\t\t\t\t\t</td>\r\n\t\t\t\t\t\t\t\t\t\t\t</tr>\r\n\t\t\t\t\t\t\t\t\t\t</table>\r\n\t\t\t\t\t\t\t\t\t\t<!-- // CENTERING TABLE -->\r\n\t\t\t\t\t\t\t\t\t</td>\r\n\t\t\t\t\t\t\t\t</tr>\r\n\t\t\t\t\t\t\t\t<!-- // MODULE ROW -->\r\n\t\t\t\t\t\t\t\t<!-- MODULE ROW // -->\r\n\t\t\t\t\t\t\t\t<tr>\r\n\t\t\t\t\t\t\t\t\t<td align=\"center\" valign=\"top\">\r\n\t\t\t\t\t\t\t\t\t\t<!-- CENTERING TABLE // -->\r\n\t\t\t\t\t\t\t\t\t\t<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">\r\n\t\t\t\t\t\t\t\t\t\t\t<tr>\r\n\t\t\t\t\t\t\t\t\t\t\t\t<td align=\"center\" valign=\"top\">\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t<!-- FLEXIBLE CONTAINER // -->\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"850\" class=\"flexibleContainer\">\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t<tr>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<td align=\"center\" valign=\"top\" width=\"850\" class=\"flexibleContainerCell\">\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<table border=\"0\" cellpadding=\"30\" cellspacing=\"0\" width=\"100%\">\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<tr>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<td align=\"center\" valign=\"top\">\r\n\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<!-- CONTENT TABLE // -->\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<tr>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<td valign=\"top\" class=\"textContent\">\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\r\n\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</td>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</tr>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</table>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<!-- // CONTENT TABLE -->\r\n\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</td>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</tr>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</table>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</td>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t</tr>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t</table>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t<!-- // FLEXIBLE CONTAINER -->\r\n\t\t\t\t\t\t\t\t\t\t\t\t</td>\r\n\t\t\t\t\t\t\t\t\t\t\t</tr>\r\n\t\t\t\t\t\t\t\t\t\t</table>\r\n\t\t\t\t\t\t\t\t\t\t<!-- // CENTERING TABLE -->\r\n\t\t\t\t\t\t\t\t\t</td>\r\n\t\t\t\t\t\t\t\t</tr>\r\n\t\t\t\t\t\t\t\t<!-- // MODULE ROW -->\r\n\r\n\t\t\t\t\t\t\t</table>\r\n\t\t\t\t\t\t\t<!-- // END -->\r\n\r\n\t\t\t\t\t\t\t<!-- EMAIL FOOTER // -->\r\n\t\t\t\t\t\t\t<!--\r\n\t\t\t\t\t\t\t\tThe table \"emailBody\" is the email's container.\r\n\t\t\t\t\t\t\t\tIts width can be set to 100% for a color band\r\n\t\t\t\t\t\t\t\tthat spans the width of the page.\r\n\t\t\t\t\t\t\t-->\r\n\t\t\t\t\t\t\t<table bgcolor=\"#E1E1E1\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"850\" id=\"emailFooter\">\r\n\r\n\t\t\t\t\t\t\t\t<!-- FOOTER ROW // -->\r\n\t\t\t\t\t\t\t\t<!--\r\n\t\t\t\t\t\t\t\t\tTo move or duplicate any of the design patterns\r\n\t\t\t\t\t\t\t\t\tin this email, simply move or copy the entire\r\n\t\t\t\t\t\t\t\t\tMODULE ROW section for each content block.\r\n\t\t\t\t\t\t\t\t-->\r\n\t\t\t\t\t\t\t\t<tr>\r\n\t\t\t\t\t\t\t\t\t<td align=\"center\" valign=\"top\">\r\n\t\t\t\t\t\t\t\t\t\t<!-- CENTERING TABLE // -->\r\n\t\t\t\t\t\t\t\t\t\t<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">\r\n\t\t\t\t\t\t\t\t\t\t\t<tr>\r\n\t\t\t\t\t\t\t\t\t\t\t\t<td align=\"center\" valign=\"top\">\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t<!-- FLEXIBLE CONTAINER // -->\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"850\" class=\"flexibleContainer\">\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t<tr>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<td align=\"center\" valign=\"top\" width=\"850\" class=\"flexibleContainerCell\">\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<table border=\"0\" cellpadding=\"30\" cellspacing=\"0\" width=\"100%\">\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<tr>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<td valign=\"top\" bgcolor=\"#E1E1E1\">\r\n\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<div style=\"font-family:Helvetica,Arial,sans-serif;font-size:13px;color:#828282;text-align:center;line-height:120%;\">\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</div>\r\n\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</td>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</tr>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</table>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</td>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t</tr>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t</table>\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t<!-- // FLEXIBLE CONTAINER -->\r\n\t\t\t\t\t\t\t\t\t\t\t\t</td>\r\n\t\t\t\t\t\t\t\t\t\t\t</tr>\r\n\t\t\t\t\t\t\t\t\t\t</table>\r\n\t\t\t\t\t\t\t\t\t\t<!-- // CENTERING TABLE -->\r\n\t\t\t\t\t\t\t\t\t</td>\r\n\t\t\t\t\t\t</tr>\r\n\r\n\t\t\t\t\t\t\t</table>\r\n\t\t\t\t\t\t\t<!-- // END -->\r\n\r\n\t\t\t\t\t\t</td>\r\n\t\t\t\t\t</tr>\r\n\t\t\t\t</table>\r\n\t\t\t</center>\r\n\t\t</body>\r\n\t</html> \r\n";
				
				MailService.sendEmail(email, "S'il vous plaiz, verifiez votre email", body);

				logger.info("Incoming Request to register: Username " + user.getEmail() + " is successfull");
				return Response.ok("{\"onboarding\":\"successful\", \"username\":\""+username+"\"}").build();
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
