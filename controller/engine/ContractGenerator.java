package com.eden.api.controller.engine;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.xmlbeans.XmlCursor;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContractGenerator {

	private StringBuilder contractTemplate = new StringBuilder();
	XWPFDocument document = new XWPFDocument();

	public ContractGenerator(StringBuilder contractTemplate) {
		this.contractTemplate = contractTemplate;
	}

	public ContractGenerator() {

	}

	public StringBuilder getContractTemplate() {
		return contractTemplate;
	}

	public void setContractTemplate(StringBuilder contractTemplate) {
		this.contractTemplate = contractTemplate;
	}

	public StringBuilder generateTemplate() throws FileNotFoundException, IOException {

		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(new FileReader("contract_template.txt"));

		try {

			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append("\n\n");
				// sb.append(System.lineSeparator());
				line = br.readLine();
			}
			String everything = sb.toString();
		} finally {
			br.close();
		}

		return sb;
	}

	public XWPFDocument loadContractTemplate() throws IOException {

		XWPFDocument xdoc = new XWPFDocument();

		try {
			FileInputStream fis = new FileInputStream("contract_template.docx");
			// xdoc = new XWPFDocument(OPCPackage.open(fis));
			document = new XWPFDocument(OPCPackage.open(fis));

			// extract the paragraphs
			List<XWPFParagraph> xwpfParagraphs = document.getParagraphs();

			// create the mapping
			Map<String, String> replacementMap = setMap();

			// sent it for mapping
			replaceElementInParagraphs(xwpfParagraphs, replacementMap);

			// display changes
			XWPFWordExtractor extractor = new XWPFWordExtractor(document);
			System.out.println(extractor.getText());

			// System.out.println(extractor.getText());
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return document;
	} // loadContractTemplate

	public Map<String, String> setMap() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("$CONTRACT_DATE", "<Date of contract here>"); // date of contract
		map.put("$TRADE_NAME", "<Nike Ltd>"); // customer company name /trading name
		map.put("$KIOSK_DETAILS", "<Kiosk details here>"); // kiosk no, and full address
		map.put("$CUSTOMER_ADDRESS", "<Customer address>"); // customer address
		map.put("$START_DATE", "<Lease start date>");
		map.put("$END_DATE", "<Lease end date>");
		map.put("$PAYMENT_SCHEDULE", "<Payment schedule here>");

		System.out.println("using entrySet and toString");
		for (Map.Entry<String, String> entry : map.entrySet()) {
			System.out.println(entry);
		}
		System.out.println();

		System.out.println("using entrySet and manual string creation");
		for (Map.Entry<String, String> entry : map.entrySet()) {
			System.out.println(entry.getKey() + "=" + entry.getValue());
		}
		System.out.println();

		System.out.println("using keySet");
		for (String key : map.keySet()) {
			System.out.println(key + "=" + map.get(key));
		}
		System.out.println();

		return map;
	} // setMap

	private void replaceElementInParagraphs(List<XWPFParagraph> xwpfParagraphs, Map<String, String> replacedMap) {
		if (!searchInParagraphs(xwpfParagraphs, replacedMap)) {
			replaceElementInParagraphs(xwpfParagraphs, replacedMap);
		}
	} // replaceElementInParagraphs

	private boolean searchInParagraphs(List<XWPFParagraph> xwpfParagraphs, Map<String, String> replacedMap) {
		for (XWPFParagraph xwpfParagraph : xwpfParagraphs) {
			List<XWPFRun> xwpfRuns = xwpfParagraph.getRuns();
			for (XWPFRun xwpfRun : xwpfRuns) {
				String xwpfRunText = xwpfRun.getText(xwpfRun.getTextPosition());
				for (Map.Entry<String, String> entry : replacedMap.entrySet()) {
					if (xwpfRunText != null && xwpfRunText.contains(entry.getKey())) {
						if (entry.getValue().contains("\n")) {
							String[] paragraphs = entry.getValue().split("\n");
							entry.setValue("");
							createParagraphs(xwpfParagraph, paragraphs);
							return false;
						}
						System.out.println("match found ! : " + xwpfRunText.toString());
						System.out.println("getKey = " + entry.getKey() + " and getValue = " + entry.getValue());
						xwpfRunText = xwpfRunText.replace(entry.getKey(), entry.getValue()); // xwpfRunText.replaceAll(entry.getKey(),
																								// "CHANGED"
																								// /*entry.getValue()*/);
						System.out.println("after replacement ! : " + xwpfRunText.toString());
					}
				}
				xwpfRun.setText(xwpfRunText, 0);
			}
		}
		return true;
	} // searchInParagraphs

	private void createParagraphs(XWPFParagraph xwpfParagraph, String[] paragraphs) {
		if (xwpfParagraph != null) {
			for (int i = 0; i < paragraphs.length; i++) {
				XmlCursor cursor = xwpfParagraph.getCTP().newCursor();
				XWPFParagraph newParagraph = document.insertNewParagraph(cursor);
				newParagraph.setAlignment(xwpfParagraph.getAlignment());
				newParagraph.getCTP().insertNewR(0).insertNewT(0).setStringValue(paragraphs[i]);
				newParagraph.setNumID(xwpfParagraph.getNumID());
			}
			document.removeBodyElement(document.getPosOfParagraph(xwpfParagraph));
		}
	} // createParagraphs

} // ContractGenerator
