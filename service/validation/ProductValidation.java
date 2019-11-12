package com.eden.api.service.validation;

import java.io.File;
import java.io.IOException;

import org.apache.tomcat.util.http.fileupload.FileUtils;

import com.edenstar.model.dash.CreateProduct;

public class ProductValidation {
	
	public boolean fieldsAreEmpty(CreateProduct p) {

		// check to make sure that the mandatory parameters are not empty
		if (p.getDescription().contentEquals("") || p.getOrigin().contentEquals("")
				|| p.getPriceRange().contentEquals("") || p.getStaff_email().contentEquals("") ||
				p.getCompanyID() < 1) {

			return true;

		} // if

		return false;

	} // fieldsAreEmpty
	
	public boolean isNull(CreateProduct p) {
		// make sure that none of the mandatory fields are null

			if (p.getDescription() == null)
				return true;
			if (p.getOrigin() == null)
				return true;
			if (p.getStaff_email() == null)
				return true;
			if(p.getPriceRange() == null)
				return true;


		return false;

	} // isNull
	
	public void deleteDirectory(String companyName, int productID) throws IOException {

		String myPath = System.getProperty("user.dir"); 
		companyName = companyName.replace(" ", "_");
		companyName = companyName.replace("@", "_");
		
		System.out.println("my path is " + myPath);
		myPath = myPath + "/product_photos/" + companyName.toLowerCase() + "/" + productID;
		System.out.println("my path is " + myPath);
		try {
			FileUtils.deleteDirectory(new File(myPath));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	} // deleteDirectory


} // ProductValidation
