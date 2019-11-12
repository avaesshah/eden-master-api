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

import com.eden.api.service.BookingService;
import com.eden.api.service.EngineService;
import com.eden.api.service.LocationService;
import com.eden.api.service.validation.ZoneValidation;
import com.eden.api.util.Constants;
import com.eden.api.util.Response;
import com.eden.api.util.ResponseEnum;
import com.edenstar.model.Calendar;
import com.edenstar.model.Discount;
import com.edenstar.model.Kiosk;
import com.edenstar.model.Rate;
import com.edenstar.model.Zone;
import com.edenstar.model.dash.CreateKiosk;
import com.edenstar.model.dash.CreateZone;


@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class KioskController extends BaseController {

	Response response = new Response();

	ZoneValidation zoneValid = new ZoneValidation();

	@Autowired
	private LocationService locationService;
	
	@Autowired
	private BookingService bookingService;

	@Autowired
	private EngineService cpu;

	private boolean zoneExists(int zone_id) {

		Zone z = new Zone();
		boolean c_exists = true;

		try {

			z = locationService.getZone(zone_id);

			// if the record does not exist we should return false
			if (z.getZoneNumber() == 0)
				return false;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return c_exists;

	} // zoneExists

	private void copyKiosk(CreateKiosk k) {

		// we first need to obtain the kiosk over from the
		// database then compare values
		Kiosk kiosk = new Kiosk();

		try {

			// now we obtain the kiosk from the database
			kiosk = locationService.getKioskByID(k.getKioskID());

			if (k.getLockKiosk().contentEquals("")) {
				k.setIsLocked(kiosk.getIsLocked());
			} else {
				k.setIsLocked(Integer.parseInt(k.getLockKiosk()));
			}

			if (k.getVoidKiosk().contentEquals("")) {
				k.setIsVoid(kiosk.getIsVoid());
			} else {
				k.setIsVoid(Integer.parseInt(k.getVoidKiosk()));
			}

			k.setGridLocationRow(kiosk.getGridLocationRow());
			k.setGridLocationColumn(kiosk.getGridLocationColumn());
			k.setZoneID(kiosk.getZoneID());

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	} // copyKiosk

	// **************************************************************************************
	// get all kiosk information for given kiosk_id
	// **************************************************************************************
	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_DASH
			+ Constants.PATH_GET_KIOSK, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response getKiosk(@RequestBody CreateKiosk k) {

		Kiosk kiosk = new Kiosk();
		Rate rate = new Rate();
		Zone zone = new Zone();

		try {

			// make sure that the staff_email and rateID are not null
			if (k.getStaff_email_id() == null || k.getKioskID() == 0) {
				response = Response.build("Error", "Mandatory field staff_email / kioskID cannot be null", false);
				return response;
			} // null fields

			// check to see if the fields are empty
			if (k.getStaff_email_id().contentEquals("") || k.getKioskID() == 0) {
				response = Response.build("Error", "Mandatory field : staff_email / kioskID cannot be empty", false);
				return response;
			} // empty fields

			// check to see if the staff user exists on the database
			if (!cpu.userExists(k.getStaff_email_id())) {
				response = Response.build("Error",
						"Staff email " + k.getStaff_email_id() + " does not exist on the database", false);
				return response;
			} // staff checks

			// check to see if the kioskID exists
			if (!cpu.kioskExists(k.getKioskID())) {
				response = Response.build("Error", "Kiosk ID " + k.getKioskID() + " does not exist on the database",
						false);
				return response;
			} // kiosk checks

			// get the kiosk information for kiosk_id
			kiosk = locationService.getKioskByID(k.getKioskID());

			// get the rate information for the kiosk's zone
			rate = locationService.getRate(kiosk.getZoneID());

			// get the zone information
			zone = locationService.getZone(kiosk.getZoneID());

			// we need to wrap the information into an object and sent it via JSON,
			// the CreateZone is the best way
			CreateZone z = new CreateZone(zone.getZoneID(), zone.getZoneNumber(), zone.getZoneName(),
					zone.getLocationID(), null);
			//z.setRate(rate);
			
			z.getRateAndDiscount().setRateID(rate.getRateID());
			z.getRateAndDiscount().setStandardRate(rate.getRateMax());
			z.getRateAndDiscount().setZoneID(rate.getZoneID());
			z.getKioskList().add(kiosk);
			z.setStaff_email_id(k.getStaff_email_id());
			
			String kioskStatus = "Active";
			if (kiosk.getIsVoid() == 1) kioskStatus = kioskStatus + "Void";
			if (kiosk.getIsLocked() == 1) kioskStatus = kioskStatus + "Locked";
			
			StringBuilder discountStatus = new StringBuilder();//" [ NO DISCOUNT REGISTERED ] ";
			List<Discount> discountList = locationService.getDiscountByRateID(rate.getRateID());
			if (!discountList.isEmpty()) {
				z.getRateAndDiscount().setDiscountList(discountList);
				discountStatus.append(" [ Discount Table ] \n");
				discountStatus.append(" ================== \n");
				for (int i = 0; i < discountList.size(); i++) {
					discountStatus.append(discountList.get(i).getPercentageDiscount() + " % from month " + discountList.get(i).getFromMonth() + 
							" to month " + discountList.get(i).getToMonth() + "\n");
				}
				
				z.setDiscountStatus(discountStatus.toString());
				
			}
			
			List<Calendar> kioskCalendarList = bookingService.getBookingsForKiosk(k.getKioskID());
			if (!kioskCalendarList.isEmpty()) {
				z.setBookingCalendar(kioskCalendarList);
				//kioskStatus = kioskStatus + " [ " + kioskCalendarList.size() + " ACTIVE BOOKINGS ] ";
			} else {
				//kioskStatus = kioskStatus + " [ " + kioskCalendarList.size() + " ACTIVE BOOKINGS ] ";
			}
			
			z.setKioskStatus(kioskStatus);

			response = Response.build("Success",
					"Kiosk ID " + k.getKioskID() + " successfully retrieved from the database", true);

			response.setData(z);

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // getKiosk

	// **************************************************************************************
	// add a kiosk to existing zone
	// **************************************************************************************
	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_DASH
			+ Constants.PATH_ADD_KIOSK, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response addZone(@RequestBody CreateKiosk k) {

		try {

			// make sure that the staff_email and zoneID are not null
			if (k.getStaff_email_id() == null || k.getZoneID() == 0) {
				response = Response.build("Error", "Mandatory field staff_email / zoneID cannot be null", false);
				return response;
			} // null fields

			// check to see if the fields are empty
			if (k.getStaff_email_id().contentEquals("") || k.getZoneID() == 0) {
				response = Response.build("Error", "Mandatory field : staff_email / zoneID cannot be empty", false);
				return response;
			} // empty fields

			// check to see if the staff user exists on the database
			if (!cpu.userExists(k.getStaff_email_id())) {
				response = Response.build("Error", "staff email id " + k.getStaff_email_id() + " does not exist",
						false);
				return response;
			} // userExists

			// check if the staff user has clearance to perform this action
			if (cpu.checkClearance(k.getStaff_email_id().toLowerCase()) == false) {

				// insufficient priviledges
				response = Response.build("Access Denied",
						"insufficient privileges - you must be either be admin or manager_admin to perform this action",
						false);
				return response;

			} // clearance check

			// check to see if the locationID exists
			if (!zoneExists(k.getZoneID())) {

				response = Response.build("Error", "Zone id" + k.getZoneID() + " does not exist on the database",
						false);
				return response;

			} // ZoneExists

			// once all the validations have been completed we can go ahead and add the
			// kiosk
			// but first we need to obtain the locationID

			Zone zone = new Zone();
			zone = locationService.getZone(k.getZoneID());

			// we now assign the location id to k
			k.setLocation_id(zone.getLocationID());

			// we need to find out the maxium rows and columns for the given location_id
			k.setMax_rows(locationService.getMaxRowsOrColumns(zone.getLocationID(), "rows"));
			k.setMax_columns(locationService.getMaxRowsOrColumns(zone.getLocationID(), "columns"));

			// we need to find out how many kiosks there are and assign the next kiosk
			// number
			k.setKioskNumber(locationService.getKioskCount(zone.getLocationID()) + 1);

			// once we have the kiosk number set, we need to find the next row and column
			// available
			// and assign it to the kiosk
			k = locationService.getNextRowAndCol(k);

			// we need to set the void and locked marker to zero by default
			k.setIsLocked(0);
			k.setIsVoid(0);

			// now that all required information has been generated, we can go ahead and add
			// a kiosk and obtain generated id
			int kiosk_id = locationService.addKiosk(k);

			if (kiosk_id > 0) {
				Kiosk newKiosk = new Kiosk();
				newKiosk = locationService.getKioskByID(kiosk_id);
				response = Response.build("Success", "New kiosk has been added to the database successfully", true);
				response.setData(newKiosk);
			} else {
				response = Response.build("Error", "New kiosk could not be added to the database", false);
				return response;
			}

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // add a kiosk

	// **************************************************************************************
	// DELETE a kosk
	// **************************************************************************************
	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_DASH
			+ Constants.PATH_DELETE_KIOSK, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response deleteKiosk(@RequestBody CreateKiosk k) {

		try {

			// make sure that the staff_email and kioskID are not null
			if (k.getStaff_email_id() == null || k.getKioskID() == 0) {
				response = Response.build("Error", "Mandatory field staff_email / kioskID cannot be null", false);
				return response;
			} // null fields

			// check to see if the fields are empty
			if (k.getStaff_email_id().contentEquals("") || k.getKioskID() == 0) {
				response = Response.build("Error", "Mandatory field : staff_email / kioskID cannot be empty", false);
				return response;
			} // empty fields

			// check to see if the staff user exists on the database
			if (!cpu.userExists(k.getStaff_email_id())) {
				response = Response.build("Error", "staff email id " + k.getStaff_email_id() + " does not exist",
						false);
				return response;
			} // userExists

			// check if the staff user has clearance to perform this action
			if (cpu.checkClearance(k.getStaff_email_id().toLowerCase()) == false) {

				// insufficient priviledges
				response = Response.build("Access Denied",
						"insufficient privileges - you must be either be admin or manager_admin to perform this action",
						false);
				return response;

			} // clearance check

			// check to see if the kioskID exists
			if (!cpu.kioskExists(k.getKioskID())) {

				response = Response.build("Error", "Kiosk of ID " + k.getKioskID() + " does not exist on the database",
						false);
				return response;

			} // KioskExists

			// after all validation checks we can now delete the kiosk
			int status = locationService.deleteKiosk(k.getKioskID());

			if (status == 1) {
				response = Response.build("Success", "kiosk has been successfully deleted from the database", true);

			} else {
				response = Response.build("Failure",
						"Kiosk of id " + k.getKioskID() + " could not be deleted from the database", false);

			} // nested if

		} catch (

		Exception e) {

			e.printStackTrace();
		} // try-catch
		return response;

	} // deleteKiosk

	// **************************************************************************************
	// update kiosk information to the database
	// **************************************************************************************
	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_DASH
			+ Constants.PATH_UPDATE_KIOSK, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response updateKiosk(@RequestBody CreateKiosk k) {

		try {

			// make sure that the staff_email and kioskID are not null
			if (k.getStaff_email_id() == null || k.getKioskID() == 0) {
				response = Response.build("Error", "Mandatory field staff_email_id / kioskID cannot be null", false);
				return response;
			} // null fields

			// check to see if the fields are empty
			if (k.getStaff_email_id().contentEquals("") || k.getKioskID() == 0) {
				response = Response.build("Error", "Mandatory field : staff_email_id / kioskID cannot be empty", false);
				return response;
			} // empty fields

			// check to see if the staff user exists on the database
			if (!cpu.userExists(k.getStaff_email_id())) {
				response = Response.build("Error", "staff email id " + k.getStaff_email_id() + " does not exist",
						false);
				return response;
			} // userExists

			// check if the staff user has clearance to perform this action
			if (cpu.checkClearance(k.getStaff_email_id().toLowerCase()) == false) {

				// insufficient priviledges
				response = Response.build("Access Denied",
						"insufficient privileges - you must be either be admin or manager_admin to perform this action",
						false);
				return response;

			} // clearance check

			if (!k.getVoidKiosk().contentEquals("")) {

				// we have to make sure that the values isVoid and isLocked are either 0 or 1
				if (k.getVoidKiosk().contentEquals("0") || k.getVoidKiosk().contentEquals("1")) {

				} else {

					response = Response.build("Error", "voidKiosk can only be 1 or 0", false);
					return response;
				} // isVoid check

			} // voidKiosk

			if (!k.getLockKiosk().contentEquals("")) {

				if (k.getLockKiosk().contentEquals("0") || k.getLockKiosk().contentEquals("1")) {

				} else {
					response = Response.build("Error", "lockKiosk can only be 1 or 0", false);
					return response;

				} // isLocked check

			} // lockKiosk

			// check to see if the kioskID exists
			if (!cpu.kioskExists(k.getKioskID())) {

				response = Response.build("Error", "Kiosk of ID " + k.getKioskID() + " does not exist on the database",
						false);
				return response;

			} // KioskExists

			// we need to copy over the zone information from the database
			// and update the new information
			copyKiosk(k);

			// now that all the check have been performed, we can go ahead and update zone
			// with new data
			int status = locationService.updateKiosk(k);

			if (status == 1) {
				Kiosk newKiosk = new Kiosk();
				newKiosk = locationService.getKioskByID(k.getKioskID());
				response = Response.build("Success", "New kiosk details have been updated successfully", true);
				response.setData(newKiosk);
			} else {
				response = Response.build("Error", "Kiosk details could not be updated to the database", false);
			} // nested if

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // updateKiosk
	
	
	// **************************************************************************************
	// assign zone to kiosk
	// **************************************************************************************
	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_DASH
			+ Constants.PATH_ASSIGN_ZONE, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response assignZone(@RequestBody CreateKiosk k) {

		try {

			// make sure that the staff_email and kioskID are not null
			if (k.getStaff_email_id() == null || k.getKioskID() == 0 || k.getZoneID() == 0) {
				response = Response.build("Error", "Mandatory field staff_email_id / kioskID / zoneID cannot be null", false);
				return response;
			} // null fields

			// check to see if the fields are empty
			if (k.getStaff_email_id().contentEquals("") || k.getKioskID() == 0 || k.getZoneID() == 0) {
				response = Response.build("Error", "Mandatory field : staff_email_id / kioskID / zoneID cannot be empty", false);
				return response;
			} // empty fields

			// check to see if the staff user exists on the database
			if (!cpu.userExists(k.getStaff_email_id())) {
				response = Response.build("Error", "staff email id " + k.getStaff_email_id() + " does not exist",
						false);
				return response;
			} // userExists

			// check if the staff user has clearance to perform this action
			if (cpu.checkClearance(k.getStaff_email_id().toLowerCase()) == false) {

				// insufficient priviledges
				response = Response.build("Access Denied",
						"insufficient privileges - you must be either be admin or manager_admin to perform this action",
						false);
				return response;

			} // clearance check

			// check to see if the kioskID exists
			if (!cpu.kioskExists(k.getKioskID())) {

				response = Response.build("Error", "Kiosk of ID " + k.getKioskID() + " does not exist on the database",
						false);
				return response;

			} // KioskExists
			
			// check to see if the zoneID exists
			if (!cpu.zoneExists(k.getZoneID())) {

				response = Response.build("Error", "Zone of ID " + k.getZoneID() + " does not exist on the database",
						false);
				return response;

			} // ZoneExists

			// we need to copy over the zone information from the database
			// and update the new information
			copyKioskZone(k);

			// now that all the check have been performed, we can go ahead and update zone
			// with new data
			int status = locationService.updateKiosk(k);

			if (status == 1) {
				Kiosk newKiosk = new Kiosk();
				newKiosk = locationService.getKioskByID(k.getKioskID());
				response = Response.build("Success", "Zone assigned successfully", true);
				response.setData(newKiosk);
			} else {
				response = Response.build("Error", "Zone assignment failed", false);
			} // nested if

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // assignZone
	
	private void copyKioskZone(CreateKiosk k) {

		// we first need to obtain the kiosk over from the
		// database then compare values
		Kiosk kiosk = new Kiosk();

		try {

			// now we obtain the kiosk from the database
			kiosk = locationService.getKioskByID(k.getKioskID());

			k.setIsLocked(kiosk.getIsLocked());
			k.setIsVoid(kiosk.getIsVoid());
			k.setGridLocationRow(kiosk.getGridLocationRow());
			k.setGridLocationColumn(kiosk.getGridLocationColumn());
			k.setKioskNumber(kiosk.getKioskNumber());

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	} // copyKioskZone
	

} // KioskController
