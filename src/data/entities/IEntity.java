package data.entities;

/**
 * Provides access to array of {@code Object} that implements {@code IField}.
 * Name of {@code Object} must follow certain naming convention: 
 * <ul>
 *  <li>all letters must be in uppercase;</li>
 *  <li>begins from word <q>FIELD_NAME_</q>;</li>
 *  <li>and ends with name of a field that represent that {@code Object}.</li>
 * </ul>
 * It is more apropriate to use {@code Enum} type, for example:
 * <pre>
 *  public class Table implements IEntity {
 *      private static enum FieldNames implements IField {
 *          TABLE_FIELD_FIELDNAME("NAMEFROMDB")
 *          private dbFieldName;
 *          FieldNames( String dbFieldName ) {
 *              this.dbFieldName = dbFieldName;
 *          }
 *          public String getDBFieldName() {
 *              return dbFieldName;
 *          }
 *      }
 *      private String fieldName; *      
 *      public String getFieldName() {
 *          return fieldName;
 *      }
 *      public void setFieldName( String fieldName ) {
 *          this.fieldName = fieldName;
 *      }
 *      @Override
 *      public IField getFieldNames() {
 *          return FieldNames.values();
 *      }
 *  }
 * </pre>
 * @author Belkin Sergei.
 */
public interface IEntity {
    <E extends IField> E[] getFieldNames();
    String getTableName();
}
