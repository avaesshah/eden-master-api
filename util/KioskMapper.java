package com.eden.api.util;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.edenstar.model.Kiosk;

public class KioskMapper implements RowMapper<Kiosk> {

	@Override
	public Kiosk mapRow(ResultSet rs, int rowNum) throws SQLException {

		Kiosk k = new Kiosk();
		
		
		k.setKioskID(rs.getInt("kiosk_id"));
		k.setKioskNumber(rs.getInt("kiosk_number"));
		k.setGridLocationRow(rs.getInt("grid_location_row"));
		k.setGridLocationColumn(rs.getInt("grid_location_column"));
		k.setIsVoid(rs.getInt("is_void"));
		k.setIsLocked(rs.getInt("is_locked"));
		k.setZoneID(rs.getInt("zone_id"));


		return k;
	}	
} 
