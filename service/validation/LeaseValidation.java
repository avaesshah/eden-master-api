package com.eden.api.service.validation;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import org.apache.tomcat.util.http.fileupload.IOUtils;

import com.eden.api.controller.engine.KeyGenerator;
import com.eden.api.util.Constants;
import com.edenstar.model.booking.ProcessLease;

public class LeaseValidation {
	
	public boolean isNull(ProcessLease l, String mode) {
		// make sure that none of the mandatory fields are null

		if (l.getStaff_email_id() == null)
			return true;

		if (mode.contentEquals("comment")) {

			if (l.getComments() == null)
				return true;
			
			if (l.getLease_id() == 0)
				return true;
		}
		
		if (mode.contentEquals("contract")) {

			if (l.getContract_upload() == null || l.getContract_upload().length == 0)
				return true;
			
			if (l.getLease_id() == 0)
				return true;
		}
		
		if (mode.contentEquals("generate")) {
			
			if (l.getLease_id() == 0)
				return true;
		}
		
//
//		if (mode.contentEquals("getapplication")) {
//
//			if (l.getApplication_id() == 0)
//				return true;
//		}


		return false;

	} // isNull

	public boolean fieldsAreEmpty(ProcessLease l, String mode) {

		if (l.getStaff_email_id().contentEquals(""))
			return true;

		if (mode.contentEquals("comment")) {
			if (l.getComments().contentEquals(""))
				return true;
		}

		if (mode.contentEquals("contract")) {
			if (l.getLease_id() == 0)
				return true;
		}


		return false;

	} // fieldsAreEmpty
	
	private void cleanUp(Path path) throws NoSuchFileException, DirectoryNotEmptyException, IOException{
		  Files.delete(path);
		}

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
	}// if
		
		return mimeType;

	}

	public String writeImageStreamToFile(byte[] imgInputStream, String bookingRef) throws Exception {

		String absolutePath = "";
		// replace all spaces in the company name ...
		bookingRef = bookingRef.replace("/", "_");
		
		try {

			System.out.println("processing scan ...");

			String filetype = detectFileType(imgInputStream);

			String imageFileName = "CONTRACT_SCAN_" + bookingRef + filetype;

			new File("contract_scans/" + bookingRef.toLowerCase()).mkdirs();

			File targetFile = new File("contract_scans/" + bookingRef.toLowerCase() + "/" + imageFileName.toLowerCase());

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
