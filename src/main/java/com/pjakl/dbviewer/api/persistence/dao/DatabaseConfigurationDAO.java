package com.pjakl.dbviewer.api.persistence.dao;

import com.pjakl.dbviewer.api.persistence.entities.DatabaseConfiguration;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DatabaseConfigurationDAO extends PagingAndSortingRepository<DatabaseConfiguration, UUID> {
}
