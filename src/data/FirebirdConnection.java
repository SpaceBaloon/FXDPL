/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 *
 * @author P5
 */
public class FirebirdConnection {
    
    /**
     * "jdbc:firebirdsql://p6/c:/data/clients.fdb?encoding=unicode_fss";
     */
    private static final String URL_PREFIX = 
            "jdbc:firebirdsql://";
    
    public static Connection getConnection( String url, String user, String passw ) 
            throws SQLException {
        Properties props = new Properties();
        props.setProperty( "user", user);
        props.setProperty( "password", passw);
        props.setProperty( "encoding", "unicode_fss");
        String connectionURL = URL_PREFIX + url;
        return DriverManager.getConnection( connectionURL, props );        
    }    
    
}
