package com.gt.jdbcutils;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.gt.jdbcutils.components.Column;
import com.gt.jdbcutils.components.Database;
import com.gt.jdbcutils.components.Index;
import com.gt.jdbcutils.components.Table;
import com.gt.jdbcutils.helpers.JdbcMetaDecoder;

import org.junit.jupiter.api.Test;

public class JdbcMetaDecoderTest {
    
    @Test
    public void test() {
        // hsqldb.jar should be in the class path or made part of the current jar
        try {
            Class.forName("org.hsqldb.jdbcDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // connect to the database.   This will load the db files and start the
        // database if it is not alread running.
        // db_file_name_prefix is used to open or create files that hold the state
        // of the db.
        // It can contain directory names relative to the
        // current working directory
        try {

            // String folder = "C:/Users/guill/prog/java_mvn/copasp/localdb/database";
            String folder = "/home/prog/java_mvn/copasp/localdb/database";
            
            Connection conn = DriverManager.getConnection("jdbc:hsqldb:"
                                               + "file://" + folder,    // filenames
                                               "sa",                     // username
                                               "");


            assertNotNull(conn);

            System.out.println("Construyendo metadata");

            try {
                Database database = JdbcMetaDecoder.buildDatabase(conn);
                for(Table table : database.getTables()) {
                    System.out.println(table.getSchema() + "." + table.getNombre());
                    for(Column col : table.getColumns()) {
                        System.out.println("\t" + col.toString());
                    }
                    System.out.println("pk");
                    for(Column col : table.getPrimaryKey().getColumns()) {
                        System.out.println("\t" + col.toString());

                    }
                    System.out.println("indices");
                    for(Index idx : table.getIndexes()) {
                        System.out.println("\t" + idx.toString());

                    }
                }
    
            } catch(Exception ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "error", ex);
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }                      // password
    }
}
