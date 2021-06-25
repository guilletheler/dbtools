package com.gt.jdbcutils.helpers;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.gt.jdbcutils.components.Column;
import com.gt.jdbcutils.components.Database;
import com.gt.jdbcutils.components.ForeignKey;
import com.gt.jdbcutils.components.Index;
import com.gt.jdbcutils.components.PrimaryKey;
import com.gt.jdbcutils.components.Table;

public class JdbcMetaDecoder {

    public static Database buildDatabase(Connection conn) throws SQLException {

        Database database = new Database();

        database.setNombre(conn.getCatalog());

        DatabaseMetaData metaData = conn.getMetaData();

        try (ResultSet rs = metaData.getTables(conn.getCatalog(), null, "%", new String[] { "TABLE" })) {
            while (rs.next()) {
                buildTable(database, conn, rs.getString("TABLE_SCHEM"), rs.getString("TABLE_NAME"));
            }
        }

        try (ResultSet rs = metaData.getTables(conn.getCatalog(), null, "%", new String[] { "TABLE" })) {
            while (rs.next()) {
                buildForeignKeys(database, conn, rs.getString("TABLE_SCHEM"), rs.getString("TABLE_NAME"));
            }
        }

        return database;
    }

    private static void buildTable(Database database, Connection conn, String schemaName, String tableName)
            throws SQLException {

        Table table = new Table();

        table.setDatabase(database);
        table.setSchema(schemaName);
        table.setNombre(tableName);

        database.getTables().add(table);

        DatabaseMetaData metaData = conn.getMetaData();

        try (ResultSet rs = metaData.getColumns(conn.getCatalog(), schemaName, tableName, "%")) {
            while (rs.next()) {
                Column col = new Column();
                col.setTable(table);
                col.setName(rs.getString("COLUMN_NAME"));
                col.setAutoIncrement(rs.getString("IS_AUTOINCREMENT") != null
                        && rs.getString("IS_AUTOINCREMENT").equalsIgnoreCase("yes"));
                col.setDataType(rs.getInt("DATA_TYPE"));
                col.setLargo(rs.getInt("COLUMN_SIZE"));
                col.setDecimalDigits(rs.getInt("DECIMAL_DIGITS"));
                col.setNullable(rs.getInt("NULLABLE") == 0);
                col.setDefaultValue(rs.getString("COLUMN_DEF"));
                table.getColumns().add(col);
            }
        }

        try (ResultSet rs = metaData.getPrimaryKeys(conn.getCatalog(), schemaName, tableName)) {

            table.setPrimaryKey(new PrimaryKey());
            table.getPrimaryKey().setTable(table);
            while (rs.next()) {
                Column col = table.getColumnByName(rs.getString("COLUMN_NAME"));
                table.getPrimaryKey().getColumns().add(col);
            }
        }

        try (ResultSet rs = metaData.getIndexInfo(null, schemaName, tableName, false, false)) {
            while (rs.next()) {
                if (rs.getString("INDEX_NAME") == null || rs.getString("INDEX_NAME").equals("null")) {
                    continue;
                }

                if (rs.getString("TABLE_NAME") == null || rs.getString("TABLE_NAME").equals("null")) {
                    continue;
                }

                if (rs.getString("INDEX_NAME").startsWith("SYS_IDX_SYS_PK_")
                        || rs.getString("INDEX_NAME").startsWith("SYS_IDX_FK")) {
                    continue;
                }

                Index idx = table.getIndexByName(rs.getString("INDEX_NAME"));
                if (idx == null) {
                    idx = new Index();
                    idx.setTable(table);
                    idx.setName(rs.getString("INDEX_NAME"));
                    idx.setUnique(!rs.getBoolean("NON_UNIQUE"));
                    table.getIndexes().add(idx);
                }

                idx.getColumns().add(table.getColumnByName(rs.getString("COLUMN_NAME")));
            }
        }
    }

    private static void buildForeignKeys(Database database, Connection conn, String schemaName, String tableName)
            throws SQLException {

        DatabaseMetaData metaData = conn.getMetaData();

        try (ResultSet rs = metaData.getImportedKeys(conn.getCatalog(), schemaName, tableName)) {
            while (rs.next()) {
                Table table = database.getTableByName(schemaName, tableName);

                ForeignKey foreignKey = table.getForeignKeyByName(rs.getString("FK_NAME"));

                if (foreignKey == null) {
                    foreignKey = new ForeignKey();
                    foreignKey.setName(rs.getString("FK_NAME"));
                    foreignKey.setTable(table);
                    foreignKey.setRefTable(
                            database.getTableByName(rs.getString("PKTABLE_SCHEM"), rs.getString("PKTABLE_NAME")));
                    table.getForeignKeys().add(foreignKey);
                }

                foreignKey.getColumns().add(table.getColumnByName(rs.getString("FKCOLUMN_NAME")));
                foreignKey.getRefColumns().add(foreignKey.getRefTable().getColumnByName(rs.getString("PKCOLUMN_NAME")));
            }
        }
    }
}
