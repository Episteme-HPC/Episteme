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

package org.episteme.core.mathematics.loaders.openmath;

import org.episteme.core.io.AbstractResourceReader;
import org.episteme.core.mathematics.numbers.real.Real;
import org.episteme.core.mathematics.numbers.real.RealDouble;
import org.episteme.core.mathematics.numbers.complex.Complex;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;
import java.io.*;
import java.util.*;

/**
 * OpenMath Reader for mathematical objects.
 * <p>
 * OpenMath is an extensible standard for representing the semantics of
 * mathematical objects. This reader parses OpenMath XML and converts
 * it to Episteme mathematical objects where possible.
 * </p>
 * <p>
 * <b>Supported OpenMath Elements:</b>
 * <ul>
 *   <li><b>OMI:</b> Integers → Long</li>
 *   <li><b>OMF:</b> Floats → RealDouble</li>
 *   <li><b>OMSTR:</b> Strings → String</li>
 *   <li><b>OMV:</b> Variables → OpenMathVariable</li>
 *   <li><b>OMS:</b> Symbols → OpenMathSymbol</li>
 *   <li><b>OMA:</b> Applications → OpenMathApplication (or Complex for complex_cartesian)</li>
 *   <li><b>OMBIND:</b> Bindings → OpenMathBinding</li>
 *   <li><b>OMB:</b> Byte arrays → byte[]</li>
 * </ul>
 * </p>
 * <p>
 * <b>Example:</b>
 * <pre>{@code
 * OpenMathReader reader = new OpenMathReader();
 * Object obj = reader.read(new File("expression.om"));
 * if (obj instanceof Complex) {
 *     Complex c = (Complex) obj;
 *     System.out.println("Complex number: " + c);
 * }
 * }</pre>
 * </p>
 * * @see <a href="https://openmath.org/">OpenMath.org</a>
 * @see OpenMathWriter
 * @author Silvere Martin-Michiellot
 * @author Gemini AI (Google DeepMind)
 * @since 1.0
 */
public class OpenMathReader extends AbstractResourceReader<Object> {

    public OpenMathReader() {
    }

    // ===== ResourceReader interface =====

    @Override
    public String getResourcePath() {
        return null; // File-based
    }

    @Override
    public Class<Object> getResourceType() {
        return Object.class;
    }

    @Override
    public String getName() {
        return "OpenMath Reader";
    }

    @Override
    public String getDescription() {
        return "Reads mathematical objects from OpenMath XML format";
    }

    @Override
    public String getLongDescription() {
        return "OpenMath is an extensible standard for representing the semantics " +
               "of mathematical objects. Supports Content Dictionaries for arithmetic, " +
               "complex numbers, linear algebra, and more.";
    }

    @Override
    public String getCategory() {
        return "Mathematics";
    }

    @Override
    public String[] getSupportedVersions() {
        return new String[] {"2.0"};
    }

    @Override
    public String[] getSupportedExtensions() {
        return new String[] {".om", ".xml"};
    }

    @Override
    protected Object loadFromSource(String resourceId) throws Exception {
        File file = new File(resourceId);
        if (file.exists()) {
            return read(file);
        }
        try (InputStream is = getClass().getResourceAsStream(resourceId)) {
            if (is != null) {
                return read(is);
            }
        }
        throw new OpenMathException("Resource not found: " + resourceId);
    }

    @Override
    protected Object loadFromInputStream(InputStream is, String id) throws Exception {
        return read(is);
    }

    // ===== OpenMath-specific methods =====

    /**
     * Reads an OpenMath object from an input stream.
     *
     * @param input the input stream containing OpenMath XML
     * @return the parsed mathematical object
     * @throws OpenMathException if parsing fails
     */
    public Object read(InputStream input) throws OpenMathException {
        try {
            DocumentBuilderFactory factory = org.episteme.core.io.SecureXMLFactory.createSecureNamespaceAwareDocumentBuilderFactory();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(input);
            return parseDocument(doc);
        } catch (Exception e) {
            throw new OpenMathException("Failed to parse OpenMath", e);
        }
    }

    /**
     * Reads an OpenMath object from a file.
     *
     * @param file the file containing OpenMath XML
     * @return the parsed mathematical object
     * @throws OpenMathException if parsing fails
     */
    public Object read(File file) throws OpenMathException {
        try (FileInputStream fis = new FileInputStream(file)) {
            return read(fis);
        } catch (IOException e) {
            throw new OpenMathException("Failed to read file: " + file, e);
        }
    }

    /**
     * Reads an OpenMath object from a string.
     *
     * @param openmath the OpenMath XML as a string
     * @return the parsed mathematical object
     * @throws OpenMathException if parsing fails
     */
    public Object readFromString(String openmath) throws OpenMathException {
        return read(new ByteArrayInputStream(openmath.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    }

    private Object parseDocument(Document doc) throws OpenMathException {
        Element root = doc.getDocumentElement();
        String localName = root.getLocalName();
        
        if ("OMOBJ".equals(localName)) {
            // The content is the first child element
            NodeList children = root.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                if (children.item(i) instanceof Element) {
                    return parseElement((Element) children.item(i));
                }
            }
            throw new OpenMathException("Empty OMOBJ element");
        } else {
            // Root is already the content element
            return parseElement(root);
        }
    }

    private Object parseElement(Element elem) throws OpenMathException {
        String name = elem.getLocalName();
        if (name == null) name = elem.getTagName();
        
        switch (name) {
            case "OMI":
                return parseInteger(elem);
            case "OMF":
                return parseFloat(elem);
            case "OMSTR":
                return parseString(elem);
            case "OMV":
                return parseVariable(elem);
            case "OMS":
                return parseSymbol(elem);
            case "OMA":
                return parseApplication(elem);
            case "OMBIND":
                return parseBinding(elem);
            case "OMB":
                return parseByteArray(elem);
            case "OME":
                return parseError(elem);
            case "OMATTR":
                return parseAttribution(elem);
            default:
                throw new OpenMathException("Unknown OpenMath element: " + name);
        }
    }

    private Long parseInteger(Element elem) {
        String text = elem.getTextContent().trim();
        // Handle hex format
        if (text.startsWith("x") || text.startsWith("X")) {
            return Long.parseLong(text.substring(1), 16);
        }
        return Long.parseLong(text);
    }

    private RealDouble parseFloat(Element elem) {
        String dec = elem.getAttribute("dec");
        String hex = elem.getAttribute("hex");
        
        double value;
        if (dec != null && !dec.isEmpty()) {
            value = Double.parseDouble(dec);
        } else if (hex != null && !hex.isEmpty()) {
            value = Double.longBitsToDouble(Long.parseLong(hex, 16));
        } else {
            // Try text content
            value = Double.parseDouble(elem.getTextContent().trim());
        }
        
        return (RealDouble) RealDouble.of(value);
    }

    private String parseString(Element elem) {
        return elem.getTextContent();
    }

    private OpenMathVariable parseVariable(Element elem) {
        String name = elem.getAttribute("name");
        return new OpenMathVariable(name);
    }

    private OpenMathSymbol parseSymbol(Element elem) {
        String cd = elem.getAttribute("cd");
        String name = elem.getAttribute("name");
        return new OpenMathSymbol(cd, name);
    }

    private Object parseApplication(Element elem) throws OpenMathException {
        List<Object> args = new ArrayList<>();
        NodeList children = elem.getChildNodes();
        
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                args.add(parseElement((Element) children.item(i)));
            }
        }
        
        if (args.isEmpty()) {
            throw new OpenMathException("Empty OMA element");
        }
        
        Object operator = args.get(0);
        List<Object> operands = args.subList(1, args.size());
        
        // Check for known content dictionary functions
        if (operator instanceof OpenMathSymbol) {
            OpenMathSymbol sym = (OpenMathSymbol) operator;
            
            // Complex numbers from complex1 CD
            if ("complex1".equals(sym.getContentDictionary()) && 
                "complex_cartesian".equals(sym.getName())) {
                if (operands.size() == 2) {
                    Real real = toReal(operands.get(0));
                    Real imag = toReal(operands.get(1));
                    return Complex.of(real, imag);
                }
            }
            
            // Could add more CD interpretations here
        }
        
        return new OpenMathApplication(operator, operands);
    }

    private Real toReal(Object obj) {
        if (obj instanceof Real) {
            return (Real) obj;
        } else if (obj instanceof Number) {
            return RealDouble.of(((Number) obj).doubleValue());
        }
        throw new IllegalArgumentException("Cannot convert to Real: " + obj);
    }

    private OpenMathBinding parseBinding(Element elem) throws OpenMathException {
        Object binder = null;
        List<OpenMathVariable> variables = new ArrayList<>();
        Object body = null;
        
        NodeList children = elem.getChildNodes();
        int elementIndex = 0;
        
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                Element child = (Element) children.item(i);
                String name = child.getLocalName();
                
                if (elementIndex == 0) {
                    // First element is the binder
                    binder = parseElement(child);
                } else if ("OMBVAR".equals(name)) {
                    // Parse bound variables
                    NodeList vars = child.getChildNodes();
                    for (int j = 0; j < vars.getLength(); j++) {
                        if (vars.item(j) instanceof Element) {
                            Element varElem = (Element) vars.item(j);
                            if ("OMV".equals(varElem.getLocalName())) {
                                variables.add(parseVariable(varElem));
                            }
                        }
                    }
                } else {
                    // Last element is the body
                    body = parseElement(child);
                }
                elementIndex++;
            }
        }
        
        return new OpenMathBinding(binder, variables, body);
    }

    private byte[] parseByteArray(Element elem) {
        String text = elem.getTextContent().trim();
        // Base64 decode
        return Base64.getDecoder().decode(text);
    }

    private OpenMathError parseError(Element elem) throws OpenMathException {
        List<Object> components = new ArrayList<>();
        NodeList children = elem.getChildNodes();
        
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                components.add(parseElement((Element) children.item(i)));
            }
        }
        
        OpenMathSymbol errorSymbol = components.isEmpty() ? null : 
            (components.get(0) instanceof OpenMathSymbol ? (OpenMathSymbol) components.get(0) : null);
        List<Object> errorArgs = components.size() > 1 ? components.subList(1, components.size()) : Collections.emptyList();
        
        return new OpenMathError(errorSymbol, errorArgs);
    }

    private Object parseAttribution(Element elem) throws OpenMathException {
        // Parse OMATTR - attributed object
        NodeList children = elem.getChildNodes();
        Object content = null;
        Map<OpenMathSymbol, Object> attributes = new LinkedHashMap<>();
        
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                Element child = (Element) children.item(i);
                String name = child.getLocalName();
                
                if ("OMATP".equals(name)) {
                    // Parse attribute pairs
                    NodeList pairs = child.getChildNodes();
                    OpenMathSymbol currentKey = null;
                    for (int j = 0; j < pairs.getLength(); j++) {
                        if (pairs.item(j) instanceof Element) {
                            Element pairElem = (Element) pairs.item(j);
                            if (currentKey == null && "OMS".equals(pairElem.getLocalName())) {
                                currentKey = parseSymbol(pairElem);
                            } else if (currentKey != null) {
                                attributes.put(currentKey, parseElement(pairElem));
                                currentKey = null;
                            }
                        }
                    }
                } else {
                    content = parseElement(child);
                }
            }
        }
        
        return new OpenMathAttributed(content, attributes);
    }
}

