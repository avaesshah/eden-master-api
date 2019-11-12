package com.eden.api.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.eden.api.service.CustomerService;
import com.eden.api.service.EngineService;
import com.eden.api.service.validation.CustomerValidation;
import com.eden.api.util.Constants;
import com.eden.api.util.Response;
import com.eden.api.util.ResponseEnum;
import com.edenstar.model.Company;
import com.edenstar.model.Customer;
import com.edenstar.model.Product;
import com.edenstar.model.ProductPhoto;
import com.edenstar.model.dash.CreateCustomer;
import com.edenstar.model.dash.GetCustomer;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class CustomerController extends BaseController {

	Response response = new Response();

	@Autowired
	private CustomerService customerService;

	@Autowired
	private EngineService cpu;

	private CustomerValidation cusValid = new CustomerValidation();

	public CreateCustomer checkChanges(CreateCustomer c) {

		// gets the user record from database and compares
		// to see if the new data is null, and if so copy the database value over

		// get the record
		Customer r = new Customer();

		try {
			r = customerService.getCustDetails(c.getEmailIDCus());

			c.setCustomerID(r.getCustomerID()); // this value cannot be changed
			if (c.getAddress() == null)
				c.setAddress(r.getAddress().toString());

			if (c.getEmailIDCus() == null)
				c.setEmailIDCus(r.getEmailIDCus().toString());

			if (c.getFirstName() == null)
				c.setFirstName(r.getFirstName().toString());

			if (c.getLastName() == null)
				c.setLastName(r.getLastName().toString());

			if (c.getPoBox() == null)
				c.setPoBox(r.getPoBox().toString());

			if (c.getEmirate() == null)
				c.setEmirate(r.getEmirate().toString());

			if (c.getMobileNumber() == null)
				c.setMobileNumber(r.getMobileNumber().toString());

			if (c.getOfficeNumber() == null)
				c.setOfficeNumber(r.getOfficeNumber().toString());

			if (c.getTradeLicence() < 0 || c.getTradeLicence() > 1) {
				c.setTradeLicence(r.getTradeLicence());

				if (c.getTradeName() == null)
					c.setTradeName(r.getTradeName().toString());
			}

		} catch (Exception e) {
			e.printStackTrace();
		} // try-catch

		return c;

	} // checkChanges

	// **************************************************************************************
	// add a customer + company to the database
	// **************************************************************************************
	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_DASH
			+ Constants.PATH_ADD_CUST, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response addCustomer(@RequestBody CreateCustomer c) {

		try {

			// make sure none of the mandatory fields are null
			if (cusValid.isNull(c, true)) {
				response = Response.build("Error",
						"mandatory parameters : [staff_email/ emailIDCus / firstName / lastName / address / emirate /"
								+ " poBox / mobileNumber / tradeLicence [= 1 or 0] ] cannot be null",
						false);
				return response;
			} // isNull check

			// check to see if any of the mandatory fields are empty
			if (cusValid.fieldsAreEmpty(c, true)) {
				response = Response.build("Error",
						"no data entered for mandatory parameters, either [staff_email / emailIDCus / firstName / lastName / address / emirate /"
								+ " poBox / mobileNumber / tradeLicence [= 1 or 0]",
						false);
				return response;
			} // isEmpty check

			// check to see if the staff user exists on the database
			if (!cpu.userExists(c.getStaff_email())) {
				response = Response.build("Error", "staff email id " + c.getStaff_email() + " does not exist", false);
				return response;
			} // userExists

			// format the fields to standard
			c = cusValid.formatFields(c);

			// check to see if the new customer exists on database ...
			if (!cpu.customerExists(c.getEmailIDCus())) {

				// if the customer does not exist, add it !

				int customer_id = customerService.addCustomer(c);
				System.out.println(" the generated customer id is " + customer_id);

				if (customer_id > 0) {
					// response = Response.build("Success", "customer successfully added to the
					// database", true);

					// set the customer id in the company object
					c.getCompany().setCustomerID(customer_id);

					// set the customer id in the customer object
					c.setCustomerID(customer_id);

					// we now have to add the company
					int company_id = customerService.addCompany(c);
					System.out.println(" the generated company id is = " + company_id);

					// we need to also add a directory for the product photos to be stored
					// cusValid.addCompanyDirectory(c.getCompany().getCompanyName());

					if (company_id > 0) {
						response = Response.build("Success", "customer and company successfully added to the database",
								true);
						c.getCompany().setCompanyID(company_id);
						response.setData(c);
						return response;
					} else {
						response = Response.build("Failure", "company could not be added to the database", false);
						return response;
					}

				} else {
					response = Response.build("Failure", "customer could not be added to the database", false);
					return response;
				} // nested if

			} else {
				response = Response.build("Error", "customer already exists on database", false);
				return response;
			}

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // addCustomer (and company)

	// **************************************************************************************
	// get a customer and all associated data from the database
	// **************************************************************************************
	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_DASH
			+ Constants.PATH_GET_CUST, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response getCustomer(@RequestBody GetCustomer c) {

		// list of companies associated with customer
		List<Company> companyList = new ArrayList<Company>();

		// list of products associated with each company
		List<ArrayList<Product>> listOfProductLists = new ArrayList<ArrayList<Product>>();
		ArrayList<Product> productList = new ArrayList<Product>();

		// list of photos associated with each product
		List<ArrayList<ProductPhoto>> listOfProductPhotosLists = new ArrayList<ArrayList<ProductPhoto>>();
		ArrayList<ProductPhoto> productPhotoList = new ArrayList<ProductPhoto>();

		Customer cust = new Customer();

		try {

			// make sure that the staff_email and emailID are not null
			if (c.getStaff_email() == null || c.getEmailIDCus() == null) {
				response = Response.build("Error", "Mandatory fields : [emailIDCus / staff_email] cannot be null",
						false);
				return response;
			} // null fields

			// check to see if the fields are empty
			if (c.getStaff_email().contentEquals("") || c.getEmailIDCus().contentEquals("")) {
				response = Response.build("Error", "Mandatory fields : [emailIDCus / staff_email] cannot be empty",
						false);
				return response;
			} // empty fields

			// check to see if the staff user exists on the database
			if (!cpu.userExists(c.getStaff_email())) {
				response = Response.build("Error",
						"Staff email " + c.getStaff_email() + " does not exist on the database", false);
				return response;
			} // check if staff exists

			cust = customerService.getCustDetails(c.getEmailIDCus().toString().toLowerCase());
			if (cust.getEmailIDCus() == null) {
				response = Response.build("Error",
						"Customer email " + c.getEmailIDCus() + " does not exist on the database", false);
				return response;
			} // check to see if customer exists

			if (!cust.getEmailIDCus().contentEquals(c.getEmailIDCus().toString().toLowerCase())) {
				response = Response.build("Error",
						"Customer email " + c.getEmailIDCus() + " does not exist on the database", false);
				return response;
			} // check to see if customer details match the result

			// copy over the new customer details
			c = cusValid.setCustomerDetails(cust, c);

			// first must populate the number of companies and assign it to a list
			companyList = customerService.getCustComp(cust.getCustomerID());

			// we assign the company list to the GetCustomer object
			c.setCompanyList(companyList);

			// we then need to obtain the first company id for the first company to obtain
			// the products
			for (int i = 0; i < companyList.size(); i++) {

				// get a list of products associated with the each company_id
				productList = customerService.getCompanyProduct(companyList.get(i).getCompanyID());
				listOfProductLists.add(productList);

				// now we have to obtain each product photo associated to each product_id
				for (int j = 0; j < productList.size(); j++) {

					// see if there exists any product photos for that product
					productPhotoList = customerService.getProductPhoto(productList.get(j).getProductID());

					// now let us check to see of there are any product photos, if so add the list
					// to the list
					if (!productPhotoList.isEmpty()) {

						// now we add the list of photos for one product into the list of photos
						listOfProductPhotosLists.add(productPhotoList);
					} // if statement

				} // nested for

			} // for loop

			c.setProductList(listOfProductLists);
			c.setProductPhotoLists(listOfProductPhotosLists);

			response = Response.build("Success", "Customer details successfully obtained from database", true);
			response.setData(c);
			// return response;

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // getCustomer

	// **************************************************************************************
	// DELETE a customer and all associated companies and products from the database
	// **************************************************************************************
	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_DASH
			+ Constants.PATH_DELETE_CUST, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response deleteCustomer(@RequestBody CreateCustomer c) {

		try {

			// make sure that the staff_email and emailID are not null
			if (c.getStaff_email() == null || c.getCustomerID() == 0) {
				response = Response.build("Error", "Mandatory fields : [customerID / staff_email] cannot be null",
						false);
				return response;
			} // null fields

			// check to see if the fields are empty
			if (c.getStaff_email().contentEquals("") || c.getCustomerID() == 0) {
				response = Response.build("Error", "Mandatory fields : [customerID /staff_email] cannot be empty",
						false);
				return response;
			} // empty fields

			// check to see if the staff user exists on the database
			if (!cpu.userExists(c.getStaff_email())) {
				response = Response.build("Error", "staff email id " + c.getStaff_email() + " does not exist", false);
				return response;
			} // staff exists

			// check if the staff user has clearance to perform this action
			if (cpu.checkClearance(c.getStaff_email().toString()) == false) {

				// insufficient priviledges
				response = Response.build("Access Denied",
						"insufficient privileges - you must be either be admin or manager_admin to perform this action",
						false);
				return response;

			} // clearance check

			// check to see if the customer exists on database ...
			if (!cpu.customerExists(c.getCustomerID())) {
				// if the username does not exist, throw an error
				response = Response.build("Error", "Customer does not exist on the database", false);
				return response;

			}

			// perform delete action

			// the MSQL database has been set up with a delete cascading which means that
			// deleting the customer
			// will automatically delete all associated companies, products, product photos
			// from the database
			// use with caution !

			// delete record

			// if there are any product photos listed, we need to delete the corresponding
			// company directory

			List<Company> companyList = new ArrayList<Company>();
			companyList = customerService.getCustComp(c.getCustomerID());

			if (companyList.size() == 0) {

			} else {

				// we need to obtain the company name from the productID ...
				for (int i = 0; i < companyList.size(); i++) {
					cusValid.deleteDirectory(companyList.get(i).getCompanyName());
				}

			} // delete product photos directory

			int status = customerService.deleteCustomer(c.getCustomerID());

			if (status == 1) {
				response = Response.build("Success",
						"customer and all associated records (companies, products and product photos)"
								+ "  have successfully been deleted from the database",
						true);
				return response;

			} else {
				response = Response.build("Failure", "customer details could not be deleted from the database", false);
				return response;

			} // nested if

		} catch (

		Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // try-catch

		return response;

	} // deleteCustomer

	// **************************************************************************************
	// update a customer to the database
	// **************************************************************************************
	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_DASH
			+ Constants.PATH_UPDATE_CUST, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response updateCustomer(@RequestBody CreateCustomer c) {

		try {

			// check to see if the staff user exists on the database
			if (!cpu.userExists(c.getStaff_email())) {
				response = Response.build("Error", "staff email id " + c.getStaff_email() + " does not exist", false);
				return response;
			}

//			// check if the staff user has clearance to perform this action
//			if (cpu.checkClearance(c.getStaff_email().toString()) == false) {
//
//				// insufficient priviledges
//				response = Response.build("Access Denied",
//						"insufficient privileges - you must be either be admin or manager_admin to perform this action",
//						false);
//				return response;
//
//			} // clearance check

			// make sure that the customer exists on database ...
			if (cpu.customerExists(c.getEmailIDCus())) {

				// if the customer exists, we just update it with the amended details
				// first we must make sure that any null fields are replaced with database
				// values
				System.out.println(" customer " + c.getEmailIDCus() + " exists !");

				c = checkChanges(c);

				// format the fields to standard
				c = cusValid.formatFields(c);

				int status = customerService.updateUser(c);

				System.out.println("status = " + status);

				if (status == 1) {
					response = Response.build("Success", "customer details successfully updated to the database", true);
					response.setData(c);
					return response;
				} else {
					response = Response.build("Failure", "customer details could not be updated to the database",
							false);
					return response;
				} // nested if

			} else {
				// if the customer does not exist, throw an error
				response = Response.build("Error", "customer does not exist on the database", false);
				return response;

			} // if

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // updateCustomer

	// **************************************************************************************
	// get all customers from the database
	// **************************************************************************************
	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_DASH
			+ Constants.PATH_GETALL_CUST, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response getCustomers(@RequestBody GetCustomer c) {

		List<Customer> customerList = new ArrayList<Customer>();
		Customer customer = new Customer();

		try {

			// make sure that the staff_email and emailID are not null
			if (c.getStaff_email() == null || c.getEmailIDCus() == null) {
				response = Response.build("Error", "Mandatory fields : [emailID / staff_email] cannot be null", false);
				return response;
			} // null fields

			// check to see if the fields are empty
			if (c.getStaff_email().contentEquals("") || c.getEmailIDCus().contentEquals("")) {
				response = Response.build("Error", "Mandatory fields : [emailID / staff_email] cannot be empty", false);
				return response;
			} // empty fields

			// check to see if the staff user exists on the database
			if (!cpu.userExists(c.getStaff_email())) {
				response = Response.build("Error",
						"Staff email " + c.getStaff_email() + " does not exist on the database", false);
				return response;
			}

			// now we see if the request is for all the user or just one user
			// emailID = "*" means all the users and emailID = "somone@mail.com" for
			// specific user
			if (c.getEmailIDCus().contentEquals("*")) {

				// get all the users ...

				customerList = customerService.getAllCustomers();

				response = Response.build(ResponseEnum.OK.getStatus(), Constants.SUCCESS, ResponseEnum.OK.getMessage(),
						true);
				response.setData(customerList);

			} else {

				// get customer by email ID

				// customerList =
				// userService.getAllByType(u.getEmailID().toString().toLowerCase());
				customer = customerService.getCustDetails(c.getEmailIDCus().toLowerCase());

				// if the customerr does not exist ..
				if (customer.getEmailIDCus() == null) {
					response = Response.build("Error",
							"Customer email " + c.getEmailIDCus() + " does not exist on the database", false);
					return response;
				}

				if (customer.getEmailIDCus().contentEquals(c.getEmailIDCus())) {

					response = Response.build(ResponseEnum.OK.getStatus(), Constants.SUCCESS,
							ResponseEnum.OK.getMessage(), true);
					response.setData(customer);

				} else {
					// customer does not exist
					response = Response.build("Error", "customer email ID does not exist on the database", false);
					return response;
				} // nested if

			} // outer if statement

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // getCustomers

} // Customer Controller
