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

package org.episteme.core.io.properties;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Loads properties from JSON resources.
 *
 * @author Silvere Martin-Michiellot
 * @author Gemini AI (Google DeepMind)
 * @since 1.0
 */
public class PropertiesReader {
    // Aggressive touch for IDE re-indexing

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Map<String, PropertySet> cache = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * Loads properties for a specific item from a JSON resource.
     * 
     * @param resourcePath Path to the JSON file (classpath).
     * @param itemName     Name of the item to load (key in the JSON object).
     * @return A PropertySet containing the loaded properties.
     */
    public static PropertySet load(String resourcePath, String itemName) {
        String cacheKey = resourcePath + ":" + itemName;
        if (cache.containsKey(cacheKey)) {
            return cache.get(cacheKey);
        }

        try {
            InputStream isTemp = PropertiesReader.class.getResourceAsStream(resourcePath);
            if (isTemp == null && resourcePath.startsWith("/")) {
                // Try context class loader with relative path
                isTemp = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath.substring(1));
            }

            if (isTemp == null) {
                throw new IllegalArgumentException("Resource not found: " + resourcePath);
            }

            try (InputStream is = isTemp) {
                JsonNode root = mapper.readTree(is);
                JsonNode itemNode = root.get(itemName);

                if (itemNode == null) {
                    throw new IllegalArgumentException("Item '" + itemName + "' not found in " + resourcePath);
                }

                PropertySet propertySet = new PropertySet();
                parseProperties(itemNode, propertySet);

                cache.put(cacheKey, propertySet);
                return propertySet;
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to load properties from " + resourcePath, e);
        }
    }

    private static void parseProperties(JsonNode node, PropertySet set) {
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String keyName = field.getKey();
            JsonNode valueNode = field.getValue();

            // Heuristic Type Inference:
            // Since the JSON structure is flat key-value without type metadata,
            // we infer the property type from the JSON value type.
            // - JSON Numbers -> Double (wrapped in PropertyKey<Number>)
            // - JSON Strings -> String (wrapped in PropertyKey<String>)
            // - JSON Booleans -> Boolean (wrapped in PropertyKey<Boolean>)
            //
            // Note: Downstream consumers of PropertySet must know the expected type
            // for a given key to retrieve it correctly. This loader provides a
            // best-effort mapping for generic access.
            if (valueNode.isNumber()) {
                set.set(PropertyKey.of(keyName, Number.class), valueNode.asDouble());
            } else if (valueNode.isTextual()) {
                String text = valueNode.asText();
                Object inferred = org.episteme.core.util.TypeInference.infer(text);
                // We need to handle the generic type safety warning here, or just cast
                // PropertyKey expects a class.
                @SuppressWarnings("unchecked")
                Class<Object> inferredClass = (Class<Object>) inferred.getClass();
                set.set(PropertyKey.of(keyName, inferredClass), inferred);
            } else if (valueNode.isBoolean()) {
                set.set(PropertyKey.of(keyName, Boolean.class), valueNode.asBoolean());
            } else {
                // For arrays or objects, we currently skip or could store as JsonNode
                // For now, skipping complex types to keep PropertySet simple
            }
        }
    }
}

