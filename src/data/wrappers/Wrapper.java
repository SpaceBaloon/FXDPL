package data.wrappers;

import data.entities.IEntity;
import java.sql.ResultSet;
import java.util.List;

/**
 *
 * @author Belkin Sergei
 * @param <T> type that reflect structure of retrieved data.
 * @param <C> class that implements IEntity and T interface. 
 */
public interface Wrapper<T, C extends IEntity> {
    T wrap( ResultSet rs, Class<C> cls );
    List<IDBPropertyReference> getProperties();
    boolean hasProperty( String name );
    IDBPropertyReference getPropertyByName( String name );
    IDBPropertyReference getPropertyByFieldName( String fieldName );
}
