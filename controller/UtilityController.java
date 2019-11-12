package com.eden.api.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.eden.api.util.Constants;
import com.eden.api.util.Response;
import com.eden.api.util.ResponseEnum;

@CrossOrigin(origins="*", allowedHeaders="*")
@RestController
@RequestMapping(Constants.BASE_PATH)
public class UtilityController {

	
	@RequestMapping(value = Constants.PATH_PING, method = RequestMethod.GET)
	public @ResponseBody Response ping() {
		
		Response response = Response.build(ResponseEnum.OK.getStatus(),
				ResponseEnum.OK.getMessage(), true);
		
		return response;

	}

	
}
