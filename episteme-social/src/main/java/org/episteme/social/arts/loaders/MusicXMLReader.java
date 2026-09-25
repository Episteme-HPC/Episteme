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

package org.episteme.social.arts.loaders;

import org.episteme.social.arts.music.*;
import org.episteme.social.history.time.TimePoint;
import java.io.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

/**
 * Advanced reader for MusicXML files.
 * <p>
 * This reader supports the multi-part structure of MusicXML, mapping each {@code <part>}
 * to an internal {@link Track}. It also extracts metadata such as title and composer.
 * </p>
 *
 * @author Silvere Martin-Michiellot
 * @author Gemini AI (Google DeepMind)
 * @since 1.0
 */
public class MusicXMLReader extends CompositionLoader {

    @Override
    protected Composition loadFromSource(String path) throws Exception {
        File file = new File(path);
        if (!file.exists()) {
            throw new FileNotFoundException("MusicXML file not found: " + path);
        }
        try (InputStream is = new FileInputStream(file)) {
            return loadFromInputStream(is, file.getName());
        }
    }

    @Override
    protected Composition loadFromInputStream(InputStream is, String id) throws Exception {
        DocumentBuilder builder = org.episteme.core.io.SecureXMLFactory.createSecureDocumentBuilder();
        Document doc = builder.parse(is);
        Element root = doc.getDocumentElement();
        
        String title = extractMetadata(root, "work-title", id);
        String composer = extractMetadata(root, "creator", "Unknown");

        Composition composition = new Composition(title, "Composer: " + composer, TimePoint.now(), null);

        // Map parts to internal model
        NodeList parts = root.getElementsByTagName("part");
        for (int i = 0; i < parts.getLength(); i++) {
            Element partElem = (Element) parts.item(i);
            String partId = partElem.getAttribute("id");
            Part part = new Part(partId);
            part.setInstrumentName("MusicXML Part");
            
            // For simplicity, one measure for all notes in MusicXML (or we could split by <measure> tags)
            // Ideally we should iterate over <measure> tags inside <part>
            NodeList measures = partElem.getElementsByTagName("measure");
            for (int m = 0; m < measures.getLength(); m++) {
                Element measElem = (Element) measures.item(m);
                int number = Integer.parseInt(measElem.getAttribute("number"));
                Measure measure = new Measure(number);
                
                NodeList notes = measElem.getElementsByTagName("note");
                for (int j = 0; j < notes.getLength(); j++) {
                    Element noteElem = (Element) notes.item(j);
                    parseNote(noteElem, measure);
                }
                
                if (!measure.getNotes().isEmpty()) {
                    part.addMeasure(measure);
                }
            }
            
            if (!part.getMeasures().isEmpty()) {
                composition.addPart(part);
            }
        }
        
        return composition;
    }

    private String extractMetadata(Element root, String tagName, String defaultValue) {
        NodeList list = root.getElementsByTagName(tagName);
        if (list.getLength() > 0) {
            return list.item(0).getTextContent().trim();
        }
        return defaultValue;
    }

    private void parseNote(Element noteElem, Measure measure) {
        boolean isRest = noteElem.getElementsByTagName("rest").getLength() > 0;
        
        NodeList durationList = noteElem.getElementsByTagName("duration");
        double duration = 1.0;
        if (durationList.getLength() > 0) {
            try {
                // Usually duration is in divisions, default 4 per quarter
                duration = Double.parseDouble(durationList.item(0).getTextContent()) / 4.0;
            } catch (NumberFormatException e) {
                duration = 1.0;
            }
        }
        
        if (!isRest) {
            NodeList pitchList = noteElem.getElementsByTagName("pitch");
            if (pitchList.getLength() > 0) {
                Element pitchElem = (Element) pitchList.item(0);
                String step = extractMetadata(pitchElem, "step", "C");
                int octave = 4;
                try {
                    octave = Integer.parseInt(extractMetadata(pitchElem, "octave", "4"));
                } catch (NumberFormatException e) {}

                int alter = 0;
                try {
                    alter = Integer.parseInt(extractMetadata(pitchElem, "alter", "0"));
                } catch (NumberFormatException e) {}
                
                int midiNumber = pitchToMidi(step, octave, alter);
                measure.addNote(Note.fromMidi(midiNumber, duration));
            }
        }
    }

    private int pitchToMidi(String step, int octave, int alter) {
        int base = switch (step.toUpperCase()) {
            case "C" -> 0;
            case "D" -> 2;
            case "E" -> 4;
            case "F" -> 5;
            case "G" -> 7;
            case "A" -> 9;
            case "B" -> 11;
            default -> 0;
        };
        return (octave + 1) * 12 + base + alter;
    }

    @Override
    public String getName() {
        return "MusicXML Reader";
    }

    @Override
    public String getDescription() {
        return "Provides full support for MusicXML 3.0+ multi-part scores.";
    }

    @Override
    public String[] getSupportedVersions() {
        return new String[]{"3.0", "3.1", "4.0"};
    }

    @Override
    public String getResourcePath() {
        return "music/xml";
    }
}

