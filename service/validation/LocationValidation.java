package com.eden.api.service.validation;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import com.eden.api.controller.engine.KeyGenerator;
import com.eden.api.util.Constants;
import com.edenstar.model.Rate;
import com.edenstar.model.dash.CreateLocation;

public class LocationValidation {

	public boolean fieldsAreEmpty(CreateLocation l) {

		// check to make sure that the mandatory parameters are not empty
		if (l.getStaff_email_id().contentEquals("") || l.getNum_max_rows() < 1 || l.getNum_max_columns() < 1
				|| l.getLocationName().contentEquals("") || l.getLocationArea().contentEquals("")) {

			return true;

		} // if

		return false;

	} // fieldsAreEmpty

	public boolean isNull(CreateLocation l) {
		// make sure that none of the mandatory fields are null

		if (l.getNum_max_columns() == 0)
			return true;
		if (l.getNum_max_rows() == 0)
			return true;
		if (l.getStaff_email_id() == null)
			return true;
		if (l.getLocationName() == null)
			return true;
		if (l.getLocationArea() == null)
			return true;

		return false;

	} // isNull

	public boolean validateRates(CreateLocation l) {
		// makes sure that the rates passed for the zones are within normal values
		boolean isValid = true;
		List<Rate> rateList = new ArrayList<>();

		// we need to first go through the array and check each standardRate then copy
		// it over to the new RateList

		for (int i = 0; i < l.getRateDiscountList().size(); i++) {

			if (l.getRateDiscountList().get(i).getStandardRate() < 0.0)
				isValid = false;

			if (isValid) {
				Rate rate = new Rate();
				rate.setDiscountDurationDays(365);
				rate.setRateCoeff(0.0);
				rate.setRateMax(l.getRateDiscountList().get(i).getStandardRate());
				rate.setRateMin(l.getRateDiscountList().get(i).getStandardRate());
				rate.setStandardRate(l.getRateDiscountList().get(i).getStandardRate());
				rateList.add(rate);
			}

			// for each rate there may be a discount aray
			// we will check this seperately
		} // for

		l.setRateList(rateList);

//		for (i = 0; i < l.getRateList().size(); i++) {
//
//			if (l.getRateList().get(i).getRateMax() < 0.0
//					|| l.getRateList().get(i).getRateMin() > l.getRateList().get(i).getRateMax()
//					|| l.getRateList().get(i).getDiscountDurationDays() < 1)
//				isValid = false;
//
//		} // for loop

		// if all the values in the array are normal (i.e. isValid = true)
		// we should calculate the coefficient here for all the rates

//		if (isValid) {
//
//			for (i = 0; i < l.getRateList().size(); i++) {
//
//				double coeff = (l.getRateList().get(i).getRateMin() - l.getRateList().get(i).getRateMax())
//						/ l.getRateList().get(i).getDiscountDurationDays();
//
//				// round off to 2 decimal places
//				// rateCoeff = round(coeff, 3);
//				rateCoeff = coeff;
//
//				// set the coefficient
//				l.getRateList().get(i).setRateCoeff(rateCoeff);
//
//			} // for loop
//
//		} // set the rate coefficient

		return isValid;

	} // validateRates

	public boolean validateDiscount(CreateLocation l) {
		boolean isValid = true;

		for (int i = 0; i < l.getRateDiscountList().size(); i++) {
			// we need to check the discounts individually

			if (l.getRateDiscountList().get(i).getDiscountList().size() != 0) {

				for (int j = 0; j < l.getRateDiscountList().get(i).getDiscountList().size(); j++) {

					if (l.getRateDiscountList().get(i).getDiscountList().get(j).getPercentageDiscount() < 0.00
							|| l.getRateDiscountList().get(i).getDiscountList().get(j).getPercentageDiscount() > 100.00)
						isValid = false;

//					if (l.getRateDiscountList().get(i).getDiscountList().get(j).getFromMonth() < 0
//							|| l.getRateDiscountList().get(i).getDiscountList().get(j).getFromMonth() >= l
//									.getRateDiscountList().get(i).getDiscountList().get(j).getToMonth())
//						isValid = false;

				} // for

			} // outer for

		} // outer for loop

		return isValid;

	} // validateDiscount
	
	public boolean validateDiscountMonths(CreateLocation l) {
		boolean isValid = true;

		for (int i = 0; i < l.getRateDiscountList().size(); i++) {
			// we need to check the discounts individually

			if (l.getRateDiscountList().get(i).getDiscountList().size() != 0) {

				for (int j = 0; j < l.getRateDiscountList().get(i).getDiscountList().size(); j++) {


					if (l.getRateDiscountList().get(i).getDiscountList().get(j).getFromMonth() < 0
							|| l.getRateDiscountList().get(i).getDiscountList().get(j).getFromMonth() >= l
									.getRateDiscountList().get(i).getDiscountList().get(j).getToMonth())
						isValid = false;

				} // for

			} // outer for

		} // outer for loop

		return isValid;

	} // validateDiscount

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

			// IOUtils.closeQuietly(outStream);

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
		} // if

		return mimeType;

	}

	public String writeImageStreamToFile(byte[] imgInputStream, String locationName) throws Exception {

		String absolutePath = "";
		// replace all spaces in the location name ...
		locationName = locationName.replace(" ", "_");

		try {

			System.out.println("processing image ...");

			String filetype = detectFileType(imgInputStream);

			// first we have to generate a unique name
			KeyGenerator kGen = new KeyGenerator();
			String imageFileName = "MAP_" + kGen.generateKey(locationName) + filetype;

			new File("images/" + locationName.toLowerCase()).mkdirs();
			File targetFile = new File("images/" + locationName.toLowerCase() + "/" + imageFileName.toLowerCase());
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

	public void deleteDirectory(String locationName) throws IOException {

		String myPath = System.getProperty("user.dir");
		locationName = locationName.replace(" ", "_");

		System.out.println("my path is " + myPath);
		myPath = myPath + "/images/" + locationName.toLowerCase();
		System.out.println("my path is " + myPath);
		try {
			FileUtils.deleteDirectory(new File(myPath));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	} // deleteDirectory

} // LocationValidation
