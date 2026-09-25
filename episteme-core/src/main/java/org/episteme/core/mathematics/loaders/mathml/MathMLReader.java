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

package org.episteme.core.mathematics.loaders.mathml;

import org.episteme.core.io.AbstractResourceReader;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.mathml.MathMLDocument;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MathML Reader for Content and Presentation MathML.
 * <p>
 * MathML (Mathematical Markup Language) is the W3C standard for representing
 * mathematical notation and content. This reader supports both Content MathML
 * (semantic structure) and Presentation MathML (visual rendering).
 * </p>
 * <p>
 * <b>Supported MathML 3.0 Elements:</b>
 * <ul>
 *   <li><b>Presentation:</b> mi, mn, mo, mrow, mfrac, msqrt, mroot, msub, msup, etc.</li>
 *   <li><b>Content:</b> apply, ci, cn, csymbol, plus, times, integral, etc.</li>
 *   <li><b>Matrices:</b> vector, matrix, matrixrow</li>
 *   <li><b>Functions:</b> sin, cos, exp, ln, log, and all trigonometric variants</li>
 *   <li><b>Sets:</b> set, list, union, intersect</li>
 *   <li><b>Calculus:</b> int, diff, partialdiff, limit</li>
 *   <li><b>Logic:</b> and, or, not, implies, forall, exists</li>
 * </ul>
 * </p>
 * <p>
 * <b>Example:</b>
 * <pre>{@code
 * MathMLReader reader = new MathMLReader();
 * MathMLDocument doc = reader.read(new File("equation.xml"));
 * MathMLMathElement math = doc.getMathElement();
 * }</pre>
 * </p>
 * * @see <a href="https://www.w3.org/Math/">W3C MathML</a>
 * @author Silvere Martin-Michiellot
 * @author Gemini AI (Google DeepMind)
 * @since 1.0
 */
public class MathMLReader extends AbstractResourceReader<MathMLDocument> {

    private static final Logger logger = LoggerFactory.getLogger(MathMLReader.class);
    private static final String MATHML_NS = "http://www.w3.org/1998/Math/MathML";

    public MathMLReader() {
    }

    // ===== ResourceReader interface =====

    @Override
    public String getResourcePath() {
        return null; // File-based, path provided at load time
    }

    @Override
    public Class<MathMLDocument> getResourceType() {
        return MathMLDocument.class;
    }

    @Override
    public String getName() {
        return "MathML Reader";
    }

    @Override
    public String getDescription() {
        return "Reads mathematical expressions from MathML format";
    }

    @Override
    public String getLongDescription() {
        return "MathML is the W3C standard for representing mathematical notation " +
               "and content. Supports both Content MathML (semantic) and " +
               "Presentation MathML (visual).";
    }

    @Override
    public String getCategory() {
        return "Mathematics";
    }

    @Override
    public String[] getSupportedVersions() {
        return new String[] {"3.0", "2.0"};
    }

    @Override
    public String[] getSupportedExtensions() {
        return new String[] {".xml", ".mml", ".mathml"};
    }

    @Override
    protected MathMLDocument loadFromSource(String resourceId) throws Exception {
        File file = new File(resourceId);
        if (file.exists()) {
            return read(file);
        }
        // Try as resource path
        try (InputStream is = getClass().getResourceAsStream(resourceId)) {
            if (is != null) {
                return read(is);
            }
        }
        throw new MathMLException("Resource not found: " + resourceId);
    }

    @Override
    protected MathMLDocument loadFromInputStream(InputStream is, String id) throws Exception {
        return read(is);
    }

    // ===== MathML-specific methods =====

    /**
     * Reads a MathML document from an input stream.
     *
     * @param input the input stream containing MathML data
     * @return the parsed MathML document
     * @throws MathMLException if parsing fails
     */
    public MathMLDocument read(InputStream input) throws MathMLException {
        try {
            DocumentBuilderFactory factory = org.episteme.core.io.SecureXMLFactory.createSecureNamespaceAwareDocumentBuilderFactory();
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            // Parse into a standard DOM document first
            org.w3c.dom.Document stdDoc = builder.parse(input);
            
            // Create MathML document and import the content
            MathMLDocumentImpl mathDoc = new MathMLDocumentImpl();
            Element root = stdDoc.getDocumentElement();
            
            // Import the parsed content into the MathML document
            if (root != null) {
                Node imported = mathDoc.importNode(root, true);
                if (imported instanceof Element) {
                    // Create the proper MathML elements
                    Element mathElement = mathDoc.createElementNS(MATHML_NS, root.getLocalName());
                    copyChildren(root, mathElement, mathDoc);
                    mathDoc.appendChild(mathElement);
                }
            }
            
            return mathDoc;
        } catch (Exception e) {
            throw new MathMLException("Failed to parse MathML", e);
        }
    }

    /**
     * Reads a MathML document from a file.
     *
     * @param file the file containing MathML data
     * @return the parsed MathML document
     * @throws MathMLException if parsing fails
     */
    public MathMLDocument read(File file) throws MathMLException {
        try (FileInputStream fis = new FileInputStream(file)) {
            return read(fis);
        } catch (IOException e) {
            throw new MathMLException("Failed to read file: " + file, e);
        }
    }

    /**
     * Reads a MathML document from a string.
     *
     * @param mathml the MathML content as a string
     * @return the parsed MathML document
     * @throws MathMLException if parsing fails
     */
    public MathMLDocument readFromString(String mathml) throws MathMLException {
        return read(new ByteArrayInputStream(mathml.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    }

    private void copyChildren(Element source, Element target, MathMLDocumentImpl doc) {
        org.w3c.dom.NodeList children = source.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof Element) {
                Element childElem = (Element) child;
                String ns = childElem.getNamespaceURI();
                if (ns == null) ns = MATHML_NS;
                Element newChild = doc.createElementNS(ns, childElem.getLocalName());
                
                // Copy attributes
                org.w3c.dom.NamedNodeMap attrs = childElem.getAttributes();
                for (int j = 0; j < attrs.getLength(); j++) {
                    Node attr = attrs.item(j);
                    newChild.setAttribute(attr.getNodeName(), attr.getNodeValue());
                }
                
                // Copy text content or recurse
                if (childElem.getChildNodes().getLength() == 1 && 
                    childElem.getFirstChild().getNodeType() == Node.TEXT_NODE) {
                    newChild.setTextContent(childElem.getTextContent());
                } else {
                    copyChildren(childElem, newChild, doc);
                }
                
                target.appendChild(newChild);
            } else if (child.getNodeType() == Node.TEXT_NODE) {
                String text = child.getTextContent().trim();
                if (!text.isEmpty()) {
                    target.appendChild(doc.createTextNode(text));
                }
            }
        }
    }
}

