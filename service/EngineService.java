package com.eden.api.service;

import com.edenstar.model.Customer;
import com.edenstar.model.dash.GetCustomer;

public interface EngineService {

	// check to see if quote is on database
	boolean quoteExists(String quoteRef) throws Exception;

	// check to see of the staff user exists
	boolean userExists(String staff_email) throws Exception;

	// check to see if a kiosk exists
	boolean kioskExists(int kiosk_id) throws Exception;

	// check if customer exists
	boolean customerExists(int customerID) throws Exception;

	// check user clearance
	boolean checkClearance(String staff_email) throws Exception;

	// check if company exists
	boolean companyExists(int companyID) throws Exception;

	// check if a customer exists by customer email
	boolean customerExists(String cust_email) throws Exception;

	// check if location exists by location id
	boolean locationExists(int location_id) throws Exception;

	// check if a location exists by locartion name
	boolean locationExists(String locationName) throws Exception;

	// check if product exists
	boolean productExists(int productID) throws Exception;

	// check if a product photo exists
	boolean productPhotoExists(int productPhotoID) throws Exception;

	// check if a zone exists
	boolean zoneExists(int zone_id) throws Exception;

	// check if a manager exists on the database
	boolean managerExists(int employee_id) throws Exception;

	// check to see if the employee has manager clearance
	boolean checkManagerClearance(String emailID) throws Exception;

	// check if an application exists
	boolean applicationExists(int application_id) throws Exception;

	// check if a booking exists by ooking reference
	boolean bookingExists(String booking_ref) throws Exception;

	// this goes through all the bookings and flags them as expired or nearly
	// expired as the case may be
	void refreshBookingFlags() throws Exception;

	// this goes through all th e quotes and finds out which ones have expired and
	// flags them
	void refreshQuoteFlags() throws Exception;
	
	// checks for any overdue payments and sets the flag
	void refreshOverdueFlags() throws Exception;

	// checks to make sure that the applications are not expired, flags if it is
	void refreshApplicationFlags() throws Exception;

	// deletes expired quotations
	void cleanUpQuotes() throws Exception;

	// deletes expired applications
	void cleanUpApplications() throws Exception;

	// deletes expired bookings
	void cleanUpBookings() throws Exception;

	// checks to see if a booking has been cancelled
	boolean bookingCancelled(int booking_id) throws Exception;

	// returns customer information
	GetCustomer getCustomerInfo(Customer c) throws Exception;

	// check to see if a lease exists
	boolean leaseExists(int lease_id) throws Exception;

	// check fo maanager or accounts status
	boolean checkAccountClearance(String staff_email_id) throws Exception;

	// archives unsubmitted and expired quotes
	void archiveQuotes() throws Exception;

	// archives declined applications and corresponding quotes
	void archiveApplications() throws Exception;

	

}
