package com.pjakl.dbviewer.services;

import com.pjakl.dbviewer.api.persistence.dao.DatabaseConnectionDAO;
import com.pjakl.dbviewer.api.persistence.entities.DatabaseColumn;
import com.pjakl.dbviewer.api.persistence.entities.DatabaseConfiguration;
import com.pjakl.dbviewer.routing.DatabaseConfigurationContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class DatabaseConnectionService {
    @Autowired
    DatabaseConnectionDAO dao;

    public List<String> getAllSchemaNames(DatabaseConfiguration currentConfiguration) {
        DatabaseConfigurationContextHolder.set(currentConfiguration);
        return dao.getAllSchemaNames();
    }

    public List<String> getTablesForSchema(DatabaseConfiguration currentConfiguration, String schemaName) {
        DatabaseConfigurationContextHolder.set(currentConfiguration);
        return dao.getTablesForSchema(schemaName);
    }

    public List<DatabaseColumn> getColumnsForTable(DatabaseConfiguration currentConfiguration, String schemaName, String tableName) {
        DatabaseConfigurationContextHolder.set(currentConfiguration);
        return dao.getColumnsForTable(schemaName, tableName);
    }

    public Page<Map<String, Object>> getTablePreview(DatabaseConfiguration currentConfiguration, PageRequest pageRequest, String schemaName, String tableName) {
        DatabaseConfigurationContextHolder.set(currentConfiguration);
        return dao.getTablePreview(pageRequest, schemaName, tableName);
    }
}
