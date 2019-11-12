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
import com.eden.api.controller.engine.KeyGenerator;
import com.eden.api.service.BookingService;
import com.eden.api.service.CustomerService;
import com.eden.api.service.EngineService;
import com.eden.api.service.LocationService;
import com.eden.api.service.QueryService;
import com.eden.api.service.UserService;
import com.eden.api.service.validation.CustomerValidation;
import com.eden.api.service.validation.LocationQueryValidation;
import com.eden.api.service.validation.ZoneValidation;
import com.eden.api.util.Constants;
import com.eden.api.util.Response;
import com.eden.api.util.ResponseEnum;
import com.edenstar.model.Company;
import com.edenstar.model.Customer;
import com.edenstar.model.Kiosk;
import com.edenstar.model.Quote;
import com.edenstar.model.User;
import com.edenstar.model.booking.AddQuote;
import com.edenstar.model.booking.GetQuote;
import com.edenstar.model.booking.KioskQuery;
import com.edenstar.model.booking.forms.QuotationForm;
import com.edenstar.model.dash.CreateCompany;
import com.edenstar.model.dash.CreateCustomer;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class QuoteController extends BaseController {

	Response response = new Response();
	Availability availability = new Availability();
	LocationQueryValidation locValid = new LocationQueryValidation();

	@Autowired
	private EngineService cpu;

	ZoneValidation zoneValid = new ZoneValidation();

	@Autowired
	private LocationService locationService;

	@Autowired
	private UserService userService;

	@Autowired
	private BookingService bookingService;

	@Autowired
	private CustomerService customerService;

	@Autowired
	private QueryService bpu; // [b]ooking [p]rocessing [u]nit

	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_BOOKING
			+ Constants.PATH_ADD_QUOTES, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response addQuotations(@RequestBody AddQuote q) {

		List<Kiosk> kioskIDList = new ArrayList<>();

		System.out.println("kiosk list size = " + q.getKiosk_id_list().size());
		
		try {
			// validation routines ...

			// make sure none of the mandatory fields are null
			if (locValid.isMultipleNull(q) == true) {
				response = Response.build("Error",
						"mandatory parameters : [staff_email_id / kiosk_id_list / startDate / endDate / emailIDCus / firstName / lastName] cannot be null",
						false);
				return response;
			} // isNull check

			// check to see if any of the mandatory fields are empty
			if (locValid.fieldsAreEmptyMultiple(q) == true) {
				response = Response.build("Error",
						"no data entered for mandatory parameters, either [staff_email_id / kiosk_id_list / startDate / endDate]",
						false);
				return response;
			} // isEmpty check

			// check to see if the staff user exists on the database
			if (!cpu.userExists(q.getStaff_email_id())) {
				response = Response.build("Error",
						"Staff email " + q.getStaff_email_id() + " does not exist on the database", false);
				return response;
			} // staff checks

			// make sure that the kiosk is not void or locked
			for (int i = 0; i < q.getKiosk_id_list().size(); i++) {
				Kiosk k = new Kiosk();
				k = locationService.getKioskByID(q.getKiosk_id_list().get(i).getKioskID());
				if (k.getIsLocked() == 1 || k.getIsVoid() == 1) {
					response = Response.build("Error",
							"Kiosk ID " + q.getKiosk_id_list().get(i).getKioskID() + " is not available: locked/void",
							false);
					return response;
				}
			} // for

			// check to see if the kiosk exists
			for (int i = 0; i < q.getKiosk_id_list().size(); i++) {

				if (!cpu.kioskExists(q.getKiosk_id_list().get(i).getKioskID())) {
					response = Response.build("Error",
							"Kiosk ID " + q.getKiosk_id_list().get(i).getKioskID() + " does not exist on the database",
							false);
					return response;
				} // kiosk checks
			} // for

			// check dates
			String dateError = locValid.checkDates(q.getStartDate(), q.getEndDate());
			if (dateError == null) {
				// continue as normal ...
			} else {
				response = Response.build("Error", dateError, false);
				return response;
			}

			// we now have to obtain rate and availablity information about the kiosk
			// we create a quote and map relevant information to it
			List<KioskQuery> k_query_list = new ArrayList<>();

			for (int i = 0; i < q.getKiosk_id_list().size(); i++) {

				Quote quote = new Quote();

				quote.setKiosk_ID(q.getKiosk_id_list().get(i).getKioskID());
				quote.setLease_duration_days(availability.getDuration(q.getStartDate(), q.getEndDate()));
				quote.setStart_date(q.getStartDate());
				quote.setEnd_date(q.getEndDate());

				// we need to check to see if the kiosk is still available
				KioskQuery k_query = new KioskQuery();

				k_query = bpu.checkAvailability(quote, q.getStaff_email_id());

				// fully booked ...
				if (k_query.getAvailability_status().contentEquals(Constants.not_avail)) {
					response = Response.build("Attention",
							"Kiosk ID " + q.getKiosk_id_list().get(i).getKioskID() + " is fully booked", false);
					response.setData(k_query);
					return response;
				}
				// partly booked
				if (k_query.getAvailability_status().contentEquals(Constants.part_avail)) {
					response = Response.build("Attention", "Kiosk ID " + q.getKiosk_id_list().get(i).getKioskID()
							+ " is partly booked for dates specified", false);
					response.setData(k_query);
					return response;
				}
					
				k_query.setDateList(null);
				k_query_list.add(k_query);
				// q.setQ_kiosk(k_query);
			} // for loop

			// now that we have available dates, we assign it to the addQuote data type
			q.setQ_kiosk_list(k_query_list);

			// stage 11 : check whether the user customer email exists on the database ...
			Customer c = new Customer();
			c = customerService.getCustDetails(q.getEmailIDCus().toLowerCase());

			if (c.getCustomerID() < 1) {
				// customer does not exist, need to create one with basic information ...
				CreateCustomer customer = new CreateCustomer();
				availability.mapCustomer(customer, q);

				// format the fields
				CustomerValidation cusValid = new CustomerValidation();
				customer = cusValid.formatFields(customer);

				// we now send the customer information to the database and obtain a customer id
				int customer_id = customerService.addCustomer(customer);
				System.out.println("****** the generated customer id is *****" + customer_id);

				if (customer_id > 0) {
					// response = Response.build("Success", "customer successfully added to the
					// database", true);

					// set the customer id in the company object
					customer.getCompany().setCustomerID(customer_id);

					// we have to add the company name if one is specified
					if (q.getCompanyName() == null) {

					} else {
						customer.getCompany().setCompanyName(q.getCompanyName());
					}

					// set the customer id in the customer object
					customer.setCustomerID(customer_id);

					// we now have to add the company
					int company_id = customerService.addCompany(customer);
					System.out.println(" the generated company id is = " + company_id);

					if (company_id > 0) {
						System.out.println("Customer and company successfully added");
						customer.getCompany().setCompanyID(company_id);

						// copy over the customer information
						availability.mapQuote(q, customer);

						// copy over company info
						q.setCompanyList(customerService.getCustComp(q.getCustomerID()));

						response.setData(q);

					} else {
						response = Response.build("Failure", "company could not be added to the database", false);
						return response;
					}

				} else {
					response = Response.build("Failure", "customer could not be added to the database", false);
					return response;
				} // nested if

			} else {
				// port customer details over

				availability.mapCustomer(q, c);

				// copy over company info
				q.setCompanyList(customerService.getCustComp(q.getCustomerID()));

				// if the customer specifies a new company name, this should be added to the
				// list of companies
				boolean isNewCompany = true;

				if (q.getCompanyName() == null) {

				} else {

					for (int i = 0; i < q.getCompanyList().size(); i++) {

						if (q.getCompanyName().toLowerCase()
								.equalsIgnoreCase(q.getCompanyList().get(i).getCompanyName())) {
							// set isNewCompany to false
							isNewCompany = false;
						} // if
					} // for

					if (isNewCompany) {
						// create the new company
						// set the customer id in the customer object
						CreateCompany addCompany = new CreateCompany();
						addCompany.setCompanyName(q.getCompanyName());
						addCompany.setCustomerID(q.getCustomerID());
						addCompany.setEmail_id_cus(q.getEmailIDCus());

						// we now have to add the company
						int company_id = customerService.addAnotherCompany(addCompany);
						System.out.println(" the generated company id is = " + company_id);

						if (company_id > 0) {
							System.out.println("New company successfully added, company id = " + company_id);

							// copy over company info
							q.setCompanyList(customerService.getCustComp(q.getCustomerID()));
							response.setData(q);

						} else {
							response = Response.build("Failure", "company could not be added to the database", false);
							return response;
						}

					}

				} // if outer
			}

			// set the company information over to company name
			String companyNames = "";
			for (int j = 0; j < q.getCompanyList().size(); j++) {
				companyNames = companyNames + "[" + q.getCompanyList().get(j).getCompanyName() + "]";
			}

			q.setCompanyName(companyNames);

			// now we have all the necessary information, we now need to commit the
			// information to the
			// database and obtain the quote id

			// we first need to obtain the employee id
			User u = userService.getUserDetails(q.getStaff_email_id());
			q.setEmployee_id(u.getEmployeeID());

			// we need to set the current date - the date of the quote
			Instant instant = Instant.now();
			ZoneId zoneId_Dubai = ZoneId.of("Asia/Dubai");
			ZonedDateTime zdt_Dubai = ZonedDateTime.ofInstant(instant, zoneId_Dubai);

			q.setDate_of_quote(zdt_Dubai.toLocalDate());

			// we now add a unique reference key for the customer
			String quoteRefs = "";

			for (int i = 0; i < q.getKiosk_id_list().size(); i++) {

				q.setKiosk_id(q.getKiosk_id_list().get(i).getKioskID());
				q.setQ_kiosk(q.getQ_kiosk_list().get(i));

				KeyGenerator keyGen = new KeyGenerator();
				String uniqueRef = "QUO/" + keyGen.generateKey(q.getFirstName(), q.getLastName());
				q.setQuoteRef(uniqueRef);
				quoteRefs = quoteRefs + "[ " + uniqueRef + " ]";
				// set the quote expiry in days
				q.setExpiry_duration_days(Constants.quotation_valid_for_days);

				int quote_id = bookingService.addQuote(q);

				if (quote_id < 1) {
					// quote not successfully added to the database
					response = Response.build("Failure", "Quotation failed to be added to the database", false);
					response.setData(q);
					return response;
				}

				// if the quote was succesfull we update with quote_id
				System.out.println("*** Generated quote ID = " + quote_id + " ***");

				// assign the quote to the addQuote object
				q.setQuote_id(quote_id);

				// we now have to email the quotation form to the customer
				User staff_details = userService.getUserDetails(q.getStaff_email_id());

				QuotationForm quoteForm = availability.generateQuoteEmail(q, staff_details);

				// QuotationFormNew quoteForm = new QuotationFormNew();
				String messageBody = quoteForm.toString();

				// we now have to email the quotation to the customer
				boolean emailSent = bookingService.emailQuoteToCustomer(q.getEmailIDCus(), messageBody);
				System.out.println("Email to customer sent = " + emailSent);

			} // for loop
			
			// all data has been generated, we can now email the quote to the customer
			response = Response.build("Success", "Quotation(s) + " + quoteRefs + " successfully generated and emailed to the customer",
					true);

			// slim down JSON response
			//availability.slimDownJSON(q);
			//response.setData(q);

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}
		System.out.println("Size of kioskIDList = " + kioskIDList.size());
		//response.setData(q);

		return response;

	} // addQuotations

	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_BOOKING
			+ Constants.PATH_ADD_QUOTE, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response addQuotation(@RequestBody AddQuote q) {

		try {
			// validation routines ...

			// make sure none of the mandatory fields are null
			if (locValid.isNull(q) == true) {
				response = Response.build("Error",
						"mandatory parameters : [staff_email_id / kiosk_id / startDate / endDate / emailIDCus / firstName / lastName] cannot be null",
						false);
				return response;
			} // isNull check

			// check to see if any of the mandatory fields are empty
			if (locValid.fieldsAreEmpty(q) == true) {
				response = Response.build("Error",
						"no data entered for mandatory parameters, either [staff_email_id / kiosk_id / startDate / endDate]",
						false);
				return response;
			} // isEmpty check

			// check to see if the staff user exists on the database
			if (!cpu.userExists(q.getStaff_email_id())) {
				response = Response.build("Error",
						"Staff email " + q.getStaff_email_id() + " does not exist on the database", false);
				return response;
			} // staff checks

			// check to see if the kiosk exists
			if (!cpu.kioskExists(q.getKiosk_id())) {
				response = Response.build("Error", "Kiosk ID " + q.getKiosk_id() + " does not exist on the database",
						false);
				return response;
			} // kiosk checks

			// check dates
			String dateError = locValid.checkDates(q.getStartDate(), q.getEndDate());
			if (dateError == null) {
				// continue as normal ...
			} else {
				response = Response.build("Error", dateError, false);
				return response;
			}

			// make sure that the kiosk is not void or locked
			Kiosk k = new Kiosk();
			k = locationService.getKioskByID(q.getKiosk_id());
			if (k.getIsLocked() == 1 || k.getIsVoid() == 1) {
				response = Response.build("Error", "Kiosk ID " + q.getKiosk_id() + " is not available: locked/void",
						false);
				return response;
			}

			// we now have to obtain rate and availablity information about the kiosk
			// we create a quote and map relevant information to it
			Quote quote = new Quote();
			quote.setKiosk_ID(q.getKiosk_id());
			quote.setLease_duration_days(availability.getDuration(q.getStartDate(), q.getEndDate()));
			quote.setStart_date(q.getStartDate());
			quote.setEnd_date(q.getEndDate());

			// we need to check to see if the kiosk is still available
			KioskQuery k_query = bpu.checkAvailability(quote, q.getStaff_email_id());

			// fully booked ...
			if (k_query.getAvailability_status().contentEquals(Constants.not_avail)) {
				response = Response.build("Attention", "Kiosk ID " + q.getKiosk_id() + " is fully booked", false);
				response.setData(k_query);
				return response;
			}

			// partly booked
			if (k_query.getAvailability_status().contentEquals(Constants.part_avail)) {
				response = Response.build("Attention",
						"Kiosk ID " + q.getKiosk_id() + " is partly booked for dates specified", false);
				response.setData(k_query);
				return response;
			}

			// now that we have available dates, we assign it to the addQuote data type
			q.setQ_kiosk(k_query);

			// stage 11 : check whether the user customer email exists on the database ...
			Customer c = new Customer();
			c = customerService.getCustDetails(q.getEmailIDCus().toLowerCase());

			if (c.getCustomerID() < 1) {
				// customer does not exist, need to create one with basic information ...
				CreateCustomer customer = new CreateCustomer();
				availability.mapCustomer(customer, q);

				// format the fields
				CustomerValidation cusValid = new CustomerValidation();
				customer = cusValid.formatFields(customer);

				// we now send the customer information to the database and obtain a customer id
				int customer_id = customerService.addCustomer(customer);
				System.out.println("****** the generated customer id is *****" + customer_id);

				if (customer_id > 0) {
					// response = Response.build("Success", "customer successfully added to the
					// database", true);

					// set the customer id in the company object
					customer.getCompany().setCustomerID(customer_id);

					// we have to add the company name if one is specified
					if (q.getCompanyName() == null) {

					} else {
						customer.getCompany().setCompanyName(q.getCompanyName());
					}

					// set the customer id in the customer object
					customer.setCustomerID(customer_id);

					// we now have to add the company
					int company_id = customerService.addCompany(customer);
					System.out.println(" the generated company id is = " + company_id);

					if (company_id > 0) {
						System.out.println("Customer and company successfully added");
						customer.getCompany().setCompanyID(company_id);

						// copy over the customer information
						availability.mapQuote(q, customer);

						// copy over company info
						q.setCompanyList(customerService.getCustComp(q.getCustomerID()));

						response.setData(q);

					} else {
						response = Response.build("Failure", "company could not be added to the database", false);
						return response;
					}

				} else {
					response = Response.build("Failure", "customer could not be added to the database", false);
					return response;
				} // nested if

			} else {
				// port customer details over

				availability.mapCustomer(q, c);

				// copy over company info
				q.setCompanyList(customerService.getCustComp(q.getCustomerID()));

				// if the customer specifies a new company name, this should be added to the
				// list of companies
				boolean isNewCompany = true;

				if (q.getCompanyName() == null) {

				} else {

					for (int i = 0; i < q.getCompanyList().size(); i++) {

						if (q.getCompanyName().toLowerCase()
								.equalsIgnoreCase(q.getCompanyList().get(i).getCompanyName())) {
							// set isNewCompany to false
							isNewCompany = false;
						} // if
					} // for

					if (isNewCompany == true) {
						// create the new company
						// set the customer id in the customer object
						CreateCompany addCompany = new CreateCompany();
						addCompany.setCompanyName(q.getCompanyName());
						addCompany.setCustomerID(q.getCustomerID());
						addCompany.setEmail_id_cus(q.getEmailIDCus());

						// we now have to add the company
						int company_id = customerService.addAnotherCompany(addCompany);
						System.out.println(" the generated company id is = " + company_id);

						if (company_id > 0) {
							System.out.println("New company successfully added, company id = " + company_id);

							// copy over company info
							q.setCompanyList(customerService.getCustComp(q.getCustomerID()));
							response.setData(q);

						} else {
							response = Response.build("Failure", "company could not be added to the database", false);
							return response;
						}

					}

				} // if outer
			}

			// set the company information over to company name
			String companyNames = "";
			for (int j = 0; j < q.getCompanyList().size(); j++) {
				companyNames = companyNames + "[" + q.getCompanyList().get(j).getCompanyName() + "]";
			}

			q.setCompanyName(companyNames);

			// now we have all the necessary information, we now need to commit the
			// information to the
			// database and obtain the quote id

			// we first need to obtain the employee id
			User u = userService.getUserDetails(q.getStaff_email_id());
			q.setEmployee_id(u.getEmployeeID());

			// we need to set the current date - the date of the quote
			Instant instant = Instant.now();
			ZoneId zoneId_Dubai = ZoneId.of("Asia/Dubai");
			ZonedDateTime zdt_Dubai = ZonedDateTime.ofInstant(instant, zoneId_Dubai);

			q.setDate_of_quote(zdt_Dubai.toLocalDate());

			// we now add a unique reference key for the customer
			KeyGenerator keyGen = new KeyGenerator();
			String uniqueRef = "QUO/" + keyGen.generateKey(q.getFirstName(), q.getLastName());
			q.setQuoteRef(uniqueRef);

			// set the quote expiry in days
			q.setExpiry_duration_days(Constants.quotation_valid_for_days);

			int quote_id = bookingService.addQuote(q);

			if (quote_id < 1) {
				// quote not successfully added to the database
				response = Response.build("Failure", "Quotation failed to be added to the database", false);
				response.setData(q);
				return response;
			}

			// if the quote was succesfull we update with quote_id
			System.out.println("*** Generated quote ID = " + quote_id + " ***");

			// assign the quote to the addQuote object
			q.setQuote_id(quote_id);

			// we now have to email the quotation form to the customer
			User staff_details = userService.getUserDetails(q.getStaff_email_id());
			QuotationForm quoteForm = availability.generateQuoteEmail(q, staff_details);
			// QuotationFormNew quoteForm = new QuotationFormNew();
			String messageBody = quoteForm.toString();

			// we now have to email the quotation to the customer
			boolean emailSent = bookingService.emailQuoteToCustomer(q.getEmailIDCus(), messageBody);

			// all data has been generated, we can now email the quote to the customer
			response = Response.build("Success", "Quotation successfully generated and emailed to the customer",
					emailSent);

			// slim down JSON response
			availability.slimDownJSON(q);
			response.setData(q);

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // addQuote

	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_BOOKING
			+ Constants.PATH_DELETE_QUOTE, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response deleteQuotation(@RequestBody AddQuote q) {
		// make sure none of the mandatory fields are null

		try {

			if (locValid.isNull(q, true) == true) {
				response = Response.build("Error", "Mandatory parameters : [staff_email_id / quoteRef] cannot be null",
						false);
				return response;
			} // isNull check

			// check to see if any of the mandatory fields are empty
			if (locValid.fieldsAreEmpty(q, true)) {
				response = Response.build("Error",
						"No data entered for mandatory parameters, either [staff_email_id / quoteRef]", false);
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

			// we now can proceed to delete quotation
			Quote quote = new Quote();
			quote = bookingService.getQuote(q.getQuoteRef());

			// delete procedures
			int status = bookingService.deleteQuote(quote.getQuote_ID());

			if (status == 1) {
				response = Response.build("Success",
						"Quotation " + quote.getQuoteRef() + " has been successfully deleted from the database", true);

			} else {
				response = Response.build("Failure",
						"Quotation of id " + quote.getQuote_ID() + " could not be deleted from the database", false);
				return response;

			} // if

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // deleteQuote

	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_BOOKING
			+ Constants.PATH_GET_QUOTE, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response getQuote(@RequestBody AddQuote q) {
		// make sure none of the mandatory fields are null

		try {

			if (locValid.isNull(q, true)) {
				response = Response.build("Error", "Mandatory parameters : [staff_email_id / quoteRef] cannot be null",
						false);
				return response;
			} // isNull check

			// check to see if any of the mandatory fields are empty
			if (locValid.fieldsAreEmpty(q, true)) {
				response = Response.build("Error",
						"No data entered for mandatory parameters, either [staff_email_id / quoteRef]", false);
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

			// we now can proceed to check if the kiosk is still available for booking
			// as per the dates specified

			Quote quote = new Quote();
			quote = bookingService.getQuote(q.getQuoteRef());

			if (quote.getIsExpired() == 1) {
				// expired action ..
				response = Response.build("EXPIRED", "Quotation reference " + q.getQuoteRef() + " has expired", false);
				return response;
			}

			/// format and set dates in java format
			// we need to convert the dates from SQL format (yyyy-MM-dd)
			// into Java format (dd/MM/yyyy)
			String startDate = availability.formatDateForJava(quote.getLease_start_date().toString());
			String endDate = availability.formatDateForJava(quote.getLease_end_date().toString());
			String dateOfQuote = availability.formatDateForJava(quote.getDate_of_quote().toString());

			// we have to check if the quote is still valid
			// we need to check if the quotation is expired
			if (availability.isQuoteExpired(dateOfQuote, quote.getExpiry_duration_days())) {
				// expired action ..
				response = Response.build("EXPIRED", "Quotation reference " + q.getQuoteRef() + " has expired", false);
				// set expired flag in database
				int status = bookingService.setQuoteExpiredFlag(quote.getQuote_ID());
				System.out.println("Flag set = " + status);
				return response;
			}

			// we need to check if the start date has exceeded the current date
			if (availability.isQuoteOutOfDate(startDate)) {
				// expired action ..
				response = Response.build("OUT OF DATE",
						"Quotation reference " + q.getQuoteRef() + " : start date is before current date", false);
				// set expired flag in database
				int status = bookingService.setQuoteExpiredFlag(quote.getQuote_ID());
				System.out.println("Flag set = " + status);
				return response;
			}

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
			List<Company> compList = new ArrayList<>();
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

			// we need to slim down the JSON response to make it relevant and faster
			availability.slimDownJSON(getQuote);

			response.setData(getQuote);

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // getQuote

} // QuoteController
