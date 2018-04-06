package data.wrappers;

import com.sun.javafx.property.PropertyReference;
import data.entities.IEntity;
import data.entities.IField;
import java.lang.reflect.Field;
import javafx.beans.property.Property;

/**
 *
 * @author Belkin Sergei
 * @param <T>
 */
public class DBPropertyReferenceProps<T> implements IDBPropertyReference<T> {
    
    protected final PropertyReference<T> propertyRef;
    protected IField dbField;
    
    @Override
    public boolean isWritable() {
        return propertyRef.isWritable();
    }
    
    @Override
    public boolean isReadable() {
        return propertyRef.isReadable();
    }
    
    @Override
    public boolean isPersistable() {
        return dbField != null;
    }
    
    @Override
    public String getName() {
        return propertyRef.getName();
    }

    public DBPropertyReferenceProps( Field field, Class<?> cls ) {
        if( field == null ) throw new NullPointerException( "Field argument must be specified." );        
        this.propertyRef = new PropertyReference<>( cls, field.getName() );
    }
    
    public DBPropertyReferenceProps( String name, Class<?> cls ) {
        this.propertyRef = new PropertyReference<>( cls, name );
    }
    
    public void initDBProperty( IEntity inst ) {
        if( inst == null ) throw new NullPointerException( "IEntity argument must be set." );
        String fieldPropName = "FIELD_NAME_" + propertyRef.getName().toUpperCase();
        for( IField e : ( ( IEntity ) inst ).getFieldNames() ) {
            String enumName = e.toString();
            String enumValue = e.getDBFieldName();
            if( fieldPropName.equals( enumName ) 
                    && enumValue != null 
                    && !enumValue.trim().isEmpty() ) {
                dbField = e;
                break;
            }
        }
    }

    @Override
    public void set( Object inst, T arg ) {
        propertyRef.set(inst, arg );
    }

    @Override
    public T get( Object inst ) {
        return propertyRef.get( inst );
    }

    @Override
    public Class<?> getType() {
        return propertyRef.getType();
    }

    @Override
    public String getDBFieldName() {
        String res = null;
        if( isPersistable() )
            res = dbField.getDBFieldName();
        return res;
    }
    
}