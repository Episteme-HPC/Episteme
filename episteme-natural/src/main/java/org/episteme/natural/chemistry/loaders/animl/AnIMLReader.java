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

package org.episteme.natural.chemistry.loaders.animl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Analytical Information Markup Language (AnIML) Reader.
 * <p>
 * Parses AnIML files containing spectroscopic, chromatographic, and other
 * analytical chemistry data. AnIML is an ASTM standard (E2077) for representing
 * analytical instrument data.
 * </p>
 * <p>
 * <b>Supported Data Types:</b>
 * <ul>
 *   <li>Spectroscopy data (UV-Vis, IR, NMR, MS)</li>
 *   <li>Chromatography data (GC, HPLC, IC)</li>
 *   <li>Electrochemical data</li>
 *   <li>Thermal analysis data (DSC, TGA)</li>
 * </ul>
 * </p>
 * <p>
 * This implementation uses standard DOM parsing to remove dependencies on
 * external schemas (org.astm).
 * </p>
 * * @see <a href="https://www.astm.org/e2077-22.html">ASTM E2077 AnIML Standard</a>
 * @author Silvere Martin-Michiellot
 * @author Gemini AI (Google DeepMind)
 * @since 1.0
 */
public class AnIMLReader {
    
    /**
     * Creates a new AnIMLReader.
     */
    public AnIMLReader() {
    }

    /**
     * Reads AnIML data from an input stream.
     *
     * @param input the input stream containing AnIML data
     * @return an AnIML document containing parsed analytical data
     * @throws AnIMLException if parsing fails
     */
    public AnIMLDocument read(InputStream input) throws AnIMLException {
        try {
            DocumentBuilderFactory dbf = org.episteme.core.io.SecureXMLFactory.createSecureNamespaceAwareDocumentBuilderFactory();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(input);
            return convertToDocument(doc.getDocumentElement());
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new AnIMLException("Failed to parse AnIML data", e);
        }
    }

    /**
     * Reads AnIML data from a file.
     *
     * @param file the file containing AnIML data
     * @return an AnIML document containing parsed analytical data
     * @throws AnIMLException if parsing fails
     */
    public AnIMLDocument read(File file) throws AnIMLException {
        try (InputStream is = new FileInputStream(file)) {
            return read(is);
        } catch (IOException e) {
            throw new AnIMLException("Failed to read AnIML file: " + file, e);
        }
    }

    /**
     * Converts DOM-parsed AnIML objects to Episteme domain objects.
     */
    private AnIMLDocument convertToDocument(Element root) throws AnIMLException {
        AnIMLDocument doc = new AnIMLDocument();
        
        // Convert samples
        Element sampleSet = getChild(root, "SampleSet");
        if (sampleSet != null) {
            for (Element sample : getChildren(sampleSet, "Sample")) {
                doc.addSample(convertSample(sample));
            }
        }
        
        // Convert experiment steps
        Element experimentSteps = getChild(root, "ExperimentStepSet");
        if (experimentSteps != null) {
            for (Element step : getChildren(experimentSteps, "ExperimentStep")) {
                doc.addExperimentStep(convertExperimentStep(step));
            }
        }
        
        return doc;
    }

    /**
     * Converts a DOM sample element to an AnIMLSample.
     */
    private AnIMLSample convertSample(Element sample) {
        AnIMLSample result = new AnIMLSample();
        result.setId(getAttribute(sample, "sampleID"));
        result.setName(getAttribute(sample, "name"));
        
        // Extract container info if present
        String containerID = getAttribute(sample, "containerID");
        if (containerID != null) {
            result.setContainerId(containerID);
        }
        
        return result;
    }

    /**
     * Converts a DOM experiment step element to an AnIMLExperimentStep.
     */
    private AnIMLExperimentStep convertExperimentStep(Element step) {
        AnIMLExperimentStep result = new AnIMLExperimentStep();
        result.setId(getAttribute(step, "experimentStepID"));
        result.setName(getAttribute(step, "name"));
        
        // Convert technique info
        Element technique = getChild(step, "Technique");
        if (technique != null) {
            result.setTechniqueName(getAttribute(technique, "name"));
            result.setTechniqueUri(getAttribute(technique, "uri"));
        }
        
        // Convert results containing series data
        for (Element resultElement : getChildren(step, "Result")) {
            Element seriesSet = getChild(resultElement, "SeriesSet");
            if (seriesSet != null) {
                for (Element series : getChildren(seriesSet, "Series")) {
                    result.addSeriesData(convertSeries(series));
                }
            }
        }
        
        return result;
    }

    /**
     * Converts a DOM series element to AnIMLSeriesData.
     */
    private AnIMLSeriesData convertSeries(Element series) {
        AnIMLSeriesData result = new AnIMLSeriesData();
        result.setName(getAttribute(series, "name"));
        result.setSeriesId(getAttribute(series, "seriesID"));
        result.setDependency(getAttribute(series, "dependency"));
        result.setPlotScale(getAttribute(series, "plotScale"));
        
        // Extract unit if present
        Element unit = getChild(series, "Unit");
        if (unit != null) {
            result.setUnitLabel(getAttribute(unit, "label"));
            result.setUnitQuantity(getAttribute(unit, "quantity"));
        }
        
        // Extract data values
        extractSeriesValues(series, result);
        
        return result;
    }

    /**
     * Extracts numerical values from a series element.
     */
    private void extractSeriesValues(Element series, AnIMLSeriesData result) {
        // Check for individual values
        Element individualValues = getChild(series, "IndividualValueSet");
        if (individualValues != null) {
            List<Double> values = new ArrayList<>();
            NodeList children = individualValues.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node node = children.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    try {
                        String text = node.getTextContent().trim();
                        if (!text.isEmpty()) {
                            values.add(Double.parseDouble(text));
                        }
                    } catch (NumberFormatException e) {
                        /* ignore non-numeric content */
                    }
                }
            }
            result.setValues(toDoubleArray(values));
        }
        
        // Check for encoded values (Base64 encoded binary data)
        Element encodedValues = getChild(series, "EncodedValueSet");
        if (encodedValues != null) {
            String content = encodedValues.getTextContent().trim();
            if (!content.isEmpty()) {
                try {
                    byte[] decoded = Base64.getDecoder().decode(content);
                    result.setEncodedData(decoded);
                } catch (IllegalArgumentException e) {
                    /* ignore invalid base64 */
                }
            }
        }
        
        // Check for auto-incremented values (start, increment, end)
        Element autoIncrementedValues = getChild(series, "AutoIncrementedValueSet");
        if (autoIncrementedValues != null) {
            Double start = extractFirstDouble(getChild(autoIncrementedValues, "StartValue"));
            Double increment = extractFirstDouble(getChild(autoIncrementedValues, "Increment"));
            
            if (start != null && increment != null) {
                int count = result.getValues() != null ? result.getValues().length : 0;
                
                // Generate values if we know the count
                if (count > 0) {
                    double[] generated = new double[count];
                    for (int i = 0; i < count; i++) {
                        generated[i] = start + i * increment;
                    }
                    result.setAutoIncrementedValues(generated);
                }
                
                result.setStartValue(start);
                result.setIncrement(increment);
            }
        }
    }

    private Double extractFirstDouble(Element parent) {
        if (parent == null) return null;
        try {
            String text = parent.getTextContent().trim();
            if (!text.isEmpty()) {
                return Double.parseDouble(text);
            }
        } catch (NumberFormatException e) {
            /* continue */
        }
        return null;
    }

    /**
     * Converts a list of Double to a primitive array.
     */
    private double[] toDoubleArray(List<Double> list) {
        double[] array = new double[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    // Helper methods for DOM navigation

    private Element getChild(Element parent, String localName) {
        NodeList nl = parent.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                String nodeName = n.getLocalName();
                if (nodeName == null) nodeName = n.getNodeName();
                if (localName.equals(nodeName)) {
                    return (Element) n;
                }
            }
        }
        return null;
    }

    private List<Element> getChildren(Element parent, String localName) {
        List<Element> list = new ArrayList<>();
        NodeList nl = parent.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                String nodeName = n.getLocalName();
                if (nodeName == null) nodeName = n.getNodeName();
                if (localName.equals(nodeName)) {
                    list.add((Element) n);
                }
            }
        }
        return list;
    }

    private String getAttribute(Element e, String name) {
        if (e.hasAttribute(name)) {
            return e.getAttribute(name);
        }
        return null;
    }
}

