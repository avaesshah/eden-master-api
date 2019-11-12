package com.eden.api.service;

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

public interface CustomerService {


	// get customer details
	Customer getCustDetails(String cust_email) throws Exception;

	// add a customer to the database 
	int addCustomer(CreateCustomer c) throws Exception;

	// add a company
	int addCompany(CreateCustomer c) throws Exception;

	// sends a list of companies assoaicated with a customer
	List<Company> getCustComp(int customer_id) throws Exception;

	// sends a list of products asociated with a company by company_id
	ArrayList<Product> getCompanyProduct(int companyID) throws Exception;

	// sends a list of photos associated with a product by product_id
	ArrayList<ProductPhoto> getProductPhoto(int productID) throws Exception;

	// delete customer and all associated data (companies, products, photos)
	int deleteCustomer(int customerID) throws Exception;

	// update customer details
	int updateUser(CreateCustomer c) throws Exception;

	// returns a list of all users
	List<Customer> getAllCustomers() throws Exception;

	// add another company under the customer's id
	int addAnotherCompany(CreateCompany c) throws Exception;

	// returns company details
	Company getCompany(int companyID) throws Exception;
	
	// delete a company from the databvase
	int deleteCompany(int companyID) throws Exception;

	// get a list of all the companies
	List<Company> getAllCompanies() throws Exception;

	// add a product to a company
	int addProduct(CreateProduct p) throws Exception;

	// get a product 
	Product getProduct(int productID) throws Exception;

	// delete a product
	int deleteProduct(int productID) throws Exception;

	// list all products under a company
	List<Product> getProducts(int companyID) throws Exception;

	// updates the database with new updated product information
	int updateProduct(CreateProduct p) throws Exception;

	// add a product photo to product
	int addProductPhoto(CreateProductPhoto p) throws Exception;

	// delete a product photo
	int deleteProductPhoto(int productPhotoId) throws Exception;

	// returns a single product photo
	ProductPhoto getProductPhotobyID(int productPhotoID) throws Exception;

	// update product photo
	int updateProductPhoto(CreateProductPhoto p) throws Exception;

	// update company information
	int updateCompany(CreateCompany c) throws Exception;

	// returns customer details by id
	Customer getCustomerDetails(int customerID) throws Exception;

	// Counts the number of companies registered under a customer
	int countCompanies(int customerID) throws Exception;
	
	

} // CustomerService
