package com.eden.api.util;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.edenstar.model.Quote;

public class QuoteMapper implements RowMapper<Quote> {

	@Override
	public Quote mapRow(ResultSet rs, int rowNum) throws SQLException {

		Quote q = new Quote();

		q.setQuote_ID(rs.getInt("quote_id"));
		q.setQuoteRef(rs.getString("quote_ref"));
		q.setCustomer_ID(rs.getInt("customer_id"));
		q.setKiosk_ID(rs.getInt("kiosk_id"));
		q.setEmployee_ID(rs.getInt("employee_id"));
		q.setDate_of_quote(rs.getDate("date_of_quote"));
		q.setLease_start_date(rs.getDate("lease_start_date"));
		q.setLease_end_date(rs.getDate("lease_end_date"));
		q.setLease_duration_days(rs.getInt("lease_duration_days"));
		q.setLease_total(rs.getDouble("lease_total"));
		q.setRate(rs.getDouble("rate"));
		q.setExpiry_duration_days(rs.getInt("expiry_duration_days"));
		q.setIsExpired(rs.getInt("is_expired"));
		q.setQuotationPdf(rs.getBytes("quotation_pdf"));
		q.setIsSubmitted(rs.getInt("is_submitted"));

		return q;
	}
}
