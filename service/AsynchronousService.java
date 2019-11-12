package com.eden.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import com.eden.api.MyThread;
import com.eden.api.TronBot;

@Service
public class AsynchronousService {

    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private TaskExecutor taskExecutor;
    

    public void executeAsynchronously() {
        MyThread myThread = applicationContext.getBean(MyThread.class);
        taskExecutor.execute(myThread);
        
        
        TronBot tronBot = applicationContext.getBean(TronBot.class);
        taskExecutor.execute(tronBot);
    }
}
