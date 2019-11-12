package com.eden.api.controller.engine;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DurationFieldType;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import com.edenstar.model.Discount;
import com.edenstar.model.Rate;
import com.edenstar.model.booking.KioskQuery;
import com.edenstar.model.booking.QueryLocation;

public class DiscountCalculator {

	private double discountedRate;

	public DiscountCalculator(double discountedRate) {
		super();
		this.discountedRate = discountedRate;
	}

	public DiscountCalculator() {
		super();
	}

	public double getDiscountedRate() {
		return discountedRate;
	}

	public void setDiscountedRate(double discountedRate) {
		this.discountedRate = discountedRate;
	}
	
	public double applySingleDiscount(List<Discount> discountList, Rate rate, KioskQuery k, QueryLocation l) {
		// TODO Auto-generated method stub
		// We check the duration of the lease required and obtain it in months
		// then we cross reference the duration period with the discount list
		// and obtain a discount percentage which we will apply to the the rate

		// Step 1 - obtain the to and from date
		// convert to DateTime for calculation

		String startDate = l.getStartDate();
		String endDate = l.getEndDate();

		DateTime startDateCal = formatDateForCalculation(startDate);
		DateTime endDateCal = formatDateForCalculation(endDate);

		// convert the dates into java.util.Date format for calculations
		Date leaseStartDate = formatDateForJava(startDate);
		Date leaseEndDate = endDateCal.plusDays(1).toDate();

		// first we need to obtain the number of months and days in the period
		//System.out.println("Start date = " + startDate + " and End Date = " + endDate);
		Period rentalPeriod = toMonthsAndDays(leaseStartDate, leaseEndDate);
		int months = rentalPeriod.getMonths();
		int days = rentalPeriod.getDays();

		//System.out.println(" has " + months + " months and " + days + " days");

		// we now hae a list of discounts, if it is empty then we simply return the
		// standard
		if (discountList.isEmpty())
			return rate.getRateMax();

		// we now have to check which level of discount applies by comparing the month
		// duration
		// against the fromMonth and toMonth range
		double discountRate = 0.0;
		for (int i = 0; i < discountList.size(); i++) {

			if (months >= discountList.get(i).getFromMonth() && months <= discountList.get(i).getToMonth())
				discountRate = discountList.get(i).getPercentageDiscount();
			
			if (months > discountList.get(i).getToMonth()) discountRate = discountList.get(i).getPercentageDiscount();

		} // for
		
		// if the months is outside the discount window, then we have to set it to the maximum discount
		// otherwise it will default to 0% discount
		

		//System.out.println("Percentage discount found for " + months + " = " + discountRate + " %");

		// we have to apply this discount to the standard rate and return it
		discountRate = (100 - discountRate) / 100;
		double rateAfterDiscount = rate.getRateMax() * discountRate;

		//System.out.println("Rate after discount = " + rateAfterDiscount);

		return round(rateAfterDiscount, 3);
	} // applySingleDiscount

	public double applyDiscount(List<Discount> rateDiscountList, Rate rate, KioskQuery k, QueryLocation l) {
		// We check the duration of the lease required and obtain it in months
		// then we cross reference the duration period with the discount list
		// and obtain a discount percentage which we will apply to the the rate

		// Step 1 - obtain the to and from date
		// convert to DateTime for calculation

		String startDate = l.getStartDate();
		String endDate = l.getEndDate();

		DateTime startDateCal = formatDateForCalculation(startDate);
		DateTime endDateCal = formatDateForCalculation(endDate);

		// convert the dates into java.util.Date format for calculations
		Date leaseStartDate = formatDateForJava(startDate);
		Date leaseEndDate = endDateCal.plusDays(1).toDate();

		// first we need to obtain the number of months and days in the period
		//System.out.println("Start date = " + startDate + " and End Date = " + endDate);
		Period rentalPeriod = toMonthsAndDays(leaseStartDate, leaseEndDate);
		int months = rentalPeriod.getMonths();
		int days = rentalPeriod.getDays();

		//System.out.println(" has " + months + " months and " + days + " days");

		// we now have the number of months, we have to extract the discount bands from
		// the
		// rateDiscounList for a particular rate_id

		List<Discount> discountList = new ArrayList<>();

		int rate_id = rate.getRateID();
		for (int i = 0; i < rateDiscountList.size(); i++) {
			if (rateDiscountList.get(i).getRate_id() == rate_id) {
				Discount discount = new Discount();
				discount.setDiscount_id(rateDiscountList.get(i).getDiscount_id());
				discount.setFromMonth(rateDiscountList.get(i).getFromMonth());
				discount.setToMonth(rateDiscountList.get(i).getToMonth());
				discount.setPercentageDiscount(rateDiscountList.get(i).getPercentageDiscount());
				discountList.add(discount);
			} // nested if
		} // for

		// we now hae a list of discounts, if it is empty then we simply return the
		// standard
		if (discountList.isEmpty())
			return rate.getRateMax();

		// we now have to check which level of discount applies by comparing the month
		// duration
		// against the fromMonth and toMonth range
		double discountRate = 0.0;
		for (int i = 0; i < discountList.size(); i++) {

			if (months >= discountList.get(i).getFromMonth() && months <= discountList.get(i).getToMonth())
				discountRate = discountList.get(i).getPercentageDiscount();
			
			if (months > discountList.get(i).getToMonth()) discountRate = discountList.get(i).getPercentageDiscount();

		} // for

		//System.out.println("Percentage discount found for " + months + " months = " + discountRate + " %");

		// we have to apply this discount to the standard rate and return it
		discountRate = (100 - discountRate) / 100;
		double rateAfterDiscount = rate.getRateMax() * discountRate;

		//System.out.println("Rate after discount = " + rateAfterDiscount);

		return round(rateAfterDiscount, 3);
	} // applyDiscount

	public Period toMonthsAndDays(Date from, Date to) {
		// fields used by the period - use only months and days
		if (from == null || to == null)
			return null;
		PeriodType fields = PeriodType
				.forFields(new DurationFieldType[] { DurationFieldType.months(), DurationFieldType.days() });

		Period period = new Period(from.getTime(), to.getTime()).normalizedStandard(fields);

		return period;
	}

	public Period toDays(Date from, Date to) {
		// fields used by the period - use only months and days
		if (from == null || to == null)
			return null;
		PeriodType fields = PeriodType.forFields(new DurationFieldType[] { DurationFieldType.days() });
		Period period = new Period(from.getTime(), to.getTime()).normalizedStandard(fields);

		return period;
	}

	private java.util.Date formatDateForJava(String dateStr) {

		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		Date dateForJava = null;
		// DateTime date_java_format = null;

		try {

			dateForJava = format.parse(dateStr);

			// date_java_format = new DateTime(dateForJava);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return dateForJava;

	} // formatDateForJava

	private static double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	} // round

	private org.joda.time.DateTime formatDateForCalculation(String dateStr) {

		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		Date dateForJava = null;
		DateTime date_java_format = null;

		try {

			dateForJava = format.parse(dateStr);

			date_java_format = new DateTime(dateForJava);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return date_java_format;

	} // formatDateForJava


}
