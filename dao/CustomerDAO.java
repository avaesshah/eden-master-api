package com.eden.api.dao;

import java.util.ArrayList;
import java.util.List;

import com.edenstar.model.Company;
import com.edenstar.model.Customer;
import com.edenstar.model.Product;
import com.edenstar.model.ProductPhoto;
import com.edenstar.model.dash.CreateCompany;
import com.edenstar.model.dash.CreateCustomer;
import com.edenstar.model.dash.CreateProduct;
import com.edenstar.model.dash.CreateProductPhoto;

public interface CustomerDAO {

	// get the uswer details by email id
	Customer getCustDetails(String cust_email) throws Exception;

	// add a customer
	int addCustomer(CreateCustomer c) throws Exception;

	// add a company
	int addCompany(CreateCustomer c) throws Exception;

	// get a list of companies associated with customer
	List<Company> getCompanies(int customer_id) throws Exception;

	// get a list of products associated with company
	ArrayList<Product> getProducts(int companyID) throws Exception;

	// get a list of product photos assicated with a product
	ArrayList<ProductPhoto> getProductPhotos(int productID) throws Exception;

	// deletes a customer and all associated data (companies, products, photos)
	int deleteCustomer(int customerID) throws Exception;

	// update customer details
	int updateCustomer(CreateCustomer c) throws Exception;

	// returns a list of all the customers
	List<Customer> getAllCustomers() throws Exception;

	// add another company under customer id
	int addAnotherCompany(CreateCompany c) throws Exception;

	// get company details
	Company getCompany(int companyID) throws Exception;

	// delete company
	int deleteCompany(int companyID) throws Exception;

	// get a list of all companies
	List<Company> getAllCompanies() throws Exception;

	// add product to a company
	int addProduct(CreateProduct p) throws Exception;
	
	// get product details
	Product getProduct(int productID) throws Exception;

	// delete product
	int deleteProduct(int productID) throws Exception;

	// update product details
	int updateProduct(CreateProduct p) throws Exception;

	// add a product photo under a product
	int addProductPhoto(CreateProductPhoto p) throws Exception;

	// delete a product photo
	int deleteProductPhoto(int productPhotoId) throws Exception;

	// returns a product photo by ID
	ProductPhoto getProductPhotobyID(int productPhotoID) throws Exception;

	// update product photo
	int updateProductPhoto(CreateProductPhoto p) throws Exception;

	// update company information
	int updateCompany(CreateCompany c) throws Exception;

	// gert customer details by customer ID
	Customer getCustomerDetails(int customerID) throws Exception;

	// gets a company count
	int countCompanies(int customerID) throws Exception;

} // CustomerDAO
