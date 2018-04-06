package data.entities;

import java.sql.ResultSet;

/**
 *
 * @author Belkin Sergei
 */
public interface ILookupHandler<T> {
    
    void handleData( ResultSet rs, T result );
    
}
