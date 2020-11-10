package com.pjakl.dbviewer.routing;

import com.pjakl.dbviewer.api.persistence.entities.DatabaseConfiguration;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DynamicRoutingDatasource extends AbstractRoutingDataSource {

    private final Map<UUID, DataSource> datasources;

    public DynamicRoutingDatasource() {
        this.datasources = new HashMap<>();
    }

    @Override
    public void afterPropertiesSet() {
        // no initialization
    }

    @Override
    protected DataSource determineTargetDataSource() {
        DatabaseConfiguration configuration = (DatabaseConfiguration) determineCurrentLookupKey();
        if(configuration == null) {
            throw new IllegalStateException("No connection set in context.");
        }
        DataSource dataSource = datasources.get(configuration.getId());
        // not known service yet
        if(dataSource == null) {
            dataSource = getDataSourceFromConfiguration(configuration);
            datasources.put(configuration.getId(), dataSource);
        }
        return dataSource;
    }


    private HikariDataSource getDataSourceFromConfiguration(DatabaseConfiguration configuration) {
        HikariDataSource dataSource = new HikariDataSource();

        dataSource.setInitializationFailTimeout(0);
        dataSource.setMaximumPoolSize(2);
        dataSource.setConnectionTimeout(5000);
        dataSource.setDataSourceClassName("org.postgresql.ds.PGSimpleDataSource");
        dataSource.addDataSourceProperty("url", "jdbc:postgresql://" + configuration.getHostname() + ":" + configuration.getPort() + "/" + configuration.getDatabaseName());
        dataSource.addDataSourceProperty("user", configuration.getUsername());
        dataSource.addDataSourceProperty("password", configuration.getPassword());
        return dataSource;
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return DatabaseConfigurationContextHolder.getDatabaseConfiguration();
    }
}
