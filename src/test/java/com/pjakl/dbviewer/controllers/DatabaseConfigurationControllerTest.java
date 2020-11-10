package com.pjakl.dbviewer.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pjakl.dbviewer.api.persistence.entities.DatabaseConfiguration;
import com.pjakl.dbviewer.exception.DomainEntityNotFoundException;
import com.pjakl.dbviewer.services.DatabaseConfigurationService;
import com.pjakl.dbviewer.services.DatabaseConnectionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.Collections;
import java.util.UUID;

import static com.pjakl.dbviewer.Stub.databaseConfiguration;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.ResultMatcher.matchAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest
public class DatabaseConfigurationControllerTest {

    public static final String CONFIGURATION_NAME = "name";
    public static final UUID CONFIGURATION_ID = UUID.randomUUID();

    public DatabaseConfiguration databaseConfiguration;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    private MockMvc mvc;
    @MockBean
    private DatabaseConfigurationService databaseConfigurationService;
    @MockBean
    private DatabaseConnectionService databaseConnectionService;

    @Before
    public void onSetup() {
        databaseConfiguration = databaseConfiguration(CONFIGURATION_NAME, CONFIGURATION_ID);
    }

    @Test
    public void shouldReturnPaginatedListOfConfigurations() throws Exception {

        when(databaseConfigurationService.listConfigurations(any())).thenReturn(new PageImpl<>(Collections.singletonList(databaseConfiguration)));

        mvc.perform(get("/configurations")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size", is(1)))
                .andExpect(databaseConfigurationChecks("$.content[0].", databaseConfiguration));
    }

    @Test
    public void shouldReturnConfigurationById() throws Exception {

        when(databaseConfigurationService.getConfigurationById(CONFIGURATION_ID)).thenReturn(databaseConfiguration);

        mvc.perform(get("/configurations/" + CONFIGURATION_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(databaseConfigurationChecks("$.", databaseConfiguration));
    }

    @Test
    public void shouldReturn404UponNonExistingID() throws Exception {
        when(databaseConfigurationService.getConfigurationById(CONFIGURATION_ID))
                .thenThrow(new DomainEntityNotFoundException("Configuration with id=" + CONFIGURATION_ID + " doesn't exist"));

        mvc.perform(get("/configurations/" + CONFIGURATION_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Configuration with id=" + CONFIGURATION_ID + " doesn't exist")));
    }

    @Test
    public void shouldReturnProperStatusUponDelete() throws Exception {
        mvc.perform(delete("/configurations/" + CONFIGURATION_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void shouldReturn404UponDeleteNonExisting() throws Exception {
        doThrow(new DomainEntityNotFoundException("Configuration with id=" + CONFIGURATION_ID + " doesn't exist")).when(databaseConfigurationService).deleteConfiguration(CONFIGURATION_ID);

        mvc.perform(delete("/configurations/" + CONFIGURATION_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Configuration with id=" + CONFIGURATION_ID + " doesn't exist")));
    }

    @Test
    public void shouldCreateNewConfiguration() throws Exception {

        when(databaseConfigurationService.saveConfiguration(any())).thenReturn(databaseConfiguration);

        mvc.perform(post("/configurations")
                .content(objectMapper.writeValueAsBytes(databaseConfiguration))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(databaseConfigurationChecks("$.", databaseConfiguration));
    }

    @Test
    public void shouldReturn405UponPatchRequest() throws Exception {
        var dbConfiguration = databaseConfiguration(CONFIGURATION_NAME, CONFIGURATION_ID);

        mvc.perform(patch("/configurations/" + CONFIGURATION_ID)
                .content(objectMapper.writeValueAsBytes(dbConfiguration))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed());
    }


    @Test
    public void shouldReturnNotAllowedUponPatch() throws Exception {

        mvc.perform(patch("/configurations/" + CONFIGURATION_ID)
                .content(objectMapper.writeValueAsBytes(databaseConfiguration))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void shouldValidateNotBlankName() throws Exception {
        databaseConfiguration.setName("");
        mvc.perform(post("/configurations")
                .content(objectMapper.writeValueAsBytes(databaseConfiguration))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Invalid arguments")))
                .andExpect(jsonPath("$.detailedMessage", is("[name: must not be blank]")));
    }

    @Test
    public void shouldValidateNonNullName() throws Exception {
        databaseConfiguration.setName(null);
        mvc.perform(post("/configurations")
                .content(objectMapper.writeValueAsBytes(databaseConfiguration))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Invalid arguments")))
                .andExpect(jsonPath("$.detailedMessage", is("[name: must not be blank]")));
    }

    @Test
    public void shouldValidateNotNullPassword() throws Exception {
        databaseConfiguration.setPassword(null);
        mvc.perform(post("/configurations")
                .content(objectMapper.writeValueAsBytes(databaseConfiguration))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Invalid arguments")))
                .andExpect(jsonPath("$.detailedMessage", is("[password: must not be blank]")));
    }

    @Test
    public void shouldValidateNotNullDatabaseName() throws Exception {
        databaseConfiguration.setDatabaseName(null);
        mvc.perform(post("/configurations")
                .content(objectMapper.writeValueAsBytes(databaseConfiguration))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Invalid arguments")))
                .andExpect(jsonPath("$.detailedMessage", is("[databaseName: must not be blank]")));
    }

    @Test
    public void shouldValidateNotNullHostName() throws Exception {
        databaseConfiguration.setHostname(null);
        mvc.perform(post("/configurations")
                .content(objectMapper.writeValueAsBytes(databaseConfiguration))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Invalid arguments")))
                .andExpect(jsonPath("$.detailedMessage", is("[hostname: must not be blank]")));
    }

    @Test
    public void shouldValidateNotNullPort() throws Exception {
        databaseConfiguration.setPort(null);
        mvc.perform(post("/configurations")
                .content(objectMapper.writeValueAsBytes(databaseConfiguration))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Invalid arguments")))
                .andExpect(jsonPath("$.detailedMessage", is("[port: must not be null]")));
    }

    @Test
    public void shouldValidatePortRange() throws Exception {
        databaseConfiguration.setPort(999999);
        mvc.perform(post("/configurations")
                .content(objectMapper.writeValueAsBytes(databaseConfiguration))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Invalid arguments")))
                .andExpect(jsonPath("$.detailedMessage", is("[port: must be less than or equal to 65535]")));
    }
    

    private ResultMatcher databaseConfigurationChecks(String jsonRootPath, DatabaseConfiguration dbConfiguration) throws Exception {
        return matchAll(
                jsonPath(jsonRootPath + "name", is(dbConfiguration.getName())),
                jsonPath(jsonRootPath + "hostname", is(dbConfiguration.getHostname())),
                jsonPath(jsonRootPath + "id", is(dbConfiguration.getId().toString())),
                jsonPath(jsonRootPath + "databaseName", is(dbConfiguration.getDatabaseName())),
                jsonPath(jsonRootPath + "username", is(dbConfiguration.getUsername())),
                jsonPath(jsonRootPath + "password", is(dbConfiguration.getPassword()))
        );
    }

}
