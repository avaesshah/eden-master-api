package com.eden.api.service.validation;

import com.edenstar.model.booking.ProcessBooking;

public class BookingValidation {

	public boolean isNullOrEmpty(ProcessBooking a) {

		if (a.getStaff_email_id() == null || a.getBooking_ref() == null 
				|| a.getBooking_ref().isEmpty() || a.getStaff_email_id().isEmpty())
			return true;
				
		return false;

	} // isNullOrEmpty

	public boolean isNull(ProcessBooking b) {
		// TODO Auto-generated method stub
		if (b.getStaff_email_id() == null || b.getStaff_email_id().isEmpty()) return true;
		
		return false;
	} // isNull

}
