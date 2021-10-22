package com.gt.jdbcutils.helpers;

import java.sql.Types;
import java.util.Optional;

import com.gt.jdbcutils.components.Column;
import com.gt.jdbcutils.components.SqlDialect;
import com.gt.jdbcutils.components.Table;

public class DialectSyntaxHelper {

	public static void addColumnDef(StringBuilder sb, Column column, SqlDialect dialect) {
		switch (dialect) {
		case HSQL:
		case POSTGRES:
		case SQLSERVER:
		default:
			sb.append(column.getName());
			break;
		case MYSQL:
			sb.append("`").append(column.getName()).append("`");
			break;
		}

		sb.append(" ");

		boolean addSize = true;

		switch (column.getDataType()) {
		case Types.CHAR:
		case Types.VARCHAR:
		case Types.LONGVARCHAR:
			addVarcharDefinition(sb, column, dialect);
			break;

		case Types.NUMERIC:
		case Types.DECIMAL:
			sb.append("NUMERIC");
			break;

		case Types.BIT:
		case Types.BOOLEAN:
			addBooleanDefinition(sb, column, dialect);
			addSize = false;
			break;

		case Types.TINYINT:
			sb.append("TINYINT");
			addSize = false;
			break;

		case Types.SMALLINT:
			sb.append("SMALLINT");
			addSize = false;
			break;

		case Types.INTEGER:
			sb.append("INTEGER");
			break;

		case Types.BIGINT:
			sb.append("BIGINT");
			addSize = false;
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
			sb.append("TIMESTAMP");

			break;
		}

		if (addSize) {
			sb.append("(");
			sb.append(column.getLargo().toString());
			if (Optional.ofNullable(column.getDecimalDigits()).orElse(0) > 0) {
				sb.append(",");
				sb.append(column.getDecimalDigits().toString());
			}
			sb.append(")");
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
		switch (dialect) {
		case MYSQL:
			sb.append("`").append(table.getSchema()).append("`").append(".").append("`").append(table.getNombre())
					.append("`");
			break;
		case HSQL:
		case POSTGRES:
		case SQLSERVER:
		default:
			sb.append(table.getSchema()).append(".").append(table.getNombre());
			break;
		}

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
