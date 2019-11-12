package com.eden.api.controller.engine;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.eden.api.util.Constants;
import com.edenstar.model.Calendar;
import com.edenstar.model.Company;
import com.edenstar.model.Customer;
import com.edenstar.model.Discount;
import com.edenstar.model.Kiosk;
import com.edenstar.model.Location;
import com.edenstar.model.Quote;
import com.edenstar.model.Rate;
import com.edenstar.model.User;
import com.edenstar.model.Zone;
import com.edenstar.model.booking.AddQuote;
import com.edenstar.model.booking.DateSlice;
import com.edenstar.model.booking.GetQuote;
import com.edenstar.model.booking.GridBuilder;
import com.edenstar.model.booking.KioskQuery;
import com.edenstar.model.booking.QueryLocation;
import com.edenstar.model.booking.forms.QuotationForm;
import com.edenstar.model.dash.CreateCustomer;
import com.edenstar.model.dash.CreateLocation;

import java.awt.Color;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class Availability {

	public List<DateSlice> checkAvailability(List<DateSlice> inputDates, List<ArrayList<DateSlice>> bookingList) {

		// here we check each dateSlice in the userDates list against all the DateSlices
		// in the
		// bookingList, if there is a match we set the flag and assign the calender id

		for (int i = 0; i < inputDates.size(); i++) {

			for (int j = 0; j < bookingList.size(); j++) {

				for (int k = 0; k < bookingList.get(j).size(); k++) {

					// compare the dates
					if (bookingList.get(j).get(k).getDate().equals(inputDates.get(i).getDate())) {

						inputDates.get(i).setAvailable(false);
						inputDates.get(i).setCalenderID(bookingList.get(j).get(k).getCalenderID());

					} // if statement

				} // innermost for loop

			} // inner for loop

		} // outer for loop

		return inputDates;
	} // checkAvailability

	public List<DateSlice> generateDateList(DateTime startDate, DateTime endDate) throws Exception {

		List<DateSlice> dateList = new ArrayList<DateSlice>();
		// SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		int i = 0;
		int durationPeriod = 0;

		try {

			durationPeriod = Days
					.daysBetween(startDate.withTimeAtStartOfDay(), endDate.plusDays(1).withTimeAtStartOfDay())
					.getDays();

			for (i = 0; i < durationPeriod; i++) {

				DateSlice d = new DateSlice();
				DateTime dateTime = new DateTime(startDate);

				d.setAvailable(true); // availability flag set to true by default;

				dateTime = dateTime.plusDays(i); // increment the days by
				d.setDate(dateTime.toDate());

				dateList.add(d); // add the DateSlice to the list !

			} // for loop

		} catch (Exception e) {
			e.printStackTrace();
		}

		return dateList;
	} // generateDateList

	public void printDateList(List<DateSlice> dList) {

		int i = 0;

		do {
			System.out.println(dList.get(i).toString());
			i++;
		} while (i < dList.size());

	} // printDateList

	public List<DateSlice> getDateSliceList(Calendar c) {

		String startDateIn = c.getLeaseStartDate().toString(); // "01/12/2019";
		String endDateIn = c.getLeaseEndDate().toString(); // "04/12/2019";

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

		Date startDate = null;
		Date endDate = null;

		List<DateSlice> dateListIn = new ArrayList<DateSlice>();

		try {

			startDate = format.parse(startDateIn);
			endDate = format.parse(endDateIn);

			DateTime start_date = new DateTime(startDate);
			DateTime end_date = new DateTime(endDate);

			// we now generate an array of DateSlices to perform our date operations
			dateListIn = new ArrayList<DateSlice>(this.generateDateList(start_date, end_date));

			for (int i = 0; i < dateListIn.size(); i++) {
				dateListIn.get(i).setCalenderID(c.getCalendarID());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return dateListIn;

	} // getDateSliceList

	// recinded - replaced with discount model
	public KioskQuery mapKiosk(KioskQuery k, Kiosk kiosk, List<Rate> rateList, List<Zone> zoneList) {

		k.setKioskID(kiosk.getKioskID());
		k.setKioskNumber(kiosk.getKioskNumber());
		k.setGridLocationRow(kiosk.getGridLocationRow());
		k.setGridLocationColumn(kiosk.getGridLocationColumn());
		k.setIsVoid(kiosk.getIsVoid());
		k.setIsLocked(kiosk.getIsLocked());
		k.setZoneID(kiosk.getZoneID());

		for (int i = 0; i < zoneList.size(); i++) {

			if (k.getZoneID() == zoneList.get(i).getZoneID()) {
				k.setZone(zoneList.get(i));
			}

		} // zonelist

		for (int i = 0; i < rateList.size(); i++) {

			if (k.getZoneID() == rateList.get(i).getZoneID()) {
				// here we set the daily rate and calculate the total lease
				k.setDaily_rate(round(
						rateList.get(i).getRateMax() + (rateList.get(i).getRateCoeff() * k.getLease_duration()), 3));

				if (k.getDaily_rate() < rateList.get(i).getRateMin())
					k.setDaily_rate(rateList.get(i).getRateMin());

				// we now have the daily rate, we can calculate the total lease
				k.setLease_total(round(k.getDaily_rate() * k.getLease_duration(), 2));
			}

		} // ratelist

		return k;
	} // mapKiosk

	public KioskQuery mapKiosk(KioskQuery k, Kiosk kiosk, List<Rate> rateList, List<Zone> zoneList,
			List<Discount> rateDiscountList, QueryLocation l) {

		k.setKioskID(kiosk.getKioskID());
		k.setKioskNumber(kiosk.getKioskNumber());
		k.setGridLocationRow(kiosk.getGridLocationRow());
		k.setGridLocationColumn(kiosk.getGridLocationColumn());
		k.setIsVoid(kiosk.getIsVoid());
		k.setIsLocked(kiosk.getIsLocked());
		k.setZoneID(kiosk.getZoneID());

		for (int i = 0; i < zoneList.size(); i++) {

			if (k.getZoneID() == zoneList.get(i).getZoneID()) {
				k.setZone(zoneList.get(i));
			}

		} // zonelist

		for (int i = 0; i < rateList.size(); i++) {

			if (k.getZoneID() == rateList.get(i).getZoneID()) {
				// here we set the daily rate and calculate the total lease

				// first we need to check if there is a discount available against the rate_id
				// the discount is carried in the rateDiscountList
				if (rateDiscountList.isEmpty()) {
					// System.out.println("calculating daily rate without discount ...");

					k.setDaily_rate(round(
							rateList.get(i).getRateMax() + (rateList.get(i).getRateCoeff() * k.getLease_duration()),
							3));

					if (k.getDaily_rate() < rateList.get(i).getRateMin())
						k.setDaily_rate(rateList.get(i).getRateMin());

					// System.out.println("Daily rate without discount = " + k.getDaily_rate());
				} else {
					// apply standard rate
					// System.out.println("calculating daily rate with discount ...");

					// apply discount
					DiscountCalculator discountCalc = new DiscountCalculator();
					Rate rate = rateList.get(i);
					double rateAfterDiscount = discountCalc.applyDiscount(rateDiscountList, rate, k, l);
					k.setDaily_rate(rateAfterDiscount);
//					System.out.println("rateBeforeDiscount = " + rate.getRateMax());
//					System.out.println("rateAfterDiscount = " + rateAfterDiscount);

				} // if

				// we now have the daily rate, we can calculate the total lease
				k.setLease_total(round(k.getDaily_rate() * k.getLease_duration(), 2));

			} // if

		} // ratelist

		return k;
	} // mapKiosk

	// recinded - replaced with discount model
	public KioskQuery mapKiosk(KioskQuery k, Kiosk kiosk, Rate rate, Zone zone) {

		k.setKioskID(kiosk.getKioskID());
		k.setKioskNumber(kiosk.getKioskNumber());
		k.setGridLocationRow(kiosk.getGridLocationRow());
		k.setGridLocationColumn(kiosk.getGridLocationColumn());
		k.setIsVoid(kiosk.getIsVoid());
		k.setIsLocked(kiosk.getIsLocked());
		k.setZoneID(kiosk.getZoneID());
		k.setZone(zone);

		// set the pricing information
		double dailyRate = round(rate.getRateMax() + (rate.getRateCoeff() * k.getLease_duration()), 3);
		if (dailyRate < rate.getRateMin())
			dailyRate = rate.getRateMin();

		k.setDaily_rate(dailyRate);
		k.setLease_total(round(k.getDaily_rate() * k.getLease_duration(), 2));

		return k;
	} // mapKiosk

	public KioskQuery mapKiosk(KioskQuery k, Kiosk kiosk, Rate rate, Zone zone, List<Discount> discountList,
			QueryLocation l) {

		k.setKioskID(kiosk.getKioskID());
		k.setKioskNumber(kiosk.getKioskNumber());
		k.setGridLocationRow(kiosk.getGridLocationRow());
		k.setGridLocationColumn(kiosk.getGridLocationColumn());
		k.setIsVoid(kiosk.getIsVoid());
		k.setIsLocked(kiosk.getIsLocked());
		k.setZoneID(kiosk.getZoneID());
		k.setZone(zone);

		if (discountList.isEmpty()) {
			// set the pricing information
			double dailyRate = round(rate.getRateMax() + (rate.getRateCoeff() * k.getLease_duration()), 3);
			if (dailyRate < rate.getRateMin())
				dailyRate = rate.getRateMin();
			k.setDaily_rate(dailyRate);
		} else {
			// apply discount
			DiscountCalculator discountCalc = new DiscountCalculator();
			double rateAfterDiscount = discountCalc.applySingleDiscount(discountList, rate, k, l);
			k.setDaily_rate(rateAfterDiscount);
		} // if

		k.setLease_total(round(k.getDaily_rate() * k.getLease_duration(), 2));

		return k;
	} // mapKiosk

	private static double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	} // round

	public int getDuration(String start_date_str, String end_date_str) {

		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

		Date startDate = null;
		Date endDate = null;
		int leaseDuration = 0;

		try {

			startDate = format.parse(start_date_str);
			endDate = format.parse(end_date_str);

			DateTime start_date = new DateTime(startDate);
			DateTime end_date = new DateTime(endDate);

			leaseDuration = Days.daysBetween(start_date, end_date).getDays() + 1;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return leaseDuration;

	} // getDuration

	public void processFlags(List<KioskQuery> k) {
		int bookingCount = 0;
		// method which checks the calendar for each kiosk and sets the availability
		// flag

		for (int i = 0; i < k.size(); i++) {
			bookingCount = 0;

			for (int j = 0; j < k.get(i).getDateList().size(); j++) {

				if (k.get(i).getDateList().get(j).getCalenderID() > 0) {
					bookingCount++;
				}

			} // inner for

			if (k.get(i).getIsLocked() < 1 && k.get(i).getIsVoid() < 1) {

				if (bookingCount == k.get(i).getDateList().size()) {
					// kiosk is fully booked
					k.get(i).setAvailability_color(Constants.not_avail_colour);
					k.get(i).setAvailability_status(Constants.not_avail);
					k.get(i).setAvailability_code(Constants.not_avail_c);
					k.get(i).setNumber_days_available(0);
				}

				if (bookingCount > 0 && bookingCount < k.get(i).getDateList().size()) {
					k.get(i).setAvailability_color(Constants.part_avail_colour);
					k.get(i).setAvailability_status(Constants.part_avail);
					k.get(i).setAvailability_code(Constants.part_avail_c);
					k.get(i).setNumber_days_available(k.get(i).getDateList().size() - bookingCount);
				}

				if (bookingCount == 0) {
					k.get(i).setAvailability_color(Constants.avail_colour);
					k.get(i).setAvailability_status(Constants.avail);
					k.get(i).setAvailability_code(Constants.avail_c);
					k.get(i).setNumber_days_available(k.get(i).getDateList().size());

				}
			} else {

				if (k.get(i).getIsLocked() == 1) { // kiosk locked

					k.get(i).setAvailability_color(Constants.lock_avail_colour);
					k.get(i).setAvailability_status(Constants.lock_avail);
					k.get(i).setAvailability_code(Constants.lock_avail_c);
					k.get(i).setNumber_days_available(0);
				}

				if (k.get(i).getIsVoid() == 1) { // kiosk void

					k.get(i).setAvailability_color(Constants.void_avail_colour);
					k.get(i).setAvailability_status(Constants.void_avail);
					k.get(i).setAvailability_code(Constants.void_avail_c);
					k.get(i).setNumber_days_available(0);

				}

				// outer if
			}

		} // outer for

	}

	public void setGridSize(List<KioskQuery> k, int row_max, int col_max) {

		// method which checks the calendar for each kiosk and sets the availability
		// flag

		for (int i = 0; i < k.size(); i++) {

			k.get(i).setLocation_grid_row_max(row_max);
			k.get(i).setLocation_grid_column_max(col_max);

		} // inner for

	} // setGridSize

	public void processFlags(KioskQuery k) {
		int bookingCount = 0;
		// method which checks the calendar for each kiosk and sets the availability
		// flag

		for (int j = 0; j < k.getDateList().size(); j++) {

			if (k.getDateList().get(j).getCalenderID() > 0) {
				bookingCount++;
			}

		} // inner for

		if (k.getIsLocked() < 1 && k.getIsVoid() < 1) {

			if (bookingCount == k.getDateList().size()) {
				// kiosk is fully booked
				k.setAvailability_color(Constants.not_avail_colour);
				k.setAvailability_status(Constants.not_avail);
				k.setAvailability_code(Constants.not_avail_c);
				k.setNumber_days_available(0);
			}

			if (bookingCount > 0 && bookingCount < k.getDateList().size()) {
				k.setAvailability_color(Constants.part_avail_colour);
				k.setAvailability_status(Constants.part_avail);
				k.setAvailability_code(Constants.part_avail_c);
				k.setNumber_days_available(k.getDateList().size() - bookingCount);
			}

			if (bookingCount == 0) {
				k.setAvailability_color(Constants.avail_colour);
				k.setAvailability_status(Constants.avail);
				k.setAvailability_code(Constants.avail_c);
				k.setNumber_days_available(k.getDateList().size());

			}
		} else {

			if (k.getIsLocked() == 1) { // kiosk locked

				k.setAvailability_color(Constants.lock_avail_colour);
				k.setAvailability_status(Constants.lock_avail);
				k.setAvailability_code(Constants.lock_avail_c);
				k.setNumber_days_available(0);
			}

			if (k.getIsVoid() == 1) { // kiosk void

				k.setAvailability_color(Constants.void_avail_colour);
				k.setAvailability_status(Constants.void_avail);
				k.setAvailability_code(Constants.void_avail_c);
				k.setNumber_days_available(0);

			} // outer if
		}

	}

	public void formatDates(List<KioskQuery> kioskQueryList) {

		for (int i = 0; i < kioskQueryList.size(); i++) {

			for (int j = 0; j < kioskQueryList.get(i).getDateList().size(); j++) {

				String dateString = new SimpleDateFormat("dd/MM/yyyy")
						.format(kioskQueryList.get(i).getDateList().get(j).getDate());

				kioskQueryList.get(i).getDateList().get(j).setKioskCalanderDATE(dateString);
				kioskQueryList.get(i).getDateList().get(j).setDate(null);

			} // inner for loop

		} // outer for loop

	}

	public void formatDates(KioskQuery k_query) {

		for (int j = 0; j < k_query.getDateList().size(); j++) {

			String dateString = new SimpleDateFormat("dd/MM/yyyy").format(k_query.getDateList().get(j).getDate());
			k_query.getDateList().get(j).setKioskCalanderDATE(dateString);
			k_query.getDateList().get(j).setDate(null);

		} // inner for loop
	}

	public void mapCustomer(AddQuote q, Customer c) {

		q.setCustomerID(c.getCustomerID());
		q.setFirstName(c.getFirstName());
		q.setLastName(c.getLastName());
		q.setEmailIDCus(c.getEmailIDCus());
		q.setAddress(c.getAddress());
		q.setEmirate(c.getEmirate());
		q.setMobileNumber(c.getMobileNumber());
		q.setPoBox(c.getPoBox());
		q.setOfficeNumber(c.getOfficeNumber());
		q.setTradeLicence(c.getTradeLicence());
		q.setTradeName(c.getTradeName());

	}

	public void mapCustomer(CreateCustomer customer, AddQuote q) {
		String s = "QUOTE ONLY - customer information pending booking";

		customer.setFirstName(q.getFirstName());
		customer.setLastName(q.getLastName());
		customer.setEmailIDCus(q.getEmailIDCus());

		customer.setAddress(s);
		customer.setEmirate(s);
		customer.setMobileNumber(s);
		customer.setPoBox(s);
		customer.setOfficeNumber(s);
		customer.setTradeLicence(0);
		customer.setTradeName(s);

		customer.setStaff_email(q.getStaff_email_id());

	}

	public void mapQuote(AddQuote q, CreateCustomer customer) {

		q.setCustomerID(customer.getCustomerID());
		q.setEmailIDCus(customer.getEmailIDCus());
		q.setFirstName(customer.getFirstName());
		q.setLastName(customer.getLastName());
		q.setAddress(customer.getAddress());
		q.setPoBox(customer.getPoBox());
		q.setEmirate(customer.getEmirate());
		q.setMobileNumber(customer.getMobileNumber());
		q.setOfficeNumber(customer.getOfficeNumber());
		q.setTradeLicence(customer.getTradeLicence());
		q.setTradeName(customer.getTradeName());

	}

	public QuotationForm generateQuoteEmail(AddQuote q, User staff_details) {
		QuotationForm qForm = new QuotationForm();
		qForm.loadValues(q, staff_details);
		return qForm;

	}

	public List<GridBuilder> scaleBackJSON(List<KioskQuery> k, List<GridBuilder> gridBuilder, Location location) {

		for (int i = 0; i < k.size(); i++) {

			GridBuilder g = new GridBuilder();

			g.setLocationName(k.get(i).getLocation_name());
			g.setLocationArea(location.getLocationArea());
			g.setLocationMap(location.getMapURL());

			g.setKioskID(k.get(i).getKioskID());
			g.setKioskNumber(k.get(i).getKioskNumber());

			g.setZoneNumber(k.get(i).getZone().getZoneNumber());
			g.setZoneName(k.get(i).getZone().getZoneName());
			g.setZoneColour(k.get(i).getZone().getZoneColour());

			g.setAvailability_color(k.get(i).getAvailability_color());
			g.setAvailability_status(k.get(i).getAvailability_status());
			g.setAvailability_code(k.get(i).getAvailability_code());

			g.setGridLocationRow(k.get(i).getGridLocationRow());
			g.setGridLocationColumn(k.get(i).getGridLocationColumn());

			g.setLocation_grid_row_max(k.get(i).getLocation_grid_row_max());
			g.setLocation_grid_column_max(k.get(i).getLocation_grid_column_max());

			g.setLease_total(k.get(i).getLease_total());

			gridBuilder.add(g);

		}

		return gridBuilder;

	}

	public void slimDownJSON(KioskQuery k_query) {

		k_query.setZoneID(0);
		k_query.getZone().setZoneID(0);
		k_query.getZone().setLocationID(0);
		k_query.setNumber_days_available(0);
		k_query.setGridLocationRow(0);
		k_query.setGridLocationColumn(0);

	}

	public String generateZoneColour(int i) {

		// rgb(253,126,20) - orange
		int r, g, b;
		r = 253;
		g = 126 - (i * 5);
		b = 0;

		Color color = new Color(r, g, b);
		String hex = Integer.toHexString(color.getRGB() & 0xffffff);
		if (hex.length() < 6) {
			hex = "0" + hex;
		}
		hex = "#" + hex;

		return hex;
	} // generateZoneColour

	public String generateZoneColour(String zoneName) {
		if (zoneName == null)
			zoneName = "purple";
		ColourDetector colourDetector = new ColourDetector();
		return colourDetector.colourDetection(zoneName);
	} // generateZoneColour

	public String formatDateForJava(String dateFromDB) {

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date dateForJava = null;
		DateTime date_Java_format = null;

		try {

			dateForJava = format.parse(dateFromDB);

			date_Java_format = new DateTime(dateForJava);

		} catch (Exception e) {
			e.printStackTrace();
		}

		DateTimeFormatter fmt = DateTimeFormat.forPattern("dd/MM/yyyy");
		// String dateInString = date_Java_format.toString(fmt);

		return date_Java_format.toString(fmt);

	} // formatDateForJava

	public Date convertStrToDate(String dateIn) throws Exception {

		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

		Date dateOut = null;

		try {

			dateOut = format.parse(dateIn);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return dateOut;
	}

	public java.sql.Date formatDateForDB(String dateStr) {

		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		Date dateForDB = null;
		DateTime date_SQL_format = null;

		try {

			dateForDB = format.parse(dateStr);

			date_SQL_format = new DateTime(dateForDB);

		} catch (Exception e) {
			e.printStackTrace();
		}

		DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
		String dateInString = date_SQL_format.toString(fmt);

		java.sql.Date newDate = java.sql.Date.valueOf(dateInString);

		return newDate;

	} // formatDateForDB

	public void setAvailibilityFlag(GetQuote getQuote, KioskQuery kQuery) {

		System.out.println("availability code = " + kQuery.getAvailability_code());

		switch (kQuery.getAvailability_code()) {

		case Constants.avail_c:
			getQuote.setAvailability_status(Constants.avail);
			break;

		case Constants.lock_avail_c:
			getQuote.setAvailability_status(Constants.lock_avail);
			break;

		case Constants.not_avail_c:
			getQuote.setAvailability_status(Constants.not_avail);
			break;

		case Constants.part_avail_c:
			getQuote.setAvailability_status(Constants.part_avail);
			break;

		case Constants.void_avail_c:
			getQuote.setAvailability_status(Constants.void_avail);
			break;

		} // case

	} // setAvailabilityFlag

	public boolean isQuoteExpired(String dateOfQuote, int expiry_duration_days) {

		// first we convert the String dateOfQuote into a DateTime object
		DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy");
		DateTime quoteDate = formatter.parseDateTime(dateOfQuote);

		// now we have to add the expiry duration to quote date to obtain the
		// expiry date
		DateTime expiryDate = new DateTime();
		expiryDate = quoteDate.plusDays(expiry_duration_days);

		DateTime currentDate = new DateTime();

		// System.out.println("quote date = " + quoteDate + ", expiry date = " +
		// expiryDate + ", current date = " + currentDate);

		// now check to see if the quote date if after the expiry date

		return currentDate.isAfter(expiryDate);

	} // isQuoteExpired

	public boolean isQuoteOutOfDate(String startDate) {

		// first we convert the String dateOfQuote into a DateTime object
		DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy");
		DateTime start_date = formatter.parseDateTime(startDate);

		DateTime currentDate = new DateTime();

		// System.out.println("start date = " + start_date + ", current date = " +
		// currentDate);

		return currentDate.isAfter(start_date);
	} // isQuoteOutOfDate

	public boolean isApplicationExpired(String dateOfApplication, int expiry_duration_days) {

		// first we convert the String dateOfQuote into a DateTime object
		DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy");
		DateTime applicationDate = formatter.parseDateTime(dateOfApplication);

		// now we have to add the expiry duration to quote date to obtain the
		// expiry date
		DateTime expiryDate = new DateTime();
		expiryDate = applicationDate.plusDays(expiry_duration_days);

		DateTime currentDate = new DateTime();

		System.out.println("application date = " + applicationDate + ", expiry date = " + expiryDate
				+ ", current date = " + currentDate);

		// now check to see if the application date if after the expiry date

		return currentDate.isAfter(expiryDate);
	} // isApplicationExpired

	public void slimDownJSON(GetQuote q) {

		q.setIsExpired(0);
		q.getKioskQuery().setKioskID(0);
		q.getKioskQuery().setGridLocationColumn(0);
		q.getKioskQuery().setGridLocationRow(0);
		q.getKioskQuery().setZoneID(0);
		q.getKioskQuery().getZone().setLocationID(0);
		q.getKioskQuery().getZone().setZoneID(0);
		q.getKioskQuery().setDaily_rate(0);
		q.getKioskQuery().setLease_total(0);
		q.getKioskQuery().setLease_duration(0);
		q.getKioskQuery().setNumber_days_available(0);
		q.getKioskQuery().setDateList(null);

	}

	public void slimDownJSON(AddQuote q) {

		q.getQ_kiosk().setKioskID(0);
		q.getQ_kiosk().setGridLocationColumn(0);
		q.getQ_kiosk().setGridLocationRow(0);
		q.getQ_kiosk().setZoneID(0);
		q.getQ_kiosk().getZone().setLocationID(0);
		q.getQ_kiosk().getZone().setZoneID(0);
		q.getQ_kiosk().setDaily_rate(0);
		q.getQ_kiosk().setLease_total(0);
		q.getQ_kiosk().setLease_duration(0);
		q.getQ_kiosk().setNumber_days_available(0);
		q.getQ_kiosk().setDateList(null);
	}

	public void slimDownJSON(List<User> m) {

		for (int i = 0; i < m.size(); i++) {
			m.get(i).setPassword(null);
			m.get(i).setPasswordHint(null);
			m.get(i).setIsblocked(0);
			m.get(i).setAddress(null);
			m.get(i).setDob(null);

		}
	}

	public void reviseApplication(double new_lease_total, Quote qOld) {

		if (new_lease_total == 0) {

		} else {

			if (new_lease_total < 1.0) {

			} else {

				// we set the revised price to the application
				qOld.setLease_total(new_lease_total);

				// new rate
				// rate = total / duration
				double rate = new_lease_total / qOld.getLease_duration_days();

				// we have to make changes to the rate too
				qOld.setRate(rate);
			} // nested if
		} // outer if

	} // reviseApplication

	public QuotationForm generateQuoteRevisedEmail(Quote quote, User staff_details, List<Company> companyList,
			KioskQuery k_query, Customer customer) {

		QuotationForm qForm = new QuotationForm();

		// we need to set the current date - the date of the quote
		quote.setDate_of_quote(new Date());

		String dateOfQuoteStr = new SimpleDateFormat("dd/MM/yyyy").format(quote.getDate_of_quote());

		String dateOfQuote = dateOfQuoteStr;
		// we now check if the kiosk is still available for the given dates

		quote.setQuote_date(dateOfQuote);

		qForm.loadValues(quote, staff_details, k_query, customer, companyList);
		return qForm;

	} // generateQuoteRevisedEmail

	public boolean isBookingExpired(String endDate) {

		// first we convert the String endDate into a DateTime object
		DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy");
		DateTime leaseExpiryDate = formatter.parseDateTime(endDate);

		DateTime currentDate = new DateTime();

		// System.out.println("lease expiry date = " + leaseExpiryDate + ", current date
		// = " + currentDate);

		// now check to see if the quote date if after the expiry date

		return currentDate.isAfter(leaseExpiryDate);
	} // isBookingExpired

	public boolean isBookingNearlyExpired(String endDate, int expiry_due_days) {

		// first we convert the String endDate into a DateTime object
		DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy");
		DateTime leaseExpiryDate = formatter.parseDateTime(endDate);
		DateTime currentDate = new DateTime();

		int daysRemaining = 0;

		// System.out.println("lease expiry date = " + leaseExpiryDate.toDate() + ",
		// current date = " + currentDate.toDate());

		daysRemaining = Days.daysBetween(currentDate.withTimeAtStartOfDay(), leaseExpiryDate.withTimeAtStartOfDay())
				.getDays();

		System.out.println("days remaining = " + daysRemaining);

		if (daysRemaining <= expiry_due_days && daysRemaining > 0)
			return true;

		return false;

	} // isBookingNearlyExpired

	public boolean isPaymentOverdue(String dueByDate) {
		// first we convert the String endDate into a DateTime object
		DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy");
		DateTime paymentDueDate = formatter.parseDateTime(dueByDate);

		DateTime currentDate = new DateTime();

		// System.out.println("lease expiry date = " + leaseExpiryDate + ", current date
		// = " + currentDate);

		// now check to see if the quote date if after the expiry date

		return currentDate.isAfter(paymentDueDate);
	} // isPaymentOverdue

	private int generateRandomIntRange(int min, int max) {
		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}

	public String generateZoneColourName() {

		String[] colourArray = new String[25];

		colourArray[0] = "Red";
		colourArray[1] = "Green";
		colourArray[2] = "Yellow";
		colourArray[3] = "Blue";
		colourArray[4] = "Orange";
		colourArray[5] = "Purple";
		colourArray[6] = "Cyan";
		colourArray[7] = "Magenta";
		colourArray[8] = "Green";
		colourArray[9] = "Lime";
		colourArray[10] = "Pink";
		colourArray[11] = "Teal";
		colourArray[12] = "Lavender";
		colourArray[13] = "Brown";
		colourArray[14] = "Beige";
		colourArray[15] = "Maroon";
		colourArray[16] = "Mint";
		colourArray[17] = "Olive";
		colourArray[18] = "Apricot";
		colourArray[19] = "Navy";
		colourArray[20] = "Silver";
		colourArray[21] = "Gold";
		colourArray[22] = "Bronze";
		colourArray[23] = "Platinum";
		colourArray[24] = "Copper";

		int i = generateRandomIntRange(1, 25);
		String zoneColour = colourArray[i - 1] + " Zone";
		return zoneColour;

	} // generateZoneColourName

	public List<GridBuilder> buildGridQuery(CreateLocation getLoc) {

		List<GridBuilder> outputGrid = new ArrayList<>();

		for (int i = 0; i < getLoc.getKioskList().size(); i++) {
			GridBuilder gB = new GridBuilder();

			gB.setLocationName(getLoc.getLocationName());
			gB.setLocationArea(getLoc.getLocationArea());
			gB.setLocationMap(getLoc.getMapURL());

			for (int j = 0; j < getLoc.getZoneList().size(); j++) {

				if (getLoc.getKioskList().get(i).getZoneID() == getLoc.getZoneList().get(j).getZoneID()) {
					gB.setZoneNumber(getLoc.getZoneList().get(j).getZoneNumber());
					gB.setZoneName(getLoc.getZoneList().get(j).getZoneName());
					gB.setZoneColour(getLoc.getZoneList().get(j).getZoneColour());
					gB.setZoneID(getLoc.getZoneList().get(j).getZoneID());
				}

			} // nested for

			gB.setKioskID(getLoc.getKioskList().get(i).getKioskID());
			gB.setKioskNumber(getLoc.getKioskList().get(i).getKioskNumber());
			gB.setGridLocationRow(getLoc.getKioskList().get(i).getGridLocationRow());
			gB.setGridLocationColumn(getLoc.getKioskList().get(i).getGridLocationColumn());

			// set the availability flag and colour

			// default - available
			if (getLoc.getKioskList().get(i).getIsLocked() == 0 && getLoc.getKioskList().get(i).getIsVoid() == 0) {
				gB.setAvailability_code(Constants.avail_c);
				gB.setAvailability_color(Constants.avail_colour);
			}

			// locked
			if (getLoc.getKioskList().get(i).getIsLocked() == 1) {
				gB.setAvailability_code(Constants.lock_avail_c);
				gB.setAvailability_color(Constants.lock_avail_colour);
			}

			// void
			if (getLoc.getKioskList().get(i).getIsVoid() == 1) {
				gB.setAvailability_code(Constants.void_avail_c);
				gB.setAvailability_color(Constants.void_avail_colour);
			}

			outputGrid.add(gB);

		} // for

		return outputGrid;
	} // buildGridQuery

}