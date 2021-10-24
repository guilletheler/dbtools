package com.gt.jdbcutils.helpers;

import java.sql.Types;
import java.util.Optional;
import java.util.stream.Collectors;

import com.gt.jdbcutils.components.Column;
import com.gt.jdbcutils.components.ForeignKey;
import com.gt.jdbcutils.components.Index;
import com.gt.jdbcutils.components.SqlDialect;
import com.gt.jdbcutils.components.Table;

public class DialectSyntaxHelper {

	public static void addTableCreationScript(StringBuilder sb, Table table, SqlDialect dialect) {

		sb.append("-- DROP TABLE ");
		addTableName(sb, table, dialect);
		sb.append(";\n\n");

		sb.append("CREATE TABLE ");
		addTableName(sb, table, dialect);
		sb.append(" (\n");

		for (int i = 0; i < table.getColumns().size(); i++) {
			if (i > 0) {
				sb.append(",\n");
			}
			sb.append("\t");
			addColumnDef(sb, table.getColumns().get(i), dialect);
		}

		if (table.getPrimaryKey() != null && !table.getPrimaryKey().getColumns().isEmpty()) {
			sb.append(",\n\t");
			addPrimaryKeyDef(sb, table, dialect);
		}
		sb.append("\n);\n\n");
	}

	public static void addPrimaryKeyDef(StringBuilder sb, Table table, SqlDialect dialect) {
		if (table.getPrimaryKey() == null || table.getPrimaryKey().getColumns().isEmpty()) {
			return;
		}
		sb.append("PRIMARY KEY (");
		for (int i = 0; i < table.getPrimaryKey().getColumns().size(); i++) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(formatMysql(table.getPrimaryKey().getColumns().get(i).getName(), dialect));
		}
		sb.append(")");
	}

	public static void addIndexesDef(StringBuilder sb, Table table, SqlDialect dialect) {
		for (Index idx : table.getIndexes()) {
			sb.append("CREATE ");
			if (idx.isUnique()) {
				sb.append("UNIQUE ");
			}
			sb.append("INDEX ");
			sb.append(formatMysql(idx.getName(), dialect));
			sb.append(" ON ");
			addTableName(sb, table, dialect);
			sb.append(" (");
			sb.append(idx.getColumns().stream().map(col -> formatMysql(col.getName(), dialect))
					.collect(Collectors.joining(", ")));
			sb.append(");\n\n");

		}
	}

	public static void addForeignKeysDef(StringBuilder sb, Table table, SqlDialect dialect) {
		if (table.getForeignKeys().isEmpty()) {
			return;
		}
		for (ForeignKey fk : table.getForeignKeys()) {
			sb.append("ALTER TABLE ");
			addTableName(sb, table, dialect);
			sb.append("\n\tADD CONSTRAINT ");
			sb.append(formatMysql(fk.getName(), dialect));
			sb.append("\n\tFOREIGN KEY (");
			sb.append(fk.getColumns().stream().map(col -> formatMysql(col.getName(), dialect))
					.collect(Collectors.joining(", ")));
			sb.append(") ");
			sb.append("\n\tREFERENCES ");
			addTableName(sb, fk.getRefTable(), dialect);
			sb.append("(");
			sb.append(fk.getRefColumns().stream().map(col -> formatMysql(col.getName(), dialect))
					.collect(Collectors.joining(", ")));
			sb.append(");\n\n");
		}
	}

	public static void addColumnDef(StringBuilder sb, Column column, SqlDialect dialect) {
		sb.append(formatMysql(column.getName(), dialect));

		sb.append(" ");

		switch (column.getDataType()) {
		case Types.CHAR:
		case Types.VARCHAR:
		case Types.LONGVARCHAR:
			addVarcharDefinition(sb, column, dialect);
			break;

		case Types.NUMERIC:
		case Types.DECIMAL:
			sb.append("NUMERIC");
			sb.append("(");
			sb.append(column.getLargo().toString());
			if (Optional.ofNullable(column.getDecimalDigits()).orElse(0) > 0) {
				sb.append(",");
				sb.append(column.getDecimalDigits().toString());
			}
			sb.append(")");
			break;

		case Types.BIT:
		case Types.BOOLEAN:
			addBooleanDefinition(sb, column, dialect);
			break;

		case Types.TINYINT:
			sb.append("TINYINT");
			break;

		case Types.SMALLINT:
			sb.append("SMALLINT");
			break;

		case Types.INTEGER:
			sb.append("INTEGER");
			break;

		case Types.BIGINT:
			sb.append("BIGINT");
			break;

		case Types.REAL:
		case Types.FLOAT:
			sb.append("FLOAT");
			break;

		case Types.DOUBLE:
			addDoubleDefinition(sb, column, dialect);
			break;

		case Types.BINARY:
		case Types.VARBINARY:
		case Types.LONGVARBINARY:
			addByteaDefinition(sb, column, dialect);
			break;

		case Types.DATE:
			sb.append("DATE");
			break;

		case Types.TIME:
			sb.append("TIME");
			break;

		case Types.TIMESTAMP:
			sb.append("TIMESTAMP WITHOUT TIME ZONE");
			break;
		}

		switch (dialect) {
		case HSQL:
			if (column.getDefaultValue() != null && !column.getDefaultValue().isEmpty()) {
				sb.append(" DEFAULT '");
				sb.append(column.getDefaultValue());
				sb.append("'");
			}
			if (!column.isNullable()) {
				sb.append(" NOT NULL");
			}
			break;
		case POSTGRES:
		case SQLSERVER:
		case MYSQL:
		default:
			if (!column.isNullable()) {
				sb.append(" NOT NULL");
			}
			if (column.getDefaultValue() != null && !column.getDefaultValue().isEmpty()) {
				sb.append(" DEFAULT ");

				if (column.getDataType() == Types.VARCHAR) {
					if (!column.getDefaultValue().startsWith("'")) {
						sb.append("'");
					}
				}

				sb.append(column.getDefaultValue());

				if (column.getDataType() == Types.VARCHAR) {
					if (!column.getDefaultValue().endsWith("'")) {
						sb.append("'");
					}
				}
			}
			break;
		}
	}

	private static void addByteaDefinition(StringBuilder sb, Column column, SqlDialect dialect) {
		switch (dialect) {
		case HSQL:
			sb.append("BLOB");
			break;
		case MYSQL:
			if (column.getLargo() < 256) {
				sb.append("TINYBLOB");
			} else if (column.getLargo() < 5536) {
				sb.append("BLOB");
			} else if (column.getLargo() < 16777216) {
				sb.append("MEDIUMBLOB");
			} else {
				sb.append("LONGBLOB");
			}
			break;
		case POSTGRES:
		case SQLSERVER:
		default:
			sb.append("BYTEA");
			break;
		}
	}

	private static void addBooleanDefinition(StringBuilder sb, Column column, SqlDialect dialect) {
		if (dialect.equals(SqlDialect.SQLSERVER)) {
			sb.append("BIT");
		} else {
			sb.append("BOOLEAN");
		}
	}

	private static void addDoubleDefinition(StringBuilder sb, Column column, SqlDialect dialect) {
		switch (dialect) {
		case HSQL:
		case POSTGRES:
			sb.append("DOUBLE PRECISION");
			break;
		case MYSQL:
		case SQLSERVER:
			sb.append("NUMERIC");
			break;
		default:
			break;

		}
	}

	private static void addVarcharDefinition(StringBuilder sb, Column column, SqlDialect dialect) {

		if (column.getLargo() > 1000 && dialect.equals(SqlDialect.HSQL)) {
			sb.append("TEXT");
		} else {
			sb.append("VARCHAR(").append(column.getLargo().toString()).append(")");
		}

	}

	public static void addTableName(StringBuilder sb, Table table, SqlDialect dialect) {
		if (table.getSchema() != null && !table.getSchema().isEmpty()) {
			sb.append(formatMysql(table.getSchema(), dialect));
			sb.append(".");
		}

		sb.append(formatMysql(table.getNombre(), dialect));
	}

	private static String formatMysql(String str, SqlDialect dialect) {
		if (dialect == SqlDialect.MYSQL) {
			return "`" + str + "`";
		}
		return str;
	}

	public static Class<?> SQLToJavaClass(Column column) {

		switch (column.getDataType()) {
		case Types.BIGINT:
			return long.class;
		case Types.LONGVARBINARY:
			return byte[].class;
		case Types.BINARY:
			return byte[].class;
		case Types.BIT:
			return boolean.class;
		case Types.CHAR:
			return String.class;
		case Types.DATE:
			return java.sql.Date.class;
		case Types.TIMESTAMP:
			return java.sql.Timestamp.class;
		case Types.DECIMAL:
			return java.math.BigDecimal.class;
		case Types.DOUBLE:
			return double.class;
		case Types.INTEGER:
			return int.class;
		case Types.NCHAR:
			return String.class;
		case Types.LONGVARCHAR:
			return String.class;
		case Types.NUMERIC:
			return java.math.BigDecimal.class;
		case Types.VARCHAR:
			return String.class;
		case Types.NVARCHAR:
			return String.class;
		case Types.REAL:
			return float.class;
		case Types.SMALLINT:
			return short.class;
		case Types.TINYINT:
			return short.class;
		case Types.VARBINARY:
			return byte[].class;
		}
		
		return Object.class;
	}
}
