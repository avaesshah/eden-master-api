package com.eden.api.util;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.edenstar.model.ProductPhoto;

public class ProductPhotoMapper implements RowMapper<ProductPhoto> {

	@Override
	public ProductPhoto mapRow(ResultSet rs, int rowNum) throws SQLException {

		ProductPhoto p = new ProductPhoto();

		p.setProductPhotoId(rs.getInt("product_photo_id"));
		p.setProductID(rs.getInt("product_id"));
		p.setDescription(rs.getString("description"));
		p.setProductPhoto(rs.getBytes("product_photo"));
		p.setPhotoPhotoURL(rs.getString("product_photo_URL"));

		return p;
	}
}
