package com.eden.api.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.eden.api.controller.engine.AccountEngine;
import com.eden.api.controller.engine.Availability;
import com.eden.api.controller.engine.KeyGenerator;
import com.eden.api.service.AccountService;
import com.eden.api.service.BookingService;
import com.eden.api.service.CustomerService;
import com.eden.api.service.EngineService;
import com.eden.api.service.QueryService;
import com.eden.api.service.UserService;
import com.eden.api.service.validation.ApplicationValidation;
import com.eden.api.service.validation.ZoneValidation;
import com.eden.api.util.Constants;
import com.eden.api.util.Response;
import com.eden.api.util.ResponseEnum;
import com.edenstar.model.Account;
import com.edenstar.model.Application;
import com.edenstar.model.Booking;
import com.edenstar.model.Company;
import com.edenstar.model.Customer;
import com.edenstar.model.Lease;
import com.edenstar.model.Payment;
import com.edenstar.model.Product;
import com.edenstar.model.ProductPhoto;
import com.edenstar.model.Quote;
import com.edenstar.model.User;
import com.edenstar.model.booking.ApplicationStream;
import com.edenstar.model.booking.KioskQuery;
import com.edenstar.model.booking.ProcessApplication;
import com.edenstar.model.booking.ProductTrain;
import com.edenstar.model.booking.ViewApplication;
import com.edenstar.model.booking.forms.BookingForm;
import com.edenstar.model.booking.forms.DeclineApplication;
import com.edenstar.model.booking.forms.QuotationForm;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class ApplicationController extends BaseController {

	Response response = new Response();
	Availability availability = new Availability();
	ApplicationValidation appValid = new ApplicationValidation();

	ZoneValidation zoneValid = new ZoneValidation();

	@Autowired
	private EngineService cpu;

	@Autowired
	private QueryService bpu;

	@Autowired
	private BookingService bookingService;

	@Autowired
	private AccountService accountService;

	@Autowired
	private CustomerService customerService;

	@Autowired
	private UserService userService;

	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_BOOKING
			+ Constants.PATH_GET_APPLICATIONS, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response getApplications(@RequestBody ProcessApplication a) {

		List<ApplicationStream> applicationList = new ArrayList<ApplicationStream>();

		try {
			if (appValid.isNull(a, "")) {
				response = Response.build("Error", "Mandatory parameters : [staff_email_id] cannot be null", false);
				return response;
			} // isNull check

			// check to see if any of the mandatory fields are empty
			if (appValid.fieldsAreEmpty(a, "")) {
				response = Response.build("Error", "No data entered for mandatory parameters, either [staff_email_id]",
						false);
				return response;
			} // isEmpty check

			// check to see if the staff user exists on the database
			if (!cpu.userExists(a.getStaff_email_id())) {
				response = Response.build("Error",
						"Staff email " + a.getStaff_email_id() + " does not exist on the database", false);
				return response;
			} // staff checks

			if (!cpu.checkManagerClearance(a.getStaff_email_id())) {
				response = Response.build("Error",
						"Employee ID " + a.getStaff_email_id() + " does not have manager status", false);
				return response;
			}

			if (a.getApplicationViewMode() == null || a.getApplicationViewMode().isEmpty()) {
				response = Response.build("Error", "applicationViewMode cannot be null", false);
				return response;
			}

			// we have cleared validation, we need to obtain the application list for the
			// employee if

			switch (a.getApplicationViewMode().toLowerCase()) {
			case "*":
				System.out.println("All applications");
				applicationList = bookingService.getAllApplicationList("*");
				break;
			case "declined":
				System.out.println("All declined applications");
				applicationList = bookingService.getAllApplicationList("declined");
				break;
			case "approved":
				System.out.println("All approved applications");
				applicationList = bookingService.getAllApplicationList("approved");
				break;
			case "expired":
				System.out.println("All expired applications");
				applicationList = bookingService.getAllApplicationList("expired");
				break;
			case "pending":
				System.out.println("All pending applications");
				applicationList = bookingService.getAllApplicationList("pending");
				break;
			case "review":
				System.out.println("All applications pending review");
				applicationList = bookingService.getAllApplicationList("review");
				break;
			case "company":
				System.out.println("All applications by company");
				applicationList = bookingService.getAllApplicationList("company");
				break;
			case "company_pending":
				System.out.println("All applications by company pending approval");
				applicationList = bookingService.getAllApplicationList("company_pending");
				break;

			default:
				System.out.println("All applications under manager logged in");
				applicationList = bookingService
						.getApplicationList(userService.getUserDetails(a.getStaff_email_id()).getEmployeeID());

			} // switch

			// add a list of companies for each customer
//			for (int i = 0; i < applicationList.size(); i++) {
//				int customer_id = applicationList.get(i).getCustomer_id();
//
//				applicationList.get(i).setCompanyList(customerService.getCustComp(customer_id));
//			}

			response = Response.build("Success", applicationList.size() + " applications successfully retrieved", true);
			response.setData(applicationList);

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // getApplications

	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_BOOKING
			+ Constants.PATH_VIEW_APPLICATION, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response viewApplication(@RequestBody ProcessApplication a) {

		Application application = new Application();
		String applicationStatus = "";

		try {
			if (appValid.isNull(a, "getapplication") == true) {
				response = Response.build("Error",
						"Mandatory parameters : [staff_email_id / application_id] cannot be null", false);
				return response;
			} // isNull check

			// check to see if any of the mandatory fields are empty
			if (appValid.fieldsAreEmpty(a, "getapplication") == true) {
				response = Response.build("Error",
						"No data entered for mandatory parameters, either [staff_email_id / application_id]", false);
				return response;
			} // isEmpty check

			// check that tha application exists
			if (!cpu.applicationExists(a.getApplication_id())) {
				response = Response.build("Error",
						"Application ID " + a.getApplication_id() + " does not exist on the database", false);
				return response;
			} // applicationExists

			// check to see if the staff user exists on the database
			if (!cpu.userExists(a.getStaff_email_id())) {
				response = Response.build("Error",
						"Staff email " + a.getStaff_email_id() + " does not exist on the database", false);
				return response;
			} // staff checks

			if (!cpu.checkManagerClearance(a.getStaff_email_id())) {
				response = Response.build("Error",
						"Employee ID " + a.getStaff_email_id() + " does not have manager status", false);
				return response;
			}

			// we have to check that the application is still valid
			application = bookingService.getApplication(a.getApplication_id());
			Quote quote = new Quote();
			quote = bookingService.getQuote(application.getQuoteID());

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

			if (application.getIsExpired() == 1) {
				// expired action ..
				applicationStatus = "APPLICATION EXPIRED";
//				response = Response.build("EXPIRED", "Application has expired", false);
//				return response;
			}

			// check if kiosk is still available for the given dates
			if (!bpu.isKioskAvailable(quote, a.getStaff_email_id())) {
				applicationStatus = applicationStatus + " * BOOKING DATES NO LONGER AVAILABLE * ";

//				response = Response.build("DATES NOT AVAILABLE",
//						"Kiosk " + quote.getKiosk_ID() + " is no longer available for booking dates ["
//								+ quote.getStart_date() + " to " + quote.getEnd_date() + "]",
//						false);
//				return response;
			}

			// now all checks have been performed, we can now diplay full details in a mega
			// data stream
			// called ViewApplication

			// get customer details
			Customer c = new Customer();
			c = customerService.getCustomerDetails(quote.getCustomer_ID());
			ViewApplication applicationStreamOut = new ViewApplication(c);

			// set company details
			// first must populate the number of companies and assign it to a list
			List<Company> companyList = customerService.getCustComp(quote.getCustomer_ID());

			// we assign the company list to the applicationStreamOut data train
			applicationStreamOut.setCompanyList(companyList);
			List<ProductTrain> productTrainList = new ArrayList<>();

			for (int i = 0; i < companyList.size(); i++) {
				List<Product> productList = new ArrayList<>();

				productList = customerService.getProducts(companyList.get(i).getCompanyID());
				for (int j = 0; j < productList.size(); j++) {
					List<ProductPhoto> productPhotoList = customerService
							.getProductPhoto(productList.get(j).getProductID());
					ProductTrain productTrain = new ProductTrain(productList.get(j));
					productTrain.setProductPhotosList(productPhotoList);
					productTrainList.add(productTrain);

				} // product cycle

			} // company cycle

			// set the productTrainList to output stream
			applicationStreamOut.setProductList(productTrainList);

			// set the quote information
			applicationStreamOut.setQuote(quote);

			// get the application information
			applicationStreamOut.setApplication(application);

			// obtain kiosk information
			KioskQuery kQuery = new KioskQuery();
			kQuery = bpu.checkAvailability(quote, a.getStaff_email_id());

			kQuery.setDateList(null);

			applicationStreamOut.setKiosk_info(kQuery);
			applicationStreamOut.setApplicationStatus(applicationStatus);

			response = Response.build("Success", "Application id : " + a.getApplication_id() + " successfully obtained",
					true);
			response.setData(applicationStreamOut);

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // viewApplication

	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_BOOKING
			+ Constants.PATH_APPROVE_APPLICATION, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response approveApplication(@RequestBody ProcessApplication a) {

		Application application = new Application();

		try {

			if (appValid.isNull(a, "getapplication") == true) {
				response = Response.build("Error",
						"Mandatory parameters : [staff_email_id / application_id] cannot be null", false);
				return response;
			} // isNull check

			// check to see if any of the mandatory fields are empty
			if (appValid.fieldsAreEmpty(a, "getapplication") == true) {
				response = Response.build("Error",
						"No data entered for mandatory parameters, either [staff_email_id / application_id]", false);
				return response;
			} // isEmpty check

			// check that tha application exists
			if (!cpu.applicationExists(a.getApplication_id())) {
				response = Response.build("Error",
						"Application ID " + a.getApplication_id() + " does not exist on the database", false);
				return response;
			} // applicationExists

			// check to see if the staff user exists on the database
			if (!cpu.userExists(a.getStaff_email_id())) {
				response = Response.build("Error",
						"Staff email " + a.getStaff_email_id() + " does not exist on the database", false);
				return response;
			} // staff checks

			if (!cpu.checkManagerClearance(a.getStaff_email_id())) {
				response = Response.build("Error",
						"Employee ID " + a.getStaff_email_id() + " does not have manager status", false);
				return response;
			}

			// we have to check that the application is still valid
			application = bookingService.getApplication(a.getApplication_id());
			Quote quote = new Quote();
			quote = bookingService.getQuote(application.getQuoteID());

			/// format and set dates in java format
			// we need to convert the dates from SQL format (yyyy-MM-dd)
			// into Java format (dd/MM/yyyy)
			String startDate = availability.formatDateForJava(quote.getLease_start_date().toString());
			String endDate = availability.formatDateForJava(quote.getLease_end_date().toString());
			String dateOfQuote = availability.formatDateForJava(quote.getDate_of_quote().toString());
			// String dateOfApplication =
			// availability.formatDateForJava(application.getDateOfApplication().toString());

			// we now check if the application is expired

			quote.setStart_date(startDate);
			quote.setEnd_date(endDate);
			quote.setQuote_date(dateOfQuote);

			if (application.getIsExpired() == 1) {
				// expired action ..
				response = Response.build("EXPIRED", "Application has expired", false);
				return response;
			}

			// check if kiosk is still available for the given dates
			if (!bpu.isKioskAvailable(quote, a.getStaff_email_id())) {
				response = Response.build("DATES NOT AVAILABLE",
						"Kiosk " + quote.getKiosk_ID() + " is no longer available for booking dates ["
								+ quote.getStart_date() + " to " + quote.getEnd_date() + "]",
						false);
				return response;
			}

			// we can now proceed to log a booking in the database against the kiosk
			// calendar

			// generate booking reference first

			Customer customer = customerService.getCustomerDetails(quote.getCustomer_ID());

			// we now add a unique booking reference key for the customer
			KeyGenerator keyGen = new KeyGenerator();
			String uniqueRef = "BK/" + keyGen.generateKey(customer.getFirstName(), customer.getLastName());

			// we create the new booking data train
			Booking booking = new Booking(quote);
			booking.setBooking_ref(uniqueRef);

			// add the application to the booking
			booking.setApplication(application);

			// insert booking dates into kiosk calendar
			int calendar_id = bookingService.addDatesToKioskCalendar(booking);

			if (calendar_id == 0) {
				response = Response.build("Error", "Booking dates failed to update to kiosk calendar on database",
						false);
				return response;
			}

			booking.setCalendar_id(calendar_id);

			// set current date as date of booking
			booking.setDate_of_booking(new Date());
			String dateOfBookingStr = new SimpleDateFormat("dd/MM/yyyy").format(booking.getDate_of_quote());
			booking.setBooking_date(dateOfBookingStr);

			// we will add the information into the booking table and get a generated id
			int booking_id = bookingService.addBooking(booking);

			if (booking_id == 0) {
				response = Response.build("Error", "Booking dates failed to be added to the database", false);
				return response;
			}

			// *** we have to create a lease in the lease table using the booking id ***
			Lease lease = new Lease();
			lease.setBooking_id(booking_id);

			int lease_id = bookingService.addLease(lease);

			if (lease_id == 0) {
				response = Response.build("Error", "Lease failed to be added to the database", false);
				return response;
			}

			// we have to now create a new account
			Account account = new Account();
			account.setLease_id(lease_id);
			account.setLeaseTotal(quote.getLease_total());
			account.setLeaseRemaining(quote.getLease_total());

			int account_id = accountService.addAccount(account);

			if (account_id == 0) {
				response = Response.build("Error", "Account failed to be added to the database", false);
				return response;
			}

			// we have both a leasing and account row in the database,
			// we now need to generate the payment schedule
			AccountEngine accountsEngine = new AccountEngine();

			List<Payment> paymentSchedule = accountsEngine.generatePaymentSchedule(booking.getStart_date(),
					booking.getEnd_date(), booking.getLease_total(), account_id);

			if (paymentSchedule.isEmpty() || paymentSchedule.size() == 0) {
				response = Response.build("Error", "Payment schedule is empty !", false);
				return response;
			}

			int status = 0;

			for (int i = 0; i < paymentSchedule.size(); i++) {
				status = accountService.addPayment(paymentSchedule.get(i));

				if (status == 0) {
					System.out.println(
							"payment " + paymentSchedule.get(i).toString() + " failed to be added to the database");
				} else {
					paymentSchedule.get(i).setPayment_id(status);
				}
				System.out.println(paymentSchedule.get(i).toString());
			}

			// update the number of payments required in account
			accountService.updateNumOfPaymentsRem(account_id, paymentSchedule.size());

			// calculate the security deposit
			double securityDeposit = 0.00;// round(booking.getLease_total() * Constants.securityDepositPercentage,2);
			account.setDepositAmount(securityDeposit);

			// update acccounts with security deposit
			accountService.updateSecurityDeposit(account_id, securityDeposit);

			booking.setPaymentSchedule(paymentSchedule);
			booking.setAccount(account);
			booking.setLease(lease);

			booking.setBooking_id(booking_id);
			int bookingFlagStatus = bookingService.setIsApprovedFlag(a.getApplication_id(), true);
			System.out.println("Booking flag status = " + bookingFlagStatus);

			// now that we have completed the booking, the quote and application can be
			// deleted
			// delete / backup procedures here ....

			// obtain the kiosk details for the email to the customer
			// obtain kiosk information
			KioskQuery kQuery = bpu.checkAvailability(quote, a.getStaff_email_id());

			kQuery.setDateList(null);

			// an email with the booking details will be sent to the successful customer
			BookingForm bookingForm = new BookingForm();

			// generate booking email
			String applicationDetails = bpu.generateBookingEmail(booking, customer, kQuery);

			String messageBody = bookingForm.toString() + "\n" + applicationDetails;

			// we now have to email the quotation to the customer
			boolean emailSent = bookingService.emailBookingToCustomer(customer.getEmailIDCus(), messageBody);

			// we copy the quote information into booking, so if the quote is deleted all
			// information
			// is retained in the booking table
			response = Response.build("Success",
					"Booking dates successfully entered onto the database and email sent to customer", emailSent);
			response.setData(booking);

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // approveapplication

	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_BOOKING
			+ Constants.PATH_DELETE_APPLICATION, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response deleteApplication(@RequestBody ProcessApplication a) {
		// make sure none of the mandatory fields are null

		try {
			if (appValid.isNull(a, "getapplication") == true) {
				response = Response.build("Error",
						"Mandatory parameters : [staff_email_id / application_id] cannot be null", false);
				return response;
			} // isNull check

			// check to see if any of the mandatory fields are empty
			if (appValid.fieldsAreEmpty(a, "getapplication") == true) {
				response = Response.build("Error",
						"No data entered for mandatory parameters, either [staff_email_id / application_id]", false);
				return response;
			} // isEmpty check

			// check that tha application exists
			if (!cpu.applicationExists(a.getApplication_id())) {
				response = Response.build("Error",
						"Application ID " + a.getApplication_id() + " does not exist on the database", false);
				return response;
			} // applicationExists

			// check to see if the staff user exists on the database
			if (!cpu.userExists(a.getStaff_email_id())) {
				response = Response.build("Error",
						"Staff email " + a.getStaff_email_id() + " does not exist on the database", false);
				return response;
			} // staff checks

			if (!cpu.checkManagerClearance(a.getStaff_email_id())) {
				response = Response.build("Error",
						"Employee ID " + a.getStaff_email_id() + " does not have manager status", false);
				return response;
			}

			// delete procedures
			int status = bookingService.deleteApplication(a.getApplication_id());

			if (status == 1) {
				response = Response.build("Success",
						"Application " + a.getApplication_id() + " has been successfully deleted from the database",
						true);

			} else {
				response = Response.build("Failure",
						"Application id " + a.getApplication_id() + " could not be deleted from the database", false);
				return response;

			} // if

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // deleteApplication

	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_BOOKING
			+ Constants.PATH_LOCK_KIOSK, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response lockKiosk(@RequestBody ProcessApplication a) {

		try {

			if (appValid.isNull(a, "kiosk") == true) {
				response = Response.build("Error",
						"Mandatory parameters : [staff_email_id / kiosk_id / lock_kiosk (= 1 or 0) ] cannot be null",
						false);
				return response;
			} // isNull check

			// check to see if any of the mandatory fields are empty
			if (appValid.fieldsAreEmpty(a, "kiosk") == true) {
				response = Response.build("Error",
						"No data entered for mandatory parameters, either [staff_email_id / kiosk_id]", false);
				return response;
			} // isEmpty check

			// check that tha application exists
			if (!cpu.kioskExists(a.getKiosk_id())) {
				response = Response.build("Error", "Kiosk ID " + a.getKiosk_id() + " does not exist on the database",
						false);
				return response;
			} // kioskExists

			// check to see if the staff user exists on the database
			if (!cpu.userExists(a.getStaff_email_id())) {
				response = Response.build("Error",
						"Staff email " + a.getStaff_email_id() + " does not exist on the database", false);
				return response;
			} // staff checks

			if (!cpu.checkManagerClearance(a.getStaff_email_id())) {
				response = Response.build("Error",
						"Employee ID " + a.getStaff_email_id() + " does not have manager status", false);
				return response;
			}

			Boolean lockStatus = false;
			String lockStat = "UNLOCKED";

			// set boolean flag
			if (a.getLock_kiosk() == 1) {
				lockStatus = true;
				lockStat = "LOCKED";
			}

			// delete procedures
			int status = bookingService.setKioskLock(a.getKiosk_id(), lockStatus);

			if (status == 1) {
				response = Response.build("Success", "Kiosk " + a.getKiosk_id() + " has been successfully " + lockStat,
						true);

			} else {
				response = Response.build("Failure", "Kiosk id " + a.getKiosk_id() + " could not be " + lockStat,
						false);
				return response;

			} // if

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;
	} // lockKiosk

	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_BOOKING
			+ Constants.PATH_DECLINE_APPLICATION, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response declineApplication(@RequestBody ProcessApplication a) {

		Application application = new Application();

		try {

			if (appValid.isNull(a, "getapplication") == true) {
				response = Response.build("Error",
						"Mandatory parameters : [staff_email_id / application_id] cannot be null", false);
				return response;
			} // isNull check

			// check to see if any of the mandatory fields are empty
			if (appValid.fieldsAreEmpty(a, "getapplication") == true) {
				response = Response.build("Error",
						"No data entered for mandatory parameters, either [staff_email_id / application_id]", false);
				return response;
			} // isEmpty check

			// check that tha application exists
			if (!cpu.applicationExists(a.getApplication_id())) {
				response = Response.build("Error",
						"Application ID " + a.getApplication_id() + " does not exist on the database", false);
				return response;
			} // applicationExists

			// check to see if the staff user exists on the database
			if (!cpu.userExists(a.getStaff_email_id())) {
				response = Response.build("Error",
						"Staff email " + a.getStaff_email_id() + " does not exist on the database", false);
				return response;
			} // staff checks

			if (!cpu.checkManagerClearance(a.getStaff_email_id())) {
				response = Response.build("Error",
						"Employee ID " + a.getStaff_email_id() + " does not have manager status", false);
				return response;
			}

			// we have to check that the application is still valid
			application = bookingService.getApplication(a.getApplication_id());
			Quote quote = new Quote();
			quote = bookingService.getQuote(application.getQuoteID());

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

			Customer customer = customerService.getCustomerDetails(quote.getCustomer_ID());

			// an email with the booking details will be sent to the successful customer
			DeclineApplication declineBookingEmailHeader = new DeclineApplication();

			// generate booking email
			String applicationDetails = bpu.generateDeclineBookingEmail(quote, customer);

			String messageBody = declineBookingEmailHeader.toString() + "\n" + applicationDetails;

			// we now have to email the quotation to the customer
			boolean emailSent = bookingService.emailDeclinedApplicationToCustomer(customer.getEmailIDCus(),
					messageBody);

			// we set the isExpired flag on the quote
			int expired_flag = bookingService.setQuoteExpiredFlag(quote.getQuote_ID());
			System.out.println("quote expired flag set =" + expired_flag);
			// and the expired flag on the application
			expired_flag = bookingService.setApplicationExpiredFlag(a.getApplication_id(), true);
			// and the declined flag
			int declined_flag = bookingService.setApplicationDeclinedFlag(a.getApplication_id(), true);

			System.out.println("Expired flag set = " + expired_flag + "  Declined flag set = " + declined_flag);

			// we copy the quote information into booking, so if the quote is deleted all
			// information
			// is retained in the booking table
			response = Response.build("Success", "Application successfully declined and email sent out to applicant",
					emailSent);

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // declineapplication

	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_BOOKING
			+ Constants.PATH_UPLOAD_DEPOSIT, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response uploadDeposit(@RequestBody ProcessApplication a) {

		try {

			if (a.getQuoteRef() != null) {
				// obtain application id from quote reference
				Application application = bookingService.getApplicationByQuoteRef(a.getQuoteRef());
				System.out.println("application id = " + application.getApplicationID() + " for quoteRef " + a.getQuoteRef());
				
				if (application.getApplicationID() == 0) {
					response = Response.build("Error",
							"Application ID " + a.getApplication_id() + " does not exist on the database", false);
					return response;
				}

				a.setApplication_id(application.getApplicationID());
			}

			if (appValid.isNull(a, "deposit") == true) {
				response = Response.build("Error",
						"Mandatory parameters : [staff_email_id / application_id [or quoteRef] / deposit_scan] cannot be null",
						false);
				return response;
			} // isNull check

			// check to see if any of the mandatory fields are empty
			if (appValid.fieldsAreEmpty(a, "deposit") == true) {
				response = Response.build("Error",
						"No data entered for mandatory parameters, either [staff_email_id / application_id [or quoteRef] / deposit_scan]",
						false);
				return response;
			} // isEmpty check

			// check that tha application exists
			if (!cpu.applicationExists(a.getApplication_id())) {
				response = Response.build("Error",
						"Application ID " + a.getApplication_id() + " does not exist on the database", false);
				return response;
			} // applicationExists

			// check to see if the staff user exists on the database
			if (!cpu.userExists(a.getStaff_email_id())) {
				response = Response.build("Error",
						"Staff email " + a.getStaff_email_id() + " does not exist on the database", false);
				return response;
			} // staff checks

			// we now have the scanned deposit, we shall upload it to the server
			// if an image of the product has been included we must add the url to the
			// database and save it on the server

			Application application = bookingService.getApplication(a.getApplication_id());
			Quote quote = bookingService.getQuote(application.getQuoteID());
			String urlToDepositScan = "";

			if (a.getDeposit_scan() == null) {

			} else {
				urlToDepositScan = appValid.writeImageStreamToFile(a.getDeposit_scan(), quote.getQuoteRef());
				System.out.println("the deposit scan url is = " + urlToDepositScan);
				a.setDeposit_scan_url(urlToDepositScan);
				a.setDeposit_scan(null);
			}

			// now we update the application with the deposit url
			int updateStatus = bookingService.updateDepositURL(a.getApplication_id(), urlToDepositScan);

			if (updateStatus == 0) {
				response = Response.build("Failure", "Deposit URL was not added to the database", false);
				return response;
			} else {
				response = Response.build("Success", "Deposit URL was successfully added to the database", true);
			}

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // uploadDeposit

	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_BOOKING
			+ Constants.PATH_GET_QUOTES, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response getQuotes(@RequestBody ProcessApplication a) {

		List<Quote> quoteList = new ArrayList<Quote>();
		List<Quote> expiredList = new ArrayList<Quote>();

		try {
			if (appValid.isNull(a, "")) {
				response = Response.build("Error", "Mandatory parameters : [staff_email_id] cannot be null", false);
				return response;
			} // isNull check

			// check to see if any of the mandatory fields are empty
			if (appValid.fieldsAreEmpty(a, "")) {
				response = Response.build("Error", "No data entered for mandatory parameters, either [staff_email_id]",
						false);
				return response;
			} // isEmpty check

			// check to see if the staff user exists on the database
			if (!cpu.userExists(a.getStaff_email_id())) {
				response = Response.build("Error",
						"Staff email " + a.getStaff_email_id() + " does not exist on the database", false);
				return response;
			} // staff checks

			// we have cleared validation, we need to obtain the application list for the
			// employee if
			quoteList = bookingService.getQuoteList();

			if (a.getShow_expired() == 0) {

			} else {

				expiredList = bookingService.getExpiredQuotes("deleted");

//				for (int i = 0; i < quoteList.size(); i++) {
//
//					if (quoteList.get(i).getIsExpired() == 1) {
//
//						Quote q = new Quote(quoteList.get(i));
//						expiredList.add(q);
//
//					} // if
//				} // for

				response = Response.build("Success",
						expiredList.size() + " expired quotes successfully obtained from database", true);
				response.setData(expiredList);
				return response;

			}

			response = Response.build("Success", quoteList.size() + " valid quotes successfully obtained from database",
					true);
			response.setData(quoteList);

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // getQuotes

	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_BOOKING
			+ Constants.PATH_REVISE_APPLICATION, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response reviseApplication(@RequestBody ProcessApplication a) {

		Application application = new Application();

		try {

			if (appValid.isNull(a, "getapplication") == true) {
				response = Response.build("Error",
						"Mandatory parameters : [staff_email_id / application_id] cannot be null", false);
				return response;
			} // isNull check

			// check to see if any of the mandatory fields are empty
			if (appValid.fieldsAreEmpty(a, "getapplication") == true) {
				response = Response.build("Error",
						"No data entered for mandatory parameters, either [staff_email_id / application_id]", false);
				return response;
			} // isEmpty check

			if (a.getLease_total() == 0) {
				response = Response.build("Error", "No data entered for mandatory parameters lease_total", false);
				return response;
			} // check lease total

			// check that tha application exists
			if (!cpu.applicationExists(a.getApplication_id())) {
				response = Response.build("Error",
						"Application ID " + a.getApplication_id() + " does not exist on the database", false);
				return response;
			} // applicationExists

			// check to see if the staff user exists on the database
			if (!cpu.userExists(a.getStaff_email_id())) {
				response = Response.build("Error",
						"Staff email " + a.getStaff_email_id() + " does not exist on the database", false);
				return response;
			} // staff checks

			if (!cpu.checkManagerClearance(a.getStaff_email_id())) {
				response = Response.build("Error",
						"Employee ID " + a.getStaff_email_id() + " does not have manager status", false);
				return response;
			}

			// we have to check that the application is still valid
			application = bookingService.getApplication(a.getApplication_id());
			Quote quote = bookingService.getQuote(application.getQuoteID());

			/// format and set dates in java format
			// we need to convert the dates from SQL format (yyyy-MM-dd)
			// into Java format (dd/MM/yyyy)
			String startDate = availability.formatDateForJava(quote.getLease_start_date().toString());
			String endDate = availability.formatDateForJava(quote.getLease_end_date().toString());
			String dateOfQuote = availability.formatDateForJava(quote.getDate_of_quote().toString());
			String dateOfApplication = availability.formatDateForJava(application.getDateOfApplication().toString());

			// we now check if the application is expired

			if (availability.isApplicationExpired(dateOfApplication, quote.getExpiry_duration_days())) {
				// expired action ..
				response = Response.build("EXPIRED", "Application reference " + a.getApplication_id() + " has expired",
						false);
				// set expired flag in database
				int status = bookingService.setApplicationExpiredFlag(a.getApplication_id(), true);
				System.out.println("Application expiry flag set = " + status);
				return response;
			}

			quote.setStart_date(startDate);
			quote.setEnd_date(endDate);
			quote.setQuote_date(dateOfQuote);

			if (application.getIsExpired() == 1) {
				// expired action ..
				response = Response.build("EXPIRED", "Application has expired", false);
				return response;
			}

			if (availability.isQuoteOutOfDate(startDate)) {
				// expired action ..
				response = Response.build("OUT OF DATE",
						"Application reference " + a.getApplication_id() + " : start date is before current date",
						false);
				// set expired flag in database
				int status = bookingService.setQuoteExpiredFlag(quote.getQuote_ID());
				System.out.println("Flag set = " + status);
				return response;
			}

			// check if kiosk is still available for the given dates
			if (!bpu.isKioskAvailable(quote, a.getStaff_email_id())) {
				response = Response.build("DATES NOT AVAILABLE",
						"Kiosk " + quote.getKiosk_ID() + " is no longer available for booking dates ["
								+ quote.getStart_date() + " to " + quote.getEnd_date() + "]",
						false);
				return response;
			}

			// once all the checks have been validated, we can check for any changes to the
			// quote that the manager wishes to make and commit it to the database
			availability.reviseApplication(a.getLease_total(), quote);

			// we have to commit the new leasing price to the database
			int update_status = bookingService.updateRevisedLease(quote, application);

			if (update_status == 0) {
				response = Response.build("Error", "Revised price was not updated", false);
				return response;
			}

			// *** email the revised quote to the customer ***

			// we now have to email the quotation form to the customer
			User staff_details = userService.getUserDetails(a.getStaff_email_id());

			// get customer details
			Customer customer = customerService.getCustomerDetails(quote.getCustomer_ID());

			// get company details
			List<Company> companyList = customerService.getCustComp(quote.getCustomer_ID());

			// we need to check to see if the kiosk is still available
			KioskQuery k_query = bpu.checkAvailability(quote, a.getStaff_email_id());

			QuotationForm quoteForm = availability.generateQuoteRevisedEmail(quote, staff_details, companyList, k_query,
					customer);

			// QuotationFormNew quoteForm = new QuotationFormNew();
			String messageBody = quoteForm.toString();

			// we now have to email the quotation to the customer
			boolean emailSent = bookingService.emailQuoteToCustomer(customer.getEmailIDCus(), messageBody);
			System.out.println("Revised email to customer sent = " + emailSent);

			response = Response.build("Success", "Quote revision updated successfully and email sent out to customer",
					true);

			// we now have to switch the review flag off
			int status_update = bookingService.setApplicationReviewFlag(a.getApplication_id(), false);

			if (status_update == 0)
				System.out.println("update review flag failed");

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // revise application

	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_BOOKING
			+ Constants.PATH_ADD_COMMENTS, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response addCommentToApplication(@RequestBody ProcessApplication a) {

		try {

			if (appValid.isNull(a, "getapplication") == true) {
				response = Response.build("Error",
						"Mandatory parameters : [staff_email_id / application_id] cannot be null", false);
				return response;
			} // isNull check

			// check to see if any of the mandatory fields are empty
			if (appValid.fieldsAreEmpty(a, "getapplication") == true) {
				response = Response.build("Error",
						"No data entered for mandatory parameters, either [staff_email_id / application_id]", false);
				return response;
			} // isEmpty check

			if (a.getComments() == null || a.getComments().isEmpty()) {
				response = Response.build("Error", "No data entered for mandatory parameters comments", false);
				return response;
			} // check comments

			// check that tha application exists
			if (!cpu.applicationExists(a.getApplication_id())) {
				response = Response.build("Error",
						"Application ID " + a.getApplication_id() + " does not exist on the database", false);
				return response;
			} // applicationExists

			// check to see if the staff user exists on the database
			if (!cpu.userExists(a.getStaff_email_id())) {
				response = Response.build("Error",
						"Staff email " + a.getStaff_email_id() + " does not exist on the database", false);
				return response;
			} // staff checks

			// get previous comments already on file
			Application application = bookingService.getApplication(a.getApplication_id());
			String previousComments = application.getComments();

			// get employee information for timestamp
			User u = userService.getUserDetails(a.getStaff_email_id());
			String staff_name = u.getFirstName() + " " + u.getLastName();

			// update comments to database
			String dateStamp = new SimpleDateFormat("dd/MM/yyyy hh:mm").format(new Date());
			String comments = previousComments + "[ " + staff_name + " | " + dateStamp + " ]" + " : " + a.getComments()
					+ "\n";

			// the review flag is automatically set
			int status = bookingService.addCommentsToApplication(a.getApplication_id(), comments);

			if (status == 0) {
				response = Response.build("Error", "Comments were not updated", false);
				return response;
			}

			response = Response.build("Success", "Comments successfully updated and review flag checked", true);

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}
		return response;

	} // add comments to application

//	private static double round(double value, int places) {
//		if (places < 0)
//			throw new IllegalArgumentException();
//
//		BigDecimal bd = new BigDecimal(value);
//		bd = bd.setScale(places, RoundingMode.HALF_UP);
//		return bd.doubleValue();
//	} // round

} // ApplicationController
