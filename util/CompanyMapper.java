package com.eden.api.util;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.edenstar.model.Company;

public class CompanyMapper implements RowMapper<Company> {

	@Override
	public Company mapRow(ResultSet rs, int rowNum) throws SQLException {

		Company c = new Company();

		c.setCompanyID(rs.getInt("company_id"));
		c.setCompanyName(rs.getString("company_name"));
		c.setCompanyLogo(rs.getBytes("company_logo"));
		c.setCustomerID(rs.getInt("customer_id"));

		return c;
	}
	
}
