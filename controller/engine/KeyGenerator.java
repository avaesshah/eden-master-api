package com.eden.api.controller.engine;

public class KeyGenerator {

	private static final long twepoch = 1288834974657L;
	private static final long sequenceBits = 17;
	private static final long sequenceMax = 65536;
	private static volatile long lastTimestamp = -1L;
	private static volatile long sequence = 0L;

	public String generateKey(String firstName, String lastName) {

		Long uniqueID = generateLongId();
		uniqueID = uniqueID % 10000000;

		String input = lastName + firstName; // input string
		input = input.replace(" ", "");
		String firstThreeChars = ""; // substring containing first 4 characters

		if (input.length() > 3) {
			firstThreeChars = input.substring(0, 3);
		} else {
			firstThreeChars = input;
		}

		StringBuilder sb1 = new StringBuilder(uniqueID.toString());

		sb1.append("/");
		sb1.append(firstThreeChars.toUpperCase());

		System.out.println("unique key generated is  : " + sb1.toString());
		return sb1.toString();

	} // generateKey
	
	public String generateKey(String filename) {

		Long uniqueID = generateLongId();
		uniqueID = uniqueID % 100000000;

		String input =  filename; // input string

		StringBuilder sb1 = new StringBuilder(uniqueID.toString());

		sb1.append("_");
		sb1.append(input.toUpperCase());
		

		System.out.println("unique key generated is  : " + sb1.toString());
		return sb1.toString();

	} // generateKey

	private static synchronized Long generateLongId() {
		long timestamp = System.currentTimeMillis();
		if (lastTimestamp == timestamp) {
			sequence = (sequence + 1) % sequenceMax;
			if (sequence == 0) {
				timestamp = tilNextMillis(lastTimestamp);
			}
		} else {
			sequence = 0;
		}
		lastTimestamp = timestamp;
		Long id = ((timestamp - twepoch) << sequenceBits) | sequence;
		return id;
	}

	private static long tilNextMillis(long lastTimestamp) {
		long timestamp = System.currentTimeMillis();
		while (timestamp <= lastTimestamp) {
			timestamp = System.currentTimeMillis();
		}
		return timestamp;
	}

}
