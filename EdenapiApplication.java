package com.eden.api;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.eden.api.util.Constants;

@SpringBootApplication
public class EdenapiApplication {
	
		
	public static void main(String[] args) {
		
		SpringApplication.run(EdenapiApplication.class, args);
		System.out.println("    ______    __              _____ __                ___    ____  ____\n" + 
				"   / ____/___/ /__  ____     / ___// /_____ ______   /   |  / __ \\/  _/\n" + 
				"  / __/ / __  / _ \\/ __ \\    \\__ \\/ __/ __ `/ ___/  / /| | / /_/ // /  \n" + 
				" / /___/ /_/ /  __/ / / /   ___/ / /_/ /_/ / /     / ___ |/ ____// /   \n" + 
				"/_____/\\__,_/\\___/_/ /_/   /____/\\__/\\__,_/_/     /_/  |_/_/   /___/");
		

        InetAddress ip;
        String hostname;
        try {
            ip = InetAddress.getLocalHost();
            hostname = ip.getHostName();
            System.out.println("Your current IP address : " + ip);
            System.out.println("Your current Hostname : " + hostname);
            System.out.println("Current version " + Constants.eden_version);
 
        } catch (UnknownHostException e) {
 
            e.printStackTrace();
        }
        

        
        
		
	} // main


}
