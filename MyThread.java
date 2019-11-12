package com.eden.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.eden.api.service.EngineService;

@Component
@Scope("prototype")
public class MyThread implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(MyThread.class);

	@Autowired
	private EngineService cpu;

	@Override
	public void run() {
		LOGGER.info("Called from thread");
		System.out.println("\n[BOT} : Running flagBots !\n");

		do {
			try {
				Thread.sleep(2000);

				cpu.refreshBookingFlags();
				cpu.refreshApplicationFlags();
				cpu.refreshQuoteFlags();
				cpu.refreshOverdueFlags();

				Thread.sleep(21600000);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} while (1 < 2);
	}
}
