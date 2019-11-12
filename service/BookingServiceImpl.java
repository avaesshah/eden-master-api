package com.eden.api.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eden.api.dao.BookingDAO;
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

@Service("bookingService")
public class BookingServiceImpl implements BookingService {

	@Autowired
	private BookingDAO bookingDAO;

	@Autowired
	private EmailService emailService;

	@Override
	public List<Calendar> getBookingsForKiosk(int kioskID) throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.getBookingForKiosk(kioskID);
	}

	@Override
	public int addQuote(AddQuote q) throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.addQuote(q);
	}

	@Override
	public boolean emailQuoteToCustomer(String emailIDCus, String messageBody) throws Exception {
		// TODO Auto-generated method stub
		return emailService.emailQuoteToCustomer(emailIDCus, messageBody);
	}

	@Override
	public Quote getQuote(String quoteRef) throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.getQuote(quoteRef);
	}

	@Override
	public int deleteQuote(int quote_ID) throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.deleteQuote(quote_ID);
	}

	@Override
	public int setQuoteExpiredFlag(int quote_ID) throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.setExpiredFlag(quote_ID);
	}

	@Override
	public int addApplication(AddQuote q) throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.addApplication(q);
	}

	@Override
	public boolean emailApplicationToManager(String emailID, String messageBody) throws Exception {
		// TODO Auto-generated method stub
		return emailService.emailApplicationToManager(emailID, messageBody);
	}

	@Override
	public List<GetApplications> getApplications(int employeeID) throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.getApplications(employeeID);
	}

	@Override
	public Quote getQuote(int quote_id) throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.getQuote(quote_id);
	}

	@Override
	public Application getApplication(int application_id) throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.getApplication(application_id);
	}

	@Override
	public int extendExpiry(int quote_ID) throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.extendExpiry(quote_ID);
	}

	@Override
	public List<ApplicationStream> getApplicationList(int employeeID) throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.getApplicationCustomers(employeeID);
	}

	@Override
	public int addBooking(Booking booking) throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.addBooking(booking);
	}

	@Override
	public int addDatesToKioskCalendar(Booking booking) throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.addDatesToKioskCalendar(booking);
	}

	@Override
	public boolean emailBookingToCustomer(String emailIDCus, String messageBody) throws Exception {
		// TODO Auto-generated method stub
		return emailService.emailBookingToCustomer(emailIDCus, messageBody);
	}

	@Override
	public int deleteApplication(int application_id) throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.deleteApplication(application_id);
	}

	@Override
	public int deleteBooking(int booking_id) throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.deleteBooking(booking_id);
	}

	@Override
	public int deleteCalendarDates(int calendar_id) throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.deleteCalendar(calendar_id);
	}

	@Override
	public int setKioskLock(int kiosk_id, boolean lock) throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.setKioskLock(kiosk_id, lock);
	}

	@Override
	public boolean emailDeclinedApplicationToCustomer(String emailIDCus, String messageBody) throws Exception {
		// TODO Auto-generated method stub
		return emailService.emailDeclinedApplicationToCustomer(emailIDCus, messageBody);
	}

	@Override
	public int updateDepositURL(int application_id, String urlToDepositScan) throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.updateDepositURL(application_id, urlToDepositScan);
	}

	@Override
	public List<Quote> getQuoteList() throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.getQuotes();
	}

	@Override
	public int setApplicationExpiredFlag(int application_id, boolean b) throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.setApplicationExpiredFlag(application_id, b);
	}

	@Override
	public int setApplicationDeclinedFlag(int application_id, boolean b) throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.setApplicationDeclinedFlag(application_id, b);
	}

	@Override
	public List<ApplicationStream> getAllApplicationList(String mode) throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.getAllApplications(mode);
	}

	@Override
	public int setIsApprovedFlag(int application_id, boolean b) throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.setIsApprovedFlag(application_id, b);
	}

	@Override
	public int updateRevisedLease(Quote quote, Application application) throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.updateRevisedLease(quote, application);
	}

	@Override
	public int setApplicationReviewFlag(int application_id, boolean b) throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.setApplicationReviewFlag(application_id, b);
	}

	@Override
	public int addCommentsToApplication(int application_id, String comments) throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.addCommentsToApplication(application_id, comments);
	}

	@Override
	public Booking getBooking(String booking_ref) throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.getBooking(booking_ref);
	}

	@Override
	public int setBookingExpiredFlag(int booking_id, boolean b) throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.setBookingExpiredFlag(booking_id, b);
	}

	@Override
	public int setBookingCancelledFlag(int booking_id, boolean b) throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.setBookingCancelledFlag(booking_id, b);
	}

	@Override
	public int setBookingExpiryDueFlag(int booking_id, boolean b) throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.setBookingExpiryDueFlag(booking_id, b);
	}

	@Override
	public int setBookingReviewFlag(int booking_id, boolean b) throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.setBookingReviewFlag(booking_id, b);
	}

	@Override
	public int addCommentsToBooking(int booking_id, String comments) throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.addCommentsToBooking(booking_id, comments);
	}

	@Override
	public List<BookingStream> getAllBookingList(String mode) throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.getAllBookings(mode);
	}

	@Override
	public List<BookingStream> getBookingByField(String mode, int field_id) throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.getBookingByField(mode, field_id);
	}

	@Override
	public List<BookingStream> getActiveBookings() throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.getActiveBookings();
	}

	@Override
	public List<Quote> getActiveQuotes() throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.getActiveQuotes();
	}

	@Override
	public List<Application> getActiveApplications() throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.getActiveApplications();
	}

	@Override
	public Booking getBookingByQuoteRef(String quoteRef) throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.getBookingByQuoteRef(quoteRef);
	}

	@Override
	public Application getApplicationByQuoteID(int quote_ID) throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.getApplicationByQuoteID(quote_ID);
	}

	@Override
	public Booking getBookingByApplicationID(int applicationID) throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.getBookingByApplicationID(applicationID);
	}

	@Override
	public List<Quote> getExpiredQuotes(String mode) throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.getExpiredQuotes(mode);
	}

	@Override
	public boolean emailCancellationToCustomer(String emailIDCus, String messageBody) throws Exception {
		// TODO Auto-generated method stub
		return emailService.emailCancellationToCustomer(emailIDCus, messageBody);
	}

	@Override
	public boolean isBookingCancelled(int booking_id) throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.isBookingCancelled(booking_id);
	}

	@Override
	public int addLease(Lease lease) throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.addLease(lease);
	}

	@Override
	public List<LeaseStream> getAllLeaseList(String mode) throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.getAllLeases(mode);
	}

	@Override
	public int addCommentsToLease(int lease_id, String comments) throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.addCommentsToLease(lease_id, comments);
	}

	@Override
	public Lease getLease(int lease_id) throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.getLease(lease_id);
	}

	@Override
	public int updateContractURL(int lease_id, String urlToContractScan) throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.updateContractURL(lease_id, urlToContractScan);
	}

	@Override
	public Lease getLeaseByBookingID(int booking_id) throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.getLeaseByBookingID(booking_id);
	}

	@Override
	public void setIsSubmittedFlag(int quote_id, boolean b) throws Exception {
		// TODO Auto-generated method stub
		bookingDAO.setIsSubmittedFlag(quote_id, b);
	}

	@Override
	public Booking getBookingByAccID(int account_id) throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.getBookingByAccID(account_id);
	}

	@Override
	public Application getApplicationByQuoteRef(String quoteRef) throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.getApplicationByQuoteRef(quoteRef);
	}

	@Override
	public int archiveQuote(Quote quote) throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.archiveQuote(quote);
	}

	@Override
	public int removeQuote(int quote_ID) throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.removeQuote(quote_ID);
	}

	@Override
	public List<Application> getDeclinedApplications() throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.getDeclinedApplications();
	}

	@Override
	public List<Application> getArchivedApplications() throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.getArchivedApplications();
	}

	@Override
	public int archiveApplication(Application application) throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.archiveApplication(application);
	}

	@Override
	public int removeApplication(int applicationID) throws Exception {
		// TODO Auto-generated method stub
		return bookingDAO.removeApplication(applicationID);
	}

}
