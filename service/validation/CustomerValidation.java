package com.eden.api.service.validation;

import java.io.File;
import java.io.IOException;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import com.edenstar.model.Customer;
import com.edenstar.model.dash.CreateCustomer;
import com.edenstar.model.dash.GetCustomer;

public class CustomerValidation {

//	@Resource(name="customerService")
//	CustomerService customerService;
//
//	@Resource(name="userService")
//	UserService userService;

	public boolean fieldsAreEmpty(CreateCustomer nCust, boolean allDetails) {

		// if only the basic information is required, only email address and first name
		// is necessary
		if (allDetails) {

			// check to make sure that the mandatory parameters are not empty
			if (nCust.getEmailIDCus().contentEquals("") || nCust.getFirstName().contentEquals("")
					|| nCust.getLastName().contentEquals("") || nCust.getAddress().contentEquals("")
					|| nCust.getPoBox().contentEquals("") || nCust.getEmirate().contentEquals("")
					|| nCust.getMobileNumber().contentEquals("")) {

				return true;

			} // if

		} else {
			// only basic details required for quote
			if (nCust.getEmailIDCus().contentEquals("") || nCust.getFirstName().contentEquals("")
					|| nCust.getLastName().contentEquals("")) {
				return true;
			}

		}

		return false;

	} // fieldsAreEmpty

	public boolean isNull(CreateCustomer c, boolean allDetails) {
		// make sure that none of the mandatory fields are null

		if (allDetails) {

			if (c.getEmailIDCus() == null || c.getFirstName() == null || c.getLastName() == null
					|| c.getAddress() == null || c.getPoBox() == null || c.getEmirate() == null
					|| c.getMobileNumber() == null) {

				return true;

			} // if

			if (c.getTradeLicence() < 0 || c.getTradeLicence() > 1)
				return true;

//			if (c.getEmailIDCus() == null)
//				return true;	
//			if (c.getFirstName() == null)
//				return true;
//			if (c.getLastName() == null)
//				return true;
//			if (c.getAddress() == null)
//				return true;
//			if (c.getPoBox() == null)
//				return true;
//			if (c.getEmirate() == null)
//				return true;
//			if (c.getMobileNumber() == null)
//				return true;

		} else {

			// check basic details if only a quote is required

			if (c.getEmailIDCus() == null || c.getFirstName() == null || c.getLastName() == null)
				return true;
		}

		return false;

	} // isNull

	public CreateCustomer formatFields(CreateCustomer c) {

		String tempStr;

		// Capitalise first and last names ...
		tempStr = c.getFirstName().toString();
		tempStr = tempStr.substring(0, 1).toUpperCase() + tempStr.substring(1).toLowerCase();
		c.setFirstName(tempStr);

		tempStr = c.getLastName().toString();
		tempStr = tempStr.substring(0, 1).toUpperCase() + tempStr.substring(1).toLowerCase();
		c.setLastName(tempStr);

		// lowercase the email_id
		tempStr = c.getEmailIDCus().toString();
		tempStr = tempStr.toLowerCase();
		c.setEmailIDCus(tempStr);

		// set the company name to customer email addeess if none specified
		if (c.getCompany().getCompanyName() == null) {
			c.getCompany().setCompanyName(c.getEmailIDCus());
		}

		if (c.getCompany().getCompanyName().contentEquals("")) {
			c.getCompany().setCompanyName(c.getEmailIDCus());
		}

		return c;

	} // formatFields

//	public boolean customerExists(String cust_email) {
//
//		Customer r = new Customer();
//		boolean c_exists = true;
//
//		try {
//			r = customerService.getCustDetails(cust_email);
//
//			// if the record does not exist we should return false
//			if (r.getEmailIDCus() == null)
//				return false;
//
//			System.out.println("cust_email = " + cust_email);
//			System.out.println("result r = " + r.getEmailIDCus());
//			// first check to see if the staff email exists on the database
//			if (!r.getEmailIDCus().contentEquals(cust_email)) {
//				// does not match
//				System.out.println("customer " + cust_email + " does not exist");
//				c_exists = false;
//			}
//
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		return c_exists;
//
//	} // customerExists

//	public boolean userExists(String staff_email) {
//
//		User r = new User();
//		boolean u_exists = true;
//
//		try {
//			System.out.println("staff_email = " + staff_email);
//			
//			r = userService.getUserDetails(staff_email); 
//			if (r.getEmailID() == null)
//				return false;
//
//
//			System.out.println("result r = " + r.getEmailID());
//
//			// first check to see if the staff email exists on the database
//			if (!r.getEmailID().contentEquals(staff_email)) {
//				// does not match
//				System.out.println("user " + staff_email + " does not exist");
//				u_exists = false;
//			}
//
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		return u_exists;
//
//	} // userExists

//	public CreateCustomer checkChanges(CreateCustomer c) {
//
//		// gets the user record from database and compares
//		// to see if the new data is null, and if so copy the database value over
//
//		// get the record
//		Customer r = new Customer();
//
//		try {
//			r = customerService.getCustDetails(c.getEmailIDCus());
//
//			c.setCustomerID(r.getCustomerID()); // this value cannot be changed
//			if (c.getAddress() == null)
//				c.setAddress(r.getAddress().toString());
//
//			if (c.getEmailIDCus() == null)
//				c.setEmailIDCus(r.getEmailIDCus().toString());
//
//			if (c.getFirstName() == null)
//				c.setFirstName(r.getFirstName().toString());
//
//			if (c.getLastName() == null)
//				c.setLastName(r.getLastName().toString());
//
//			if (c.getPoBox() == null)
//				c.setPoBox(r.getPoBox().toString());
//
//			if (c.getEmirate() == null)
//				c.setEmirate(r.getEmirate().toString());
//
//			if (c.getMobileNumber() == null)
//				c.setMobileNumber(r.getMobileNumber().toString());
//
//			if (c.getOfficeNumber() == null)
//				c.setOfficeNumber(r.getOfficeNumber().toString());
//
//			if (c.getTradeLicence() < 0 || c.getTradeLicence() > 1) {
//				c.setTradeLicence(r.getTradeLicence());
//			}
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		} // try-catch
//		
//		return c;
//
//	} // checkChanges

	public GetCustomer setCustomerDetails(Customer cust, GetCustomer c_copy) {
		// TODO Auto-generated method stub
		c_copy.setCustomerID(cust.getCustomerID());
		c_copy.setFirstName(cust.getFirstName());
		c_copy.setLastName(cust.getLastName());
		c_copy.setAddress(cust.getAddress());
		c_copy.setEmirate(cust.getEmirate());
		c_copy.setMobileNumber(cust.getMobileNumber());
		c_copy.setOfficeNumber(cust.getOfficeNumber());
		c_copy.setEmailIDCus(cust.getEmailIDCus());
		c_copy.setPoBox(cust.getPoBox());
		c_copy.setTradeLicence(cust.getTradeLicence());
		c_copy.setTradeName(cust.getTradeName());

		return c_copy;

	} // setCustomerDetails

	public void deleteDirectory(String companyName) {
		// TODO Auto-generated method stub
		String myPath = System.getProperty("user.dir");
		companyName = companyName.replace(" ", "_");
		companyName = companyName.replace("@", "_");

		System.out.println("my path is " + myPath);
		myPath = myPath + "/product_photos/" + companyName.toLowerCase();
		System.out.println("my path is " + myPath);
		try {
			File f = new File(myPath);
			if (f.exists() && f.isDirectory()) {
				FileUtils.deleteDirectory(new File(myPath));
				System.out.println("Directory " + myPath + " has been deleted");
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	} // deleteDirectory

	public void addCompanyDirectory(String companyName) {

		// replace all spaces in the company name ...
		companyName = companyName.replace(" ", "_");
		companyName = companyName.replace("@", "_");

		System.out.println("creating directory ...");

		new File("product_photos/" + companyName.toLowerCase()).mkdirs();

	}

}
