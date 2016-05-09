package it.quartara.boser.console.helper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ConnectionHelper {
	
	private static final Logger log = LoggerFactory.getLogger(ConnectionHelper.class);
	private static final String user = "boser";
	private static final String password = "boser";
	
	static {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			log.error("driver mysql non trovato", e);
		}
	}
	
	public static Connection getConnection(String dnsName) throws SQLException {
		String url = "jdbc:mysql://"+dnsName+":3306/boser";
		Connection conn = DriverManager.getConnection(url, user, password);
		return conn;
	}

}
