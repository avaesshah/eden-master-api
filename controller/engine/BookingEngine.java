package com.eden.api.controller.engine;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.eden.api.service.AccountService;
import com.eden.api.service.BookingService;
import com.eden.api.service.CustomerService;
import com.eden.api.service.LocationService;
import com.eden.api.service.QueryService;
import com.eden.api.service.UserService;
import com.eden.api.util.Constants;
import com.edenstar.model.Booking;
import com.edenstar.model.Calendar;
import com.edenstar.model.Customer;
import com.edenstar.model.Discount;
import com.edenstar.model.Kiosk;
import com.edenstar.model.Location;
import com.edenstar.model.Quote;
import com.edenstar.model.Rate;
import com.edenstar.model.User;
import com.edenstar.model.Zone;
import com.edenstar.model.booking.AddQuote;
import com.edenstar.model.booking.DateSlice;
import com.edenstar.model.booking.GetQuote;
import com.edenstar.model.booking.KioskQuery;
import com.edenstar.model.booking.QueryLocation;

@Component
public class BookingEngine implements QueryService {

	Availability availability = new Availability();

	@Autowired
	private LocationService locationService;

	@Autowired
	private UserService userService;

	@Autowired
	private BookingService bookingService;

	@Autowired
	private CustomerService customerService;
	
	@Autowired
	private AccountService accountService;

	@Autowired
	public BookingEngine() {
		super();
	}

	@Override
	public KioskQuery checkAvailability(Quote q, String staffEmail) throws Exception {

		// make sure that the kiosk is not void or locked
		Kiosk k = new Kiosk();
		KioskQuery k_query = new KioskQuery();

		try {
			k = locationService.getKioskByID(q.getKiosk_ID());

			// we now have to obtain rate and availablity information about the kiosk

			// stage 1 : obtain rate information by zone id
			Rate r = new Rate();
			r = locationService.getRate(k.getZoneID());

			// stage 2: obtain zone infomration
			Zone z = new Zone();
			z = locationService.getZone(k.getZoneID());

			// stage 4: create KioskQuery model and set the lease duration

			k_query.setLease_duration(q.getLease_duration_days());

			Location _location = locationService.getLocation(z.getLocationID());
			k_query.setLocation_name(_location.getLocationName().toUpperCase());
			k_query.setLocation_area(_location.getLocationArea().toUpperCase());

			
			// stage 5: create QueryLocation model
			QueryLocation l = new QueryLocation();
			l.setStaff_email_id(staffEmail);
			l.setStartDate(q.getStart_date());
			l.setEndDate(q.getEnd_date());
			l.setLocation_id(z.getLocationID());
			
			// stage 6: build query model by binding all the information together
			// k_query = availability.mapKiosk(k_query, k, r, z); - this is the old pre-discount model
			// we now employ a new model which requires the discount list and from and to dates of the
			// lease to be send to mapKiosk
			List<Discount> discountList = locationService.getDiscountByRateID(r.getRateID());
			k_query = availability.mapKiosk(k_query, k, r, z, discountList, l);

			// stage 7: obtain the date availablility for the kiosk
			// here we get the calendar availability for the kiosk
			k_query.setDateList(new ArrayList<DateSlice>(processKioskAvailability(k_query, l)));

			// stage 8: once we have the availability, we need to find out if all the days
			// are available
			availability.processFlags(k_query);

			// stage 9: format dates
			availability.formatDates(k_query);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return k_query;

	} // checkAvailability

	private List<DateSlice> processKioskAvailability(KioskQuery k, QueryLocation l) {
		// we now generate an array of DateSlices from the input JSON date range to
		// perform our date comparison operations
		// first we need to covert the input String dates into DateTime type objects
		List<DateSlice> dateListIn = new ArrayList<DateSlice>();

		String startDateIn = l.getStartDate();
		String endDateIn = l.getEndDate();

		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

		Date startDate = null;
		Date endDate = null;

		try {

			startDate = format.parse(startDateIn);
			endDate = format.parse(endDateIn);

			DateTime start_date = new DateTime(startDate);
			DateTime end_date = new DateTime(endDate);

			// we now generate an array of DateSlices to perform our date operations
			// dateListIn = availability.generateDateList(start_date, end_date);
			dateListIn = new ArrayList<DateSlice>(availability.generateDateList(start_date, end_date));

			// first step : convert incoming date range into a list of dates
			// we need to know the start date and end date then calculate the number days
			List<ArrayList<DateSlice>> kioskBookingList = getKioskCalendar(k, l);

			// we now have the dateslice array for the user's input and the kiosk's bookings
			// in dateslice format. We need to send both of these to the availability
			// utility
			// which would compare the each date in the input datesclice array with all the
			// dates
			// in the bookingList array to see if any matches are present, if they are then
			// availability flag for that particular day is set to false and the calender_id
			// is assigned.

			dateListIn = new ArrayList<>(availability.checkAvailability(dateListIn, kioskBookingList));

		} catch (Exception e) {
			e.printStackTrace();
		}

		return dateListIn;

	} // processKioskAvailability

	private List<ArrayList<DateSlice>> getKioskCalendar(KioskQuery k, QueryLocation l) {
		// The booking calender for each kiosk is consulted against the
		// required booking dates, and the results of the calendar are
		// attached to the kiosk
		List<Calendar> bookingCalendar = new ArrayList<>();
		List<ArrayList<DateSlice>> bookingList = new ArrayList<ArrayList<DateSlice>>();

		try {

			bookingCalendar = new ArrayList<>(bookingService.getBookingsForKiosk(k.getKioskID()));

			// once the booking calender has been populated we have to convert the dates
			// into
			// DateSlices and add the list of DateSlice to another List called bookingList
			int i = 0;
			for (i = 0; i < bookingCalendar.size(); i++) {

				ArrayList<DateSlice> dateListDB = new ArrayList<DateSlice>();

				dateListDB = new ArrayList<>(availability.getDateSliceList(bookingCalendar.get(i)));
				bookingList.add(dateListDB);

			} // for

		} catch (Exception e) {

			e.printStackTrace();
		}

		return bookingList;

	} // getKioskCalendar

	private String listCompanies(AddQuote q) {
		String companiesList = "";

		for (int i = 0; i < q.getCompanyList().size(); i++) {
			companiesList = companiesList + q.getCompanyList().get(i).getCompanyName();

			if (i == q.getCompanyList().size() - 1) {

			} else {
				companiesList = companiesList + " , ";
			}
		}

		return companiesList;

	}

	@Override
	public String generateApplicationReport(GetQuote getQuote, User manager_details, Quote quote, AddQuote q) {
		// TODO Auto-generated method stub
		User salesPerson = new User();

		// now we have to add the expiry duration to quote date to obtain the
		// expiry date
		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		String todaysDate = formatter.format(date);

		// copy over company info
		try {
			q.setCompanyList(customerService.getCustComp(quote.getCustomer_ID()));
			salesPerson = userService.getUserDetails(q.getStaff_email_id());

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		StringBuffer b = new StringBuffer();
		String spaces = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
		for (int i = 0; i < manager_details.getFirstName().length() + 10; i++) {
			spaces = spaces + "&nbsp;";
		}

		b.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"//fonts.googleapis.com/css?family=Ubuntu\" />");
		b.append("<div align=\"left\">" + "Dear " + manager_details.getFirstName() + "," + "</br>");
		b.append(spaces + "please could you review the booking application given below. Thank you. " + "</br></br>");
		b.append("<b>Application ID : </b>" + getQuote.getApplication_id() + "</br>");
		b.append("<b>Quotation Reference : </b>" + quote.getQuoteRef() + "</br>");
		b.append("<b>Application Date : </b>" + todaysDate + "</br></br>");
		b.append("<b>LOCATION</b>" + "</br></br>");
		b.append("<b>Location : </b>" + getQuote.getKioskQuery().getLocation_name() + "</br>");
		b.append("<b>Location Area: </b>" + getQuote.getKioskQuery().getLocation_area() + "</br>");
		b.append("<b>Zone Name : </b>" + getQuote.getKioskQuery().getZone().getZoneName() + "</br>");
		b.append("<b>Zone Number : </b>" + getQuote.getKioskQuery().getZone().getZoneNumber() + "</br>");
		b.append("<b>Kiosk Number : </b>" + getQuote.getKioskQuery().getKioskNumber() + "</br></br>");
		b.append("<b>CUSTOMER DETAILS</b>" + "</br></br>");
		b.append("<b>Customer Name : </b>" + getQuote.getCustomer().getFirstName() + " "
				+ getQuote.getCustomer().getLastName() + "</br>");
		b.append("<b>Customer Email : </b>" + getQuote.getCustomer().getEmailIDCus() + "</br>");
		b.append("<b>Customer Contact : </b>" + getQuote.getCustomer().getMobileNumber() + "</br>");
		b.append("<b>Company(s): </b>" + listCompanies(q) + "</br></br>");
		b.append("<b>LEASE DETAILS</b>" + "</br></br>");
		b.append("<b>Lease Start Date : </b>" + getQuote.getStart_date() + "</br>");
		b.append("<b>Lease End Date : </b>" + getQuote.getEnd_date() + "</br>");
		b.append("<b>Lease Duration : </b>" + getQuote.getLease_duration_days() + " days" + "</br>");
		b.append("<b>Daily Rate : </b>" + getQuote.getRate() + " AED" + "</br>");
		b.append("<b>Total Leasing Cost : </b>" + getQuote.getLease_total() + " AED" + "</br></br>");
		b.append("<b>SALES TEAM</b>" + "</br></br>");
		b.append("<b>Sales Person Dealing : </b>" + salesPerson.getFirstName() + " " + salesPerson.getLastName()
				+ "</br>");
		b.append("<b>Sales Email : </b>" + salesPerson.getEmailID() + "</br>");
		b.append("</br></br></br>" + "<b>Sales Team<b>" + "</br></br>");

		return b.toString();
	}

	@Override
	public boolean isKioskAvailable(Quote quote, String staff_email_id) throws Exception {
		boolean isAvailable = true;

		// we need to check to see if the kiosk is still available
		KioskQuery kQuery = new KioskQuery();
		kQuery = checkAvailability(quote, staff_email_id);

		GetQuote getQuote = new GetQuote(quote);

		// set the kioskQuery information
		getQuote.setKioskQuery(kQuery);

		// get customer details
		getQuote.setCustomer(customerService.getCustomerDetails(getQuote.getCustomer_ID()));

		availability.setAvailibilityFlag(getQuote, kQuery);

		// update response according to the availability status
		switch (kQuery.getAvailability_code()) {

		case Constants.avail_c:
			isAvailable = true;
			break;

		case Constants.lock_avail_c:
			isAvailable = true;
			break;

		case Constants.not_avail_c:
			isAvailable = false;
			break;

		case Constants.part_avail_c:
			isAvailable = false;
			break;

		case Constants.void_avail_c:
			isAvailable = false;
			break;

		} // switch statement

		return isAvailable;

	} // isKioskAvailable

	@Override
	public String generateBookingEmail(Booking booking, Customer customer, KioskQuery kQuery) throws Exception {
		User salesPerson = new User();

		// now we have to add the expiry duration to quote date to obtain the
		// expiry date
		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		String todaysDate = formatter.format(date);

		// copy over company info
		try {

			salesPerson = userService.getUserDetailsByEmpID(booking.getApplication().getSalesID());

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		StringBuffer b = new StringBuffer();
		String spaces = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";

		for (int i = 0; i < (customer.getFirstName().length() + customer.getLastName().length() + 14); i++) {
			spaces = spaces + "&nbsp;";
		}

		b.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"//fonts.googleapis.com/css?family=Ubuntu\" />");
		b.append("<div align=\"left\">" + "Dear " + customer.getFirstName() + " " + customer.getLastName() + ","
				+ "</br>");
		b.append(spaces
				+ "we are delighted to inform you that your booking application for the kiosk below has been approved."
				+ "</br></br>");
		b.append(
				"Please find a summary of your booking below. A copy of the lease agreement will be sent out to you in due course, in the meantime"
						+ "</br>");
		b.append("if you have any questions please don't hesitate to contact us with the reference below. Thank you. "
				+ "</br></br>");

		b.append("<font size=\"2\">");
		b.append("<b>Booking  Reference : </b>" + booking.getBooking_ref() + "</br>");
		b.append("<b>Booking Date : </b>" + todaysDate + "</br></br>");

		b.append("<font size=\"3\">" + "<b>Kiosk</b>" + "</font></br></br>");
		b.append("<b>Location : </b>" + kQuery.getLocation_name().toUpperCase() + "</br>");
		b.append("<b>Location Area: </b>" + kQuery.getLocation_area() + "</br>");
		b.append("<b>Zone Name : </b>" + kQuery.getZone().getZoneName() + "</br>");
		b.append("<b>Zone Number : </b>" + kQuery.getZone().getZoneNumber() + "</br>");
		b.append("<b>Kiosk Number : </b>" + kQuery.getKioskNumber() + "</br></br>");

		b.append("<font size=\"3\">" + "<b>Lease</b>" + "</font></br></br>");
		b.append("<b>Lease Start Date : </b>" + booking.getStart_date() + "</br>");
		b.append("<b>Lease End Date : </b>" + booking.getEnd_date() + "</br>");
		b.append("<b>Lease Duration : </b>" + booking.getLease_duration_days() + " days" + "</br></br>");
		b.append("<font size=\"4\">" + "<b>Leasing Total : " + booking.getLease_total() + " AED" + "</b></font>"
				+ "</br></br>");
		b.append("<b>Your sales contact : </b>" + salesPerson.getFirstName() + " " + salesPerson.getLastName()
				+ "</br>");
		b.append("<b>Contact : </b>" + salesPerson.getPhoneNumber() + "</br>");
		b.append("<b>Email : </b>" + salesPerson.getEmailID() + "</br></br></br>");
		b.append("Kind Regards" + "</br>");
		b.append("<b>Eden Star Sales Team<b>" + "</br></br>");

		return b.toString();
	} // generateBookingEmail

	@Override
	public String generateDeclineBookingEmail(Quote quote, Customer customer) throws Exception {

		// now we have to add the expiry duration to quote date to obtain the
		// expiry date
		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("EEEEE dd MMM yyyy");
		String todaysDate = formatter.format(date);

		StringBuffer b = new StringBuffer();
		String spaces = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";

		for (int i = 0; i < (customer.getFirstName().length() + customer.getLastName().length() + 14); i++) {
			spaces = spaces + "&nbsp;";
		}

		b.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"//fonts.googleapis.com/css?family=Ubuntu\" />");
		b.append("<font size=\"2\">" + "</br>");
		b.append("<div align=\"left\"><b>" + todaysDate + "</b></br>");
		b.append("<b>Reference: </b>" + quote.getQuoteRef() + "</br></br></br>");

		b.append("<div align=\"left\">" + "Dear " + customer.getFirstName() + " " + customer.getLastName() + ","
				+ "</br>");
		b.append(spaces
				+ "we thank you for your interest with Eden Star Bookings, however after reviewing your application we regretfully cannot proceed with your booking on this occasion."
				+ "</br>");
		b.append("</br>");
		b.append(
				"If you have any questions regarding this decision, please don't hesitate to contact us with the reference above. We welcome any future bookings you may take with us. Thank you."
						+ "</br></br>");
		b.append("</br></br>");

		b.append("Yours Sincerely" + "</br></br></br>");
		b.append("<b>Eden Star Sales Management<b>" + "</br></br>");

		return b.toString();
	} // generateDeclineBookingEmail

	@Override
	public void addBookingComment(int booking_id, String comment, String previousComments, String staff_email_id)
			throws Exception {

		if (previousComments == null)
			previousComments = "";

		User u = new User();
		String staff_name = "";
		// get employee information for timestamp

		if (staff_email_id.isEmpty() || staff_email_id.contentEquals("") || staff_email_id == null) {
			staff_name = "AUTOGEN";
		} else {
			u = userService.getUserDetails(staff_email_id);
			staff_name = u.getFirstName() + " " + u.getLastName();
		}

		// update comments to database
		// String dateStamp = new SimpleDateFormat("dd/MM/yyyy hh:mm").format(new
		// Date());

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
		LocalDateTime now = LocalDateTime.now();
		String dateStamp = dtf.format(now);

		String comments = "[ " + staff_name + " | " + dateStamp + " ]" + " : " + comment + " " + previousComments;

		// the review flag is automatically set
		int status = bookingService.addCommentsToBooking(booking_id, comments);

		if (status == 0) {
			System.out.println("Error: comments were not updated");
		}

	} // addBookingComment

	@Override
	public void addLeaseComment(int lease_id, String comment, String previousComments, String staff_email_id)
			throws Exception {
		if (previousComments == null)
			previousComments = "";

		User u = new User();
		String staff_name = "";
		// get employee information for timestamp

		if (staff_email_id.isEmpty() || staff_email_id.contentEquals("") || staff_email_id == null) {
			staff_name = "AUTOGEN";
		} else {
			u = userService.getUserDetails(staff_email_id);
			staff_name = u.getFirstName() + " " + u.getLastName();
		}

		// update comments to database
		// String dateStamp = new SimpleDateFormat("dd/MM/yyyy hh:mm").format(new
		// Date());

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
		LocalDateTime now = LocalDateTime.now();
		String dateStamp = dtf.format(now);

		String comments = "[ " + staff_name + " | " + dateStamp + " ]" + " : " + comment + " " + previousComments;

		// the review flag is automatically set
		int status = bookingService.addCommentsToLease(lease_id, comments);

		if (status == 0) {
			System.out.println("Error: comments were not updated");
		}

	} // addBookingComment

	@Override
	public void addApplicationComment(int applicationID, String comment, String previousComments, String staff_email_id)
			throws Exception {

		if (previousComments == null)
			previousComments = "";

		User u = new User();
		String staff_name = "";
		// get employee information for timestamp

		if (staff_email_id.isEmpty() || staff_email_id.contentEquals("") || staff_email_id == null) {
			staff_name = "AUTOGEN";
		} else {
			u = userService.getUserDetails(staff_email_id);
			staff_name = u.getFirstName() + " " + u.getLastName();
		}

		// update comments to database
		// String dateStamp = new SimpleDateFormat("dd/MM/yyyy hh:mm").format(new
		// Date());

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
		LocalDateTime now = LocalDateTime.now();
		String dateStamp = dtf.format(now);

		String comments = "[ " + staff_name + " | " + dateStamp + " ]" + " : " + comment + " " + previousComments;

		// the review flag is automatically set
		int status = bookingService.addCommentsToApplication(applicationID, comments);// .addCommentsToBooking(booking_id,
																						// comments);

		if (status == 0) {
			System.out.println("Error: comments were not updated");
		}

	} // addApplicationComment
	
	@Override
	public void addAccountComment(int account_id, String comment, String previousComments, String staff_email_id)
			throws Exception {
		if (previousComments == null) previousComments = "";
		
		User u = new User();
		String staff_name = "";
		// get employee information for timestamp

		if (staff_email_id.isEmpty() || staff_email_id.contentEquals("") || staff_email_id == null) {
			staff_name = "AUTOGEN";
		} else {
			u = userService.getUserDetails(staff_email_id);
			staff_name = u.getFirstName() + " " + u.getLastName();
		}

		// update comments to database
		//String dateStamp = new SimpleDateFormat("dd/MM/yyyy hh:mm").format(new Date());
		
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
		LocalDateTime now = LocalDateTime.now();
		String dateStamp = dtf.format(now);
		
		String comments = "[ " + staff_name + " | " + dateStamp + " ]" + " : " + comment + " " + previousComments;

		// the review flag is automatically set
		int status = accountService.addCommentsToAccount(account_id, comments);

		if (status == 0) {
			System.out.println("Error: comments were not updated");
		}

	} // addAcountComment
	
	@Override
	public void addPaymentComment(int payment_id, String comment, String previousComments, String staff_email_id)
			throws Exception {
		if (previousComments == null) previousComments = "";
		
		User u = new User();
		String staff_name = "";
		// get employee information for timestamp

		if (staff_email_id.isEmpty() || staff_email_id.contentEquals("") || staff_email_id == null) {
			staff_name = "AUTOGEN";
		} else {
			u = userService.getUserDetails(staff_email_id);
			staff_name = u.getFirstName() + " " + u.getLastName();
		}

		// update comments to database
		//String dateStamp = new SimpleDateFormat("dd/MM/yyyy hh:mm").format(new Date());
		
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
		LocalDateTime now = LocalDateTime.now();
		String dateStamp = dtf.format(now);
		
		String comments = "[ " + staff_name + " | " + dateStamp + " ]" + " : " + comment + " " + previousComments;

		// the review flag is automatically set
		int status = accountService.addCommentsToPayment(payment_id, comments);

		if (status == 0) {
			System.out.println("Error: comments were not updated");
		}

	} // addPaymentComment

	@Override
	public String generateBookingCancellationEmail(Booking booking, Customer customer, KioskQuery kQuery)
			throws Exception {
		User salesPerson = new User();

		// now we have to add the expiry duration to quote date to obtain the
		// expiry date
		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		String todaysDate = formatter.format(date);

		// copy over company info
		try {

			salesPerson = userService.getUserDetailsByEmpID(booking.getApplication().getSalesID());

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		StringBuffer b = new StringBuffer();
		String spaces = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";

		for (int i = 0; i < (customer.getFirstName().length() + customer.getLastName().length() + 14); i++) {
			spaces = spaces + "&nbsp;";
		}

		b.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"//fonts.googleapis.com/css?family=Ubuntu\" />");
		b.append("<div align=\"left\">" + "Dear " + customer.getFirstName() + " " + customer.getLastName() + ","
				+ "</br>");
		b.append(spaces
				+ "we like to inform you that your booking has been cancelled. Please find the details of the cancellation below. If you have any questions you can contact us with your booking reference given below."
				+ "</br></br>");

		b.append("<font size=\"2\">");
		b.append("<b>Booking  Reference : </b>" + booking.getBooking_ref() + "</br>");
		b.append("<b>Cancellation Date : </b>" + todaysDate + "</br></br>");

		b.append("<font size=\"3\">" + "<b>Kiosk</b>" + "</font></br></br>");
		b.append("<b>Location : </b>" + kQuery.getLocation_name().toUpperCase() + "</br>");
		b.append("<b>Location Area: </b>" + kQuery.getLocation_area() + "</br>");
		b.append("<b>Zone Name : </b>" + kQuery.getZone().getZoneName() + "</br>");
		b.append("<b>Zone Number : </b>" + kQuery.getZone().getZoneNumber() + "</br>");
		b.append("<b>Kiosk Number : </b>" + kQuery.getKioskNumber() + "</br></br>");

		b.append("<font size=\"3\">" + "<b>Lease</b>" + "</font></br></br>");
		b.append("<b>Lease Start Date : </b>" + booking.getStart_date() + "</br>");
		b.append("<b>Lease End Date : </b>" + booking.getEnd_date() + "</br>");
		b.append("<b>Lease Duration : </b>" + booking.getLease_duration_days() + " days" + "</br></br>");
		b.append("<font size=\"3\">" + "<b>Leasing Total : " + booking.getLease_total() + " AED - CANCELLED"
				+ "</b></font>" + "</br></br>");
		b.append("<b>Your sales contact : </b>" + salesPerson.getFirstName() + " " + salesPerson.getLastName()
				+ "</br>");
		b.append("<b>Contact : </b>" + salesPerson.getPhoneNumber() + "</br>");
		b.append("<b>Email : </b>" + salesPerson.getEmailID() + "</br></br></br>");
		b.append("Kind Regards" + "</br>");
		b.append("<b>Eden Star Sales Team<b>" + "</br></br>");

		return b.toString();
	} // generateBookingCancellationEmail

	@Override
	public String generateBookingEmailRepeat(Booking booking, Customer customer, KioskQuery kQuery) throws Exception {
		// TODO Auto-generated method stub
		User salesPerson = new User();

		// now we have to add the expiry duration to quote date to obtain the
		// expiry date
		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		String todaysDate = formatter.format(date);
		System.out.println("todays date = " + todaysDate);

		// copy over company info
		try {

			salesPerson = userService.getUserDetailsByEmpID(booking.getApplication().getSalesID());

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		StringBuffer b = new StringBuffer();
		String spaces = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";

		for (int i = 0; i < (customer.getFirstName().length() + customer.getLastName().length() + 14); i++) {
			spaces = spaces + "&nbsp;";
		}

		b.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"//fonts.googleapis.com/css?family=Ubuntu\" />");
		b.append("<div align=\"left\">" + "Dear " + customer.getFirstName() + " " + customer.getLastName() + ","
				+ "</br>");
		b.append(spaces
				+ "Please find your booking summary below, as requested. If you have any questions, please do not hesitate to contact our customer support team with your booking reference given below. Thank you."
				+ "</br></br>");

		b.append("<font size=\"2\">");
		b.append("<b>Booking  Reference : </b>" + booking.getBooking_ref() + "</br>");
		b.append("<b>Booking Date : </b>" + booking.getBooking_date() + "</br></br>");

		b.append("<font size=\"3\">" + "<b>Kiosk</b>" + "</font></br></br>");
		b.append("<b>Location : </b>" + kQuery.getLocation_name().toUpperCase() + "</br>");
		b.append("<b>Location Area: </b>" + kQuery.getLocation_area() + "</br>");
		b.append("<b>Zone Name : </b>" + kQuery.getZone().getZoneName() + "</br>");
		b.append("<b>Zone Number : </b>" + kQuery.getZone().getZoneNumber() + "</br>");
		b.append("<b>Kiosk Number : </b>" + kQuery.getKioskNumber() + "</br></br>");

		b.append("<font size=\"3\">" + "<b>Lease</b>" + "</font></br></br>");
		b.append("<b>Lease Start Date : </b>" + booking.getStart_date() + "</br>");
		b.append("<b>Lease End Date : </b>" + booking.getEnd_date() + "</br>");
		b.append("<b>Lease Duration : </b>" + booking.getLease_duration_days() + " days" + "</br></br>");
		b.append("<font size=\"4\">" + "<b>Leasing Total : " + booking.getLease_total() + " AED" + "</b></font>"
				+ "</br></br>");
		b.append("<b>Your sales contact : </b>" + salesPerson.getFirstName() + " " + salesPerson.getLastName()
				+ "</br>");
		b.append("<b>Contact : </b>" + salesPerson.getPhoneNumber() + "</br>");
		b.append("<b>Email : </b>" + salesPerson.getEmailID() + "</br></br></br>");
		b.append("Kind Regards" + "</br>");
		b.append("<b>Eden Star Sales Team<b>" + "</br></br>");

		return b.toString();
	} // generateBookingEmailRepeat

} // BookingEngine
