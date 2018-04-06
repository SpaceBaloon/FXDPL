package data.wrappers;

import data.entities.IEntity;
import data.entities.ILookupHandler;
import java.sql.ResultSet;

/**
 * This implementation handles situation with two {@code ResultSet} - master and lookup.
 * {@code LookupWrapper} wrap master {@code ResultSet} item and then provide opportunity to 
 * handle obtained object before return it.
 * Lookup data handles with {@code ILookupHandler} passed to the constructor.
 * @author Belkin Sergei.
 * @param <T> type of lookup object that will be returned.
 * Lookup data must be reflected at last level of inheritance.
 * @param <C> 
 */
public class LookupWrapper<T, C extends IEntity> extends DefaultWrapper<T, C> {

    private T result;
    private final ILookupHandler<T> handler;
    @Override
    protected T getInstance( Class<C> cls ) throws InstantiationException, IllegalAccessException {
        return result;
    }

    public LookupWrapper( ILookupHandler<T> handler ) {
        this.handler = handler;
    }    
    
    @Override
    public T wrap( ResultSet rs, Class<C> cls ) {
        try {
            result = ( T ) cls.newInstance();
            
            Class<?> superCls = result.getClass().getSuperclass();
            if( !superCls.getName().equals( "java.lang.Object" ) )
                super.wrap(rs, ( Class<C> ) superCls);
            /**
             * For lookup data.
             */
            if( handler != null )
                handler.handleData( rs, result );
        } catch( IllegalAccessException | InstantiationException ex ) {
            ex.printStackTrace();
        }
        return result;
    }
    
}