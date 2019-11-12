package com.eden.api.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import com.eden.api.util.CompanyMapper;
import com.eden.api.util.CustomerMapper;
import com.eden.api.util.ProductMapper;
import com.eden.api.util.ProductPhotoMapper;
import com.edenstar.model.Company;
import com.edenstar.model.Customer;
import com.edenstar.model.Product;
import com.edenstar.model.ProductPhoto;
import com.edenstar.model.dash.CreateCompany;
import com.edenstar.model.dash.CreateCustomer;
import com.edenstar.model.dash.CreateProduct;
import com.edenstar.model.dash.CreateProductPhoto;

@Component
public class CustomerDAOImpl implements CustomerDAO {

	private JdbcTemplate jdbcTemplate;
	Customer result = new Customer();

	@Autowired
	public CustomerDAOImpl(JdbcTemplate _jdbcTemplate) {
		this.jdbcTemplate = _jdbcTemplate;
	}

	@Override
	public Customer getCustDetails(String cust_email) throws Exception {

		// User result = new User();
		String query = "SELECT * FROM customer where email_id_cus ='" + cust_email + "'";
		Customer r = new Customer();

		try {

			r = jdbcTemplate.queryForObject(query, new CustomerMapper());

		} catch (Exception e) {

		} // try

		return r;

	} // getCustDetails

	@Override
	public int addCustomer(CreateCustomer c) throws Exception {

		NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
		final KeyHolder keyHolder = new GeneratedKeyHolder();
		int status = 0;

		final String INSERT_QUERY = "insert into customer (email_id_cus, first_name, last_name, address, po_box"
				+ ", emirate, mobile_number, office_number, trade_licence, trade_name ) values (:email_id_cus, :first_name, :last_name, :address, :po_box"
				+ ", :emirate, :mobile_number, :office_number, :trade_licence, :trade_name)";

		try {
			// Creating map with all required params
			MapSqlParameterSource paramMap = new MapSqlParameterSource();

			paramMap.addValue("customer_id", c.getCustomerID());
			paramMap.addValue("email_id_cus", c.getEmailIDCus().toString().toLowerCase());
			paramMap.addValue("first_name", c.getFirstName());
			paramMap.addValue("last_name", c.getLastName());
			paramMap.addValue("address", c.getAddress());
			paramMap.addValue("po_box", c.getPoBox());
			paramMap.addValue("emirate", c.getEmirate());
			paramMap.addValue("mobile_number", c.getMobileNumber());
			paramMap.addValue("office_number", c.getOfficeNumber());
			paramMap.addValue("trade_licence", c.getTradeLicence());
			paramMap.addValue("trade_name", c.getTradeName());

			// Passing map containing named params
			status = namedJdbcTemplate.update(INSERT_QUERY, paramMap, keyHolder, new String[] { "customer_id" });

			// get the auto generated primary key from the new customer insertion
			Number generatedCusID = keyHolder.getKey();

			String companyName = c.getCompany().getCompanyName().toString();

			System.out.println("the customer_id for " + c.getEmailIDCus() + " is " + generatedCusID + " company name = "
					+ companyName);

			status = generatedCusID.intValue();

		} catch (Exception e) {

		} // try

		System.out.println("customer id generated = " + status);
		return status;
	}

	@Override
	public int addCompany(CreateCustomer c) throws Exception {
		NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
		final KeyHolder keyHolder = new GeneratedKeyHolder();
		int status = 0;

		final String INSERT_QUERY = "insert into company (company_name, company_logo, customer_id) values"
				+ " (:company_name, :company_logo, :customer_id)";

		try {
			// Creating map with all required params
			MapSqlParameterSource paramMap = new MapSqlParameterSource();

			paramMap.addValue("company_id", c.getCompany().getCompanyID());
			paramMap.addValue("company_name", c.getCompany().getCompanyName().toString());
			paramMap.addValue("company_logo", c.getCompany().getCompanyLogo());
			paramMap.addValue("customer_id", c.getCompany().getCustomerID());

			status = namedJdbcTemplate.update(INSERT_QUERY, paramMap, keyHolder, new String[] { "company_id" });

			// get the auto generated primary key from the new customer insertion
			Number generatedComID = keyHolder.getKey();
			status = generatedComID.intValue();

			System.out.println("new company ID = " + status);

		} catch (Exception e) {

		} // try

		return status;
	}

	@Override
	public List<Company> getCompanies(int customer_id) throws Exception {

		String query = "SELECT * FROM company where customer_id ='" + customer_id + "'";
		List<Company> companyList = new ArrayList<Company>();
		List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();

		try {

			rows = jdbcTemplate.queryForList(query);

			for (Map<String, Object> row : rows) {

				Company c = new Company();
				c.setCompanyID((Integer) (row.get("company_id")));
				c.setCompanyName((String) (row.get("company_name")));
				c.setCompanyLogo((byte[]) (row.get("company_logo")));
				c.setCustomerID((Integer) (row.get("customer_id")));

				companyList.add(c);
			}

		} catch (Exception e) {

		} // try

		return companyList;

	} // getCompanies

	@Override
	public ArrayList<Product> getProducts(int companyID) throws Exception {
		String query = "SELECT * FROM product where company_id ='" + companyID + "'";

		ArrayList<Product> productList = new ArrayList<Product>();
		List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();

		try {

			rows = jdbcTemplate.queryForList(query);

			for (Map<String, Object> row : rows) {

				Product p = new Product();
				p.setProductID((Integer) (row.get("product_id")));
				p.setCompanyID((Integer) (row.get("company_id")));
				p.setDescription((String) (row.get("description")));
				p.setOrigin((String) (row.get("origin")));
				p.setPriceRange((String) (row.get("price_range")));

				productList.add(p);
			}

		} catch (Exception e) {

		} // try

		return productList;
	} // getProducts

	@Override
	public ArrayList<ProductPhoto> getProductPhotos(int productID) throws Exception {
		// TODO Auto-generated method stub
		String query = "SELECT * FROM product_photo where product_id ='" + productID + "'";

		ArrayList<ProductPhoto> productPhotoList = new ArrayList<ProductPhoto>();
		List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();

		try {

			rows = jdbcTemplate.queryForList(query);

			for (Map<String, Object> row : rows) {

				ProductPhoto p = new ProductPhoto();

				p.setProductPhotoId((Integer) (row.get("product_photo_id")));
				p.setProductID((Integer) (row.get("product_id")));
				p.setProductPhoto((byte[]) (row.get("product_photo")));
				p.setDescription((String) (row.get("description")));
				p.setPhotoPhotoURL((String) (row.get("product_photo_URL")));

				productPhotoList.add(p);
			}

		} catch (Exception e) {

		} // try

		return productPhotoList;
	} // getProductPhotos

	@Override
	public int deleteCustomer(int customerID) throws Exception {

		int status = 0;

		final String DELETE_QUERY = "update customer set is_archived = 1 where customer_id = ?"; //"delete from customer where customer_id = ?";

		// define query arguments
		Object[] params = { customerID };

		status = jdbcTemplate.update(DELETE_QUERY, params);
		System.out.println("delete status = " + status);

		return status;

	} // deleteCustomer

	@Override
	public int updateCustomer(CreateCustomer c) throws Exception {

		NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
		int status = 0;

		String UPDATE_QUERY = "update customer set email_id_cus = :email_id_cus,"
				+ " first_name = :first_name, last_name = :last_name, address = :address,"
				+ "po_box = :po_box, emirate = :emirate, mobile_number = :mobile_number,"
				+ "office_number = :office_number, trade_licence = :trade_licence, trade_name = :trade_name where customer_id = :customer_id";

		try {

			// Adding params using MapSqlParameterSource class
			SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("customer_id", c.getCustomerID())
					.addValue("email_id_cus", c.getEmailIDCus()).addValue("first_name", c.getFirstName())
					.addValue("last_name", c.getLastName()).addValue("address", c.getAddress())
					.addValue("po_box", c.getPoBox()).addValue("emirate", c.getEmirate())
					.addValue("mobile_number", c.getMobileNumber()).addValue("office_number", c.getOfficeNumber())
					.addValue("trade_licence", c.getTradeLicence()).addValue("trade_name", c.getTradeName());

			status = namedJdbcTemplate.update(UPDATE_QUERY, namedParameters);

			if (status != 0) {
				System.out.println("User data updated for ID " + c.getEmailIDCus());
			} else {
				System.out.println("No Employee found with ID " + c.getEmailIDCus());
			}

		} catch (Exception e) {

		} // try
		return status;

	} // updateCustomer

	@Override
	public List<Customer> getAllCustomers() throws Exception {

		String query = "SELECT * FROM customer where is_archived = 0";
		List<Customer> customerList = new ArrayList<Customer>();
		List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();

		try {

			rows = jdbcTemplate.queryForList(query);

			for (Map<String, Object> row : rows) {

				Customer c = new Customer();

				c.setCustomerID((Integer) (row.get("customer_id")));
				c.setEmailIDCus((String) (row.get("email_id_cus")));
				c.setFirstName((String) (row.get("first_name")));
				c.setLastName((String) (row.get("last_name")));
				c.setAddress((String) (row.get("address")));
				c.setPoBox((String) (row.get("po_box")));
				c.setEmirate((String) (row.get("emirate")));
				c.setMobileNumber((String) (row.get("mobile_number")));
				c.setOfficeNumber((String) (row.get("office_number")));
				c.setTradeLicence((Integer) (row.get("trade_licence")));
				c.setTradeName((String) (row.get("trade_name")));

				customerList.add(c);
			}

		} catch (Exception e) {

		} // try

		return customerList;

	} // getAllCustomers

	@Override
	public int addAnotherCompany(CreateCompany c) throws Exception {
		NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
		final KeyHolder keyHolder = new GeneratedKeyHolder();
		int status = 0;

		final String INSERT_QUERY = "insert into company (company_name, company_logo, customer_id) values"
				+ " (:company_name, :company_logo, :customer_id)";

		try {

			// Creating map with all required params
			MapSqlParameterSource paramMap = new MapSqlParameterSource();

			paramMap.addValue("company_name", c.getCompanyName().toString());
			paramMap.addValue("company_logo", c.getCompanyLogo());
			paramMap.addValue("customer_id", c.getCustomerID());

			status = namedJdbcTemplate.update(INSERT_QUERY, paramMap, keyHolder, new String[] { "company_id" });

			// get the auto generated primary key from the new customer insertion
			Number generatedComID = keyHolder.getKey();
			status = generatedComID.intValue();

			System.out.println("new company ID = " + status);

		} catch (Exception e) {

		} // try

		return status;
	} // addAnotherCompany

	@Override
	public Company getCompany(int companyID) throws Exception {
		// User result = new User();
		String query = "SELECT * FROM company where company_id ='" + companyID + "'";
		Company r = new Company();

		try {

			r = jdbcTemplate.queryForObject(query, new CompanyMapper());

		} catch (Exception e) {

		} // try

		return r;
	} // getCompany

	@Override
	public int deleteCompany(int companyID) throws Exception {
		// TODO Auto-generated method stub
		int status = 0;

		final String DELETE_QUERY = "delete from company where company_id = ?";

		// define query arguments
		Object[] params = { companyID };

		status = jdbcTemplate.update(DELETE_QUERY, params);
		System.out.println("delete status = " + status);

		return status;
	} // deleteCompany

	@Override
	public List<Company> getAllCompanies() throws Exception {
		// TODO Auto-generated method stub
		String query = "SELECT * FROM company";
		List<Company> companyList = new ArrayList<Company>();
		List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();

		try {

			rows = jdbcTemplate.queryForList(query);

			for (Map<String, Object> row : rows) {

				Company c = new Company();

				c.setCompanyID((Integer) (row.get("company_id")));
				c.setCompanyName((String) (row.get("company_name")));
				c.setCompanyLogo((byte[]) (row.get("company_logo")));
				c.setCustomerID((Integer) (row.get("customer_id")));

				companyList.add(c);
			}

		} catch (Exception e) {

		} // try

		return companyList;
	} // getAllCompanies

	@Override
	public int addProduct(CreateProduct p) throws Exception {
		NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
		final KeyHolder keyHolder = new GeneratedKeyHolder();
		int status = 0;

		final String INSERT_QUERY = "insert into product (company_id, description, origin, price_range) values"
				+ " (:company_id, :description, :origin, :price_range)";

		try {

			// Creating map with all required params
			MapSqlParameterSource paramMap = new MapSqlParameterSource();

			paramMap.addValue("company_id", p.getCompanyID());
			paramMap.addValue("description", p.getDescription());
			paramMap.addValue("origin", p.getOrigin());
			paramMap.addValue("price_range", p.getPriceRange());

			status = namedJdbcTemplate.update(INSERT_QUERY, paramMap, keyHolder, new String[] { "product_id" });

			// get the auto generated primary key from the new customer insertion
			Number generatedComID = keyHolder.getKey();
			status = generatedComID.intValue();

			System.out.println("new product ID = " + status);

		} catch (Exception e) {

		} // try

		return status;
	} // add product

	@Override
	public Product getProduct(int productID) throws Exception {

		String query = "SELECT * FROM product where product_id ='" + productID + "'";
		Product r = new Product();

		try {

			r = jdbcTemplate.queryForObject(query, new ProductMapper());

		} catch (Exception e) {

		} // try

		return r;
	} // getProduct

	@Override
	public int deleteProduct(int productID) throws Exception {
		// TODO Auto-generated method stub
		int status = 0;

		final String DELETE_QUERY = "delete from product where product_id = ?";

		// define query arguments
		Object[] params = { productID };

		status = jdbcTemplate.update(DELETE_QUERY, params);
		System.out.println("delete status = " + status);

		return status;
	} // deleteProduct

	@Override
	public int updateProduct(CreateProduct p) throws Exception {

		NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
		int status = 0;

		String UPDATE_QUERY = "update product set company_id = :company_id, description = :description, origin = :origin,  price_range = :price_range "
				+ "where product_id = :product_id";

		try {

			System.out.println("product_id = " + p.getProductID());
			System.out.println("company_id = " + p.getCompanyID());
			System.out.println("description = " + p.getDescription());
			System.out.println(("origin = " + p.getOrigin()));
			System.out.println("price_range = " + p.getPriceRange());

			// Adding params using MapSqlParameterSource class
			SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("product_id", p.getProductID())
					.addValue("company_id", p.getCompanyID()).addValue("description", p.getDescription())
					.addValue("origin", p.getOrigin()).addValue("price_range", p.getPriceRange());

			status = namedJdbcTemplate.update(UPDATE_QUERY, namedParameters);

			if (status != 0) {
				System.out.println("Product data updated for ID " + p.getDescription());
			} else {
				System.out.println("Product failed to update for " + p.getDescription());
			}

		} catch (Exception e) {

		} // try
		return status;
	} // updateProduct

	@Override
	public int addProductPhoto(CreateProductPhoto p) throws Exception {

		NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
		final KeyHolder keyHolder = new GeneratedKeyHolder();
		int status = 0;

		final String INSERT_QUERY = "insert into product_photo (product_photo, description, product_id, product_photo_URL) values"
				+ " (:product_photo, :description, :product_id, :product_photo_URL)";

		try {

			// Creating map with all required params
			MapSqlParameterSource paramMap = new MapSqlParameterSource();

			paramMap.addValue("product_photo", p.getProductPhoto());
			paramMap.addValue("description", p.getDescription());
			paramMap.addValue("product_id", p.getProductID());
			paramMap.addValue("product_photo_URL", p.getPhotoPhotoURL());

			status = namedJdbcTemplate.update(INSERT_QUERY, paramMap, keyHolder, new String[] { "product_photo_id" });

			// get the auto generated primary key from the new customer insertion
			Number generatedComID = keyHolder.getKey();
			status = generatedComID.intValue();

			System.out.println("new product photo ID = " + status);

		} catch (Exception e) {

		} // try

		return status;
	}

	@Override
	public int deleteProductPhoto(int productPhotoId) throws Exception {

		int status = 0;

		final String DELETE_QUERY = "delete from product_photo where product_photo_id = ?";

		// define query arguments
		Object[] params = { productPhotoId };

		status = jdbcTemplate.update(DELETE_QUERY, params);
		System.out.println("delete status = " + status);

		return status;
	} // deleteProductPhoto

	@Override
	public ProductPhoto getProductPhotobyID(int productPhotoID) throws Exception {
		String query = "SELECT * FROM product_photo where product_photo_id ='" + productPhotoID + "'";
		ProductPhoto r = new ProductPhoto();

		try {

			r = jdbcTemplate.queryForObject(query, new ProductPhotoMapper());

		} catch (Exception e) {

		} // try

		return r;
	} // getProductPhotobyID

	@Override
	public int updateProductPhoto(CreateProductPhoto p) throws Exception {

		NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
		int status = 0;

		String UPDATE_QUERY = "update product_photo set product_photo = :product_photo, description = :description, product_id = :product_id,"
				+ "product_photo_URL = :product_photo_URL"
				+ " where product_photo_id = :product_photo_id";

		try {

			// Adding params using MapSqlParameterSource class
			SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("product_photo_id", p.getProductPhotoId())
					.addValue("product_photo", p.getProductPhoto()).addValue("description", p.getDescription())
					.addValue("product_id", p.getProductID())
					.addValue("product_photo_URL", p.getPhotoPhotoURL());

			status = namedJdbcTemplate.update(UPDATE_QUERY, namedParameters);

			if (status != 0) {
				System.out.println("Product photo data updated for ID " + p.getDescription());
			} else {
				System.out.println("Product photo failed to update for " + p.getDescription());
			}

		} catch (Exception e) {

		} // try
		return status;
	} // updateProductPhoto

	@Override
	public int updateCompany(CreateCompany c) throws Exception {
		
		NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
		int status = 0;

		String UPDATE_QUERY = "update company set company_name = :company_name, company_logo = :company_logo, customer_id = :customer_id"
				+ " where company_id = :company_id";

		try {

			// Adding params using MapSqlParameterSource class
			SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("company_id", c.getCompanyID())
					.addValue("company_name", c.getCompanyName())
					.addValue("company_logo", c.getCompanyLogo()).addValue("customer_id", c.getCustomerID());

			status = namedJdbcTemplate.update(UPDATE_QUERY, namedParameters);

			if (status != 0) {
				System.out.println("Company data updated for ID " + c.getCompanyName());
			} else {
				System.out.println("Company data failed to update for " + c.getCompanyName());
			}

		} catch (Exception e) {

		} // try
		return status;
			
	} // update company information

	@Override
	public Customer getCustomerDetails(int customerID) throws Exception {

		String query = "SELECT * FROM customer where customer_id ='" + customerID + "'";
		Customer r = new Customer();

		try {

			r = jdbcTemplate.queryForObject(query, new CustomerMapper());

		} catch (Exception e) {

		} // try

		return r;
	} // getCUstomerDetails

	@Override
	public int countCompanies(int customerID) throws Exception {
		int result = 0;

		String query = "SELECT count(*) as companyCount from company where customer_id = ?";

		try {

			Object[] params = { customerID };
			result = jdbcTemplate.queryForObject(query, params,  Integer.class);

		} catch (Exception e) {

		} // try

		return result;
		
	} // countCompanies

} // CustomerDAOImpl
