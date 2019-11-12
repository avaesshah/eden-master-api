package com.eden.api.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import com.eden.api.util.LoginMapper;
import com.edenstar.model.User;
import com.edenstar.model.dash.CreateUser;

@Component
public class UserDAOImpl implements UserDAO {

	private JdbcTemplate jdbcTemplate;
	User result = new User();

	@Autowired
	public UserDAOImpl(JdbcTemplate _jdbcTemplate) {
		this.jdbcTemplate = _jdbcTemplate;
	}

	@Override
	public User login(String _username, String _password) throws Exception {

		// User result = new User();
		String query = "SELECT * FROM users where is_archived = 0 and email_id ='" + _username + "'";
		User r = new User();
		
		try {

			r = jdbcTemplate.queryForObject(query, new LoginMapper());

		} catch (Exception e) {

		} // try

		return r;
	} // login

	@Override
	public List<User> getAllByType(String userType) throws Exception {
		// TODO Auto-generated method stub
		String query = "SELECT * FROM users where is_archived = 0 and user_level ='" + userType + "'";
		List<User> userList = new ArrayList<User>();
		List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();

		try {

			// userList = jdbcTemplate.query(query, new BeanPropertyRowMapper(User.class));
			// cannot use the above mapper because it returns nul for employee_id and
			// emailID
			// must manually enter the details

			rows = jdbcTemplate.queryForList(query);

			for (Map<String, Object> row : rows) {

				User u = new User();
				u.setEmployeeID((Integer) (row.get("employee_id")));
				u.setEmailID((String) (row.get("email_id")));
				u.setPassword((String) (row.get("password")));
				u.setUserLevel((String) (row.get("user_level")));
				u.setPasswordHint((String) (row.get("password_hint")));
				u.setIsblocked((Integer) (row.get("isblocked")));
				u.setFirstName((String) (row.get("first_name")));
				u.setLastName((String) (row.get("last_name")));
				u.setAddress((String) (row.get("address")));
				u.setDob((String) (row.get("dob")));
				u.setPhoneNumber((String) (row.get("phone_number")));
				u.setProfileUrl((String) (row.get("profile_url")));

				userList.add(u);
			}

		} catch (Exception e) {

		} // try

		return userList;
	} // getAllByType

	@Override
	public List<User> getAllUsers() throws Exception {

		String query = "SELECT * FROM users where is_archived = 0";
		List<User> userList = new ArrayList<User>();
		List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();

		try {

			// userList = jdbcTemplate.query(query, new BeanPropertyRowMapper(User.class));
			// cannot use the above mapper because it returns nul for employee_id and
			// emailID
			// must manually enter the details

			rows = jdbcTemplate.queryForList(query);

			for (Map<String, Object> row : rows) {

				User u = new User();
				u.setEmployeeID((Integer) (row.get("employee_id")));
				u.setEmailID((String) (row.get("email_id")));
				u.setPassword((String) (row.get("password")));
				u.setUserLevel((String) (row.get("user_level")));
				u.setPasswordHint((String) (row.get("password_hint")));
				u.setIsblocked((Integer) (row.get("isblocked")));
				u.setFirstName((String) (row.get("first_name")));
				u.setLastName((String) (row.get("last_name")));
				u.setAddress((String) (row.get("address")));
				u.setDob((String) (row.get("dob")));
				u.setPhoneNumber((String) (row.get("phone_number")));
				u.setProfileUrl((String) (row.get("profile_url")));

				userList.add(u);
			}

		} catch (Exception e) {

		} // try

		return userList;

	} // getAllUsers

	@Override
	public User getUserDetails(String _username) throws Exception {

		// User result = new User();
		String query = "SELECT * FROM users where email_id ='" + _username + "'";
		User r1 = new User();
		
		try {

			r1 = jdbcTemplate.queryForObject(query, new LoginMapper());

		} catch (Exception e) {

		} // try

		return r1;
	} // getUserDetails

	@Override
	public User getUserDetailsByEmpID(int employee_id) throws Exception {

		// User result = new User();
		String query = "SELECT * FROM users where employee_id ='" + employee_id + "'";

		try {

			result = jdbcTemplate.queryForObject(query, new LoginMapper());

		} catch (Exception e) {

		} // try

		return result;
	} // getUserDetails

	@Override
	public int updateUserDetails(User u) throws Exception {

		NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
		int status = 0;

		String UPDATE_QUERY = "update users set email_id = :email_id,"
				+ " password = :password, user_level = :user_level, password_hint = :password_hint,"
				+ "isblocked = :isblocked, first_name = :first_name, last_name = :last_name,"
				+ "address = :address, dob = :dob, phone_number =:phone_number,"
				+ "profile_url = :profile_url where employee_id = :employee_id";

		try {

			// Adding params using MapSqlParameterSource class
			SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("employee_id", u.getEmployeeID())
					.addValue("email_id", u.getEmailID()).addValue("password", u.getPassword())
					.addValue("user_level", u.getUserLevel()).addValue("password_hint", u.getPasswordHint())
					.addValue("isblocked", u.getIsblocked()).addValue("first_name", u.getFirstName())
					.addValue("last_name", u.getLastName()).addValue("address", u.getAddress())
					.addValue("dob", u.getDob()).addValue("phone_number", u.getPhoneNumber())
					.addValue("profile_url", u.getProfileUrl());

			status = namedJdbcTemplate.update(UPDATE_QUERY, namedParameters);

			if (status != 0) {
				System.out.println("User data updated for ID " + u.getEmailID());
			} else {
				System.out.println("No Employee found with ID " + u.getEmailID());
			}

		} catch (Exception e) {

		} // try
		return status;

	} // updateUserDetails

	@Override
	public int addUser(User u) throws Exception {

		NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
		int status = 0;

		final String INSERT_QUERY = "insert into users (email_id, password, user_level, password_hint,"
				+ "isblocked, first_name,last_name, address, dob, phone_number, profile_url ) values (:email_id, :password, :user_level, :password_hint,"
				+ ":isblocked, :first_name,:last_name, :address, :dob, :phone_number, :profile_url)";

		try {
			// Creating map with all required params
			Map<String, Object> paramMap = new HashMap<String, Object>();

			paramMap.put("employee_id", u.getEmployeeID());
			paramMap.put("email_id", u.getEmailID().toString().toLowerCase());
			paramMap.put("password", u.getPassword());
			paramMap.put("user_level", u.getUserLevel().toString().toLowerCase());
			paramMap.put("password_hint", u.getPasswordHint());
			paramMap.put("isblocked", u.getIsblocked());
			paramMap.put("first_name", u.getFirstName());
			paramMap.put("last_name", u.getLastName());
			paramMap.put("address", u.getAddress());
			paramMap.put("dob", u.getDob());
			paramMap.put("phone_number", u.getPhoneNumber());
			paramMap.put("profile_url", u.getProfileUrl().toString().toLowerCase());

			// Passing map containing named params
			status = namedJdbcTemplate.update(INSERT_QUERY, paramMap);

		} catch (Exception e) {

		} // try

		System.out.println("add user status = " + status);
		return status;

	} // addUser

	@Override
	public int deleteUser(CreateUser u) {

		int status = 0;

		final String DELETE_QUERY = "update users set is_archived = 1 where employee_id = ?";// "delete from users where employee_id = ?";

		// define query arguments
		Object[] params = { u.getEmployeeID() };

		status = jdbcTemplate.update(DELETE_QUERY, params);
		System.out.println("delete status = " + status);
		
		return status;
	}

} // UserDAOImpl
