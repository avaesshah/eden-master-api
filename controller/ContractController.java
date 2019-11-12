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
import com.eden.api.service.BookingService;
import com.eden.api.service.EngineService;
import com.eden.api.service.QueryService;
import com.eden.api.service.validation.LeaseValidation;
import com.eden.api.service.validation.ZoneValidation;
import com.eden.api.util.Constants;
import com.eden.api.util.Response;
import com.eden.api.util.ResponseEnum;
import com.edenstar.model.Lease;
import com.edenstar.model.booking.BookingStream;
import com.edenstar.model.booking.LeaseStream;
import com.edenstar.model.booking.ProcessLease;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class ContractController extends BaseController {

	Response response = new Response();
	Availability availability = new Availability();
	LeaseValidation leaValid = new LeaseValidation();

	ZoneValidation zoneValid = new ZoneValidation();

	@Autowired
	private EngineService cpu;

	@Autowired
	private QueryService bpu;

	@Autowired
	private BookingService bookingService;

	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_BOOKING
			+ Constants.PATH_GET_LEASES, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response getLeases(@RequestBody ProcessLease l) {

		List<LeaseStream> contractList = new ArrayList<>();

		try {
			if (leaValid.isNull(l, "")) {
				response = Response.build("Error", "Mandatory parameters : [staff_email_id] cannot be null", false);
				return response;
			} // isNull check

			// check to see if any of the mandatory fields are empty
			if (leaValid.fieldsAreEmpty(l, "")) {
				response = Response.build("Error", "No data entered for mandatory parameters, either [staff_email_id]",
						false);
				return response;
			} // isEmpty check

			// check to see if the staff user exists on the database
			if (!cpu.userExists(l.getStaff_email_id())) {
				response = Response.build("Error",
						"Staff email " + l.getStaff_email_id() + " does not exist on the database", false);
				return response;
			} // staff checks

			if (l.getLeaseViewMode() == null || l.getLeaseViewMode().isEmpty()) {
				response = Response.build("Error", "leaseViewMode cannot be null", false);
				return response;
			}

			// we have cleared validation, we need to obtain the application list for the
			// employee if

			switch (l.getLeaseViewMode().toLowerCase()) {
			case "*":
				System.out.println("All contracts");
				contractList = bookingService.getAllLeaseList("*");
				break;

			case "pending":
				System.out.println("All contracts pending signing and uploading");
				contractList = bookingService.getAllLeaseList("pending");
				break;

			case "expired":
				System.out.println("All contracts that have expired");
				contractList = bookingService.getAllLeaseList("expired");
				break;

			case "signed":
				System.out.println("All contracts that have signed");
				contractList = bookingService.getAllLeaseList("signed");
				break;

			default:

				System.out.println("get contract by id");
				String leaseIDStr = l.getLeaseViewMode();
				int lease_id = Integer.parseInt(leaseIDStr);

				if (lease_id < 1) {
					response = Response.build("Error", "please specify a valid lease_id", false);
					return response;
				}

				Lease contract = bookingService.getLease(lease_id);

				if (contract.getLease_id() == 0) {
					response = Response.build("Error",
							"Contract with lease_id " + lease_id + " does not exist on database", false);
					return response;
				}

				response = Response.build("Success", "Contract with lease ID " + lease_id + " successfully retrieved",
						true);
				response.setData(contract);
				return response;

			} // switch

			response = Response.build("Success", contractList.size() + " contracts successfully retrieved", true);
			response.setData(contractList);

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // getLease

	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_BOOKING
			+ Constants.PATH_ADD_COMMENT_TO_LEASE, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response addComments(@RequestBody ProcessLease l) {

		try {
			if (leaValid.isNull(l, "comment")) {
				response = Response.build("Error",
						"Mandatory parameters : [staff_email_id / comments / lease_id] cannot be null", false);
				return response;
			} // isNull check

			// check to see if any of the mandatory fields are empty
			if (leaValid.fieldsAreEmpty(l, "comment")) {
				response = Response.build("Error",
						"No data entered for mandatory parameters, either [staff_email_id / comments]", false);
				return response;
			} // isEmpty check

			// check to see if the staff user exists on the database
			if (!cpu.userExists(l.getStaff_email_id())) {
				response = Response.build("Error",
						"Staff email " + l.getStaff_email_id() + " does not exist on the database", false);
				return response;
			} // staff checks

			// check that tha application exists
			Lease lease = bookingService.getLease(l.getLease_id());
			if (lease.getLease_id() == 0) {
				response = Response.build("Error", "Lease ID " + l.getLease_id() + " does not exist on the database",
						false);
				return response;
			} // leaseExists

			String previousComments = lease.getComments();
			if (previousComments == null)
				previousComments = "";
			bpu.addLeaseComment(lease.getLease_id(), l.getComments(), previousComments, l.getStaff_email_id());

			response = Response.build("Success", "Comments added to lease ID " + l.getLease_id() + " successfully",
					true);

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // addCommentTOLease

	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_BOOKING
			+ Constants.PATH_UPLOAD_CONTRACT, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response uploadContract(@RequestBody ProcessLease l) {

		try {

			if (leaValid.isNull(l, "contract")) {
				response = Response.build("Error",
						"Mandatory parameters : [staff_email_id / contract_upload / lease_id] cannot be null", false);
				return response;
			} // isNull check

			// check to see if any of the mandatory fields are empty
			if (leaValid.fieldsAreEmpty(l, "contract")) {
				response = Response.build("Error",
						"No data entered for mandatory parameters, either [staff_email_id / contract_upload]", false);
				return response;
			} // isEmpty check

			// check to see if the staff user exists on the database
			if (!cpu.userExists(l.getStaff_email_id())) {
				response = Response.build("Error",
						"Staff email " + l.getStaff_email_id() + " does not exist on the database", false);
				return response;
			} // staff checks

			// check that tha application exists
			Lease lease = bookingService.getLease(l.getLease_id());
			if (lease.getLease_id() == 0) {
				response = Response.build("Error", "Lease ID " + l.getLease_id() + " does not exist on the database",
						false);
				return response;
			} // leaseExists

			// we now have the scanned contract, we shall upload it to the server
			// we need to obtain the booking ref to create the file
			List<BookingStream> booking = bookingService.getBookingByField("booking", lease.getBooking_id());
			String booking_ref = "contract";

			if (booking.size() == 0) {

			} else {
				booking_ref = booking.get(0).getBooking_ref();
			}

			String urlToContractScan = "";

			if (l.getContract_upload() == null) {

			} else {
				urlToContractScan = leaValid.writeImageStreamToFile(l.getContract_upload(), booking_ref);
				System.out.println("the contract scan url is = " + urlToContractScan);
				l.setContract_upload_url(urlToContractScan);
				l.setContract_upload(null);
			}

			// now we update the application with the deposit url
			int updateStatus = bookingService.updateContractURL(l.getLease_id(), urlToContractScan);

			if (updateStatus == 0) {
				response = Response.build("Failure", "Contract URL was not added to the database", false);
				return response;
			} else {
				response = Response.build("Success", "Contract URL was successfully added to the database", true);
			}

			String previousComments = "";

			if (booking.isEmpty()) {

			} else {
				previousComments = booking.get(0).getComments();
			}
			String previousLeaseComments = lease.getComments();

			bpu.addBookingComment(lease.getBooking_id(), "CONTRACT UPLOADED", previousComments, "");
			bpu.addLeaseComment(lease.getLease_id(), "CONTRACT UPLOADED", previousLeaseComments, "");
		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // uploadContract

	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_BOOKING
			+ Constants.PATH_GENERATE_CONTRACT, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response generateContract(@RequestBody ProcessLease l) {

		try {

			if (leaValid.isNull(l, "generate")) {
				response = Response.build("Error",
						"Mandatory parameters : [staff_email_id / lease_id] cannot be null", false);
				return response;
			} // isNull check

			// check to see if any of the mandatory fields are empty
			if (leaValid.fieldsAreEmpty(l, "contract")) {
				response = Response.build("Error",
						"No data entered for mandatory parameters, either [staff_email_id / lease_id]", false);
				return response;
			} // isEmpty check

			// check to see if the staff user exists on the database
			if (!cpu.userExists(l.getStaff_email_id())) {
				response = Response.build("Error",
						"Staff email " + l.getStaff_email_id() + " does not exist on the database", false);
				return response;
			} // staff checks

			// check that tha application exists
			Lease lease = bookingService.getLease(l.getLease_id());
			if (lease.getLease_id() == 0) {
				response = Response.build("Error", "Lease ID " + l.getLease_id() + " does not exist on the database",
						false);
				return response;
			} // leaseExists

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // generateContract

} // Contract Controller
