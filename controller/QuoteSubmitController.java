package com.eden.api.controller;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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

import com.eden.api.controller.engine.Availability;
import com.eden.api.service.BookingService;
import com.eden.api.service.CustomerService;
import com.eden.api.service.EngineService;
import com.eden.api.service.QueryService;
import com.eden.api.service.UserService;
import com.eden.api.service.validation.LocationQueryValidation;
import com.eden.api.service.validation.ZoneValidation;
import com.eden.api.util.Constants;
import com.eden.api.util.Response;
import com.eden.api.util.ResponseEnum;
import com.edenstar.model.Company;
import com.edenstar.model.Customer;
import com.edenstar.model.Quote;
import com.edenstar.model.User;
import com.edenstar.model.booking.AddQuote;
import com.edenstar.model.booking.GetQuote;
import com.edenstar.model.booking.KioskQuery;
import com.edenstar.model.booking.forms.ApplicationForm;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class QuoteSubmitController extends BaseController {

	Response response = new Response();
	Availability availability = new Availability();
	LocationQueryValidation locValid = new LocationQueryValidation();

	ZoneValidation zoneValid = new ZoneValidation();

	@Autowired
	private EngineService cpu;

	@Autowired
	private QueryService bpu;

	@Autowired
	private BookingService bookingService;

	@Autowired
	private CustomerService customerService;

	@Autowired
	private UserService userService;

	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_BOOKING
			+ Constants.PATH_GET_MANAGERS, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response getManagersEmail() {

		List<User> managerList = new ArrayList<User>();
		List<User> managerAdminList = new ArrayList<User>();

		System.out.println("hello from get");
		try {

			managerList = userService.getAllByType("manager");
			managerAdminList = userService.getAllByType("manager_admin");

			// add the managerAdminList to managerList
			if (managerAdminList.isEmpty()) {

			} else {

				for (int i = 0; i < managerAdminList.size(); i++) {
					managerList.add(managerAdminList.get(i));

				} // nested for

			} // if

			availability.slimDownJSON(managerList);

			if (managerList.isEmpty()) {
				response = Response.build("Error", "There are no managers registered on the system", false);
				return response;
			} else {
				response = Response.build("Success", "Manager list successfully retrieved", true);
				response.setData(managerList);
			}

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // getManagerEmail

	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_BOOKING
			+ Constants.PATH_GET_CUSTOMER, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response getCustomer(@RequestBody AddQuote q) {
		Customer customer = new Customer();
		List<Company> companyList = new ArrayList<>();

		try {
			if (locValid.isNull(q, true)) {
				response = Response.build("Error", "Mandatory parameters : [staff_email_id / quoteRef] cannot be null",
						false);
				return response;
			} // isNull check

			// check to see if any of the mandatory fields are empty
			if (locValid.fieldsAreEmpty(q, true)) {
				response = Response.build("Error",
						"No data entered for mandatory parameters, either [staff_email_id / quoteRef ]", false);
				return response;
			} // isEmpty check

			// check to see if the staff user exists on the database
			if (!cpu.userExists(q.getStaff_email_id())) {
				response = Response.build("Error",
						"Staff email " + q.getStaff_email_id() + " does not exist on the database", false);
				return response;
			} // staff checks

			// check to see if the kiosk exists
			if (!cpu.quoteExists(q.getQuoteRef())) {
				response = Response.build("Error",
						"Quotation reference " + q.getQuoteRef() + " does not exist on the database", false);
				return response;
			} // quoteExists

			Quote quote = new Quote();
			quote = bookingService.getQuote(q.getQuoteRef());

			if (quote.getIsExpired() == 1) {
				// expired action ..
				response = Response.build("EXPIRED", "Quotation reference " + q.getQuoteRef() + " has expired", false);
				return response;
			}

			// once all the validation is complete, the customer details are
			// retrieved and sent to client

			customer = customerService.getCustomerDetails(quote.getCustomer_ID());
			response = Response.build("SUCCESS", "Customer details for quote ref: " + q.getQuoteRef() + " obtained successfully ",
					true);
			
			AddQuote customerDetails = new AddQuote(customer);
			companyList = customerService.getCustComp(customer.getCustomerID());
			customerDetails.setCompanyList(companyList);
			response.setData(customerDetails);

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // getCustomer

	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_BOOKING
			+ Constants.PATH_SUBMIT_QUOTE, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response submitQuote(@RequestBody AddQuote q) {
		// make sure none of the mandatory fields are null

		try {

			if (locValid.isNull(q, true, "quote") == true) {
				response = Response.build("Error",
						"Mandatory parameters : [staff_email_id / quoteRef / employee_id (manager's id)] cannot be null",
						false);
				return response;
			} // isNull check

			// check to see if any of the mandatory fields are empty
			if (locValid.fieldsAreEmpty(q, true, "quote")) {
				response = Response.build("Error",
						"No data entered for mandatory parameters, either [staff_email_id / quoteRef / employee_id (manager's id)]",
						false);
				return response;
			} // isEmpty check

			// check to see if the staff user exists on the database
			if (!cpu.userExists(q.getStaff_email_id())) {
				response = Response.build("Error",
						"Staff email " + q.getStaff_email_id() + " does not exist on the database", false);
				return response;
			} // staff checks

			// check to see if employee_id is a valid manager
			if (!cpu.managerExists(q.getEmployee_id())) {
				response = Response.build("Error",
						"Employee ID " + q.getEmployee_id() + " does not exist on the database", false);
				return response;
			} // staff checks


			// check to see if the quote exists
			if (!cpu.quoteExists(q.getQuoteRef())) {
				response = Response.build("Error",
						"Quotation reference " + q.getQuoteRef() + " does not exist on the database", false);
				return response;
			} // quoteExists

			// we now can proceed to check if the kiosk is still available for booking
			// as per the dates specified

			Quote quote = new Quote();
			quote = bookingService.getQuote(q.getQuoteRef());

			if (quote.getIsExpired() == 1) {
				// expired action ..
				response = Response.build("EXPIRED", "Quotation reference " + q.getQuoteRef() + " has expired", false);
				return response;
			}
			
			if (quote.getIsSubmitted() == 1) {
				// expired action ..
				response = Response.build("SUBMITTED", "Quotation reference " + q.getQuoteRef() + " has already been submitted for application", false);
				return response;
			}

			/// format and set dates in java format
			// we need to convert the dates from SQL format (yyyy-MM-dd)
			// into Java format (dd/MM/yyyy)
			String startDate = availability.formatDateForJava(quote.getLease_start_date().toString());
			String endDate = availability.formatDateForJava(quote.getLease_end_date().toString());
			String dateOfQuote = availability.formatDateForJava(quote.getDate_of_quote().toString());


			// we now check if the kiosk is still available for the given dates

			quote.setStart_date(startDate);
			quote.setEnd_date(endDate);
			quote.setQuote_date(dateOfQuote);

			// we need to check to see if the kiosk is still available
			KioskQuery kQuery = new KioskQuery();
			kQuery = bpu.checkAvailability(quote, q.getStaff_email_id());

			GetQuote getQuote = new GetQuote(quote);

			// set the kioskQuery information
			getQuote.setKioskQuery(kQuery);

			// get customer details
			getQuote.setCustomer(customerService.getCustomerDetails(getQuote.getCustomer_ID()));

			// get customer's company details
			List<Company> compList = new ArrayList<Company>();
			compList = customerService.getCustComp(getQuote.getCustomer_ID());
			getQuote.setCompanyList(compList);

			availability.setAvailibilityFlag(getQuote, kQuery);

			// update response according to the availability status
			switch (kQuery.getAvailability_code()) {

			case Constants.avail_c:
				response = Response.build("Attention", "Kiosk ID " + kQuery.getKioskID() + " is currently available",
						true);
				break;

			case Constants.lock_avail_c:
				response = Response.build("Attention", "Kiosk ID " + kQuery.getKioskID() + " is currently locked",
						false);
				break;

			case Constants.not_avail_c:
				response = Response.build("Attention", "Kiosk ID " + kQuery.getKioskID() + " is now fully booked",
						false);
				break;

			case Constants.part_avail_c:
				response = Response.build("Attention",
						"Kiosk ID " + kQuery.getKioskID() + " is partly booked for dates specified", false);
				break;

			case Constants.void_avail_c:
				response = Response.build("Attention", "Kiosk ID " + kQuery.getKioskID() + " is currently closed/void",
						false);
				break;

			} // switch statement

			// extend the expiry days for application
//			getQuote.setExpiry_duration_days(getQuote.getExpiry_duration_days());
			
			// we need to slim down the JSON response to make it relevant and faster
			availability.slimDownJSON(getQuote);

			// create a new application and submit to database

			// set the date of application
			// we need to set the current date - the date of the quote
			Instant instant = Instant.now();
			ZoneId zoneId_Dubai = ZoneId.of("Asia/Dubai");
			ZonedDateTime zdt_Dubai = ZonedDateTime.ofInstant(instant, zoneId_Dubai);

			q.setDate_of_application(zdt_Dubai.toLocalDate()); // set current date
			q.setQuote_id(quote.getQuote_ID());

			// if the comments are null, initialise them
			if (q.getComments() == null) {
				q.setComments("");
			}

			int sales_id = userService.getUserDetails(q.getStaff_email_id()).getEmployeeID();
			q.setSales_id(sales_id);
			// we create an application and obtain the application id
			int application_id = bookingService.addApplication(q);
			getQuote.setApplication_id(application_id);

			// we now have all the necessary information to submit an application to the to
			// manager via email
			// we now have to email the quotation form to the customer

			User manager_details = userService.getUserDetailsByEmpID(q.getEmployee_id());
			// QuotationForm quoteForm = availability.generateQuoteEmail(q, staff_details);
			ApplicationForm applicationForm = new ApplicationForm();

			// generate booking report for email
			String applicationDetails = bpu.generateApplicationReport(getQuote, manager_details, quote, q);

			String messageBody = applicationForm.toString() + "\n" + applicationDetails;

			// we now have to email the quotation to the customer
			boolean emailSent = bookingService.emailApplicationToManager(manager_details.getEmailID(), messageBody);

			// all data has been generated, we can now email the quote to the customer
			response = Response.build("Success", "Quotation successfully generated and emailed to the manager",
					emailSent);

			availability.slimDownJSON(getQuote);
			response.setData(getQuote);
			
			// we need to set the is_submitted flag
			bookingService.setIsSubmittedFlag(q.getQuote_id(), true);
			

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // getQuote

} // QuoteSubmitController
