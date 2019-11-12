package com.eden.api.service;

import java.util.List;

import com.edenstar.model.User;
import com.edenstar.model.dash.CreateUser;

public interface UserService {
	
	// login method
	User login(String _username, String _password) throws Exception;
	
	// get the user details for the lost password
	User getUserDetails(String _username) throws Exception;
	
	// get user details by employee id
	User getUserDetailsByEmpID(int i) throws Exception;
	
	// get all users
	List<User> getAllUsers() throws Exception;
	
	// email lost password method
	Boolean emailUserPassword(User u) throws Exception;
	
	// updates user details such as password etc
	int updateUser(User u) throws Exception;

	// adds a new user to the database
	int addUser(User u) throws Exception;

	// get a list of users by type
	List<User> getAllByType(String userType) throws Exception;

	// delete user
	int deleteUser(CreateUser u) throws Exception;

}
