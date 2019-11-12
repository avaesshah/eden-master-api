package com.eden.api.service;

import java.util.List;

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

public interface BookingService {

	// returns the booking calendar for the given kiosk id
	List<Calendar> getBookingsForKiosk(int kioskID) throws Exception;

	// add a quote to the database
	int addQuote(AddQuote q) throws Exception;

	// email a quote to the customer
	boolean emailQuoteToCustomer(String emailIDCus, String messageBody) throws Exception;

	// returns a quotation by quote reference
	Quote getQuote(String quoteRef) throws Exception;

	// deletes a quotation
	int deleteQuote(int quote_ID) throws Exception;

	// delete an application
	int deleteApplication(int application_id) throws Exception;

	// delete a booking
	int deleteBooking(int booking_id) throws Exception;

	// delete calendar entry
	int deleteCalendarDates(int calendar_id) throws Exception;

	// sets the expired flag
	int setQuoteExpiredFlag(int quote_ID) throws Exception;

	// add a booking application to the database
	int addApplication(AddQuote q) throws Exception;

	// send an booking application form to the manager
	boolean emailApplicationToManager(String emailID, String messageBody) throws Exception;

	// obtains a list of applications
	List<GetApplications> getApplications(int employeeID) throws Exception;

	// obtains quote by id
	Quote getQuote(int quote_id) throws Exception;

	// obtain booking application by id
	Application getApplication(int application_id) throws Exception;

	// extends the expiry of the quotation when it is submitted a an application
	int extendExpiry(int quote_ID) throws Exception;

	// obtain an application list for given manager id
	List<ApplicationStream> getApplicationList(int employeeID) throws Exception;

	// add a booking to the database
	int addBooking(Booking booking) throws Exception;

	// add booking dates to kiosk calendar
	int addDatesToKioskCalendar(Booking booking) throws Exception;

	// email the customer the booking summary
	boolean emailBookingToCustomer(String emailIDCus, String messageBody) throws Exception;

	// set the kiosk lock
	int setKioskLock(int kiosk_id, boolean lock) throws Exception;

	// email the customer to inform them that their application has been declined
	boolean emailDeclinedApplicationToCustomer(String emailIDCus, String messageBody) throws Exception;

	// updates application with the deposit scan
	int updateDepositURL(int application_id, String urlToDepositScan) throws Exception;

	// get a list of quotes
	List<Quote> getQuoteList() throws Exception;

	// set the expired flag for an application
	int setApplicationExpiredFlag(int application_id, boolean b) throws Exception;

	// sets the declined flag
	int setApplicationDeclinedFlag(int application_id, boolean b) throws Exception;

	// get a list of all the application
	List<ApplicationStream> getAllApplicationList(String string) throws Exception;

	// sets the approval flag in application
	int setIsApprovedFlag(int application_id, boolean b) throws Exception;

	// update quote with new price
	int updateRevisedLease(Quote quote, Application application) throws Exception;

	// application review flag switch
	int setApplicationReviewFlag(int application_id, boolean b) throws Exception;

	// add comments to application
	int addCommentsToApplication(int application_id, String comments) throws Exception;

	// get booking by booking ref
	Booking getBooking(String booking_ref) throws Exception;

	// set the booking expired flag
	int setBookingExpiredFlag(int booking_id, boolean b) throws Exception;
	
	// set the booking cancelled flag
	int setBookingCancelledFlag(int booking_id, boolean b) throws Exception;
	
	// set the booking expiry due flag
	int setBookingExpiryDueFlag(int booking_id, boolean b) throws Exception;
	
	// set the booking review flag
	int setBookingReviewFlag(int booking_id, boolean b) throws Exception;

	// set booking commments
	int addCommentsToBooking(int booking_id, String comments) throws Exception;

	// get a list of bookings by mode
	List<BookingStream> getAllBookingList(String mode) throws Exception;

	// get all bookings by field id (location_id, kiosk_id etc)
	List<BookingStream> getBookingByField(String mode, int field_id) throws Exception;

	// get all active bookings
	List<BookingStream> getActiveBookings() throws Exception;

	// get all active quotes
	List<Quote> getActiveQuotes() throws Exception;

	// get all active applications
	List<Application> getActiveApplications() throws Exception;

	// obtains a booking by the quoteRef
	Booking getBookingByQuoteRef(String quoteRef) throws Exception;

	// obtains an application by quote id
	Application getApplicationByQuoteID(int quote_ID) throws Exception;

	// obtains booking by application ID
	Booking getBookingByApplicationID(int applicationID) throws Exception;

	// obtains a list of expired quotes
	List<Quote> getExpiredQuotes(String mode) throws Exception;

	// booking cancellation email
	boolean emailCancellationToCustomer(String emailIDCus, String messageBody) throws Exception;

	// checks if a booking is cancelled
	boolean isBookingCancelled(int booking_id) throws Exception;

	// add a new lease
	int addLease(Lease lease) throws Exception;

	// returns a list of all contracts
	List<LeaseStream> getAllLeaseList(String string) throws Exception;

	// add comments to lease
	int addCommentsToLease(int lease_id, String comments) throws Exception;

	// gets a lease by lease id
	Lease getLease(int lease_id) throws Exception;

	// update lease with contract url 
	int updateContractURL(int lease_id, String urlToContractScan) throws Exception;

	// obtains lease by booking id
	Lease getLeaseByBookingID(int booking_id) throws Exception;

	// sets the submitted for application flag
	void setIsSubmittedFlag(int quote_id, boolean b) throws Exception;

	// booking by account id
	Booking getBookingByAccID(int account_id) throws Exception;

	// get application by quote reference
	Application getApplicationByQuoteRef(String quoteRef) throws Exception;

	// archive unsubmitted and expired quote
	int archiveQuote(Quote quote) throws Exception;

	// remove quote
	int removeQuote(int quote_ID) throws Exception;

	// obtain a list of declined applications
	List<Application> getDeclinedApplications() throws Exception;

	// obtains a list of archived applications
	List<Application> getArchivedApplications() throws Exception;

	// archive application
	int archiveApplication(Application application) throws Exception;

	// remove application
	int removeApplication(int applicationID) throws Exception;

}
