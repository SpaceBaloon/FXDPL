package data.wrappers;

import data.entities.IEntity;
import java.lang.reflect.Field;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Basic implementation of {@code Wrapper}.
 * 
 * @author Belkin Sergei.
 * @param <T> returning type.
 * @param <C> type of class that will be instatianted.
 */
public class DefaultWrapper<T, C extends IEntity> implements Wrapper<T, C> {
    
    private boolean needInitProperties = true;
    private final List<IDBPropertyReference> properties = new ArrayList<>();

    @Override
    public List<IDBPropertyReference> getProperties() {
        return properties;
    }
    
    protected T getInstance( Class<C> cls ) 
            throws InstantiationException, IllegalAccessException {
        return ( T ) cls.newInstance();
    }
    
    protected IDBPropertyReference getPropertyRef( T result, Field field, Class<?> cls ) {
        DBPropertyReference res = new DBPropertyReference( field, cls );
        res.initDBProperty( ( IEntity ) result );
        return res;
    }
    
    protected void handleClassFields(T result, ResultSet rs, Class<?> classLoop) {
        Field[] fields = classLoop.getDeclaredFields();
        for( Field field : fields ) {
            IDBPropertyReference propertyRef = null;
            try {
                propertyRef = getPropertyRef( result, field, classLoop );
                if( propertyRef.isReadable() && propertyRef.isWritable() && propertyRef.isPersistable() ) {
                    if( needInitProperties ) properties.add( propertyRef );
                    String fieldType = propertyRef.getType().getName();
                    String fieldName = propertyRef.getDBFieldName();
                    switch( fieldType ) {
                        case "java.lang.Short":
                            propertyRef.set( result, rs.getShort( fieldName ) );
                            break;
                        case "java.lang.Integer":
                            propertyRef.set( result, rs.getInt( fieldName ) );
                            break;
                        case "java.lang.Long":
                            propertyRef.set( result, rs.getLong( fieldName ) );
                            break;
                        case "java.lang.Double":
                            propertyRef.set( result, rs.getDouble( fieldName ) );
                            break;
                        case "java.lang.Boolean":
                            propertyRef.set( result, rs.getBoolean( fieldName ) );
                            break;
                        case "java.math.BigDecimal":
                            propertyRef.set( result, rs.getBigDecimal( fieldName ) );
                            break;
                        case "java.time.LocalDate": {
                            Date date = rs.getDate( fieldName );
                            propertyRef.set( result, date == null ? null : date.toLocalDate() );
                            break;
                        }
                        case "java.time.LocalDateTime": {
                            Timestamp date = rs.getTimestamp( fieldName );
                            propertyRef.set( result, date == null ? null : date.toLocalDateTime() );
                            break;
                        }
                        default:
                            propertyRef.set( result, rs.getString( fieldName ) );
                    }
                }
            } catch( Exception ex) {
                /**
                 * Skip bad fields.
                 */
                String msg = "Error while handling field: ";
                if( propertyRef != null ) {
                    msg += propertyRef.toString();
                } else {
                    msg += "couldn't obtain propertyRef.";
                }
                System.err.println( msg );
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * This implementation does not throw any exceptions.
     * 
     * @param rs {@code ResultSet} instance from which data will be extracted.
     * @param cls {@code Class} that will be instatiated.
     * @return <T> It is allowed to return {@code null} value.
     */
    @Override
    public T wrap( ResultSet rs, Class<C> cls ) {                
        T result = null;
        try {
            result = getInstance( cls );
            
            Class<?> classLoop = cls;
            /**
             * Because {@code Class.getDeclaredFields()) provides all fields 
             * that declared only in given class we need to obtain all super classes fields 
             * that this one inherits.
            */
            while( classLoop != Object.class ) {
                try {
                    handleClassFields( result, rs, classLoop );
                } catch( Exception e ) {
                    /**
                     * If something went wrong we skip that portion of class.
                     */
                    e.printStackTrace();
                }
                classLoop = classLoop.getSuperclass();
            }
            //init properties list only for first time
            needInitProperties = false;
        } catch( Exception ex ) {
            ex.printStackTrace();
        }
        return result;
    }

    @Override
    public boolean hasProperty( String name ) {
        boolean res = false;
        if( properties != null && !properties.isEmpty() ) {
            res = properties.stream().filter( c -> c.getName().equals( name ) ).count() > 0;
        }
        return res;
    }

    protected IDBPropertyReference getProperty( String name, String fieldName ) {
        IDBPropertyReference res = null;
        if( properties != null && !properties.isEmpty() ) {
            res = properties.stream().filter( ( p ) -> {
                return p.getName().equals( name ) || p.getDBFieldName().equals( fieldName );
            } ).findFirst().orElse( null );
        }
        return res;
    }
    
    @Override
    public IDBPropertyReference getPropertyByName( String name ) {
        return getProperty( name, null );
    }
    

    @Override
    public IDBPropertyReference getPropertyByFieldName( String fieldName ) {
        return getProperty( null, fieldName );
    }    
    
}
