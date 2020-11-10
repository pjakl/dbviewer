package com.pjakl.dbviewer.services;

import com.pjakl.dbviewer.api.persistence.dao.DatabaseConfigurationDAO;
import com.pjakl.dbviewer.api.persistence.entities.DatabaseConfiguration;
import com.pjakl.dbviewer.configuration.DBViewerConfiguration;
import com.pjakl.dbviewer.exception.DomainEntityNotFoundException;
import org.jasypt.encryption.StringEncryptor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Optional;
import java.util.UUID;

import static com.pjakl.dbviewer.Stub.databaseConfiguration;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = DBViewerConfiguration.class)
@TestPropertySource(properties = {"encryptor.dbEncryptorPassword=test"})
public class DatabaseConfigurationServiceTest {
    public static final UUID CONFIGURATION_ID = UUID.randomUUID();
    public static final String NAME = "name";

    @InjectMocks
    DatabaseConfigurationService service;

    @Mock
    DatabaseConfigurationDAO dao;

    @Autowired
    private StringEncryptor stringEncryptor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        service.setStringEncryptor(stringEncryptor);
    }

    @Test
    public void shouldEncryptPassword() {
        service.saveConfiguration(databaseConfiguration(NAME, CONFIGURATION_ID));

        var captor = ArgumentCaptor.forClass(DatabaseConfiguration.class);
        verify(dao, times(1)).save(captor.capture());

        assertNotSame(captor.getValue().getPassword(), "password");
    }

    @Test
    public void shouldEncryptAndDecryptToSameValue() {
        service.saveConfiguration(databaseConfiguration(NAME, CONFIGURATION_ID));

        var captor = ArgumentCaptor.forClass(DatabaseConfiguration.class);
        verify(dao, times(1)).save(captor.capture());

        DatabaseConfiguration savedEntity = captor.getValue();
        assertNotEquals(savedEntity.getPassword(), "password");

        when(dao.findById(CONFIGURATION_ID)).thenReturn(Optional.of(savedEntity));

        DatabaseConfiguration decryptedConfiguration = service.getConfigurationById(CONFIGURATION_ID, true);
        assertEquals(decryptedConfiguration.getPassword(), "password");

    }

    @Test
    public void shouldNotDecryptByDefault() {
        service.saveConfiguration(databaseConfiguration(NAME, CONFIGURATION_ID));

        var captor = ArgumentCaptor.forClass(DatabaseConfiguration.class);
        verify(dao, times(1)).save(captor.capture());

        DatabaseConfiguration savedEntity = captor.getValue();
        assertNotEquals(captor.getValue().getPassword(), "password");

        when(dao.findById(CONFIGURATION_ID)).thenReturn(Optional.of(savedEntity));

        DatabaseConfiguration decryptedConfiguration = service.getConfigurationById(CONFIGURATION_ID);
        assertNotEquals(decryptedConfiguration.getPassword(), "password");

    }

    @Test(expected = DomainEntityNotFoundException.class)
    public void shouldThrowExceptionWhenNoConfigurationFound() {

        when(dao.findById(CONFIGURATION_ID)).thenReturn(Optional.empty());

        service.getConfigurationById(CONFIGURATION_ID, true);
    }

    @Test
    public void shouldUpdateConfiguration() {
        var existingConfiguration = databaseConfiguration(NAME, CONFIGURATION_ID);
        when(dao.findById(CONFIGURATION_ID)).thenReturn(Optional.of(existingConfiguration));

        DatabaseConfiguration update = new DatabaseConfiguration();
        update.setDatabaseName("newDatabaseName");
        update.setPort(5678);
        update.setPassword("newPassword");
        update.setUsername("newUsername");
        update.setHostname("newHostName");
        update.setName("newName");

        service.updateConfiguration(CONFIGURATION_ID, update);

        var captor = ArgumentCaptor.forClass(DatabaseConfiguration.class);
        verify(dao, times(1)).save(captor.capture());

        DatabaseConfiguration savedEntity = captor.getValue();
        assertNotEquals(captor.getValue().getPassword(), "password");

        assertEquals(update.getDatabaseName(), savedEntity.getDatabaseName());
        assertEquals(update.getPassword(), stringEncryptor.decrypt(savedEntity.getPassword()));
        assertEquals(update.getPort(), savedEntity.getPort());
        assertEquals(update.getName(), savedEntity.getName());
        assertEquals(update.getHostname(), savedEntity.getHostname());
        assertEquals(update.getUsername(), savedEntity.getUsername());

    }


}
