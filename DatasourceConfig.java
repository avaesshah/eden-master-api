package com.eden.api;

import javax.sql.DataSource;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatasourceConfig {

        
    @Bean
    public DataSource datasource() {
        return DataSourceBuilder.create()
          .driverClassName("org.mariadb.jdbc.Driver")
          .url("jdbc:mysql://jouple.net:3306/jouplenet_eden_db")
          .username("jouplenet_edenstar_admin")
          .password("lymUGl5I62lj")
          .build(); 
    }
    
}
