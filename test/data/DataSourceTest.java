package data;

import data.entities.IEntity;
import data.entities.IField;
import data.managers.BasicDAO;
import data.wrappers.DefaultWrapper;
import data.wrappers.Wrapper;
import java.sql.SQLException;
import java.util.List;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Belkin Sergei
 */
public class DataSourceTest {
    
    public DataSourceTest() throws SQLException {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    
    public static interface IDoc {
        Long getId();
        void setId( Long id );
    }
    
    public static class Doc implements IDoc, IEntity {

        private Long id;
        
        @Override
        public Long getId() {
            return this.id;
        }

        @Override
        public void setId( Long id ) {
            this.id = id;
        }

        @Override
        public IField[] getFieldNames() {
            return new IField[] { 
                new IField() {
                    @Override
                    public String getDBFieldName() {
                        return "ID";
                    }
                    @Override
                    public String toString() {
                        return "FIELD_NAME_ID";
                    }
                }
            };
        }

        @Override
        public String getTableName() {
            return "DOCS";
        }
        
    }
    
    private static class MockTextField implements ListChangeListener<IDoc> {
        
        private DataSource dataSource;
        private final Property textPtoperty = new SimpleLongProperty();
        private Property other;
        private String fieldName;

        public String getFieldName() {
            return fieldName;
        }

        public void setFieldName( String fieldName ) {
            if( this.fieldName != fieldName ) {
                if( textPtoperty.isBound() ) {
                    textPtoperty.unbindBidirectional( other );
                    other = null;
                }
                this.fieldName = fieldName;
                if( fieldName != null && !fieldName.trim().isEmpty() ) {
                    updatePropertyState();
                }                
            }
            
        }
        
        protected void updatePropertyState() {
            if( this.dataSource != null ) {
                other = this.dataSource.getRowObjectPropertyByName( getFieldName() );
                if( other != null ) {
                    textPtoperty.bindBidirectional( other );
                }
            }            
        }

        public Property getTextPtoperty() {
            return textPtoperty;
        }

        public DataSource getDataSource() {
            return dataSource;
        }

        public void setDataSource( DataSource dataSource ) {
            if( dataSource != this.dataSource ) {
                if( this.dataSource != null ) {
                    this.dataSource.getDataObject().getData().removeListener( this );                    
                }
                this.dataSource = dataSource;
                if( this.dataSource != null )
                    this.dataSource.getDataObject().getData().addListener( this );
                updatePropertyState();
            }
        }

        @Override
        public void onChanged( ListChangeListener.Change<? extends IDoc> c ) {
            while( c.next() ) {
                if( c.wasPermutated() ) {
                    System.out.println( "Permutated" );
                } else if( c.wasUpdated() ) {
                    System.out.println( "Updated" );
                } else {
                    List<? extends IDoc> list = c.getRemoved();                    
                    if( list != null ) {
                        System.out.println( "Removoed" );
                        for( IDoc el : list )
                            System.out.println( el.toString() );
                    }
                    list = c.getAddedSubList();
                    if( list != null ) {
                        System.out.println( "Added" );
                        for( IDoc el : list )
                            System.out.println( el.toString() );
                    }
                }
            }
        }
        
    }
            

    @Test
    public void testSomeMethod() throws SQLException {
        System.out.println( "############ TEST SOME METHOD ############" );
        final String QUERY_TEXT = "SELECT FIRST(1) * FROM DOCS";
        Wrapper<IDoc> wrapper = new DefaultWrapper<>();
        BasicDAO<IDoc> dataObject = new BasicDAO<IDoc>(  ) {            
            @Override
            public Wrapper getWrapper() {
                return wrapper;
            }            
            @Override
            public Class<?> getWrapperClass() {
                return Doc.class;
            }
        };
        DataSource dataSource = new DataSource();
        dataSource.setDataObject( dataObject );
        MockTextField textField = new MockTextField();
        textField.setDataSource( dataSource );
        textField.setFieldName( "id");
        ObservableList<IDoc> open = dataObject.open( 
                new BasicQueryObject(QUERY_TEXT, null, InitDBUtils.getConnection() )
        );
        
        assertNotNull( wrapper.getProperties() );
        assertTrue( wrapper.getProperties().size() > 0 );
//        wrapper.getProperties().forEach( ( t ) -> {
//            System.out.println( t.getName() );
//        });
        assertTrue( wrapper.hasProperty( "id" ) );
        assertTrue( dataSource.getRowObjectPropertyByName( "id" ) != null );
        IDoc rowObj = ( IDoc ) dataSource.getRowObject().get();
        assertNotNull( rowObj );
//        assertTrue( textField.getTextPtoperty().isBound() );
        assertEquals( rowObj.getId(), textField.getTextPtoperty().getValue() );
        final Integer newValue = 132;
        textField.getTextPtoperty().setValue( newValue );
        assertEquals( rowObj.getId(), textField.getTextPtoperty().getValue() );
        System.out.println( "############ TEST SOME METHOD ############" );
    }
    
    static class TestCase {        
        private Long id;
        private String name;
        
        public Long getId() {
            return id;
        }
        
        public void setId( Long id ) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName( String name ) {
            this.name = name;
        }
    }
    
    @Test
    public void testProperties() {
        TestCase t1 = new TestCase();
        Property p1 = new SimpleObjectProperty( t1, "name" );
        String expResult = "one";
        p1.setValue( expResult );
//        assertEquals( expResult, t1.getName() );
        assertEquals( expResult, p1.getValue() );
        
        Property p2 = new SimpleObjectProperty();        
        p2.bindBidirectional( p1 );
        assertEquals( p2.getValue(), p1.getValue() );
        
    }
    
    @Test
    public void testPropertiesListener() {
        System.out.println( "############### TEST PROPERTY LISTENER ###############" );
        Property p = new SimpleStringProperty("initValue");
        p.addListener( (observable, oldValue, newValue ) -> {
            System.out.format( "Current value - %s, oldValue - %s, newValue - %s", 
                    p.getValue().toString(), oldValue.toString(), newValue.toString()  );
        } );
        p.setValue( "nextValue" );
        System.out.println( "############### TEST PROPERTY LISTENER ###############" );
    }
    
    @Test
    public void testGetRowObjectPropertyByName() {
        System.out.println( "############ TEST GET ROW OBJECT PROPERTY BY NAME ############" );
        
        DataSource ds = new DataSource();
        Property rowObjectPropertyByName = ds.getRowObjectPropertyByName( "name" );
        assertNotNull( rowObjectPropertyByName );        
        
        System.out.println( "############ TEST GET ROW OBJECT PROPERTY BY NAME ############" );
    }
    
}
