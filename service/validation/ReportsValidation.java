package com.eden.api.service.validation;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.edenstar.model.reports.ProcessReport;

public class ReportsValidation {

	public boolean isNull(ProcessReport r, String mode) {
		// make sure that none of the mandatory fields are null

		if (r.getStaff_email_id() == null)
			return true;

		if (r.getReportViewMode() == null 
				|| r.getReportViewMode().contentEquals("") 
				|| r.getReportViewMode().isEmpty())
			return true;

//		if (r.getField_id() == 0)
//			return true;
		
		if (mode.contentEquals("dates")) {
			
			if (r.getReport_end_date() == null ||
				r.getReport_end_date().contentEquals("")
				|| r.getReport_start_date() == null
				|| r.getReport_start_date().contentEquals(""))
				return true;
				
		}

		return false;

	} // isNull


	private boolean isValidDate(String dateToValidate) {
		String pattern = "dd/MM/yyyy";

		try {
			DateTimeFormatter fmt = DateTimeFormat.forPattern(pattern);
			fmt.parseDateTime(dateToValidate);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public String checkDates(String dateStr) throws Exception {

		String isValid = "";
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

		Date startDate = null;

		try {

			startDate = format.parse(dateStr);

			if (isValidDate(dateStr) == false) {
				isValid = isValid + "[date entered is not a valid date]";
			}

			if (!isValid.equals(""))
				return isValid;

		} catch (Exception e) {
			e.printStackTrace();
		}

		if (isValid.equals(""))
			return null;

		return isValid;
	} // checkDates

}
