package com.eden.api.util;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.edenstar.model.Product;

public class ProductMapper implements RowMapper<Product> {

	@Override
	public Product mapRow(ResultSet rs, int rowNum) throws SQLException {

		Product p = new Product();

		p.setProductID(rs.getInt("product_id"));
		p.setCompanyID(rs.getInt("company_id"));
		p.setDescription(rs.getString("description"));
		p.setOrigin(rs.getString("origin"));
		p.setPriceRange(rs.getString("price_range"));

		return p;
	}
	
}