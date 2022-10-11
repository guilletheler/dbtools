package com.gt.dbtools.pojogentest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import pojogen.PojoGen;

public class MainClass {

	public static void main(String[] argv) {
		try {
			
			Class.forName("org.hsqldb.jdbcDriver");
			
			Connection conn = DriverManager.getConnection("jdbc:hsqldb:file://C:/Users/guill/prog/hsqldb/lotinfo/database", "sa", "");
			
			PojoGen pg = new PojoGen();
			pg.setProjectPackageName("com.gt.lotinfo.web");
			pg.setEntitiesSubPackageName("model.agencias");
			pg.setConn(conn);
			System.out.println(pg.buildPojo("public", "PREMIOS", true));
			
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}