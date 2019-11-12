package com.eden.api.dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import com.eden.api.controller.engine.Availability;
import com.eden.api.util.AccountMapper;
import com.eden.api.util.ApplicationMapper;
import com.eden.api.util.BookingMapper;
import com.eden.api.util.Constants;
import com.eden.api.util.LeaseMapper;
import com.eden.api.util.QuoteMapper;
import com.edenstar.model.Account;
import com.edenstar.model.Application;
import com.edenstar.model.BookMap;
import com.edenstar.model.Booking;
import com.edenstar.model.Calendar;
import com.edenstar.model.Lease;
import com.edenstar.model.Quote;
import com.edenstar.model.booking.AddQuote;
import com.edenstar.model.booking.ApplicationStream;
import com.edenstar.model.booking.BookingStream;
import com.edenstar.model.booking.GetApplications;
import com.edenstar.model.booking.LeaseStream;

@Component
public class BookingDAOImpl implements BookingDAO {

	private JdbcTemplate jdbcTemplate;

	@Autowired
	public BookingDAOImpl(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public List<Calendar> getBookingForKiosk(int kioskID) throws Exception {

		String query = "SELECT * FROM calendar where is_archived = 0 and kiosk_id ='" + kioskID + "'";
		List<Calendar> calendarList = new ArrayList<Calendar>();
		List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();

		try {

			rows = jdbcTemplate.queryForList(query);

			for (Map<String, Object> row : rows) {

				Calendar c = new Calendar();

				c.setCalendarID((Integer) (row.get("calendar_id")));
				c.setKioskID((Integer) (row.get("kiosk_id")));
				c.setLeaseStartDate((Date) (row.get("lease_start_date")));
				c.setLeaseEndDate((Date) (row.get("lease_end_date")));
				c.setLeaseDurationDays((Integer) (row.get("lease_duration_days")));

				calendarList.add(c);
			}

		} catch (Exception e) {

		} // try

		return calendarList;

	} // getBookingForKiosk

	private java.sql.Date formatDateForDB(String dateStr) {

		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		Date dateForDB = null;
		DateTime date_SQL_format = null;

		try {

			dateForDB = format.parse(dateStr);

			date_SQL_format = new DateTime(dateForDB);

		} catch (Exception e) {
			e.printStackTrace();
		}

		DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
		String dateInString = date_SQL_format.toString(fmt);

		java.sql.Date newDate = java.sql.Date.valueOf(dateInString);

		return newDate;

	} // formatDateForDB

	@Override
	public int addQuote(AddQuote q) throws Exception {

		NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
		final KeyHolder keyHolder = new GeneratedKeyHolder();
		int status = 0;

		// format the dates
		java.sql.Date lease_start_date = formatDateForDB(q.getStartDate());
		java.sql.Date lease_end_date = formatDateForDB(q.getEndDate());
		long millis = System.currentTimeMillis();
		java.sql.Date date_of_quote = new java.sql.Date(millis);
		int isExpired = 0;

		final String INSERT_QUERY = "insert into quote (quote_ref, customer_id, kiosk_id, employee_id, date_of_quote,"
				+ "lease_start_date, lease_end_date, lease_duration_days, lease_total, rate, expiry_duration_days, is_expired, quotation_pdf) values"
				+ " (:quote_ref, :customer_id, :kiosk_id, :employee_id, :date_of_quote,"
				+ ":lease_start_date, :lease_end_date, :lease_duration_days, :lease_total, :rate, :expiry_duration_days, :is_expired, :quotation_pdf)";

		try {

			System.out.println("SQL start date = " + lease_start_date);
			System.out.println("SQL end_date" + lease_end_date);
			System.out.println("SQL Date of quote" + date_of_quote);

			// Creating map with all required params
			MapSqlParameterSource paramMap = new MapSqlParameterSource();

			paramMap.addValue("quote_ref", q.getQuoteRef());
			paramMap.addValue("customer_id", q.getCustomerID());
			paramMap.addValue("kiosk_id", q.getKiosk_id());
			paramMap.addValue("employee_id", q.getEmployee_id());
			paramMap.addValue("date_of_quote", date_of_quote);
			paramMap.addValue("lease_start_date", lease_start_date);
			paramMap.addValue("lease_end_date", lease_end_date);
			paramMap.addValue("lease_duration_days", q.getQ_kiosk().getLease_duration());
			paramMap.addValue("lease_total", q.getQ_kiosk().getLease_total());
			paramMap.addValue("rate", q.getQ_kiosk().getDaily_rate());
			paramMap.addValue("expiry_duration_days", q.getExpiry_duration_days());
			paramMap.addValue("is_expired", isExpired);
			paramMap.addValue("quotation_pdf", q.getQuotationPDF());

			status = namedJdbcTemplate.update(INSERT_QUERY, paramMap, keyHolder, new String[] { "quote_id" });

			// get the auto generated primary key from the new customer insertion
			Number generatedComID = keyHolder.getKey();
			status = generatedComID.intValue();

			System.out.println("new quote ID = " + status);

		} catch (Exception e) {

		} // try

		return status;
	} // addQuote

	@Override
	public Quote getQuote(String quoteRef) throws Exception {

		Quote q = new Quote();
		String query = "SELECT * FROM quote where quote_ref ='" + quoteRef.toUpperCase() + "'";

		try {

			q = jdbcTemplate.queryForObject(query, new QuoteMapper());

		} catch (Exception e) {

		} // try

		return q;

	} // getQuote

	@Override
	public int deleteQuote(int quote_ID) throws Exception {
		int status = 0;

		final String DELETE_QUERY = "update quote set is_archived = 1 where quote_id = ?"; // "delete from quote where
																							// quote_id = ?";

		// define query arguments
		Object[] params = { quote_ID };

		status = jdbcTemplate.update(DELETE_QUERY, params);
		System.out.println("delete status = " + status);

		return status;

	} // deleteQuote

	@Override
	public int deleteApplication(int application_id) throws Exception {
		int status = 0;

		final String DELETE_QUERY = "update application set is_archived = 1 where application_id = ?";// "delete from
																										// application
																										// where
																										// application_id
																										// = ?";

		// define query arguments
		Object[] params = { application_id };

		status = jdbcTemplate.update(DELETE_QUERY, params);
		System.out.println("delete status = " + status);

		return status;

	} // deleteApplication

	@Override
	public int deleteBooking(int booking_id) throws Exception {
		int status = 0;

		final String DELETE_QUERY = "update booking set is_archived = 1 where booking_id = ?";// "delete from booking
																								// where booking_id =
																								// ?";

		// define query arguments
		Object[] params = { booking_id };

		status = jdbcTemplate.update(DELETE_QUERY, params);
		System.out.println("delete status = " + status);

		return status;

	} // deleteBooking

	@Override
	public int deleteCalendar(int calendar_id) throws Exception {
		int status = 0;

		final String DELETE_QUERY = "update calendar set is_archived = 1 where calendar_id = ?";// "delete from calendar
																								// where calendar_id =
																								// ?";

		// define query arguments
		Object[] params = { calendar_id };

		status = jdbcTemplate.update(DELETE_QUERY, params);
		System.out.println("delete status = " + status);

		return status;

	} // deleteCalendar

	@Override
	public int setExpiredFlag(int quote_ID) throws Exception {
		// TODO Auto-generated method stub
		int status = 0;

		final String UPDATE_QUERY = "update quote set is_expired = 1 where quote_id = ?";

		// define query arguments
		Object[] params = { quote_ID };

		status = jdbcTemplate.update(UPDATE_QUERY, params);
		System.out.println("expired status = " + status);

		return status;
	} // setExpiredFlag

	@Override
	public int setApplicationExpiredFlag(int application_id, boolean isFlagON) throws Exception {
		// TODO Auto-generated method stub
		int status = 0;
		int flagPosition = 0;

		if (isFlagON)
			flagPosition = 1;

		final String UPDATE_QUERY = "update application set is_expired = ? , is_archived = 1 where application_id = ?";

		// define query arguments
		Object[] params = { flagPosition, application_id };

		status = jdbcTemplate.update(UPDATE_QUERY, params);
		System.out.println("expired status = " + status);

		return status;
	} // setApplicationExpiredFlag

	@Override
	public int setApplicationDeclinedFlag(int application_id, boolean isFlagON) throws Exception {
		// TODO Auto-generated method stub
		int status = 0;
		int flagPosition = 0;

		if (isFlagON)
			flagPosition = 1;

		final String UPDATE_QUERY = "update application set is_declined = ? where application_id = ?";

		// define query arguments
		Object[] params = { flagPosition, application_id };

		status = jdbcTemplate.update(UPDATE_QUERY, params);
		System.out.println("expired status = " + status);

		return status;
	} // setApplicationDeclinedFlag

	@Override
	public int setKioskLock(int kiosk_ID, boolean lock) throws Exception {
		// TODO Auto-generated method stub
		int status = 0;
		int lockInt = 0;

		if (lock)
			lockInt = 1;
		if (!lock)
			lockInt = 0;

		final String UPDATE_QUERY = "update kiosk set is_locked = ? where kiosk_id = ?";

		// define query arguments
		Object[] params = { lockInt, kiosk_ID };

		status = jdbcTemplate.update(UPDATE_QUERY, params);
		System.out.println("locked status = " + status);

		return status;
	} // setkioskLock

	@Override
	public int addApplication(AddQuote q) throws Exception {

		NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
		final KeyHolder keyHolder = new GeneratedKeyHolder();
		int status = 0;

		// format the dates
		long millis = System.currentTimeMillis();
		java.sql.Date date_of_application = new java.sql.Date(millis);
		int is_approved = 0;

		final String INSERT_QUERY = "insert into application (quote_id, date_of_application, review_flag, comments, is_approved,"
				+ "manager_id, sales_id) values (:quote_id, :date_of_application, :review_flag, :comments, :is_approved, :manager_id, :sales_id)";

		try {

			System.out.println("SQL Date of application: " + date_of_application);

			// Creating map with all required params
			MapSqlParameterSource paramMap = new MapSqlParameterSource();

			paramMap.addValue("quote_id", q.getQuote_id());
			paramMap.addValue("date_of_application", date_of_application);
			paramMap.addValue("review_flag", q.getReview_flag());
			paramMap.addValue("comments", q.getComments());
			paramMap.addValue("is_approved", is_approved); // default is 0
			paramMap.addValue("manager_id", q.getEmployee_id());
			paramMap.addValue("sales_id", q.getSales_id());

			status = namedJdbcTemplate.update(INSERT_QUERY, paramMap, keyHolder, new String[] { "application_id" });

			// get the auto generated primary key from the new customer insertion
			Number generatedComID = keyHolder.getKey();
			status = generatedComID.intValue();

			System.out.println("new application ID = " + status);

		} catch (Exception e) {

		} // try

		return status;
	} // addApplication

	@Override
	public List<GetApplications> getApplications(int employeeID) {

		List<GetApplications> applicationList = new ArrayList<GetApplications>();

		// first we obtain a list of applications
		String query = "SELECT * FROM application where is_archived = 0 and manager_id ='" + employeeID + "'";
		List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();

		try {

			rows = jdbcTemplate.queryForList(query);

			for (Map<String, Object> row : rows) {

				GetApplications a = new GetApplications();

				a.setApplicationID((Integer) (row.get("application_id")));
				a.setQuoteID((Integer) (row.get("quote_id")));
				a.setDateOfApplication((Date) (row.get("date_of_application")));
				a.setReviewFlag((Integer) (row.get("review_flag")));
				a.setComments((String) (row.get("comments")));
				a.setIsApproved((Integer) (row.get("is_approved")));
				a.setManagerID((Integer) (row.get("manager_id")));
				a.setSalesID((Integer) (row.get("sales_id")));
				a.setSecurity_deposit((String) (row.get("security_deposit")));
				a.setIsDeclined((Integer) (row.get("is_declined")));
				a.setIsExpired((Integer) (row.get("is_expired")));

				applicationList.add(a);
			}

		} catch (Exception e) {

		} // try

		return applicationList;

	} // getApplications

	@Override
	public Quote getQuote(int quote_id) throws Exception {
		Quote q = new Quote();
		String query = "SELECT * FROM quote where quote_id ='" + quote_id + "'";

		try {

			q = jdbcTemplate.queryForObject(query, new QuoteMapper());

		} catch (Exception e) {

		} // try

		return q;
	} // getQuote

	@Override
	public Application getApplication(int application_id) throws Exception {

		Application a = new Application();
		String query = "SELECT * FROM application where application_id ='" + application_id + "'";

		try {

			a = jdbcTemplate.queryForObject(query, new ApplicationMapper());

		} catch (Exception e) {

		} // try

		return a;
	} // getApplication

	@Override
	public int extendExpiry(int quote_ID) throws Exception {
		int status = 0;

		final String UPDATE_QUERY = "update quote set expiry_duration_days = ? + expiry_duration_days where quote_id = ?";

		// define query arguments
		Object[] params = { Constants.extend_expiry_when_application, quote_ID };

		status = jdbcTemplate.update(UPDATE_QUERY, params);
		System.out.println("update status = " + status);

		return status;
	}

	@Override
	public List<ApplicationStream> getApplicationCustomers(int manager_id) throws Exception {

		String query = "SELECT * FROM location "
				+ "natural join zone natural join kiosk natural join quote natural join customer natural join application "
				+ "where is_archived = 0 and manager_id = '" + manager_id + "'";

		List<ApplicationStream> appList = new ArrayList<ApplicationStream>();
		List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();

		try {

			rows = jdbcTemplate.queryForList(query);

			for (Map<String, Object> row : rows) {

				ApplicationStream a = new ApplicationStream();

				a.setApplication_id((Integer) (row.get("application_id")));
				a.setQuote_ref((String) (row.get("quote_ref")));
				a.setIs_approved((Integer) (row.get("is_approved")));
				a.setDate_of_application(formatDateFoJava(row.get("date_of_application").toString()));
				a.setFirst_name((String) (row.get("first_name")));
				a.setLast_name((String) (row.get("last_name")));
				a.setLease_start_date(formatDateFoJava(row.get("lease_start_date").toString()));
				a.setLease_end_date(formatDateFoJava((row.get("lease_end_date")).toString()));
				a.setLocation_name((String) (row.get("location_name")).toString().toUpperCase());
				a.setLocation_area((String) (row.get("location_area")));
				a.setZone_number((Integer) (row.get("zone_number")));
				a.setZone_name((String) (row.get("zone_name")));
				a.setCustomer_id((Integer) (row.get("customer_id")));

				appList.add(a);
			}

		} catch (Exception e) {

		} // try

		return appList;

	} // getApplicationCustomers

	private String formatDateFoJava(String dateStr) throws Exception {

		Availability availability = new Availability();
		String dateForJava = "";

		try {

			dateForJava = availability.formatDateForJava(dateStr);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return dateForJava;

	} // formatDateForJava

	@Override
	public int addBooking(Booking booking) throws Exception {

		NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
		final KeyHolder keyHolder = new GeneratedKeyHolder();
		int status = 0;

		// format the dates
		java.sql.Date lease_start_date = formatDateForDB(booking.getStart_date());
		java.sql.Date lease_end_date = formatDateForDB(booking.getEnd_date());
		long millis = System.currentTimeMillis();
		java.sql.Date date_of_booking = new java.sql.Date(millis);

		final String INSERT_QUERY = "insert into booking (application_id, booking_ref, quote_ref, calendar_id, customer_id, sales_id, manager_id, kiosk_id, "
				+ "date_of_booking, comments, lease_start_date, lease_end_date, lease_duration_days, rate, lease_total, security_deposit) values"
				+ " (:application_id, :booking_ref, :quote_ref, :calendar_id, :customer_id, :sales_id, :manager_id, :kiosk_id, "
				+ ":date_of_booking, :comments, :lease_start_date, :lease_end_date, :lease_duration_days, :rate, :lease_total, :security_deposit)";

		try {

			System.out.println("SQL start date = " + lease_start_date);
			System.out.println("SQL end_date" + lease_end_date);
			System.out.println("SQL Date of booking" + date_of_booking);

			// Creating map with all required params
			MapSqlParameterSource paramMap = new MapSqlParameterSource();

			paramMap.addValue("application_id", booking.getApplication().getApplicationID());
			paramMap.addValue("quote_ref", booking.getQuoteRef());
			paramMap.addValue("calendar_id", booking.getCalendar_id());
			paramMap.addValue("customer_id", booking.getCustomer_ID());
			paramMap.addValue("manager_id", booking.getApplication().getManagerID());
			paramMap.addValue("booking_ref", booking.getBooking_ref());
			paramMap.addValue("kiosk_id", booking.getKiosk_ID());
			paramMap.addValue("sales_id", booking.getApplication().getSalesID());
			paramMap.addValue("date_of_booking", date_of_booking);
			paramMap.addValue("lease_start_date", lease_start_date);
			paramMap.addValue("lease_end_date", lease_end_date);
			paramMap.addValue("lease_duration_days", booking.getLease_duration_days());
			paramMap.addValue("lease_total", booking.getLease_total());
			paramMap.addValue("rate", booking.getRate());
			paramMap.addValue("comments", booking.getApplication().getComments());
			paramMap.addValue("security_deposit", booking.getApplication().getSecurity_deposit());

			status = namedJdbcTemplate.update(INSERT_QUERY, paramMap, keyHolder, new String[] { "booking_id" });

			// get the auto generated primary key from the new customer insertion
			Number generatedComID = keyHolder.getKey();
			status = generatedComID.intValue();

			System.out.println("new booking ID = " + status);

		} catch (Exception e) {

		} // try

		return status;
	} // addBooking

	@Override
	public int addDatesToKioskCalendar(Booking booking) throws Exception {

		NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
		final KeyHolder keyHolder = new GeneratedKeyHolder();
		int status = 0;

		// format the dates
		java.sql.Date lease_start_date = formatDateForDB(booking.getStart_date());
		java.sql.Date lease_end_date = formatDateForDB(booking.getEnd_date());

		final String INSERT_QUERY = "insert into calendar (kiosk_id, lease_start_date, lease_end_date, lease_duration_days) values"
				+ " (:kiosk_id, :lease_start_date, :lease_end_date, :lease_duration_days)";

		try {

			System.out.println("SQL start date = " + lease_start_date);
			System.out.println("SQL end_date" + lease_end_date);

			// Creating map with all required params
			MapSqlParameterSource paramMap = new MapSqlParameterSource();

			paramMap.addValue("kiosk_id", booking.getKiosk_ID());
			paramMap.addValue("lease_start_date", lease_start_date);
			paramMap.addValue("lease_end_date", lease_end_date);
			paramMap.addValue("lease_duration_days", booking.getLease_duration_days());

			status = namedJdbcTemplate.update(INSERT_QUERY, paramMap, keyHolder, new String[] { "calendar_id" });

			// get the auto generated primary key from the new customer insertion
			Number generatedComID = keyHolder.getKey();
			status = generatedComID.intValue();

			System.out.println("new calendar ID = " + status);

		} catch (Exception e) {

		} // try

		return status;
	} // addDatesToKioskCalendar

	@Override
	public int updateDepositURL(int application_id, String deposit_url) throws Exception {
		int status = 0;

		final String UPDATE_QUERY = "update application set security_deposit = ? where application_id = ?";

		// define query arguments
		Object[] params = { deposit_url, application_id };

		status = jdbcTemplate.update(UPDATE_QUERY, params);
		System.out.println("update status = " + status);

		return status;
	}

	@Override
	public List<Quote> getQuotes() throws Exception {

		List<Quote> quoteList = new ArrayList<Quote>();

		// first we obtain a list of applications
		String query = "SELECT * FROM quote where is_archived = 0 and is_expired = 0 and is_submitted = 0";
		List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();

		try {

			rows = jdbcTemplate.queryForList(query);

			for (Map<String, Object> row : rows) {

				Quote q = new Quote();

				q.setQuote_ID((Integer) row.get("quote_id"));
				q.setQuoteRef((String) row.get("quote_ref"));
				q.setCustomer_ID((Integer) row.get("customer_id"));
				q.setKiosk_ID((Integer) row.get("kiosk_id"));
				q.setEmployee_ID((Integer) row.get("employee_id"));
				q.setDate_of_quote((java.sql.Date) row.get("date_of_quote"));

				q.setLease_start_date((java.sql.Date) row.get("lease_start_date"));
				q.setLease_end_date((java.sql.Date) row.get("lease_end_date"));
				q.setLease_duration_days((Integer) row.get("lease_duration_days"));
				q.setLease_total((Double) row.get("lease_total"));
				q.setRate((Double) row.get("rate"));
				q.setExpiry_duration_days((Integer) row.get("expiry_duration_days"));
				q.setIsExpired((Integer) row.get("is_expired"));
				q.setQuotationPdf((byte[]) row.get("quotation_pdf"));

				/// format and set dates in java format
				// we need to convert the dates from SQL format (yyyy-MM-dd)
				// into Java format (dd/MM/yyyy)
				Availability availability = new Availability();
				String startDate = availability.formatDateForJava(q.getLease_start_date().toString());
				String endDate = availability.formatDateForJava(q.getLease_end_date().toString());
				String dateOfQuote = availability.formatDateForJava(q.getDate_of_quote().toString());
				// we now check if the kiosk is still available for the given dates

				q.setStart_date(startDate);
				q.setEnd_date(endDate);
				q.setQuote_date(dateOfQuote);

				quoteList.add(q);
			}

		} catch (Exception e) {

		} // try

		return quoteList;

	} // getQuote

	@Override
	public List<ApplicationStream> getAllApplications(String mode) throws Exception {
		String query = "";

		if (mode.contentEquals("*"))
			query = "SELECT * FROM location "
					+ "natural join zone natural join kiosk natural join quote natural join customer natural join application where application.is_archived = 0";

		if (mode.contentEquals("declined"))
			query = "SELECT * FROM location "
					+ "natural join zone natural join kiosk natural join quote natural join customer natural join application "
					+ "where application.is_archived = 0 and application.is_declined = 1";

		if (mode.contentEquals("expired"))
			query = "SELECT * FROM location "
					+ "natural join zone natural join kiosk natural join quote natural join customer natural join application "
					+ "where application.is_expired = 1";

		if (mode.contentEquals("expired_nondeleted"))
			query = "SELECT * FROM location "
					+ "natural join zone natural join kiosk natural join quote natural join customer natural join application "
					+ "where application.is_archived = 0 and application.is_expired = 1";

		if (mode.contentEquals("approved"))
			query = "SELECT * FROM application \n" + "inner join quote on application.quote_id = quote.quote_id\n"
					+ "inner join kiosk on quote.kiosk_id = kiosk.kiosk_id\n"
					+ "inner join zone on kiosk.zone_id = zone.zone_id\n"
					+ "inner join location on zone.location_id = location.location_id\n"
					+ "inner join customer on quote.customer_id = customer.customer_id\n"
					+ "where application.is_approved = 1 \n" + "group by application_id;";

		if (mode.contentEquals("review"))
			query = "SELECT * FROM location "
					+ "natural join zone natural join kiosk natural join quote natural join customer natural join application "
					+ "where application.is_archived = 0 and application.review_flag = 1";

		if (mode.contentEquals("pending"))
			query = "SELECT * FROM location "
					+ "natural join zone natural join kiosk natural join quote natural join customer natural join application "
					+ "where application.is_approved = 0 AND application.is_declined = 0 AND application.is_expired = 0  AND application.is_archived = 0";

		if (mode.contentEquals("company"))
			query = "SELECT * FROM company natural join customer natural join quote natural join kiosk natural join zone natural join location natural join application where company.is_archived = 0 group by application_id";

		if (mode.contentEquals("company_pending"))
			query = "select * from application \n" + "inner join quote on application.quote_id = quote.quote_id\n"
					+ "inner join kiosk on quote.kiosk_id = kiosk.kiosk_id\n"
					+ "inner join zone on kiosk.zone_id = zone.zone_id\n"
					+ "inner join location on zone.location_id = location.location_id\n"
					+ "inner join customer on quote.customer_id = customer.customer_id\n"
					+ "inner join company on company.customer_id = customer.customer_id\n"
					+ "where application.is_archived = 0 and application.is_approved = 0 and application.is_archived = 0 and application.is_declined = 0\n"
					+ "group by application_id;";

		List<ApplicationStream> appList = new ArrayList<ApplicationStream>();
		List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();

		try {

			rows = jdbcTemplate.queryForList(query);

			for (Map<String, Object> row : rows) {

				ApplicationStream a = new ApplicationStream();

				a.setApplication_id((Integer) (row.get("application_id")));
				a.setQuote_ref((String) (row.get("quote_ref")));
				a.setIs_approved((Integer) (row.get("is_approved")));
				a.setIs_declined((Integer) (row.get("is_declined")));
				a.setIs_expired((Integer) (row.get("is_expired")));
				a.setDate_of_application(formatDateFoJava(row.get("date_of_application").toString()));
				a.setFirst_name((String) (row.get("first_name")));
				a.setLast_name((String) (row.get("last_name")));
				a.setLease_start_date(formatDateFoJava(row.get("lease_start_date").toString()));
				a.setLease_end_date(formatDateFoJava((row.get("lease_end_date")).toString()));
				a.setLocation_name((String) (row.get("location_name")).toString().toUpperCase());
				a.setLocation_area((String) (row.get("location_area")));
				a.setZone_number((Integer) (row.get("zone_number")));
				a.setZone_name((String) (row.get("zone_name")));
				a.setCustomer_id((Integer) (row.get("customer_id")));
				a.setCompany_name((String) (row.get("company_name")));
				// if (mode.contentEquals("company"))

				appList.add(a);
			}

		} catch (Exception e) {

		} // try

		return appList;

	} // getAllApplications

	@Override
	public int setIsApprovedFlag(int application_id, boolean isFlagON) throws Exception {
		// TODO Auto-generated method stub
		int status = 0;
		int flagPosition = 0;

		if (isFlagON)
			flagPosition = 1;

		final String UPDATE_QUERY = "update application set is_approved = ? where application_id = ?";

		// define query arguments
		Object[] params = { flagPosition, application_id };

		status = jdbcTemplate.update(UPDATE_QUERY, params);
		System.out.println("approved status = " + status);

		return status;
	} // setIsApprovedFlag

	@Override
	public int updateRevisedLease(Quote quote, Application application) throws Exception {
		int status = 0;
		long millis = System.currentTimeMillis();
		java.sql.Date date_of_quote = new java.sql.Date(millis);

		final String UPDATE_QUERY = "update quote set lease_total = ? , rate = ? , date_of_quote = ? where quote_id = ?";

		// define query arguments
		Object[] params = { quote.getLease_total(), quote.getRate(), date_of_quote, quote.getQuote_ID() };

		status = jdbcTemplate.update(UPDATE_QUERY, params);
		System.out.println("update status = " + status);

		return status;
	} // updateRevisedLease

	@Override
	public int setApplicationReviewFlag(int application_id, boolean isFlagON) throws Exception {
		// TODO Auto-generated method stub
		int status = 0;
		int flagPosition = 0;

		if (isFlagON)
			flagPosition = 1;

		final String UPDATE_QUERY = "update application set review_flag = ? where application_id = ?";

		// define query arguments
		Object[] params = { flagPosition, application_id };

		status = jdbcTemplate.update(UPDATE_QUERY, params);
		System.out.println("approved status = " + status);

		return status;
	} // setApplicationReviewFLag

	@Override
	public int addCommentsToApplication(int application_id, String comments) throws Exception {
		int status = 0;

		final String UPDATE_QUERY = "update application set comments = ? , review_flag = 0 where application_id = ?";

		// define query arguments
		Object[] params = { comments, application_id };

		status = jdbcTemplate.update(UPDATE_QUERY, params);
		System.out.println("update status = " + status);

		return status;
	} // addCommentsToApplication

	@Override
	public Booking getBooking(String booking_ref) throws Exception {

		BookMap b = new BookMap();
		Application a = new Application();

		System.out.println("booking ref = " + booking_ref);
		String query = "SELECT * FROM booking where booking_ref = '" + booking_ref + "'";

		try {

			// define query arguments
			// Object[] params = { bookingRef };

			b = jdbcTemplate.queryForObject(query, new BookingMapper());

		} catch (Exception e) {

		} // try

		a.setApplicationID(b.getApplicationID());
		a.setSalesID(b.getSales_id());
		a.setManagerID(b.getManager_id());
		a.setComments(b.getComments());
		a.setSecurity_deposit(b.getSecurity_deposit());
		Booking booking = new Booking(b);
		booking.setApplication(a);

		return booking;
	} // getBooking

	@Override
	public int setBookingExpiredFlag(int booking_id, boolean isFlagON) throws Exception {
		// TODO Auto-generated method stub
		int status = 0;
		int flagPosition = 0;

		if (isFlagON)
			flagPosition = 1;

		final String UPDATE_QUERY = "update booking set is_expired = ? where booking_id = ?";

		// define query arguments
		Object[] params = { flagPosition, booking_id };

		status = jdbcTemplate.update(UPDATE_QUERY, params);
		System.out.println("approved status = " + status);

		return status;
	} // setBookingExpiredFlag

	@Override
	public int setBookingCancelledFlag(int booking_id, boolean isFlagON) throws Exception {
		// TODO Auto-generated method stub
		int status = 0;
		int flagPosition = 0;

		if (isFlagON)
			flagPosition = 1;

		final String UPDATE_QUERY = "update booking set is_cancelled = ? where booking_id = ?";

		// define query arguments
		Object[] params = { flagPosition, booking_id };

		status = jdbcTemplate.update(UPDATE_QUERY, params);
		System.out.println("approved status = " + status);

		return status;
	} // setBookingCancelledFlag

	@Override
	public int setBookingReviewFlag(int booking_id, boolean isFlagON) throws Exception {
		// TODO Auto-generated method stub
		int status = 0;
		int flagPosition = 0;

		if (isFlagON)
			flagPosition = 1;

		final String UPDATE_QUERY = "update booking set review_flag = ? where booking_id = ?";

		// define query arguments
		Object[] params = { flagPosition, booking_id };

		status = jdbcTemplate.update(UPDATE_QUERY, params);
		System.out.println("approved status = " + status);

		return status;
	} // setBookingReviewFlag

	@Override
	public int setBookingExpiryDueFlag(int booking_id, boolean isFlagON) throws Exception {
		// TODO Auto-generated method stub
		int status = 0;
		int flagPosition = 0;

		if (isFlagON)
			flagPosition = 1;

		final String UPDATE_QUERY = "update booking set is_expiry_due = ? where booking_id = ?";

		// define query arguments
		Object[] params = { flagPosition, booking_id };

		status = jdbcTemplate.update(UPDATE_QUERY, params);
		System.out.println("approved status = " + status);

		return status;
	} // setBookingExpiryDueFlag

	@Override
	public int addCommentsToBooking(int booking_id, String comments) throws Exception {
		int status = 0;

		final String UPDATE_QUERY = "update booking set comments = ? , review_flag = 0 where booking_id = ?";

		// define query arguments
		Object[] params = { comments, booking_id };

		status = jdbcTemplate.update(UPDATE_QUERY, params);
		System.out.println("update status = " + status);

		return status;
	} // add comments to booking

	@Override
	public List<BookingStream> getAllBookings(String mode) throws Exception {
		String query = "";

		System.out.println("booking mode = " + mode);

		if (mode.contentEquals("*"))
			query = "SELECT * FROM booking \n" + "inner join quote on booking.quote_ref = quote.quote_ref\n"
					+ "inner join kiosk on quote.kiosk_id = kiosk.kiosk_id\n"
					+ "inner join zone on kiosk.zone_id = zone.zone_id\n"
					+ "inner join location on zone.location_id = location.location_id\n"
					+ "inner join customer on booking.customer_id = customer.customer_id \n"
					+ "where booking.is_archived = 0 \n" + "group by booking_id;";

		// query = "SELECT * FROM location natural join zone natural join kiosk natural
		// join customer natural join booking where booking.is_archived = 0 ";

		if (mode.contentEquals("cancelled"))
			query = "SELECT * FROM booking \n" + "inner join quote on booking.quote_ref = quote.quote_ref\n"
					+ "inner join kiosk on quote.kiosk_id = kiosk.kiosk_id\n"
					+ "inner join zone on kiosk.zone_id = zone.zone_id\n"
					+ "inner join location on zone.location_id = location.location_id\n"
					+ "inner join customer on booking.customer_id = customer.customer_id \n"
					+ "where booking.is_cancelled = 1 \n" + "group by booking_id;";

		// query = "SELECT * FROM location natural join zone natural join kiosk natural
		// join customer natural join booking where booking.is_archived = 0 and
		// booking.is_cancelled = 1";

		if (mode.contentEquals("review"))
			query = "SELECT * FROM booking \n" + "inner join quote on booking.quote_ref = quote.quote_ref\n"
					+ "inner join kiosk on quote.kiosk_id = kiosk.kiosk_id\n"
					+ "inner join zone on kiosk.zone_id = zone.zone_id\n"
					+ "inner join location on zone.location_id = location.location_id\n"
					+ "inner join customer on booking.customer_id = customer.customer_id \n"
					+ "where booking.is_archived = 0 and booking.review_flag \n" + "group by booking_id;";

		// query = "SELECT * FROM location natural join zone natural join kiosk natural
		// join customer natural join booking where booking.is_archived = 0 and
		// booking.review_flag = 1";

		if (mode.contentEquals("expired"))
			query = "SELECT * FROM booking \n" + "inner join quote on booking.quote_ref = quote.quote_ref\n"
					+ "inner join kiosk on quote.kiosk_id = kiosk.kiosk_id\n"
					+ "inner join zone on kiosk.zone_id = zone.zone_id\n"
					+ "inner join location on zone.location_id = location.location_id\n"
					+ "inner join customer on booking.customer_id = customer.customer_id \n"
					+ "where booking.is_expired = 1 \n" + "group by booking_id;";
		// query = "SELECT * FROM location natural join zone natural join kiosk natural
		// join customer natural join booking where booking.is_expired = 1";

		if (mode.contentEquals("expired_nondeleted"))
			query = "SELECT * FROM booking \n" + "inner join quote on booking.quote_ref = quote.quote_ref\n"
					+ "inner join kiosk on quote.kiosk_id = kiosk.kiosk_id\n"
					+ "inner join zone on kiosk.zone_id = zone.zone_id\n"
					+ "inner join location on zone.location_id = location.location_id\n"
					+ "inner join customer on booking.customer_id = customer.customer_id \n"
					+ "where booking.is_archived = 0 and booking.is_expired = 1 \n" + "group by booking_id;";

		// query = "SELECT * FROM location natural join zone natural join kiosk natural
		// join customer natural join booking where booking.is_archived = 0 and
		// booking.is_expired = 1";

		if (mode.contentEquals("expiry_due"))
			query = "SELECT * FROM booking \n" + "inner join quote on booking.quote_ref = quote.quote_ref\n"
					+ "inner join kiosk on quote.kiosk_id = kiosk.kiosk_id\n"
					+ "inner join zone on kiosk.zone_id = zone.zone_id\n"
					+ "inner join location on zone.location_id = location.location_id\n"
					+ "inner join customer on booking.customer_id = customer.customer_id \n"
					+ "where booking.is_archived = 0 and booking.is_expiry_due = 1 \n" + "group by booking_id;";

		// query = "SELECT * FROM location natural join zone natural join kiosk natural
		// join customer natural join booking where booking.is_archived = 0 and
		// booking.is_expiry_due = 1";

		if (mode.contentEquals("company"))
			query = "SELECT * FROM booking \n" + "inner join quote on booking.quote_ref = quote.quote_ref\n"
					+ "inner join kiosk on quote.kiosk_id = kiosk.kiosk_id\n"
					+ "inner join zone on kiosk.zone_id = zone.zone_id\n"
					+ "inner join location on zone.location_id = location.location_id\n"
					+ "inner join customer on booking.customer_id = customer.customer_id \n"
					+ "inner join company on company.customer_id = customer.customer_id\n"
					+ "where booking.is_archived = 0 \n" + "group by booking_id;";

		// query = "SELECT * FROM location natural join zone natural join kiosk natural
		// join customer natural join company natural join booking where
		// booking.is_archived = 0 group by booking_id";

		List<BookingStream> bookList = new ArrayList<BookingStream>();
		List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();

		try {

			rows = jdbcTemplate.queryForList(query);

			System.out.println("number of rows = " + rows.size());

			for (Map<String, Object> row : rows) {

				BookingStream b = new BookingStream();

				b.setBooking_id((Integer) (row.get("booking_id")));
				b.setBooking_ref((String) (row.get("booking_ref")));

				b.setIs_cancelled((Integer) (row.get("is_cancelled")));
				b.setIs_expiry_due((Integer) (row.get("is_expiry_due")));
				b.setIs_expired((Integer) (row.get("is_expired")));
				b.setReview_flag((Integer) (row.get("review_flag")));

				b.setDate_of_booking(formatDateFoJava(row.get("date_of_booking").toString()));
				b.setFirst_name((String) (row.get("first_name")));
				b.setLast_name((String) (row.get("last_name")));

				b.setLease_start_date(formatDateFoJava(row.get("lease_start_date").toString()));
				b.setLease_end_date(formatDateFoJava((row.get("lease_end_date")).toString()));
				b.setLease_total((Double) (row.get("lease_total")));
				b.setLocation_name((String) (row.get("location_name")).toString().toUpperCase());
				b.setLocation_area((String) (row.get("location_area")));
				b.setZone_number((Integer) (row.get("zone_number")));
				b.setZone_name((String) (row.get("zone_name")));
				b.setKiosk_number((Integer) (row.get("kiosk_number")));

				b.setCustomer_id((Integer) (row.get("customer_id")));
				b.setCompany_name((String) (row.get("company_name")));
				// if (mode.contentEquals("company"))

				// System.out.println(b.toString());

				bookList.add(b);
			}

		} catch (Exception e) {

		} // try

		return bookList;

	} // getAllApplications

	@Override
	public List<BookingStream> getBookingByField(String mode, int field_id) throws Exception {
		String query = "";

		System.out.println("booking mode = " + " field_id = " + field_id);

		if (mode.contentEquals("sales"))
			query = "SELECT * FROM booking\n" + "inner join quote on booking.quote_ref = quote.quote_ref\n"
					+ "inner join kiosk on quote.kiosk_id = kiosk.kiosk_id\n"
					+ "inner join zone on kiosk.zone_id = zone.zone_id\n"
					+ "inner join location on zone.location_id = location.location_id\n"
					+ "inner join customer on booking.customer_id = customer.customer_id \n"
					+ "where booking.sales_id = " + field_id + " \n"
					+ "group by booking_id;";
			
			//query = "SELECT * FROM location natural join zone natural join kiosk natural join customer natural join booking where booking.is_archived = 0 and booking.sales_id = "
			//		+ field_id;

//		if (mode.contentEquals("sales_history"))
//			query = "SELECT * FROM booking\n" + 
//					"inner join quote on booking.quote_ref = quote.quote_ref\n" + 
//					"inner join kiosk on quote.kiosk_id = kiosk.kiosk_id\n" + 
//					"inner join zone on kiosk.zone_id = zone.zone_id\n" + 
//					"inner join location on zone.location_id = location.location_id\n" + 
//					"inner join customer on booking.customer_id = customer.customer_id \n" + 
//					"where booking.sales_id = " + field_id +"\n" + 
//					"group by booking_id;";
			
			//query = "SELECT * FROM location natural join zone natural join kiosk natural join customer natural join booking where booking.sales_id = "
			//		+ field_id;

		if (mode.contentEquals("manager"))
			query = "SELECT * FROM booking\n" + "inner join quote on booking.quote_ref = quote.quote_ref\n"
					+ "inner join kiosk on quote.kiosk_id = kiosk.kiosk_id\n"
					+ "inner join zone on kiosk.zone_id = zone.zone_id\n"
					+ "inner join location on zone.location_id = location.location_id\n"
					+ "inner join customer on booking.customer_id = customer.customer_id \n"
					+ "where booking.manager_id = " + field_id + " \n"
					+ "group by booking_id;";
			
			//query = "SELECT * FROM location natural join zone natural join kiosk natural join customer natural join booking where booking.is_archived = 0 and booking.manager_id = "
			//		+ field_id;

		if (mode.contentEquals("customer"))
			query = "SELECT * FROM booking\n" + "inner join quote on booking.quote_ref = quote.quote_ref\n"
					+ "inner join kiosk on quote.kiosk_id = kiosk.kiosk_id\n"
					+ "inner join zone on kiosk.zone_id = zone.zone_id\n"
					+ "inner join location on zone.location_id = location.location_id\n"
					+ "inner join customer on booking.customer_id = customer.customer_id \n"
					+ "where booking.customer_id = " + field_id + " \n"
					+ "group by booking_id;";
						
			//query = "SELECT * FROM location natural join zone natural join kiosk natural join customer natural join booking where booking.is_archived = 0 and booking.customer_id = "
			//		+ field_id;

		if (mode.contentEquals("kiosk"))
			query = "SELECT * FROM booking\n" + "inner join quote on booking.quote_ref = quote.quote_ref\n"
					+ "inner join kiosk on quote.kiosk_id = kiosk.kiosk_id\n"
					+ "inner join zone on kiosk.zone_id = zone.zone_id\n"
					+ "inner join location on zone.location_id = location.location_id\n"
					+ "inner join customer on booking.customer_id = customer.customer_id \n"
					+ "inner join company on company.customer_id = customer.customer_id\n"
					+ "where booking.is_archived = 0 and booking.kiosk_id = " + field_id + " \n"
					+ "group by booking_id;";

		// query = "SELECT * FROM location natural join zone natural join kiosk natural
		// join customer natural join booking where booking.is_archived = 0 and
		// booking.kiosk_id = "
		// + field_id;

		if (mode.contentEquals("application"))
			query = "SELECT * FROM booking\n" + "inner join quote on booking.quote_ref = quote.quote_ref\n"
					+ "inner join kiosk on quote.kiosk_id = kiosk.kiosk_id\n"
					+ "inner join zone on kiosk.zone_id = zone.zone_id\n"
					+ "inner join location on zone.location_id = location.location_id\n"
					+ "inner join customer on booking.customer_id = customer.customer_id \n"
					+ "where booking.application_id = " + field_id + " \n"
					+ "group by booking_id;";
			
			//query = "SELECT * FROM location natural join zone natural join kiosk natural join customer natural join booking where booking.is_archived = 0 and booking.application_id = "
			//		+ field_id;

		if (mode.contentEquals("booking"))
			query = "SELECT * FROM booking\n" + "inner join quote on booking.quote_ref = quote.quote_ref\n"
					+ "inner join kiosk on quote.kiosk_id = kiosk.kiosk_id\n"
					+ "inner join zone on kiosk.zone_id = zone.zone_id\n"
					+ "inner join location on zone.location_id = location.location_id\n"
					+ "inner join customer on booking.customer_id = customer.customer_id \n"
					+ "where booking.booking_id = " + field_id + " \n"
					+ "group by booking_id;";
			
	    //	query = "SELECT * FROM location natural join zone natural join kiosk natural join customer natural join booking where booking.is_archived = 0 and booking.booking_id = "
		//			+ field_id;

		if (mode.contentEquals("location"))
			query = "SELECT * FROM booking\n" + "inner join quote on booking.quote_ref = quote.quote_ref\n"
					+ "inner join kiosk on quote.kiosk_id = kiosk.kiosk_id\n"
					+ "inner join zone on kiosk.zone_id = zone.zone_id\n"
					+ "inner join location on zone.location_id = location.location_id\n"
					+ "inner join customer on booking.customer_id = customer.customer_id \n"
					+ "where booking.is_archived = 0 and location.location_id = " + field_id + " \n"
					+ "group by booking_id;";
			
			//query = "SELECT * FROM location natural join zone natural join kiosk natural join customer natural join booking where booking.is_archived = 0 and location.location_id = "
			//		+ field_id;

		if (mode.contentEquals("zone"))
			query = "SELECT * FROM booking\n" + "inner join quote on booking.quote_ref = quote.quote_ref\n"
					+ "inner join kiosk on quote.kiosk_id = kiosk.kiosk_id\n"
					+ "inner join zone on kiosk.zone_id = zone.zone_id\n"
					+ "inner join location on zone.location_id = location.location_id\n"
					+ "inner join customer on booking.customer_id = customer.customer_id \n"
					+ "where booking.is_archived = 0 and zone.zone_id = " + field_id + " \n"
					+ "group by booking_id;";
			
			//query = "SELECT * FROM location natural join zone natural join kiosk natural join customer natural join booking where booking.is_archived = 0 and zone.zone_id = "
			//		+ field_id;

		if (mode.contentEquals("rate"))
			query = "SELECT * FROM booking\n" + "inner join quote on booking.quote_ref = quote.quote_ref\n"
					+ "inner join kiosk on quote.kiosk_id = kiosk.kiosk_id\n"
					+ "inner join zone on kiosk.zone_id = zone.zone_id\n"
					+ "inner join rate on rate.zone_id = zone.zone_id\n"
					+ "inner join location on zone.location_id = location.location_id\n"
					+ "inner join customer on booking.customer_id = customer.customer_id \n"
					+ "where booking.is_archived = 0 and rate.rate_id  = " + field_id + " \n"
					+ "group by booking_id;";
			
			//query = "SELECT * FROM location natural join zone natural join kiosk natural join customer natural join booking where booking.is_archived = 0 and rate.rate_id = "
			//		+ field_id;

		List<BookingStream> bookList = new ArrayList<>();
		List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();

		try {

			rows = jdbcTemplate.queryForList(query);

			System.out.println("number of rows = " + rows.size());

			for (Map<String, Object> row : rows) {

				BookingStream b = new BookingStream();

				b.setBooking_id((Integer) (row.get("booking_id")));
				b.setBooking_ref((String) (row.get("booking_ref")));

				b.setIs_cancelled((Integer) (row.get("is_cancelled")));
				b.setIs_expiry_due((Integer) (row.get("is_expiry_due")));
				b.setIs_expired((Integer) (row.get("is_expired")));
				b.setReview_flag((Integer) (row.get("review_flag")));
				b.setComments((String) (row.get("comments")));

				b.setDate_of_booking(formatDateFoJava(row.get("date_of_booking").toString()));
				b.setFirst_name((String) (row.get("first_name")));
				b.setLast_name((String) (row.get("last_name")));

				b.setLease_start_date(formatDateFoJava(row.get("lease_start_date").toString()));
				b.setLease_end_date(formatDateFoJava((row.get("lease_end_date")).toString()));
				b.setLease_total((Double) (row.get("lease_total")));
				b.setLocation_name((String) (row.get("location_name")).toString().toUpperCase());
				b.setLocation_area((String) (row.get("location_area")));
				b.setZone_number((Integer) (row.get("zone_number")));
				b.setZone_name((String) (row.get("zone_name")));
				b.setKiosk_number((Integer) (row.get("kiosk_number")));

				b.setCustomer_id((Integer) (row.get("customer_id")));
				b.setCompany_name((String) (row.get("company_name")));
				// if (mode.contentEquals("company"))

				// System.out.println(b.toString());

				bookList.add(b);
			}

		} catch (Exception e) {

		} // try

		return bookList;
	}

	@Override
	public List<BookingStream> getActiveBookings() throws Exception {

		String query = "SELECT * FROM booking where booking.is_archived = 0 and is_cancelled = 0 and is_expired = 0";

		List<BookingStream> bookList = new ArrayList<>();
		List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();

		try {

			rows = jdbcTemplate.queryForList(query);

			System.out.println("number of rows = " + rows.size());

			for (Map<String, Object> row : rows) {

				BookingStream b = new BookingStream();

				b.setBooking_id((Integer) (row.get("booking_id")));
				b.setBooking_ref((String) (row.get("booking_ref")));

				b.setIs_cancelled((Integer) (row.get("is_cancelled")));
				b.setIs_expiry_due((Integer) (row.get("is_expiry_due")));
				b.setIs_expired((Integer) (row.get("is_expired")));
				b.setReview_flag((Integer) (row.get("review_flag")));

				b.setLease_end_date(formatDateFoJava((row.get("lease_end_date")).toString()));
				b.setExpiry_due_period_days((Integer) (row.get("expiry_due_period_days")));
				b.setComments((String) (row.get("comments")));

				bookList.add(b);
			}

		} catch (Exception e) {

		} // try

		return bookList;
	} // get active bookings

	@Override
	public List<Quote> getActiveQuotes() throws Exception {
		// TODO Auto-generated method stub
		String query = "SELECT * FROM quote where is_archived = 0 and is_expired = 0";

		List<Quote> quoteList = new ArrayList<>();
		List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();

		try {

			rows = jdbcTemplate.queryForList(query);

			System.out.println("number of rows = " + rows.size());

			for (Map<String, Object> row : rows) {

				Quote q = new Quote();

				q.setQuote_ID((Integer) row.get("quote_id"));
				q.setQuoteRef((String) row.get("quote_ref"));
				q.setCustomer_ID((Integer) row.get("customer_id"));
				q.setKiosk_ID((Integer) row.get("kiosk_id"));
				q.setEmployee_ID((Integer) row.get("employee_id"));
				q.setDate_of_quote((java.sql.Date) row.get("date_of_quote"));

				q.setLease_start_date((java.sql.Date) row.get("lease_start_date"));
				q.setLease_end_date((java.sql.Date) row.get("lease_end_date"));
				q.setLease_duration_days((Integer) row.get("lease_duration_days"));
				q.setLease_total((Double) row.get("lease_total"));
				q.setRate((Double) row.get("rate"));
				q.setExpiry_duration_days((Integer) row.get("expiry_duration_days"));
				q.setIsExpired((Integer) row.get("is_expired"));

				quoteList.add(q);
			}

		} catch (Exception e) {

		} // try

		return quoteList;

	} // getActiveQuotes

	@Override
	public List<Application> getActiveApplications() throws Exception {
		List<Application> applicationList = new ArrayList<>();

		// first we obtain a list of applications
		// String query = "SELECT * FROM application where is_archived = 0 and
		// is_expired = 0";
		String query = "SELECT * FROM application where is_approved = 0 and is_declined = 0 and is_expired = 0 and is_archived = 0";
		List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();

		try {

			rows = jdbcTemplate.queryForList(query);

			for (Map<String, Object> row : rows) {

				Application a = new Application();

				a.setApplicationID((Integer) (row.get("application_id")));
				a.setQuoteID((Integer) (row.get("quote_id")));
				a.setDateOfApplication((Date) (row.get("date_of_application")));
				a.setReviewFlag((Integer) (row.get("review_flag")));
				a.setComments((String) (row.get("comments")));
				a.setIsApproved((Integer) (row.get("is_approved")));
				a.setManagerID((Integer) (row.get("manager_id")));
				a.setSalesID((Integer) (row.get("sales_id")));
				a.setSecurity_deposit((String) (row.get("security_deposit")));
				a.setIsDeclined((Integer) (row.get("is_declined")));
				a.setIsExpired((Integer) (row.get("is_expired")));

				applicationList.add(a);
			}

		} catch (Exception e) {

		} // try

		return applicationList;
	} // getActiveApplications

	@Override
	public List<Application> getDeclinedApplications() throws Exception {
		List<Application> applicationList = new ArrayList<>();

		// first we obtain a list of applications
		// String query = "SELECT * FROM application where is_archived = 0 and
		// is_expired = 0";
		String query = "SELECT * FROM application where is_approved = 0 and is_declined = 1 and is_expired = 0 and is_archived = 0";
		List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();

		try {

			rows = jdbcTemplate.queryForList(query);

			for (Map<String, Object> row : rows) {

				Application a = new Application();

				a.setApplicationID((Integer) (row.get("application_id")));
				a.setQuoteID((Integer) (row.get("quote_id")));
				a.setDateOfApplication((Date) (row.get("date_of_application")));
				a.setReviewFlag((Integer) (row.get("review_flag")));
				a.setComments((String) (row.get("comments")));
				a.setIsApproved((Integer) (row.get("is_approved")));
				a.setManagerID((Integer) (row.get("manager_id")));
				a.setSalesID((Integer) (row.get("sales_id")));
				a.setSecurity_deposit((String) (row.get("security_deposit")));
				a.setIsDeclined((Integer) (row.get("is_declined")));
				a.setIsExpired((Integer) (row.get("is_expired")));

				applicationList.add(a);
			}

		} catch (Exception e) {

		} // try

		return applicationList;
	} // getDeclinedApplications

	@Override
	public List<Application> getArchivedApplications() throws Exception {
		List<Application> applicationList = new ArrayList<>();

		String query = "SELECT * FROM application where is_archived = 1";
		List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();

		try {

			rows = jdbcTemplate.queryForList(query);

			for (Map<String, Object> row : rows) {

				Application a = new Application();

				a.setApplicationID((Integer) (row.get("application_id")));
				a.setQuoteID((Integer) (row.get("quote_id")));
				a.setDateOfApplication((Date) (row.get("date_of_application")));
				a.setReviewFlag((Integer) (row.get("review_flag")));
				a.setComments((String) (row.get("comments")));
				a.setIsApproved((Integer) (row.get("is_approved")));
				a.setManagerID((Integer) (row.get("manager_id")));
				a.setSalesID((Integer) (row.get("sales_id")));
				a.setSecurity_deposit((String) (row.get("security_deposit")));
				a.setIsDeclined((Integer) (row.get("is_declined")));
				a.setIsExpired((Integer) (row.get("is_expired")));

				applicationList.add(a);
			}

		} catch (Exception e) {

		} // try

		return applicationList;
	} // getArchivedApplications

	@Override
	public Booking getBookingByQuoteRef(String quote_ref) throws Exception {

		BookMap b = new BookMap();
		Application a = new Application();

		System.out.println("quote ref = " + quote_ref);
		String query = "select * from booking where is_archived = 0 and quote_ref = '" + quote_ref + "'";

		try {

			// define query arguments
			// Object[] params = { bookingRef };

			b = jdbcTemplate.queryForObject(query, new BookingMapper());

		} catch (Exception e) {

		} // try

		Booking booking = new Booking(b);
		booking.setApplication(a);

		return booking;
	} // getBookingByQuoteRef

	@Override
	public Application getApplicationByQuoteID(int quote_id) throws Exception {

		Application a = new Application();
		String query = "SELECT * FROM application where is_archived = 0 and application_id = '" + quote_id + "'";

		try {

			a = jdbcTemplate.queryForObject(query, new ApplicationMapper());

		} catch (Exception e) {

		} // try

		return a;
	} // getApplicationByQuoteID

	@Override
	public Booking getBookingByApplicationID(int applicationID) throws Exception {
		BookMap b = new BookMap();
		Application a = new Application();

		String query = "SELECT * FROM booking where is_archived = 0 and application_id = '" + applicationID + "'";

		try {

			// define query arguments
			// Object[] params = { bookingRef };

			b = jdbcTemplate.queryForObject(query, new BookingMapper());

		} catch (Exception e) {

		} // try

		a.setApplicationID(b.getApplicationID());
//		a.setSalesID(b.getSales_id());
//		a.setManagerID(b.getManager_id());
		a.setComments(b.getComments());
//		a.setSecurity_deposit(b.getSecurity_deposit());
		Booking booking = new Booking(b);
		booking.setApplication(a);

		return booking;
	} // getBookingByApplicationID

	@Override
	public List<Quote> getExpiredQuotes(String mode) throws Exception {
		List<Quote> quoteList = new ArrayList<Quote>();
		String query = "";

		// first we obtain a list of applications
		if (mode.contentEquals("expired"))
			query = "SELECT * FROM quote where is_expired = 1 and is_archived = 0";

		if (mode.contentEquals("expiredUnsubmitted"))
			query = "SELECT * FROM quote where is_expired = 1 and is_archived = 1 and is_submitted = 0";

		if (mode.contentEquals("deleted"))
			query = "SELECT * FROM quote where is_expired = 1 and is_archived = 1";

		if (mode.contentEquals("valid"))
			query = "SELECT * FROM quote where is_expired = 0 and is_archived = 0";

		List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();

		try {

			rows = jdbcTemplate.queryForList(query);

			for (Map<String, Object> row : rows) {

				Quote q = new Quote();

				q.setQuote_ID((Integer) row.get("quote_id"));
				q.setQuoteRef((String) row.get("quote_ref"));
				q.setCustomer_ID((Integer) row.get("customer_id"));
				q.setKiosk_ID((Integer) row.get("kiosk_id"));
				q.setEmployee_ID((Integer) row.get("employee_id"));
				q.setDate_of_quote((java.sql.Date) row.get("date_of_quote"));

				q.setLease_start_date((java.sql.Date) row.get("lease_start_date"));
				q.setLease_end_date((java.sql.Date) row.get("lease_end_date"));
				q.setLease_duration_days((Integer) row.get("lease_duration_days"));
				q.setLease_total((Double) row.get("lease_total"));
				q.setRate((Double) row.get("rate"));
				q.setExpiry_duration_days((Integer) row.get("expiry_duration_days"));
				q.setIsExpired((Integer) row.get("is_expired"));
				q.setQuotationPdf((byte[]) row.get("quotation_pdf"));

				/// format and set dates in java format
				// we need to convert the dates from SQL format (yyyy-MM-dd)
				// into Java format (dd/MM/yyyy)
				Availability availability = new Availability();
				String startDate = availability.formatDateForJava(q.getLease_start_date().toString());
				String endDate = availability.formatDateForJava(q.getLease_end_date().toString());
				String dateOfQuote = availability.formatDateForJava(q.getDate_of_quote().toString());
				// we now check if the kiosk is still available for the given dates

				q.setStart_date(startDate);
				q.setEnd_date(endDate);
				q.setQuote_date(dateOfQuote);

				quoteList.add(q);
			}

		} catch (Exception e) {

		} // try

		return quoteList;
	}

	@Override
	public boolean isBookingCancelled(int booking_id) throws Exception {
		// TODO Auto-generated method stub

		final String query = "select is_cancelled from booking where booking_id = ?";

		int result = 0;
		boolean isCancelled = false;

		try {

			// define query arguments
			Object[] params = { booking_id };
			result = jdbcTemplate.queryForObject(query, params, Integer.class);

		} catch (Exception e) {

		} // try

		if (result == 1)
			isCancelled = true;

		return isCancelled;
	} // isBookingCancelled

	@Override
	public int addLease(Lease lease) throws Exception {

		NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
		final KeyHolder keyHolder = new GeneratedKeyHolder();
		int status = 0;
		lease.setReviewFlag(0); // default position

		final String INSERT_QUERY = "insert into lease (booking_id, contract_url, comments, contract_signed_date,"
				+ "review_flag) values (:booking_id, :contract_url, :comments, :contract_signed_date,:review_flag)";

		try {

			// Creating map with all required params
			MapSqlParameterSource paramMap = new MapSqlParameterSource();

			paramMap.addValue("booking_id", lease.getBooking_id());
			paramMap.addValue("contract_url", lease.getContract_URL());
			paramMap.addValue("comments", lease.getComments());
			paramMap.addValue("contract_signed_date", lease.getContractSignedDate());
			paramMap.addValue("review_flag", lease.getReviewFlag());

			status = namedJdbcTemplate.update(INSERT_QUERY, paramMap, keyHolder, new String[] { "lease_id" });

			// get the auto generated primary key from the new customer insertion
			Number generatedComID = keyHolder.getKey();
			status = generatedComID.intValue();

			System.out.println("new lease ID = " + status);

		} catch (Exception e) {

		} // try

		return status;
	} // addLease

	@Override
	public List<LeaseStream> getAllLeases(String mode) throws Exception {
		String query = "";

		System.out.println("lease mode = " + mode);

		if (mode.contentEquals("*"))
			query = "select * from lease \n" + "inner join booking on lease.booking_id = booking.booking_id\n"
					+ "inner join customer on booking.customer_id = customer.customer_id\n"
					+ "inner join kiosk on booking.kiosk_id = kiosk.kiosk_id\n"
					+ "inner join zone on kiosk.zone_id = zone.zone_id\n"
					+ "inner join location on zone.location_id = location.location_id\n"
					+ "where lease.is_archived = 0\n" + "group by lease_id;";

		if (mode.contentEquals("pending"))
			query = "select * from lease \n" + "inner join booking on lease.booking_id = booking.booking_id\n"
					+ "inner join customer on booking.customer_id = customer.customer_id\n"
					+ "inner join kiosk on booking.kiosk_id = kiosk.kiosk_id\n"
					+ "inner join zone on kiosk.zone_id = zone.zone_id\n"
					+ "inner join location on zone.location_id = location.location_id\n"
					+ "where lease.is_archived = 0 and lease.contract_uploaded = 0\n" + "group by lease_id;";

		if (mode.contentEquals("expired"))
			query = "select * from lease \n" + "inner join booking on lease.booking_id = booking.booking_id\n"
					+ "inner join customer on booking.customer_id = customer.customer_id\n"
					+ "inner join kiosk on booking.kiosk_id = kiosk.kiosk_id\n"
					+ "inner join zone on kiosk.zone_id = zone.zone_id\n"
					+ "inner join location on zone.location_id = location.location_id\n"
					+ "where lease.is_archived = 1\n" + "group by lease_id;";

		if (mode.contentEquals("signed"))
			query = "select * from lease \n" + "inner join booking on lease.booking_id = booking.booking_id\n"
					+ "inner join customer on booking.customer_id = customer.customer_id\n"
					+ "inner join kiosk on booking.kiosk_id = kiosk.kiosk_id\n"
					+ "inner join zone on kiosk.zone_id = zone.zone_id\n"
					+ "inner join location on zone.location_id = location.location_id\n"
					+ "where lease.contract_uploaded = 1\n" + "group by lease_id;";

		List<LeaseStream> contractList = new ArrayList<>();
		List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();

		try {

			rows = jdbcTemplate.queryForList(query);

			System.out.println("number of rows = " + rows.size());

			for (Map<String, Object> row : rows) {

				LeaseStream b = new LeaseStream();

				b.setLease_id((Integer) (row.get("lease_id")));
				b.setContract_url((String) (row.get("contract_url")));
				b.setContract_signed_date(formatDateFoJava((row.get("lease_start_date")).toString()));
				b.setContractUploaded((Integer) (row.get("contract_uploaded")));

				b.setBooking_id((Integer) (row.get("booking_id")));
				b.setDate_of_booking(formatDateFoJava(row.get("date_of_booking").toString()));
				b.setBooking_ref((String) (row.get("booking_ref")));

//				b.setDate_of_booking(formatDateFoJava(row.get("date_of_booking").toString()));
				b.setFirst_name((String) (row.get("first_name")));
				b.setLast_name((String) (row.get("last_name")));

				b.setLease_start_date(formatDateFoJava(row.get("lease_start_date").toString()));
				b.setLease_end_date(formatDateFoJava((row.get("lease_end_date")).toString()));
				b.setLease_total((Double) (row.get("lease_total")));
				b.setLocation_name((String) (row.get("location_name")).toString().toUpperCase());
				b.setLocation_area((String) (row.get("location_area")));
				b.setZone_number((Integer) (row.get("zone_number")));
				b.setZone_name((String) (row.get("zone_name")));
				b.setKiosk_number((Integer) (row.get("kiosk_number")));

				b.setCustomer_id((Integer) (row.get("customer_id")));
				// b.setCompany_name((String) (row.get("company_name")));

				if (b.getContractUploaded() == 0) {
					b.setUpload_status("*** Please upload signed contract ***");
				} else {
					b.setUpload_status(" *** Contract signed and uploaded ***");
				}

				// System.out.println(b.toString());

				contractList.add(b);
			}

		} catch (Exception e) {

		} // try

		return contractList;
	} // getAllLeases

	@Override
	public int addCommentsToLease(int lease_id, String comments) throws Exception {
		// TODO Auto-generated method stub
		int status = 0;

		final String UPDATE_QUERY = "update lease set comments = ? , review_flag = 0 where lease_id = ?";

		// define query arguments
		Object[] params = { comments, lease_id };

		status = jdbcTemplate.update(UPDATE_QUERY, params);
		System.out.println("update status = " + status);

		return status;
	} // addCommentsToLease

	@Override
	public Lease getLease(int lease_id) throws Exception {

		Lease l = new Lease();
		String query = "SELECT * FROM lease where lease_id ='" + lease_id + "'";

		try {

			l = jdbcTemplate.queryForObject(query, new LeaseMapper());

		} catch (Exception e) {

		} // try

		return l;
	} // getLease

	@Override
	public Account getAccount(int account_id) throws Exception {

		Account a = new Account();
		String query = "SELECT * FROM account where account_id ='" + account_id + "'";

		try {

			a = jdbcTemplate.queryForObject(query, new AccountMapper());

		} catch (Exception e) {

		} // try

		return a;
	} // getAccount

	@Override
	public int updateContractURL(int lease_id, String urlToContractScan) throws Exception {
		int status = 0;
		long millis = System.currentTimeMillis();
		java.sql.Date contract_signed_date = new java.sql.Date(millis);

		final String UPDATE_QUERY = "update lease set contract_url = ?, contract_signed_date = ? , contract_uploaded = 1 where lease_id = ?";

		// define query arguments
		Object[] params = { urlToContractScan, contract_signed_date, lease_id };

		status = jdbcTemplate.update(UPDATE_QUERY, params);
		System.out.println("update status = " + status);

		return status;
	} // updateContractURL

	@Override
	public Lease getLeaseByBookingID(int booking_id) throws Exception {
		Lease l = new Lease();
		String query = "SELECT * FROM lease where booking_id ='" + booking_id + "'";

		try {

			l = jdbcTemplate.queryForObject(query, new LeaseMapper());

		} catch (Exception e) {

		} // try

		return l;

	} // get lease by booking id

	@Override
	public void setIsSubmittedFlag(int quote_id, boolean isFlagON) throws Exception {
		// TODO Auto-generated method stub
		int status = 0;
		int flagPosition = 0;

		if (isFlagON)
			flagPosition = 1;

		final String UPDATE_QUERY = "update quote set is_submitted = ? where quote_id = ?";

		// define query arguments
		Object[] params = { flagPosition, quote_id };

		status = jdbcTemplate.update(UPDATE_QUERY, params);
		System.out.println("submitted flag set status = " + status);

	} // setIsSubmittedFlag

	@Override
	public Booking getBookingByAccID(int account_id) throws Exception {
		// TODO Auto-generated method stub
		BookMap b = new BookMap();
		Application a = new Application();

		String query = "select * from booking \n" + "inner join lease on lease.booking_id = booking.booking_id\n"
				+ "inner join account on account.lease_id = lease.lease_id\n" + "where account_id ='" + account_id
				+ "'";

		try {

			// define query arguments
			// Object[] params = { bookingRef };

			b = jdbcTemplate.queryForObject(query, new BookingMapper());

		} catch (Exception e) {

		} // try

		Booking booking = new Booking(b);
		booking.setApplication(a);

		return booking;
	} // getBookingByAccID

	@Override
	public Application getApplicationByQuoteRef(String quoteRef) throws Exception {
		// TODO Auto-generated method stub
		Application a = new Application();
		String query = "select * from application\n" + "inner join quote on application.quote_id = quote.quote_id\n"
				+ "where quote_ref = '" + quoteRef.toUpperCase() + "'";

		try {

			a = jdbcTemplate.queryForObject(query, new ApplicationMapper());

		} catch (Exception e) {

		} // try

		return a;
	} // getApplicationByQuoteRef

	@Override
	public int archiveQuote(Quote quote) throws Exception {
		// TODO Auto-generated method stub
		NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
		final KeyHolder keyHolder = new GeneratedKeyHolder();
		int status = 0;

		long millis = System.currentTimeMillis();
		java.sql.Date date_of_archive = new java.sql.Date(millis);

		final String INSERT_QUERY = "insert into quote_arch (date_of_archive, quote_id, quote_ref, customer_id, kiosk_id, employee_id, date_of_quote,"
				+ "lease_start_date, lease_end_date, lease_duration_days, lease_total, rate, expiry_duration_days, is_expired, is_submitted) values"
				+ " (:date_of_archive, :quote_id, :quote_ref, :customer_id, :kiosk_id, :employee_id, :date_of_quote,"
				+ ":lease_start_date, :lease_end_date, :lease_duration_days, :lease_total, :rate, :expiry_duration_days, :is_expired, :is_submitted)";

		try {

			System.out.println("Date of archiving : " + date_of_archive);

			// Creating map with all required params
			MapSqlParameterSource paramMap = new MapSqlParameterSource();

			paramMap.addValue("date_of_archive", date_of_archive);
			paramMap.addValue("quote_id", quote.getQuote_ID());
			paramMap.addValue("quote_ref", quote.getQuoteRef());
			paramMap.addValue("customer_id", quote.getCustomer_ID());
			paramMap.addValue("kiosk_id", quote.getKiosk_ID());
			paramMap.addValue("employee_id", quote.getEmployee_ID());
			paramMap.addValue("date_of_quote", quote.getDate_of_quote());
			paramMap.addValue("lease_start_date", quote.getLease_start_date());
			paramMap.addValue("lease_end_date", quote.getLease_end_date());
			paramMap.addValue("lease_duration_days", quote.getLease_duration_days());
			paramMap.addValue("lease_total", quote.getLease_total());
			paramMap.addValue("rate", quote.getRate());
			paramMap.addValue("expiry_duration_days", quote.getExpiry_duration_days());
			paramMap.addValue("is_expired", quote.getIsExpired());
			paramMap.addValue("is_submitted", quote.getIsSubmitted());

			status = namedJdbcTemplate.update(INSERT_QUERY, paramMap, keyHolder, new String[] { "quote_arch_id" });

			// get the auto generated primary key from the new customer insertion
			Number generatedComID = keyHolder.getKey();
			status = generatedComID.intValue();

			System.out.println("new quote_arch_id = " + status);

		} catch (Exception e) {

		} // try

		return status;
	} // archiveQuote

	@Override
	public int removeQuote(int quote_ID) throws Exception {
		// TODO Auto-generated method stub
		int status = 0;

		final String DELETE_QUERY = "delete from quote where quote_id = ?";

		// define query arguments
		Object[] params = { quote_ID };

		status = jdbcTemplate.update(DELETE_QUERY, params);
		System.out.println("delete status = " + status);

		return status;
	} // removeQuote

	@Override
	public int archiveApplication(Application application) throws Exception {
		// TODO Auto-generated method stub
		NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
		final KeyHolder keyHolder = new GeneratedKeyHolder();
		int status = 0;

		// format the dates
		long millis = System.currentTimeMillis();
		java.sql.Date date_of_archive = new java.sql.Date(millis);

		final String INSERT_QUERY = "insert into application_arch (date_of_archive, application_id, quote_id, date_of_application, review_flag, comments, is_approved,"
				+ "manager_id, sales_id, security_deposit, is_declined, is_expired) values "
				+ "(:date_of_archive, :application_id, :quote_id, :date_of_application, :review_flag, :comments, :is_approved, :manager_id, :sales_id, :security_deposit, :is_declined, :is_expired)";

		try {

			System.out.println("Date of archiving: " + date_of_archive);

			// Creating map with all required params
			MapSqlParameterSource paramMap = new MapSqlParameterSource();

			paramMap.addValue("date_of_archive", date_of_archive);
			paramMap.addValue("application_id", application.getApplicationID());
			paramMap.addValue("quote_id", application.getQuoteID());
			paramMap.addValue("date_of_application", application.getDateOfApplication());
			paramMap.addValue("review_flag", application.getReviewFlag());
			paramMap.addValue("comments", application.getComments());
			paramMap.addValue("is_approved", application.getIsApproved());
			paramMap.addValue("manager_id", application.getManagerID());
			paramMap.addValue("sales_id", application.getSalesID());
			paramMap.addValue("security_deposit", application.getSecurity_deposit());
			paramMap.addValue("is_declined", application.getIsDeclined());
			paramMap.addValue("is_expired", application.getIsExpired());

			status = namedJdbcTemplate.update(INSERT_QUERY, paramMap, keyHolder,
					new String[] { "application_arch_id" });

			// get the auto generated primary key from the new customer insertion
			Number generatedComID = keyHolder.getKey();
			status = generatedComID.intValue();

			System.out.println("new application_arch_id = " + status);

		} catch (Exception e) {

		} // try

		return status;
	} // archiveApplication

	@Override
	public int removeApplication(int applicationID) throws Exception {
		// TODO Auto-generated method stub
		int status = 0;

		final String DELETE_QUERY = "delete from application where application_id = ?";

		// define query arguments
		Object[] params = { applicationID };

		status = jdbcTemplate.update(DELETE_QUERY, params);
		System.out.println("delete status = " + status);

		return status;
	} // removeApplication

} // BookingDAOImpl
