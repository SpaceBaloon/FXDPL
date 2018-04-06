package data;

import data.managers.BasicDAO;
import data.wrappers.IDBPropertyReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;

/**
 * Naive implementation of DataSource object.
 * This class catchs up data opening, changing, cursor moving.
 *
 * @author Belkin Sergei
 */
public class DataSource {

    
    private BasicDAO dataObject;
    
    public BasicDAO getDataObject() {
        return dataObject;
    }
    
    /**
     * Remove listeners from old dataObject, add to new one.
     * @param dataObject instance of BasicDAO.
     */    
    public void setDataObject( BasicDAO dataObject ) {
        if( this.dataObject != null ) {
            this.dataObject.getData().removeListener( onListChanged );
            this.dataObject.getDataProperty().removeListener( onOpeningListener );
        }
        this.dataObject = dataObject;
        if( this.dataObject != null ) {
            this.dataObject.getData().addListener( onListChanged );
            this.dataObject.getDataProperty().addListener( onOpeningListener );
        }
    }    
    
    private final ReadOnlyObjectWrapper rowObject = new ReadOnlyObjectWrapper();
    
    public ReadOnlyObjectProperty getRowObject() {
        return rowObject.getReadOnlyProperty();
    }
    
    private final Map<String, Property> rowObjectProperties = new HashMap<>();
    
    private Integer currentRowIndex = 0;
    /**
     * Listening data opening. Update rowObject.
     */
    private final ChangeListener<ObservableList> onOpeningListener;
    /**
     * Listen list changes.
     * Currently it does nothing.
     */    
    private final ListChangeListener onListChanged;    
    /**
     * Reflect cursor moving. Update rowObject;
     */
    private final ChangeListener<Number> onCursorMoving;

    public ChangeListener<Number> getOnCursorMoving() {
        return onCursorMoving;
    }
    /**
     * Listener for properties changes.
     */
    private final ChangeListener onRowObjectPropertiesChange;
    /**
     * React when rowObject changes happens.
     * Update values of the properties in rowObjectProperties list.
     */
    private final ChangeListener onRowObjectChange;
    
    DataSource masterDataSource;
    List<String> masterFields;
    Long masterDelay;
    Boolean async;

    public DataSource() {
        onRowObjectChange = (observable, oldValue, newValue ) -> {
            if( oldValue != newValue ) {
                rowObjectProperties.forEach( (name, property ) -> {
                    property.setValue( getValueForProperty( name ) );
                } );
            }
        };
        rowObject.addListener( onRowObjectChange );
        this.onRowObjectPropertiesChange = ( ObservableValue observable, 
                Object oldValue, Object newValue ) -> {
            updateRowObject();
        };
        this.onListChanged =  ( Change c ) -> {        
            while( c.next() ) {
                if( c.wasPermutated() ) {
//                System.out.println( "Was permutated " );
                } else if( c.wasUpdated()) {
//                System.out.println( "Was updated " );
                } else {
//                List<String> list = ( List<String> ) c.getRemoved();
//                if( list != null ) {
//                    System.out.println( "Was removed" );
//                    for( Object s : c.getRemoved() )
//                        System.out.println( s.toString() );
//                }
//                list = ( List<String> ) c.getAddedSubList();
//                if( list != null ) {
//                    System.out.println( "Was added" );
//                    for( Object s : c.getAddedSubList() )
//                        System.out.println( s.toString() );
//                }
                }
            }
        };
        this.onOpeningListener = ( ObservableValue<? extends ObservableList> observable,
                    ObservableList oldValue, ObservableList newValue ) -> {            
            //Reset index.
            currentRowIndex = 0;
            //Update currentRowObject.
            if( dataObject != null && dataObject.getData() != null ) {
                dataObject.getData().addListener( new WeakListChangeListener( onListChanged ) );
                rowObject.setValue( dataObject.getData().get( currentRowIndex ) );
            }
        };
        this.onCursorMoving =  ( ObservableValue<? extends Number> observable, 
            Number oldValue, Number newValue ) -> {
            if( oldValue != newValue && dataObject != null && dataObject.getData() != null ) {
                currentRowIndex = newValue.intValue();
                if( currentRowIndex >= 0 && currentRowIndex < dataObject.getData().size() )
                    rowObject.setValue( dataObject.getData().get( currentRowIndex ) );
            }
        };
    }
    
    /**
     * Retrieve value for property in the rowObjectProperties list.
     * @param name - name of the property.
     * @return value to be set. If there were no matches return null.
     */
    protected Object getValueForProperty( String name ) {
        Object res = null;
        if( dataObject != null
                && dataObject.getWrapper() != null
                && rowObject.get() != null ) {
            IDBPropertyReference propRef = dataObject.getWrapper().getPropertyByName( name );
            if( propRef != null )
                res = propRef.get( rowObject.get() );
        }
        return res;
    }
    
    /**
     * It's called after changing properties of currentRowObject 
     * to distribute changes on ObservableList.
     */
    public void updateRowObject() {
        if( dataObject != null && dataObject.getData() != null
                && dataObject.getWrapper() != null && rowObject.get() != null
                && -1 < currentRowIndex && currentRowIndex < dataObject.getData().size() ) {
            //set all available properties of rowObject
            rowObjectProperties.forEach( (name, property) -> {
                IDBPropertyReference get = dataObject.getWrapper().getPropertyByName( name );
                if( get != null )
                    get.set( rowObject.get(), property.getValue() );
            } );
            //update list for those listeners that listen list changes
            dataObject.getData().set( currentRowIndex, rowObject.get() );
        }
    }
    
    /**
     * Return SimpleObjectProperty and save it in rowObjectProperties.
     * It returns property regardless of they presence in current rowObject.
     * @param name - name of desirable property.
     * @return Property with given name.
     */
    public Property getRowObjectPropertyByName( String name ) {
        if( name == null || name.trim().isEmpty() ) return null;
        Property res;
        if( rowObjectProperties.containsKey( name ) ) {
            res = rowObjectProperties.get( name );
        } else {
            res  = new SimpleObjectProperty( getValueForProperty( name ) );
            res.addListener( onRowObjectPropertiesChange );
            rowObjectProperties.put( name, res );
        }
        return res;
    }
    
}
