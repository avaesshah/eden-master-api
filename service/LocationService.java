package com.eden.api.service;

import java.util.List;

import com.edenstar.model.Discount;
import com.edenstar.model.Kiosk;
import com.edenstar.model.Location;
import com.edenstar.model.Rate;
import com.edenstar.model.Zone;
import com.edenstar.model.dash.CreateKiosk;
import com.edenstar.model.dash.CreateLocation;
import com.edenstar.model.dash.CreateZone;

public interface LocationService {

	// find a location service by name
	Location getLocationByName(String locationName) throws Exception;

	// Add a location
	int addLocation(CreateLocation l) throws Exception;

	// Add a zone
	int addZone(CreateLocation l, int i) throws Exception;

	// Add a zone
	int addZone(CreateZone z) throws Exception;

	// Add a rate
	int addRate(CreateLocation l, int i) throws Exception;

	// Add a kiosk
	int addKiosk(CreateLocation l, int i) throws Exception;

	// Get location details by id
	Location getLocation(int location_id) throws Exception;

	// get a list of zones according to location_id
	List<Zone> getZone(CreateLocation l) throws Exception;

	// get list of rates for a given location_id
	List<Rate> getRate(CreateLocation l) throws Exception;

	// get all kiosks for a given location_id
	List<Kiosk> getKiosk(CreateLocation l) throws Exception;

	// returns the maxiumum rows or columns for a given location id
	int getMaxRowsOrColumns(int locationID, String rowOrColumm) throws Exception;

	// deletes an entire location with all associated entities - kiosks, rates,
	// zones
	int deleteLocation(int locationID) throws Exception;

	// update location details
	int updateLocation(CreateLocation l) throws Exception;

	// returns the maximum zone number
	int getMaxZoneNum(int locationID);

	// add a rate by CreateZone type
	int addRate(CreateZone z) throws Exception;

	// returns zone information for a given zone_id
	Zone getZone(int zone_id) throws Exception;

	// returns rate information for a given zone id
	Rate getRate(int zoneID) throws Exception;

	// returns a list of kiosks under a zone id
	List<Kiosk> getKiosk(int zoneID) throws Exception;

	// deletes zone and linked entities
	int deleteZone(int zoneID) throws Exception;

	// make sure that the new zoneNumber does not conflict with existing zone
	// numbers
	Zone checkZoneNum(CreateZone z) throws Exception;

	// update zone with new data
	int updateZone(CreateZone z) throws Exception;

	// returns the rate by rate_id
	Rate getRateByRateID(int rateID) throws Exception;

	// update rate with new rate information
	int updateRate(CreateZone z) throws Exception;

	// get kiosk information by ID
	Kiosk getKioskByID(int kioskID) throws Exception;

	// get number of kiosks in a location
	int getKioskCount(int locationID) throws Exception;
	
	// finds the next available kiosk row and column
	CreateKiosk getNextRowAndCol(CreateKiosk k) throws Exception;

	// adds a kiosk to a specified zone in a location
	int addKiosk(CreateKiosk k) throws Exception;

	// delete a kiosk
	int deleteKiosk(int kioskID) throws Exception;

	// update kiosk information
	int updateKiosk(CreateKiosk k) throws Exception;

	// returns a list of all locations
	List<Location> getAllLocations() throws Exception;

	// get a list of kiosk by zone
	List<Kiosk> getKioskByZone(int zoneID) throws Exception;

	// add a discount to the database
	int addDiscount(CreateLocation l, int i, int k) throws Exception;

	// obtains a discount list by location id
	List<Discount> getDiscountByLocation(CreateLocation getLoc) throws Exception;

	// obtains a discount list by rate id
	List<Discount> getDiscountByRateID(int rateID) throws Exception;

	// add discount to rate by zone
	int addDiscountByZone(CreateZone z, int k) throws Exception;

	// deletes a discount
	int deleteDiscount(int rateID) throws Exception;

	// upload map to location
	int updateLocationURL(int locationID, String mapURL) throws Exception;

} // LocationService
