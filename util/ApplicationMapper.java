package com.eden.api.util;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.edenstar.model.Application;

public class ApplicationMapper implements RowMapper<Application> {

	@Override
	public Application mapRow(ResultSet rs, int rowNum) throws SQLException {

		Application a = new Application();

		a.setApplicationID(rs.getInt("application_id"));
		a.setQuoteID(rs.getInt("quote_id"));
		a.setDateOfApplication(rs.getDate("date_of_application"));
		a.setReviewFlag(rs.getInt("review_flag"));
		a.setComments(rs.getString("comments"));
		a.setIsApproved(rs.getInt("is_approved"));
		a.setManagerID(rs.getInt("manager_id"));
		a.setSalesID(rs.getInt("sales_id"));
		a.setSecurity_deposit(rs.getString("security_deposit"));
		a.setIsDeclined(rs.getInt("is_declined"));
		a.setIsExpired(rs.getInt("is_expired"));

		return a;
	}
}
