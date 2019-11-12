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

import com.eden.api.controller.engine.Availability;
import com.eden.api.service.BookingService;
import com.eden.api.service.CustomerService;
import com.eden.api.service.EngineService;
import com.eden.api.service.QueryService;
import com.eden.api.service.UserService;
import com.eden.api.service.validation.BookingValidation;
import com.eden.api.service.validation.ZoneValidation;
import com.eden.api.util.Constants;
import com.eden.api.util.Response;
import com.eden.api.util.ResponseEnum;
import com.edenstar.model.Application;
import com.edenstar.model.Booking;
import com.edenstar.model.Company;
import com.edenstar.model.Customer;
import com.edenstar.model.Lease;
import com.edenstar.model.Product;
import com.edenstar.model.ProductPhoto;
import com.edenstar.model.Quote;
import com.edenstar.model.booking.BookingStream;
import com.edenstar.model.booking.KioskQuery;
import com.edenstar.model.booking.ProcessBooking;
import com.edenstar.model.booking.ProductTrain;
import com.edenstar.model.booking.ViewBooking;
import com.edenstar.model.booking.forms.EmailHeader;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class BookingController extends BaseController {

	Response response = new Response();
	Availability availability = new Availability();
	BookingValidation bookValid = new BookingValidation();

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
			+ Constants.PATH_VIEW_BOOKING, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response viewBooking(@RequestBody ProcessBooking a) {

		Application application = new Application();
		Booking booking;

		try {
			if (bookValid.isNullOrEmpty(a)) {
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

			// get all the booking information ...
			booking = bookingService.getBooking(a.getBooking_ref());

			// check that the booking exists ...
			if (booking.getBooking_id() == 0) {
				response = Response.build("Error",
						"Booking ref " + a.getBooking_ref() + " does not exist on the database", false);
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
			ViewBooking bookingStreamOut = new ViewBooking(c);

			// set the booking information
			bookingStreamOut.setBooking(booking);

			// set company details
			// first must populate the number of companies and assign it to a list
			List<Company> companyList = customerService.getCustComp(booking.getCustomer_ID());

			// we assign the company list to the applicationStreamOut data train
			bookingStreamOut.setCompanyList(companyList);

			// we assign the company list to the applicationStreamOut data train
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
			bookingStreamOut.setProductAndPhotoList(productTrainList);

			// obtain kiosk information
			Quote q = new Quote();
			q.setKiosk_ID(booking.getKiosk_ID());
			q.setLease_duration_days(booking.getLease_duration_days());
			q.setStart_date(startDate);
			q.setEnd_date(endDate);

			KioskQuery kQuery = new KioskQuery();
			kQuery = bpu.checkAvailability(q, a.getStaff_email_id());

			kQuery.setDateList(null);

			Lease lease = bookingService.getLeaseByBookingID(booking.getBooking_id());
			bookingStreamOut.setLease(lease);

			bookingStreamOut.setKiosk_info(kQuery);

			response = Response.build("Success", "Booking reference : " + a.getBooking_ref() + " successfully obtained",
					true);
			response.setData(bookingStreamOut);

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // viewBooking

	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_BOOKING
			+ Constants.PATH_GET_BOOKINGS, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response getBookings(@RequestBody ProcessBooking b) {

		List<BookingStream> bookingList = new ArrayList<>();

		try {

			if (bookValid.isNull(b)) {
				response = Response.build("Error", "Mandatory parameters : [staff_email_id] cannot be null", false);
				return response;
			} // isNull check

			// check to see if the staff user exists on the database
			if (!cpu.userExists(b.getStaff_email_id())) {
				response = Response.build("Error",
						"Staff email " + b.getStaff_email_id() + " does not exist on the database", false);
				return response;
			} // staff checks

			if (b.getBookingViewMode() == null || b.getBookingViewMode().isEmpty()) {
				response = Response.build("Error", "bookingViewMode cannot be null", false);
				return response;
			}

			// we have cleared validation, we need to obtain the application list for the
			// employee if

			switch (b.getBookingViewMode().toLowerCase()) {
			case "*":
				System.out.println("All bookings");
				bookingList = bookingService.getAllBookingList("*");
				break;
			case "cancelled":
				System.out.println("All cancelled applications");
				bookingList = bookingService.getAllBookingList("cancelled");
				break;
			case "review":
				System.out.println("All review bookings");
				bookingList = bookingService.getAllBookingList("review");
				break;
			case "expired":
				System.out.println("All expired bookings");
				bookingList = bookingService.getAllBookingList("expired");
				break;
			case "expiry_due":
				System.out.println("All due expiry bookings");
				bookingList = bookingService.getAllBookingList("expiry_due");
				break;
			case "company":
				System.out.println("All company bookings");
				bookingList = bookingService.getAllBookingList("company");
				break;
			case "sales":
				System.out.println("All bookings by sales employee");
				bookingList = bookingService.getBookingByField("sales", b.getField_id());
				break;
//			case "sales_history":
//				System.out.println("All bookings by sales employee");
//				User sales = userService.getUserDetails(b.getStaff_email_id());
//				int sales_id = sales.getEmployeeID();
//				bookingList = bookingService.getBookingByField("sales_history", sales_id);
//				break;

			} // switch

			response = Response.build("Success", bookingList.size() + " bookings successfully retrieved", true);
			response.setData(bookingList);

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // getBookings

	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_BOOKING
			+ Constants.PATH_GET_BOOKINGS_BY_FIELD, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response getBookingsByField(@RequestBody ProcessBooking b) {

		List<BookingStream> bookingList = new ArrayList<BookingStream>();

		try {

			if (bookValid.isNull(b)) {
				response = Response.build("Error", "Mandatory parameters : [staff_email_id] cannot be null", false);
				return response;
			} // isNull check

			// check to see if the staff user exists on the database
			if (!cpu.userExists(b.getStaff_email_id())) {
				response = Response.build("Error",
						"Staff email " + b.getStaff_email_id() + " does not exist on the database", false);
				return response;
			} // staff checks

			if (b.getBookingViewMode() == null || b.getBookingViewMode().isEmpty()) {
				response = Response.build("Error", "bookingViewMode cannot be null", false);
				return response;
			}

			if (b.getField_id() == 0) {
				response = Response.build("Error", "field_id cannot be 0", false);
				return response;
			}

			// we have cleared validation, we need to obtain the application list for the
			// employee if

			switch (b.getBookingViewMode().toLowerCase()) {

			case "manager":
				System.out.println("All bookings supervised by manager");
				bookingList = bookingService.getBookingByField("manager", b.getField_id());
				break;
			case "sales":
				System.out.println("All bookings by sales employee");
				bookingList = bookingService.getBookingByField("sales", b.getField_id());
				break;
			case "customer":
				System.out.println("All bookings by customer");
				bookingList = bookingService.getBookingByField("customer", b.getField_id());
				break;
			case "kiosk":
				System.out.println("All bookings under kiosk_id");
				bookingList = bookingService.getBookingByField("kiosk", b.getField_id());
				break;
			case "application":
				System.out.println("All bookings by sales employee");
				bookingList = bookingService.getBookingByField("application", b.getField_id());
				break;
			case "booking":
				System.out.println("booking summary under booking id");
				bookingList = bookingService.getBookingByField("booking", b.getField_id());
				break;
			case "zone":
				System.out.println("All bookings by sales employee");
				bookingList = bookingService.getBookingByField("zone", b.getField_id());
				break;
			case "rate":
				System.out.println("All bookings by sales employee");
				bookingList = bookingService.getBookingByField("rate", b.getField_id());
				break;
			case "location":
				System.out.println("All bookings under location_id");
				bookingList = bookingService.getBookingByField("location", b.getField_id());
				break;

			} // switch

			response = Response.build("Success", bookingList.size() + " booking(s) successfully retrieved", true);
			response.setData(bookingList);

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // getBookingsByField

	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_BOOKING
			+ Constants.PATH_CANCEL_BOOKING, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response cancelBooking(@RequestBody ProcessBooking b) {

		Booking booking;

		try {
			if (bookValid.isNullOrEmpty(b)) {
				response = Response.build("Error",
						"Mandatory parameters : [staff_email_id / booking_ref] cannot be null or empty", false);
				return response;
			} // isNull check

			// check to see if the staff user exists on the database
			if (!cpu.userExists(b.getStaff_email_id())) {
				response = Response.build("Error",
						"Staff email " + b.getStaff_email_id() + " does not exist on the database", false);
				return response;
			} // staff checks

			// get all the booking information ...
			booking = bookingService.getBooking(b.getBooking_ref());

			// check that the booking exists ...
			if (booking.getBooking_id() == 0) {
				response = Response.build("Error",
						"Booking ref " + b.getBooking_ref() + " does not exist on the database", false);
				return response;
			} // bookingExists

			// check to see if booking is cancelled
			if (cpu.bookingCancelled(booking.getBooking_id())) {
				response = Response.build("Error", "Booking ref " + b.getBooking_ref() + " has already been cancelled",
						false);
				return response;
			} // isBookingCancelled

			if (!cpu.checkManagerClearance(b.getStaff_email_id())) {
				response = Response.build("Denied",
						"Employee ID " + b.getStaff_email_id() + " does not have manager status", false);
				return response;
			} // manager clearance

			// now that we have validation completed, we need to carry out cancellations
			// procedures ...
			// delete booking
			int status = 0;

			status = bookingService.deleteBooking(booking.getBooking_id());

			if (status == 1) {
				System.out.println("booking successfully deleted");
			} else {
				System.out.println("booking not deleted successfully");
				response = Response.build("Error",
						"Booking " + booking.getBooking_ref() + " was not deleted from the database", false);
				return response;
			}

			// next we delete the booking from the kiosk calendar

			status = bookingService.deleteCalendarDates(booking.getCalendar_id());

			if (status == 1) {
				System.out.println("booking successfully deleted from kiosk calendar");
			} else {
				System.out.println("kiosk calendar entry was not deleted successfully");
				response = Response.build("Error",
						"Booking " + booking.getBooking_ref() + " was not deleted from the kiosk calendar", false);
				return response;
			}

			/// format and set dates in java format
			// we need to convert the dates from SQL format (yyyy-MM-dd)
			// into Java format (dd/MM/yyyy)
			String dateOfBooking = availability.formatDateForJava(booking.getDate_of_booking().toString());
			String startDate = availability.formatDateForJava(booking.getLease_start_date().toString());
			String endDate = availability.formatDateForJava(booking.getLease_end_date().toString());

			// we now check if the kiosk is still available for the given dates

			booking.setStart_date(startDate);
			booking.setEnd_date(endDate);
			booking.setBooking_date(dateOfBooking);

			// next we send an email to the customer that the booking has been cancelled.
			// an email with the booking details will be sent to the successful customer
			EmailHeader emailHeader = new EmailHeader("Booking Cancellation");

			Customer customer = customerService.getCustomerDetails(booking.getCustomer_ID());
			Quote quote = bookingService.getQuote(booking.getQuoteRef());
			quote.setStart_date(startDate);
			quote.setEnd_date(endDate);

			System.out.println(quote.toString());
			KioskQuery kQuery = bpu.checkAvailability(quote, b.getStaff_email_id());

			// generate booking email
			String cancellationDetails = bpu.generateBookingCancellationEmail(booking, customer, kQuery);

			// emailHeader.setHeaderTitle("Booking Cancellation");
			String messageBody = emailHeader.toString() + "\n" + cancellationDetails;

			// we now have to email the quotation to the customer
			boolean emailSent = bookingService.emailCancellationToCustomer(customer.getEmailIDCus(), messageBody);

			// we now set the cancellation flag
			status = bookingService.setBookingCancelledFlag(booking.getBooking_id(), true);

			// add comment
			String previousComments = booking.getApplication().getComments();
			if (previousComments == null)
				previousComments = "";

			status = bookingService.setBookingExpiryDueFlag(booking.getBooking_id(), true); // SET EXPIRED FLAG
			bpu.addBookingComment(booking.getBooking_id(), "LEASE CANCELLED", previousComments, "");

			if (emailSent && status == 1) {
				response = Response.build("Success", "Booking " + booking.getBooking_ref()
						+ " was successfully cancelled - cancellation email sent to the customer", true);
			}

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // cancelBooking

	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_BOOKING
			+ Constants.PATH_EMAIL_BOOKING, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response emailBooking(@RequestBody ProcessBooking b) {

		Booking booking;

		try {
			if (bookValid.isNullOrEmpty(b)) {
				response = Response.build("Error",
						"Mandatory parameters : [staff_email_id / booking_ref] cannot be null or empty", false);
				return response;
			} // isNull check

			// check to see if the staff user exists on the database
			if (!cpu.userExists(b.getStaff_email_id())) {
				response = Response.build("Error",
						"Staff email " + b.getStaff_email_id() + " does not exist on the database", false);
				return response;
			} // staff checks

			// get all the booking information ...
			booking = bookingService.getBooking(b.getBooking_ref());

			// check that the booking exists ...
			if (booking.getBooking_id() == 0) {
				response = Response.build("Error",
						"Booking ref " + b.getBooking_ref() + " does not exist on the database", false);
				return response;
			} // bookingExists

			// now that we have validation completed, we need to carry out cancellations
			// procedures ...
			// delete booking
			int status = 0;

			/// format and set dates in java format
			// we need to convert the dates from SQL format (yyyy-MM-dd)
			// into Java format (dd/MM/yyyy)
			String dateOfBooking = availability.formatDateForJava(booking.getDate_of_booking().toString());
			String startDate = availability.formatDateForJava(booking.getLease_start_date().toString());
			String endDate = availability.formatDateForJava(booking.getLease_end_date().toString());

			// we now check if the kiosk is still available for the given dates

			booking.setStart_date(startDate);
			booking.setEnd_date(endDate);
			booking.setBooking_date(dateOfBooking);

			// next we send an email to the customer that the booking has been cancelled.
			// an email with the booking details will be sent to the successful customer
			EmailHeader emailHeader = new EmailHeader("Booking Summary");

			Customer customer = customerService.getCustomerDetails(booking.getCustomer_ID());
			Quote quote = bookingService.getQuote(booking.getQuoteRef());
			quote.setStart_date(startDate);
			quote.setEnd_date(endDate);

			System.out.println(quote.toString());
			KioskQuery kQuery = bpu.checkAvailability(quote, b.getStaff_email_id());

			// generate booking email
			String bookingDetails = bpu.generateBookingEmailRepeat(booking, customer, kQuery);

			// emailHeader.setHeaderTitle("Booking Cancellation");
			String messageBody = emailHeader.toString() + "\n" + bookingDetails;

			boolean emailSent;
			String emailSentTo = "";

			if (b.getEmail_booking_to() == null || b.getEmail_booking_to().isEmpty()) {
				// we now have to email the quotation to the customer
				emailSent = bookingService.emailBookingToCustomer(customer.getEmailIDCus(), messageBody);
				emailSentTo = "BOOKING EMAIL SENT TO CUSTOMER";
			} else {
				// send booking to custom email
				emailSent = bookingService.emailBookingToCustomer(b.getEmail_booking_to(), messageBody);
				emailSentTo = "BOOKING EMAIL SENT TO " + b.getEmail_booking_to();
			}

			// add comment
			String previousComments = booking.getApplication().getComments();
			if (previousComments == null)
				previousComments = "";

			status = bookingService.setBookingExpiryDueFlag(booking.getBooking_id(), true); // SET EXPIRED FLAG
			bpu.addBookingComment(booking.getBooking_id(), emailSentTo, previousComments, "");

			if (emailSent && status == 1) {
				response = Response.build("Success",
						"Booking " + booking.getBooking_ref() + " was successfully emailed", true);
			}

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // emailBooking

} // BookingController
