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

package org.episteme.social.law.loaders;

import org.episteme.core.io.AbstractResourceReader;
import org.episteme.social.law.Article;
import org.episteme.social.law.Statute;
import org.episteme.social.law.StatuteStatus;
import org.episteme.social.law.StatuteType;
import org.episteme.core.util.Numbering;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Loader for Akoma Ntoso (XML-based) legal documents.
 * <p>
 * This implementation uses DOM parsing to extract the hierarchical structure
 * of legal acts, including metadata and articles.
 * </p>
 *
 * @author Silvere Martin-Michiellot
 * @author Gemini AI (Google DeepMind)
 * @since 1.2
 * @version 2.0 (Modernized)
 * @see <a href="http://www.akomantoso.org/">Akoma Ntoso Standard</a>
 */
public final class AkomaNtosoLoader extends AbstractResourceReader<Statute> implements LegalDocumentLoader {

    @Override
    protected Statute loadFromSource(String path) throws Exception {
        // Standard implementation for loading from a file path
        // In a real scenario, this would use a URL or File object.
        throw new UnsupportedOperationException("File-based loading not fully implemented. Use loadContent().");
    }

    @Override
    protected Statute loadFromInputStream(InputStream is, String id) throws Exception {
        DocumentBuilder builder = org.episteme.core.io.SecureXMLFactory.createSecureDocumentBuilder();
        Document doc = builder.parse(is);
        Element root = doc.getDocumentElement();

        // Find the act type (act, bill, debate, etc.)
        Element actPart = findActPart(root);
        if (actPart == null) throw new Exception("No <act> or similar component found in Akoma Ntoso document.");

        String title = "Unknown Title";
        NodeList titles = actPart.getElementsByTagName("docTitle");
        if (titles.getLength() > 0) {
            title = titles.item(0).getTextContent().trim();
        }

        Statute statute = new Statute("AKN-" + id, title, StatuteType.ACT, "International", 2025, StatuteStatus.ENACTED);

        // Parse metadata if available
        parseMetadata(root, statute);

        // Parse articles
        NodeList articleNodes = actPart.getElementsByTagName("article");
        for (int i = 0; i < articleNodes.getLength(); i++) {
            Element artElem = (Element) articleNodes.item(i);
            String artNumStr = artElem.getAttribute("eId").replaceAll("[^0-9.]", "");
            String content = artElem.getTextContent().trim();
            statute.addArticle(new Article(Numbering.parse(artNumStr), content));
        }

        return statute;
    }

    private void parseMetadata(Element root, Statute statute) {
        NodeList metaList = root.getElementsByTagName("meta");
        if (metaList.getLength() == 0) return;
        Element meta = (Element) metaList.item(0);

        // Date
        NodeList dateNodes = meta.getElementsByTagName("FRBRdate");
        for (int i = 0; i < dateNodes.getLength(); i++) {
            Element el = (Element) dateNodes.item(i);
            String name = el.getAttribute("name");
            String date = el.getAttribute("date");
            if (date != null && !date.isEmpty()) {
                 statute.getTraits().put("date." + name, date);
                 if ("enacted".equalsIgnoreCase(name) || "generation".equalsIgnoreCase(name)) {
                     try {
                         if (date.length() >= 4) {
                            statute.setYearEnacted(Integer.parseInt(date.substring(0, 4)));
                         }
                     } catch (NumberFormatException ignored) {}
                 }
            }
        }

        // Author
        NodeList authorNodes = meta.getElementsByTagName("FRBRauthor");
        if (authorNodes.getLength() > 0) {
            String author = ((Element)authorNodes.item(0)).getAttribute("href");
            statute.getTraits().put("author", author);
        }
        
        // URI
        NodeList uriNodes = meta.getElementsByTagName("FRBRuri");
        if (uriNodes.getLength() > 0) {
            String uri = ((Element)uriNodes.item(0)).getAttribute("value");
            statute.getTraits().put("definedBy", uri);
        }
    }

    private Element findActPart(Element root) {
        String[] types = {"act", "bill", "debateRecord", "judgment", "amendment"};
        for (String t : types) {
            NodeList list = root.getElementsByTagName(t);
            if (list.getLength() > 0) return (Element) list.item(0);
        }
        return null;
    }

    @Override
    public Statute loadContent(String xml) {
        try {
            return loadFromInputStream(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), "content");
        } catch (Exception e) {
            return new Statute("ERROR", "Invalid XML Content", StatuteType.REGULATION, "Error", 0, StatuteStatus.PROPOSED);
        }
    }

    @Override
    public String getName() {
        return "Akoma Ntoso XML Loader";
    }

    @Override
    public String getDescription() {
        return "Specialized reader for Akoma Ntoso international legal XML standard.";
    }

    @Override
    public String getLongDescription() {
        return "Parses Akoma Ntoso documents into hierarchical Statutes and Articles, " +
               "supporting parliamentary, legislative, and judiciary documents.";
    }

    @Override
    public String getCategory() {
        return "Law / XML Standard";
    }

    @Override
    public String getResourcePath() {
        return "law/akn";
    }

    @Override
    public Class<Statute> getResourceType() {
        return Statute.class;
    }

    @Override
    public String[] getSupportedVersions() {
        return new String[]{"3.0", "2.0"};
    }
}

