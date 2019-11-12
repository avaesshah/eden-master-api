package com.eden.api.service.validation;

import com.edenstar.model.dash.CreateCompany;

public class CompanyValidation {

	public boolean fieldsAreEmpty(CreateCompany c) {

		// check to make sure that the mandatory parameters are not empty
		if (c.getCompanyName().contentEquals("") || c.getCustomerID() == 0
				|| c.getStaff_email().contentEquals("")) {

			return true;

		} // if

		return false;

	} // fieldsAreEmpty
	
	public boolean isNull(CreateCompany c) {
		// make sure that none of the mandatory fields are null

			if (c.getCompanyName() == null)
				return true;
			if (c.getStaff_email() == null)
				return true;
			if (c.getCustomerID() == 0)
				return true;


		return false;

	} // isNull

} // CompanyValidation
