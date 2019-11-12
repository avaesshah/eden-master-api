package com.eden.api.util;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.edenstar.model.User;

public class LoginMapper implements RowMapper<User> {

	@Override
	public User mapRow(ResultSet rs, int rowNum) throws SQLException {

		User userDetails = new User();

		userDetails.setEmployeeID(rs.getInt("employee_id"));
		userDetails.setUserLevel(rs.getString("user_level"));
		userDetails.setEmailID(rs.getString("email_id"));
		userDetails.setFirstName(rs.getString("first_name"));
		userDetails.setLastName(rs.getString("last_name"));
		userDetails.setDob(rs.getString("dob"));
		userDetails.setPhoneNumber(rs.getString("phone_number"));
		userDetails.setAddress(rs.getString("address"));
		userDetails.setPassword(rs.getString("password"));
		userDetails.setPasswordHint(rs.getString("password_hint"));
		userDetails.setIsblocked(rs.getInt("isblocked"));
		userDetails.setProfileUrl(rs.getString("profile_url"));

		return userDetails;
	}
} // LoginMapper
