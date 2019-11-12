package com.eden.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.eden.api.service.AsynchronousService;

@Component
public class ThreadBot extends BaseController {
	
	
	@Autowired 
	private AsynchronousService asynService;


	public void run() {
		// TODO Auto-generated method stub
		asynService.executeAsynchronously();
		
	}
	


}
