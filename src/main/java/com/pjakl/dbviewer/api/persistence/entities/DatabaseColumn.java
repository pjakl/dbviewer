package com.pjakl.dbviewer.api.persistence.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DatabaseColumn {
    private String name;
    private String dataType;
    private boolean isNullable;
    private boolean isPrimaryKey;
}
