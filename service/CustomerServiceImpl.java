package com.eden.api.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eden.api.dao.CustomerDAO;
import com.edenstar.model.Company;
import com.edenstar.model.Customer;
import com.edenstar.model.Product;
import com.edenstar.model.ProductPhoto;
import com.edenstar.model.dash.CreateCompany;
import com.edenstar.model.dash.CreateCustomer;
import com.edenstar.model.dash.CreateProduct;
import com.edenstar.model.dash.CreateProductPhoto;

@Service("customerService")
public class CustomerServiceImpl implements CustomerService {
	
	@Autowired
	private CustomerDAO customerDAO;

	@Override
	public Customer getCustDetails(String cust_email) throws Exception {
		// TODO Auto-generated method stub
		return customerDAO.getCustDetails(cust_email);
	}

	@Override
	public int addCustomer(CreateCustomer c) throws Exception {
		// TODO Auto-generated method stub
		return customerDAO.addCustomer(c);
	}

	@Override
	public int addCompany(CreateCustomer c) throws Exception {
		// TODO Auto-generated method stub
		return customerDAO.addCompany(c);
	}

	@Override
	public List<Company> getCustComp(int customer_id) throws Exception {
		// TODO Auto-generated method stub
		return customerDAO.getCompanies(customer_id);
	}

	@Override
	public ArrayList<Product> getCompanyProduct(int companyID) throws Exception {
		// TODO Auto-generated method stub
		return customerDAO.getProducts(companyID);
	}

	@Override
	public ArrayList<ProductPhoto> getProductPhoto(int productID) throws Exception {
		// TODO Auto-generated method stub
		return customerDAO.getProductPhotos(productID);
	}

	@Override
	public int deleteCustomer(int customerID) throws Exception {
		// TODO Auto-generated method stub
		return customerDAO.deleteCustomer(customerID);
	}

	@Override
	public int updateUser(CreateCustomer c) throws Exception {
		// TODO Auto-generated method stub
		return customerDAO.updateCustomer(c);
	}

	@Override
	public List<Customer> getAllCustomers() throws Exception {
		// TODO Auto-generated method stub
		return customerDAO.getAllCustomers();
	}

	@Override
	public int addAnotherCompany(CreateCompany c) throws Exception {
		// TODO Auto-generated method stub
		return customerDAO.addAnotherCompany(c);
	}

	@Override
	public Company getCompany(int companyID) throws Exception {
		// TODO Auto-generated method stub
		return customerDAO.getCompany(companyID);
	}

	@Override
	public int deleteCompany(int companyID) throws Exception {
		// TODO Auto-generated method stub
		return customerDAO.deleteCompany(companyID);
	}

	@Override
	public List<Company> getAllCompanies() throws Exception {
		// TODO Auto-generated method stub
		return customerDAO.getAllCompanies();
	}

	@Override
	public int addProduct(CreateProduct p) throws Exception {
		// TODO Auto-generated method stub
		return customerDAO.addProduct(p);
	}

	@Override
	public Product getProduct(int productID) throws Exception {
		// TODO Auto-generated method stub
		return customerDAO.getProduct(productID);
	}

	@Override
	public int deleteProduct(int productID) throws Exception {
		// TODO Auto-generated method stub
		return customerDAO.deleteProduct(productID);
	}

	@Override
	public List<Product> getProducts(int companyID) throws Exception {
		// TODO Auto-generated method stub
		return customerDAO.getProducts(companyID);
	}

	@Override
	public int updateProduct(CreateProduct p) throws Exception {
		// TODO Auto-generated method stub
		return customerDAO.updateProduct(p);
	}

	@Override
	public int addProductPhoto(CreateProductPhoto p) throws Exception {
		// TODO Auto-generated method stub
		return customerDAO.addProductPhoto(p);
	}

	@Override
	public int deleteProductPhoto(int productPhotoId) throws Exception {
		// TODO Auto-generated method stub
		return customerDAO.deleteProductPhoto(productPhotoId);
	}

	@Override
	public ProductPhoto getProductPhotobyID(int productPhotoID) throws Exception {
		// TODO Auto-generated method stub
		return customerDAO.getProductPhotobyID(productPhotoID);
	}

	@Override
	public int updateProductPhoto(CreateProductPhoto p) throws Exception {
		// TODO Auto-generated method stub
		return customerDAO.updateProductPhoto(p);
	}

	@Override
	public int updateCompany(CreateCompany c) throws Exception {
		// TODO Auto-generated method stub
		return customerDAO.updateCompany(c);
	}

	@Override
	public Customer getCustomerDetails(int customerID) throws Exception {
		// TODO Auto-generated method stub
		return customerDAO.getCustomerDetails(customerID);
	}

	@Override
	public int countCompanies(int customerID) throws Exception {
		// TODO Auto-generated method stub
		return customerDAO.countCompanies(customerID);
	}

	
} // CustomerServiceImpl
