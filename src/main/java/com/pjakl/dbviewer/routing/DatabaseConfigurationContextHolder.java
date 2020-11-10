package com.pjakl.dbviewer.routing;

import com.pjakl.dbviewer.api.persistence.entities.DatabaseConfiguration;

/**
 * Thread-local storage to store context of database used to query
 */
public class DatabaseConfigurationContextHolder {

    private static final ThreadLocal<DatabaseConfiguration> CONTEXT =
            new ThreadLocal<>();

    public static void set(DatabaseConfiguration databaseConfiguration) {
        CONTEXT.set(databaseConfiguration);
    }

    public static DatabaseConfiguration getDatabaseConfiguration() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }

}
