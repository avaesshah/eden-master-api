package com.eden.api.dao;

import java.util.List;

import com.edenstar.model.Account;
import com.edenstar.model.Application;
import com.edenstar.model.Booking;
import com.edenstar.model.Calendar;
import com.edenstar.model.Lease;
import com.edenstar.model.Quote;
import com.edenstar.model.booking.AddQuote;
import com.edenstar.model.booking.ApplicationStream;
import com.edenstar.model.booking.BookingStream;
import com.edenstar.model.booking.GetApplications;
import com.edenstar.model.booking.LeaseStream;

public interface BookingDAO {

	// returns the booking calendar for a given kiosk
	List<Calendar> getBookingForKiosk(int kioskID) throws Exception;

	// add a quote to the database
	int addQuote(AddQuote q) throws Exception;

	// gets a quotation by quotation reference
	Quote getQuote(String quoteRef) throws Exception;

	// delete a quote
	int deleteQuote(int quote_ID) throws Exception;

	// set the expired flag for quote
	int setExpiredFlag(int quote_ID) throws Exception;

	// add a booking application to the database
	int addApplication(AddQuote q) throws Exception;

	// obtain a list of applications
	List<GetApplications> getApplications(int employeeID) throws Exception;

	// obtain quote by id
	Quote getQuote(int quote_id) throws Exception;

	// obtain application by ID
	Application getApplication(int application_id) throws Exception;

	// extends the quotation expiry days
	int extendExpiry(int quote_ID) throws Exception;

	// returns a list of customers that have booking application under a manager ID
	List<ApplicationStream> getApplicationCustomers(int manager_id) throws Exception;

	// adds a booking to the database
	int addBooking(Booking booking) throws Exception;

	// add booking dates to kiosk calendar
	int addDatesToKioskCalendar(Booking booking) throws Exception;

	// delete application
	int deleteApplication(int application_id) throws Exception;

	// delete booking
	int deleteBooking(int booking_id) throws Exception;

	// delete calendar dates
	int deleteCalendar(int calendar_id) throws Exception;

	// set the kiosk lock
	int setKioskLock(int kiosk_ID, boolean lock) throws Exception;

	// update the application with the deposit url
	int updateDepositURL(int application_id, String deposit_url) throws Exception;

	// returns a list of quotes
	List<Quote> getQuotes() throws Exception;

	// declined flag in applicaton
	int setApplicationDeclinedFlag(int application_id, boolean b) throws Exception;

	// application expired flag
	int setApplicationExpiredFlag(int application_id, boolean b) throws Exception;

	// get all applications
	List<ApplicationStream> getAllApplications(String mode) throws Exception;

	// set the application approved flag
	int setIsApprovedFlag(int application_id, boolean b) throws Exception;

	// update the quote with revised price
	int updateRevisedLease(Quote quote, Application application) throws Exception;

	// application review flag switch
	int setApplicationReviewFlag(int application_id, boolean b) throws Exception;

	// add comments to application
	int addCommentsToApplication(int application_id, String comments) throws Exception;

	// obtain booking by reference
	Booking getBooking(String booking_ref) throws Exception;

	// booking expired flag
	int setBookingExpiredFlag(int booking_id, boolean b) throws Exception;

	// booking cancelled flag
	int setBookingCancelledFlag(int booking_id, boolean isFlagON) throws Exception;

	// booking review flag
	int setBookingReviewFlag(int booking_id, boolean isFlagON) throws Exception;

	// booking expiry due flag
	int setBookingExpiryDueFlag(int booking_id, boolean isFlagON) throws Exception;

	// add booking comments
	int addCommentsToBooking(int booking_id, String comments) throws Exception;

	// get a list of bookings by mode
	List<BookingStream> getAllBookings(String mode) throws Exception;

	// get bookings by field
	List<BookingStream> getBookingByField(String mode, int field_id) throws Exception;

	// obtains a list of active bookings
	List<BookingStream> getActiveBookings() throws Exception;

	// obtains a list of active quotes
	List<Quote> getActiveQuotes() throws Exception;

	// obtains a list of active applications
	List<Application> getActiveApplications() throws Exception;

	// obtains a booking by quote ref
	Booking getBookingByQuoteRef(String quote_ref) throws Exception;

	// obtains an application by quote id
	Application getApplicationByQuoteID(int quote_id) throws Exception;

	// obtains a booking by application ID
	Booking getBookingByApplicationID(int applicationID) throws Exception;

	// list of expired quotes
	List<Quote> getExpiredQuotes(String mode) throws Exception;

	// check for cancellations
	boolean isBookingCancelled(int booking_id) throws Exception;

	// add a lease
	int addLease(Lease lease) throws Exception;

	// returns a list of leases
	List<LeaseStream> getAllLeases(String mode) throws Exception;

	// add comments to leasde
	int addCommentsToLease(int lease_id, String comments) throws Exception;

	// obtains lease by id
	Lease getLease(int lease_id) throws Exception;

	// get finanical account
	Account getAccount(int account_id) throws Exception;

	// upload contract scan
	int updateContractURL(int lease_id, String urlToContractScan) throws Exception;

	// get lease by booking id
	Lease getLeaseByBookingID(int booking_id) throws Exception;

	// set the is submitted flag
	void setIsSubmittedFlag(int quote_id, boolean b) throws Exception;

	// get booking by account id
	Booking getBookingByAccID(int account_id) throws Exception;

	// get application by quote reference
	Application getApplicationByQuoteRef(String quoteRef) throws Exception;

	// archive expred and unsubmitted quote
	int archiveQuote(Quote quote) throws Exception;

	// remove a quote
	int removeQuote(int quote_ID) throws Exception;

	// returns a list of declined applications
	List<Application> getDeclinedApplications() throws Exception;

	// returns a list of archived applications
	List<Application> getArchivedApplications() throws Exception;

	// archive application
	int archiveApplication(Application application) throws Exception;

	// remove application
	int removeApplication(int applicationID) throws Exception;

}
