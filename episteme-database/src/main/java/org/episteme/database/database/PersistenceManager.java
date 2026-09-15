/*
 * Episteme - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2025-2026 - Silvere Martin-Michiellot and Gemini AI (Google DeepMind)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.episteme.database.database;

import org.episteme.core.util.persistence.Persistent;
import org.episteme.core.util.persistence.Id;
import org.episteme.core.util.persistence.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

/**
 * Automagically handles persistence for Episteme classes annotated with {@link Persistent}.
 * @author Silvere Martin-Michiellot
 * @author Gemini AI (Google DeepMind)
 * @since 1.0
 */
public class PersistenceManager implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(PersistenceManager.class);
    private final Connection connection;
    private final Map<Class<?>, String> tableNames = new java.util.concurrent.ConcurrentHashMap<>();

    public PersistenceManager(String jdbcUrl, String user, String password) throws SQLException {
        this.connection = DriverManager.getConnection(jdbcUrl, user, password);
    }

    @Override
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    /**
     * Registers a class for persistence and creates the table if it doesn't exist.
     */
    public void register(Class<?> clazz) throws SQLException {
        if (!clazz.isAnnotationPresent(Persistent.class)) {
            throw new IllegalArgumentException("Class must be annotated with @Persistent");
        }

        Persistent ann = clazz.getAnnotation(Persistent.class);
        String tableName = ann.value().isEmpty() ? clazz.getSimpleName().toUpperCase() : ann.value();
        tableNames.put(clazz, tableName);

        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
                .append(tableName)
                .append(" (");

        List<Field> fields = getAllFields(clazz);
        boolean first = true;
        for (Field field : fields) {
            if (field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(Attribute.class)) {
                if (!first) sql.append(", ");
                sql.append(field.getName().toUpperCase()).append(" ");
                
                Class<?> type = field.getType();
                // Improved type mapping
                if (type == String.class || type.isEnum()) {
                    sql.append("VARCHAR(255)");
                } else if (type == int.class || type == Integer.class) {
                    sql.append("INT");
                } else if (type == long.class || type == Long.class) {
                    sql.append("BIGINT");
                } else if (type == double.class || type == Double.class || type == float.class || type == Float.class) {
                    sql.append("DOUBLE");
                } else if (type == boolean.class || type == Boolean.class) {
                    sql.append("BOOLEAN");
                } else if (type == java.time.LocalDate.class) {
                    sql.append("DATE");
                } else if (type == java.time.Instant.class || type == java.time.LocalDateTime.class) {
                    sql.append("TIMESTAMP");
                } else if (org.episteme.core.mathematics.numbers.real.Real.class.isAssignableFrom(type)) {
                    sql.append("DOUBLE");
                } else {
                    sql.append("VARCHAR(1000)"); // Default to string representation
                }

                if (field.isAnnotationPresent(Id.class)) {
                    sql.append(" PRIMARY KEY");
                }
                first = false;
            }
        }
        sql.append(")");

        logger.info("Creating table: {}", sql);
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql.toString());
        }
    }

    public void save(Object obj) throws SQLException, IllegalAccessException {
        Class<?> clazz = obj.getClass();
        String tableName = tableNames.get(clazz);
        if (tableName == null) {
            register(clazz);
            tableName = tableNames.get(clazz);
        }

        List<Field> fields = getAllFields(clazz);
        StringBuilder cols = new StringBuilder();
        StringBuilder values = new StringBuilder();
        List<Object> params = new ArrayList<>();

        for (Field field : fields) {
            if (field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(Attribute.class)) {
                if (cols.length() > 0) {
                    cols.append(", ");
                    values.append(", ");
                }
                cols.append(field.getName().toUpperCase());
                values.append("?");
                field.setAccessible(true);
                Object val = field.get(obj);
                params.add(convertValue(val));
            }
        }

        String sql = "MERGE INTO " + tableName + " (" + cols + ") KEY (" + getIdColumn(clazz) + ") VALUES (" + values + ")";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            pstmt.executeUpdate();
        }
    }

    private Object convertValue(Object val) {
        if (val == null) return null;
        if (val instanceof org.episteme.core.util.identity.Identified) {
            Object idVal = ((org.episteme.core.util.identity.Identified<?>) val).getId();
            return convertValue(idVal);
        }
        if (val instanceof org.episteme.core.mathematics.numbers.real.Real) {
            return ((org.episteme.core.mathematics.numbers.real.Real) val).doubleValue();
        }
        if (val instanceof org.episteme.core.measure.Quantity) {
            return val.toString(); // e.g. "10.5 m/s"
        }
        if (val instanceof java.time.LocalDate || val instanceof java.time.Instant) {
             return val; // JDBC handles these
        }
        if (val.getClass().isEnum()) {
            return ((Enum<?>) val).name();
        }
        if (val.getClass().isAnnotationPresent(Persistent.class)) {
            // If it's another persistent object, store its ID
            try {
                for (Field f : getAllFields(val.getClass())) {
                    if (f.isAnnotationPresent(Id.class)) {
                        f.setAccessible(true);
                        return convertValue(f.get(val));
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to extract ID from persistent object", e);
            }
        }
        return val;
    }

    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }
        return fields;
    }

    private String getIdColumn(Class<?> clazz) {
        for (Field field : getAllFields(clazz)) {
            if (field.isAnnotationPresent(Id.class)) {
                return field.getName().toUpperCase();
            }
        }
        return "ID"; // Fallback
    }
}

