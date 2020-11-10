package com.pjakl.dbviewer.dao;

import com.pjakl.dbviewer.api.persistence.dao.DatabaseConfigurationDAO;
import com.pjakl.dbviewer.api.persistence.entities.DatabaseConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.PersistenceException;

import static com.pjakl.dbviewer.Stub.databaseConfiguration;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@DataJpaTest
public class DatabaseConfigurationDAOTest {

    public static final String CONFIGURATION_NAME = "name";

    @Autowired
    DatabaseConfigurationDAO dao;

    @Autowired
    private TestEntityManager testEntityManager;


    @Test
    public void shouldSaveConfiguration() {
        DatabaseConfiguration savedEntity = dao.save(databaseConfiguration(CONFIGURATION_NAME, null));
        assertNotNull(savedEntity.getId());
    }

    @Test
    public void shouldSaveAndLoadConfiguration() {
        DatabaseConfiguration savedEntity = dao.save(databaseConfiguration(CONFIGURATION_NAME, null));
        assertNotNull(savedEntity.getId());

        DatabaseConfiguration loadedEntity = dao.findById(savedEntity.getId()).orElseThrow(IllegalStateException::new);

        assertEquals(savedEntity.getId(), loadedEntity.getId());
        assertEquals(savedEntity.getPort(), loadedEntity.getPort());
        assertEquals(savedEntity.getHostname(), loadedEntity.getHostname());
        assertEquals(savedEntity.getPassword(), loadedEntity.getPassword());
        assertEquals(savedEntity.getUsername(), loadedEntity.getUsername());
        assertEquals(savedEntity.getName(), loadedEntity.getName());
        assertEquals(savedEntity.getDatabaseName(), loadedEntity.getDatabaseName());
    }

    @Test
    public void shouldPaginateListOfAllTransactions() {
        dao.save(databaseConfiguration("name1", null));
        dao.save(databaseConfiguration("name2", null));
        dao.save(databaseConfiguration("name3", null));

        Page<DatabaseConfiguration> page = dao.findAll(PageRequest.of(0, 20));
        assertEquals(page.getTotalElements(), 3);
        assertEquals(page.getContent().size(), 3);
    }

    @Test
    public void shouldDeleteEntity() {
        var savedEntity = dao.save(databaseConfiguration(CONFIGURATION_NAME, null));
        var loadedEntity = dao.findById(savedEntity.getId());
        assertTrue(loadedEntity.isPresent());
        dao.delete(loadedEntity.get());

        assertFalse(dao.findById(savedEntity.getId()).isPresent());
    }

    @Test(expected = PersistenceException.class)
    public void shouldFailWhenTryingToSaveConfigurationWithSameName() {
        dao.save(databaseConfiguration(CONFIGURATION_NAME, null));
        dao.save(databaseConfiguration(CONFIGURATION_NAME, null));
        testEntityManager.flush();
        testEntityManager.clear();
    }
}
