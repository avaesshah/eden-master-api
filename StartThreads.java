package com.eden.api;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.eden.api.service.AsynchronousService;

@Component
public class StartThreads {
	
    
    @Autowired 
    private AsynchronousService aSync;
    
    @PostConstruct
    public void init() {
        aSync.executeAsynchronously();
    }

}
