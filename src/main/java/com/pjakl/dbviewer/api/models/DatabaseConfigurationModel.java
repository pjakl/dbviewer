package com.pjakl.dbviewer.api.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pjakl.dbviewer.api.persistence.entities.DatabaseConfiguration;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DatabaseConfigurationModel {
    private UUID id;
    @NotBlank
    private String name;
    @NotBlank
    private String hostname;
    @NotNull
    @Min(0)
    @Max(65535)
    private Integer port;
    @NotBlank
    private String databaseName;
    @NotBlank
    private String username;
    @NotBlank
    private String password;

    public DatabaseConfigurationModel(DatabaseConfiguration entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.hostname = entity.getHostname();
        this.port = entity.getPort();
        this.databaseName = entity.getDatabaseName();
        this.username = entity.getUsername();
        this.password = entity.getPassword();
    }

    public DatabaseConfiguration toEntity() {
        DatabaseConfiguration con = new DatabaseConfiguration();
        con.setId(this.id);
        con.setName(this.name);
        con.setHostname(this.hostname);
        con.setPort(this.port);
        con.setDatabaseName(this.databaseName);
        con.setUsername(this.username);
        con.setPassword(this.password);
        return con;
    }


}
