package com.eden.api.controller;

import java.sql.Date;
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
import com.eden.api.service.AccountService;
import com.eden.api.service.CustomerService;
import com.eden.api.service.EngineService;
import com.eden.api.service.LocationService;
import com.eden.api.service.UserService;
import com.eden.api.service.validation.ReportsValidation;
import com.eden.api.util.Constants;
import com.eden.api.util.Response;
import com.eden.api.util.ResponseEnum;
import com.edenstar.model.Customer;
import com.edenstar.model.Location;
import com.edenstar.model.User;
import com.edenstar.model.Zone;
import com.edenstar.model.reports.ProcessReport;
import com.edenstar.model.reports.ReportStream;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class ReportsController extends BaseController {

	Response response = new Response();
	Availability availability = new Availability();
	ReportsValidation repValid = new ReportsValidation();

	@Autowired
	private EngineService cpu;

	@Autowired
	private AccountService accountService;

	@Autowired
	private LocationService locationService;

	@Autowired
	private UserService userService;

	@Autowired
	private CustomerService customerService;

	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_REPORT
			+ Constants.PATH_GET_REPORTS, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response getReport(@RequestBody ProcessReport r) {

		List<ReportStream> reportStreamOut = new ArrayList<>();

		try {

			if (repValid.isNull(r, "")) {
				response = Response.build("Error",
						"Mandatory parameters : [ staff_email_id / reportViewMode ] cannot be null or empty", false);
				return response;
			} // isNull check

			if (repValid.isNull(r, "dates")) {
				response = Response.build("Error",
						"Mandatory parameters : [ report_start_date / report_end_date ] cannot be null or empty",
						false);
				return response;
			} // isNull check

			// check to see if the staff user exists on the database
			if (!cpu.userExists(r.getStaff_email_id())) {
				response = Response.build("Error",
						"Staff email " + r.getStaff_email_id() + " does not exist on the database", false);
				return response;
			} // staff checks

			// only managers and accounts personnel can view a customer's account
			if (!cpu.checkAccountClearance(r.getStaff_email_id())) {
				response = Response.build("Denied",
						"Employee ID " + r.getStaff_email_id() + " does not have manager or accounts clearance", false);
				return response;
			}

			if (r.getReport_start_date() != null) {

				String dateError = repValid.checkDates(r.getReport_start_date());
				if (dateError != null) {
					response = Response.build("Error", "Report start date : " + dateError, false);
					return response;
				}
			}

			if (r.getReport_end_date() != null) {

				String dateError = repValid.checkDates(r.getReport_end_date());
				if (dateError != null) {
					response = Response.build("Error", "Report end date : " + dateError, false);
					return response;
				}
			}

			// we need to convert the dates into sql string dates
			Availability availability = new Availability();
			Date reportStartDate = availability.formatDateForDB(r.getReport_start_date());
			Date reportEndDate = availability.formatDateForDB(r.getReport_end_date());

			r.setReportStartDate(reportStartDate);
			r.setReportEndDate(reportEndDate);

			// we have cleared validation, we need to obtain the account list for the
			// employee if

			switch (r.getReportViewMode().toLowerCase()) {

			case "bookings_location":
				System.out.println("Bookings by a location");
				// check if the location exists
				Location location = locationService.getLocation(r.getField_id());
				if (location.getLocationID() == 0) {
					response = Response.build("Error", "Location id : " + r.getField_id() + " does not exist", false);
					return response;
				}
				reportStreamOut = accountService.getReport("bookings_location", r.getField_id(), r.getReportStartDate(),
						r.getReportEndDate());

				if (!reportStreamOut.isEmpty()) {
					reportStreamOut.get(0).getLocation_peformance().setLocationArea(location.getLocationArea());
					reportStreamOut.get(0).getLocation_peformance().setLocationName(location.getLocationName());
					reportStreamOut.get(0).getLocation_peformance().setLocationID(r.getField_id());
					reportStreamOut.get(0).setReport_title("Bookings and sales by location");
					reportStreamOut.get(0).setReport_date_from(r.getReport_start_date());
					reportStreamOut.get(0).setReport_date_to(r.getReport_end_date());
					reportStreamOut.get(0).getLocation_peformance().setMapURL(location.getMapURL());
				}

				break;

			case "bookings_all_locations":
				System.out.println("Bookings by all locations");
				reportStreamOut = accountService.getReport("bookings_all_locations", r.getField_id(),
						r.getReportStartDate(), r.getReportEndDate());

				if (!reportStreamOut.isEmpty()) {
					for (int i = 0; i < reportStreamOut.size(); i++) {
						reportStreamOut.get(i).setReport_title("Bookings and sales by all locations");
						reportStreamOut.get(i).setReport_date_from(r.getReport_start_date());
						reportStreamOut.get(i).setReport_date_to(r.getReport_end_date());
					}
				}

				break;

			case "bookings_employee":
				System.out.println("Bookings by sales employee");
				// check if the location exists
				User salesEmployee = userService.getUserDetailsByEmpID(r.getField_id());
				if (salesEmployee.getEmployeeID() == 0) {
					response = Response.build("Error", "Employee id : " + r.getField_id() + " does not exist", false);
					return response;
				}
				reportStreamOut = accountService.getReport("bookings_employee", r.getField_id(), r.getReportStartDate(),
						r.getReportEndDate());

				if (!reportStreamOut.isEmpty()) {

					reportStreamOut.get(0).getSales_performance().setEmployeeID(r.getField_id());
					reportStreamOut.get(0).getSales_performance().setFirstName(salesEmployee.getFirstName());
					reportStreamOut.get(0).getSales_performance().setLastName(salesEmployee.getLastName());
					reportStreamOut.get(0).setReport_title("Bookings and sales by employee");
					reportStreamOut.get(0).setReport_date_from(r.getReport_start_date());
					reportStreamOut.get(0).setReport_date_to(r.getReport_end_date());

				}

				break;

			case "bookings_all_employees":
				System.out.println("Bookings and sales by all employees");

				reportStreamOut = accountService.getReport("bookings_all_employees", r.getField_id(),
						r.getReportStartDate(), r.getReportEndDate());

				if (!reportStreamOut.isEmpty()) {
					for (int i = 0; i < reportStreamOut.size(); i++) {
						reportStreamOut.get(i).setReport_title("Bookings and sales by all employees");
						reportStreamOut.get(i).setReport_date_from(r.getReport_start_date());
						reportStreamOut.get(i).setReport_date_to(r.getReport_end_date());
					}
				}

				break;

			case "bookings_customer":
				System.out.println("Bookings by customer");
				// check if the location exists
				Customer customer = customerService.getCustomerDetails(r.getField_id());
				if (customer.getCustomerID() == 0) {
					response = Response.build("Error", "Customer id : " + r.getField_id() + " does not exist", false);
					return response;
				}
				reportStreamOut = accountService.getReport("bookings_customer", r.getField_id(), r.getReportStartDate(),
						r.getReportEndDate());

				if (!reportStreamOut.isEmpty()) {

					reportStreamOut.get(0).getCustomer_revenue().setCustomerID(customer.getCustomerID());
					reportStreamOut.get(0).getCustomer_revenue().setFirstName(customer.getFirstName());
					reportStreamOut.get(0).getCustomer_revenue().setLastName(customer.getLastName());
					reportStreamOut.get(0).setReport_title("Bookings by customer");
					reportStreamOut.get(0).setReport_date_from(r.getReport_start_date());
					reportStreamOut.get(0).setReport_date_to(r.getReport_end_date());

				}

				break;

			case "bookings_all_customers":
				System.out.println("Bookings and sales revenue by all customers");

				reportStreamOut = accountService.getReport("bookings_all_customers", r.getField_id(),
						r.getReportStartDate(), r.getReportEndDate());

				if (!reportStreamOut.isEmpty()) {
					for (int i = 0; i < reportStreamOut.size(); i++) {
						reportStreamOut.get(i).setReport_title("Bookings and sales revenue by all customers");
						reportStreamOut.get(i).setReport_date_from(r.getReport_start_date());
						reportStreamOut.get(i).setReport_date_to(r.getReport_end_date());
					}
				}

				break;

			case "bookings_all_zones":
				System.out.println("Bookings and sales by all zones");

				reportStreamOut = accountService.getReport("bookings_all_zones", r.getField_id(),
						r.getReportStartDate(), r.getReportEndDate());

				if (!reportStreamOut.isEmpty()) {
					for (int i = 0; i < reportStreamOut.size(); i++) {
						reportStreamOut.get(i).setReport_title("Bookings and sales revenue by all customers");
						reportStreamOut.get(i).setReport_date_from(r.getReport_start_date());
						reportStreamOut.get(i).setReport_date_to(r.getReport_end_date());
					}
				}

				break;

			case "bookings_kiosks_by_zone":
				System.out.println("Bookings and sales revenue by all customers");

				// check to see if the zone id exists
				Zone zone = locationService.getZone(r.getField_id());
				if (zone.getZoneID() == 0) {
					response = Response.build("Error", "Zone id : " + r.getField_id() + " does not exist", false);
					return response;
				}

				reportStreamOut = accountService.getReport("bookings_kiosks_by_zone", r.getField_id(),
						r.getReportStartDate(), r.getReportEndDate());

				if (!reportStreamOut.isEmpty()) {
					for (int i = 0; i < reportStreamOut.size(); i++) {
						reportStreamOut.get(i).setReport_title(
								"Bookings and sales revenue by kiosks under zone id " + r.getField_id());
						reportStreamOut.get(i).setReport_date_from(r.getReport_start_date());
						reportStreamOut.get(i).setReport_date_to(r.getReport_end_date());
					}
				}

			default:

			} // switch

			response = Response.build("Success", reportStreamOut.size() + " report(s) successfully retrieved", true);
			// System.out.println("report = " + reportStreamOut.toString());
			response.setData(reportStreamOut);

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}
		return response;

	} // getAccounts

} // reportsController
