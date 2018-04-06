package data.wrappers;

/**
 *
 * @author Belkin Sergei
 * @param <T>
 */
public interface IDBPropertyReference<T> extends IPropertyReference<T> {
    boolean isPersistable();
    String getDBFieldName();
}
