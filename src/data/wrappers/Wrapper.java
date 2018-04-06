package data.wrappers;

import java.sql.ResultSet;
import java.util.List;

/**
 *
 * @author Belkin Sergei
 * @param <T> type that reflect structure of retrieved data.
 */
public interface Wrapper<T> {
    T wrap( ResultSet rs, Class<?> cls );
    List<IDBPropertyReference> getProperties();
    boolean hasProperty( String name );
    IDBPropertyReference getPropertyByName( String name );
    IDBPropertyReference getPropertyByFieldName( String fieldName );
}
