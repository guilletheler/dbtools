package com.gt.jdbcutils.components;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
public class Database {
    String nombre;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    List<Table> tables = new ArrayList<>();

    public Table getTableByName(String schemaName, String tableName) {
        return tables.stream()
                .filter(table -> table.getSchema().equals(schemaName) && table.getNombre().equals(tableName)).findAny()
                .orElse(null);
    }
}
