package com.pjakl.dbviewer.services;

import com.pjakl.dbviewer.api.persistence.dao.DatabaseConfigurationDAO;
import com.pjakl.dbviewer.api.persistence.entities.DatabaseConfiguration;
import com.pjakl.dbviewer.exception.DomainEntityNotFoundException;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Service
@Transactional
public class DatabaseConfigurationService {

    @Autowired
    private DatabaseConfigurationDAO databaseConfigurationDAO;

    @Autowired
    private StringEncryptor stringEncryptor;

    @Transactional(readOnly = true)
    public Page<DatabaseConfiguration> listConfigurations(Pageable pagingRequest) {
        return databaseConfigurationDAO.findAll(pagingRequest);
    }

    public DatabaseConfiguration saveConfiguration(DatabaseConfiguration model) {
        model.setPassword(stringEncryptor.encrypt(model.getPassword()));
        return databaseConfigurationDAO.save(model);
    }

    @Transactional(readOnly = true)
    public DatabaseConfiguration getConfigurationById(UUID configurationId) {
       return getConfigurationById(configurationId, false);
    }


    @Transactional(readOnly = true)
    public DatabaseConfiguration getConfigurationById(UUID configurationId, boolean shouldDecryptPassword) {
        DatabaseConfiguration configuration = databaseConfigurationDAO.findById(configurationId).orElseThrow(() -> new DomainEntityNotFoundException(String.format("Configuration with id=%s not found", configurationId)));
        if (shouldDecryptPassword) {
            configuration.setPassword(stringEncryptor.decrypt(configuration.getPassword()));
        }
        return configuration;
    }

    public void deleteConfiguration(UUID configurationId) {
        var conn = getConfigurationById(configurationId);
        databaseConfigurationDAO.delete(conn);
    }

    public DatabaseConfiguration updateConfiguration(UUID configurationId, DatabaseConfiguration update) {
        var existingConfiguration = getConfigurationById(configurationId);
        existingConfiguration.setName(update.getName());
        existingConfiguration.setHostname(update.getHostname());
        existingConfiguration.setDatabaseName(update.getDatabaseName());
        existingConfiguration.setPassword(stringEncryptor.encrypt(update.getPassword()));
        existingConfiguration.setPort(update.getPort());
        existingConfiguration.setUsername(update.getUsername());

        return databaseConfigurationDAO.save(existingConfiguration);
    }

    public void setStringEncryptor(StringEncryptor stringEncryptor) {
        this.stringEncryptor = stringEncryptor;
    }
}
