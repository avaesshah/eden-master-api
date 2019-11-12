package com.eden.api.util;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.edenstar.model.Calendar;


public class CalendarMapper implements RowMapper<Calendar> {

	@Override
	public Calendar mapRow(ResultSet rs, int rowNum) throws SQLException {

		Calendar c = new Calendar();
		
		c.setCalendarID(rs.getInt("calendar_id"));
		c.setKioskID(rs.getInt("kiosk_id"));
		c.setLeaseStartDate(rs.getDate("lease_start_date"));
		c.setLeaseEndDate(rs.getDate("lease_end_date"));
		c.setLeaseDurationDays(rs.getInt("lease_duration_days"));

		return c;
	}
}
