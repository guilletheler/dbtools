package com.gt.jdbcutils;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
public class Table {

    Database database;

    String schema;
    String nombre;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    List<Column> columns = new ArrayList<>();
    PrimaryKey primaryKey;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    List<Index> indexes = new ArrayList<>();

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    List<ForeignKey> foreignKeys = new ArrayList<>();

    public Column getColumnByName(String columnName) {
        return columns.stream().filter(col -> col.getName().equals(columnName)).findAny().orElse(null);
    }

    public Index getIndexByName(String indexName) {
        return indexes.stream().filter(col -> col.getName().equals(indexName)).findAny().orElse(null);
    }

    public ForeignKey getForeignKeyByName(String foreignKeyName) {
        return foreignKeys.stream().filter(col -> col.getName().equals(foreignKeyName)).findAny().orElse(null);
    }
}
