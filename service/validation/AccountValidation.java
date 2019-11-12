package com.eden.api.service.validation;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.eden.api.controller.engine.KeyGenerator;
import com.eden.api.util.Constants;
import com.edenstar.model.account.ProcessAccount;

public class AccountValidation {

	public boolean isNull(ProcessAccount a, String mode) {
		// make sure that none of the mandatory fields are null

		if (a.getStaff_email_id() == null)
			return true;

		if (mode.contentEquals("comment")) {

			if (a.getComments() == null)
				return true;

			if (a.getAccount_id() == 0)
				return true;
		}

		if (mode.contentEquals("comment_payment")) {

			if (a.getComments() == null)
				return true;

			if (a.getPayment_id() == 0)
				return true;
		}

		if (mode.contentEquals("viewaccount")) {

			if (a.getBookingRef() == null || a.getBookingRef().contentEquals(""))
				return true;
		}

		if (mode.contentEquals("update")) {

			if (a.getAccount_id() == 0)
				return true;
		}

		if (mode.contentEquals("updatepayment")) {

			if (a.getPayment_id() == 0)
				return true;
		}

		if (mode.contentEquals("document")) {

			if (a.getDocument_upload() == null)
				return true;

			if (a.getAccount_id() == 0)
				return true;
		}

		return false;

	} // isNull

	public boolean fieldsAreEmpty(ProcessAccount a, String mode) {

		if (a.getStaff_email_id().contentEquals(""))
			return true;

		if (mode.contentEquals("comment") || mode.contentEquals("comment_payment")) {
			if (a.getComments().contentEquals(""))
				return true;
		}

		if (mode.contentEquals("document")) {

			if (a.getDocument_upload().equals(null))
				return true;

		}

		return false;

	} // fieldsAreEmpty

	private boolean isValidDate(String dateToValidate) {
		String pattern = "dd/MM/yyyy";

		try {
			DateTimeFormatter fmt = DateTimeFormat.forPattern(pattern);
			fmt.parseDateTime(dateToValidate);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public String checkDates(String dateStr) throws Exception {

		String isValid = "";
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

		Date startDate = null;

		try {

			startDate = format.parse(dateStr);

			if (isValidDate(dateStr) == false) {
				isValid = isValid + "[date entered is not a valid date]";
			}

			if (!isValid.equals(""))
				return isValid;

		} catch (Exception e) {
			e.printStackTrace();
		}

		if (isValid.equals(""))
			return null;

		return isValid;
	} // checkDates

	private String detectFileType(byte[] imgInputStream) throws IOException {

		String mimeType = null;
		OutputStream outStream = null;

		try {

			System.out.println("Determining file type ...");
			
			KeyGenerator keyGen = new KeyGenerator();
			String uniqueKey = keyGen.generateKey("tempfile");

			String tempFile =  uniqueKey + ".doc";

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

			mimeType = ".doc";

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

	public String writeImageStreamToFile(byte[] imgInputStream, String bookingRef) throws Exception {

		String absolutePath = "";
		// replace all spaces in the company name ...
		bookingRef = bookingRef.replace("/", "_");

		try {

			System.out.println("processing scan ...");

			String filetype = detectFileType(imgInputStream);

			String imageFileName = "ACC_DOC_SCAN_" + bookingRef + filetype;

			new File("accounting_scans/" + bookingRef.toLowerCase()).mkdirs();

			File targetFile = new File(
					"accounting_scans/" + bookingRef.toLowerCase() + "/" + imageFileName.toLowerCase());

			OutputStream outStream = new FileOutputStream(targetFile);
			outStream.write(imgInputStream);

			InputStream is = new BufferedInputStream(new FileInputStream(targetFile));
			String mimeType = URLConnection.guessContentTypeFromStream(is);

			System.out.println("targetFile path = " + targetFile.getPath());
			System.out.println("targetFile absolute path = " + targetFile.getAbsolutePath());
			System.out.println("targetFile type is = " + mimeType);
			absolutePath = targetFile.getAbsolutePath();

			IOUtils.closeQuietly(outStream);

		} catch (IOException io) {
			throw new RuntimeException(io);
		}

		absolutePath = absolutePath.replace(Constants.jouplefilepath, Constants.jouplenet);
		return absolutePath;

	} // writeImageStreamToFile


}
