package com.gt.jdbcutils.helpers;

import java.sql.JDBCType;
import java.sql.Types;

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
        
        switch (column.getDataType()) {
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
                addVarcharDefinition(sb, column, dialect);
                break;

            case Types.NUMERIC:
            case Types.DECIMAL:
                addNumericDefinition(sb, column, dialect);
                break;

            case Types.BIT:
            case Types.BOOLEAN:
                result = Boolean.class;
                break;

            case Types.TINYINT:
                result = Byte.class;
                break;

            case Types.SMALLINT:
                result = Short.class;
                break;

            case Types.INTEGER:
                result = Integer.class;
                break;

            case Types.BIGINT:
                result = Long.class;
                break;

            case Types.REAL:
            case Types.FLOAT:
                result = Float.class;
                break;

            case Types.DOUBLE:
                result = Double.class;
                break;

            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                result = Byte[].class;
                break;

            case Types.DATE:
                result = java.sql.Date.class;
                break;

            case Types.TIME:
                result = java.sql.Time.class;
                break;

            case Types.TIMESTAMP:
                result = java.sql.Timestamp.class;
                break;
        }        

        switch(column.getDataType()) {
            case java.sql.Types.BIGINT:
            break;
        }
    }

    private static void addNumericDefinition(StringBuilder sb, Column column, SqlDialect dialect) {
    }

    private static void addVarcharDefinition(StringBuilder sb, Column column, SqlDialect dialect) {

        
        if(column.getLargo() > 1000 && dialect.equals(SqlDialect.HSQL)) {
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

}
