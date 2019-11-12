package com.eden.api.service;

import com.edenstar.model.Booking;
import com.edenstar.model.Customer;
import com.edenstar.model.Quote;
import com.edenstar.model.User;
import com.edenstar.model.booking.AddQuote;
import com.edenstar.model.booking.GetQuote;
import com.edenstar.model.booking.KioskQuery;

public interface QueryService {
	
	// checks the input dates against booking calendar for booking availability
	KioskQuery checkAvailability(Quote q, String staffEmail) throws Exception;

	// generate booking application report for manager to review
	String generateApplicationReport(GetQuote getQuote, User manager_details, Quote quote, AddQuote q);

	// returns true if the kiosk is available, false for everything else
	boolean isKioskAvailable(Quote quote, String staff_email_id) throws Exception;

	// generate booking email to customer
	String generateBookingEmail(Booking booking, Customer customer, KioskQuery kQuery) throws Exception;

	// declined booking application email message
	String generateDeclineBookingEmail(Quote quote, Customer customer) throws Exception;

	// add comment to booking
	void addBookingComment(int booking_id, String comment, String previousComments, String staff_email_id) throws Exception;

	// add comment to application
	void addApplicationComment(int applicationID, String comment, String previousComments, String staff_email_id) throws Exception;

	// create cancellation email message
	String generateBookingCancellationEmail(Booking booking, Customer customer, KioskQuery kQuery) throws Exception;

	// send email out to customer or another email
	String generateBookingEmailRepeat(Booking booking, Customer customer, KioskQuery kQuery) throws Exception;

	// add comments to lease
	void addLeaseComment(int lease_id, String comment, String previousComments, String staff_email_id) throws Exception;

	// add comments to account
	void addAccountComment(int account_id, String comment, String previousComments, String staff_email_id)
			throws Exception;

	// add payment comments
	void addPaymentComment(int payment_id, String comment, String previousComments, String staff_email_id)
			throws Exception;
	
	

}
