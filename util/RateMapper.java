package com.eden.api.util;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.edenstar.model.Rate;

public class RateMapper implements RowMapper<Rate> {

	@Override
	public Rate mapRow(ResultSet rs, int rowNum) throws SQLException {

		Rate r = new Rate();

		r.setRateID(rs.getInt("rate_id"));
		r.setRateMax(rs.getDouble("rate_max"));
		r.setRateMin(rs.getDouble("rate_min"));
		r.setDiscountDurationDays(rs.getInt("discount_duration_days"));
		r.setRateCoeff(rs.getDouble("rate_coeff"));
		r.setZoneID(rs.getInt("zone_id"));
		r.setStandardRate(rs.getDouble("standard_rate"));

		return r;
	}
}
