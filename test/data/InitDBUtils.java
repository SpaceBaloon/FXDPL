package data;

import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 * @author Belkin Sergei
 */
public class InitDBUtils {
    
    private static final String MAIN_CON_URL = "P6/C:/DATA/CLIENTS.FDB";
    private static final String MAIN_CON_USER = "SYSDBA";
    private static final String MAIN_CON_PASW = "masterkey";    
    private static Connection mainConnection = null;
        
    public static synchronized Connection getConnection() throws SQLException {
        if( mainConnection == null ) {
            mainConnection = FirebirdConnection.getConnection( MAIN_CON_URL, MAIN_CON_USER, MAIN_CON_PASW );
        }
        return mainConnection;
    }
    
}
