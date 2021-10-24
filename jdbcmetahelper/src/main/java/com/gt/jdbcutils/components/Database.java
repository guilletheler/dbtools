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
		return tables.stream().filter(table -> table.getSchema().equalsIgnoreCase(schemaName)
				&& table.getNombre().equalsIgnoreCase(tableName)).findAny().orElse(null);
	}

	public List<Table> getOneToMany(Table table) {
		
		List<Table> ret = new ArrayList<>();
		
		for(Table tableRef : getTables()) {
			for(ForeignKey fk : tableRef.foreignKeys) {
				if(fk.getRefTable().equals(table)) {
					ret.add(tableRef);
				}
			}
		}
		
		return ret;
	}

	public Column getManyToOne(Table table, Column column) {

		for (ForeignKey fk : table.getForeignKeys()) {
			if (fk.getColumns().size() == 1 && fk.getRefColumns().size() == 1
					&& fk.getColumns().get(0).equals(column)) {
				return fk.getRefColumns().get(0);
			}
		}

		return null;
	}
}
