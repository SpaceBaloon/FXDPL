package data.managers;

import data.QueryObject;
import data.wrappers.Wrapper;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;

/**
 * This class serves data managing retrieved from DB.
 * Data is cached and represents with ObservableList.
 * By means of this class you have next opportunities:
 * <ul>
 *  <li>Retrieve data using {@code BasicDAO.open()}.</li>
 *  <li>Cached data by default.</li>
 *  <li>Manipulate data (add, update, delete) via {@code BasicDAO.getData()} property.</li>
 *  <li>Cancel updates using {@code BasicDAO.cancel()}</li>
 *  <li>Commit changes to DB via {@code BasicDAO.commit()}</li>
 * </ul>
 * TODO: change data representation from ArrayList to Set to avoid duplicates.
 * @author Belkin Sergei
 * @param <T> type that reflect quered structure.
 */
public abstract class BasicDAO<T> {

    private boolean skip = false;
    
    /**
     * Helper that represents pair of updatable object and updating object and
     * also index that reflect position of updatable object.
     * Index is needed because list can contains duplicates.
     * 
     * @param <T> 
     */
    public static class Pair<T> {
        
        private final Integer idx;

        public Integer getIdx() {
            return idx;
        }
        private T lastValue;
        private T initValue;

        public void setLastValue( T lastValue ) {
            this.lastValue = lastValue;
        }

        public void setInitValue( T initValue ) {
            this.initValue = initValue;
        }

        public T getLastValue() {
            return lastValue;
        }

        public T getInitValue() {
            return initValue;
        }

        public Pair( Integer idx, T initValue, T lastValue ) {
            this.idx = idx;
            this.lastValue = lastValue;
            this.initValue = initValue;
        }
                
    }

    /**
     * Query object is needed to retrive info about query text, parameters etc.
     */
    private QueryObject query;
    
    public QueryObject getQuery() {
        return query;
    }

    public void setQuery( QueryObject query ) {
        this.query = query;
    }

    /**
     * Cached data retrieved from DB.
     * @return ObservableList.
     */    
    private ObservableList<T> data = FXCollections.observableArrayList();

    public ObservableList<T> getData() {
        return data;
    }    
    
    /**
     * It's used to listen new data coming.
     */
    private final ReadOnlyObjectWrapper< ObservableList<T> > dataProperty;
    public ReadOnlyObjectProperty< ObservableList<T> > getDataProperty() {
        return dataProperty;
    }    
    
    /**
     * Track removed items.
     */
    private final List<T> removedItemsList = new ArrayList<>();

    public List<T> getRemovedItemsList() {
        return removedItemsList;
    }
    
    /**
     * Track updated items.
     */
    private final List<Pair<T>> updatedItemsList = new ArrayList<>();

    public List<Pair<T>> getUpdatedItemsList() {
        return updatedItemsList;
    }
    
    /**
     * Track added items.
     */
    private final List<T> addedItemsList = new ArrayList<>();

    public List<T> getAddedItemsList() {
        return addedItemsList;
    }
    
    /**
     * Listener for cached data.
     */
    private final WeakListChangeListener<T> listChangeListener = new WeakListChangeListener<>( 
            ( c ) -> {
                if( skip ) return;
                while( c.next() ) {
                    if( c.wasReplaced() ) {
                        List<? extends T> removed = c.getRemoved();
                        List<? extends T> addedSubList = c.getAddedSubList();
                        int size = removed.size() > addedSubList.size() ? removed.size() : addedSubList.size();
                        //for each replaced items
                        for( int i=0; i < size; i++ ) {
                            T remItem = removed.get( i );
                            T addItem = addedSubList.get( i );                            
                            //No actions for null values and for equal items
                            if( remItem == null || addItem == null || remItem.equals( addItem ) ) { continue; };
                            //if replace new items with another one
                            if( addedItemsList.contains( remItem ) ) {
                                addedItemsList.set( addedItemsList.indexOf( remItem ), addItem );
                            } else {
                                //for new items
                                boolean asNew = true;
                                //traverse updatedItemList
                                for( Pair<T> pair : updatedItemsList ) {
                                    //looking for match with lastValue 
                                    //(initValue always point out to true DB data)
                                    if( pair.lastValue.equals( remItem ) ) {
                                        pair.setLastValue( addItem );
                                        asNew = false;
                                        break;
                                    }
                                }
                                if( asNew ) {
                                    updatedItemsList.add( new Pair<>( c.getFrom() + i, remItem, addItem ) );
                                }
                            }
                        }
                    } else if( c.wasRemoved() ) {
                        //only if removed item was obtained from DB(including changed)
                        c.getRemoved().forEach( ( item ) -> {
                            if( !addedItemsList.contains( item ) ) {
                                //it's not new item
                                T inst = item;
                                Pair remPair = null;
                                //try to find this item in updated list
                                //if found remove pair from updated list and add to removed
                                for( Pair<T> pair : updatedItemsList ) {
                                    if( pair.getLastValue().equals( item ) ) {
                                        remPair = pair;
                                        inst = pair.getInitValue();
                                        break;
                                    }
                                }
                                if( remPair != null )
                                    updatedItemsList.remove( remPair );
                                removedItemsList.add( inst );
                            } else {
                                addedItemsList.remove( item );
                            }
                        });
                    } else if( c.wasAdded() ) {
                        c.getAddedSubList().forEach( (item) -> {
                            if( removedItemsList.contains( item ) ) {
                                removedItemsList.remove( item );
                            } else
                                addedItemsList.add( item );
                        });
                    }
                }
            }
    );

    public BasicDAO() {
        this( null );
    }

    public BasicDAO( QueryObject query ) {
        this.query = query;
        dataProperty = new ReadOnlyObjectWrapper<>( data );
    }

    public ObservableList<T> open( Object[] params ) throws SQLException {
        if( query == null )
            throw new RuntimeException( "Set QueryObject before calling open." );
        query.setParameters( params );
        return open( query );
    }

    /**
     * After reopening we need to rebind our listeners to new list.
     * @param query QueryObject that provide information about this query.
     * @return ObservableList 
     * @throws SQLException 
     */
    public ObservableList<T> open( QueryObject query ) throws SQLException {        
        List<T> newData = new ArrayList<>();
        this.query = query;
        if( query != null && getWrapper() != null && query.getConnection() != null ) {
            final String QUERY_TEXT = query.getSelectText();
            if( QUERY_TEXT != null && !QUERY_TEXT.isEmpty() ) {
                Connection con = query.getConnection();
                try ( PreparedStatement ps =
                        con.prepareStatement( QUERY_TEXT )) {
                    int idx = 1;
                    Object[] params = query.getParameters();
                    if( params != null )
                        for( Object param : query.getParameters() ) {
                            ps.setObject( idx++, param );
                        }
                    ResultSet rs = ps.executeQuery();                
                    while( rs.next() ) {
                        newData.add( ( T ) getWrapper().wrap( rs, getWrapperClass() ) );
                    }
                }
            }
        }
        data = FXCollections.observableArrayList( newData );
        data.addListener( listChangeListener );
        dataProperty.set( data );
        return data;
    }

    abstract public Wrapper getWrapper();

    abstract public Class<?> getWrapperClass();
    
    /**
     * TODO: after cancel it's needed to restore init order.
     */
    public void cancel() {
        skip = true;
        try {
            updatedItemsList.forEach( ( pair ) -> {
                int i = pair.getIdx();
                //if saved index not valid
                if( i < 0 || i >= data.size() || !data.get( i ).equals( pair.getLastValue() ) )
                    i = data.indexOf( pair.getLastValue() );
                if( 0 <= i && i < data.size() )
                    data.set( i, pair.initValue );
            } );
            updatedItemsList.clear();
            //ignore duplicates( list order may be change )
            addedItemsList.forEach( ( item ) -> {
                data.remove( item );
            });
            addedItemsList.clear();
            //restore items in not init order
            removedItemsList.forEach( ( item ) -> {
                data.add( item );
            } );
            removedItemsList.clear();            
        } finally {
            skip = false;
        }
    }
    
    public void commit() {
        
        for( Pair<T> pair : updatedItemsList ) {
            
            
            
        }
        
    }

}
