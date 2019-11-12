package com.eden.api.controller;

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

import com.eden.api.controller.engine.Availability;
import com.eden.api.service.AccountService;
import com.eden.api.service.BookingService;
import com.eden.api.service.CustomerService;
import com.eden.api.service.EngineService;
import com.eden.api.service.QueryService;
import com.eden.api.service.validation.AccountValidation;
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
import com.edenstar.model.Quote;
import com.edenstar.model.account.AccountStream;
import com.edenstar.model.account.ProcessAccount;
import com.edenstar.model.account.ViewAccount;
import com.edenstar.model.booking.KioskQuery;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class AccountController extends BaseController {

	Response response = new Response();
	Availability availability = new Availability();
	AccountValidation accValid = new AccountValidation();

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

	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_ACCOUNT
			+ Constants.PATH_VIEW_ACCOUNT, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response viewAccount(@RequestBody ProcessAccount a) {

		Application application = new Application();
		Booking booking;

		try {
			if (accValid.isNull(a, "viewaccount")) {
				response = Response.build("Error",
						"Mandatory parameters : [staff_email_id / booking_ref] cannot be null or empty", false);
				return response;
			} // isNull check

			// check to see if the staff user exists on the database
			if (!cpu.userExists(a.getStaff_email_id())) {
				response = Response.build("Error",
						"Staff email " + a.getStaff_email_id() + " does not exist on the database", false);
				return response;
			} // staff checks

			// only managers and accounts personnel can view a customer's account
			if (!cpu.checkAccountClearance(a.getStaff_email_id())) {
				response = Response.build("Denied",
						"Employee ID " + a.getStaff_email_id() + " does not have manager or accounts clearance", false);
				return response;
			}

			// get all the booking information ...
			booking = bookingService.getBooking(a.getBookingRef().toUpperCase());

			// check that the booking exists ...
			if (booking.getBooking_id() == 0) {
				response = Response.build("Error",
						"Booking ref " + a.getBookingRef() + " does not exist on the database", false);
				return response;
			} // bookingExists

			// get application information
			application = bookingService.getApplication(booking.getApplication().getApplicationID());
			application.setComments(application.getComments() + " " + booking.getApplication().getComments());
			booking.setApplication(application);

			// System.out.println("application = " + application.toString());

			/// format and set dates in java format
			// we need to convert the dates from SQL format (yyyy-MM-dd)
			// into Java format (dd/MM/yyyy)
			String dateOfBooking = availability.formatDateForJava(booking.getDate_of_booking().toString());
			String dateOfApplication = availability.formatDateForJava(application.getDateOfApplication().toString());
			String startDate = availability.formatDateForJava(booking.getLease_start_date().toString());
			String endDate = availability.formatDateForJava(booking.getLease_end_date().toString());

			// we now check if the kiosk is still available for the given dates

			booking.setStart_date(startDate);
			booking.setEnd_date(endDate);
			booking.setApplication_date(dateOfApplication);
			booking.setBooking_date(dateOfBooking);

			// now all checks have been performed, we can now diplay full details in a mega
			// data stream called ViewBooking

			// get customer details
			Customer c = new Customer();
			c = customerService.getCustomerDetails(booking.getCustomer_ID());
			ViewAccount accountStreamOut = new ViewAccount(c);

			// set the booking information
			accountStreamOut.setBooking(booking);

			// set company details
			// first must populate the number of companies and assign it to a list
			List<Company> companyList = customerService.getCustComp(booking.getCustomer_ID());

			// we assign the company list to the applicationStreamOut data train
			accountStreamOut.setCompanyList(companyList);

			// obtain kiosk information
			Quote q = new Quote();
			q.setKiosk_ID(booking.getKiosk_ID());
			q.setLease_duration_days(booking.getLease_duration_days());
			q.setStart_date(startDate);
			q.setEnd_date(endDate);

			KioskQuery kQuery = new KioskQuery();
			kQuery = bpu.checkAvailability(q, a.getStaff_email_id());

			kQuery.setDateList(null);

			// set lease information
			Lease lease = bookingService.getLeaseByBookingID(booking.getBooking_id());
			accountStreamOut.setLease(lease);

			// set kiosk information
			accountStreamOut.setKiosk_info(kQuery);

			// set the account information
			Account account = accountService.getAccountByLeaseID(lease.getLease_id());
			accountStreamOut.setAccount(account);

			// now we obtain a payment schedule
			List<Payment> paymentSchedule = accountService.getPaymentSchedule(account.getAccount_id());

			if (paymentSchedule.isEmpty()) {
				System.out.println("payment schedule is empty !");
			} else {
				for (int i = 0; i < paymentSchedule.size(); i++) {
					String formattedDueDate = availability
							.formatDateForJava(paymentSchedule.get(i).getDueByDate().toString());
					String formattedPaidDate;
					if (paymentSchedule.get(i).getPaymentInDate() == null) {
						formattedPaidDate = "SCHEDULED";
					} else {
						formattedPaidDate = availability
								.formatDateForJava(paymentSchedule.get(i).getPaymentInDate().toString());
					}

					paymentSchedule.get(i).setDue_by_date(formattedDueDate);
					paymentSchedule.get(i).setPayment_in_date(formattedPaidDate);
					System.out.println(paymentSchedule.get(i).toString());
				}
			}

			accountStreamOut.setPaymentSchedule(paymentSchedule);

			response = Response.build("Success", "Booking reference : " + a.getBookingRef() + " successfully obtained",
					true);
			response.setData(accountStreamOut);

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // viewAccount

	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_ACCOUNT
			+ Constants.PATH_ADD_COMMENT_TO_ACCOUNT, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response addComments(@RequestBody ProcessAccount a) {

		try {
			if (accValid.isNull(a, "comment")) {
				response = Response.build("Error",
						"Mandatory parameters : [staff_email_id / comments / account_id] cannot be null", false);
				return response;
			} // isNull check

			// check to see if any of the mandatory fields are empty
			if (accValid.fieldsAreEmpty(a, "comment")) {
				response = Response.build("Error",
						"No data entered for mandatory parameters, either [staff_email_id / comments]", false);
				return response;
			} // isEmpty check

			// check to see if the staff user exists on the database
			if (!cpu.userExists(a.getStaff_email_id())) {
				response = Response.build("Error",
						"Staff email " + a.getStaff_email_id() + " does not exist on the database", false);
				return response;
			} // staff checks

			// check that tha account exists
			Account account = accountService.getAccount(a.getAccount_id());
			if (account.getAccount_id() == 0) {
				response = Response.build("Error",
						"Account ID " + a.getAccount_id() + " does not exist on the database", false);
				return response;
			} // accountExists

			String previousComments = account.getComments();
			if (previousComments == null)
				previousComments = "";
			bpu.addAccountComment(account.getAccount_id(), a.getComments(), previousComments, a.getStaff_email_id());

			response = Response.build("Success", "Comments added to account ID " + a.getAccount_id() + " successfully",
					true);

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // addCommentToAccount

	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_ACCOUNT
			+ Constants.PATH_ADD_COMMENT_TO_PAYMENT, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response addCommentsToPayment(@RequestBody ProcessAccount a) {

		try {
			if (accValid.isNull(a, "comment_payment")) {
				response = Response.build("Error",
						"Mandatory parameters : [staff_email_id / comments / payment_id] cannot be null", false);
				return response;
			} // isNull check

			// check to see if any of the mandatory fields are empty
			if (accValid.fieldsAreEmpty(a, "comment_payment")) {
				response = Response.build("Error",
						"No data entered for mandatory parameters, either [staff_email_id / comments]", false);
				return response;
			} // isEmpty check

			// check to see if the staff user exists on the database
			if (!cpu.userExists(a.getStaff_email_id())) {
				response = Response.build("Error",
						"Staff email " + a.getStaff_email_id() + " does not exist on the database", false);
				return response;
			} // staff checks

			// check that tha application exists
			Payment payment = accountService.getPayment(a.getPayment_id());
			if (payment.getPayment_id() == 0) {
				response = Response.build("Error",
						"Payment ID " + a.getPayment_id() + " does not exist on the database", false);
				return response;
			} // paymentExists

			String previousComments = payment.getComments();
			if (previousComments == null)
				previousComments = "";
			bpu.addPaymentComment(payment.getPayment_id(), a.getComments(), previousComments, a.getStaff_email_id());

			response = Response.build("Success", "Comments added to payment ID " + a.getPayment_id() + " successfully",
					true);

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // addCommentToPayment

	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_ACCOUNT
			+ Constants.PATH_UPLOAD_DOCUMENT, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response uploadAccDoc(@RequestBody ProcessAccount a) {

		try {

			if (accValid.isNull(a, "document")) {
				response = Response.build("Error",
						"Mandatory parameters : [staff_email_id / document_upload / account_id] cannot be null", false);
				return response;
			} // isNull check

			// check to see if any of the mandatory fields are empty
			if (accValid.fieldsAreEmpty(a, "document")) {
				response = Response.build("Error",
						"No data entered for mandatory parameters, either [staff_email_id / document_upload]", false);
				return response;
			} // isEmpty check

			// check to see if the staff user exists on the database
			if (!cpu.userExists(a.getStaff_email_id())) {
				response = Response.build("Error",
						"Staff email " + a.getStaff_email_id() + " does not exist on the database", false);
				return response;
			} // staff checks

			// only managers and accounts personnel can view a customer's account
			if (!cpu.checkAccountClearance(a.getStaff_email_id())) {
				response = Response.build("Denied",
						"Employee ID " + a.getStaff_email_id() + " does not have manager or accounts clearance", false);
				return response;
			}

			// check that tha account exists
			Account account = accountService.getAccount(a.getAccount_id());
			if (account.getAccount_id() == 0) {
				response = Response.build("Error",
						"Account ID " + a.getAccount_id() + " does not exist on the database", false);
				return response;
			} // accountExists

			// we now have the scanned document, we shall upload it to the server
			// we need to obtain the booking ref to create the file
			Booking booking = bookingService.getBookingByAccID(account.getAccount_id());
			String booking_ref = "accounts_document";

			if (booking.getBooking_id() == 0) {
				System.out.println("booking did not load");

			} else {
				booking_ref = booking.getBooking_ref();
			}

			String urlToDocumenttScan = "";

			if (a.getDocument_upload() == null) {

			} else {
				urlToDocumenttScan = accValid.writeImageStreamToFile(a.getDocument_upload(), booking_ref);
				System.out.println("the document scan url is = " + urlToDocumenttScan);
				a.setDocument_upload_url(urlToDocumenttScan);
				a.setDocument_upload(null);
			}

			// now we update the account with the document url
			int updateStatus = accountService.updateAccDocumentURL(a.getAccount_id(), urlToDocumenttScan);

			if (updateStatus == 0) {
				response = Response.build("Failure", "Document URL was not added to the database", false);
				return response;
			} else {
				response = Response.build("Success", "Document URL was successfully added to the database", true);
			}

			String previousComments = "";

			if (account.getComments() == null) {

			} else {
				previousComments = account.getComments();
			}

			bpu.addAccountComment(account.getAccount_id(), "ACCOUNTS DOCUMENT UPLOADED", previousComments, "");

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // uploadAccDoc

	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_ACCOUNT
			+ Constants.PATH_GET_ACCOUNT, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response getAccount(@RequestBody ProcessAccount a) {

		try {

			if (accValid.isNull(a, "update")) {
				response = Response.build("Error",
						"Mandatory parameters : [staff_email_id / account_id] cannot be null", false);
				return response;
			} // isNull check

			// check to see if any of the mandatory fields are empty
			if (accValid.fieldsAreEmpty(a, "")) {
				response = Response.build("Error", "No data entered for mandatory parameters [staff_email_id ]", false);
				return response;
			} // isEmpty check

			// check to see if the staff user exists on the database
			if (!cpu.userExists(a.getStaff_email_id())) {
				response = Response.build("Error",
						"Staff email " + a.getStaff_email_id() + " does not exist on the database", false);
				return response;
			} // staff checks

			// only managers and accounts personnel can view a customer's account
			if (!cpu.checkAccountClearance(a.getStaff_email_id())) {
				response = Response.build("Denied",
						"Employee ID " + a.getStaff_email_id() + " does not have manager or accounts clearance", false);
				return response;
			}

			// check that tha account exists
			Account account = accountService.getAccount(a.getAccount_id());
			if (account.getAccount_id() == 0) {
				response = Response.build("Error",
						"Account ID " + a.getAccount_id() + " does not exist on the database", false);
				return response;
			} // accountExists

			// add payment schedule
			List<Payment> paymentSchedule = accountService.getPaymentSchedule(a.getAccount_id());
			if (paymentSchedule.size() != 0)
				account.setPaymentSchedule(paymentSchedule);

			response = Response.build("Success",
					"Account ID " + a.getAccount_id() + " successfully retrieved from database", true);
			response.setData(account);

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // getAccount

	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_ACCOUNT
			+ Constants.PATH_UPDATE_ACCOUNT, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response updateAccount(@RequestBody ProcessAccount a) {

		try {

			if (accValid.isNull(a, "update")) {
				response = Response.build("Error",
						"Mandatory parameters : [staff_email_id / account_id] cannot be null", false);
				return response;
			} // isNull check

			// check to see if any of the mandatory fields are empty
			if (accValid.fieldsAreEmpty(a, "")) {
				response = Response.build("Error", "No data entered for mandatory parameters [staff_email_id ]", false);
				return response;
			} // isEmpty check

			// check to see if the staff user exists on the database
			if (!cpu.userExists(a.getStaff_email_id())) {
				response = Response.build("Error",
						"Staff email " + a.getStaff_email_id() + " does not exist on the database", false);
				return response;
			} // staff checks

			// only managers and accounts personnel can view a customer's account
			if (!cpu.checkAccountClearance(a.getStaff_email_id())) {
				response = Response.build("Denied",
						"Employee ID " + a.getStaff_email_id() + " does not have manager or accounts clearance", false);
				return response;
			}

			// check that tha account exists
			Account account = accountService.getAccount(a.getAccount_id());
			if (account.getAccount_id() == 0) {
				response = Response.build("Error",
						"Account ID " + a.getAccount_id() + " does not exist on the database", false);
				return response;
			} // accountExists

			// check that the dates are valid

			if (a.getDeposit_cleared_date() == null) {

			} else {

				String dateError = accValid.checkDates(a.getDeposit_cleared_date());
				if (dateError == null) {
					// continue as normal ...
				} else {
					response = Response.build("Error", dateError, false);
					return response;
				}
			}

			if (a.getDeposit_refunded_date() == null) {

			} else {

				String dateError = accValid.checkDates(a.getDeposit_cleared_date());
				if (dateError == null) {
					// continue as normal ...
				} else {
					response = Response.build("Error", dateError, false);
					return response;
				}
			}

			// we need to check the new account data against the old and make changes
			compareAccount(account, a);

			// update account with new details
			int status = accountService.updateAccount(a);

			if (status == 0) {
				response = Response.build("Error", "Account update failed", false);
				return response;
			} else {
				response = Response.build("Success", "Account ID " + a.getAccount_id() + " updated successfully", true);
			}

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}
		return response;

	} // updateAccount

	private void compareAccount(Account accountOld, ProcessAccount accountNew) throws Exception {
		// TODO Auto-generated method stub

		if (accountNew.getComments() != null) {
			String previousComments = accountOld.getComments();
			if (previousComments == null)
				previousComments = "";
			bpu.addAccountComment(accountOld.getAccount_id(), accountNew.getComments(), previousComments,
					accountNew.getStaff_email_id());

		}

		if (accountNew.getDepositAmount() == 0) {
			accountNew.setDepositAmount(accountOld.getDepositAmount());
		}

		if (accountNew.getDeposit_cleared_date() != null) {
			accountNew.setDepositCleared(1);
			String previousComments = accountOld.getComments();
			if (previousComments == null)
				previousComments = "";
			bpu.addAccountComment(accountOld.getAccount_id(), "DEPOSIT CLEARED", previousComments, "");

		}

		if (accountNew.getDeposit_refunded_date() != null) {
			accountNew.setDepositRefunded(1);
			String previousComments = accountOld.getComments();
			if (previousComments == null)
				previousComments = "";
			bpu.addAccountComment(accountOld.getAccount_id(), "DEPOSIT REFUNDED", previousComments, "");
		}

	} // compareAccount

	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_ACCOUNT
			+ Constants.PATH_GET_PAYMENT, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response getPayment(@RequestBody ProcessAccount a) {

		try {

			if (accValid.isNull(a, "updatepayment")) {
				response = Response.build("Error",
						"Mandatory parameters : [staff_email_id / payment_id] cannot be null", false);
				return response;
			} // isNull check

			// check to see if any of the mandatory fields are empty
			if (accValid.fieldsAreEmpty(a, "")) {
				response = Response.build("Error", "No data entered for mandatory parameters [staff_email_id ]", false);
				return response;
			} // isEmpty check

			// check to see if the staff user exists on the database
			if (!cpu.userExists(a.getStaff_email_id())) {
				response = Response.build("Error",
						"Staff email " + a.getStaff_email_id() + " does not exist on the database", false);
				return response;
			} // staff checks

			// only managers and accounts personnel can view a customer's account
			if (!cpu.checkAccountClearance(a.getStaff_email_id())) {
				response = Response.build("Denied",
						"Employee ID " + a.getStaff_email_id() + " does not have manager or accounts clearance", false);
				return response;
			}

			// check that tha payment exists
			Payment payment = accountService.getPayment(a.getPayment_id());
			if (payment.getPayment_id() == 0) {
				response = Response.build("Error",
						"Payment ID " + a.getPayment_id() + " does not exist on the database", false);
				return response;
			} // paymentExists

			response = Response.build("Success",
					"Payment ID " + a.getPayment_id() + " successfully retrieved from database", true);
			
			
			response.setData(payment);

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // getPayment
	
	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_ACCOUNT
			+ Constants.PATH_RETRACT_PAYMENT, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response retractPayment(@RequestBody ProcessAccount a) {
		
		try {
			
			if (accValid.isNull(a, "updatepayment")) {
				response = Response.build("Error",
						"Mandatory parameters : [staff_email_id / payment_id] cannot be null", false);
				return response;
			} // isNull check

			// check to see if any of the mandatory fields are empty
			if (accValid.fieldsAreEmpty(a, "")) {
				response = Response.build("Error", "No data entered for mandatory parameters [staff_email_id ]", false);
				return response;
			} // isEmpty check

			// check to see if the staff user exists on the database
			if (!cpu.userExists(a.getStaff_email_id())) {
				response = Response.build("Error",
						"Staff email " + a.getStaff_email_id() + " does not exist on the database", false);
				return response;
			} // staff checks

			// only managers and accounts personnel can view a customer's account
			if (!cpu.checkAccountClearance(a.getStaff_email_id())) {
				response = Response.build("Denied",
						"Employee ID " + a.getStaff_email_id() + " does not have manager or accounts clearance", false);
				return response;
			}

			// check that tha payment exists
			Payment payment = accountService.getPayment(a.getPayment_id());
			if (payment.getPayment_id() == 0) {
				response = Response.build("Error",
						"Payment ID " + a.getPayment_id() + " does not exist on the database", false);
				return response;
			} // paymentExists
			
			// check that the payment has been registered as paid
			if (payment.getIsPaid() == 0) {
				response = Response.build("Error",
						"Payment ID " + a.getPayment_id() + " cannot be retracted - not registered as paid", false);
				return response;
			}
			
			// validation has been completed, we can now retract the payment
			// first we reset the payment flags
			int status = accountService.resetPayment(payment);
			if (status != 1) {
				response = Response.build("Error",
						"Payment ID " + a.getPayment_id() + " was not successfully retracted", false);
				return response;
			}
			
			response = Response.build("Success",
					"Payment ID " + a.getPayment_id() + " was successfully retracted", true);
			
			String previousComments = payment.getComments();
			if (previousComments == null || previousComments.isEmpty()) {
				previousComments = "";
			}

			bpu.addPaymentComment(payment.getPayment_id(), "PAYMENT RETRACTED", previousComments, "");
	
		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}
		
		return response;
	
	} //retractPayment


	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_ACCOUNT
			+ Constants.PATH_UPDATE_PAYMENT, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response updatePayment(@RequestBody ProcessAccount a) {

		try {

			if (accValid.isNull(a, "updatepayment")) {
				response = Response.build("Error",
						"Mandatory parameters : [staff_email_id / payment_id] cannot be null", false);
				return response;
			} // isNull check

			// check to see if any of the mandatory fields are empty
			if (accValid.fieldsAreEmpty(a, "")) {
				response = Response.build("Error", "No data entered for mandatory parameters [staff_email_id ]", false);
				return response;
			} // isEmpty check

			// check to see if the staff user exists on the database
			if (!cpu.userExists(a.getStaff_email_id())) {
				response = Response.build("Error",
						"Staff email " + a.getStaff_email_id() + " does not exist on the database", false);
				return response;
			} // staff checks

			// only managers and accounts personnel can view a customer's account
			if (!cpu.checkAccountClearance(a.getStaff_email_id())) {
				response = Response.build("Denied",
						"Employee ID " + a.getStaff_email_id() + " does not have manager or accounts clearance", false);
				return response;
			}

			// check that tha payment exists
			Payment payment = accountService.getPayment(a.getPayment_id());
			if (payment.getPayment_id() == 0) {
				response = Response.build("Error",
						"Payment ID " + a.getPayment_id() + " does not exist on the database", false);
				return response;
			} // paymentExists

			// check that the dates are valid

			if (a.getDue_by_date() != null) {

				String dateError = accValid.checkDates(a.getDue_by_date());
				if (dateError != null) {
					response = Response.build("Error", dateError, false);
					return response;
				}
			}

			if (a.getPayment_in_date() != null) {

				String dateError = accValid.checkDates(a.getPayment_in_date());
				if (dateError != null) {
					response = Response.build("Error", dateError, false);
					return response;
				}
			}

			if (a.getPayment_in_date() != null && payment.getIsPaid() == 1) {
				response = Response.build("Error",
						"Payment for payment ID " + a.getPayment_id() + " has already been paid", false);
				return response;
			}

			// we need to check the new account data against the old and make changes
			comparePayment(payment, a);

			// update account with new details
			int status = accountService.updatePayment(a);

			// we have to check if a payment has been made, then the account books
			// have to be updated

			if (status == 0) {
				response = Response.build("Error", "Payment update failed", false);
				return response;
			}

			if (a.getPayment_in_date() != null) {
				// get account details
				Account account = accountService.getAccount(payment.getAccount_id());
				account.setNoPaymentsReceived(account.getNoPaymentsReceived() + 1);
				account.setNoPaymentsRemaining(account.getNoPaymentsRemaining() - 1);
				account.setLeaseRemaining(account.getLeaseRemaining() - a.getAmount_cleared());
				status = accountService.updateAccountBooks(account);
			}

			if (status == 0) {
				response = Response.build("Error", "Account update failed", false);
				return response;
			} else {
				response = Response.build("Success", "Payment ID " + a.getPayment_id() + " and account ID "
						+ payment.getAccount_id() + " updated successfully", true);
			}

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}
		return response;

	} // updatePayment

	private void comparePayment(Payment paymentOld, ProcessAccount paymentNew) throws Exception {
		// TODO Auto-generated method stub

		if (paymentNew.getComments() == null) {

		} else {
			System.out.println("new comments are : " + paymentNew.getComments());
			String previousComments = paymentOld.getComments();
			if (previousComments == null)
				previousComments = "";
			bpu.addPaymentComment(paymentOld.getPayment_id(), paymentNew.getComments(), previousComments,
					paymentNew.getStaff_email_id());

		}

		if (paymentNew.getPayment_in_date() != null && paymentNew.getAmount_cleared() == 0) {
			paymentNew.setAmount_cleared(paymentOld.getAmountDue());
		}

		if (paymentNew.getPayment_issue() != 0) {
			paymentNew.setPayment_issue(1);
		}

		if (paymentNew.getAmount_due() == 0) {
			paymentNew.setAmount_due(paymentOld.getAmountDue());
		}

		if (paymentNew.getPayment_method() != null) {
			paymentOld.setPaymentMethod(paymentNew.getPayment_method());
		}

//		if (paymentNew.getDue_by_date() != null) {
//			String previousComments = paymentOld.getComments();
//			if (previousComments == null)
//				previousComments = "";
//			bpu.addPaymentComment(paymentOld.getPayment_id(), "DUE BY DATE CHANGED", previousComments,
//					paymentNew.getStaff_email_id());
//
//		}

		if (paymentNew.getPayment_in_date() != null) {
			Date payInDate = null;
			payInDate = availability.convertStrToDate(paymentNew.getPayment_in_date());
			String previousComments = paymentOld.getComments();
			if (previousComments == null)
				previousComments = "";
			bpu.addPaymentComment(paymentOld.getPayment_id(), "PAYMENT MADE", previousComments,
					paymentNew.getStaff_email_id());

			if (payInDate.after(paymentOld.getDueByDate())) {
				previousComments = paymentOld.getComments();
				if (previousComments == null)
					previousComments = "";
				bpu.addPaymentComment(paymentOld.getPayment_id(), "LATE PAYMENT", previousComments, "");
				paymentNew.setOverdue(0);
				paymentNew.setIs_paid(1);
			}

		}

	} // comparePayment

	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_ACCOUNT
			+ Constants.PATH_GET_ACCOUNTS, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response getAccounts(@RequestBody ProcessAccount a) {

		List<AccountStream> accountsList = new ArrayList<>();

		try {

			if (accValid.isNull(a, "")) {
				response = Response.build("Error", "Mandatory parameters : [staff_email_id] cannot be null", false);
				return response;
			} // isNull check

			// check to see if any of the mandatory fields are empty
			if (accValid.fieldsAreEmpty(a, "")) {
				response = Response.build("Error", "No data entered for mandatory parameters [staff_email_id ]", false);
				return response;
			} // isEmpty check

			// check to see if the staff user exists on the database
			if (!cpu.userExists(a.getStaff_email_id())) {
				response = Response.build("Error",
						"Staff email " + a.getStaff_email_id() + " does not exist on the database", false);
				return response;
			} // staff checks

			// only managers and accounts personnel can view a customer's account
			if (!cpu.checkAccountClearance(a.getStaff_email_id())) {
				response = Response.build("Denied",
						"Employee ID " + a.getStaff_email_id() + " does not have manager or accounts clearance", false);
				return response;
			}

			if (a.getAccountViewMode() == null || a.getAccountViewMode().isEmpty()) {
				response = Response.build("Error", "accountViewMode cannot be null", false);
				return response;
			}

			// we have cleared validation, we need to obtain the account list for the
			// employee if

			switch (a.getAccountViewMode().toLowerCase()) {
			case "*":
				System.out.println("All accounts");
				accountsList = accountService.getAllAccountsList("*");
				break;

			case "active":
				System.out.println("All accounts for active bookings");
				accountsList = accountService.getAllAccountsList("active");
				break;

			case "account_cleared":
				System.out.println("All accounts both full rent and deposit have been paid");
				accountsList = accountService.getAllAccountsList("account_cleared");
				break;

			case "deposit_cleared":
				System.out.println("All accounts where the deposits have cleared");
				accountsList = accountService.getAllAccountsList("deposit_cleared");
				break;

			case "deposit_pending":
				System.out.println("All accounts where deposits are pending");
				accountsList = accountService.getAllAccountsList("deposit_pending");
				break;

			case "payment_overdue":
				System.out.println("All accounts with overdue payments");
				accountsList = accountService.getAllAccountsList("payment_overdue");
				break;

			case "expired":
				System.out.println("All accounts for expired bookings");
				accountsList = accountService.getAllAccountsList("expired");
				break;
//
//			case "expired":
//				System.out.println("All accounts for bookings that have expired");
//				accountsList = bookingService.getAllLeaseList("expired");
//				break;
//
//			case "signed":
//				System.out.println("All contracts that have signed");
//				accountsList = bookingService.getAllLeaseList("signed");
//				break;

			default:

				System.out.println("get account by id");
				String accountIDStr = a.getAccountViewMode();
				int account_id = Integer.parseInt(accountIDStr);

				if (account_id < 1) {
					response = Response.build("Error", "please specify a valid account_id", false);
					return response;
				}

				Account account = accountService.getAccount(account_id);

				if (account.getAccount_id() == 0) {
					response = Response.build("Error",
							"Account with account_id " + account_id + " does not exist on database", false);
					return response;
				}
				response = Response.build("Success",
						"Account ID " + account.getAccount_id() + " successfully retrieved", true);
				response.setData(account);
				return response;

			} // switch

			response = Response.build("Success", accountsList.size() + " account(s) successfully retrieved", true);
			response.setData(accountsList);

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}
		return response;

	} // getAccounts

} // accountController
