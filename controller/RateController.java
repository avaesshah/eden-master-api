package com.eden.api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.eden.api.service.EngineService;
import com.eden.api.service.LocationService;
import com.eden.api.service.validation.ZoneValidation;
import com.eden.api.util.Constants;
import com.eden.api.util.Response;
import com.eden.api.util.ResponseEnum;
import com.edenstar.model.Discount;
import com.edenstar.model.Rate;
import com.edenstar.model.Zone;
import com.edenstar.model.dash.CreateZone;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class RateController extends BaseController {

	Response response = new Response();

	ZoneValidation zoneValid = new ZoneValidation();

	@Autowired
	private LocationService locationService;

	@Autowired
	private EngineService cpu;

	private boolean rateExists(CreateZone z) {
		Rate r = new Rate();
		boolean r_exists = true;

		try {

			r = locationService.getRateByRateID(z.getRate().getRateID());

			// if the record does not exist we should return false
			if (r.getRateID() == 0)
				return false;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// if the rate does exist, we can assign the zone_id to it
		z.setZoneID(r.getZoneID());

		return r_exists;
	} // RateExists

	// **************************************************************************************
	// get all rate information for given rate_id
	// **************************************************************************************
	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_DASH
			+ Constants.PATH_GET_RATE, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response getRate(@RequestBody CreateZone z) {

		Rate rate = new Rate();
		Zone zone = new Zone();

		try {

			// make sure that the staff_email and rateID are not null
			if (z.getStaff_email_id() == null || z.getRate().getRateID() == 0) {
				response = Response.build("Error", "Mandatory field staff_email / rateID cannot be null", false);
				return response;
			} // null fields

			// check to see if the fields are empty
			if (z.getStaff_email_id().contentEquals("") || z.getRate().getRateID() == 0) {
				response = Response.build("Error", "Mandatory field : staff_email / rateID cannot be empty", false);
				return response;
			} // empty fields

			// check to see if the staff user exists on the database
			if (!cpu.userExists(z.getStaff_email_id())) {
				response = Response.build("Error",
						"Staff email " + z.getStaff_email_id() + " does not exist on the database", false);
				return response;
			} // staff checks

			// check to see if the rateID exists
			if (!rateExists(z)) {
				response = Response.build("Error",
						"Rate ID " + z.getRate().getRateID() + " does not exist on the database", false);
				return response;
			} // rate checks

			// the zone_id is copied over from the database if the rate_id exists (by the
			// rateExists method)

			// get zone information
			zone = locationService.getZone(z.getZoneID());
			z.setZoneName(zone.getZoneName());
			z.setZoneNumber(zone.getZoneNumber());
			z.setLocationID(zone.getLocationID());

			// gets the corresponding rate information relating to the zone id
			rate = locationService.getRate(z.getZoneID());

			z.setRate(rate);

			// kiosk information has been diabled for this function because it is not
			// relevant here
//			// get all kiosks relating to a location
//			kioskList = locationService.getKiosk(z.getZoneID());
//			z.setKioskList(kioskList);

			z.getRateAndDiscount().setRateID(rate.getRateID());
			z.getRateAndDiscount().setStandardRate(rate.getStandardRate());
			if (rate.getStandardRate() == 0)
				z.getRateAndDiscount().setStandardRate(rate.getRateMax());
			z.getRateAndDiscount().setZoneID(rate.getZoneID());
			z.setRate(null);

			// we will check is the rate has an associated discount scheme, and if so add it
			List<Discount> discountList = locationService.getDiscountByRateID(rate.getRateID());
			if (!discountList.isEmpty()) {

				z.getRateAndDiscount().setDiscountList(discountList);

				// z.setDiscountList(discountList);
			}

			response = Response.build("Success",
					"Rate ID " + z.getRateAndDiscount().getRateID() + " successfully retrieved from the database",
					true);

			response.setData(z);

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // getRate

	// **************************************************************************************
	// update rate information to the database
	// **************************************************************************************
	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_DASH
			+ Constants.PATH_UPDATE_RATE, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response updateRate(@RequestBody CreateZone z) {

		try {
			
			// make sure that the staff_email and rateID are not null
			if (z.getStaff_email_id() == null || z.getRate().getRateID() == 0) {
				response = Response.build("Error", "Mandatory field staff_email_id / rateID cannot be null", false);
				return response;
			} // null fields

			// check to see if the fields are empty
			if (z.getStaff_email_id().contentEquals("") || z.getRate().getRateID() == 0) {
				response = Response.build("Error", "Mandatory field : staff_email_id / rateID cannot be empty", false);
				return response;
			} // empty fields

			// check to see if the staff user exists on the database
			if (!cpu.userExists(z.getStaff_email_id())) {
				response = Response.build("Error",
						"Staff email " + z.getStaff_email_id() + " does not exist on the database", false);
				return response;
			} // staff checks

			// check if the staff user has clearance to perform this action
			if (!cpu.checkClearance(z.getStaff_email_id().toString().toLowerCase())) {

				// insufficient priviledges
				response = Response.build("Error",
						"Insufficient privileges - you must be either be admin or manager_admin to perform this action",
						false);
				return response;

			} // clearance check

			// check to see if the rateID exists
			if (!rateExists(z)) {
				response = Response.build("Error", "Zone ID " + z.getZoneID() + " does not exist on the database",
						false);
				return response;
			} // zone checks

			// we need compare and check the rate information provided against the database
			// values
			// and copy database stored values if CreateZone z does not contain them
			copyRateValues(z);

			// we now have the rate values, we have to validate them
			// we need to check that the rate has all fields within a valid range
			if (!zoneValid.validateRates(z)) {
				response = Response.build("Error", "Rate data entered not with valid range", false);
				return response;
			} // validateRates

			// once the rate values have been validated, we can commit the changes to the
			// database
			int status = locationService.updateRate(z);

			if (status == 1) {
				response = Response.build("Success", "Rate details successfully updated to the database", true);
			} else {
				response = Response.build("Error", "Rate details could not be updated to the database", false);
			} // nested if

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // updateZone

	private void copyRateValues(CreateZone z) {

		// we first need to obtain the rate values over from the
		// database
		Rate r = new Rate();
		try {

			// now that we have the rate from the database
			r = locationService.getRateByRateID(z.getRate().getRateID());

			// we check the three variables rate_max, rate_min and duration to see if
			// there are any changes ... if not copy over the database value

			if (z.getRate().getRateMax() == 0)
				z.getRate().setRateMax(r.getRateMax());

			if (z.getRate().getRateMin() == 0)
				z.getRate().setRateMin(r.getRateMin());

			if (z.getRate().getDiscountDurationDays() == 0)
				z.getRate().setDiscountDurationDays(r.getDiscountDurationDays());

			if (z.getRate().getStandardRate() == 0)
				z.getRate().setStandardRate(r.getStandardRate());

			// we need to copy over the non-changable values such as zone_id
			z.getRate().setZoneID(r.getZoneID());

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	} // copyRateValues

	// **************************************************************************************
	// ADD A DISCOUNT LIST TO A RATE
	// **************************************************************************************
	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_DASH
			+ Constants.PATH_ADD_DISCOUNT, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response addDiscountToRate(@RequestBody CreateZone z) {
		try {

			z.getRate().setRateID(z.getRateAndDiscount().getRateID());

			// make sure that the staff_email and rateID are not null
			if (z.getStaff_email_id() == null || z.getRate().getRateID() == 0) {
				response = Response.build("Error", "Mandatory field staff_email_id / rateID cannot be null", false);
				return response;
			} // null fields

			// check to see if the fields are empty
			if (z.getStaff_email_id().contentEquals("") || z.getRate().getRateID() == 0) {
				response = Response.build("Error", "Mandatory field : staff_email_id / rateID cannot be empty", false);
				return response;
			} // empty fields

			// check to see if the staff user exists on the database
			if (!cpu.userExists(z.getStaff_email_id())) {
				response = Response.build("Error",
						"Staff email " + z.getStaff_email_id() + " does not exist on the database", false);
				return response;
			} // staff checks

			// check to see if the rateID exists
			if (!rateExists(z)) {
				response = Response.build("Error",
						"Rate ID " + z.getRate().getRateID() + " does not exist on the database", false);
				return response;
			} // rate checks

			// we should make sure that a discount list does not exist against the rate id
			List<Discount> discountLst = locationService.getDiscountByRateID(z.getRateAndDiscount().getRateID());
			if (!discountLst.isEmpty()) {
				response = Response.build("Error",
						"Could not add discount; rate ID " + z.getRate().getRateID() + " already has a discount list", false);
				return response;
			} // rate checks

			// make sure the discountList is not null
			// check to see if the rateID exists
			if (z.getRateAndDiscount().getDiscountList().isEmpty()) {
				response = Response.build("Error", "discountList is empty", false);
				return response;
			} // rate checks

			// if the discountList is attached, we will validate that too
			if (!z.getRateAndDiscount().getDiscountList().isEmpty()) {
				if (!zoneValid.validateDiscount(z)) {
					response = Response.build("Error", "The discount values are not within a valid range", false);
					return response;
				} // validateDiscount
			}

			// if the discountList is attached, we will validate that too
			if (!z.getRateAndDiscount().getDiscountList().isEmpty()) {
				if (!zoneValid.validateDiscountMonths(z)) {
					response = Response.build("Error",
							"Values given for monthsTo and monthsFrom are not within a valid range or are overlapping",
							false);
					return response;
				} // validateDiscountMonths
			}

			// add the discount list
			int rate_id = z.getRateAndDiscount().getRateID();
			int count = 0;
			for (int k = 0; k < z.getRateAndDiscount().getDiscountList().size(); k++) {
				count++;
				// we write the discount values to Discount table using rate_id
				int discount_id = locationService.addDiscountByZone(z, k);

				if (discount_id == 0) {
					response = Response.build("Error", "Discount could not be added to the database", false);
					return response;
				} else {
					// we assign the new generated disount id
					z.getRateAndDiscount().getDiscountList().get(k).setDiscount_id(discount_id);
					z.getRateAndDiscount().getDiscountList().get(k).setRate_id(rate_id);
				}

			} // for loop until discount list size
			
			response = Response.build("Success", count + " discounts successfully added against rate ID " + rate_id, true);
			response.setData(z);

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // add discount to rate
	
	// **************************************************************************************
	// DELETE A DISCOUNT LIST FROM A RATE
	// **************************************************************************************
	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_DASH
			+ Constants.PATH_DELETE_DISCOUNT, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response deleteDiscountFromRate(@RequestBody CreateZone z) {
		try {

			z.getRate().setRateID(z.getRateAndDiscount().getRateID());

			// make sure that the staff_email and rateID are not null
			if (z.getStaff_email_id() == null || z.getRate().getRateID() == 0) {
				response = Response.build("Error", "Mandatory field staff_email_id / rateID cannot be null", false);
				return response;
			} // null fields

			// check to see if the fields are empty
			if (z.getStaff_email_id().contentEquals("") || z.getRate().getRateID() == 0) {
				response = Response.build("Error", "Mandatory field : staff_email_id / rateID cannot be empty", false);
				return response;
			} // empty fields

			// check to see if the staff user exists on the database
			if (!cpu.userExists(z.getStaff_email_id())) {
				response = Response.build("Error",
						"Staff email " + z.getStaff_email_id() + " does not exist on the database", false);
				return response;
			} // staff checks

			// check to see if the rateID exists
			if (!rateExists(z)) {
				response = Response.build("Error",
						"Rate ID " + z.getRate().getRateID() + " does not exist on the database", false);
				return response;
			} // rate checks

			// we should make sure that a discount list does not exist against the rate id
			List<Discount> discountLst = locationService.getDiscountByRateID(z.getRateAndDiscount().getRateID());
			if (discountLst.isEmpty()) {
				response = Response.build("Error",
						"Could not find a discount list registered against rate ID " + z.getRate().getRateID(), false);
				return response;
			} // discount list check

			// delete procedures
			int status = locationService.deleteDiscount(z.getRateAndDiscount().getRateID());
			
			if (status == 0) {
				response = Response.build("Error",
						"Could not delete discount with rate ID = " + z.getRate().getRateID(), false);
				return response;
			}
			
			response = Response.build("Success", "Discount list successfully deleted from rate of ID number " + z.getRateAndDiscount().getRateID(), true);

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // add discount to rate

} // RateController
