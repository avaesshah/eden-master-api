package com.eden.api.util;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.edenstar.model.BookMap;

public class BookingMapper implements RowMapper<BookMap> {

	@Override
	public BookMap mapRow(ResultSet rs, int rowNum) throws SQLException {

		BookMap b = new BookMap();
		
		b.setBooking_id(rs.getInt("booking_id"));
		b.setApplicationID(rs.getInt("application_id"));
		b.setBooking_ref(rs.getString("booking_ref"));
		b.setQuoteRef(rs.getString("quote_ref"));
		b.setCalendar_id(rs.getInt("calendar_id"));
		b.setCustomer_id(rs.getInt("customer_id"));	
		b.setSales_id(rs.getInt("sales_id"));
		b.setManager_id(rs.getInt("manager_id"));
		b.setKiosk_id(rs.getInt("kiosk_id"));
		b.setDate_of_booking(rs.getDate("date_of_booking"));
		b.setComments(rs.getString("comments"));
		b.setLease_start_date(rs.getDate("lease_start_date"));
		b.setLease_end_date(rs.getDate("lease_end_date"));
		b.setLease_duration_days(rs.getInt("lease_duration_days"));
		b.setRate(rs.getDouble("rate"));
		b.setLease_total(rs.getDouble("lease_total"));
		b.setSecurity_deposit(rs.getString("security_deposit"));
		b.setIs_cancelled(rs.getInt("is_cancelled"));
		b.setIs_expired(rs.getInt("is_expired"));
		b.setIs_expiry_due(rs.getInt("is_expiry_due"));
		b.setReview_flag(rs.getInt("review_flag"));
		b.setExpiry_due_period_day(rs.getInt("expiry_due_period_days"));

		return b;
	}	
}
