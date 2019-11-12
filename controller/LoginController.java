package com.eden.api.controller;

//import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.eden.api.service.UserService;
import com.eden.api.util.Constants;
import com.eden.api.util.Response;
import com.eden.api.util.ResponseEnum;
import com.edenstar.model.User;
import com.edenstar.model.dash.ChangePassword;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class LoginController extends BaseController {
	Response response = new Response();

	// private static Logger log = Logger.getLogger(LoginController.class);

	@Autowired
	private UserService userService;

	// **************************************************************************************
	// change password procedure
	// **************************************************************************************
	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_LOGIN
			+ Constants.PATH_CHANGE_PWORD, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response changePassword(@RequestBody ChangePassword c) {

		// check to make sure that the parameters are not empty
		if (c.getEmailID().contentEquals("") || c.getPassword().contentEquals("")
				|| c.getNewPassword().contentEquals("")) {

			response = Response.build("Error", "no username or password(s) entered", false);
			return response;
		}

		try {
			// if there is a username, let's see if it exists on the DB ...
			User result = userService.getUserDetails(c.getEmailID());

			// check to se if the username exists on database ...
			if (c.getEmailID().equals(result.getEmailID())) {

				// now check to see if passwords match ..
				if (result.getPassword().equals(c.getPassword())) {

					// now that the passwords match we must update the database with the new
					// password
					result.setPassword(c.getNewPassword().toString());

					int status = userService.updateUser(result);

					if (status == 1) {
						response = Response.build("Success", "password successfully changed", true);
						return response;
					} else {
						response = Response.build("Failure", "password not changed error", false);
						return response;
					}

				} else {
					response = Response.build("Error", "incorrect password", false);
					return response;
				}

			} else {
				response = Response.build("Error", "username does not exist on database", false);
				return response;
			}

		} catch (Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // changePassword

	// **************************************************************************************
	// email lost password
	// **************************************************************************************
	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_LOGIN
			+ Constants.PATH_RESET, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response emailPassword(@RequestBody User u) {

		// check the database to see if the username exists
		try {

			// make sure that there us values for email and password
			if (u.getEmailID() == null) {
				response = Response.build("Error", "Mandatory field [emailID] cannot be empty", false);
				return response;
			}

			// if there is a username, let's see if it exists on the DB ...
			User result = userService.getUserDetails(u.getEmailID());

			// check to see if the email record exists
			if (u.getEmailID().equals(result.getEmailID())) {

				// now that we have a valid record, we must email the password to the user
				Boolean emailResult = userService.emailUserPassword(result);
				if (emailResult) {
					response = Response.build("Success", "Lost password emailed to user", true);
				} else {

					response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(),
							Constants.INTERNAL_SYSTEM_ERROR, ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
				}

			} else {
				// username does not exist on Eden-Star database
				response = Response.build("Error", "username does not exist on Eden-Star database", false);
			} // if

		} catch (Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;
	} // emailPassword

	// **************************************************************************************
	// Login Procedure
	// **************************************************************************************
	@RequestMapping(value = Constants.BASE_PATH
			+ Constants.PATH_LOGIN, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response login(@RequestBody User u) {

		// Response response = new Response();

		// check the database to see if the username exists
		try {

			// make sure that there us values for email and password
			if (u.getEmailID().isEmpty() || u.getPassword().isEmpty()) {
				response = Response.build("Error", "no password/username entered", false);
				return response;
			}

			User result = userService.login(u.getEmailID(), u.getPassword());

			if (u.getEmailID().equals(result.getEmailID())) {

				// check passwords ...
				if (u.getPassword().equals(result.getPassword())) {
					response = Response.build(ResponseEnum.OK.getStatus(), Constants.SUCCESS,
							ResponseEnum.OK.getMessage(), true);
					response.setData(result);
				} else {
					response = Response.build("Error", "incorrect password", false);
				}
			} else {
				response = Response.build("Error", "username not found", false);
			}

		} catch (Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;
	}

}
