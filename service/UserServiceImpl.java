package com.eden.api.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eden.api.service.EmailService;
import com.eden.api.dao.UserDAO;
import com.edenstar.model.User;
import com.edenstar.model.dash.CreateUser;

@Service("userService")
public class UserServiceImpl implements UserService {

	@Autowired
	private UserDAO userDAO;

	@Autowired
	private EmailService emailService;

	@Override
	public User login(String _username, String _password) throws Exception {
		return userDAO.login(_username, _password);
	}

	@Override
	public User getUserDetails(String _username) throws Exception {
		return userDAO.getUserDetails(_username);
	}

	@Override
	public Boolean emailUserPassword(User u) throws Exception {
		return emailService.emailLostPassword(u);
	}

	@Override
	public int updateUser(User u) throws Exception {
		return userDAO.updateUserDetails(u);
	}

	@Override
	public int addUser(User u) throws Exception {
		return userDAO.addUser(u);
	}

	@Override
	public User getUserDetailsByEmpID(int i) throws Exception {
		return userDAO.getUserDetailsByEmpID(i);
	}

	@Override
	public List<User> getAllUsers() throws Exception {
		// TODO Auto-generated method stub
		return userDAO.getAllUsers();
	}

	@Override
	public List<User> getAllByType(String userType) throws Exception {
		// TODO Auto-generated method stub
		return userDAO.getAllByType(userType);
	}

	@Override
	public int deleteUser(CreateUser u) throws Exception {
		// TODO Auto-generated method stub
		return userDAO.deleteUser(u);
	}

}
