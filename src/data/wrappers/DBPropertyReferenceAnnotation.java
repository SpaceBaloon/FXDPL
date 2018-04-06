package data.wrappers;

import annotations.PersistedEntity;
import annotations.PersistedField;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * This implementation uses anotations to determine persistence.
 *
 * @author Belkin Sergei.
 */
public class DBPropertyReferenceAnnotation<T> extends DBPropertyReferenceProps<T>{
    
    private boolean init = false;
    private String dbFieldName = null;

    public String getDbFieldName() {
        return dbFieldName;
    }
    
    public DBPropertyReferenceAnnotation( String name, Class<?> cls ) {
        super( name, cls );
        //alow non persistable fields
//        if( !isReadable() || !isWritable() || !isPersistable() ) throw new IllegalArgumentException( 
//                "Property " + propertyRef.getName() + " is not persistable." );
    }

    @Override
    public boolean isPersistable() {
        initDBProperty();
        return dbFieldName != null;
    }
    
    protected void initDBProperty() {
        if( !init ) {
            init = true;
            Class<?> cls = propertyRef.getContainingClass();
            for( Annotation a : cls.getAnnotations() ) {
                if( a instanceof PersistedEntity && !( (PersistedEntity) a).value().trim().isEmpty() )  {
                    try {
                        Field field = cls.getDeclaredField( propertyRef.getName() );
                        for( Annotation af : field.getAnnotations() ) {
                            if( af instanceof PersistedField ) {
                                dbFieldName = ((PersistedField) af).value();
                                if( dbFieldName == null || dbFieldName.trim().isEmpty() )
                                    throw new NoSuchFieldException( "Annotation value is empty" );
                            }
                        }
                    } catch(NoSuchFieldException | SecurityException ex) {
                        ex.printStackTrace();
                        dbFieldName = null;
                    }
                    break;
                }
            }
        }
    }

    @Override
    public String getDBFieldName() {
        String res = null;
        if( isPersistable() )
            res = dbFieldName;
        return res;
    }
    
}
