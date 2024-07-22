package org.sunbird.common;

import com.datastax.driver.core.RegularStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select.Where;
import com.datastax.driver.core.querybuilder.Update;
import com.datastax.driver.core.querybuilder.Update.Assignments;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.sunbird.cassandraannotation.ClusteringKey;
import org.sunbird.cassandraannotation.PartitioningKey;
import org.sunbird.exception.ProjectCommonException;
import org.sunbird.exception.ResponseCode;
import org.sunbird.keys.JsonKey;
import org.sunbird.logging.LoggerUtil;
import org.sunbird.response.Response;
import org.sunbird.util.ProjectUtil;

/**
 * @desc This class will provide all required helper method for cassandra db operation.
 * @author Amit Kumar
 */
public final class CassandraUtil {
  private static final LoggerUtil logger = new LoggerUtil(CassandraUtil.class);

  private static final CassandraPropertyReader propertiesCache =
      CassandraPropertyReader.getInstance();
  private static final String SERIAL_VERSION_UID = "serialVersionUID";

  private CassandraUtil() {}

  /**
   * @desc This method is used to create prepared statement based on table name and column name
   *     provided in request
   * @param keyspaceName Keyspace name
   * @param tableName Table name
   * @param map Map where key is column name and value is column value
   * @return Prepared statement
   */
  public static String getPreparedStatement(
      String keyspaceName, String tableName, Map<String, Object> map) {
    StringBuilder query = new StringBuilder();
    query.append(
        Constants.INSERT_INTO + keyspaceName + Constants.DOT + tableName + Constants.OPEN_BRACE);
    Set<String> keySet = map.keySet();
    query.append(String.join(",", keySet) + Constants.VALUES_WITH_BRACE);
    StringBuilder commaSepValueBuilder = new StringBuilder();
    for (int i = 0; i < keySet.size(); i++) {
      commaSepValueBuilder.append(Constants.QUE_MARK);
      if (i != keySet.size() - 1) {
        commaSepValueBuilder.append(Constants.COMMA);
      }
    }
    query.append(commaSepValueBuilder + Constants.CLOSING_BRACE);
    return query.toString();
  }

  /**
   * @desc This method is used for creating response from the resultset i.e return map
   *     <String,Object> or map<columnName,columnValue>
   * @param results ResultSet
   * @return Response Response
   */
  public static Response createResponse(ResultSet results) {
    logger.info("195124 CassandraUtil results "+results);
    Response response = new Response();
    List<Map<String, Object>> responseList = new ArrayList<>();
    Map<String, String> columnsMapping = fetchColumnsMapping(results);
    logger.info("CassandraUtil columnsMapping "+columnsMapping);
    for(String str : columnsMapping.keySet()){
      logger.info("columnsMapping Column name  "+str);
    }
    for(String str : columnsMapping.values()){
      logger.info("columnsMapping values  "+str);
    }
    Iterator<Row> rowIterator = results.iterator();
    rowIterator.forEachRemaining(
        row -> {
          logger.info("row:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::"+row.toString());
          Map<String, Object> rowMap = new HashMap<>();
          columnsMapping
              .entrySet()
              .stream()
              .forEach(entry -> {
                logger.info("Key::::::::::::::::::::::::::::::"+entry.getKey()+"::::::::::::Value:::::::::::::"+entry.getValue());
                rowMap.put(entry.getKey(), row.getObject(entry.getValue()));});
          responseList.add(rowMap);
        });
    response.put(Constants.RESPONSE, responseList);
    logger.info("CassandraUtil Response "+response.toString());
    return response;
  }

  public static Map<String, String> fetchColumnsMapping(ResultSet results) {
    return results
        .getColumnDefinitions()
        .asList()
        .stream()
        .collect(
            Collectors.toMap(
                d -> propertiesCache.readProperty(d.getName()).trim(), d -> d.getName()));
  }

  /**
   * @desc This method is used to create update query statement based on table name and column name
   *     provided
   * @param keyspaceName String (data base keyspace name)
   * @param tableName String
   * @param map Map<String, Object>
   * @return String String
   */
  public static String getUpdateQueryStatement(
      String keyspaceName, String tableName, Map<String, Object> map) {
    StringBuilder query =
        new StringBuilder(
            Constants.UPDATE + keyspaceName + Constants.DOT + tableName + Constants.SET);
    Set<String> key = new HashSet<>(map.keySet());
    key.remove(Constants.IDENTIFIER);
    query.append(String.join(" = ? ,", key));
    query.append(
        Constants.EQUAL_WITH_QUE_MARK + Constants.WHERE_ID + Constants.EQUAL_WITH_QUE_MARK);
    return query.toString();
  }

  /**
   * @desc This method is used to create prepared statement based on table name and column name
   *     provided as varargs
   * @param keyspaceName String (data base keyspace name)
   * @param tableName String
   * @param properties
   * @return String String
   */
  public static String getSelectStatement(
      String keyspaceName, String tableName, List<String> properties) {
    StringBuilder query = new StringBuilder(Constants.SELECT);
    query.append(String.join(",", properties));
    query.append(
        Constants.FROM
            + keyspaceName
            + Constants.DOT
            + tableName
            + Constants.WHERE
            + Constants.IDENTIFIER
            + Constants.EQUAL
            + " ?; ");
    return query.toString();
  }

  public static String processExceptionForUnknownIdentifier(Exception e) {
    // Unknown identifier
    return ProjectUtil.formatMessage(
            ResponseCode.invalidPropertyError.getErrorMessage(),
            e.getMessage()
                .replace(JsonKey.UNKNOWN_IDENTIFIER, "")
                .replace(JsonKey.UNDEFINED_IDENTIFIER, ""))
        .trim();
  }

  /**
   * Method to create the update query for composite keys. Create two separate map one for primary
   * key and other one for attributes which are going to set.
   *
   * @param clazz class of Model class corresponding to table.
   * @return Map containing two submap with keys PK(containing primary key attributes) and
   *     NonPk(containing updatable attributes).
   */
  public static <T> Map<String, Map<String, Object>> batchUpdateQuery(T clazz) {
    Field[] fieldList = clazz.getClass().getDeclaredFields();

    Map<String, Object> primaryKeyMap = new HashMap<>();
    Map<String, Object> nonPKMap = new HashMap<>();
    try {
      for (Field field : fieldList) {
        String fieldName = null;
        Object fieldValue = null;
        Boolean isFieldPrimaryKeyPart = false;
        if (Modifier.isPrivate(field.getModifiers())) {
          field.setAccessible(true);
        }
        Annotation[] annotations = field.getDeclaredAnnotations();
        for (Annotation annotation : annotations) {
          if (annotation instanceof PartitioningKey) {
            isFieldPrimaryKeyPart = true;
          } else if (annotation instanceof ClusteringKey) {
            isFieldPrimaryKeyPart = true;
          }
        }
        fieldName = field.getName();
        fieldValue = field.get(clazz);
        if (!(fieldName.equalsIgnoreCase(SERIAL_VERSION_UID))) {
          if (isFieldPrimaryKeyPart) {
            primaryKeyMap.put(fieldName, fieldValue);
          } else {
            nonPKMap.put(fieldName, fieldValue);
          }
        }
      }
    } catch (Exception ex) {
      logger.error("Exception occurred - batchUpdateQuery", ex);
      throw new ProjectCommonException(
          ResponseCode.serverError,
          ResponseCode.serverError.getErrorMessage(),
          ResponseCode.SERVER_ERROR.getResponseCode());
    }
    Map<String, Map<String, Object>> map = new HashMap<>();
    map.put(JsonKey.PRIMARY_KEY, primaryKeyMap);
    map.put(JsonKey.NON_PRIMARY_KEY, nonPKMap);
    return map;
  }

  /**
   * Method to create the composite primary key.
   *
   * @param clazz class of Model class corresponding to table.
   * @return Map containing primary key attributes.
   */
  public static <T> Map<String, Object> getPrimaryKey(T clazz) {
    Field[] fieldList = clazz.getClass().getDeclaredFields();
    Map<String, Object> primaryKeyMap = new HashMap<>();

    try {
      for (Field field : fieldList) {
        String fieldName = null;
        Object fieldValue = null;
        Boolean isFieldPrimaryKeyPart = false;
        if (Modifier.isPrivate(field.getModifiers())) {
          field.setAccessible(true);
        }
        Annotation[] annotations = field.getDeclaredAnnotations();
        for (Annotation annotation : annotations) {
          if (annotation instanceof PartitioningKey) {
            isFieldPrimaryKeyPart = true;
          } else if (annotation instanceof ClusteringKey) {
            isFieldPrimaryKeyPart = true;
          }
        }
        fieldName = field.getName();
        fieldValue = field.get(clazz);
        if (!(fieldName.equalsIgnoreCase(SERIAL_VERSION_UID))) {
          if (isFieldPrimaryKeyPart) {
            primaryKeyMap.put(fieldName, fieldValue);
          }
        }
      }
    } catch (Exception ex) {
      logger.error("Exception occurred - getPrimaryKey", ex);
      throw new ProjectCommonException(
          ResponseCode.serverError,
          ResponseCode.serverError.getErrorMessage(),
          ResponseCode.SERVER_ERROR.getResponseCode());
    }
    return primaryKeyMap;
  }

  /**
   * Method to create the where clause.
   *
   * @param key represents the column name.
   * @param value represents the column value.
   * @param where where clause.
   */
  public static void createWhereQuery(String key, Object value, Where where) {
    if (value instanceof Map) {
      Map<String, Object> map = (Map<String, Object>) value;
      map.entrySet()
          .stream()
          .forEach(
              x -> {
                if (Constants.LTE.equalsIgnoreCase(x.getKey())) {
                  where.and(QueryBuilder.lte(key, x.getValue()));
                } else if (Constants.LT.equalsIgnoreCase(x.getKey())) {
                  where.and(QueryBuilder.lt(key, x.getValue()));
                } else if (Constants.GTE.equalsIgnoreCase(x.getKey())) {
                  where.and(QueryBuilder.gte(key, x.getValue()));
                } else if (Constants.GT.equalsIgnoreCase(x.getKey())) {
                  where.and(QueryBuilder.gt(key, x.getValue()));
                }
              });
    } else if (value instanceof List) {
      where.and(QueryBuilder.in(key, (List) value));
    } else {
      where.and(QueryBuilder.eq(key, value));
    }
  }

  /**
   * Method to create the cassandra update query.
   *
   * @param primaryKey map representing the composite primary key.
   * @param nonPKRecord map contains the fields that has to update.
   * @param keyspaceName cassandra keyspace name.
   * @param tableName cassandra table name.
   * @return RegularStatement.
   */
  public static RegularStatement createUpdateQuery(
      Map<String, Object> primaryKey,
      Map<String, Object> nonPKRecord,
      String keyspaceName,
      String tableName) {

    Update update = QueryBuilder.update(keyspaceName, tableName);
    Assignments assignments = update.with();
    Update.Where where = update.where();
    nonPKRecord
        .entrySet()
        .stream()
        .forEach(
            x -> {
              assignments.and(QueryBuilder.set(x.getKey(), x.getValue()));
            });
    primaryKey
        .entrySet()
        .stream()
        .forEach(
            x -> {
              where.and(QueryBuilder.eq(x.getKey(), x.getValue()));
            });
    return where;
  }

  public static void createQuery(String key, Object value, Where where) {
    if (value instanceof Map) {
      Map<String, Object> map = (Map<String, Object>) value;
      map.entrySet()
          .stream()
          .forEach(
              x -> {
                if (Constants.LTE.equalsIgnoreCase(x.getKey())) {
                  where.and(QueryBuilder.lte(key, x.getValue()));
                } else if (Constants.LT.equalsIgnoreCase(x.getKey())) {
                  where.and(QueryBuilder.lt(key, x.getValue()));
                } else if (Constants.GTE.equalsIgnoreCase(x.getKey())) {
                  where.and(QueryBuilder.gte(key, x.getValue()));
                } else if (Constants.GT.equalsIgnoreCase(x.getKey())) {
                  where.and(QueryBuilder.gt(key, x.getValue()));
                }
              });
    } else if (value instanceof List) {
      where.and(QueryBuilder.in(key, (List) value));
    } else {
      where.and(QueryBuilder.eq(key, value));
    }
  }
}
