package com.pjakl.dbviewer.api.views;

import com.pjakl.dbviewer.api.persistence.entities.DatabaseColumn;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DatabaseColumnView {
    private String name;
    private String dataType;
    private boolean isNullable;
    private boolean isPrimaryKey;

    public DatabaseColumnView(DatabaseColumn column) {
        this.name = column.getName();
        this.dataType = column.getDataType();
        this.isNullable = column.isNullable();
        this.isPrimaryKey = column.isPrimaryKey();
    }
}
