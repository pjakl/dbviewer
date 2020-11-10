package com.pjakl.dbviewer.api.controllers;

import com.pjakl.dbviewer.api.models.DatabaseConfigurationModel;
import com.pjakl.dbviewer.api.persistence.entities.DatabaseConfiguration;
import com.pjakl.dbviewer.api.views.DatabaseColumnView;
import com.pjakl.dbviewer.services.DatabaseConfigurationService;
import com.pjakl.dbviewer.services.DatabaseConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "configurations", produces = MediaType.APPLICATION_JSON_VALUE)
public class DatabaseConfigurationController {

    @Autowired
    private DatabaseConfigurationService databaseConfigurationService;

    @Autowired
    private DatabaseConnectionService databaseConnectionService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<DatabaseConfigurationModel> listConfigurations(Pageable pageable) {
        var result = databaseConfigurationService.listConfigurations(pageable);
        return new PageImpl<>(result.getContent().stream().map(DatabaseConfigurationModel::new).collect(Collectors.toList()));
    }

    @GetMapping("/{configurationId}")
    @ResponseStatus(HttpStatus.OK)
    public DatabaseConfigurationModel getConfiguration(@PathVariable("configurationId") UUID configurationId) {
        var conn = databaseConfigurationService.getConfigurationById(configurationId);
        return new DatabaseConfigurationModel(conn);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DatabaseConfigurationModel createConfiguration(@Valid @RequestBody DatabaseConfigurationModel model) {
        return new DatabaseConfigurationModel(databaseConfigurationService.saveConfiguration(model.toEntity()));
    }

    @PutMapping(value = "/{configurationId}")
    @ResponseStatus(HttpStatus.OK)
    public DatabaseConfigurationModel editConfiguration(@PathVariable("configurationId") UUID configurationId, @RequestBody @Valid DatabaseConfigurationModel model) {
        return new DatabaseConfigurationModel(databaseConfigurationService.updateConfiguration(configurationId, model.toEntity()));
    }

    @DeleteMapping(value = "/{configurationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteConfiguration(@PathVariable("configurationId") UUID configurationId) {
        databaseConfigurationService.deleteConfiguration(configurationId);
    }

    @GetMapping(value = "/{configurationId}/schemas")
    @ResponseStatus(HttpStatus.OK)
    public List<String> getSchemas(@PathVariable("configurationId") UUID configurationId) {
        DatabaseConfiguration currentConfiguration = databaseConfigurationService.getConfigurationById(configurationId, true);
        return databaseConnectionService.getAllSchemaNames(currentConfiguration);
    }

    @GetMapping(value = "/{configurationId}/schemas/{schemaName}/tables")
    @ResponseStatus(HttpStatus.OK)
    public List<String> getTablesForSchema(@PathVariable("configurationId") UUID configurationId, @PathVariable("schemaName") String schemaName) {
        DatabaseConfiguration currentConfiguration = databaseConfigurationService.getConfigurationById(configurationId, true);
        return databaseConnectionService.getTablesForSchema(currentConfiguration, schemaName);
    }

    @GetMapping(value = "/{configurationId}/schemas/{schemaName}/tables/{tableName}/columns")
    @ResponseStatus(HttpStatus.OK)
    public List<DatabaseColumnView> getColumnsForTable(@PathVariable("configurationId") UUID configurationId, @PathVariable("schemaName") String schemaName, @PathVariable("tableName") String tableName) {
        DatabaseConfiguration currentConfiguration = databaseConfigurationService.getConfigurationById(configurationId, true);
        return databaseConnectionService.getColumnsForTable(currentConfiguration, schemaName, tableName).stream().map(DatabaseColumnView::new).collect(Collectors.toList());
    }

    @GetMapping(value = "/{configurationId}/schemas/{schemaName}/tables/{tableName}/preview")
    @ResponseStatus(HttpStatus.OK)
    public Page<Map<String, Object>> getTablePreview(@PathVariable("configurationId") UUID configurationId, @PathVariable("schemaName") String schemaName, @PathVariable("tableName") String tableName, Pageable pageable) {
        DatabaseConfiguration currentConfiguration = databaseConfigurationService.getConfigurationById(configurationId, true);
        return databaseConnectionService.getTablePreview(currentConfiguration, (PageRequest) pageable, schemaName, tableName);
    }

}
