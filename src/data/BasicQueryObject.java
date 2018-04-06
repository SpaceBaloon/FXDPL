package data;

import java.sql.Connection;

/**
 *
 * @author Belkin Sergei
 */
public class BasicQueryObject implements QueryObject {

    private String selectText;
    private String insertText;
    private String deleteText;
    private Object[] params;
    private Connection connection;
    
    @Override
    public String getSelectText() {
        return selectText;
    }

    @Override
    public void setSelectText( String text ) {
        this.selectText = text;
    }

    @Override
    public void setParameters( Object[] params ) {
        this.params = params;
    }

    @Override
    public Object[] getParameters() {
        return  params;
    }

    /**
     * 
     * @param text - query text.
     * @param params - params for query.
     * @param c - Connection for executing query.
     */
    public BasicQueryObject( String text, Object[] params, Connection c ) {
        this.selectText = text;
        this.params = params;
        this.connection = c;
    }

    public BasicQueryObject() {
        this( null, null, null );
    }

    @Override
    public String getInsertText() {
        return insertText;
    }

    @Override
    public void setInsertText( String text ) {
        this.insertText = text;
    }

    @Override
    public String getDeleteText() {
        return deleteText;
    }

    @Override
    public void setDeleteText( String text ) {
        this.deleteText = text;
    }

    @Override
    public Connection getConnection() {
        return this.connection;
    }

    @Override
    public void setConnection( Connection c ) {
        this.connection = c;
    }
    
}
