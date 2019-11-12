package com.eden.api.util;

public class Constants {

	private Constants() {
		throw new IllegalStateException("Utility class");
	}
	
	public static final String eden_version = "1.0 alpha";

	public static final String BASE_PATH = "/edenapi";

	// login url functions
	public static final String PATH_LOGIN = "/login"; // login url
	public static final String PATH_RESET = "/reset"; // email lost password url
	public static final String PATH_CHANGE_PWORD = "/change"; // change password

	// dashboard url functions
	public static final String PATH_DASH = "/dash"; // dash root url
	public static final String PATH_ADD_USER = "/adduser"; // add user url
	public static final String PATH_UPDATE_USER = "/updateuser"; // update user details
	public static final String PATH_GET_USER = "/getuser"; // get a user details
	public static final String PATH_DELETE_USER = "/deleteuser"; // delete a user details

	// customer url functions
	public static final String PATH_ADD_CUST = "/addcustomer"; // add customer url
	public static final String PATH_GET_CUST = "/getcustomer"; // get customer details
	public static final String PATH_DELETE_CUST = "/deletecustomer"; // delete customer and all associated entites
	public static final String PATH_UPDATE_CUST = "/updatecustomer"; // update customer details
	public static final String PATH_GETALL_CUST = "/getallcustomers"; // returns all the customers

	// company url functions
	public static final String PATH_ADD_COMP = "/addcompany"; // add additional
	public static final String PATH_DELETE_COMP = "/deletecompany"; // delete company and associated products
	public static final String PATH_GETALL_COMP = "/getallcompanies"; // get all companies
	public static final String PATH_UPDATE_COMP = "/updatecompany"; // update company information

	// product url functions
	public static final String PATH_ADD_PROD = "/addproduct"; // add additional product
	public static final String PATH_DELETE_PROD = "/deleteproduct"; // delete a product
	public static final String PATH_GETALL_PROD = "/getproducts"; // gets all the products under a company
	public static final String PATH_UPDATE_PROD = "/updateproduct"; // updates product information
	public static final String PATH_GET_PROD = "/getproduct"; // get product details

	// product photo url function
	public static final String PATH_ADD_PROD_PHOTO = "/addproductphoto"; // adds a product photo to product
	public static final String PATH_DELETE_PROD_PHOTO = "/deleteproductphoto"; // deletes a particular product photo
	public static final String PATH_UPDATE_PROD_PHOTO = "/updateproductphoto"; // update product photo
	public static final String PATH_GET_PROD_PHOTOS = "/getproductphotos"; // get all product photos for a product
	public static final String PATH_ADD_PROD_PHOTOS = "/addproductphotos"; // adds a muslitple product photos to product
	public static final String PATH_ADD_PROD_PHOTOS_ANDROID = "/addprodphotos"; // add multiple product photos for
																				// android

	// location url function
	public static final String PATH_ADD_LOC = "/addlocation"; // adds a new location;
	public static final String PATH_UPLOAD_MAP = "/uploadmap"; // upload location map
	public static final String PATH_GET_LOCATION = "/getlocation"; // get location information
	public static final String PATH_GET_LOC_ALL = "/getalllocations"; // returns a list of locations
	public static final String PATH_DELETE_LOC = "/deletelocation"; // delete location and associated entities
	public static final String PATH_UPDATE_LOC = "/updatelocation"; // update location details
	public static final String PATH_GET_LOCATION_BY_ITEM = "/getlocationfield"; // returns specific item under location id
	public static final String PATH_GET_GRID = "/getgrid"; // returns kiosk grid for a location

	// zone url functions
	public static final String PATH_ADD_ZONE = "/addzone"; // adds zone to existing location
	public static final String PATH_ADD_ZONE_RATE_DISCOUNT = "/addzonev2"; // add zone, rate and discount
	public static final String PATH_GET_ZONE = "/getzone"; // get all zone information
	public static final String PATH_DELETE_ZONE = "/deletezone"; // delete zone and all associated kiosks and the rate
	public static final String PATH_UPDATE_ZONE = "/updatezone"; // update zone information
	public static final String PATH_GET_KIOSKBYZONE = "/getkioskbyzone"; // returns the kiosks by zone id

	// rate url functions
	public static final String PATH_GET_RATE = "/getrate"; // returns the rate information
	public static final String PATH_UPDATE_RATE = "/updaterate"; // update rate information
	public static final String PATH_ADD_DISCOUNT = "/adddiscount"; // add discount to rate
	public static final String PATH_DELETE_DISCOUNT = "/deletediscount"; // delete a discount list from a rate

	// kiosk url functions
	public static final String PATH_GET_KIOSK = "/getkiosk"; // get kiosk information
	public static final String PATH_ADD_KIOSK = "/addkiosk"; // add kiosk to zone
	public static final String PATH_DELETE_KIOSK = "/deletekiosk"; // delete a kiosk
	public static final String PATH_UPDATE_KIOSK = "/updatekiosk"; // update kiosk information
	public static final String PATH_ASSIGN_ZONE = "/assignzone"; // assign zone to kiosk

	// quotation url functions
	public static final String PATH_BOOKING = "/booking";
	public static final String PATH_QUERY_LOCATION = "/querylocation"; // obtain booking availability and pricing for
																		// kioks in a location
	public static final String PATH_ADD_QUOTE = "/addquote"; // add a quote
	public static final String PATH_ADD_QUOTES = "/addquotes"; // add multiple quotes
	public static final String PATH_DELETE_QUOTE = "/deletequote"; // delete a quote
	public static final String PATH_GET_QUOTE = "/getquote"; // get a quotation from db
	public static final String PATH_QUERY_KIOSK = "/querykiosk"; // sends a booking query for one kiosk
	public static final String PATH_SUBMIT_QUOTE = "/submitquote"; // submit a quote for application
	public static final String PATH_GET_MANAGERS = "/getmanagers"; // get a list of managers and their employee number
	public static final String PATH_GET_CUSTOMER = "/getcustomer"; // returns customer information

	// managerial booking functions
	public static final String PATH_GET_APPLICATIONS = "/getapplications"; // get applications under manager's id
	public static final String PATH_VIEW_APPLICATION = "/viewapplication"; // view a booking application
	public static final String PATH_APPROVE_APPLICATION = "/approveapplication"; // approve booking application
	public static final String PATH_DELETE_APPLICATION = "/deleteapplication"; // delete an application
	public static final String PATH_LOCK_KIOSK = "/lockkiosk"; // locks/unlocks kiosk by manager
	public static final String PATH_DECLINE_APPLICATION = "/declineapplication"; // decline a booking application
	public static final String PATH_UPLOAD_DEPOSIT = "/uploaddeposit"; // upload scan of deposit
	public static final String PATH_GET_QUOTES = "/getquotes"; // get a list of quotes
	public static final String PATH_REVISE_APPLICATION = "/reviseapplication"; // make changes to the lease
	public static final String PATH_ADD_COMMENTS = "/addcommentsappl"; // add comments to application

	// booking functions
	public static final String PATH_VIEW_BOOKING = "/viewbooking"; // view all booking information
	public static final String PATH_GET_BOOKINGS = "/getbookings"; // get all bookings
	public static final String PATH_GET_BOOKINGS_BY_FIELD = "/getbookingsbyfield"; // get bookings by field id
	public static final String PATH_CANCEL_BOOKING = "/cancelbooking"; // cancel an active booking
	public static final String PATH_EMAIL_BOOKING = "/emailbooking"; // email booking to customer or any email address

	// lease functions
	public static final String PATH_CONTRACT = "/contract";
	public static final String PATH_GET_LEASES = "/getleases"; // get a list of leases
	public static final String PATH_ADD_COMMENT_TO_LEASE = "/addleasecomment"; // add a comment to the lease
	public static final String PATH_UPLOAD_CONTRACT = "/uploadcontract"; // updload a signed contract
	public static final String PATH_GENERATE_CONTRACT = "/generatecontract"; // generate word doc contract from template

	// account functions
	public static final String PATH_ACCOUNT = "/accounts";
	public static final String PATH_ADD_COMMENT_TO_ACCOUNT = "/addaccountcomment"; // add comments to account
	public static final String PATH_ADD_COMMENT_TO_PAYMENT = "/addpaymentcomment"; // add coments to payment
	public static final String PATH_VIEW_ACCOUNT = "/viewaccount"; // view a customer's account
	public static final String PATH_UPLOAD_DOCUMENT = "/uploaddocument"; // upload an accounting document
	public static final String PATH_UPDATE_ACCOUNT = "/updateaccount"; // update customer's account
	public static final String PATH_GET_ACCOUNT = "/getaccount"; // get a customer's account
	public static final String PATH_GET_PAYMENT = "/getpayment"; // gets data on a payment
	public static final String PATH_UPDATE_PAYMENT = "/updatepayment"; // update payment information
	public static final String PATH_RETRACT_PAYMENT = "/retractpayment"; // retracr a payment
	public static final String PATH_GET_ACCOUNTS = "/getaccounts"; // gets a list of accounts

	// report functions
	public static final String PATH_REPORT = "/reports";
	public static final String PATH_GET_REPORTS = "/getreport"; // get various reports

	public static final String PATH_PING = "/ping";

	public static final String INTERNAL_SYSTEM_ERROR = "00000-0";
	public static final String UNAUTHORIZED = "00000-1";
	public static final String INVALID_JSON = "00000-2";
	public static final String REQUIRED_PARAM = "00000-3";
	public static final String SUCCESS = "10000-1";

	// email server settings
	public static final String port = "587";// "465";
	public static final String host = "smtp.gmail.com";

	// availability status
	public static final String avail = "*** AVAILABLE ***";
	public static final String not_avail = "*** NOT AVAILABLE ***";
	public static final String part_avail = "*** PARTIALLY AVAILABLE ***";
	public static final String lock_avail = "*** LOCKED - AVAILABILITY PENDING ***";
	public static final String void_avail = "*** CLOSED - NOT AVAILABLE ***";

	public static final int avail_c = 4;
	public static final int not_avail_c = 2;
	public static final int part_avail_c = 3;
	public static final int lock_avail_c = 1;
	public static final int void_avail_c = 0;

	// availability colours
	public static final String avail_colour = "#1cc88a"; //"#0ee644";
	public static final String not_avail_colour = "#f02816";//"#ff271c";
	public static final String part_avail_colour = "#ff7f00"; //"#fffb05";
	public static final String lock_avail_colour = "#97979c"; //"#9d9da3";
	public static final String void_avail_colour = "#000000";

	// quotation
	public static final int quotation_valid_for_days = 30;

	// image file paths to image links conversion
	public static final String jouplenet = "http://jouple.net";
	public static final String jouplefilepath = "/home/jouplenet/public_html";

	// extend quotation expiry when submitted as an application
	public static final int extend_expiry_when_application = 30;
	public static final int application_valid_for_days = 30;

	// security deposit percentage
	public static final double securityDepositPercentage = 0.23672997;

	

	

	

	

} // Constants
