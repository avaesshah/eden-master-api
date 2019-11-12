package com.eden.api.dao;

import java.util.List;

import com.edenstar.model.Discount;
import com.edenstar.model.Kiosk;
import com.edenstar.model.Location;
import com.edenstar.model.Rate;
import com.edenstar.model.Zone;
import com.edenstar.model.dash.CreateKiosk;
import com.edenstar.model.dash.CreateLocation;
import com.edenstar.model.dash.CreateZone;

public interface LocationDAO {

	// returns a location by name
	Location getLocationByName(String locationName) throws Exception;

	// add a new location
	int addLocation(CreateLocation l) throws Exception;

	// add a new Zone
	int addZone(CreateLocation l, int i) throws Exception;
	
	// add a new Zone with CreatZone type
	int addZone(CreateZone z) throws Exception;

	// add a Rate
	int addRate(CreateLocation l, int i) throws Exception;

	// add a Kiosk
	int addKiosk(CreateLocation l, int i) throws Exception;

	// get a Location
	Location getLocation(int location_id) throws Exception;

	// get zone list
	List<Zone> getZone(CreateLocation l) throws Exception;

	// get rate list
	List<Rate> getRate(CreateLocation l) throws Exception;

	// get kiosk list
	List<Kiosk> getKiosk(CreateLocation l) throws Exception;
	
	// get max row or col for given location id
	int getRowOrColMax(int locationID, String rowOrColumn);

	// delete entire location
	int deleteLocation(int locationID);

	// update location
	int updateLocation(CreateLocation l);

	// get the max zone number
	int getMaxZoneNum(int locationID);

	// add a new rate to a location
	int addRate(CreateZone z);

	// get zone information by zone_id
	Zone getZone(int zone_id);

	// get rate by zone id
	Rate getRate(int zoneID);
	
	// get kiosk by zone id
	List<Kiosk> getKiosk(int zoneID);

	// delete a zone
	int deleteZone(int zoneID);

	// check if zoneNumber exists
	Zone checkZoneNum(CreateZone z);

	// update zone information
	int updateZone(CreateZone z);

	// returns rate by rateID
	Rate getRateByRateID(int rateID);

	// update rate
	int updateRate(CreateZone z);

	// returns kiosk by ID
	Kiosk getKioskByID(int kioskID);

	// return num of kioks in a location
	int getKioskCount(int locationID);

	// assigns the next available row and column to new kiosk
	CreateKiosk getNextRowAndCol(CreateKiosk k);

	// adds a kiosk
	int addKiosk(CreateKiosk k);

	// delete a kiosk
	int deleteKiosk(int kioskID);

	// update kiosk
	int updateKiosk(CreateKiosk k);

	// get all locations
	List<Location> getAllLocations();

	// kiosks by zone id
	List<Kiosk> getKioskbyZone(int zone_id) throws Exception;

	// add a discount to the rate
	int addDiscount(CreateLocation l, int i, int k) throws Exception;

	// get discount by location id
	List<Discount> getDiscountByLocation(CreateLocation l) throws Exception;

	// get discount list by rate id
	List<Discount> getDiscountByRateID(int rate_id) throws Exception;

	// add discount by zone
	int addDiscountByZone(CreateZone z, int k) throws Exception;

	// delete a discount
	int deleteDiscount(int rateID) throws Exception;

	// add map to location
	int addMap(int locationID, String mapURL) throws Exception;

} // LocationDAO
