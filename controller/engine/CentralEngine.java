package com.eden.api.controller.engine;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.eden.api.service.AccountService;
import com.eden.api.service.BookingService;
import com.eden.api.service.CustomerService;
import com.eden.api.service.EngineService;
import com.eden.api.service.LocationService;
import com.eden.api.service.QueryService;
import com.eden.api.service.UserService;
import com.eden.api.util.Constants;
import com.edenstar.model.Application;
import com.edenstar.model.Booking;
import com.edenstar.model.Company;
import com.edenstar.model.Customer;
import com.edenstar.model.Kiosk;
import com.edenstar.model.Lease;
import com.edenstar.model.Location;
import com.edenstar.model.Payment;
import com.edenstar.model.Product;
import com.edenstar.model.ProductPhoto;
import com.edenstar.model.Quote;
import com.edenstar.model.User;
import com.edenstar.model.Zone;
import com.edenstar.model.booking.ApplicationStream;
import com.edenstar.model.booking.BookingStream;
import com.edenstar.model.dash.GetCustomer;

@Component
public class CentralEngine implements EngineService {

	@Autowired
	private LocationService locationService;

	@Autowired
	private UserService userService;

	@Autowired
	private BookingService bookingService;

	@Autowired
	private AccountService accountService;

	@Autowired
	private CustomerService customerService;

	@Autowired
	private QueryService bpu;

	@Autowired
	public CentralEngine() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean quoteExists(String quoteRef) throws Exception {

		Quote quote = new Quote();
		boolean q_exists = true;

		System.out.println("quote reference = " + quoteRef);
		try {

			quote = bookingService.getQuote(quoteRef);
			if (quote.getQuote_ID() == 0)
				q_exists = false;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return q_exists;
	} // quoteExists

	@Override
	public boolean userExists(String staff_email) throws Exception {
		User r = new User();
		boolean u_exists = true;

		try {
			System.out.println("staff_email = " + staff_email);

			r = userService.getUserDetails(staff_email);
			if (r.getEmailID() == null)
				return false;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return u_exists;
	} // userExists

	@Override
	public boolean kioskExists(int kiosk_id) throws Exception {

		Kiosk kiosk = new Kiosk();
		boolean k_exists = true;

		try {

			kiosk = locationService.getKioskByID(kiosk_id);
			if (kiosk.getKioskID() == 0)
				k_exists = false;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return k_exists;
	} // kioskExists

	@Override
	public boolean customerExists(int customerID) throws Exception {
		Customer r = new Customer();
		boolean c_exists = true;

		try {
			r = customerService.getCustomerDetails(customerID);

			// if the record does not exist we should return false
			if (r.getEmailIDCus() == null)
				return false;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return c_exists;
	} // customerExists

	@Override
	public boolean checkClearance(String staff_email) throws Exception {
		User r = new User();

		try {

			r = userService.getUserDetails(staff_email);

			if (r.getUserLevel().contentEquals("manager_admin")) {

				System.out.println("manager clearance accepted");
				return true;

			} else if (r.getUserLevel().equals("admin")) {

				System.out.println("administrator level clearance verified");
				return true;

			} else
				return false;

		} catch (Exception e) {

			e.printStackTrace();
		}

		return false;
	}

	@Override
	public boolean companyExists(int companyID) throws Exception {
		Company r = new Company();
		boolean c_exists = true;

		try {

			r = customerService.getCompany(companyID);
			System.out.println("get company = " + r.getCompanyName());

			// if the record does not exist we should return false
			if (r.getCompanyName() == null)
				return false;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return c_exists;
	} // companyExists

	@Override
	public boolean customerExists(String cust_email) throws Exception {
		Customer r = new Customer();
		boolean c_exists = true;

		try {
			r = customerService.getCustDetails(cust_email);

			// if the record does not exist we should return false
			if (r.getEmailIDCus() == null)
				return false;

			System.out.println("cust_email = " + cust_email);
			System.out.println("result r = " + r.getEmailIDCus());
			// first check to see if the staff email exists on the database
			if (!r.getEmailIDCus().contentEquals(cust_email)) {
				// does not match
				System.out.println("customer " + cust_email + " does not exist");
				c_exists = false;
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return c_exists;
	} // customerExists

	@Override
	public boolean locationExists(int location_id) throws Exception {
		Location r = new Location();
		boolean c_exists = true;

		try {

			r = locationService.getLocation(location_id);

			// if the record does not exist we should return false
			if (r.getLocationName() == null)
				return false;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return c_exists;
	} // locationExists

	@Override
	public boolean locationExists(String locationName) throws Exception {
		Location r = new Location();
		boolean c_exists = true;

		try {

			r = locationService.getLocationByName(locationName);

			// if the record does not exist we should return false
			if (r.getLocationName() == null)
				return false;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return c_exists;
	} // locationExists

	@Override
	public boolean productExists(int productID) throws Exception {
		Product r = new Product();
		boolean c_exists = true;

		try {

			r = customerService.getProduct(productID);
			System.out.println("get product = " + r.getDescription());

			// if the record does not exist we should return false
			if (r.getDescription() == null)
				return false;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return c_exists;
	} // productExists

	@Override
	public boolean productPhotoExists(int productPhotoID) throws Exception {
		ProductPhoto r = new ProductPhoto();
		boolean c_exists = true;

		try {

			r = customerService.getProductPhotobyID(productPhotoID);
			// System.out.println("get product photo = " + r.get(0).getDescription());

			// if the record does not exist we should return false
			if (r.getDescription() == null)
				return false;

		} catch (Exception e) {

			e.printStackTrace();
		}

		return c_exists;
	} // productPhotoExists

	@Override
	public boolean zoneExists(int zone_id) throws Exception {
		Zone z = new Zone();
		boolean c_exists = true;

		try {

			z = locationService.getZone(zone_id);

			// if the record does not exist we should return false
			if (z.getZoneNumber() == 0)
				return false;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return c_exists;
	} // zoneExists

	@Override
	public boolean managerExists(int employee_id) throws Exception {
		// TODO Auto-generated method stub
		User manager = new User();
		manager = userService.getUserDetailsByEmpID(employee_id);

		if (manager.getEmployeeID() == 0)
			return false;

		return true;
	} // manager exists

	@Override
	public boolean checkManagerClearance(String emailID) throws Exception {
		User r = new User();

		try {

			r = userService.getUserDetails(emailID);

			if (r.getUserLevel().contentEquals("manager_admin") || r.getUserLevel().equals("manager")) {

				System.out.println("manager level clearance verified");
				return true;

			} else
				return false;

		} catch (Exception e) {

			e.printStackTrace();
		}

		return false;
	}

	@Override
	public boolean applicationExists(int application_id) throws Exception {
		// TODO Auto-generated method stub
		Application a = new Application();
		a = bookingService.getApplication(application_id);

		if (a.getApplicationID() == 0)
			return false;

		return true;
	} // applicationExists

	@Override
	public boolean bookingExists(String booking_ref) throws Exception {
		// TODO Auto-generated method stub
		Booking booking = bookingService.getBooking(booking_ref);

		if (booking.getBooking_id() == 0)
			return false;

		return true;
	} // bookingExists

	@Override
	public void refreshBookingFlags() throws Exception {

		System.out.println("\nrunning the booking refresh flags tronBot\n");
		
		// we check if the booking lease has expired and set the flag
		Availability availability = new Availability();
		List<BookingStream> activeBookings = new ArrayList<>();

		// obtain a list of active (non-expired/non-cancelled) bookings
		activeBookings = bookingService.getActiveBookings();
		System.out.println("size of active list = " + activeBookings.size());

		// we need to convert the dates from SQL format (yyyy-MM-dd)
		// into Java format (dd/MM/yyyy)

		for (int i = 0; i < activeBookings.size(); i++) {

			String endDate = activeBookings.get(i).getLease_end_date();

			if (availability.isBookingExpired(endDate)) {
				// set expired flag in database
				int status = bookingService.setBookingExpiredFlag(activeBookings.get(i).getBooking_id(), true);
				System.out.println("Flag set = " + status);
				bpu.addBookingComment(activeBookings.get(i).getBooking_id(), "LEASE EXPIRED",
						activeBookings.get(i).getComments(), "");
			} // isExpired

			// now we check if the booking is nearly expired and set the flag
			if (availability.isBookingNearlyExpired(endDate, activeBookings.get(i).getExpiry_due_period_days())) {
				// set the nearly expired flag in database

				if (activeBookings.get(i).getIs_expiry_due() == 0) {

					String previousComments = activeBookings.get(i).getComments();
					if (previousComments == null)
						previousComments = "";

					int status = bookingService.setBookingExpiryDueFlag(activeBookings.get(i).getBooking_id(), true);
					if (status == 0)
						System.out.println("Booking expiry flag not set, status = " + status);
					bpu.addBookingComment(activeBookings.get(i).getBooking_id(), "LEASE EXPIRY DUE", previousComments,
							"");

				}

			} // has booking nearly expired

		} // for loop

	} // refreshBookingFlags

	@Override
	public void refreshQuoteFlags() throws Exception {

		System.out.println("\nrunning the quote refresh flags tronBot\n");

		// we check if the booking lease has expired and set the flag
		Availability availability = new Availability();
		List<Quote> activeQuotes = new ArrayList<>();

		// obtain a list of active (non-expired) quotes
		activeQuotes = bookingService.getActiveQuotes();
		System.out.println("number of active quotes = " + activeQuotes.size());

		// we need to convert the dates from SQL format (yyyy-MM-dd)
		// into Java format (dd/MM/yyyy)

		for (int i = 0; i < activeQuotes.size(); i++) {

			String startDate = availability.formatDateForJava(activeQuotes.get(i).getLease_start_date().toString());
			String quoteDate = availability.formatDateForJava(activeQuotes.get(i).getDate_of_quote().toString());
			int expiryDurationDays = activeQuotes.get(i).getExpiry_duration_days();

			if (availability.isQuoteExpired(quoteDate, expiryDurationDays) || availability.isQuoteOutOfDate(startDate)) {

				System.out.println(" \nExpired quote detected, quote ref = " + activeQuotes.get(i).getQuoteRef()
						+ " quote id = " + activeQuotes.get(i).getQuote_ID() + "\n");

				// does booking exist ?
				Booking b = bookingService.getBookingByQuoteRef(activeQuotes.get(i).getQuoteRef());

				if (b.getBooking_id() == 0) {
					// if booking does not exist .. does an application exist ?

					Application a = bookingService.getApplicationByQuoteID(activeQuotes.get(i).getQuote_ID());

					if (a.getApplicationID() == 0) {
						// the booking and application does not exist action ...
						int status = bookingService.setQuoteExpiredFlag(activeQuotes.get(i).getQuote_ID());
						System.out.println("Quote expiry flag set for " + activeQuotes.get(i).getQuote_ID()
								+ " status = " + status);

					} else {
						// is the application active ?

						if (a.getIsExpired() == 0 && a.getIsDeclined() == 0) {
							// active application flagging procedure ...
							System.out.println("* quote ref " + activeQuotes.get(i).getQuoteRef()
									+ " has an active application, ID: " + a.getApplicationID()
									+ " - no further expiry action will be taken *");

						} else {
							// application is either expired or declined
							int status = bookingService.setQuoteExpiredFlag(activeQuotes.get(i).getQuote_ID());
							System.out.println("Quote expiry flag set for " + activeQuotes.get(i).getQuote_ID()
									+ " status = " + status);

						} // nexted if

					} // outer if

				} else {
					// if booking exists, is it active ?

					if (b.getIs_cancelled() == 0 && b.getIsExpired() == 0) {
						// booking is active ...
						System.out
								.println("* quote ref " + activeQuotes.get(i).getQuoteRef() + " has an active booking: "
										+ b.getBooking_ref() + " - no further expiry action will be taken *");

					} else {

						// the booking is either cancelled or expired ...
						int status = bookingService.setQuoteExpiredFlag(activeQuotes.get(i).getQuote_ID());
						System.out.println("Quote expiry flag set for " + activeQuotes.get(i).getQuote_ID()
								+ " status = " + status);

					} // nested, nested if command

				} // nested if

			} // isExpired

//			if (availability.isQuoteOutOfDate(startDate)) {
//				// expired action ..
//				int status = bookingService.setQuoteExpiredFlag(activeQuotes.get(i).getQuote_ID());
//				System.out.println("Quote out of date, start date = " + startDate + " for quote id "
//						+ activeQuotes.get(i).getQuote_ID() + " status = " + status);
//			}

		} // for loop

	} // refreshQuoteFlags

	@Override
	public void refreshApplicationFlags() throws Exception {

		System.out.println("\nrunning the application refresh flags tronBot\n");

		// we check if the booking lease has expired and set the flag
		Availability availability = new Availability();
		List<Application> activeApplications = new ArrayList<>();

		// obtain a list of active (non-expired/non-cancelled) applications
		activeApplications = bookingService.getActiveApplications();
		System.out.println("number of active applications = " + activeApplications.size());

		for (int i = 0; i < activeApplications.size(); i++) {

			// we need to convert the dates from SQL format (yyyy-MM-dd)
			// into Java format (dd/MM/yyyy)

			String applicationDate = availability
					.formatDateForJava(activeApplications.get(i).getDateOfApplication().toString());

			if (availability.isApplicationExpired(applicationDate, Constants.application_valid_for_days)) {
				System.out.println(" \nExpired application detected, application ID = "
						+ activeApplications.get(i).getApplicationID() + "\n");

				// set expiry flag
				int status = bookingService.setApplicationExpiredFlag(activeApplications.get(i).getApplicationID(),
						true);
				System.out.println("Application expiry flag set = " + status);
				String previousComments = activeApplications.get(i).getComments();
				if (previousComments == null)
					previousComments = "";
				bpu.addApplicationComment(activeApplications.get(i).getApplicationID(), "APPLICATION EXPIRED",
						previousComments, "");
			}
		} // for loop

//			if (availability.isApplicationExpired(applicationDate, Constants.application_valid_for_days)) {
//				// set expired flag in database
//
//				System.out.println(" \nExpired application detected, application ID = "
//						+ activeApplications.get(i).getApplicationID() + "\n");
//
//				// does booking exist ?
//				Booking b = bookingService.getBookingByApplicationID(activeApplications.get(i).getApplicationID()); // .getBookingByQuoteRef(activeQuotes.get(i).getQuoteRef());
//
//				if (b.getBooking_id() == 0) {
//					// if booking does not exist .. check to see if the application is active?
//
//					Application a = bookingService.getApplication(activeApplications.get(i).getApplicationID());
//
//					if (a.getIsDeclined() == 0) {
//						// active application flagging procedure ...
//
//						System.out.println("* application id " + activeApplications.get(i).getApplicationID()
//								+ " is an active application - no further expiry action will be taken *");
//
//					} else {
//						// application is declined then flag
//
//						int status = bookingService
//								.setApplicationExpiredFlag(activeApplications.get(i).getApplicationID(), true);
//						System.out.println("Application expiry flag set = " + status);
//						String previousComments = activeApplications.get(i).getComments();
//						if (previousComments == null)
//							previousComments = "";
//						bpu.addApplicationComment(activeApplications.get(i).getApplicationID(), "APPLICATION EXPIRED",
//								previousComments, "");
//					}
//
//				} else {
//					// if booking exists, is it active ?
//
//					if (b.getIs_cancelled() == 0 && b.getIsExpired() == 0) {
//						// booking is active ...
//						System.out.println("* application id " + activeApplications.get(i).getApplicationID()
//								+ " has an active booking: " + b.getBooking_ref()
//								+ " - no further expiry action will be taken *");
//
//					} else {
//
//						// the booking is either cancelled or expired ...
//
//						int status = bookingService
//								.setApplicationExpiredFlag(activeApplications.get(i).getApplicationID(), true);
//						System.out.println("Application expiry flag set = " + status);
//						String previousComments = activeApplications.get(i).getComments();
//						if (previousComments == null)
//							previousComments = "";
//						bpu.addApplicationComment(activeApplications.get(i).getApplicationID(), "APPLICATION EXPIRED",
//								previousComments, "");
//
//					} // nested, nested if command
//
//				} // nested if
//
//			} // isExpired

		// The expired applications have been flagged, we need to check for declined
		// applications
		// and flag them as archived and expired

		// obtain a list of active (non-expired/declined/non-archived) applications
		activeApplications = bookingService.getDeclinedApplications();
		System.out.println("number of declined applications = " + activeApplications.size());

		for (int i = 0; i < activeApplications.size(); i++) {

			System.out.println(" \nDeclined application detected, application ID = "
					+ activeApplications.get(i).getApplicationID() + "\n");

			// set expiry flag
			int status = bookingService.setApplicationExpiredFlag(activeApplications.get(i).getApplicationID(), true);
			System.out.println("Application archive and expiry flag set = " + status);
			String previousComments = activeApplications.get(i).getComments();
			if (previousComments == null)
				previousComments = "";
			bpu.addApplicationComment(activeApplications.get(i).getApplicationID(), "APPLICATION DECLINED",
					previousComments, "");

		} // for loop

	} // refreshApplicationFlags

	@Override
	public void refreshOverdueFlags() throws Exception {
		// TODO Auto-generated method stub
		System.out.println("\nrunning payment overdue tronBot\n");

		// we check if the booking lease has expired and set the flag
		Availability availability = new Availability();
		List<Payment> activePayments = new ArrayList<>();

		// obtain a list of active (non-expired/non-cancelled) bookings
		activePayments = accountService.getActivePayments();
		System.out.println("size of active list = " + activePayments.size());

		// we need to convert the dates from SQL format (yyyy-MM-dd)
		// into Java format (dd/MM/yyyy)

		for (int i = 0; i < activePayments.size(); i++) {

			String dueByDate = availability.formatDateForJava(activePayments.get(i).getDueByDate().toString());

			if (availability.isPaymentOverdue(dueByDate)) {
				// set overdue flag in database

				System.out.println(
						" \nOverdue payment detected, payment ID = " + activePayments.get(i).getPayment_id() + "\n");

				int status = accountService.setPaymentOverdueFlag(activePayments.get(i).getPayment_id(), true);
				System.out.println("Payment overdue flag set = " + status);
				String previousComments = activePayments.get(i).getComments();
				if (previousComments == null)
					previousComments = "";
				bpu.addPaymentComment(activePayments.get(i).getPayment_id(), "PAYMENT OVERDUE", previousComments, "");

			} // nested if

		} // isExpired

	} // refreshOverdueFlags

	@Override
	public void cleanUpQuotes() throws Exception {

		// deletes expired quotations
		List<Quote> expiredQuotes = new ArrayList<>();
		expiredQuotes = bookingService.getExpiredQuotes("expired");

		if (expiredQuotes.isEmpty()) {
			System.out.println("[TronBot] : No garbage to collect today ...");
		} else {

			for (int i = 0; i < expiredQuotes.size(); i++) {
				// delete the garbage one by one !
				bookingService.deleteQuote(expiredQuotes.get(i).getQuote_ID());
			} // for
		}

	} // cleanUpQuotes

	@Override
	public void archiveQuotes() throws Exception {

		// moves expired and unsubmitted quotations to archive
		List<Quote> expiredUnsubmittedQuotes = new ArrayList<>();
		expiredUnsubmittedQuotes = bookingService.getExpiredQuotes("expiredUnsubmitted");

		if (expiredUnsubmittedQuotes.isEmpty()) {
			System.out.println("[TronBot] : No expired unsubmitted quotes to move today ...");
		} else {

			for (int i = 0; i < expiredUnsubmittedQuotes.size(); i++) {
				// move quote one by one !
				// write quote to archive first
				Quote quote = new Quote(expiredUnsubmittedQuotes.get(i));
				int status = bookingService.archiveQuote(quote);
				if (status == 0) {
					System.out.println(
							"Error, Quote Ref " + expiredUnsubmittedQuotes.get(i).getQuoteRef() + " archived failed");
				} else {
					System.out.println(
							"Quote Ref " + expiredUnsubmittedQuotes.get(i).getQuoteRef() + " archived successfully");
					// once the quote has been successfully copied to the archive, we can
					// remove it from the quote table
					int removeStatus = bookingService.removeQuote(expiredUnsubmittedQuotes.get(i).getQuote_ID());
					if (removeStatus == 1) {
						System.out.println("Quote Ref " + expiredUnsubmittedQuotes.get(i).getQuoteRef()
								+ " successfully removed from QUOTE table");
					} else {
						System.out.println("Quote Ref " + expiredUnsubmittedQuotes.get(i).getQuoteRef()
								+ " failed to be removed from QUOTE table");
					}

				}

			} // for
		}
	} // archiveQuotes

	@Override
	public void archiveApplications() throws Exception {
		// this archives the applications flagged archive and corresponding quotes
		// obtain applications marked for archive
		List<Application> archivedApplications = new ArrayList<>();
		archivedApplications = bookingService.getArchivedApplications();

		if (archivedApplications.isEmpty()) {
			System.out.println("[TronBot] : No applications to archive today ...");
		} else {

			for (int i = 0; i < archivedApplications.size(); i++) {
				// archive applications + corresponding quote one by one !
				// write quote to archive first
				Application application = new Application(archivedApplications.get(i));

				// we initially archive the quote referenced by the application
				Quote quote = bookingService.getQuote(archivedApplications.get(i).getQuoteID());

				int status = bookingService.archiveQuote(quote);
				if (status == 0) {
					System.out.println("Error, Quote Ref " + quote.getQuoteRef() + " archived failed");
				} else {
					
					System.out.println("Quote Ref " + quote.getQuoteRef() + " archived successfully");
					// once the quote has been successfully copied to the archive, we can
					// remove it from the quote table
					int removeStatus = bookingService.removeQuote(quote.getQuote_ID());
					if (removeStatus == 1) {
						System.out
								.println("Quote Ref " + quote.getQuoteRef() + " successfully removed from QUOTE table");
					} else {
						System.out
								.println("Quote Ref " + quote.getQuoteRef() + " failed to be removed from QUOTE table");
					}

				} // if

				// archive the application procedures ...
				status = bookingService.archiveApplication(application);
				if (status == 0) {
					System.out.println("Error, Application ID " + archivedApplications.get(i).getApplicationID()
							+ " archived failed");
				} else {
					
					System.out.println("Application ID " + archivedApplications.get(i).getApplicationID()
							+ " archived successfully");
					// once the application has been successfully copied to the archive, we can
					// remove it from the application table
					int removeStatus = bookingService.removeApplication(archivedApplications.get(i).getApplicationID());
					if (removeStatus == 1) {
						System.out.println("Application ID " + archivedApplications.get(i).getApplicationID()
								+ " successfully removed from APPLICATION table");
					} else {
						System.out.println("Application ID " + archivedApplications.get(i).getApplicationID()
								+ " failed to be removed from APPLICATION table");
					} // nested if

				} // if

			} // for

		} // outer if 

	} // archiveApplications

	@Override
	public void cleanUpApplications() throws Exception {

		// deletes expired quotations
		List<ApplicationStream> expiredApplications = new ArrayList<>();
		expiredApplications = bookingService.getAllApplicationList("expired_nondeleted");

		if (expiredApplications.isEmpty()) {
			System.out.println("[TronBot] No garbage to collect today ...");
		} else {

			for (int i = 0; i < expiredApplications.size(); i++) {
				// delete the garbage one by one !
				bookingService.deleteApplication(expiredApplications.get(i).getApplication_id());
			} // for
		}

	} // cleanUpApplications

	@Override
	public void cleanUpBookings() throws Exception {

		// deletes expired quotations
		List<BookingStream> expiredBookings = new ArrayList<>();
		expiredBookings = bookingService.getAllBookingList("expired_nondeleted");

		if (expiredBookings.isEmpty()) {
			System.out.println("[TronBot] : No garbage to collect today ...");
		} else {

			for (int i = 0; i < expiredBookings.size(); i++) {
				// delete the garbage one by one !
				bookingService.deleteBooking(expiredBookings.get(i).getBooking_id());
			} // for
		}

	} // cleanUpBookings

	@Override
	public boolean bookingCancelled(int booking_id) throws Exception {
		// TODO Auto-generated method stub
		return bookingService.isBookingCancelled(booking_id);
	}

	@Override
	public GetCustomer getCustomerInfo(Customer customer) throws Exception {
		// TODO Auto-generated method stub
		// list of companies associated with customer
		// CustomerValidation cusValid = new CustomerValidation();
		List<Company> companyList = new ArrayList<Company>();

		// list of products associated with each company
		List<ArrayList<Product>> listOfProductLists = new ArrayList<ArrayList<Product>>();
		ArrayList<Product> productList = new ArrayList<Product>();

		// list of photos associated with each product
		List<ArrayList<ProductPhoto>> listOfProductPhotosLists = new ArrayList<ArrayList<ProductPhoto>>();
		ArrayList<ProductPhoto> productPhotoList = new ArrayList<ProductPhoto>();

		Customer cust = new Customer();
		GetCustomer c = new GetCustomer();
		c.setEmailIDCus(c.getEmailIDCus());

		try {
			cust = customerService.getCustDetails(customer.getEmailIDCus());

//				// copy over the new customer details
//				c = cusValid.setCustomerDetails(cust, c);

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
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return c;
	} // getCustomer

	@Override
	public boolean leaseExists(int lease_id) throws Exception {
		Lease lease = new Lease();
		boolean l_exists = true;

		System.out.println("lease id = " + lease_id);
		try {

			lease = bookingService.getLease(lease_id);
			if (lease.getLease_id() == 0)
				l_exists = false;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return l_exists;
	} // leaseExists

	@Override
	public boolean checkAccountClearance(String staff_email_id) throws Exception {
		// TODO Auto-generated method stub
		User r = new User();

		try {

			r = userService.getUserDetails(staff_email_id);

			if (r.getUserLevel().contentEquals("manager") || r.getUserLevel().contentEquals("manager_admin")) {

				System.out.println("manager clearance accepted");
				return true;

			} else if (r.getUserLevel().equals("accounts")) {

				System.out.println("accounts clearance verified");
				return true;

			} else
				return false;

		} catch (Exception e) {

			e.printStackTrace();
		}

		return false;
	} // checkAccountClearace

} // CentralEngine
