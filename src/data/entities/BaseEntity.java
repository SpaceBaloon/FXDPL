package data.entities;

/**
 *
 * @author Belkin Sergei.
 */
public abstract class BaseEntity implements IEntity {
    
    protected IField[] getAllFieldNames( IField[]... newFields ) {
        IField[] result = null;
        int length = 0, idx = 0;
        for( IField[] fields : newFields )
            if( fields != null )
                length += fields.length;
        result = new IField[length];
        for( IField[] fields : newFields )
            if( fields != null ) {
                for( IField field : fields )
                    result[idx++] = field;
            }
        return result;        
    }

    @Override
    public IField[] getFieldNames() {
        return getAllFieldNames(( IField[] ) null);
    }
    
}
