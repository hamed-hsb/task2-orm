package ir.negah.orm.core;

import ir.negah.orm.annotation.Column;
import ir.negah.orm.annotation.Table;
import ir.negah.orm.exception.DataAccessException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class SimpleJdbcMapper {

    private final Connection connection;

    public SimpleJdbcMapper(Connection connection) {
        this.connection = Objects.requireNonNull(connection, "Database connection must not be null");
    }

    public <T> List<T> findAll(Class<T> entityType) {
        Objects.requireNonNull(entityType, "Entity type must not be null");
        validateEntity(entityType);

        String tableName = resolveTableName(entityType);
        Map<String, Field> columnFieldMappings = mapColumnsToFields(entityType);

        String columns = String.join(", ", columnFieldMappings.keySet());
        String query = "SELECT " + columns + " FROM " + tableName;

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            Constructor<T> constructor = entityType.getDeclaredConstructor();
            constructor.setAccessible(true);

            List<T> entities = new ArrayList<>();
            while (resultSet.next()) {
                T instance = constructor.newInstance();
                for (Map.Entry<String, Field> entry : columnFieldMappings.entrySet()) {
                    String columnName = entry.getKey();
                    Field field = entry.getValue();
                    Object value = resultSet.getObject(columnName);

                    if (value != null) {
                        setFieldValue(instance, field, value);
                    }
                }
                entities.add(instance);
            }
            return Collections.unmodifiableList(entities);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to execute dynamic query: " + query, e);
        } catch (ReflectiveOperationException e) {
            throw new DataAccessException("Failed to instantiate or map entity: " + entityType.getName(), e);
        }
    }

    private void validateEntity(Class<?> entityType) {
        if (!entityType.isAnnotationPresent(Table.class)) {
            throw new IllegalArgumentException("Class " + entityType.getName() + " is not annotated with @Table");
        }
    }

    private String resolveTableName(Class<?> entityType) {
        Table table = entityType.getAnnotation(Table.class);
        return table.name().isBlank() ? entityType.getSimpleName().toLowerCase() : table.name().trim();
    }

    private Map<String, Field> mapColumnsToFields(Class<?> entityType) {
        Map<String, Field> mappings = new LinkedHashMap<>();
        for (Field field : entityType.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);
                String columnName = column.name().isBlank() ? field.getName() : column.name().trim();
                field.setAccessible(true);
                mappings.put(columnName, field);
            }
        }
        if (mappings.isEmpty()) {
            throw new IllegalArgumentException("No @Column annotations found on entity: " + entityType.getName());
        }
        return mappings;
    }

    private void setFieldValue(Object instance, Field field, Object value) throws IllegalAccessException {
        Class<?> targetType = field.getType();
        if (targetType.equals(Long.class) && value instanceof Number number) {
            field.set(instance, number.longValue());
        } else if (targetType.equals(Integer.class) && value instanceof Number number) {
            field.set(instance, number.intValue());
        } else if (targetType.equals(Double.class) && value instanceof Number number) {
            field.set(instance, number.doubleValue());
        } else {
            field.set(instance, value);
        }
    }
}
