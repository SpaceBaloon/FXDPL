package data;

import java.sql.Connection;

/**
 *
 * @author Belkin Sergei
 */
public interface QueryObject {
    String getSelectText();
    void setSelectText( String text );
    void setParameters( Object[] params );
    Object[] getParameters();
    String getInsertText();
    void setInsertText( String text );
    String getDeleteText();
    void setDeleteText( String text );
    Connection getConnection();
    void setConnection( Connection c );
}