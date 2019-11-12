package com.eden.api.util;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.edenstar.model.GridCount;

public class GridMapper implements RowMapper<GridCount> {

	@Override
	public GridCount mapRow(ResultSet rs, int rowNum) throws SQLException {

		GridCount g = new GridCount();
		
		g.setGrid_max(rs.getInt("gridMax"));

		return g;
	}
}