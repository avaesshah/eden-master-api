package com.eden.api.dao;

import java.util.List;

import com.edenstar.model.User;
import com.edenstar.model.dash.CreateUser;

public interface UserDAO {
	
	// login procedure method
	User login(String _username, String _password) throws Exception;

	// get user details for lost password
	User getUserDetails(String _username) throws Exception;
	
	// get user details by employeeid
	User getUserDetailsByEmpID(int employee_id) throws Exception;

	// update user details
	int updateUserDetails(User u) throws Exception;

	// add a new user (employee)
	int addUser(User u) throws Exception;

	// get all the list of users and their details
	List<User> getAllUsers() throws Exception;
	
	// get a list of users by type
	List<User> getAllByType(String userType) throws Exception;

	// delete a user
	int deleteUser(CreateUser u) throws Exception;

	// place extra methods into here concerning the login functionality
}
