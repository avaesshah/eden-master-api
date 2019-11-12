package com.eden.api.util;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.edenstar.model.Zone;

public class ZoneMapper implements RowMapper<Zone> {

	@Override
	public Zone mapRow(ResultSet rs, int rowNum) throws SQLException {

		Zone z = new Zone();
		
		z.setZoneID(rs.getInt("zone_id"));
		z.setZoneNumber(rs.getInt("zone_number"));
		z.setLocationID(rs.getInt("location_id"));
		z.setZoneName(rs.getString("zone_name"));

		return z;
	}	
}
