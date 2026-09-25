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

package org.episteme.natural.chemistry.loaders.cml.dom.pmr;

import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;

/** DOM-based re-write of JUMBO functionality
 * Delegate functionality for DOM
 @author P.Murray-Rust, 1999
 */

/**
 * The <code>Document</code> interface represents the entire HTML or XML
 * document. Conceptually, it is the root of the document tree, and provides
 * the  primary access to the document's data.
 * <p>Since elements, text nodes, comments, processing instructions, etc.
 * cannot exist outside the context of a <code>Document</code>, the
 * <code>Document</code> interface also contains the factory methods needed
 * to create these objects.  The <code>Node</code> objects created have a
 * <code>ownerDocument</code> attribute which associates them with the
 * <code>Document</code> within whose  context they were created.
 * @author Silvere Martin-Michiellot
 * @author Gemini AI (Google DeepMind)
 * @since 1.0
 */
public class PMRDocumentImpl extends PMRNodeImpl implements PMRDocument {

// the delegate Document which carries the functionality is inherited from Node

    // this shouldn't be called directly

    protected PMRDocumentImpl() {
        try {
            pmrDocument = this;
            delegateNode = org.episteme.core.io.SecureXMLFactory.createSecureDocumentBuilder().newDocument();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

// called from subclasses when transferring delegation to subclass

    // oldPmrDoc is effectively destroyed

    protected PMRDocumentImpl(PMRDocumentImpl oldPmrDoc) {
        this((Document) oldPmrDoc.delegateNode);
        this.delegateNode = (Document) oldPmrDoc.delegateNode;
// transfer
        while (true) {
            PMRNodeImpl pmrNode = (PMRNodeImpl) oldPmrDoc.getFirstChild();
            if (pmrNode == null) break;
            oldPmrDoc.removeChild(pmrNode);
            pmrNode.setOwnerDocumentRecursively(this);
            this.appendChild(pmrNode);
        }
    }

    // creates a PMRDocument from existing w3Document
    public PMRDocumentImpl(Document document) {
// although Document is a node, simpler to do this without constructors
        super();
        pmrDocument = this;
        this.delegateNode = document;
        createAndAddChildren(this, document, 0);
    }

    /**
     * The Document Type Declaration (see <code>DocumentType</code>) associated
     * with  this document. For HTML documents as well as XML documents without
     * a document type declaration this returns <code>null</code>. The DOM Level
     * 1 does not support editing the Document Type Declaration, therefore
     * <code>docType</code> cannot be altered in any way.
     */
    public DocumentType getDoctype() {
        DocumentType dt = ((Document) delegateNode).getDoctype();
        return new PMRDocumentTypeImpl(dt, this);
    }

    /**
     * The <code>DOMImplementation</code> object that handles this document. A
     * DOM application may use objects from multiple  implementations.
     */
    public DOMImplementation getImplementation() {
        DOMImplementation di = ((Document) delegateNode).getImplementation();
        return new PMRDOMImplementationImpl(di);
    }

    /**
     * This is a convenience attribute that allows direct access to the child
     * node that is the root element of  the document. For HTML documents, this
     * is the element with the tagName "HTML".
     */
    public Element getDocumentElement() {
        if (delegateNode == null || childNodes == null) return null;
        for (int i = 0; i < childNodes.size(); i++) {
            Node n = childNodes.get(i);
            if (n instanceof Element) {
                return (Element) n;
            }
        }
        return null;
    }

    /**
     * Creates an element of the type specified. Note that the instance returned
     * implements the Element interface, so attributes can be specified
     * directly  on the returned object.
     *
     * @param tagName The name of the element type to instantiate. For XML, this
     *                is case-sensitive. For HTML, the  <code>tagName</code> parameter may
     *                be provided in any case,  but it must be mapped to the canonical
     *                uppercase form by  the DOM implementation.
     * @return A new <code>Element</code> object.
     * @throws DOMException INVALID_CHARACTER_ERR: Raised if the specified name contains an
     *                      invalid character.
     */
    public Element createElement(String tagName) throws DOMException {
        Element element = ((Document) delegateNode).createElement(tagName);
        return new PMRElementImpl(element, this);
    }

    /**
     * Creates an empty <code>DocumentFragment</code> object.
     *
     * @return A new <code>DocumentFragment</code>.
     */
    public DocumentFragment createDocumentFragment() {
        DocumentFragment df = ((Document) delegateNode).createDocumentFragment();
        return new PMRDocumentFragmentImpl(df, this);
    }

    /**
     * Creates a <code>Text</code> node given the specified string.
     *
     * @param data The data for the node.
     * @return The new <code>Text</code> object.
     */
    public Text createTextNode(String data) {
        Text text = ((Document) delegateNode).createTextNode(data);
        return new PMRTextImpl(text, this);
    }

    /**
     * Creates a <code>Comment</code> node given the specified string.
     *
     * @param data The data for the node.
     * @return The new <code>Comment</code> object.
     */
    public Comment createComment(String data) {
        Comment comment = ((Document) delegateNode).createComment(data);
        return new PMRCommentImpl(comment, this);
    }

    /**
     * Creates a <code>CDATASection</code> node whose value  is the specified
     * string.
     *
     * @param data The data for the <code>CDATASection</code> contents.
     * @return The new <code>CDATASection</code> object.
     * @throws DOMException NOT_SUPPORTED_ERR: Raised if this document is an HTML document.
     */
    public CDATASection createCDATASection(String data) throws DOMException {
        CDATASection cds = ((Document) delegateNode).createCDATASection(data);
        return new PMRCDATASectionImpl(cds, this);
    }

    /**
     * Creates a <code>ProcessingInstruction</code> node given the specified
     * name and data strings.
     *
     * @param target The target part of the processing instruction.
     * @param data   The data for the node.
     * @return The new <code>ProcessingInstruction</code> object.
     * @throws DOMException INVALID_CHARACTER_ERR: Raised if an invalid character is specified.
     *                      <br>NOT_SUPPORTED_ERR: Raised if this document is an HTML document.
     */
    public ProcessingInstruction createProcessingInstruction
            (String target, String data) throws DOMException {
        ProcessingInstruction pi = ((Document) delegateNode).createProcessingInstruction(target, data);
        return new PMRProcessingInstructionImpl(pi, this);
    }

    /**
     * Creates an <code>Attr</code> of the given name. Note that the
     * <code>Attr</code> instance can then be set on an <code>Element</code>
     * using the <code>setAttribute</code> method.
     *
     * @param name The name of the attribute.
     * @return A new <code>Attr</code> object.
     * @throws DOMException INVALID_CHARACTER_ERR: Raised if the specified name contains an
     *                      invalid character.
     */
    public Attr createAttribute(String name) throws DOMException {
        Attr attr = ((Document) delegateNode).createAttribute(name);
        return attr;
    }

    /**
     * Creates an EntityReference object.
     *
     * @param name The name of the entity to reference.
     * @return The new <code>EntityReference</code> object.
     * @throws DOMException INVALID_CHARACTER_ERR: Raised if the specified name contains an
     *                      invalid character.
     *                      <br>NOT_SUPPORTED_ERR: Raised if this document is an HTML document.
     */
    public EntityReference createEntityReference(String name) throws DOMException {
        EntityReference er = ((Document) delegateNode).createEntityReference(name);
        return new PMREntityReferenceImpl(er, this);
    }

    /**
     * Returns a <code>NodeList</code> of all the <code>Element</code>s with a
     * given tag name in the order in which they would be encountered in a
     * preorder traversal of the <code>Document</code> tree.
     *
     * @param name The name of the tag to match on. The special value "*"
     *             matches all tags.
     * @return A new <code>NodeList</code> object containing all the matched
     *         <code>Element</code>s.
     */
    public NodeList getElementsByTagName(String name) {
        ArrayList<Node> descendantNodes = this.getAllElementDescendants();
        int l = descendantNodes.size();
        ArrayList<Node> elems = new ArrayList<Node>();
        for (int i = 0; i < l; i++) {
            Node node = descendantNodes.get(i);
            if (node.getNodeName().equals(name)) {
                elems.add(node);
            }
        }
        return new PMRNodeListImpl(elems);
    }

    public Element createElementNS(String s, String t) {
        Element element = ((Document) delegateNode).createElementNS(s, t);
        Element pmrElement = new PMRElementImpl(element, this);
        return pmrElement;
    }

    public NodeList getElementsByTagNameNS(String s, String t) {
        NodeList nodeList = ((Document) delegateNode).getElementsByTagNameNS(s, t);
        return new PMRNodeListImpl(nodeList);
    }

    /**
     * hopefully supported now
     */
    public Node importNode(Node node, boolean deepCopy) {
        Node newNode = node.cloneNode(deepCopy);
        ((PMRNodeImpl) newNode).setOwnerDocumentRecursively(this);

        Document delegateOwnerDoc = (Document) delegateNode;
        Node copiedDelegateNode = ((PMRNodeImpl) newNode).getDelegateNode();
        Node newDelegateNode = delegateOwnerDoc.importNode(copiedDelegateNode, true);

        ((PMRNodeImpl) newNode).wrapRecursively(newDelegateNode);
        return (newNode);
    }

    public Attr createAttributeNS(String s, String t) {
        Attr attr = ((Document) delegateNode).createAttributeNS(s, t);
        return attr;
    }

    /**
     * An attribute specifying the encoding used for this document at the time
     * of the parsing. This is <code>null</code> when it is not known, such
     * as when the <code>Document</code> was created in memory.
     *
     * @since DOM Level 3
     */
    public String getInputEncoding() {
        return ((Document) delegateNode).getInputEncoding();
    }

    /**
     * An attribute specifying, as part of the <a href='http://www.w3.org/TR/2004/REC-xml-20040204#NT-XMLDecl'>XML declaration</a>, the encoding of this document. This is <code>null</code> when
     * unspecified or when it is not known, such as when the
     * <code>Document</code> was created in memory.
     *
     * @since DOM Level 3
     */
    public String getXmlEncoding() {
        return ((Document) delegateNode).getXmlEncoding();
    }

    /**
     * An attribute specifying, as part of the <a href='http://www.w3.org/TR/2004/REC-xml-20040204#NT-XMLDecl'>XML declaration</a>, whether this document is standalone. This is <code>false</code> when
     * unspecified.
     * <p ><b>Note:</b>  No verification is done on the value when setting
     * this attribute. Applications should use
     * <code>Document.normalizeDocument()</code> with the "validate"
     * parameter to verify if the value matches the <a href='http://www.w3.org/TR/2004/REC-xml-20040204#sec-rmd'>validity
     * constraint for standalone document declaration</a> as defined in [<a href='http://www.w3.org/TR/2004/REC-xml-20040204'>XML 1.0</a>].
     *
     * @since DOM Level 3
     */
    public boolean getXmlStandalone() {
        return ((Document) delegateNode).getXmlStandalone();
    }

    /**
     * An attribute specifying, as part of the <a href='http://www.w3.org/TR/2004/REC-xml-20040204#NT-XMLDecl'>XML declaration</a>, whether this document is standalone. This is <code>false</code> when
     * unspecified.
     * <p ><b>Note:</b>  No verification is done on the value when setting
     * this attribute. Applications should use
     * <code>Document.normalizeDocument()</code> with the "validate"
     * parameter to verify if the value matches the <a href='http://www.w3.org/TR/2004/REC-xml-20040204#sec-rmd'>validity
     * constraint for standalone document declaration</a> as defined in [<a href='http://www.w3.org/TR/2004/REC-xml-20040204'>XML 1.0</a>].
     *
     * @throws DOMException NOT_SUPPORTED_ERR: Raised if this document does not support the
     *                      "XML" feature.
     * @since DOM Level 3
     */
    public void setXmlStandalone(boolean xmlStandalone) throws DOMException {
        ((Document) delegateNode).setXmlStandalone(xmlStandalone);
    }

    /**
     * An attribute specifying, as part of the <a href='http://www.w3.org/TR/2004/REC-xml-20040204#NT-XMLDecl'>XML declaration</a>, the version number of this document. If there is no declaration and if
     * this document supports the "XML" feature, the value is
     * <code>"1.0"</code>. If this document does not support the "XML"
     * feature, the value is always <code>null</code>. Changing this
     * attribute will affect methods that check for invalid characters in
     * XML names. Application should invoke
     * <code>Document.normalizeDocument()</code> in order to check for
     * invalid characters in the <code>Node</code>s that are already part of
     * this <code>Document</code>.
     * <br> DOM applications may use the
     * <code>DOMImplementation.hasFeature(feature, version)</code> method
     * with parameter values "XMLVersion" and "1.0" (respectively) to
     * determine if an implementation supports [<a href='http://www.w3.org/TR/2004/REC-xml-20040204'>XML 1.0</a>]. DOM
     * applications may use the same method with parameter values
     * "XMLVersion" and "1.1" (respectively) to determine if an
     * implementation supports [<a href='http://www.w3.org/TR/2004/REC-xml11-20040204/'>XML 1.1</a>]. In both
     * cases, in order to support XML, an implementation must also support
     * the "XML" feature defined in this specification. <code>Document</code>
     * objects supporting a version of the "XMLVersion" feature must not
     * raise a <code>NOT_SUPPORTED_ERR</code> exception for the same version
     * number when using <code>Document.xmlVersion</code>.
     *
     * @since DOM Level 3
     */
    public String getXmlVersion() {
        return ((Document) delegateNode).getXmlVersion();
    }

    /**
     * An attribute specifying, as part of the <a href='http://www.w3.org/TR/2004/REC-xml-20040204#NT-XMLDecl'>XML declaration</a>, the version number of this document. If there is no declaration and if
     * this document supports the "XML" feature, the value is
     * <code>"1.0"</code>. If this document does not support the "XML"
     * feature, the value is always <code>null</code>. Changing this
     * attribute will affect methods that check for invalid characters in
     * XML names. Application should invoke
     * <code>Document.normalizeDocument()</code> in order to check for
     * invalid characters in the <code>Node</code>s that are already part of
     * this <code>Document</code>.
     * <br> DOM applications may use the
     * <code>DOMImplementation.hasFeature(feature, version)</code> method
     * with parameter values "XMLVersion" and "1.0" (respectively) to
     * determine if an implementation supports [<a href='http://www.w3.org/TR/2004/REC-xml-20040204'>XML 1.0</a>]. DOM
     * applications may use the same method with parameter values
     * "XMLVersion" and "1.1" (respectively) to determine if an
     * implementation supports [<a href='http://www.w3.org/TR/2004/REC-xml11-20040204/'>XML 1.1</a>]. In both
     * cases, in order to support XML, an implementation must also support
     * the "XML" feature defined in this specification. <code>Document</code>
     * objects supporting a version of the "XMLVersion" feature must not
     * raise a <code>NOT_SUPPORTED_ERR</code> exception for the same version
     * number when using <code>Document.xmlVersion</code>.
     *
     * @throws DOMException NOT_SUPPORTED_ERR: Raised if the version is set to a value that is
     *                      not supported by this <code>Document</code> or if this document
     *                      does not support the "XML" feature.
     * @since DOM Level 3
     */
    public void setXmlVersion(String xmlVersion) throws DOMException {
        ((Document) delegateNode).setXmlVersion(xmlVersion);
    }

    /**
     * An attribute specifying whether error checking is enforced or not. When
     * set to <code>false</code>, the implementation is free to not test
     * every possible error case normally defined on DOM operations, and not
     * raise any <code>DOMException</code> on DOM operations or report
     * errors while using <code>Document.normalizeDocument()</code>. In case
     * of error, the behavior is undefined. This attribute is
     * <code>true</code> by default.
     *
     * @since DOM Level 3
     */
    public boolean getStrictErrorChecking() {
        return ((Document) delegateNode).getStrictErrorChecking();
    }

    /**
     * An attribute specifying whether error checking is enforced or not. When
     * set to <code>false</code>, the implementation is free to not test
     * every possible error case normally defined on DOM operations, and not
     * raise any <code>DOMException</code> on DOM operations or report
     * errors while using <code>Document.normalizeDocument()</code>. In case
     * of error, the behavior is undefined. This attribute is
     * <code>true</code> by default.
     *
     * @since DOM Level 3
     */
    public void setStrictErrorChecking(boolean strictErrorChecking) {
        ((Document) delegateNode).setStrictErrorChecking(strictErrorChecking);
    }

    /**
     * The location of the document or <code>null</code> if undefined or if
     * the <code>Document</code> was created using
     * <code>DOMImplementation.createDocument</code>. No lexical checking is
     * performed when setting this attribute; this could result in a
     * <code>null</code> value returned when using <code>Node.baseURI</code>
     * .
     * <br> Beware that when the <code>Document</code> supports the feature
     * "HTML" [<a href='http://www.w3.org/TR/2003/REC-DOM-Level-2-HTML-20030109'>DOM Level 2 HTML</a>]
     * , the href attribute of the HTML BASE element takes precedence over
     * this attribute when computing <code>Node.baseURI</code>.
     *
     * @since DOM Level 3
     */
    public String getDocumentURI() {
        return ((Document) delegateNode).getDocumentURI();
    }

    /**
     * The location of the document or <code>null</code> if undefined or if
     * the <code>Document</code> was created using
     * <code>DOMImplementation.createDocument</code>. No lexical checking is
     * performed when setting this attribute; this could result in a
     * <code>null</code> value returned when using <code>Node.baseURI</code>
     * .
     * <br> Beware that when the <code>Document</code> supports the feature
     * "HTML" [<a href='http://www.w3.org/TR/2003/REC-DOM-Level-2-HTML-20030109'>DOM Level 2 HTML</a>]
     * , the href attribute of the HTML BASE element takes precedence over
     * this attribute when computing <code>Node.baseURI</code>.
     *
     * @since DOM Level 3
     */
    public void setDocumentURI(String documentURI) {
        ((Document) delegateNode).setDocumentURI(documentURI);
    }

    /**
     * Attempts to adopt a node from another document to this document. If
     * supported, it changes the <code>ownerDocument</code> of the source
     * node, its children, as well as the attached attribute nodes if there
     * are any. If the source node has a parent it is first removed from the
     * child list of its parent. This effectively allows moving a subtree
     * from one document to another (unlike <code>importNode()</code> which
     * create a copy of the source node instead of moving it). When it
     * fails, applications should use <code>Document.importNode()</code>
     * instead. Note that if the adopted node is already part of this
     * document (i.e. the source and target document are the same), this
     * method still has the effect of removing the source node from the
     * child list of its parent, if any. The following list describes the
     * specifics for each type of node.
     * <dl>
     * <dt>ATTRIBUTE_NODE</dt>
     * <dd>The
     * <code>ownerElement</code> attribute is set to <code>null</code> and
     * the <code>specified</code> flag is set to <code>true</code> on the
     * adopted <code>Attr</code>. The descendants of the source
     * <code>Attr</code> are recursively adopted.</dd>
     * <dt>DOCUMENT_FRAGMENT_NODE</dt>
     * <dd>The
     * descendants of the source node are recursively adopted.</dd>
     * <dt>DOCUMENT_NODE</dt>
     * <dd>
     * <code>Document</code> nodes cannot be adopted.</dd>
     * <dt>DOCUMENT_TYPE_NODE</dt>
     * <dd>
     * <code>DocumentType</code> nodes cannot be adopted.</dd>
     * <dt>ELEMENT_NODE</dt>
     * <dd><em>Specified</em> attribute nodes of the source element are adopted. Default attributes
     * are discarded, though if the document being adopted into defines
     * default attributes for this element name, those are assigned. The
     * descendants of the source element are recursively adopted.</dd>
     * <dt>ENTITY_NODE</dt>
     * <dd>
     * <code>Entity</code> nodes cannot be adopted.</dd>
     * <dt>ENTITY_REFERENCE_NODE</dt>
     * <dd>Only
     * the <code>EntityReference</code> node itself is adopted, the
     * descendants are discarded, since the source and destination documents
     * might have defined the entity differently. If the document being
     * imported into provides a definition for this entity name, its value
     * is assigned.</dd>
     * <dt>NOTATION_NODE</dt>
     * <dd><code>Notation</code> nodes cannot be
     * adopted.</dd>
     * <dt>PROCESSING_INSTRUCTION_NODE, TEXT_NODE, CDATA_SECTION_NODE,
     * COMMENT_NODE</dt>
     * <dd>These nodes can all be adopted. No specifics.</dd>
     * </dl>
     * <p ><b>Note:</b>  Since it does not create new nodes unlike the
     * <code>Document.importNode()</code> method, this method does not raise
     * an <code>INVALID_CHARACTER_ERR</code> exception, and applications
     * should use the <code>Document.normalizeDocument()</code> method to
     * check if an imported name is not an XML name according to the XML
     * version in use.
     *
     * @param source The node to move into this document.
     * @return The adopted node, or <code>null</code> if this operation
     *         fails, such as when the source node comes from a different
     *         implementation.
     * @throws DOMException NOT_SUPPORTED_ERR: Raised if the source node is of type
     *                      <code>DOCUMENT</code>, <code>DOCUMENT_TYPE</code>.
     *                      <br>NO_MODIFICATION_ALLOWED_ERR: Raised when the source node is
     *                      readonly.
     * @since DOM Level 3
     */
    public Node adoptNode(Node source) throws DOMException {
        short nodeType = source.getNodeType();
        Node adoptedNode = null;

        switch (nodeType) {
            case Node.DOCUMENT_NODE:
            case Node.DOCUMENT_TYPE_NODE:
                throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Can't add this node type");
                //break;

            case Node.ATTRIBUTE_NODE:

                break;

            case Node.PROCESSING_INSTRUCTION_NODE:
            case Node.TEXT_NODE:
            case Node.CDATA_SECTION_NODE:
            case Node.COMMENT_NODE:

                break;

            case Node.DOCUMENT_FRAGMENT_NODE:

                break;

            case Node.ELEMENT_NODE:

                break;

            case Node.ENTITY_NODE:
            case Node.NOTATION_NODE:
            default:
                // do nothing, these can't be adopted but no exception is thrown
                break;
        }

        return adoptedNode;
    }

    /**
     * The configuration used when <code>Document.normalizeDocument()</code>
     * is invoked.
     *
     * @since DOM Level 3
     */
    public DOMConfiguration getDomConfig() {
        return ((Document) delegateNode).getDomConfig();
    }

    /**
     * This method acts as if the document was going through a save and load
     * cycle, putting the document in a "normal" form. As a consequence,
     * this method updates the replacement tree of
     * <code>EntityReference</code> nodes and normalizes <code>Text</code>
     * nodes, as defined in the method <code>Node.normalize()</code>.
     * <br> Otherwise, the actual result depends on the features being set on
     * the <code>Document.domConfig</code> object and governing what
     * operations actually take place. Noticeably this method could also
     * make the document namespace well-formed according to the algorithm
     * described in , check the character normalization, remove the
     * <code>CDATASection</code> nodes, etc. See
     * <code>DOMConfiguration</code> for details.
     * <pre>// Keep in the document
     * the information defined // in the XML Information Set (Java example)
     * DOMConfiguration docConfig = myDocument.getDomConfig();
     * docConfig.setParameter("infoset", Boolean.TRUE);
     * myDocument.normalizeDocument();</pre>
     * <p/>
     * <br>Mutation events, when supported, are generated to reflect the
     * changes occurring on the document.
     * <br> If errors occur during the invocation of this method, such as an
     * attempt to update a read-only node or a <code>Node.nodeName</code>
     * contains an invalid character according to the XML version in use,
     * errors or warnings (<code>DOMError.SEVERITY_ERROR</code> or
     * <code>DOMError.SEVERITY_WARNING</code>) will be reported using the
     * <code>DOMErrorHandler</code> object associated with the "error-handler
     * " parameter. Note this method might also report fatal errors (
     * <code>DOMError.SEVERITY_FATAL_ERROR</code>) if an implementation
     * cannot recover from an error.
     *
     * @since DOM Level 3
     */
    public void normalizeDocument() {
        System.out.println("*************");
        System.out.println("NOT YET IMPLEMENTED: PMRDocumentImpl.normalizeDocument ");
        System.out.println("*************");
    }

    /**
     * Rename an existing node of type <code>ELEMENT_NODE</code> or
     * <code>ATTRIBUTE_NODE</code>.
     * <br>When possible this simply changes the name of the given node,
     * otherwise this creates a new node with the specified name and
     * replaces the existing node with the new node as described below.
     * <br>If simply changing the name of the given node is not possible, the
     * following operations are performed: a new node is created, any
     * registered event listener is registered on the new node, any user
     * data attached to the old node is removed from that node, the old node
     * is removed from its parent if it has one, the children are moved to
     * the new node, if the renamed node is an <code>Element</code> its
     * attributes are moved to the new node, the new node is inserted at the
     * position the old node used to have in its parent's child nodes list
     * if it has one, the user data that was attached to the old node is
     * attached to the new node.
     * <br>When the node being renamed is an <code>Element</code> only the
     * specified attributes are moved, default attributes originated from
     * the DTD are updated according to the new element name. In addition,
     * the implementation may update default attributes from other schemas.
     * Applications should use <code>Document.normalizeDocument()</code> to
     * guarantee these attributes are up-to-date.
     * <br>When the node being renamed is an <code>Attr</code> that is
     * attached to an <code>Element</code>, the node is first removed from
     * the <code>Element</code> attributes map. Then, once renamed, either
     * by modifying the existing node or creating a new one as described
     * above, it is put back.
     * <br>In addition,
     * <ul>
     * <li> a user data event <code>NODE_RENAMED</code> is fired,
     * </li>
     * <li>
     * when the implementation supports the feature "MutationNameEvents",
     * each mutation operation involved in this method fires the appropriate
     * event, and in the end the event {
     * <code>http://www.w3.org/2001/xml-events</code>,
     * <code>DOMElementNameChanged</code>} or {
     * <code>http://www.w3.org/2001/xml-events</code>,
     * <code>DOMAttributeNameChanged</code>} is fired.
     * </li>
     * </ul>
     *
     * @param n             The node to rename.
     * @param namespaceURI  The new namespace URI.
     * @param qualifiedName The new qualified name.
     * @return The renamed node. This is either the specified node or the new
     *         node that was created to replace the specified node.
     * @throws DOMException NOT_SUPPORTED_ERR: Raised when the type of the specified node is
     *                      neither <code>ELEMENT_NODE</code> nor <code>ATTRIBUTE_NODE</code>,
     *                      or if the implementation does not support the renaming of the
     *                      document element.
     *                      <br>INVALID_CHARACTER_ERR: Raised if the new qualified name is not an
     *                      XML name according to the XML version in use specified in the
     *                      <code>Document.xmlVersion</code> attribute.
     *                      <br>WRONG_DOCUMENT_ERR: Raised when the specified node was created
     *                      from a different document than this document.
     *                      <br>NAMESPACE_ERR: Raised if the <code>qualifiedName</code> is a
     *                      malformed qualified name, if the <code>qualifiedName</code> has a
     *                      prefix and the <code>namespaceURI</code> is <code>null</code>, or
     *                      if the <code>qualifiedName</code> has a prefix that is "xml" and
     *                      the <code>namespaceURI</code> is different from "<a href='http://www.w3.org/XML/1998/namespace'>
     *                      http://www.w3.org/XML/1998/namespace</a>" [<a href='http://www.w3.org/TR/1999/REC-xml-names-19990114/'>XML Namespaces</a>]
     *                      . Also raised, when the node being renamed is an attribute, if the
     *                      <code>qualifiedName</code>, or its prefix, is "xmlns" and the
     *                      <code>namespaceURI</code> is different from "<a href='http://www.w3.org/2000/xmlns/'>http://www.w3.org/2000/xmlns/</a>".
     * @since DOM Level 3
     */
    public Node renameNode(Node n, String namespaceURI, String qualifiedName) throws DOMException {
        Node nDelegateNode = ((PMRNodeImpl) n).getDelegateNode();
        Node newNDelegateNode = ((Document) delegateNode).renameNode(nDelegateNode, namespaceURI, qualifiedName);
        if (newNDelegateNode != nDelegateNode) {
            ((PMRNodeImpl) n).delegateNode = newNDelegateNode;
        }
        return n;
    }

    /**
     * very crude
     */
    public Element getElementById(String id) {
        Element theElem = null;
        Element elem = ((Document) delegateNode).getElementById(id);
        if (elem != null) {
            ArrayList<Node> nodes = this.getAllElementDescendants();
            int l = nodes.size();
            for (int i = 0; i < l; i++) {
                PMRElementImpl el = (PMRElementImpl) nodes.get(i);
                if (el.delegateNode == elem) {
                    theElem = el;
                    break;
                }
            }
        }
        return theElem;
    }

    public String toString() {
        String s = "PMRDocument " + this.hashCode();
        s += "[delegateNode: " + delegateNode.hashCode() + "]";
        s += "[pmrDocument: " + pmrDocument.hashCode() + "]";
        return s;
    }

    public static void test(String inFile) throws Exception {
        PMRDocumentBuilderFactory pmrDocBuilderFactory = (PMRDocumentBuilderFactory) PMRDocumentBuilderFactory.newInstance();
        PMRDocumentBuilder pmrDocBuilder = null;
        try {
            pmrDocBuilder = (PMRDocumentBuilder) pmrDocBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException pce) {
        }
        if (pmrDocBuilder != null) {
            pmrDocBuilder.parse(new File(inFile));
            StringWriter w = new StringWriter();
            //PMRDOMUtils.outputEventStream(pmrDoc, w, PMRDOMUtils.PRETTY, 0);
            System.out.println(w.toString());
        }
    }

    public static void test() throws Exception {
        PMRDocumentBuilderFactory pmrDocBuilderFactory = (PMRDocumentBuilderFactory) PMRDocumentBuilderFactory.newInstance();
        PMRDocumentBuilder pmrDocBuilder = null;
        try {
            pmrDocBuilder = (PMRDocumentBuilder) pmrDocBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException pce) {
        }
        if (pmrDocBuilder != null) {
            PMRDocument pmrDoc = (PMRDocument) pmrDocBuilder.newDocument();
        Element a = pmrDoc.createElement("aElem");
        pmrDoc.appendChild(a);
        Element b = pmrDoc.createElement("b");
        a.appendChild(b);
        Element c = pmrDoc.createElement("c");
        c.setAttribute("C1", "c1value");
        c.setAttribute("C2", "c2value");
        a.appendChild(c);
        Element d = pmrDoc.createElement("d");
        b.appendChild(d);
        d.setAttribute("Z", "zvalue");
        d.setAttribute("A", "avalue");
        Text text = pmrDoc.createTextNode("this is some text");
        b.appendChild(text);
        text = pmrDoc.createTextNode("some more text");
        d.appendChild(text);
        Comment comm = pmrDoc.createComment("this is a comment");
        b.appendChild(comm);
        ProcessingInstruction pi = pmrDoc.createProcessingInstruction("target", "rest of PI");
        b.appendChild(pi);
        Element a1 = pmrDoc.createElement("a1");
        a.insertBefore(a1, c);
        NodeList nl = a.getElementsByTagName("c");
        for (int i = 0; i < nl.getLength(); i++) {
            Element cc = (Element) nl.item(i);
            System.out.println("CC " + cc);
            NamedNodeMap nnm = cc.getAttributes();
            int ll = nnm.getLength();
            System.out.println("LEN " + ll);
            for (int J = 0; J < ll; J++) {
                Attr att = (Attr) nnm.item(J);
                System.out.println("Att " + att);
                System.out.println("Att " + att.getNodeName() + "=" + att.getNodeValue());
            }
        }
        }
        System.out.println("=================================");
        StringWriter w = new StringWriter();
        //PMRDOMUtils.outputEventStream(pmrDoc.getDocumentElement(), w, PMRDOMUtils.PRETTY, 0);
        System.out.println(w.toString());
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java uk.co.ursus.demon.pmr.PMRDocumentImpl -TEST -IN file");
            System.exit(0);
        }
        try {
            String inFile = null;
            boolean test = false;
            int i = 0;
            while (i < args.length) {
                String arg = args[i++];
                if (arg.equalsIgnoreCase("-TEST")) {
                    test = true;
                } else if (arg.equalsIgnoreCase("-IN")) {
                    inFile = args[i++];
                } else {
                    System.out.println("Unknown arg: " + arg);
                }
            }
            if (test) test();
            if (inFile != null) test(inFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //===========================================

    void createAndAddChildren(PMRNodeImpl pmrNode, Node node, int level) {
        NodeList childs = node.getChildNodes();
        if (childs != null) {
            int l = childs.getLength();
            pmrNode.childNodes = new ArrayList<Node>();
            for (int i = 0; i < l; i++) {
                Node child = childs.item(i);
                PMRNodeImpl newPMRNode = this.createAndAddPMRNode(pmrNode, child, level);
                if (newPMRNode != null) {
                    pmrNode.childNodes.add(newPMRNode);
                    newPMRNode.parentNode = pmrNode;
                }
            }
        }
    }

    PMRNodeImpl createAndAddPMRNode(PMRNodeImpl parent, Node child, int level) {
        PMRNodeImpl newNode = null;
        if (child == null) {
            System.err.println("Null child");
        } else if (child instanceof Element) {
            newNode = new PMRElementImpl((Element) child, this);
        } else if (child instanceof Text) {
            newNode = new PMRTextImpl((Text) child, this);
        } else if (child instanceof Comment) {
            newNode = new PMRCommentImpl((Comment) child, this);
        } else if (child instanceof ProcessingInstruction) {
            newNode = new PMRProcessingInstructionImpl((ProcessingInstruction) child, this);
        } else if (child instanceof Document) {
            System.err.println("Can't create Document");
        } else {
            System.err.println("(Perhaps NYI) Can't create " + child.getNodeName());
        }
        if (newNode != null) {
            createAndAddChildren(newNode, newNode.delegateNode, level + 1);
        }
        return newNode;
    }

}

