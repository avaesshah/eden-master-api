package com.eden.api.service;

import com.edenstar.model.User;

public interface EmailService {

	// sends an email to the user with their lost password
	boolean emailLostPassword(User u) throws Exception;

	// email a quote to the customer
	boolean emailQuoteToCustomer(String emailIDCus, String messageBody) throws Exception;

	// email a booking application to the manager
	boolean emailApplicationToManager(String emailID, String messageBody) throws Exception;

	// email booking to the customer
	boolean emailBookingToCustomer(String emailIDCus, String messageBody) throws Exception;

	// email declined application to customer
	boolean emailDeclinedApplicationToCustomer(String emailIDCus, String messageBody) throws Exception;

	// sends a cancelation email to the customer
	boolean emailCancellationToCustomer(String emailIDCus, String messageBody) throws Exception;

}
