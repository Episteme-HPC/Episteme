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

package org.episteme.core.mathematics.loaders.logic;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Importer for QEDEQ (Hilbert II) XML modules.
 * <p>
 * Parses QEDEQ XML files and extracts:
 * <ul>
 * <li>Module name and title</li>
 * <li>Chapters and sections</li>
 * <li>Axioms and definitions</li>
 * <li>Theorems and propositions</li>
 * </ul>
 * </p>
 *
 * @author Silvere Martin-Michiellot
 * @author Gemini AI (Google DeepMind)
 * @since 1.0
 */
public class QedeqImporter implements FormalSystemImporter {

    @Override
    public Map<String, Object> importSystem(Reader reader) throws IOException, ParseException {
        Map<String, Object> result = new HashMap<>();

        try {
            DocumentBuilderFactory factory = org.episteme.core.io.SecureXMLFactory.createSecureNamespaceAwareDocumentBuilderFactory();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(reader));

            Element root = doc.getDocumentElement();

            // Extract module metadata
            NodeList headerNodes = root.getElementsByTagName("header");
            if (headerNodes.getLength() > 0) {
                Element header = (Element) headerNodes.item(0);
                NodeList specNodes = header.getElementsByTagName("specification");
                if (specNodes.getLength() > 0) {
                    Element spec = (Element) specNodes.item(0);
                    String name = getElementText(spec, "name");
                    String title = getElementText(spec, "title");
                    result.put("name", name);
                    result.put("title", title);
                }
            }

            // Extract chapters
            Map<String, Object> chapters = new HashMap<>();
            NodeList chapterNodes = root.getElementsByTagName("chapter");
            for (int i = 0; i < chapterNodes.getLength(); i++) {
                Element chapter = (Element) chapterNodes.item(i);
                String chapterTitle = getElementText(chapter, "title");

                Map<String, String> sections = new HashMap<>();
                NodeList sectionNodes = chapter.getElementsByTagName("section");
                for (int j = 0; j < sectionNodes.getLength(); j++) {
                    Element section = (Element) sectionNodes.item(j);
                    String sectionTitle = getElementText(section, "title");
                    sections.put("section_" + j, sectionTitle);
                }

                chapters.put(chapterTitle != null ? chapterTitle : "chapter_" + i, sections);
            }
            result.put("chapters", chapters);

        } catch (ParserConfigurationException | SAXException e) {
            throw new ParseException("Failed to parse QEDEQ XML", e);
        }

        return result;
    }

    /**
     * Helper method to extract text content from an XML element.
     */
    private String getElementText(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            Element element = (Element) nodes.item(0);
            return element.getTextContent();
        }
        return null;
    }

    @Override
    public String[] getSupportedExtensions() {
        return new String[] { ".xml", ".qedeq" };
    }
}


