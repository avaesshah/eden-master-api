package com.eden.api.util;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.edenstar.model.Lease;

public class LeaseMapper implements RowMapper<Lease> {

	@Override
	public Lease mapRow(ResultSet rs, int rowNum) throws SQLException {

		Lease l = new Lease();

		l.setLease_id(rs.getInt("lease_id"));
		l.setBooking_id(rs.getInt("booking_id"));
		l.setContract_URL(rs.getString("contract_url"));
		l.setComments(rs.getString("comments"));
		l.setContractSignedDate(rs.getDate("contract_signed_date"));
		l.setReviewFlag(rs.getInt("review_flag"));
		l.setContractUploaded(rs.getInt("contract_uploaded"));
		l.setIs_archived(rs.getInt("is_archived"));


		return l;
	}	
}
