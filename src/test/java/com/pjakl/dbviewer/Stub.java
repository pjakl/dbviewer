package com.pjakl.dbviewer;

import com.pjakl.dbviewer.api.persistence.entities.DatabaseConfiguration;

import java.util.UUID;

public class Stub {

    public static DatabaseConfiguration databaseConfiguration(String name, UUID id) {
        var dbConfiguration = new DatabaseConfiguration();
        dbConfiguration.setId(id);
        dbConfiguration.setName(name);
        dbConfiguration.setHostname("hostName");
        dbConfiguration.setPassword("password");
        dbConfiguration.setPort(1234);
        dbConfiguration.setDatabaseName("databaseName");
        dbConfiguration.setUsername("username");

        return dbConfiguration;
    }
}
