package data.wrappers;

import data.entities.IEntity;
import data.entities.IField;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *
 * @author BelkinSergei
 * @param <T>
 */
public class DBPropertyReference<T> implements IDBPropertyReference<T> {
    
    private final Field field;
    private final Class<?> cls;
    private IField dbFieldInfo;
    private Method readMethod;
    private Method writeMethod;

    public Class<?> getCls() {
        return cls;
    }

    public IField getDbFieldInfo() {
        return dbFieldInfo;
    }

    @Override
    public boolean isReadable() {
        return readMethod != null;
    }
    
    @Override
    public boolean isWritable() {
        return writeMethod != null;
    }

    @Override
    public boolean isPersistable() {
        return dbFieldInfo != null;
    }

    private void initClassProperty() {
        String fieldName = field.getName();
        String methodFieldName = fieldName.substring( 0, 1 ).toUpperCase() + fieldName.substring( 1 );
        String setMethodName = "set" + methodFieldName;
        String getMethodName = "get" + methodFieldName;
        String isMethodName = "is" + methodFieldName;
        try {
            writeMethod = cls.getMethod( setMethodName, field.getType() );
            try {
                readMethod = cls.getMethod( getMethodName, ( Class<?>[] ) null );
            } catch( NoSuchMethodException ex ) {
                readMethod = cls.getMethod( isMethodName, ( Class<?>[] ) null );
            }
        } catch( NoSuchMethodException ex ) {
            throw new RuntimeException( ex );
        }
    }

    public DBPropertyReference( Field field, Class<?> cls ) {
        if( field == null || cls == null ) {
            throw new NullPointerException( "Parameters can't be null." );
        }
        this.field = field;
        this.cls = cls;
        initClassProperty();
    }

    public void initDBProperty( IEntity inst ) {
        if( inst == null ) {
            throw new NullPointerException( "IEntity must be set." );
        }
        String fieldPropertyName = "FIELD_NAME_" + field.getName().toUpperCase();
        for( IField e : ( ( IEntity ) inst ).getFieldNames() ) {
            String enumName = e.toString();
            String enumValue = e.getDBFieldName();
            if( fieldPropertyName.equals( enumName ) && enumValue != null && !enumValue.trim().isEmpty() ) {
                dbFieldInfo = e;
                break;
            }
        }
    }

    @Override
    public void set( Object inst, T arg ) {        
        if( isWritable() ) {
            try {
                writeMethod.invoke( inst, arg );
            } catch( IllegalAccessException | IllegalArgumentException | InvocationTargetException ex ) {                
                throw new RuntimeException( ex );
            }
        }
    }

    @Override
    public T get( Object inst ) {
        T result = null;
        if( isReadable() ) {
            try {
                result = ( T ) readMethod.invoke( inst, ( Object[] ) null );
            } catch( IllegalAccessException | IllegalArgumentException | InvocationTargetException ex ) {
                throw new RuntimeException( ex );
            }
        }
        return result;
    }

    @Override
    public String getDBFieldName() {
        String res = null;
        if( isPersistable() )
            res = dbFieldInfo.getDBFieldName();
        return res;
    }

    @Override
    public Class<?> getType() {
        return field.getType();
    }

    @Override
    public String getName() {
        return field.getName();
    }
    
}
