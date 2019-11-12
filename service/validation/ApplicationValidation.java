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
import com.edenstar.model.booking.ProcessApplication;

public class ApplicationValidation {

	public boolean isNull(ProcessApplication p, String mode) {
		// make sure that none of the mandatory fields are null

		if (p.getStaff_email_id() == null)
			return true;

		if (mode.contentEquals("getquote")) {

			if (p.getQuoteRef() == null)
				return true;
		}

		if (mode.contentEquals("getapplication")) {

			if (p.getApplication_id() == 0)
				return true;
		}

		if (mode.contentEquals("kiosk")) {

			if (p.getKiosk_id() == 0)
				return true;

			if ((p.getLock_kiosk() > 1) || (p.getLock_kiosk() < 0))
				return true;
		}

		if (mode.contentEquals("deposit")) {

			if (p.getApplication_id() == 0)
				return true;

			if (p.getDeposit_scan() == null)
				return true;
		}
		
		if (mode.contentEquals("deposit_quoteref")) {

			if (p.getDeposit_scan() == null)
				return true;
		}

		return false;

	} // isNull

	public boolean fieldsAreEmpty(ProcessApplication p, String mode) {

		if (p.getStaff_email_id().contentEquals(""))
			return true;

		if (mode.contentEquals("getquote")) {
			if (p.getQuoteRef().contentEquals(""))
				return true;
		}

		if (mode.contentEquals("getapplication")) {
			if (p.getApplication_id() == 0)
				return true;
		}

		if (mode.contentEquals("kiosk")) {
			if (p.getKiosk_id() == 0)
				return true;
		}

		if (mode.contentEquals("deposit")) {
			if (p.getApplication_id() == 0)
				return true;
			if (p.getDeposit_scan() == null)
				return true;
		}

		return false;

	} // fieldsAreEmpty

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
	}// if
		
		return mimeType;

	}

	public String writeImageStreamToFile(byte[] imgInputStream, String quoteRef) throws Exception {

		String absolutePath = "";
		// replace all spaces in the company name ...
		quoteRef = quoteRef.replace("/", "_");

		try {

			System.out.println("processing scan ...");

			String filetype = detectFileType(imgInputStream);

			String imageFileName = "DEPOSIT_SCAN_" + quoteRef + filetype;

			new File("quote_scans/" + quoteRef.toLowerCase()).mkdirs();

			File targetFile = new File("quote_scans/" + quoteRef.toLowerCase() + "/" + imageFileName.toLowerCase());

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
