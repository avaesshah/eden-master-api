package com.eden.api.util;


import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.edenstar.model.Customer;

public class CustomerMapper implements RowMapper<Customer> {

	@Override
	public Customer mapRow(ResultSet rs, int rowNum) throws SQLException {

		Customer custDetails = new Customer();

		custDetails.setCustomerID(rs.getInt("customer_id"));
		custDetails.setEmailIDCus(rs.getString("email_id_cus"));
		custDetails.setFirstName(rs.getString("first_name"));
		custDetails.setLastName(rs.getString("last_name"));
		custDetails.setAddress(rs.getString("address"));
		custDetails.setPoBox(rs.getString("po_box"));
		custDetails.setEmirate(rs.getString("emirate"));
		custDetails.setMobileNumber(rs.getString("mobile_number"));
		custDetails.setOfficeNumber(rs.getString("office_number"));
		custDetails.setTradeLicence(rs.getInt("trade_licence"));
		custDetails.setTradeName(rs.getString("trade_name"));
		

		return custDetails;
	}
	
} // CustomerMapper
