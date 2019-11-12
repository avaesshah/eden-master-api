package com.eden.api.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.eden.api.service.EngineService;
import com.eden.api.service.UserService;
import com.eden.api.service.validation.UserValidation;
import com.eden.api.util.Constants;
import com.eden.api.util.Response;
import com.eden.api.util.ResponseEnum;
import com.edenstar.model.User;
import com.edenstar.model.dash.CreateUser;
import com.edenstar.model.dash.GetUser;

// this controller will handle all the incoming database user-related admin tasks

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class DashboardController extends BaseController {

	Response response = new Response();

	@Autowired
	private UserService userService;

	@Autowired
	private EngineService cpu;

	UserValidation userValid = new UserValidation();

	public CreateUser checkChanges(CreateUser u) {

		// gets the user record from database and compares
		// to see if the new data is null, and if so copy the database value over

		// get the record
		User r = new User();

		try {
			r = userService.getUserDetails(u.getEmailID());

			if (u.getAddress() == null)
				u.setAddress(r.getAddress().toString());
			u.setEmployeeID(r.getEmployeeID()); // this value cannot be changed
			if (u.getPassword() == null)
				u.setPassword(r.getPassword().toString());
			if (u.getUserLevel() == null)
				u.setUserLevel(r.getUserLevel().toString());
			if (u.getPasswordHint() == null)
				u.setPasswordHint(r.getPasswordHint().toString());
			u.setIsblocked(r.getIsblocked()); // this value cannot be changed in the dash
			if (u.getFirstName() == null)
				u.setFirstName(r.getFirstName().toString());
			if (u.getLastName() == null)
				u.setLastName(r.getLastName().toString());
			if (u.getDob() == null)
				u.setDob(r.getDob().toString());
			if (u.getPhoneNumber() == null)
				u.setPhoneNumber(r.getPhoneNumber().toString());
			if (u.getProfileUrl() == null)
				u.setProfileUrl(r.getProfileUrl().toString());

		} catch (Exception e) {
			e.printStackTrace();
		} // try-catch

		return u;
	} // checkChanges

	// **************************************************************************************
	// add a user to the database
	// **************************************************************************************
	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_DASH
			+ Constants.PATH_ADD_USER, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response addUser(@RequestBody CreateUser u) {

		try {

			// make sure none of the mandatory fields are null
			if (userValid.isNull(u) == true) {
				response = Response.build("Error",
						"Mandatory parameters : [emailID / password / address / firstName / lastName /"
								+ " dob / phoneNumber / staff_email] cannot be null",
						false);
				return response;
			} // isNull check

			// check to see if any of the mandatory fields are empty
			if (userValid.fieldsAreEmpty(u) == true) {
				response = Response.build("Error",
						"No data entered for mandatory parameters, either [staff_email / emailID / password / address / firstName / lastName /"
								+ " dob / phoneNumber",
						false);
				return response;
			} // isEmpty check

			// check to see if the staff user exists on the database
			if (!cpu.userExists(u.getStaff_email())) {
				response = Response.build("Error", "staff email id " + u.getStaff_email() + " does not exist", false);
				return response;
			}

			// check the userLevel field to make sure it contains one of the following roles
			// : admin, manager_admin, sales, accounts, manager
			if (userValid.userLevelOK(u) == false) {
				response = Response.build("Error",
						"Field [userLevel] can only have value [manager / admin / sales / accounts / manager_admin]",
						false);
				return response;
			}

			// format the fields to standard
			u = userValid.formatFields(u);

			// check if the staff user has clearance to perform this action
			if (cpu.checkClearance(u.getStaff_email().toString()) == false) {

				// insufficient priviledges
				response = Response.build("Access Denied",
						"Insufficient privileges - you must be either be admin or manager_admin to perform this action",
						false);
				return response;

			} // clearance check

			// check to see if the new username exists on database ...
			if (!cpu.userExists(u.getEmailID())) {

				// if the user does not exist, add it !

				int status = userService.addUser(u);

				if (status == 1) {
					response = Response.build("Success", "user successfully added to the database", true);
					return response;
				} else {
					response = Response.build("Failure", "user could not be added to the database", false);
					return response;
				} // nested if

			} else {
				response = Response.build("Error", "user already exists on database", false);
				return response;
			}

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // addUser

	// **************************************************************************************
	// update a user to the database
	// **************************************************************************************
	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_DASH
			+ Constants.PATH_UPDATE_USER, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response updateUser(@RequestBody CreateUser u) {

		try {

			// check to see if the staff user exists on the database
			if (!cpu.userExists(u.getStaff_email())) {
				response = Response.build("Error", "staff email id " + u.getStaff_email() + " does not exist", false);
				return response;
			}

			// check if the staff user has clearance to perform this action
			if (cpu.checkClearance(u.getStaff_email().toString()) == false) {

				// insufficient priviledges
				response = Response.build("Access Denied",
						"insufficient privileges - you must be either be admin or manager_admin to perform this action",
						false);
				return response;

			} // clearance check

			// make sure that the username exists on database ...
			if (cpu.userExists(u.getEmailID())) {

				// the username exists, we just update it with the amended details
				// first we must make sure that any null fields are replaced with database
				// values
				u = checkChanges(u);

				// format the fields to standard
				u = userValid.formatFields(u);

				int status = userService.updateUser(u);

				if (status == 1) {
					response = Response.build("Success", "user details successfully updated to the database", true);
					return response;
				} else {
					response = Response.build("Failure", "user details could not be updated to the database", false);
					return response;
				} // nested if

			} else {
				// if the username does not exist, throw an error
				response = Response.build("Error", "user does not exist on the database", false);
				return response;

			} // if

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // updateUser

	// **************************************************************************************
	// get a user or all users or users by department from the database
	// **************************************************************************************
	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_DASH
			+ Constants.PATH_GET_USER, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response getUser(@RequestBody GetUser u) {

		List<User> userList = new ArrayList<>();
		User user = new User();

		try {

			// make sure that the staff_email and emailID are not null
			if (u.getStaff_email() == null || u.getEmailID() == null) {
				response = Response.build("Error", "Mandatory fields : [emailID / staff_email] cannot be null", false);
				return response;
			} // null fields

			// check to see if the fields are empty
			if (u.getStaff_email().contentEquals("") || u.getEmailID().contentEquals("")) {
				response = Response.build("Error", "Mandatory fields : [emailID / staff_email] cannot be empty", false);
				return response;
			} // empty fields

			// check to see if the staff user exists on the database
			if (!cpu.userExists(u.getStaff_email())) {
				response = Response.build("Error",
						"Staff email " + u.getStaff_email() + " does not exist on the database", false);
				return response;
			}

			// check if the staff user has clearance to perform this action
			if (cpu.checkClearance(u.getStaff_email()) == false) {

				// insufficient priviledges
				response = Response.build("Access Denied",
						"Insufficient privileges - you must be either be admin or manager_admin to perform this action",
						false);
				return response;

			} // clearance check

			// now we see if the request is for all the user or just one user
			// emailID = "*" means all the users and emailID = "somone@mail.com" for
			// specific user
			if (u.getEmailID().contentEquals("*")) {

				// get all the users ...

				userList = userService.getAllUsers();

				response = Response.build("Success", userList.size() + " users details successfully retrieved from the database", true);
				response.setData(userList);

			} else if (u.getEmailID().contentEquals("manager") || u.getEmailID().contentEquals("manager_admin")
					|| u.getEmailID().contentEquals("sales") || u.getEmailID().contentEquals("accounts")
					|| u.getEmailID().contentEquals("admin")) {

				// get a list of all managers / sales / admin / manager_admin / accounts as
				// requested

				userList = userService.getAllByType(u.getEmailID().toString().toLowerCase());

				response = Response.build(ResponseEnum.OK.getStatus(), Constants.SUCCESS, ResponseEnum.OK.getMessage(),
						true);
				response = Response.build("Success", userList.size() + " users of type " + u.getEmailID().toString().toLowerCase() + " successfully retrieved from the database", true);
				response.setData(userList);

			} else {
				// get requested user details

				user = userService.getUserDetails(u.getEmailID());

				// see if the user email exists on database
				if (user.getEmployeeID() > 0) {

					response = Response.build("Success", "User details for email ID " +  user.getEmailID()+ " successfully retrieved from the database", true);
					response.setData(user);

				} else {
					// user does not exist
					response = Response.build("Error", "User email ID does not exist on the database", false);
					return response;
				} // nested if

			} // outer if statement

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;
	} // getUser

	// **************************************************************************************
	// DELETE a user from the database
	// **************************************************************************************
	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_DASH
			+ Constants.PATH_DELETE_USER, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response deleteUser(@RequestBody CreateUser u) {

		try {

			// make sure that the staff_email and emailID are not null
			if (u.getStaff_email() == null || u.getEmailID() == null) {
				response = Response.build("Error", "Mandatory fields : [emailID /staff_email] cannot be null", false);
				return response;
			} // null fields

			// check to see if the fields are empty
			if (u.getStaff_email().contentEquals("") || u.getEmailID().contentEquals("")) {
				response = Response.build("Error", "Mandatory fields : [emailID /staff_email] cannot be empty", false);
				return response;
			} // empty fields

			// check to see if the staff user exists on the database
			if (!cpu.userExists(u.getStaff_email())) {
				response = Response.build("Error", "Staff email id " + u.getStaff_email() + " does not exist", false);
				return response;
			}

			// check if the staff user has clearance to perform this action
			if (cpu.checkClearance(u.getStaff_email().toString()) == false) {

				// insufficient priviledges
				response = Response.build("Access Denied",
						"Insufficient privileges - you must be either be admin or manager_admin to perform this action",
						false);
				return response;

			} // clearance check

			// check to see if the username exists on database ...
			if (cpu.userExists(u.getEmailID())) {

				// if the username exists, get the employee id and send record for deletion
				User r = userService.getUserDetails(u.getEmailID());
				u.setEmployeeID(r.getEmployeeID());

				// delete record
				int status = userService.deleteUser(u);

				if (status == 1) {
					response = Response.build("Success", "User successfully deleted from the database", true);
					return response;
				} else {
					response = Response.build("Failure", "User details could not be deleted from the database", false);
					return response;
				} // nested if

			} else {
				// if the username does not exist, throw an error
				response = Response.build("Error", "User does not exist on the database", false);
				return response;

			} // outer if statement

		} catch (

		Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // try-catch

		return response;

	} // deleteUser

} // DashboardController