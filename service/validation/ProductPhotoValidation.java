package com.eden.api.service.validation;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;

import org.apache.tomcat.util.http.fileupload.IOUtils;

import com.eden.api.controller.engine.KeyGenerator;
import com.eden.api.util.Constants;
import com.edenstar.model.dash.CreateProductPhoto;

public class ProductPhotoValidation {

	public boolean fieldsAreEmpty(CreateProductPhoto p) {

		// check to make sure that the mandatory parameters are not empty
		if (p.getStaff_email().contentEquals("") || p.getDescription().contentEquals("") || p.getProductID() < 1) {

			return true;

		} // if

		return false;

	} // fieldsAreEmpty

	public boolean isNull(CreateProductPhoto p) {
		// make sure that none of the mandatory fields are null

		if ((p.getProductPhoto() == null) || (p.getDescription() == null) || p.getProductID() < 1
				|| p.getStaff_email() == null) {

		}
		return false;

	} // isNull

	private String shortenString(String input) {

		String outputString = ""; // substring containing first 4 characters

		if (input.length() > 5) {
			outputString = input.substring(0, 5);
		} else {
			outputString = input;
		}

		return outputString;

	}
	
	private String detectFileType(byte[] imgInputStream) throws IOException {

		String mimeType = null;
		OutputStream outStream = null;

		try {

			System.out.println("Determining file type ...");

			KeyGenerator keyGen = new KeyGenerator();
			String uniqueKey = keyGen.generateKey("tempfile");
			
			String tempFile = uniqueKey + ".pdf";
			
			new File("temp/").mkdirs();
			File targetFile = new File("temp/" + tempFile);

		    outStream = new FileOutputStream(targetFile);
			outStream.write(imgInputStream);

			InputStream is = new BufferedInputStream(new FileInputStream(targetFile));
			mimeType = URLConnection.guessContentTypeFromStream(is);

			System.out.println("targetFile type is = " + mimeType);
			
			System.out.println("tempfile deleted = " + targetFile.delete());

			//IOUtils.closeQuietly(outStream);

		} catch (IOException io) {
			throw new RuntimeException(io);
		} finally {
			IOUtils.closeQuietly(outStream);
		}
		

	if (mimeType == null) {
		
		mimeType = ".pdf";
		
	} else {
		
		switch (mimeType) {
		case "image/gif":
			System.out.println(mimeType);
			mimeType = ".gif";
			break;

		case "image/bmp":
			System.out.println(mimeType);
			mimeType = ".bmp";
			break;

		case "image/jpeg":
			System.out.println(mimeType);
			mimeType = ".jpg";
			break;

		case "image/png":
			System.out.println(mimeType);
			mimeType = ".png";
			break;

		case "image/tiff":
			System.out.println(mimeType);
			mimeType = ".tif";
			break;

		case "application/msword":
			System.out.println(mimeType);
			mimeType = ".doc";
			break;

		case "application/pdf":
			System.out.println(mimeType);
			mimeType = ".pdf";
			break;

		default:
			System.out.println("File type note determined");
			mimeType = ".zip";
			break;

		} // switch
	}// if
		
		return mimeType;

	}


	public String writeImageStreamToFile(byte[] imgInputStream, String companyName, String productDescription,
			int productID) throws Exception {

		String absolutePath = "";
		// replace all spaces in the company name ...
		companyName = companyName.replace(" ", "_");
		companyName = companyName.replace("@", "_");
		productDescription = productDescription.replace(" ", "_");

		try {

			System.out.println("processing image ...");
			
			String filetype =  detectFileType(imgInputStream);

			// first we have to generate a unique name
			KeyGenerator kGen = new KeyGenerator();

			productDescription = shortenString(productDescription);
			String imageFileName = "PRODUCT_PHOTO_" + productDescription + "_" + kGen.generateKey(companyName) + filetype;

			new File("product_photos/" + companyName.toLowerCase() + "/" + productID).mkdirs();

			File targetFile = new File("product_photos/" + companyName.toLowerCase() + "/" + productID + "/"
					+ imageFileName.toLowerCase());

			OutputStream outStream = new FileOutputStream(targetFile);
			outStream.write(imgInputStream);

			System.out.println("targetFile path = " + targetFile.getPath());
			System.out.println("targetFile absolute path = " + targetFile.getAbsolutePath());
			absolutePath = targetFile.getAbsolutePath();

			IOUtils.closeQuietly(outStream);

		} catch (IOException io) {
			throw new RuntimeException(io);
		}

		absolutePath = absolutePath.replace(Constants.jouplefilepath, Constants.jouplenet);
		return absolutePath;

	} // writeImageStreamToFile

} // ProductPhotoValidation
