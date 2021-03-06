package com.gt.jdbcutils.components;

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
    
    boolean fkFetched = false;

    public Column getColumnByName(String columnName) {
        return columns.stream().filter(col -> col.getName().equalsIgnoreCase(columnName)).findAny().orElse(null);
    }

    public Index getIndexByName(String indexName) {
        return indexes.stream().filter(col -> col.getName().equalsIgnoreCase(indexName)).findAny().orElse(null);
    }

    public ForeignKey getForeignKeyByName(String foreignKeyName) {
        return foreignKeys.stream().filter(col -> col.getName().equalsIgnoreCase(foreignKeyName)).findAny().orElse(null);
    }
    
    public Column getManyToOne(Column column) {

		for (ForeignKey fk : getForeignKeys()) {
			if (fk.getColumns().size() == 1 && fk.getRefColumns().size() == 1
					&& fk.getColumns().get(0).equals(column)) {
				return fk.getRefColumns().get(0);
			}
		}

		return null;
	}
}
