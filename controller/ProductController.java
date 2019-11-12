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

import com.eden.api.service.CustomerService;
import com.eden.api.service.EngineService;
import com.eden.api.service.validation.ProductValidation;
import com.eden.api.util.Constants;
import com.eden.api.util.Response;
import com.eden.api.util.ResponseEnum;
import com.edenstar.model.Company;
import com.edenstar.model.Product;
import com.edenstar.model.ProductPhoto;
import com.edenstar.model.dash.CreateProduct;
import com.edenstar.model.dash.GetProductDetails;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class ProductController extends BaseController {

	Response response = new Response();

	ProductValidation prodValid = new ProductValidation();

	@Autowired
	private CustomerService customerService;

	@Autowired
	private EngineService cpu;

	// **************************************************************************************
	// add a product for a existing company
	// **************************************************************************************
	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_DASH
			+ Constants.PATH_ADD_PROD, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response addProduct(@RequestBody CreateProduct p) {

		try {

			// make sure none of the mandatory fields are null
			if (prodValid.isNull(p) == true) {
				response = Response.build("Error",
						"mandatory parameters : [staff_email / description / origin / priceRange / companyID ] cannot be null",
						false);
				return response;
			} // isNull check

			// check to see if any of the mandatory fields are empty
			if (prodValid.fieldsAreEmpty(p) == true) {
				response = Response.build("Error",
						"no data entered for mandatory parameters, either [staff_email / description / orgin / priceRange / companyID ]",
						false);
				return response;
			} // isEmpty check

			// check to see if the staff user exists on the database
			if (!cpu.userExists(p.getStaff_email())) {
				response = Response.build("Error", "staff email id " + p.getStaff_email() + " does not exist", false);
				return response;
			} // userExists

			// we need to check if the company exists

			if (!cpu.companyExists(p.getCompanyID())) {

				response = Response.build("Failure", "product could not be added, companyID does not exist", false);
				return response;
			} // companyExists

			System.out.println("adding product to database ...");

			// if the company exists we create a new product under the company id number

			System.out.println("company_ID = " + p.getCompanyID());

			// we now create a product under the company id
			int product_id = customerService.addProduct(p);

			if (product_id > 0) {
				response = Response.build("Success", "Product successfully added to the database", true);
				p.setProductID(product_id);
				response.setData(p);
				// return response;
			} else {
				response = Response.build("Failure", "Product could not be added to the database", false);
				return response;
			} // nested if

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}
		return response;

	} // add a product

	// **************************************************************************************
	// DELETE a product and all associated photos from the database
	// **************************************************************************************
	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_DASH
			+ Constants.PATH_DELETE_PROD, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response deleteProduct(@RequestBody CreateProduct p) {

		try {
			// make sure that the staff_email and product_ID are not null
			if (p.getStaff_email() == null || (p.getProductID() < 1)) {
				response = Response.build("Error", "Mandatory fields, staff_email and/or productID, cannot be null",
						false);
				return response;
			} // null fields

			// check to see if the fields are empty
			if (p.getStaff_email().contentEquals("") || p.getProductID() < 1) {
				response = Response.build("Error", "Mandatory fields : [productID / staff_email] cannot be empty",
						false);
				return response;
			} // empty fields

			// check to see if the staff user exists on the database
			if (!cpu.userExists(p.getStaff_email())) {
				response = Response.build("Error", "staff email id " + p.getStaff_email() + " does not exist", false);
				return response;
			} // staff exists

			// check if the staff user has clearance to perform this action
			if (cpu.checkClearance(p.getStaff_email().toString()) == false) {

				// insufficient priviledges
				response = Response.build("Access Denied",
						"Insufficient privileges - you must be either be admin or manager_admin to perform this action",
						false);
				return response;

			} // clearance check

			System.out.println("you are here ---> productID = " + p.getProductID());

			// we now have to check if the product exists on the database
			if (cpu.productExists(p.getProductID())) {
				// delete procedures

				// if there are any product photos listed, we need to delete the corresponding
				// product photo directory
				List<ProductPhoto> prodPhotoList = new ArrayList<ProductPhoto>();
				prodPhotoList = customerService.getProductPhoto(p.getProductID());

				if (prodPhotoList.size() == 0) {

				} else {

					// we need to obtain the company name from the productID ...
					Product prod = new Product();
					prod = customerService.getProduct(p.getProductID());
					Company comp = new Company();
					comp = customerService.getCompany(prod.getCompanyID());
					System.out.println("company name = " + comp.getCompanyName());
					prodValid.deleteDirectory(comp.getCompanyName(), p.getProductID());

				} // delete product photos directory

				int status = customerService.deleteProduct(p.getProductID());

				if (status == 1) {
					response = Response.build("Success", "Product and all associated records (product photos)"
							+ "  have successfully been deleted from the database", true);
					return response;

				} else {
					response = Response.build("Failure", "Product could not be deleted from the database", false);
					return response;

				} // nested if

			} else {
				// Product does not exist
				response = Response.build("Error", "Product does not exist on the database", false);
				return response;

			} // if

		} catch (

		Exception e) {

			e.printStackTrace();
		} // try-catch
		return response;

	} // deleteProduct

	public CreateProduct checkChanges(CreateProduct p) {

		// gets the product record from database and compares
		// to see if the new data is null, and if so copy the database value over

		// get the record
		Product r = new Product();

		try {
			r = customerService.getProduct(p.getProductID());

			// the company ID and product ID remain the same.
			p.setCompanyID(r.getCompanyID());

			if (p.getDescription() == null)
				p.setDescription(r.getDescription().toString());

			if (p.getOrigin() == null)
				p.setOrigin(r.getOrigin().toString());

			if (p.getPriceRange() == null)
				p.setPriceRange(r.getPriceRange().toString());

		} catch (Exception e) {
			e.printStackTrace();
		} // try-catch

		return p;

	} // checkChanges

	// **************************************************************************************
	// get all products from the database relating to a company id
	// **************************************************************************************
	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_DASH
			+ Constants.PATH_GETALL_PROD, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response getProducts(@RequestBody CreateProduct p) {

		List<Product> productList = new ArrayList<Product>();

		try {

			// make sure that the staff_email and emailID are not null
			if (p.getStaff_email() == null || p.getCompanyID() == 0) {
				response = Response.build("Error", "Mandatory field staff_email / companyID cannot be null", false);
				return response;
			} // null fields

			// check to see if the fields are empty
			if (p.getStaff_email().contentEquals("") || p.getCompanyID() == 0) {
				response = Response.build("Error", "Mandatory field : staff_email / companyID cannot be empty", false);
				return response;
			} // empty fields

			// check to see if the staff user exists on the database
			if (!cpu.userExists(p.getStaff_email())) {
				response = Response.build("Error",
						"Staff email " + p.getStaff_email() + " does not exist on the database", false);
				return response;
			}

			// get all the products related to company ID ...

			productList = customerService.getProducts(p.getCompanyID());

			// check to see if there any products stored under the company
			if (productList.isEmpty()) {
				response = Response.build("Error", "There are no products stored under this company", false);
				return response;
			}

			response = Response.build(ResponseEnum.OK.getStatus(), Constants.SUCCESS, ResponseEnum.OK.getMessage(),
					true);
			response.setData(productList);

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // getProducts

	// **************************************************************************************
	// update a product information to the database
	// **************************************************************************************
	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_DASH
			+ Constants.PATH_UPDATE_PROD, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response updateProduct(@RequestBody CreateProduct p) {

		try {

			// check to see if the staff user exists on the database
			if (!cpu.userExists(p.getStaff_email())) {
				response = Response.build("Error", "staff email id " + p.getStaff_email() + " does not exist", false);
				return response;
			}

			// make sure that the customer exists on database ...
			if (cpu.productExists(p.getProductID())) {

				// if the product exists, we just update it with the amended details
				// first we must make sure that any null fields are replaced with database
				// values
				System.out.println(" product " + p.getProductID() + " exists !");

				// check to see if there are any changes against the database value and copy
				// them over
				p = checkChanges(p);

				System.out.println("updating product information for product_id = " + p.getProductID());
				int status = customerService.updateProduct(p);

				System.out.println("status = " + status);

				if (status == 1) {
					response = Response.build("Success", "product details successfully updated to the database", true);
					response.setData(p);
				} else {
					response = Response.build("Error", "product details could not be updated to the database", false);
					return response;
				} // nested if

			} else {
				// if the customer does not exist, throw an error
				response = Response.build("Error", "product does not exist on the database", false);
				return response;

			} // if

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // updateProduct

	// **************************************************************************************
	// get product infomation by product id
	// **************************************************************************************
	@RequestMapping(value = Constants.BASE_PATH + Constants.PATH_DASH
			+ Constants.PATH_GET_PROD, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response getProduct(@RequestBody CreateProduct p) {

		List<ProductPhoto> productPhotoList = new ArrayList<ProductPhoto>();

		try {

			// make sure that the staff_email and emailID are not null
			if (p.getStaff_email() == null || p.getProductID() == 0) {
				response = Response.build("Error", "Mandatory field staff_email / productID cannot be null", false);
				return response;
			} // null fields

			// check to see if the fields are empty
			if (p.getStaff_email().contentEquals("") || p.getProductID() == 0) {
				response = Response.build("Error", "Mandatory field : staff_email / productID cannot be empty", false);
				return response;
			} // empty fields

			// check to see if the staff user exists on the database
			if (!cpu.userExists(p.getStaff_email())) {
				response = Response.build("Error",
						"Staff email " + p.getStaff_email() + " does not exist on the database", false);
				return response;
			}

			// check to see if there any products stored under the company
			if (!cpu.productExists(p.getProductID())) {
				response = Response.build("Error", "Product with ID" + p.getProductID() + " does not exist", false);
				return response;
			}

			// create get product data object
			GetProductDetails productDetails = new GetProductDetails();

			Product product = customerService.getProduct(p.getProductID());
			productDetails.setCompanyID(product.getCompanyID());
			productDetails.setDescription(product.getDescription());
			productDetails.setOrigin(product.getOrigin());
			productDetails.setPriceRange(product.getPriceRange());

			// get all the product photo details based on productID ...

			productPhotoList = customerService.getProductPhoto(p.getProductID()); // getProducts(p.getCompanyID());

			if (productPhotoList.size() == 0) {

			} else {
				productDetails.setPhotoList(productPhotoList);
			}

			response = Response.build(ResponseEnum.OK.getStatus(), Constants.SUCCESS, ResponseEnum.OK.getMessage(),
					true);

			response.setData(productDetails);

		} catch (

		Exception e) {
			response = Response.build(ResponseEnum.INTERNAL_SERVER_ERROR.getStatus(), Constants.INTERNAL_SYSTEM_ERROR,
					ResponseEnum.INTERNAL_SERVER_ERROR.getMessage(), false);
			e.printStackTrace();
		}

		return response;

	} // getProduct

} // ProductController
