package com.eden.api.controller;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.eden.api.controller.engine.Availability;
import com.eden.api.service.EngineService;
import com.eden.api.service.LocationService;
import com.eden.api.service.validation.LocationValidation;
import com.eden.api.util.Constants;
import com.eden.api.util.Response;
import com.eden.api.util.ResponseEnum;
import com.edenstar.model.Discount;
import com.edenstar.model.Kiosk;
import com.edenstar.model.Location;
import com.edenstar.model.Rate;
import com.edenstar.model.RateModel;
import com.edenstar.model.Zone;
import com.edenstar.model.dash.CreateLocation;
import com.edenstar.model.dash.ProcessLocation;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class LocationController extends BaseController {

	Response response = new Response();

	LocationValidation locValid = new LocationValidation();

	@Autowired
	private LocationService locationService;

	@Autowired
	private EngineService cpu;

	// **************************************************************************************
	// add a location - generates kiosks, rates and zones for a location
	// **************************************************************************************
	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_DASH
			+ Constants.PATH_ADD_LOC, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response addLocation(@RequestBody CreateLocation l) {

		String error = "";
		Availability availability = new Availability();

		try {

			if (l.getRateDiscountList().isEmpty()) {
				response = Response.build("Error", "The rateDiscountList cannot be empty", false);
				return response;
			}

			// make sure none of the mandatory fields are null
			if (locValid.isNull(l)) {
				response = Response.build("Error",
						"Mandatory parameters : [staff_email / num_max_rows / num_max_columns / locationName / locationArea] cannot be null",
						false);
				return response;
			} // isNull check

			if (l.getLocationMap() == null) {
				l.setMapURL("");
			}

			// check to see if any of the mandatory fields are empty
			if (locValid.fieldsAreEmpty(l)) {
				response = Response.build("Error",
						"No data entered for mandatory parameters, either [staff_email / num_max_rows / num_max_columns / locationName / locationArea]",
						false);
				return response;
			} // isEmpty check

			// we need to check that the array containing the rates (rateList) has all
			// fields within a valid range
			if (!locValid.validateRates(l)) {
				response = Response.build("Error", "Rates are not within valid range", false);
				return response;
			} // validateRates

			// we need to check that the array containing the discount (rateDiscountList)
			// has all
			// fields within a valid range
			if (!locValid.validateDiscount(l)) {
				response = Response.build("Error", "Discount percentage or months are not within a valid range", false);
				return response;
			} // validateDiscount

			if (!locValid.validateDiscountMonths(l)) {
				response = Response.build("Error",
						"Values given for monthsTo and monthsFrom are not within a valid range or are overlapping",
						false);
				return response;
			} // validateDiscountMonths

			// check to see if the staff user exists on the database
			if (!cpu.userExists(l.getStaff_email_id())) {
				response = Response.build("Error", "Staff email id " + l.getStaff_email_id() + " does not exist",
						false);
				return response;
			} // userExists

			// check if the staff user has clearance to perform this action
			if (cpu.checkClearance(l.getStaff_email_id().toLowerCase()) == false) {

				// insufficient priviledges
				response = Response.build("Access Denied",
						"Insufficient privileges - you must be either be admin or manager_admin to perform this action",
						false);
				return response;

			} // clearance check

			if (cpu.locationExists(l.getLocationName().toLowerCase())) {
				// now we check if the location area name is the same
				// if so, don't allow a duplicate value
				Location r = locationService.getLocationByName(l.getLocationName().toLowerCase());
				if (r.getLocationArea().toLowerCase().contentEquals(l.getLocationArea().toLowerCase())) {

					System.out.println("you are now existing because of duplication ...");
					// the location already exists !
					response = Response.build("Failure", "Duplicate location and area", false);
					return response;
				}
			} // if

			// if it does not exist, create it !
			System.out.println(" Adding " + l.getLocationName() + " to database ... ");

			// if an image map of the location has been included we must add the url to the
			// database and save it on the server

			if (l.getLocationMap() == null) {
				l.setMapURL("");
			} else {
				String urlToMap = locValid.writeImageStreamToFile(l.getLocationMap(), l.getLocationName());
				System.out.println("the map url is = " + urlToMap);
				l.setMapURL(urlToMap);
			}

			// we now create a location with location name
			int location_id = locationService.addLocation(l);

			if (location_id > 0) {
				System.out.println("Location successfully created");
				l.setLocationID(location_id);

				// now that we have created a location, we must use the location_id to create
				// the zones corresponding rates for each zone ...
				// the ratesList contains the rate per zone, so we will create the number of
				// zones
				// exactly the same size as the rateList

				int i = 0;
				for (i = 0; i < l.getRateDiscountList().size(); i++) {

					System.out.println("zone number " + (i + 1) + " created for location id " + l.getLocationID());

					// create the zone
					Zone zone = new Zone();
					zone.setZoneNumber(i + 1);
					zone.setZoneName(availability.generateZoneColourName());
					zone.setLocationID(location_id);

					// add the zone to the ArrayList
					l.getZoneList().add(zone);

					// create zone ...
					int zone_id = locationService.addZone(l, i);

					// if the zone has been added correctly, a unique zone_id would be generated
					// which we shall use to create a corresponding
					// rate ...

					if (zone_id > 0) {

						// set the zone id to zoneList
						l.getZoneList().get(i).setZoneID(zone_id);

						// set the zone_id to rateList
						l.getRateList().get(i).setZoneID(zone_id);

						// create rate ...
						int rate_id = locationService.addRate(l, i);

						// now we check to see if the rate_id was successfully generated
						if (rate_id > 0) {

							// set the rate_id
							l.getRateList().get(i).setRateID(rate_id);
							l.getRateDiscountList().get(i).setRateID(rate_id);

							// now that the rate id has been successfully generated we add the corresponding
							// discount list
							// using the generated rate_id
							for (int k = 0; k < l.getRateDiscountList().get(i).getDiscountList().size(); k++) {

								// we write the discount values to Discount table using rate_id
								int discount_id = locationService.addDiscount(l, i, k);

								if (discount_id == 0) {
									error = error + " [discount_id for rate id " + rate_id
											+ " has not been generated.] ";
								} else {
									// we assign the new generated disount id
									l.getRateDiscountList().get(i).getDiscountList().get(k).setDiscount_id(discount_id);
								}

							} // discount list size

						} else {

							error = error + " [rate_id for zone number " + (i + 1) + " has not been generated.] ";

						} // if rate_id

					} else {

						error = error + " [zone_id for zone number " + (i + 1) + " has not been generated.] ";

					} // if zone_id

				} // for i = 0 to size of rateList

				// once the zones and corresponding rates have been generated, we now generate
				// the kiosks
				// for the location. By default all the kiosks are set to zone 1, rate 1 which
				// the admin
				// user can later adjust as they please

				int x, y;
				int kioskNumber = 0;
				for (x = 0; x < l.getNum_max_rows(); x++) {

					for (y = 0; y < l.getNum_max_columns(); y++) {

						// each kiosk has to be created, then added to the database
						Kiosk kiosk = new Kiosk();

						kioskNumber++;
						kiosk.setKioskNumber(kioskNumber);
						kiosk.setGridLocationRow(x + 1);
						kiosk.setGridLocationColumn(y + 1);
						kiosk.setIsLocked(0);
						kiosk.setIsVoid(0);
						kiosk.setZoneID(l.getZoneList().get(0).getZoneID()); // set all kiosks to zone 1 by default

						// we add the kiosk to the kioskList
						l.getKioskList().add(kiosk);

						// we now receive a generated key when we add the kiosk to the database
						// create rate ...
						int kiosk_id = locationService.addKiosk(l, kioskNumber - 1);

						// now we check to see if the rate_id was successfully generated
						if (kiosk_id > 0) {

							// set the kiosk_id
							l.getKioskList().get(kioskNumber - 1).setKioskID(kiosk_id);

						} else {

							error = error + " [kiosk_id for kiosk number " + kioskNumber + " has not been generated.] ";

						} // if rate_id

					} // for - y - columns

				} // for - x - rows

			} else {
				response = Response.build("Failure", "Location could not be added to the database", false);
				return response;
			} // nested if

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		if (error.contentEquals("")) {
			response = Response.build("Success", "Location, rates, zones and kiosks successfully generated on database",
					true);
			l.setLocationMap(null); // prevent the map being sent back in the JSON response
			l.setRateList(null);
			response.setData(l);
		} else {
			System.out.println(error);
			response = Response.build("Failure", error, false);
		}

		return response;

	} // addLocation

	// **************************************************************************************
	// get all location information for given location_id
	// **************************************************************************************
	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_DASH
			+ Constants.PATH_GET_LOCATION, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response getLocation(@RequestBody CreateLocation l) {

		List<Kiosk> kioskList = new ArrayList<>();
		List<Zone> zoneList = new ArrayList<>();
		List<Rate> rateList = new ArrayList<>();
		List<RateModel> rateDiscountList = new ArrayList<>();

		try {

			// make sure that the staff_email and locationID are not null
			if (l.getStaff_email_id() == null || l.getLocationID() == 0) {
				response = Response.build("Error", "Mandatory field staff_email_id / locationID cannot be null", false);
				return response;
			} // null fields

			// check to see if the fields are empty
			if (l.getStaff_email_id().contentEquals("") || l.getLocationID() == 0) {
				response = Response.build("Error", "Mandatory field : staff_email_id / locationID cannot be empty",
						false);
				return response;
			} // empty fields

			// check to see if the staff user exists on the database
			if (!cpu.userExists(l.getStaff_email_id())) {
				response = Response.build("Error",
						"Staff email " + l.getStaff_email_id() + " does not exist on the database", false);
				return response;
			} // staff checks

			// check to see if the locationID exists
			if (!cpu.locationExists(l.getLocationID())) {
				response = Response.build("Error",
						"Location ID " + l.getLocationID() + " does not exist on the database", false);
				return response;
			} // location checks

			// set the location name
			Location loc = new Location();
			loc = locationService.getLocation(l.getLocationID());
			l.setLocationName(loc.getLocationName());
			l.setLocationArea(loc.getLocationArea());
			l.setMapURL(loc.getMapURL());

			// get all the zones relating to the location
			zoneList = locationService.getZone(l);
			l.setZoneList(zoneList);

			// get all rates relating to the location
			rateList = locationService.getRate(l);
			// l.setRateList(rateList);

			// we need to go through the rateList and create a rate discount list
			for (int z = 0; z < rateList.size(); z++) {
				RateModel rateModel = new RateModel();
				List<Discount> discountList = locationService.getDiscountByRateID(rateList.get(z).getRateID());
				if (!discountList.isEmpty()) {
					rateModel.setDiscountList(discountList);
				}
				rateModel.setRateID(rateList.get(z).getRateID());
				rateModel.setStandardRate(rateList.get(z).getRateMax());
				rateModel.setZoneID(rateList.get(z).getZoneID());
				rateDiscountList.add(rateModel);
			}
			l.setRateDiscountList(rateDiscountList);

			// get all kiosks relating to a location
			kioskList = locationService.getKiosk(l);
			l.setKioskList(kioskList);

			// get the maximum rows for a given location by location_id
			int max_rows = locationService.getMaxRowsOrColumns(l.getLocationID(), "rows");
			l.setNum_max_rows(max_rows);

			// get the maximum columns for a given location by location_id
			int max_columns = locationService.getMaxRowsOrColumns(l.getLocationID(), "columns");
			l.setNum_max_columns(max_columns);

			response = Response.build("Success",
					"Location ID " + l.getLocationID() + " was successfully retrieved from the database", true);

			response.setData(l);

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // getLocation

	// **************************************************************************************
	// DELETE a location and all associated kiosks, rates and zones
	// **************************************************************************************
	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_DASH
			+ Constants.PATH_DELETE_LOC, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response deleteProductPhoto(@RequestBody CreateLocation l) {

		try {

			// make sure that the staff_email and locationID are not null
			if (l.getStaff_email_id() == null || l.getLocationID() == 0) {
				response = Response.build("Error", "Mandatory field staff_email / locationID cannot be null", false);
				return response;
			} // null fields

			// check to see if the fields are empty
			if (l.getStaff_email_id().contentEquals("") || l.getLocationID() == 0) {
				response = Response.build("Error", "Mandatory field : staff_email / locationID cannot be empty", false);
				return response;
			} // empty fields

			// check to see if the staff user exists on the database
			if (!cpu.userExists(l.getStaff_email_id())) {
				response = Response.build("Error",
						"Staff email " + l.getStaff_email_id() + " does not exist on the database", false);
				return response;
			} // staff checks

			// check to see if the locationID exists
			if (!cpu.locationExists(l.getLocationID())) {
				response = Response.build("Error",
						"Location ID " + l.getLocationID() + " does not exist on the database", false);
				return response;
			} // location checks

			// check if the staff user has clearance to perform this action
			if (cpu.checkClearance(l.getStaff_email_id().toString().toLowerCase()) == false) {

				// insufficient priviledges
				response = Response.build("Access Denied",
						"insufficient privileges - you must be either be admin or manager_admin to perform this action",
						false);
				return response;

			} // clearance check

			// we now have to check if the location exists on the database
			if (cpu.locationExists(l.getLocationID())) {

				// delete maps from image directory
				Location loc = new Location();
				loc = locationService.getLocation(l.getLocationID());
				locValid.deleteDirectory(loc.getLocationName());

				// delete procedures
				int status = locationService.deleteLocation(l.getLocationID());

				if (status == 1) {
					response = Response.build("Success",
							"location and associated kiosks, rates and zones deleted from the database", true);
					return response;

				} else {
					response = Response.build("Failure",
							"Location of id " + l.getLocationID() + " could not be deleted from the database", false);
					return response;

				} // nested if

			} else {
				// Location does not exist
				response = Response.build("Error",
						"Location ID " + l.getLocationID() + "does not exist on the database", false);
				return response;

			} // if

		} catch (

		Exception e) {

			e.printStackTrace();
		} // try-catch

		return response;

	} // deleteLocation

	// **************************************************************************************
	// update a location information to the database
	// **************************************************************************************
	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_DASH
			+ Constants.PATH_UPDATE_LOC, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response updateLocation(@RequestBody CreateLocation l) {

		try {

			// make sure that the staff_email and locationID are not null
			if (l.getStaff_email_id() == null || l.getLocationID() == 0 || l.getLocationMap() == null) {
				response = Response.build("Error",
						"Mandatory field staff_email_id / locationID / locationMap cannot be null", false);
				return response;
			} // null fields

			// check to see if the fields are empty
			if (l.getStaff_email_id().contentEquals("") || l.getLocationID() == 0) {
				response = Response.build("Error", "Mandatory field : staff_email_id / locationID cannot be empty",
						false);
				return response;
			} // empty fields

			// check to see if the staff user exists on the database
			if (!cpu.userExists(l.getStaff_email_id())) {
				response = Response.build("Error",
						"Staff email " + l.getStaff_email_id() + " does not exist on the database", false);
				return response;
			} // staff checks

			// check to see if the locationID exists
			if (!cpu.locationExists(l.getLocationID())) {
				response = Response.build("Error",
						"Location ID " + l.getLocationID() + " does not exist on the database", false);
				return response;
			} // location checks

			// we now need to download the information from the database compare it with the
			// new data

			Location loc = locationService.getLocation(l.getLocationID());
			if (l.getLocationMap() == null) {
				l.setMapURL("");
			} else {
				String urlToMap = locValid.writeImageStreamToFile(l.getLocationMap(), loc.getLocationName());
				System.out.println("the map url is = " + urlToMap);
				l.setMapURL(urlToMap);
			}

			copyLocationValues(l);
			l.setLocationMap(null);
			System.out.println(l.getLocationArea() + loc.getLocationName() + l.getMapURL());

			int status = locationService.updateLocation(l);

			System.out.println("status = " + status);

			if (status == 1) {
				response = Response.build("Success", "Location details successfully updated to the database", true);
			} else {
				response = Response.build("Error", "Location details could not be updated to the database", false);
			} // nested if

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // updateLocation

	private void copyLocationValues(CreateLocation l) {
		// TODO Auto-generated method stub

		Location loc = new Location();

		// download old location information from Database
		try {
			loc = locationService.getLocation(l.getLocationID());

			if (l.getLocationName() == null)
				l.setLocationName(loc.getLocationName());

			if (l.getLocationArea() == null)
				l.setLocationArea(loc.getLocationArea());

			if (l.getLocationMap() == null) {
				l.setMapURL(loc.getMapURL());
			} else {
				String urlToMap = locValid.writeImageStreamToFile(l.getLocationMap(), l.getLocationName());
				System.out.println("the map url is = " + urlToMap);
				l.setMapURL(urlToMap);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// **************************************************************************************
	// get all locations
	// **************************************************************************************
	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_DASH
			+ Constants.PATH_GET_LOC_ALL, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response getAllLocations(@RequestBody CreateLocation l) {
		List<Location> locationList = new ArrayList<Location>();

		try {

			// make sure that the staff_email and locationID are not null
			if (l.getStaff_email_id() == null) {
				response = Response.build("Error", "Mandatory field staff_email_id", false);
				return response;
			} // null fields

			// check to see if the fields are empty
			if (l.getStaff_email_id().contentEquals("")) {
				response = Response.build("Error", "Mandatory field : staff_email_id cannot be empty", false);
				return response;
			} // empty fields

			// check to see if the staff user exists on the database
			if (!cpu.userExists(l.getStaff_email_id())) {
				response = Response.build("Error",
						"Staff email " + l.getStaff_email_id() + " does not exist on the database", false);
				return response;
			} // staff checks

			// get a list of all the locations and return it to the client
			locationList = locationService.getAllLocations();

			// locations successfully loaded
			response = Response.build("Success",
					locationList.size() + " locations successfully retrieved from the database", true);
			response.setData(locationList);

		} catch (Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // getalllocations

	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_DASH
			+ Constants.PATH_GET_LOCATION_BY_ITEM, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response getLocation(@RequestBody ProcessLocation p) {

		List<Kiosk> kioskList = new ArrayList<>();
		List<Zone> zoneList = new ArrayList<>();
		List<Rate> rateList = new ArrayList<>();
		List<RateModel> rateDiscountList = new ArrayList<>();

		try {

			if (p.getLocation_id() == 0 || p.getStaff_email_id() == null || p.getShowField() == null) {
				response = Response.build("Error",
						"Mandatory fields either [staff_email_id / location_id / showField] cannot be null", false);
				return response;
			} // isNull

			if (p.getStaff_email_id().isEmpty() || p.getShowField().isEmpty()) {
				response = Response.build("Error",
						"Mandatory fields either [staff_email_id / showField] cannot be empty", false);
				return response;
			} // isEmpty

			// check to see if the location exists on the database
			if (!cpu.locationExists(p.getLocation_id())) {
				response = Response.build("Error",
						"Location of ID " + p.getLocation_id() + " does not exist on the database", false);
				return response;
			} // location checks

			// check to see if the staff user exists on the database
			if (!cpu.userExists(p.getStaff_email_id())) {
				response = Response.build("Error",
						"Staff email " + p.getStaff_email_id() + " does not exist on the database", false);
				return response;
			} // staff checks

			CreateLocation l = new CreateLocation();
			l.setLocationID(p.getLocation_id());

			switch (p.getShowField().toLowerCase()) {
			case "rate":
				System.out.println("Obtaining rate list");
				rateList = locationService.getRate(l);

				// we need to go through the rateList and create a rate discount list
				for (int z = 0; z < rateList.size(); z++) {
					RateModel rateModel = new RateModel();
					List<Discount> discountList = locationService.getDiscountByRateID(rateList.get(z).getRateID());
					if (!discountList.isEmpty()) {
						rateModel.setDiscountList(discountList);
					}
					rateModel.setRateID(rateList.get(z).getRateID());
					rateModel.setStandardRate(rateList.get(z).getRateMax());
					rateModel.setZoneID(rateList.get(z).getZoneID());
					rateDiscountList.add(rateModel);
				}
				p.setRateDiscountList(rateDiscountList);

				// p.setRate(rateLisKt);
				break;
			case "zone":
				System.out.println("Obtaining zone list");
				zoneList = locationService.getZone(l);
				p.setZone(zoneList);
				break;
			case "kiosk":
				System.out.println("Obtaining list of kiosks");
				kioskList = locationService.getKiosk(l);
				p.setKiosk(kioskList);
				break;

			default:
				response = Response.build("Error", "Please chose either showField = [zone/rate/kiosk]", false);
				return response;

			} // switch

			String fieldName = p.getShowField();
			fieldName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1).toLowerCase();

			response = Response.build("Success", fieldName + " data successfully obtained", true);
			response.setData(p);

		} catch (Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // getLocation

	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_DASH
			+ Constants.PATH_UPLOAD_MAP, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response uploadMap(@RequestBody CreateLocation l) {

		try {

			// make sure that the staff_email and locationID are not null
			if (l.getStaff_email_id() == null || l.getLocationID() == 0 || l.getLocationMap() == null) {
				response = Response.build("Error",
						"Mandatory field staff_email_id / locationID / locationMap cannot be null", false);
				return response;
			} // null fields

			// check to see if the fields are empty
			if (l.getStaff_email_id().contentEquals("") || l.getLocationID() == 0) {
				response = Response.build("Error", "Mandatory field : staff_email_id / locationID cannot be empty",
						false);
				return response;
			} // empty fields

			// check to see if the staff user exists on the database
			if (!cpu.userExists(l.getStaff_email_id())) {
				response = Response.build("Error",
						"Staff email " + l.getStaff_email_id() + " does not exist on the database", false);
				return response;
			} // staff checks

			// check to see if the locationID exists
			if (!cpu.locationExists(l.getLocationID())) {
				response = Response.build("Error",
						"Location ID " + l.getLocationID() + " does not exist on the database", false);
				return response;
			} // location checks

			if (l.getLocationMap() == null) {
				response = Response.build("Error", "locationMap cannot be empty", false);
				return response;
			}

			Location loc = locationService.getLocation(l.getLocationID());
			if (l.getLocationMap() == null) {
				l.setMapURL("");
			} else {
				String urlToMap = locValid.writeImageStreamToFile(l.getLocationMap(), loc.getLocationName());
				System.out.println("the map url is = " + urlToMap);
				l.setMapURL(urlToMap);
			}

			// now we update the application with the deposit url
			int updateStatus = locationService.updateLocationURL(l.getLocationID(), l.getMapURL());

			if (updateStatus == 0) {
				response = Response.build("Failure", "Map was not added to the database", false);
				return response;
			} else {
				response = Response.build("Success", "Map successfully added to location", true);
			}

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // uploadMap

} // LocationController
