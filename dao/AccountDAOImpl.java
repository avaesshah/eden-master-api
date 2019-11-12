package com.eden.api.dao;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
import com.eden.api.util.PaymentMapper;
import com.edenstar.model.Account;
import com.edenstar.model.Payment;
import com.edenstar.model.account.AccountStream;
import com.edenstar.model.account.ProcessAccount;
import com.edenstar.model.reports.CustomerRevenue;
import com.edenstar.model.reports.KioskPerformance;
import com.edenstar.model.reports.LocationPerformance;
import com.edenstar.model.reports.ReportStream;
import com.edenstar.model.reports.SalesPerformance;
import com.edenstar.model.reports.ZonePerformance;

@Component
public class AccountDAOImpl implements AccountDAO {

	private JdbcTemplate jdbcTemplate;

	@Autowired
	public AccountDAOImpl(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public int addAccount(Account account) throws Exception {

		NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
		final KeyHolder keyHolder = new GeneratedKeyHolder();
		int status = 0;

		final String INSERT_QUERY = "insert into account (lease_id, deposit_amount, deposit_cleared_date, deposit_cleared, deposit_refunded,"
				+ "deposit_refunded_date, comments, flag_for_manager, lease_total, lease_remaining, no_payments_received, no_payments_remaining,"
				+ "document_upload_url, is_archived) values (:lease_id, :deposit_amount, :deposit_cleared_date, :deposit_cleared, :deposit_refunded,"
				+ ":deposit_refunded_date, :comments, :flag_for_manager, :lease_total, :lease_remaining, :no_payments_received, :no_payments_remaining,"
				+ ":document_upload_url, :is_archived)";

		try {

			// Creating map with all required params
			MapSqlParameterSource paramMap = new MapSqlParameterSource();

			paramMap.addValue("lease_id", account.getLease_id());
			paramMap.addValue("deposit_amount", account.getDepositAmount());
			paramMap.addValue("deposit_cleared_date", account.getDepositClearedDate());
			paramMap.addValue("deposit_cleared", account.getDepositCleared());
			paramMap.addValue("deposit_refunded", account.getDepositRefunded());
			paramMap.addValue("deposit_refunded_date", account.getDepositRefundedDate());
			paramMap.addValue("comments", account.getComments());
			paramMap.addValue("flag_for_manager", account.getFlagForManager());
			paramMap.addValue("lease_total", account.getLeaseTotal());
			paramMap.addValue("lease_remaining", account.getLeaseRemaining());
			paramMap.addValue("no_payments_received", account.getNoPaymentsReceived());
			paramMap.addValue("no_payments_remaining", account.getNoPaymentsRemaining());
			paramMap.addValue("document_upload_url", account.getDocumentUploadUrl());
			paramMap.addValue("is_archived", account.getIs_archived());

			status = namedJdbcTemplate.update(INSERT_QUERY, paramMap, keyHolder, new String[] { "account_id" });

			// get the auto generated primary key from the new customer insertion
			Number generatedComID = keyHolder.getKey();
			status = generatedComID.intValue();

			System.out.println("new account ID = " + status);

		} catch (Exception e) {

		} // try

		return status;
	} // addAccount

	@Override
	public int addPayment(Payment payment) throws Exception {

		NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
		final KeyHolder keyHolder = new GeneratedKeyHolder();
		int status = 0;

		final String INSERT_QUERY = "insert into payment (account_id, due_by_date, amount_due, amount_cleared, payment_in_date, payment_method,"
				+ "comments, overdue, is_paid, payment_issue, is_archived ) "
				+ "values (:account_id, :due_by_date, :amount_due, :amount_cleared, :payment_in_date, :payment_method, :comments, :overdue, :is_paid, :payment_issue, :is_archived)";

		String dueByDateStr = new SimpleDateFormat("dd/MM/yyyy").format(payment.getDueByDate());
		java.sql.Date dueByDate = formatDateForDB(dueByDateStr);

		try {

			// Creating map with all required params
			MapSqlParameterSource paramMap = new MapSqlParameterSource();

			paramMap.addValue("account_id", payment.getAccount_id());
			paramMap.addValue("due_by_date", dueByDate);
			paramMap.addValue("amount_due", payment.getAmountDue());
			paramMap.addValue("amount_cleared", payment.getAmountCleared());
			paramMap.addValue("payment_in_date", payment.getPaymentInDate());
			paramMap.addValue("payment_method", payment.getPaymentMethod());
			paramMap.addValue("comments", payment.getPaymentMethod());
			paramMap.addValue("overdue", payment.getOverdue());
			paramMap.addValue("is_paid", payment.getIsPaid());
			paramMap.addValue("payment_issue", payment.getPaymentIssue());
			paramMap.addValue("is_archived", payment.getIs_archived());

			status = namedJdbcTemplate.update(INSERT_QUERY, paramMap, keyHolder, new String[] { "payment_id" });

			// get the auto generated primary key from the new customer insertion
			Number generatedComID = keyHolder.getKey();
			status = generatedComID.intValue();

			System.out.println("new account ID = " + status);

		} catch (Exception e) {

		} // try

		return status;
	} // add payment

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

	private java.sql.Date formatDateForDB(String dateStr) {

		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		Date dateForDB = null;
		DateTime date_SQL_format = null;

		if (dateStr == null)
			return null;

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
	public void updateNumOfPaymentRem(int account_id, int numOfPayments) throws Exception {
		// TODO Auto-generated method stub
		int status = 0;

		final String UPDATE_QUERY = "update account set no_payments_remaining = ? where account_id = ?";

		// define query arguments
		Object[] params = { numOfPayments, account_id };

		status = jdbcTemplate.update(UPDATE_QUERY, params);
		System.out.println("num of payments update status = " + status);

	} // updateNumOfPayment

	@Override
	public void updateSecurityDeposit(int account_id, double securityDeposit) throws Exception {
		// TODO Auto-generated method stub

		int status = 0;

		final String UPDATE_QUERY = "update account set deposit_amount = ? where account_id = ?";

		// define query arguments
		Object[] params = { securityDeposit, account_id };

		status = jdbcTemplate.update(UPDATE_QUERY, params);
		System.out.println("security deposit amount update status = " + status);

	} // updateSecurityDeposit

	@Override
	public int addCommentsToAccount(int account_id, String comments) throws Exception {
		// TODO Auto-generated method stub
		int status = 0;

		final String UPDATE_QUERY = "update account set comments = ? , flag_for_manager = 1 where account_id = ?";

		// define query arguments
		Object[] params = { comments, account_id };

		status = jdbcTemplate.update(UPDATE_QUERY, params);
		System.out.println("update status = " + status);

		return status;
	} // addCommentsToAccount

	@Override
	public int addCommentsToPayment(int payment_id, String comments) throws Exception {
		// TODO Auto-generated method stub
		int status = 0;

		final String UPDATE_QUERY = "update payment set comments = ? , payment_issue = 1 where payment_id = ?";

		// define query arguments
		Object[] params = { comments, payment_id };

		status = jdbcTemplate.update(UPDATE_QUERY, params);
		System.out.println("update status = " + status);

		return status;
	} // addCommentsToPayment

	@Override
	public Account getAccount(int account_id) throws Exception {
		// TODO Auto-generated method stub
		Account a = new Account();
		String query = "SELECT * FROM account where account_id ='" + account_id + "'";

		try {

			a = jdbcTemplate.queryForObject(query, new AccountMapper());

		} catch (Exception e) {

		} // try

		return a;
	} // getAccount

	@Override
	public Payment getPayment(int payment_id) throws Exception {
		// TODO Auto-generated method stub
		Payment p = new Payment();
		String query = "SELECT * FROM payment where payment_id ='" + payment_id + "'";

		try {

			p = jdbcTemplate.queryForObject(query, new PaymentMapper());

		} catch (Exception e) {

		} // try

		return p;
	} // getPayment

	@Override
	public Account getAccountByLeaseID(int lease_id) throws Exception {
		// TODO Auto-generated method stub
		Account a = new Account();
		String query = "SELECT * FROM account where lease_id ='" + lease_id + "'";

		try {

			a = jdbcTemplate.queryForObject(query, new AccountMapper());

		} catch (Exception e) {

		} // try

		return a;
	} // getAccountByLeaseID

	@Override
	public List<Payment> getPaymentSchedule(int account_id) throws Exception {
		// TODO Auto-generated method stub
		List<Payment> paymentList = new ArrayList<>();

		// first we obtain a list of applications
		String query = "SELECT * FROM payment where is_archived = 0 and account_id ='" + account_id + "'";
		List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();

		try {

			rows = jdbcTemplate.queryForList(query);
			System.out.println("rows = " + rows.size());

			for (Map<String, Object> row : rows) {

				Payment p = new Payment();

				p.setPayment_id((Integer) (row.get("payment_id")));
				p.setAccount_id((Integer) (row.get("account_id")));
				p.setDueByDate((Date) (row.get("due_by_date")));
				p.setAmountDue((Double) (row.get("amount_due")));
				p.setAmountCleared((Double) (row.get("amount_cleared")));
				p.setPaymentInDate((Date) (row.get("payment_in_date")));
				p.setPaymentMethod((String) (row.get("payment_method")));
				p.setComments((String) (row.get("comments")));		
				p.setCommentHistory(processComments(p.getComments()));
				p.setOverdue((Integer) (row.get("overdue")));
				p.setIsPaid((Integer) (row.get("is_paid")));
				p.setPaymentIssue((Integer) (row.get("payment_issue")));
				p.setIs_archived((Integer) (row.get("is_archived")));

				paymentList.add(p);
			}

		} catch (Exception e) {

		} // try

		return paymentList;
	} // getPaymentSchedule

	@Override
	public int uploadAccDocument(int account_id, String urlToDocumenttScan) throws Exception {
		int status = 0;

		final String UPDATE_QUERY = "update account set document_upload_url = ? where account_id = ?";

		// define query arguments
		Object[] params = { urlToDocumenttScan, account_id };

		status = jdbcTemplate.update(UPDATE_QUERY, params);
		System.out.println("update status = " + status);

		return status;
	} // uploadAccDocument

	@Override
	public int updateAccount(ProcessAccount a) throws Exception {
		int status = 0;
		java.sql.Date depositClearedDate = null;
		java.sql.Date depositRefundedDate = null;

		if (a.getDeposit_cleared_date() != null) {
			depositClearedDate = formatDateForDB(a.getDeposit_cleared_date());
			a.setDepositCleared(1);
		}

		if (a.getDeposit_refunded_date() != null) {
			depositRefundedDate = formatDateForDB(a.getDeposit_refunded_date());
			a.setDepositRefunded(1);
		}

		final String UPDATE_QUERY = "update account set " + "deposit_amount = ?, " + "deposit_cleared_date = ?, "
				+ "deposit_cleared = ?, " + "deposit_refunded_date = ?, " + "deposit_refunded = ?, "
				+ "flag_for_manager = ? " + "where account_id = ?";

		// define query arguments
		Object[] params = { a.getDepositAmount(), depositClearedDate, a.getDepositCleared(), depositRefundedDate,
				a.getDepositRefunded(), a.getFlagForManager(), a.getAccount_id() };

		status = jdbcTemplate.update(UPDATE_QUERY, params);
		System.out.println("update status = " + status);

		return status;
	} // updateAccount

	@Override
	public int updatePayment(ProcessAccount a) throws Exception {
		// TODO Auto-generated method stub
		int status = 0;
//		java.sql.Date dueByDate = null;
		java.sql.Date payInDate = null;

//		if (a.getDue_by_date() != null) {
//			dueByDate = formatDateForDB(a.getDue_by_date());
//		}

		if (a.getPayment_in_date() != null) {
			payInDate = formatDateForDB(a.getPayment_in_date());
			a.setIs_paid(1);
			a.setOverdue(0);
		}

		if (a.getAmount_due() < 0.0)
			a.setAmount_due(0.0);
		if (a.getAmount_cleared() < 0.0)
			a.setAmount_cleared(0.0);

		final String UPDATE_QUERY = "update payment set " + "amount_due = ?, " + "amount_cleared = ?, "
				+ "payment_in_date = ?, " + "payment_method = ?, " + "overdue = ?, " + "is_paid = ?, "
				+ "payment_issue = ? " + "where payment_id = ?";

		// define query arguments
		Object[] params = { a.getAmount_due(), a.getAmount_cleared(), payInDate, a.getPayment_method(), a.getOverdue(),
				a.getIs_paid(), a.getPayment_issue(), a.getPayment_id() };

		status = jdbcTemplate.update(UPDATE_QUERY, params);
		System.out.println("update status = " + status);

		return status;
	} // updatePayment

	@Override
	public int updateAccountBooks(Account a) throws Exception {
		int status = 0;

		final String UPDATE_QUERY = "update account set " + "no_payments_received = ?, " + "no_payments_remaining = ?, "
				+ "lease_remaining = ? " + "where account_id = ?";

		// define query arguments
		Object[] params = { a.getNoPaymentsReceived(), a.getNoPaymentsRemaining(), a.getLeaseRemaining(),
				a.getAccount_id() };

		status = jdbcTemplate.update(UPDATE_QUERY, params);
		System.out.println("update status = " + status);

		return status;
	} // updateAccountBooks

	@Override
	public List<AccountStream> getAllAccounts(String mode) throws Exception {
		String query = "";

		System.out.println("account view mode = " + mode);

		if (mode.contentEquals("*"))
			query = "SELECT * FROM account\r\n" + "inner join lease on account.lease_id = lease.lease_id\r\n"
					+ "inner join booking on lease.booking_id = booking.booking_id\r\n"
					+ "inner join quote on booking.quote_ref = quote.quote_ref\r\n"
					+ "inner join kiosk on quote.kiosk_id = kiosk.kiosk_id\r\n"
					+ "inner join zone on kiosk.zone_id = zone.zone_id\r\n"
					+ "inner join location on zone.location_id = location.location_id\r\n"
					+ "inner join customer on booking.customer_id = customer.customer_id\r\n"
					+ "inner join company on customer.customer_id = company.customer_id\r\n"
					+ "where  booking.is_expired = 0\r\n" + "group by account_id;";

		if (mode.contentEquals("active"))
			query = "SELECT * FROM account\r\n" + "inner join lease on account.lease_id = lease.lease_id\r\n"
					+ "inner join booking on lease.booking_id = booking.booking_id\r\n"
					+ "inner join quote on booking.quote_ref = quote.quote_ref\r\n"
					+ "inner join kiosk on quote.kiosk_id = kiosk.kiosk_id\r\n"
					+ "inner join zone on kiosk.zone_id = zone.zone_id\r\n"
					+ "inner join location on zone.location_id = location.location_id\r\n"
					+ "inner join customer on booking.customer_id = customer.customer_id\r\n"
					+ "inner join company on customer.customer_id = company.customer_id\r\n"
					+ "where booking.is_archived = 0 and booking.is_cancelled = 0 and booking.is_expired = 0\r\n"
					+ "group by account_id;";

		if (mode.contentEquals("account_cleared"))
			query = "SELECT * FROM account\r\n" + "inner join lease on account.lease_id = lease.lease_id\r\n"
					+ "inner join booking on lease.booking_id = booking.booking_id\r\n"
					+ "inner join quote on booking.quote_ref = quote.quote_ref\r\n"
					+ "inner join kiosk on quote.kiosk_id = kiosk.kiosk_id\r\n"
					+ "inner join zone on kiosk.zone_id = zone.zone_id\r\n"
					+ "inner join location on zone.location_id = location.location_id\r\n"
					+ "inner join customer on booking.customer_id = customer.customer_id\r\n"
					+ "inner join company on customer.customer_id = company.customer_id\r\n"
					+ "where  account.lease_remaining = 0 and account.deposit_cleared = 1\r\n" + "group by account_id;";

		if (mode.contentEquals("deposit_cleared"))
			query = "SELECT * FROM account\r\n" + "inner join lease on account.lease_id = lease.lease_id\r\n"
					+ "inner join booking on lease.booking_id = booking.booking_id\r\n"
					+ "inner join quote on booking.quote_ref = quote.quote_ref\r\n"
					+ "inner join kiosk on quote.kiosk_id = kiosk.kiosk_id\r\n"
					+ "inner join zone on kiosk.zone_id = zone.zone_id\r\n"
					+ "inner join location on zone.location_id = location.location_id\r\n"
					+ "inner join customer on booking.customer_id = customer.customer_id\r\n"
					+ "inner join company on customer.customer_id = company.customer_id\r\n"
					+ "where  account.deposit_cleared = 1\r\n" + "group by account_id;";

		if (mode.contentEquals("deposit_pending"))
			query = "SELECT * FROM account\r\n" + "inner join lease on account.lease_id = lease.lease_id\r\n"
					+ "inner join booking on lease.booking_id = booking.booking_id\r\n"
					+ "inner join quote on booking.quote_ref = quote.quote_ref\r\n"
					+ "inner join kiosk on quote.kiosk_id = kiosk.kiosk_id\r\n"
					+ "inner join zone on kiosk.zone_id = zone.zone_id\r\n"
					+ "inner join location on zone.location_id = location.location_id\r\n"
					+ "inner join customer on booking.customer_id = customer.customer_id\r\n"
					+ "inner join company on customer.customer_id = company.customer_id\r\n"
					+ "where account.deposit_cleared = 0\r\n" + "group by account_id;";

		if (mode.contentEquals("deposit_refunded"))
			query = "SELECT * FROM account\r\n" + "inner join lease on account.lease_id = lease.lease_id\r\n"
					+ "inner join booking on lease.booking_id = booking.booking_id\r\n"
					+ "inner join quote on booking.quote_ref = quote.quote_ref\r\n"
					+ "inner join kiosk on quote.kiosk_id = kiosk.kiosk_id\r\n"
					+ "inner join zone on kiosk.zone_id = zone.zone_id\r\n"
					+ "inner join location on zone.location_id = location.location_id\r\n"
					+ "inner join customer on booking.customer_id = customer.customer_id\r\n"
					+ "inner join company on customer.customer_id = company.customer_id\r\n"
					+ "where  account.deposit_refunded = 1\r\n" + "group by account_id;";

		if (mode.contentEquals("payment_overdue"))
			query = "SELECT * FROM payment\r\n" + "inner join account on payment.account_id = account.account_id\r\n"
					+ "inner join lease on account.lease_id = lease.lease_id\r\n"
					+ "inner join booking on lease.booking_id = booking.booking_id\r\n"
					+ "inner join quote on booking.quote_ref = quote.quote_ref\r\n"
					+ "inner join kiosk on quote.kiosk_id = kiosk.kiosk_id\r\n"
					+ "inner join zone on kiosk.zone_id = zone.zone_id\r\n"
					+ "inner join location on zone.location_id = location.location_id\r\n"
					+ "inner join customer on booking.customer_id = customer.customer_id\r\n"
					+ "inner join company on customer.customer_id = company.customer_id\r\n"
					+ "where  payment.overdue = 1\r\n" + "group by payment_id;";

		if (mode.contentEquals("expired"))
			query = "select * from account\r\n" + "inner join lease on account.lease_id = lease.lease_id\r\n"
					+ "inner join booking on lease.booking_id = booking.booking_id\r\n"
					+ "inner join quote on booking.quote_ref = quote.quote_ref\r\n"
					+ "inner join kiosk on quote.kiosk_id = kiosk.kiosk_id\r\n"
					+ "inner join zone on kiosk.zone_id = zone.zone_id\r\n"
					+ "inner join location on zone.location_id = location.location_id\r\n"
					+ "inner join customer on booking.customer_id = customer.customer_id\r\n"
					+ "inner join company on customer.customer_id = company.customer_id\r\n"
					+ "where booking.is_expired = 1\r\n" + "group by account_id";

		List<AccountStream> accountsList = new ArrayList<>();
		List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();

		try {

			rows = jdbcTemplate.queryForList(query);

			System.out.println("number of rows = " + rows.size());

			for (Map<String, Object> row : rows) {

				AccountStream a = new AccountStream();

				a.setLease_id((Integer) (row.get("lease_id")));

				a.setBooking_id((Integer) (row.get("booking_id")));
				a.setBooking_ref((String) (row.get("booking_ref")));

				a.setAccount_id((Integer) (row.get("account_id")));
				a.setDepositAmount((Double) (row.get("deposit_amount")));

				if (row.get("deposit_cleared_date") != null)
					a.setDeposit_cleared_date(formatDateFoJava(row.get("deposit_cleared_date").toString()));

				if (row.get("deposit_refunded_date") != null)
					a.setDeposit_refunded_date(formatDateFoJava(row.get("deposit_refunded_date").toString()));

				a.setDepositCleared((Integer) (row.get("deposit_cleared")));
				a.setDepositRefunded((Integer) (row.get("deposit_refunded")));

				a.setComments((String) (row.get("comments")));
				a.setCommmentsHistory(processComments(a.getComments()));
				
				
				a.setFlagForManager((Integer) (row.get("flag_for_manager")));
				a.setLeaseTotal((Double) (row.get("lease_total")));
				a.setLeaseRemaining((Double) (row.get("lease_remaining")));
				a.setNoPaymentsReceived((Integer) (row.get("no_payments_received")));
				a.setNoPaymentsRemaining((Integer) (row.get("no_payments_remaining")));

				a.setFirst_name((String) (row.get("first_name")));
				a.setLast_name((String) (row.get("last_name")));

				a.setLease_start_date(formatDateFoJava(row.get("lease_start_date").toString()));
				a.setLease_end_date(formatDateFoJava((row.get("lease_end_date")).toString()));
				a.setLocation_name((String) (row.get("location_name")).toString().toUpperCase());
				a.setLocation_area((String) (row.get("location_area")));
				a.setZone_number((Integer) (row.get("zone_number")));
				a.setZone_name((String) (row.get("zone_name")));
				a.setKiosk_number((Integer) (row.get("kiosk_number")));

				a.setCustomer_id((Integer) (row.get("customer_id")));
				a.setCompanyName((String) (row.get("company_name")));

				accountsList.add(a);
			}

		} catch (Exception e) {

		} // try

		return accountsList;
	} // getAllAccounts

	@Override
	public List<ReportStream> getReport(String mode, int field_id, Date reportStartDate, Date reportEndDate)
			throws Exception {
		String query = "";
		double totalSales = 0.0;
		int totalBookings = 0;

		System.out.println("report view mode = " + mode);

		if (mode.contentEquals("bookings_location"))
			query = "SELECT count(*) as totalBookings, sum(booking.lease_total) as totalSales FROM booking \r\n"
					+ "inner join application on booking.application_id = application.application_id\r\n"
					+ "inner join quote on application.quote_id = quote.quote_id\r\n"
					+ "inner join kiosk on quote.kiosk_id = kiosk.kiosk_id\r\n"
					+ "inner join zone on kiosk.zone_id = zone.zone_id\r\n"
					+ "inner join location on zone.location_id = location.location_id\r\n"
					+ "where not (date_of_booking not between '" + reportStartDate.toString() + "'  and '"
					+ reportEndDate.toString() + "')\r\n" + "and booking.is_cancelled = 0 and location.location_id = "
					+ field_id;

		if (mode.contentEquals("bookings_all_locations"))
			query = "select location.*, sum(booking.lease_total) as totalSales, count(booking.lease_total) as totalBookings from location\r\n"
					+ "left join zone on zone.location_id = location.location_id\r\n"
					+ "left join kiosk on zone.zone_id = kiosk.zone_id\r\n"
					+ "left join quote on kiosk.kiosk_id = quote.kiosk_id\r\n"
					+ "left join application on quote.quote_id = application.quote_id\r\n"
					+ "left join booking on application.application_id = booking.application_id\r\n"
					+ "and booking.is_cancelled = 0\r\n" + "and (date_of_booking between '" + reportStartDate.toString()
					+ "' and '" + reportEndDate.toString() + "')\r\n" + " where location.is_archived = 0\r\n"
					+ "group by location.location_id;";

		if (mode.contentEquals("bookings_employee"))
			query = "SELECT count(*) as totalBookings, sum(booking.lease_total) as totalSales FROM booking\r\n"
					+ "inner join application on booking.application_id = application.application_id\r\n"
					+ "inner join quote on application.quote_id = quote.quote_id\r\n"
					+ "inner join users on quote.employee_id = users.employee_id\r\n"
					+ "where not (date_of_booking not between '" + reportStartDate.toString() + "' and '"
					+ reportEndDate.toString() + "')\r\n" + "and booking.is_cancelled = 0 and users.employee_id = "
					+ field_id;

		if (mode.contentEquals("bookings_all_employees"))
			query = "select users.*, sum(booking.lease_total) as totalSales, count(booking.lease_total) as totalBookings from users \r\n"
					+ "left join booking on users.employee_id = booking.sales_id\r\n"
					+ "and booking.is_cancelled = 0 and (date_of_booking between '" + reportStartDate.toString()
					+ "' and '" + reportEndDate.toString() + "')\r\n" + "group by users.employee_id;";

		if (mode.contentEquals("bookings_customer"))
			query = "SELECT count(*) as totalBookings, sum(booking.lease_total) as totalSales FROM booking\r\n"
					+ "inner join application on booking.application_id = application.application_id\r\n"
					+ "inner join quote on application.quote_id = quote.quote_id\r\n"
					+ "inner join customer on quote.customer_id = customer.customer_id\r\n"
					+ "where not (date_of_booking not between '" + reportStartDate.toString() + "' and '"
					+ reportEndDate.toString() + "')\r\n" + "and booking.is_cancelled = 0 and customer.customer_id = "
					+ field_id;

		if (mode.contentEquals("bookings_all_customers"))
			query = "select customer.*, sum(booking.lease_total) as totalSales, count(booking.lease_total) as totalBookings from customer\r\n"
					+ "left join booking on customer.customer_id = booking.customer_id\r\n"
					+ "and booking.is_cancelled = 0 and (date_of_booking between '" + reportStartDate.toString()
					+ "' and '" + reportEndDate.toString() + "')\r\n" + "group by customer.customer_id;";

		if (mode.contentEquals("bookings_all_zones"))
			query = "select zone.*, sum(booking.lease_total) as totalSales, count(booking.lease_total) as totalBookings from zone \r\n"
					+ "left join kiosk on zone.zone_id = kiosk.zone_id\r\n"
					+ "left join quote on kiosk.kiosk_id = quote.kiosk_id\r\n"
					+ "left join application on quote.quote_id = application.quote_id\r\n"
					+ "left join booking on application.application_id = booking.application_id\r\n"
					+ "and booking.is_cancelled = 0 and (date_of_booking between '" + reportStartDate.toString()
					+ "' and '" + reportEndDate.toString() + "') \r\n" + "where zone.is_archived = 0\r\n"
					+ "group by zone.zone_id;";

		if (mode.contentEquals("bookings_kiosks_by_zone"))
			query = "select kiosk.*, sum(booking.lease_total) as totalSales, count(booking.lease_total) as totalBookings from kiosk \r\n"
					+ "left join quote on kiosk.kiosk_id = quote.kiosk_id\r\n"
					+ "left join application on quote.quote_id = application.quote_id\r\n"
					+ "left join booking on application.application_id = booking.application_id\r\n"
					+ "and booking.is_cancelled = 0 and (date_of_booking between '" + reportStartDate.toString()
					+ "' and '" + reportEndDate.toString() + "') \r\n"
					+ "where kiosk.is_archived = 0 and kiosk.zone_id = " + field_id + "\r\n"
					+ "group by kiosk.kiosk_id;";

		List<ReportStream> reportList = new ArrayList<>();
		List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();

		try {

			rows = jdbcTemplate.queryForList(query);

			System.out.println("number of rows = " + rows.size());

			for (Map<String, Object> row : rows) {

				ReportStream r = new ReportStream();

				if (mode.contentEquals("bookings_location")) {

					if (row.get("totalSales") != null) {
						totalSales = round((Double) row.get("totalSales"), 2);
					} else
						totalSales = 0.0;

					if (row.get("totalBookings") != null) {
						totalBookings = Integer.valueOf((row.get("totalBookings").toString()));
					} else {
						totalBookings = 0;
					}
					r.setLocation_peformance(new LocationPerformance(totalSales, totalBookings));
				}

				if (mode.contentEquals("bookings_employee")) {

					if (row.get("totalSales") != null) {
						totalSales = round((Double) row.get("totalSales"), 2);
					} else
						totalSales = 0.0;

					if (row.get("totalBookings") != null) {
						totalBookings = Integer.valueOf((row.get("totalBookings").toString()));
					} else {
						totalBookings = 0;
					}
					r.setSales_performance(new SalesPerformance(totalSales, totalBookings));
				}

				if (mode.contentEquals("bookings_customer")) {

					if (row.get("totalSales") != null) {
						totalSales = round((Double) row.get("totalSales"), 2);
					} else
						totalSales = 0.0;

					if (row.get("totalBookings") != null) {
						totalBookings = Integer.valueOf((row.get("totalBookings").toString()));
					} else {
						totalBookings = 0;
					}
					r.setCustomer_revenue(new CustomerRevenue(totalSales, totalBookings));
				}

				if (mode.contentEquals("bookings_all_locations")) {

					if (row.get("totalSales") != null) {
						totalSales = round((Double) row.get("totalSales"), 2);
					} else
						totalSales = 0.0;

					if (row.get("totalBookings") != null) {
						totalBookings = Integer.valueOf((row.get("totalBookings").toString()));
					} else {
						totalBookings = 0;
					}

					r.setLocation_peformance(new LocationPerformance(totalSales, totalBookings));
					r.getLocation_peformance().setLocationID((Integer) (row.get("location_id")));
					r.getLocation_peformance().setLocationArea(((String) (row.get("location_area"))).toUpperCase());
					r.getLocation_peformance().setLocationName(((String) (row.get("location_name"))).toUpperCase());
					r.getLocation_peformance().setMapURL((String) (row.get("mapURL")));

				}

				if (mode.contentEquals("bookings_all_employees")) {

					if (row.get("totalSales") != null) {
						totalSales = round((Double) row.get("totalSales"), 2);
					} else
						totalSales = 0.0;

					if (row.get("totalBookings") != null) {
						totalBookings = Integer.valueOf((row.get("totalBookings").toString()));
					} else {
						totalBookings = 0;
					}

					r.setSales_performance(new SalesPerformance(totalSales, totalBookings));
					r.getSales_performance().setEmployeeID((Integer) (row.get("employee_id")));
					r.getSales_performance().setFirstName((String) (row.get("first_name")));
					r.getSales_performance().setLastName((String) (row.get("last_name")));
					r.getSales_performance().setUserLevel((String) (row.get("user_level")));

				}

				if (mode.contentEquals("bookings_all_customers")) {

					if (row.get("totalSales") != null) {
						totalSales = round((Double) row.get("totalSales"), 2);
					} else
						totalSales = 0.0;

					if (row.get("totalBookings") != null) {
						totalBookings = Integer.valueOf((row.get("totalBookings").toString()));
					} else {
						totalBookings = 0;
					}

					r.setCustomer_revenue(new CustomerRevenue(totalSales, totalBookings));
					r.getCustomer_revenue().setCustomerID((Integer) (row.get("customer_id")));
					r.getCustomer_revenue().setFirstName((String) (row.get("first_name")));
					r.getCustomer_revenue().setLastName((String) (row.get("last_name")));

				}

				if (mode.contentEquals("bookings_all_zones")) {

					if (row.get("totalSales") != null) {
						totalSales = round((Double) row.get("totalSales"), 2);
					} else
						totalSales = 0.0;

					if (row.get("totalBookings") != null) {
						totalBookings = Integer.valueOf((row.get("totalBookings").toString()));
					} else {
						totalBookings = 0;
					}

					r.setZone_performance(new ZonePerformance(totalSales, totalBookings));
					r.getZone_performance().setZoneID((Integer) (row.get("zone_id")));
					r.getZone_performance().setZoneNumber((Integer) (row.get("zone_number")));
					r.getZone_performance().setZoneName((String) (row.get("zone_name")));
				}
				
				if (mode.contentEquals("bookings_kiosks_by_zone")) {

					if (row.get("totalSales") != null) {
						totalSales = round((Double) row.get("totalSales"), 2);
					} else
						totalSales = 0.0;

					if (row.get("totalBookings") != null) {
						totalBookings = Integer.valueOf((row.get("totalBookings").toString()));
					} else {
						totalBookings = 0;
					}

					r.setKiosk_performance(new KioskPerformance(totalSales, totalBookings));
					r.getKiosk_performance().setKioskID((Integer) (row.get("kiosk_id")));
					r.getKiosk_performance().setKioskNumber((Integer) (row.get("kiosk_number")));
				}

				reportList.add(r);
			}

		} catch (Exception e) {

		} // try

		return reportList;

	} // getReport

	private static double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	} // round

	@Override
	public int resetPayment(Payment payment) throws Exception {
		// TODO Auto-generated method stub
		int status = 0;

		final String UPDATE_ACCOUNT_QUERY = "update account set account.no_payments_remaining = (account.no_payments_remaining + 1), \r\n" + 
				"account.no_payments_received = (account.no_payments_received - 1), account.lease_remaining = account.lease_remaining + ? where account_id = ?";
		
		final String UPDATE_PAYMENT_QUERY = "update payment set payment_method = null, payment_in_date = null, amount_cleared = 0.0, is_paid = 0 where payment_id = ?";

		// define query arguments
		Object[] params = { payment.getAmountCleared(), payment.getAccount_id() };

		status = jdbcTemplate.update(UPDATE_ACCOUNT_QUERY, params);
		System.out.println("Account reset =  " + status);
		
		Object[] params2 = { payment.getPayment_id() };
		status = jdbcTemplate.update(UPDATE_PAYMENT_QUERY, params2);
		System.out.println("Payment reset =  " + status);
		
		return status;
		
	} // resetPayment

	@Override
	public List<Payment> getActivePayments() throws Exception {
		// TODO Auto-generated method stub
		List<Payment> paymentList = new ArrayList<>();

		// first we obtain a list of applications
		String query = "SELECT * FROM payment where is_archived = 0 and is_paid = 0 and overdue = 0";
		List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();

		try {

			rows = jdbcTemplate.queryForList(query);
			System.out.println("rows = " + rows.size());

			for (Map<String, Object> row : rows) {

				Payment p = new Payment();

				p.setPayment_id((Integer) (row.get("payment_id")));
				p.setAccount_id((Integer) (row.get("account_id")));
				p.setDueByDate((Date) (row.get("due_by_date")));
				p.setAmountDue((Double) (row.get("amount_due")));
				p.setAmountCleared((Double) (row.get("amount_cleared")));
				p.setPaymentInDate((Date) (row.get("payment_in_date")));
				p.setPaymentMethod((String) (row.get("payment_method")));
				p.setComments((String) (row.get("comments")));
				p.setOverdue((Integer) (row.get("overdue")));
				p.setIsPaid((Integer) (row.get("is_paid")));
				p.setPaymentIssue((Integer) (row.get("payment_issue")));
				p.setIs_archived((Integer) (row.get("is_archived")));
				
				p.setCommentHistory(processComments(p.getComments()));

				paymentList.add(p);
			}

		} catch (Exception e) {

		} // try

		return paymentList;
	} // getActivePayments

	@Override
	public int setPaymentOverdueFlag(int payment_id, boolean isFlagON) throws Exception {
		// TODO Auto-generated method stub
		int status = 0;
		int flagPosition = 0;

		if (isFlagON)
			flagPosition = 1;

		final String UPDATE_QUERY = "update payment set overdue = ? where payment_id = ?";

		// define query arguments
		Object[] params = { flagPosition, payment_id };

		status = jdbcTemplate.update(UPDATE_QUERY, params);
		System.out.println("overdue status = " + status);

		return status;
	} // setPaymentOverdueFlag
	
	public String[] processComments(String comments) {
		String[] tempArray = null;
		if (comments == null || comments.isEmpty())
			return tempArray;

		String stringToSplit = comments;
		String delimiter = "\\[";
		tempArray = stringToSplit.split(delimiter);
		for (int i = 1; i < tempArray.length; i++) {
			tempArray[i] = "[" + tempArray[i];
			//System.out.println(tempArray[i]);
		}

		return tempArray;
	} // processComments

} // AccountDAOImpl
