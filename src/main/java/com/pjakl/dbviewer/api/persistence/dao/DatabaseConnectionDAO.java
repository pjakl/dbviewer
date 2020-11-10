package com.pjakl.dbviewer.api.persistence.dao;


import com.pjakl.dbviewer.api.persistence.entities.DatabaseColumn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class DatabaseConnectionDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<String> getAllSchemaNames() {
        return jdbcTemplate.query("select nspname from pg_catalog.pg_namespace", (rs, rowNum) -> rs.getString("nspname"));
    }

    public List<String> getTablesForSchema(String schemaName) {
        return jdbcTemplate.query("select tablename from pg_catalog.pg_tables where schemaname = ?",
                new ArgumentPreparedStatementSetter(new String[]{schemaName}),
                (rs, rowNum) -> rs.getString("tablename"));
    }

    public List<DatabaseColumn> getColumnsForTable(String schemaName, String tableName) {
        return jdbcTemplate.query("SELECT c.column_name as name, c.data_type as dataType, " +
                                             "CASE WHEN c.is_nullable = 'YES' THEN TRUE ELSE FALSE END as isNullable,\n" +
                                             "(SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END\n" +
                                                "FROM information_schema.table_constraints tc\n" +
                                                "JOIN information_schema.constraint_column_usage AS ccu USING (constraint_schema, constraint_name)\n" +
                                                "WHERE c.table_schema = tc.constraint_schema\n" + "AND tc.table_name = c.table_name " +
                                                        "AND ccu.column_name = c.column_name AND constraint_type = 'PRIMARY KEY') AS isPrimaryKey\n" +
                                        "FROM information_schema.columns AS c\n" +
                                        "WHERE c.table_name = ? AND c.table_schema = ?",
                new ArgumentPreparedStatementSetter(new String[]{tableName, schemaName}),
                (rs, rowNum) -> new DatabaseColumn(
                                        rs.getString("name"),
                                        rs.getString("dataType"),
                                        rs.getBoolean("isNullable"),
                                        rs.getBoolean("isPrimaryKey")
                        )
        );
    }

    public Page<Map<String, Object>> getTablePreview(PageRequest pageRequest, String schemaName, String tableName) {
        String sqlQueryTotal =  String.format("SELECT COUNT(*) FROM %s.%s", schemaName, tableName);
        Long total = jdbcTemplate.queryForObject(sqlQueryTotal, Long.class);

        String sqlQuery = String.format("SELECT * FROM %s.%s LIMIT %d OFFSET %d", schemaName, tableName, pageRequest.getPageSize(), pageRequest.getOffset());
        var content =  jdbcTemplate.queryForList(sqlQuery);
        return new PageImpl<Map<String, Object>>(content, pageRequest, total);
    }
}
