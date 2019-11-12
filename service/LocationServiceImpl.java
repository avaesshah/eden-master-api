package com.eden.api.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eden.api.dao.LocationDAO;
import com.edenstar.model.Discount;
import com.edenstar.model.Kiosk;
import com.edenstar.model.Location;
import com.edenstar.model.Rate;
import com.edenstar.model.Zone;
import com.edenstar.model.dash.CreateKiosk;
import com.edenstar.model.dash.CreateLocation;
import com.edenstar.model.dash.CreateZone;

@Service("locationService")
public class LocationServiceImpl implements LocationService {

	@Autowired
	private LocationDAO locationDAO;

	@Override
	public Location getLocationByName(String locationName) throws Exception {

		return locationDAO.getLocationByName(locationName);
	}

	@Override
	public int addLocation(CreateLocation l) throws Exception {

		return locationDAO.addLocation(l);
	}

	@Override
	public int addZone(CreateLocation l, int i) throws Exception {

		return locationDAO.addZone(l, i);
	}

	@Override
	public int addRate(CreateLocation l, int i) throws Exception {

		return locationDAO.addRate(l, i);
	}

	@Override
	public int addKiosk(CreateLocation l, int i) throws Exception {

		return locationDAO.addKiosk(l, i);
	}

	@Override
	public Location getLocation(int location_id) throws Exception {

		return locationDAO.getLocation(location_id);
	}

	@Override
	public List<Zone> getZone(CreateLocation l) throws Exception {

		return locationDAO.getZone(l);
	}

	@Override
	public List<Rate> getRate(CreateLocation l) throws Exception {

		return locationDAO.getRate(l);
	}

	@Override
	public List<Kiosk> getKiosk(CreateLocation l) throws Exception {

		return locationDAO.getKiosk(l);
	}

	@Override
	public int getMaxRowsOrColumns(int locationID, String rowOrColumn) throws Exception {

		return locationDAO.getRowOrColMax(locationID, rowOrColumn);
	}

	@Override
	public int deleteLocation(int locationID) throws Exception {

		return locationDAO.deleteLocation(locationID);
	}

	@Override
	public int updateLocation(CreateLocation l) throws Exception {

		return locationDAO.updateLocation(l);
	}

	@Override
	public int getMaxZoneNum(int locationID) {

		return locationDAO.getMaxZoneNum(locationID);
	}

	@Override
	public int addZone(CreateZone z) throws Exception {

		return locationDAO.addZone(z);
	}

	@Override
	public int addRate(CreateZone z) throws Exception {

		return locationDAO.addRate(z);
	}

	@Override
	public Zone getZone(int zone_id) throws Exception {

		return locationDAO.getZone(zone_id);
	}

	@Override
	public Rate getRate(int zoneID) throws Exception {

		return locationDAO.getRate(zoneID);
	}

	@Override
	public List<Kiosk> getKiosk(int zoneID) throws Exception {

		return locationDAO.getKiosk(zoneID);
	}

	@Override
	public int deleteZone(int zoneID) throws Exception {

		return locationDAO.deleteZone(zoneID);
	}

	@Override
	public Zone checkZoneNum(CreateZone z) throws Exception {

		return locationDAO.checkZoneNum(z);
	}

	@Override
	public int updateZone(CreateZone z) throws Exception {

		return locationDAO.updateZone(z);
	}

	@Override
	public Rate getRateByRateID(int rateID) throws Exception {
		// TODO Auto-generated method stub
		return locationDAO.getRateByRateID(rateID);
	}

	@Override
	public int updateRate(CreateZone z) throws Exception {
		// TODO Auto-generated method stub
		return locationDAO.updateRate(z);
	}

	@Override
	public Kiosk getKioskByID(int kioskID) throws Exception {
		// TODO Auto-generated method stub
		return locationDAO.getKioskByID(kioskID);
	}

	@Override
	public int getKioskCount(int locationID) {
		// TODO Auto-generated method stub
		return locationDAO.getKioskCount(locationID);
	}

	@Override
	public CreateKiosk getNextRowAndCol(CreateKiosk k) {
		// TODO Auto-generated method stub
		return locationDAO.getNextRowAndCol(k);
	}

	@Override
	public int addKiosk(CreateKiosk k) throws Exception {
		// TODO Auto-generated method stub
		return locationDAO.addKiosk(k);
	}

	@Override
	public int deleteKiosk(int kioskID) throws Exception {
		// TODO Auto-generated method stub
		return locationDAO.deleteKiosk(kioskID);
	}

	@Override
	public int updateKiosk(CreateKiosk k) throws Exception {
		// TODO Auto-generated method stub
		return locationDAO.updateKiosk(k);
	}

	@Override
	public List<Location> getAllLocations() throws Exception {
		// TODO Auto-generated method stub
		return locationDAO.getAllLocations();
	}

	@Override
	public List<Kiosk> getKioskByZone(int zoneID) throws Exception {
		// TODO Auto-generated method stub
		return locationDAO.getKioskbyZone(zoneID);
	}

	@Override
	public int addDiscount(CreateLocation l, int i, int k) throws Exception {
		// TODO Auto-generated method stub
		return locationDAO.addDiscount(l,i,k);
	}

	@Override
	public List<Discount> getDiscountByLocation(CreateLocation getLoc) throws Exception {
		// TODO Auto-generated method stub
		return locationDAO.getDiscountByLocation(getLoc);
	}

	@Override
	public List<Discount> getDiscountByRateID(int rateID) throws Exception {
		// TODO Auto-generated method stub
		return locationDAO.getDiscountByRateID(rateID);
	}

	@Override
	public int addDiscountByZone(CreateZone z, int k) throws Exception {
		// TODO Auto-generated method stub
		return locationDAO.addDiscountByZone(z,k);
	}

	@Override
	public int deleteDiscount(int rateID) throws Exception {
		// TODO Auto-generated method stub
		return locationDAO.deleteDiscount(rateID);
	}

	@Override
	public int updateLocationURL(int locationID, String mapURL) throws Exception {
		// TODO Auto-generated method stub
		return locationDAO.addMap(locationID, mapURL);
	}

} // LocationService
