package com.eden.api.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import com.eden.api.util.GridMapper;
import com.eden.api.util.KioskMapper;
import com.eden.api.util.LocationMapper;
import com.eden.api.util.RateMapper;
import com.eden.api.util.ZoneMapper;
import com.edenstar.model.Discount;
import com.edenstar.model.GridCount;
import com.edenstar.model.Kiosk;
import com.edenstar.model.Location;
import com.edenstar.model.Rate;
import com.edenstar.model.Zone;
import com.edenstar.model.dash.CreateKiosk;
import com.edenstar.model.dash.CreateLocation;
import com.edenstar.model.dash.CreateZone;

@Component
public class LocationDAOImpl implements LocationDAO {

	private JdbcTemplate jdbcTemplate;

	@Autowired
	public LocationDAOImpl(JdbcTemplate jdbcTemplate) {
		super();
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public Location getLocationByName(String locationName) throws Exception {

		Location r = new Location();
		String query = "SELECT * FROM location where location.is_archived = 0 and location_name ='" + locationName + "'";

		try {

			r = jdbcTemplate.queryForObject(query, new LocationMapper());

		} catch (Exception e) {

		} // try

		return r;
	} // getLocationByName

	@Override
	public int addLocation(CreateLocation l) throws Exception {

		NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
		final KeyHolder keyHolder = new GeneratedKeyHolder();
		int status = 0;

		final String INSERT_QUERY = "insert into location (location_name, location_area, mapURL) values" + " (:location_name,  :location_area, :mapURL)";

		try {

			// Creating map with all required params
			MapSqlParameterSource paramMap = new MapSqlParameterSource();

			paramMap.addValue("location_name", l.getLocationName().toUpperCase());
			paramMap.addValue("location_area", l.getLocationArea().toUpperCase());
			paramMap.addValue("mapURL", l.getMapURL().toLowerCase());

			status = namedJdbcTemplate.update(INSERT_QUERY, paramMap, keyHolder, new String[] { "location_id" });

			// get the auto generated primary key from the new customer insertion
			Number generatedComID = keyHolder.getKey();
			status = generatedComID.intValue();

			System.out.println("new location ID = " + status);

		} catch (Exception e) {

		} // try

		return status;
	} // addLocation

	@Override
	public int addZone(CreateLocation l, int i) throws Exception {

		NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
		final KeyHolder keyHolder = new GeneratedKeyHolder();
		int status = 0;

		final String INSERT_QUERY = "insert into zone (zone_number, location_id, zone_name) values"
				+ " (:zone_number, :location_id, :zone_name)";

		try {

			// Creating map with all required params
			MapSqlParameterSource paramMap = new MapSqlParameterSource();

			paramMap.addValue("zone_number", l.getZoneList().get(i).getZoneNumber());
			paramMap.addValue("location_id", l.getLocationID());
			paramMap.addValue("zone_name", l.getZoneList().get(i).getZoneName().toUpperCase());

			status = namedJdbcTemplate.update(INSERT_QUERY, paramMap, keyHolder, new String[] { "zone_id" });

			// get the auto generated primary key from the new customer insertion
			Number generatedComID = keyHolder.getKey();
			status = generatedComID.intValue();

			System.out.println("new zone ID = " + status);

		} catch (Exception e) {

		} // try

		return status;
	} // addZone

	@Override
	public int addRate(CreateLocation l, int i) throws Exception {

		NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
		final KeyHolder keyHolder = new GeneratedKeyHolder();
		int status = 0;

		final String INSERT_QUERY = "insert into rate (rate_max, rate_min, discount_duration_days, rate_coeff, zone_id, standard_rate) values"
				+ " (:rate_max, :rate_min, :discount_duration_days, :rate_coeff, :zone_id, :standard_rate)";

		try {

			// Creating map with all required params
			MapSqlParameterSource paramMap = new MapSqlParameterSource();

			paramMap.addValue("rate_max", l.getRateList().get(i).getRateMax());
			paramMap.addValue("rate_min", l.getRateList().get(i).getRateMin());
			paramMap.addValue("discount_duration_days", l.getRateList().get(i).getDiscountDurationDays());
			paramMap.addValue("rate_coeff", l.getRateList().get(i).getRateCoeff());
			paramMap.addValue("zone_id", l.getRateList().get(i).getZoneID());
			paramMap.addValue("standard_rate", l.getRateList().get(i).getStandardRate());

			status = namedJdbcTemplate.update(INSERT_QUERY, paramMap, keyHolder, new String[] { "rate_id" });

			// get the auto generated primary key from the new customer insertion
			Number generatedComID = keyHolder.getKey();
			status = generatedComID.intValue();

			System.out.println("new rate ID = " + status);

		} catch (Exception e) {

		} // try

		return status;
	} // addRate

	@Override
	public int addKiosk(CreateLocation l, int i) throws Exception {

		NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
		final KeyHolder keyHolder = new GeneratedKeyHolder();
		int status = 0;

		final String INSERT_QUERY = "insert into kiosk (kiosk_number, grid_location_row, grid_location_column, is_void, is_locked, zone_id) values"
				+ " (:kiosk_number, :grid_location_row, :grid_location_column, :is_void, :is_locked, :zone_id)";

		try {

			// Creating map with all required params
			MapSqlParameterSource paramMap = new MapSqlParameterSource();

			paramMap.addValue("kiosk_number", l.getKioskList().get(i).getKioskNumber());
			paramMap.addValue("grid_location_row", l.getKioskList().get(i).getGridLocationRow());
			paramMap.addValue("grid_location_column", l.getKioskList().get(i).getGridLocationColumn());
			paramMap.addValue("is_void", l.getKioskList().get(i).getIsVoid());
			paramMap.addValue("is_locked", l.getKioskList().get(i).getIsLocked());
			paramMap.addValue("zone_id", l.getKioskList().get(i).getZoneID());

			status = namedJdbcTemplate.update(INSERT_QUERY, paramMap, keyHolder, new String[] { "kiosk_id" });

			// get the auto generated primary key from the new customer insertion
			Number generatedComID = keyHolder.getKey();
			status = generatedComID.intValue();

			System.out.println("new zone ID = " + status);

		} catch (Exception e) {

		} // try

		return status;
	} // addKiosk

	@Override
	public Location getLocation(int location_id) throws Exception {

		Location r = new Location();
		String query = "SELECT * FROM location where location_id ='" + location_id + "'";

		try {

			r = jdbcTemplate.queryForObject(query, new LocationMapper());

		} catch (Exception e) {

		} // try

		return r;
	} // getLocation

	@Override
	public List<Zone> getZone(CreateLocation l) throws Exception {

		String query = "SELECT * FROM zone where is_archived = 0 and location_id ='" + l.getLocationID() + "'";
		List<Zone> zoneList = new ArrayList<Zone>();
		List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();

		try {

			rows = jdbcTemplate.queryForList(query);

			for (Map<String, Object> row : rows) {

				Zone z = new Zone();

				z.setZoneID((Integer) (row.get("zone_id")));
				z.setZoneNumber((Integer) (row.get("zone_number")));
				z.setLocationID((Integer) (row.get("location_id")));
				z.setZoneName((String) (row.get("zone_name")));

				zoneList.add(z);
			}

		} catch (Exception e) {

		} // try

		return zoneList;
	} // getZone

	@Override
	public List<Rate> getRate(CreateLocation l) throws Exception {

		String query = "SELECT * FROM rate natural join zone natural join location where location_id ='"
				+ l.getLocationID() + "'";
		List<Rate> rateList = new ArrayList<Rate>();
		List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();

		try {

			rows = jdbcTemplate.queryForList(query);
			System.out.println("getRate rows = " + rows.size());
			for (Map<String, Object> row : rows) {

				Rate r = new Rate();

				r.setRateID((Integer) (row.get("rate_id")));
				r.setRateMax((Double) (row.get("rate_max")));
				r.setRateMin((Double) (row.get("rate_min")));
				r.setDiscountDurationDays((Integer) (row.get("discount_duration_days")));
				r.setRateCoeff((Double) (row.get("rate_coeff")));
				r.setZoneID((Integer) (row.get("zone_id")));
				r.setStandardRate((Double)(row.get("standard_rate")));

				rateList.add(r);
			}

		} catch (Exception e) {

		} // try

		return rateList;
	} // getRate
	
	@Override
	public List<Discount> getDiscountByLocation(CreateLocation l) throws Exception {

		String query = "SELECT * FROM discount natural join rate natural join zone natural join location where location_id ='"
				+ l.getLocationID() + "'";
		List<Discount> discountList = new ArrayList<>();
		List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();

		try {

			rows = jdbcTemplate.queryForList(query);

			for (Map<String, Object> row : rows) {

				Discount d = new Discount();

				d.setDiscount_id((Integer) (row.get("discount_id")));
				d.setRate_id((Integer) (row.get("rate_id")));
				d.setFromMonth((Integer) (row.get("from_month")));
				d.setToMonth((Integer) (row.get("to_month")));
				d.setPercentageDiscount((Double) (row.get("percentage")));
				
				discountList.add(d);
			}

		} catch (Exception e) {

		} // try

		return discountList;
	} // getDiscount
	
	@Override
	public List<Discount> getDiscountByRateID(int rate_id) throws Exception {

		String query = "SELECT * FROM discount natural join rate where rate_id ='"
				+ rate_id + "'";
		List<Discount> discountList = new ArrayList<>();
		List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();

		try {

			rows = jdbcTemplate.queryForList(query);

			for (Map<String, Object> row : rows) {

				Discount d = new Discount();

				d.setDiscount_id((Integer) (row.get("discount_id")));
				d.setRate_id((Integer) (row.get("rate_id")));
				d.setFromMonth((Integer) (row.get("from_month")));
				d.setToMonth((Integer) (row.get("to_month")));
				d.setPercentageDiscount((Double) (row.get("percentage")));
				
				discountList.add(d);
			}

		} catch (Exception e) {

		} // try

		return discountList;
	} // getDiscount

	@Override
	public List<Kiosk> getKiosk(CreateLocation l) throws Exception {

		String query = "SELECT * FROM kiosk natural join zone natural join location where location_id ='"
				+ l.getLocationID() + "'";
		List<Kiosk> kioskList = new ArrayList<Kiosk>();
		List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();

		try {

			rows = jdbcTemplate.queryForList(query);

			for (Map<String, Object> row : rows) {

				Kiosk k = new Kiosk();

				k.setKioskID((Integer) (row.get("kiosk_id")));
				k.setKioskNumber((Integer) (row.get("kiosk_number")));
				k.setGridLocationRow((Integer) (row.get("grid_location_row")));
				k.setGridLocationColumn((Integer) (row.get("grid_location_column")));
				k.setIsVoid((Integer) (row.get("is_void")));
				k.setIsLocked((Integer) (row.get("is_locked")));
				k.setZoneID((Integer) (row.get("zone_id")));

				kioskList.add(k);
			}

		} catch (Exception e) {

		} // try

		return kioskList;
	} // getKiosk
	
	@Override
	public List<Kiosk> getKioskbyZone(int zone_id) throws Exception {

		String query = "SELECT * FROM kiosk natural join zone where zone_id ='"
				+ zone_id + "'";
		List<Kiosk> kioskList = new ArrayList<Kiosk>();
		List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();

		try {

			rows = jdbcTemplate.queryForList(query);

			for (Map<String, Object> row : rows) {

				Kiosk k = new Kiosk();

				k.setKioskID((Integer) (row.get("kiosk_id")));
				k.setKioskNumber((Integer) (row.get("kiosk_number")));
				k.setGridLocationRow((Integer) (row.get("grid_location_row")));
				k.setGridLocationColumn((Integer) (row.get("grid_location_column")));
				k.setIsVoid((Integer) (row.get("is_void")));
				k.setIsLocked((Integer) (row.get("is_locked")));
				k.setZoneID((Integer) (row.get("zone_id")));

				kioskList.add(k);
			}

		} catch (Exception e) {

		} // try

		return kioskList;
	} // getKiosk

	@Override
	public int getRowOrColMax(int locationID, String rowOrColumn) {
		GridCount result = new GridCount();
		String grid = "";

		if (rowOrColumn.contentEquals("rows")) {
			grid = "grid_location_row";
		} else if (rowOrColumn.contentEquals("columns")) {
			grid = "grid_location_column";
		}

		String query = "SELECT max(" + grid
				+ ") as gridMax FROM kiosk natural join zone natural join location where location_id = '" + +locationID
				+ "'";

		try {

			result = jdbcTemplate.queryForObject(query, new GridMapper());

		} catch (Exception e) {

		} // try

		System.out.println("result = " + result.getGrid_max() + " and location_id is = " + locationID);
		return result.getGrid_max();

	} // get max row or col

	@Override
	public int deleteLocation(int locationID) {

		int status = 0;

		final String DELETE_QUERY =  "update location set is_archived = 1 where location_id = ?";// "delete from location where location_id = ?";

		// define query arguments
		Object[] params = { locationID };

		status = jdbcTemplate.update(DELETE_QUERY, params);
		System.out.println("delete status = " + status);

		return status;
	} // deleteLocation

	@Override
	public int updateLocation(CreateLocation l) {

		NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
		int status = 0;

		String UPDATE_QUERY = "update location set location_name = :location_name, location_area = :location_area,"
				+ "location_map = :location_map, mapURL = :mapURL where location_id = :location_id";

		try {

			// Adding params using MapSqlParameterSource class
			SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("location_id", l.getLocationID())
					.addValue("location_area", l.getLocationArea())
					.addValue("location_map",  l.getLocationMap())
					.addValue("mapURL", l.getMapURL())
					.addValue("location_name", l.getLocationName().toLowerCase());

			status = namedJdbcTemplate.update(UPDATE_QUERY, namedParameters);

			if (status != 0) {
				System.out.println("Location data updated for ID " + l.getLocationID());
			} else {
				System.out.println("Location data failed to update for " + l.getLocationID());
			}

		} catch (Exception e) {

		} // try
		return status;
	}

	@Override
	public int getMaxZoneNum(int locationID) {

		int result = 0;

		String query = "SELECT max(zone_number) as zoneMax FROM zone where is_archived = 0 and location_id = '" + +locationID + "'";

		try {

			result = jdbcTemplate.queryForObject(query, Integer.class);

		} catch (Exception e) {

		} // try

		System.out.println("result = " + result + " and location_id is = " + locationID);
		return result;
	} // getMaxZoneNum

	@Override
	public int addZone(CreateZone z) throws Exception {

		NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
		final KeyHolder keyHolder = new GeneratedKeyHolder();
		int status = 0;

		final String INSERT_QUERY = "insert into zone (zone_number, location_id, zone_name) values"
				+ " (:zone_number, :location_id, :zone_name)";

		try {

			// Creating map with all required params
			MapSqlParameterSource paramMap = new MapSqlParameterSource();

			paramMap.addValue("zone_number", z.getZoneNumber());
			paramMap.addValue("location_id", z.getLocationID());
			paramMap.addValue("zone_name", z.getZoneName().toUpperCase());

			status = namedJdbcTemplate.update(INSERT_QUERY, paramMap, keyHolder, new String[] { "zone_id" });

			// get the auto generated primary key from the new customer insertion
			Number generatedComID = keyHolder.getKey();
			status = generatedComID.intValue();

			System.out.println("new zone ID = " + status);

		} catch (Exception e) {

		} // try

		return status;
	}

	@Override
	public int addRate(CreateZone z) {

		NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
		final KeyHolder keyHolder = new GeneratedKeyHolder();
		int status = 0;

		final String INSERT_QUERY = "insert into rate (rate_max, rate_min, discount_duration_days, rate_coeff, zone_id, standard_rate) values"
				+ " (:rate_max, :rate_min, :discount_duration_days, :rate_coeff, :zone_id, :standard_rate)";

		try {

			// Creating map with all required params
			MapSqlParameterSource paramMap = new MapSqlParameterSource();

			paramMap.addValue("rate_max", z.getRate().getRateMax());
			paramMap.addValue("rate_min", z.getRate().getRateMin());
			paramMap.addValue("discount_duration_days", z.getRate().getDiscountDurationDays());
			paramMap.addValue("rate_coeff", z.getRate().getRateCoeff());
			paramMap.addValue("zone_id", z.getRate().getZoneID());
			paramMap.addValue("standard_rate", z.getRate().getStandardRate());

			status = namedJdbcTemplate.update(INSERT_QUERY, paramMap, keyHolder, new String[] { "rate_id" });

			// get the auto generated primary key from the new customer insertion
			Number generatedComID = keyHolder.getKey();
			status = generatedComID.intValue();

			System.out.println("new rate ID = " + status);

		} catch (Exception e) {

		} // try

		return status;
	} // addRate

	@Override
	public Zone getZone(int zone_id) {

		Zone z = new Zone();
		String query = "SELECT * FROM zone where zone_id ='" + zone_id + "'";

		try {

			z = jdbcTemplate.queryForObject(query, new ZoneMapper());

		} catch (Exception e) {

		} // try

		return z;
	} // getZone

	@Override
	public Rate getRate(int zoneID) {
		Rate r = new Rate();
		String query = "SELECT * FROM rate where zone_id ='" + zoneID + "'";

		try {

			r = jdbcTemplate.queryForObject(query, new RateMapper());

		} catch (Exception e) {

		} // try

		return r;
	} // get Rate by zone ID

	@Override
	public List<Kiosk> getKiosk(int zoneID) {

		String query = "SELECT * FROM kiosk natural join zone where kiosk.is_archived = 0 and zone_id ='" + zoneID + "'";
		List<Kiosk> kioskList = new ArrayList<Kiosk>();
		List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();

		try {

			rows = jdbcTemplate.queryForList(query);

			for (Map<String, Object> row : rows) {

				Kiosk k = new Kiosk();

				k.setKioskID((Integer) (row.get("kiosk_id")));
				k.setKioskNumber((Integer) (row.get("kiosk_number")));
				k.setGridLocationRow((Integer) (row.get("grid_location_row")));
				k.setGridLocationColumn((Integer) (row.get("grid_location_column")));
				k.setIsVoid((Integer) (row.get("is_void")));
				k.setIsLocked((Integer) (row.get("is_locked")));
				k.setZoneID((Integer) (row.get("zone_id")));

				kioskList.add(k);
			}

		} catch (Exception e) {

		} // try

		return kioskList;
	}

	@Override
	public int deleteZone(int zoneID) {

		int status = 0;

		final String DELETE_QUERY = "update zone set is_archived = 1 where zone_id = ?"; //"delete from zone where zone_id = ?";

		// define query arguments
		Object[] params = { zoneID };

		status = jdbcTemplate.update(DELETE_QUERY, params);
		System.out.println("delete status = " + status);

		return status;
	} // deleteZone

	@Override
	public Zone checkZoneNum(CreateZone z) {

		Zone zone = new Zone();
		int location_id = 0;
		String query = "SELECT location_id FROM zone where zone_id ='" + z.getZoneID() + "'";
		String query2 = "SELECT * FROM  zone where location_id = ? AND zone_number = ?";

		try {

			location_id = jdbcTemplate.queryForObject(query, Integer.class);

			// define query arguments
			Object[] params = { location_id, z.getZoneNumber() };

			zone = jdbcTemplate.queryForObject(query2, params, new ZoneMapper());

		} catch (Exception e) {

		} // try

		return zone;
	} // checkZoneNum

	@Override
	public int updateZone(CreateZone z) {

		NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
		int status = 0;

		String UPDATE_QUERY = "update zone set zone_number = :zone_number, location_id = :location_id, zone_name = :zone_name where zone_id = :zone_id";

		try {

			// Adding params using MapSqlParameterSource class
			SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("zone_number", z.getZoneNumber())
					.addValue("location_id", z.getLocationID()).addValue("zone_name", z.getZoneName())
					.addValue("zone_id", z.getZoneID());

			status = namedJdbcTemplate.update(UPDATE_QUERY, namedParameters);

			if (status != 0) {
				System.out.println("Zone data updated for ID " + z.getZoneID());
			} else {
				System.out.println("Zone data failed to update for " + z.getZoneID());
			}

		} catch (Exception e) {

		} // try
		return status;

	} // updateZone

	@Override
	public Rate getRateByRateID(int rateID) {

		Rate r = new Rate();
		String query = "SELECT * FROM rate where rate_id ='" + rateID + "'";

		try {

			r = jdbcTemplate.queryForObject(query, new RateMapper());

		} catch (Exception e) {

		} // try

		return r;

	} // getRateByRateID

	@Override
	public int updateRate(CreateZone z) {

		NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
		int status = 0;

		String UPDATE_QUERY = "update rate set rate_max = :rate_max, "
				+ "rate_min = :rate_min, discount_duration_days = :discount_duration_days, "
				+ "rate_coeff = :rate_coeff, zone_id = :zone_id where rate_id = :rate_id";

		try {

			// Adding params using MapSqlParameterSource class
			SqlParameterSource namedParameters = new MapSqlParameterSource()
					.addValue("rate_max", z.getRate().getRateMax()).addValue("rate_min", z.getRate().getRateMin())
					.addValue("discount_duration_days", z.getRate().getDiscountDurationDays())
					.addValue("rate_coeff", z.getRate().getRateCoeff()).addValue("zone_id", z.getRate().getZoneID())
					.addValue("rate_id", z.getRate().getRateID());

			status = namedJdbcTemplate.update(UPDATE_QUERY, namedParameters);

			if (status != 0) {
				System.out.println("Rate data updated for ID " + z.getRate().getRateID());
			} else {
				System.out.println("Rate data failed to update for " + z.getRate().getRateID());
			}

		} catch (Exception e) {

		} // try
		return status;
	} // updateRate

	@Override
	public Kiosk getKioskByID(int kioskID) {

		Kiosk k = new Kiosk();
		String query = "SELECT * FROM kiosk where kiosk_id ='" + kioskID + "'";

		try {

			k = jdbcTemplate.queryForObject(query, new KioskMapper());

		} catch (Exception e) {

		} // try

		return k;

	} // getKioskByID

	@Override
	public int getKioskCount(int locationID) {

		int result = 0;

		String query = "SELECT max(kiosk_number) as kioskMax FROM kiosk natural join zone natural join location where location.is_archived = 0 and location_id = ?";

		try {

			// define query arguments
			Object[] params = { locationID };
			result = jdbcTemplate.queryForObject(query, params, Integer.class);

		} catch (Exception e) {

		} // try

		System.out.println("Maximum Kiosk Number = " + result + " and location_id is = " + locationID);
		return result;
	} // getKioskCount

	@Override
	public CreateKiosk getNextRowAndCol(CreateKiosk k) {

		int lastRow = 0;
		String query = " select max(grid_location_row) as rowMax from kiosk "
				+ "natural join zone natural join location " + "where location_id =? AND grid_location_column = ?";

		try {

			// define query arguments
			Object[] params = { k.getLocation_id(), k.getMax_columns() };

			lastRow = jdbcTemplate.queryForObject(query, params, Integer.class);

		} catch (Exception e) {

		} // try

		System.out.println("last row = " + lastRow + ", and last column is " + k.getMax_columns());

		// we need to check if the last row number = row_max, if so increment to the
		// next column
		// and set the row count to 1 else simply add 1 to the row

		if (lastRow == k.getMax_rows()) {
			// start from a new column
			k.setGridLocationRow(1); // set to 1
			k.setGridLocationColumn(k.getMax_columns() + 1); // new column

		} else {
			// increment the row
			k.setGridLocationRow(lastRow + 1); // next row location
			k.setGridLocationColumn(k.getMax_columns()); // the last column
		}

		return k;

	} // getNextRowAndCol

	@Override
	public int addKiosk(CreateKiosk k) {

		NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
		final KeyHolder keyHolder = new GeneratedKeyHolder();
		int status = 0;

		final String INSERT_QUERY = "insert into kiosk (kiosk_number, grid_location_row, grid_location_column, is_void, is_locked, zone_id) values"
				+ " (:kiosk_number, :grid_location_row, :grid_location_column, :is_void, :is_locked, :zone_id)";

		try {

			// Creating map with all required params
			MapSqlParameterSource paramMap = new MapSqlParameterSource();

			paramMap.addValue("kiosk_number", k.getKioskNumber());
			paramMap.addValue("grid_location_row", k.getGridLocationRow());
			paramMap.addValue("grid_location_column", k.getGridLocationColumn());
			paramMap.addValue("is_void", k.getIsVoid());
			paramMap.addValue("is_locked", k.getIsLocked());
			paramMap.addValue("zone_id", k.getZoneID());

			status = namedJdbcTemplate.update(INSERT_QUERY, paramMap, keyHolder, new String[] { "kiosk_id" });

			// get the auto generated primary key from the new customer insertion
			Number generatedComID = keyHolder.getKey();
			status = generatedComID.intValue();

			System.out.println("new zone ID = " + status);

		} catch (Exception e) {

		} // try

		return status;
	} // add kiosk

	@Override
	public int deleteKiosk(int kioskID) {
		int status = 0;

		final String DELETE_QUERY = "update kiosk set is_archived = 1 where kiosk_id = ?"; //"delete from kiosk where kiosk_id = ?";

		// define query arguments
		Object[] params = { kioskID };

		status = jdbcTemplate.update(DELETE_QUERY, params);
		System.out.println("delete status = " + status);

		return status;

	} // deleteKiosk

	@Override
	public int updateKiosk(CreateKiosk k) {

		NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
		int status = 0;

		String UPDATE_QUERY = "update kiosk set kiosk_number = :kiosk_number, "
				+ "grid_location_row = :grid_location_row, grid_location_column = :grid_location_column, "
				+ "is_void = :is_void, is_locked = :is_locked, zone_id = :zone_id where kiosk_id = :kiosk_id";

		try {

			// Adding params using MapSqlParameterSource class
			SqlParameterSource namedParameters = new MapSqlParameterSource()
					.addValue("kiosk_number", k.getKioskNumber()).addValue("grid_location_row", k.getGridLocationRow())
					.addValue("grid_location_column", k.getGridLocationColumn()).addValue("is_void", k.getIsVoid())
					.addValue("is_locked", k.getIsLocked()).addValue("zone_id", k.getZoneID())
					.addValue("kiosk_id", k.getKioskID());

			status = namedJdbcTemplate.update(UPDATE_QUERY, namedParameters);

			if (status != 0) {
				System.out.println("Kiosk data updated for ID " + k.getKioskID());
			} else {
				System.out.println("Kiosk data failed to update for " + k.getKioskID());
			}

		} catch (Exception e) {

		} // try
		return status;

	} // updateKiosk

	@Override
	public List<Location> getAllLocations() {

		String query = "SELECT * FROM location where is_archived = 0";
		List<Location> locationList = new ArrayList<>();
		List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();

		try {

			rows = jdbcTemplate.queryForList(query);

			for (Map<String, Object> row : rows) {

				Location l = new Location();

				l.setLocationID((Integer) (row.get("location_id")));
				l.setLocationName((String) (row.get("location_name")));
				l.setLocationArea((String) (row.get("location_area")));
				l.setMapURL((String) (row.get("mapURL")));
				
				l.setLocationName(l.getLocationName().toUpperCase());

				locationList.add(l);
			}

		} catch (Exception e) {

		} // try

		return locationList;
	}

	@Override
	public int addDiscount(CreateLocation l, int i, int k) throws Exception {
		// TODO Auto-generated method stub
		NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
		final KeyHolder keyHolder = new GeneratedKeyHolder();
		int status = 0;

		final String INSERT_QUERY = "insert into discount (rate_id, from_month, to_month, percentage) values"
				+ " (:rate_id, :from_month, :to_month, :percentage)";

		try {

			// Creating map with all required params
			MapSqlParameterSource paramMap = new MapSqlParameterSource();
			
			System.out.println("rate id = " + l.getRateDiscountList().get(i).getDiscountList().get(k).getRate_id());
			System.out.println("from_month = " + l.getRateDiscountList().get(i).getDiscountList().get(k).getFromMonth());
			System.out.println("to_month = " + l.getRateDiscountList().get(i).getDiscountList().get(k).getToMonth());
			System.out.println("percentage = " + l.getRateDiscountList().get(i).getDiscountList().get(k).getPercentageDiscount());

			paramMap.addValue("rate_id", l.getRateDiscountList().get(i).getRateID());
			paramMap.addValue("from_month", l.getRateDiscountList().get(i).getDiscountList().get(k).getFromMonth()); 
			paramMap.addValue("to_month", l.getRateDiscountList().get(i).getDiscountList().get(k).getToMonth()); 
			paramMap.addValue("percentage", l.getRateDiscountList().get(i).getDiscountList().get(k).getPercentageDiscount()); 


			status = namedJdbcTemplate.update(INSERT_QUERY, paramMap, keyHolder, new String[] { "discount_id" });

			// get the auto generated primary key from the new customer insertion
			Number generatedComID = keyHolder.getKey();
			status = generatedComID.intValue();

			System.out.println("new discount ID = " + status);

		} catch (Exception e) {

		} // try

		return status;
	} // addDiscount

	@Override
	public int addDiscountByZone(CreateZone z, int k) throws Exception {
		// TODO Auto-generated method stub
		NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
		final KeyHolder keyHolder = new GeneratedKeyHolder();
		int status = 0;

		final String INSERT_QUERY = "insert into discount (rate_id, from_month, to_month, percentage) values"
				+ " (:rate_id, :from_month, :to_month, :percentage)";

		try {

			// Creating map with all required params
			MapSqlParameterSource paramMap = new MapSqlParameterSource();
			
			System.out.println("rate id = " + z.getRateAndDiscount().getRateID());
			System.out.println("from_month = " + z.getRateAndDiscount().getDiscountList().get(k).getFromMonth());
			System.out.println("to_month = " + z.getRateAndDiscount().getDiscountList().get(k).getToMonth());
			System.out.println("percentage = " + z.getRateAndDiscount().getDiscountList().get(k).getPercentageDiscount());

			paramMap.addValue("rate_id", z.getRateAndDiscount().getRateID());
			paramMap.addValue("from_month", z.getRateAndDiscount().getDiscountList().get(k).getFromMonth()); 
			paramMap.addValue("to_month", z.getRateAndDiscount().getDiscountList().get(k).getToMonth()); 
			paramMap.addValue("percentage", z.getRateAndDiscount().getDiscountList().get(k).getPercentageDiscount()); 


			status = namedJdbcTemplate.update(INSERT_QUERY, paramMap, keyHolder, new String[] { "discount_id" });

			// get the auto generated primary key from the new customer insertion
			Number generatedComID = keyHolder.getKey();
			status = generatedComID.intValue();

			System.out.println("new discount ID = " + status);

		} catch (Exception e) {

		} // try

		return status;
	} //addDiscountByZone

	@Override
	public int deleteDiscount(int rateID) throws Exception {
		int status = 0;

		final String DELETE_QUERY =  "delete from discount where rate_id = ?";// "delete from location where location_id = ?";

		// define query arguments
		Object[] params = { rateID };

		status = jdbcTemplate.update(DELETE_QUERY, params);
		System.out.println("delete status = " + status);

		return status;
	} // delete discount

	@Override
	public int addMap(int locationID, String mapURL) throws Exception {
		int status = 0;

		final String UPDATE_QUERY = "update location set mapURL = ? where location_id = ?";

		// define query arguments
		Object[] params = { mapURL, locationID };

		status = jdbcTemplate.update(UPDATE_QUERY, params);
		System.out.println("update status = " + status);

		return status;
	} // addMap
	
	

} // LocationDAOIMpl
