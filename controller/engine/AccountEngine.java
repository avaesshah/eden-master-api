package com.eden.api.controller.engine;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.DurationFieldType;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import com.edenstar.model.Payment;

public class AccountEngine {

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
		//DateTime date_java_format = null;

		try {

			dateForJava = format.parse(dateStr);

			//date_java_format = new DateTime(dateForJava);

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

	public List<Payment> generatePaymentSchedule(String startDate, String endDate, double leaseTotal, int account_id) {
		List<Payment> paymentSchedule = new ArrayList<>();

		// convert to DateTime for calculation
		DateTime startDateCal = formatDateForCalculation(startDate);
		DateTime endDateCal = formatDateForCalculation(endDate);

		// convert the dates into java.util.Date format for calculations
		Date leaseStartDate = formatDateForJava(startDate);
		Date leaseEndDate = endDateCal.plusDays(1).toDate();

		// first we need to obtain the number of months and days in the period
		System.out.println("Start date = " + startDate + " and End Date = " + endDate);
		Period rentalPeriod = toMonthsAndDays(leaseStartDate, leaseEndDate);
		int months = rentalPeriod.getMonths();
		int days = rentalPeriod.getDays();

		System.out.println(" has " + months + " months and " + days + " days");

		if (months == 0 && days == 0) {
			return paymentSchedule;
		}

		if (months == 0) {
			// we generate out first payment schedule which is for under 1 month
			// we shall round up the rent to the nearest month
			DateTime oneMonthLater = startDateCal.plusMonths(0);
			java.util.Date dueByDate = oneMonthLater.toDate();
			System.out.println("Payment due date = " + dueByDate.toString());
			Payment firstPayment = new Payment();

			firstPayment.setDueByDate(dueByDate);
			firstPayment.setAccount_id(account_id);
			firstPayment.setAmountDue(round(leaseTotal,2));
			firstPayment.setOverdue(0);
			firstPayment.setIsPaid(0);

			paymentSchedule.add(firstPayment);

		} else if (days == 0 && months > 0) {

			double monthlyPayments = Math.round(leaseTotal / months);

			for (int i = 0; i < months; i++) {

				DateTime oneMonthLater = startDateCal.plusMonths(i);
				java.util.Date dueByDate = oneMonthLater.toDate();

				Payment monthlyPayment = new Payment();

				monthlyPayment.setDueByDate(dueByDate);
				monthlyPayment.setAccount_id(account_id);
				monthlyPayment.setAmountDue(round(monthlyPayments,2));
				monthlyPayment.setOverdue(0);
				monthlyPayment.setIsPaid(0);

				paymentSchedule.add(monthlyPayment);

			} // for

		} else if (days > 0 && months > 0) {

			// first we calculate the monthly payments
			// we need to know how many days are between the startDate and EndDate

			int totalNumDays = Days
					.daysBetween(startDateCal.withTimeAtStartOfDay(), endDateCal.plusDays(1).withTimeAtStartOfDay())
					.getDays();
			System.out.println("Rental period in days = " + totalNumDays);

			// we then have to calculate the daily rate
			double rate = (leaseTotal / totalNumDays);
			System.out.println(
					"daily rate = leasetotal (" + leaseTotal + "/" + "totalNumDays (" + totalNumDays + ") = " + rate);

			System.out.println("offset start date = " + startDateCal.plusMonths(months).withTimeAtStartOfDay().toDate()
					+ " , end date = " + endDateCal.plusDays(0).withTimeAtStartOfDay().toDate());
			int offsetDays = Days.daysBetween(startDateCal.plusMonths(months).withTimeAtStartOfDay(),
					endDateCal.plusDays(0).withTimeAtStartOfDay()).getDays();
			offsetDays = Math.abs(offsetDays);
			System.out.println("Days offset = " + offsetDays);

			// first we calculate the lease for the remaining days
			double leaseForRemainingDays = offsetDays * rate;
			System.out.println("lease for remaining days = " + days + " days " + leaseForRemainingDays);

			// subtract that for the months remaining
			double leaseRemainingForMonths = leaseTotal - leaseForRemainingDays;
			System.out.println("lease remaining for months = leaseTotal(" + leaseTotal + ") - leaseForRemainingDays ("
					+ leaseForRemainingDays + ") =" + leaseRemainingForMonths);

			// now we calculate the monthly lease
			double monthlyLease = leaseRemainingForMonths / months;

			double carryOver = 0.0;
			double remainderRent = 0.0;

			if (monthlyLease > 1000.00) {
				remainderRent = monthlyLease % 100;
				System.out.println("Carry over per month = " + remainderRent);
				carryOver = remainderRent * months;
				monthlyLease = monthlyLease - remainderRent;
			}

			// now we calculate the monthly payment schedule
			for (int i = 0; i < months; i++) {

				DateTime oneMonthLater = startDateCal.plusMonths(i);
				java.util.Date dueByDate = oneMonthLater.toDate();

				Payment monthlyPayment = new Payment();

				monthlyPayment.setDueByDate(dueByDate);
				monthlyPayment.setAccount_id(account_id);
				monthlyPayment.setAmountDue(round(monthlyLease,2));
				monthlyPayment.setOverdue(0);
				monthlyPayment.setIsPaid(0);

				paymentSchedule.add(monthlyPayment);

			} // for

			// and we calulate the last lease payment and add it to the schedule

			DateTime lastMonth = startDateCal.plusMonths(months);
			java.util.Date dueByDate = lastMonth.toDate();
			System.out.println("Payment due date = " + dueByDate.toString());
			Payment lastPayment = new Payment();

			lastPayment.setDueByDate(dueByDate);
			lastPayment.setAccount_id(account_id);
			lastPayment.setAmountDue(round(leaseForRemainingDays + carryOver, 2));
			lastPayment.setOverdue(0);
			lastPayment.setIsPaid(0);

			paymentSchedule.add(lastPayment);
		}

		return paymentSchedule;
	} // generatePaymentSchedule

}
