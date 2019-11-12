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
import com.eden.api.service.validation.CompanyValidation;
import com.eden.api.util.Constants;
import com.eden.api.util.Response;
import com.eden.api.util.ResponseEnum;
import com.edenstar.model.Company;
import com.edenstar.model.Customer;
import com.edenstar.model.dash.CreateCompany;
import com.edenstar.model.dash.GetCompany;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class CompanyController extends BaseController {

	Response response = new Response();

	CompanyValidation compValid = new CompanyValidation();

	@Autowired
	private CustomerService customerService;

	@Autowired
	private EngineService cpu;

	// **************************************************************************************
	// add a additional company for a existing customer
	// **************************************************************************************
	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_DASH
			+ Constants.PATH_ADD_COMP, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response addCompany(@RequestBody CreateCompany c) {

		try {

			// make sure none of the mandatory fields are null
			if (compValid.isNull(c) == true) {
				response = Response.build("Error",
						"mandatory parameters : [staff_email / customerID / companyName ] cannot be null", false);
				return response;
			} // isNull check

			// check to see if any of the mandatory fields are empty
			if (compValid.fieldsAreEmpty(c) == true) {
				response = Response.build("Error",
						"no data entered for mandatory parameters, either [staff_email / customerID / companyName ]",
						false);
				return response;
			} // isEmpty check

			// check to see if the staff user exists on the database
			if (!cpu.userExists(c.getStaff_email())) {
				response = Response.build("Error", "staff email id " + c.getStaff_email() + " does not exist", false);
				return response;
			} // userExists

			if (cpu.customerExists(c.getCustomerID())) {

				// if the customer exists we create a new company under the customer's id number

				int company_id = customerService.addAnotherCompany(c);

				if (company_id > 0) {
					response = Response.build("Success", "company successfully added to the database", true);

					// update with newly generated company ID
					c.setCompanyID(company_id);

					// add customer's email address to the return data
					Customer r = new Customer();
					r = customerService.getCustomerDetails(c.getCustomerID());
					c.setEmail_id_cus(r.getEmailIDCus().toString());
					response.setData(c);
					// return response;
				} else {
					response = Response.build("Failure", "company could not be added to the database", false);
					return response;
				} // nested if

			} else {
				response = Response.build("Failure", "company could not be added, customer does not exist", false);
				return response;
			} // outer if

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}
		return response;

	} // add a company

	// **************************************************************************************
	// DELETE a company and all associated products + photos from the database
	// **************************************************************************************
	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_DASH
			+ Constants.PATH_DELETE_COMP, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response deleteCompany(@RequestBody CreateCompany c) {

		try {
			// make sure that the staff_email and emailID are not null
			if (c.getStaff_email() == null || (c.getCompanyID() < 1)) {
				response = Response.build("Error", "Mandatory fields, staff_email or companyID, cannot be null", false);
				return response;
			} // null fields

			// check to see if the fields are empty
			if (c.getStaff_email().contentEquals("") || c.getCompanyID() == 0) {
				response = Response.build("Error", "Mandatory fields : [companyID / staff_email] cannot be empty",
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

			System.out.println("you are here ---> companyID = " + c.getCompanyID());

			// we now have to check if the company exists on the database
			if (!cpu.companyExists(c.getCompanyID())) {
				// Company does not exist
				response = Response.build("Error", "company id does not exist on the database", false);
				return response;
			}

			// we want to make sure that there is at least one company left registered to
			// the customer
			Company comp = new Company();
			comp = customerService.getCompany(c.getCompanyID());
			c.setCustomerID(comp.getCustomerID());

			int companyCount = customerService.countCompanies(c.getCustomerID());
			System.out.println("company count = " + companyCount + "  customer id = " + c.getCustomerID());

			if (companyCount < 2) {
				// Customer must have at least one company registerd under their name
				response = Response.build("Error", "There has to be at least one company registered to the customer",
						false);
				return response;
			}

			// delete procedures
			int status = customerService.deleteCompany(c.getCompanyID());

			if (status == 1) {
				response = Response.build("Success", "company and all associated records (products and product photos)"
						+ "  have successfully been deleted from the database", true);
				return response;

			} else {
				response = Response.build("Failure", "company id could not be deleted from the database", false);
				return response;

			}

		} catch (

		Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // try-catch
		return response;

	} // deleteCompany

	// **************************************************************************************
	// get all companies from the database
	// **************************************************************************************
	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_DASH
			+ Constants.PATH_GETALL_COMP, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response getUser(@RequestBody GetCompany c) {

		List<Company> companyList = new ArrayList<Company>();
		// Company company = new Company();

		try {

			// make sure that the staff_email and emailID are not null
			if (c.getStaff_email() == null) {
				response = Response.build("Error", "Mandatory field staff_email cannot be null", false);
				return response;
			} // null fields

			// check to see if the fields are empty
			if (c.getStaff_email().contentEquals("")) {
				response = Response.build("Error", "Mandatory field : staff_email cannot be empty", false);
				return response;
			} // empty fields

			// check to see if the staff user exists on the database
			if (!cpu.userExists(c.getStaff_email())) {
				response = Response.build("Error",
						"Staff email " + c.getStaff_email() + " does not exist on the database", false);
				return response;
			}

			if (c.getCustomerID() == 0) {
				// get all the companies ...

				companyList = customerService.getAllCompanies();

				response = Response.build(ResponseEnum.OK.getStatus(), Constants.SUCCESS, ResponseEnum.OK.getMessage(),
						true);
				response.setData(companyList);

			} else {
				// get all companies associated to a customer

				// check to see if customer exists
				if (cpu.customerExists(c.getCustomerID())) {
					// check for companies under customerID ...

					companyList = customerService.getCustComp(c.getCustomerID());

					// check to see if the company list is empty
					if (companyList.isEmpty()) {
						response = Response.build("Error", "Customer ID " + c.getCustomerID()
								+ " does not have any companies registered on database", false);
						return response;
					} else {

						// send the data if companies exist
						response = Response.build("Success",
								"Customer ID " + c.getCustomerID() + " has " + companyList.size() + " companies listed",
								true);
						response.setData(companyList);
					}

				} else {
					// customer does not exist
					response = Response.build("Error",
							"Customer ID " + c.getCustomerID() + " does not exist on the database", false);
					return response;
				}

			}

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // getCompanies

	// **************************************************************************************
	// update a comoany information to the database
	// **************************************************************************************
	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_DASH
			+ Constants.PATH_UPDATE_COMP, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response updateCompany(@RequestBody CreateCompany c) {

		try {

			// make sure that the staff_email and emailID are not null
			if (c.getStaff_email() == null || c.getCompanyID() == 0) {
				response = Response.build("Error", "Mandatory field [staff_email / companyID ]cannot be null", false);
				return response;
			} // null fields

			// check to see if the fields are empty
			if (c.getStaff_email().contentEquals("") || c.getCompanyID() == 0) {
				response = Response.build("Error", "Mandatory field : staff_email / companyID cannot be empty", false);
				return response;
			} // empty fields

			// check to see if the staff user exists on the database
			if (!cpu.userExists(c.getStaff_email())) {
				response = Response.build("Error", "staff email id " + c.getStaff_email() + " does not exist", false);
				return response;
			}

			// make sure that the company exists on database ...
			if (cpu.companyExists(c.getCompanyID())) {

				// if the company exists, we just update it with the amended details
				// first we must make sure that any null fields are replaced with database
				// values
				System.out.println(" Company " + c.getCompanyID() + " exists !");

				// check to see if there are any changes against the database value and copy
				// them over
				c = checkChanges(c);

				System.out.println("updating company information for company_id = " + c.getCompanyID());
				int status = customerService.updateCompany(c);

				System.out.println("status = " + status);

				if (status == 1) {
					response = Response.build("Success", "company details successfully updated to the database", true);

					// add customer's email address to the return data
					Customer r = new Customer();
					r = customerService.getCustomerDetails(c.getCustomerID());
					c.setEmail_id_cus(r.getEmailIDCus().toString());

					response.setData(c);
				} else {
					response = Response.build("Error", "company details could not be updated to the database", false);
					return response;
				} // nested if

			} else {
				// if the customer does not exist, throw an error
				response = Response.build("Error", "company does not exist on the database", false);
				return response;

			} // if company exists

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // updateCompany

	public CreateCompany checkChanges(CreateCompany c) {

		// gets the company record from database and compares
		// to see if the new data is null, and if so copy the database value over

		// get the record
		Company r = new Company();

		try {
			r = customerService.getCompany(c.getCompanyID());

			// the company ID and customer ID remain the same.
			c.setCustomerID(r.getCustomerID());

			if (c.getCompanyName() == null)
				c.setCompanyName(r.getCompanyName().toString());

			if (c.getCompanyLogo() == null)
				c.setCompanyLogo(r.getCompanyLogo());

		} catch (Exception e) {
			e.printStackTrace();
		} // try-catch

		return c;

	} // checkChanges

} // CompanyController
