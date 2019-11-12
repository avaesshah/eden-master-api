package com.eden.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.eden.api.service.EngineService;
import com.eden.api.util.Constants;

@Component
@Scope("prototype")
public class TronBot implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(TronBot.class);

	@Autowired
	private EngineService cpu;

	@Override
	public void run() {
		LOGGER.info("Called from thread");
		System.out.println("\n[BOT} : Running TronBOT\n");

		do {
			try {
				Thread.sleep(5000);
				
				System.out.println("[TronBot] : initiating automated clean up procedures for quotations...");
				cpu.cleanUpQuotes();
				System.out.println("[TronBot] : complete ...");
				System.out.println("[TronBot] : initiating automated clean up procedures for applications...");
				cpu.cleanUpApplications();
				System.out.println("[TronBot] : complete ...");
				System.out.println("[TronBot] : initiating automated clean up procedures for bookings...");
				cpu.cleanUpBookings();
				System.out.println("[TronBot] : complete ... going to sleep\n");
				System.out.println("\n****************************** ARCHIVER INITIATED ******************************\n");
				System.out.println("[TronBot] : initiating automated archiving procedures for quotations ...");
				cpu.archiveQuotes();
				System.out.println("[TronBot] : initiating automated archiving procedures for applications ...");
				cpu.archiveApplications();
				
				System.out.println("    ______    __              _____ __                ___    ____  ____\n" + 
						"   / ____/___/ /__  ____     / ___// /_____ ______   /   |  / __ \\/  _/\n" + 
						"  / __/ / __  / _ \\/ __ \\    \\__ \\/ __/ __ `/ ___/  / /| | / /_/ // /  \n" + 
						" / /___/ /_/ /  __/ / / /   ___/ / /_/ /_/ / /     / ___ |/ ____// /   \n" + 
						"/_____/\\__,_/\\___/_/ /_/   /____/\\__/\\__,_/_/     /_/  |_/_/   /___/");
				
	            System.out.println("Current version " + Constants.eden_version);

				Thread.sleep(43200000);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} while (1 < 2);
	}
}
