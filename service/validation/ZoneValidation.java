package com.eden.api.service.validation;

import com.edenstar.model.dash.CreateZone;

public class ZoneValidation {

	public boolean fieldsAreEmpty(CreateZone z) {

		// check to make sure that the mandatory parameters are not empty
		if (z.getStaff_email_id().contentEquals("") || z.getLocationID() == 0 || z.getRate() == null) {

			return true;

		} // if

		return false;

	} // fieldsAreEmpty

	public boolean isNull(CreateZone z) {
		// make sure that none of the mandatory fields are null

		if (z.getStaff_email_id() == null)
			return true;
		if (z.getRate() == null)
			return true;
		if (z.getLocationID() == 0)
			return true;

		return false;

	} // isNull

	public boolean isNullWithDiscount(CreateZone z) {
		// make sure that none of the mandatory fields are null

		if (z.getStaff_email_id() == null)
			return true;
		if (z.getRateAndDiscount().getStandardRate() == 0)
			return true;
		if (z.getLocationID() == 0)
			return true;

		return false;

	} // isNull

	public boolean fieldsAreEmptyWithDiscount(CreateZone z) {

		// check to make sure that the mandatory parameters are not empty
		if (z.getStaff_email_id().contentEquals("") || z.getLocationID() == 0
				|| z.getRateAndDiscount().getStandardRate() == 0) {

			return true;

		} // if

		return false;

	} // fieldsAreEmpty

	public boolean validateRatesWithDiscount(CreateZone z) {
		// makes sure that the rate passed for the zone is within normal values
		boolean isValid = true;

		if (z.getRateAndDiscount().getStandardRate() <= 0.0)
			isValid = false;

		// if all the values in the array are normal (i.e. isValid = true)
		// we should calculate the coefficient

		if (isValid) {

			z.getRate().setStandardRate(z.getRateAndDiscount().getStandardRate());
			z.getRate().setRateCoeff(0.00);
			z.getRate().setDiscountDurationDays(365);
			z.getRate().setRateMax(z.getRateAndDiscount().getStandardRate());
			z.getRate().setRateMin(z.getRateAndDiscount().getStandardRate());

			z.getRateAndDiscount().setDiscountDurationDays(365);
			z.getRateAndDiscount().setRateCoeff(0.00);
			z.getRateAndDiscount().setRateMax(z.getRateAndDiscount().getStandardRate());
			z.getRateAndDiscount().setRateMin(z.getRateAndDiscount().getStandardRate());

		} // set the rate coefficient

		return isValid;

	} // validateRates

	public boolean validateRates(CreateZone z) {
		// makes sure that the rate passed for the zone is within normal values
		boolean isValid = true;
		// double rateCoeff = 0.00d;

//			if (z.getRate().getRateMax() < 0.0
//					|| z.getRate().getRateMin() > z.getRate().getRateMax()
//					|| z.getRate().getDiscountDurationDays() < 1)
//				isValid = false;

		if (z.getRate().getStandardRate() <= 0.0)
			isValid = false;

		// if all the values in the array are normal (i.e. isValid = true)
		// we should calculate the coefficient

		if (isValid) {

			z.getRate().setRateCoeff(0.00);
			z.getRate().setDiscountDurationDays(365);
			z.getRate().setRateMax(z.getRate().getStandardRate());
			z.getRate().setRateMin(z.getRate().getStandardRate());

//				double coeff = (z.getRate().getRateMin() - z.getRate().getRateMax()) 
//						/ z.getRate().getDiscountDurationDays();
//			
//				// round off to 2 decimal places
//				rateCoeff = round(coeff, 2);
//				
//				// set the coefficient 
//				z.getRate().setRateCoeff(rateCoeff);

		} // set the rate coefficient

		return isValid;

	} // validateRates

	public boolean validateDiscount(CreateZone z) {
		boolean isValid = true;

		for (int i = 0; i < z.getRateAndDiscount().getDiscountList().size(); i++) {

			if (z.getRateAndDiscount().getDiscountList().get(i).getPercentageDiscount() < 0.00
					|| z.getRateAndDiscount().getDiscountList().get(i).getPercentageDiscount() > 100.00)
				isValid = false;

		} // for

		return isValid;

	} // validatDiscount

	public boolean validateDiscountMonths(CreateZone z) {
		boolean isValid = true;

		for (int i = 0; i < z.getRateAndDiscount().getDiscountList().size(); i++) {

			if (z.getRateAndDiscount().getDiscountList().get(i).getFromMonth() < 0
					|| z.getRateAndDiscount().getDiscountList().get(i).getFromMonth() >= z.getRateAndDiscount()
							.getDiscountList().get(i).getToMonth())
				isValid = false;

		}

		return isValid;
	} // validateDiscountMonths

} // Zone validation
