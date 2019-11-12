package com.eden.api.controller.engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import org.apache.tomcat.util.http.fileupload.IOUtils;

public class ColourDetector {

	private String hexColour;

	public ColourDetector() {
		super();
	}

	public ColourDetector(String hexColour) {
		super();
		this.hexColour = hexColour;
	}

	public String getHexColour() {
		return hexColour;
	}

	public void setHexColour(String hexColour) {
		this.hexColour = hexColour;
	}

	public String colourDetection(String zoneName) {
		BufferedReader br = null;
		String colourValueHex = null;
		zoneName = zoneName.toLowerCase();

		try {

			String sCurrentLine;
			
			InputStream inputStream = getClass().getResourceAsStream("/colours.csv");

			byte[] buffer = new byte[inputStream.available()];
			inputStream.read(buffer);

			File targetFile = new File("colours.csv");
			OutputStream outStream = new FileOutputStream(targetFile);
			outStream.write(buffer);
			
			IOUtils.closeQuietly(outStream);
			
			
			br = new BufferedReader(new FileReader("colours.csv"));

			int i = 0;
			// int count = 0;
			while ((sCurrentLine = br.readLine()) != null) {
				String[] arr = sCurrentLine.split(",");
				// for the first line it'll print

				// System.out.println("No. " + count + " colour = " + arr[0].toLowerCase() + "
				// hex = " + arr[1]);

				// we need to checj zoneName for each colour until we find a match

				Boolean found = Arrays.asList(zoneName.split(" ")).contains(arr[0].toLowerCase());
				if (found) {
					System.out.println("colour detected in zone name : " + zoneName.toLowerCase() + " has colour "
							+ arr[0].toLowerCase());
					return arr[1].toString();
				}

				i++;
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
		if (colourValueHex == null) colourValueHex = "#0000FF";

		return colourValueHex;
	}

}
