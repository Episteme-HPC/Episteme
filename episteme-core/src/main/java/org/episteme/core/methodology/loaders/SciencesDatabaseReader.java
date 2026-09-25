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

package org.episteme.core.methodology.loaders;

import org.episteme.core.io.AbstractResourceReader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.*;

/**
 * Parser and accessor for the sciences taxonomy database.
 * <p>
 * <b>Data source</b>: Stephen Chrisomalis (2004), licensed for personal use
 * with attribution.
 * Database contains 600+ scientific disciplines from acarology to zoology.
 * </p>
 *
 * @author Silvere Martin-Michiellot
 * @author Gemini AI (Google DeepMind)
 * @since 1.0
 */
public class SciencesDatabaseReader extends AbstractResourceReader<SciencesDatabaseReader.Science> {

    /**
     * Path to the embedded XML database.
     */
    private static final String RESOURCE_PATH = "/org/episteme/taxonomy/sciences.xml";

    /**
     * Represents a scientific discipline.
     */
    public static class Science {
        private final String name;
        private final String description;

        public Science(String name, String description) {
            this.name = name;
            this.description = description;
        }

        /**
         * Gets the name of the science (e.g., "astronomy").
         * 
         * @return lowercase science name
         */
        public String getName() {
            return name;
        }

        /**
         * Gets the description (e.g., "study of celestial bodies").
         * 
         * @return description
         */
        public String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            return name + ": " + description;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof Science))
                return false;
            Science science = (Science) o;
            return name.equals(science.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }

    public SciencesDatabaseReader() {
    }

    @Override
    protected Science loadFromSource(String id) throws Exception {
        // Since it's a monolithic XML, loadAll usually populates everything.
        // But for consistency:
        return loadAll().stream()
                .filter(s -> s.getName().equalsIgnoreCase(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    protected List<Science> loadAllFromSource() throws Exception {
        List<Science> list = new ArrayList<>();
        try (InputStream is = getClass().getResourceAsStream(RESOURCE_PATH)) {
            if (is == null) {
                return list;
            }

            DocumentBuilder builder = org.episteme.core.io.SecureXMLFactory.createSecureDocumentBuilder();
            Document doc = builder.parse(is);

            NodeList scienceNodes = doc.getElementsByTagName("science");

            for (int i = 0; i < scienceNodes.getLength(); i++) {
                Element scienceElement = (Element) scienceNodes.item(i);
                String name = getTextContent(scienceElement, "name");
                String description = getTextContent(scienceElement, "description");
                if (name != null && description != null) {
                    list.add(new Science(name, description));
                }
            }
        }
        return list;
    }

    private String getTextContent(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent().trim();
        }
        return null;
    }

    @Override
    public String getResourcePath() {
        return "org/episteme/taxonomy";
    }

    @Override
    public Class<Science> getResourceType() {
        return Science.class;
    }

    @Override
    public String getName() {
        return "Sciences Taxonomy Reader";
    }

    @Override
    public String getDescription() {
        return "Reader for the scientific disciplines taxonomy database.";
    }

    @Override
    public String getLongDescription() {
        return "A comprehensive database of over 600 scientific disciplines, providing names and descriptions for taxonomy and classification.";
    }

    @Override
    public String getCategory() {
        return "Scientific Data";
    }

    @Override
    public String[] getSupportedVersions() {
        return new String[] {"2004.1"};
    }

    @Override
    public String[] getSupportedExtensions() {
        return new String[] {".xml"};
    }
}
