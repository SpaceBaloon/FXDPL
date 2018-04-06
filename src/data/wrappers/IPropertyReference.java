package data.wrappers;

import javafx.beans.property.Property;

/**
 *
 * @author Belkin Sergei
 */
public interface IPropertyReference<T> {
    
    boolean isWritable();
    boolean isReadable(); 
    void set( Object inst, T arg );
    T get( Object inst );
    Class<?> getType();
    String getName(); 
    
}
