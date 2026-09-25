/*
 * $Id: OMDOMReader.java,v 1.2 2007-10-21 17:46:58 virtualcall Exp $
 *
 * Copyright (c) 2001-2004, RIACA, Technische Universiteit Eindhoven (TU/e).
 * All Rights Reserved.
 *
 * ---------------------------------------------------------------------------
 *
 *  The contents of this file are subject to the RIACA Public License
 *  Version 1.0 (the "License"). You may not use this file except in
 *  compliance with the License. A copy of the License is available at
 *  http://www.riaca.win.tue.nl/
 *
 *  Alternatively, the contents of this file may be used under the terms
 *  of the GNU Lesser General Public license (the "LGPL license"), in which 
 *  case the provisions of the LGPL license are applicable instead of those 
 *  above. A copy of the LGPL license is available at http://www.gnu.org/
 *
 *  The Original Code is ROML -- the RIACA OpenMath Library. The Initial
 *  Developer of the Original Code is Manfred N. Riem.  Portions created
 *  by Manfred N. Riem are Copyright (c) 2001. All Rights Reserved.
 *
 *  Contributor(s):
 *
 *      Ernesto Reinaldo Barreiro, Arjeh M. Cohen, Hans Cuypers, Hans Sterk,
 *      Olga Caprotti
 *
 * ---------------------------------------------------------------------------
 */
package org.episteme.core.mathematics.loaders.openmath.io;

import org.episteme.core.mathematics.loaders.openmath.*;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.StringReader;

/**
 * An OpenMath DOM reader. <p>
 *
 * @author Manfred N. Riem (mriem@manorrock.org)
 * @version $Revision: 1.2 $
 */
public class OMDOMReader {
    /**
     * Stores a static array with all the OpenMath element-names in use. <p>
     */
    protected static String[] sOMObjects = {
            "OMOBJ", "OMA", "OMATTR", "OMB", "OMBIND", "OME", "OMF", "OMI", "OMS",
            "OMSTR", "OMV", "OMBVAR", "OMATP", "OMR", "OMFOREIGN"
    };

    /**
     * Stores the input-source if we use one. <p>
     */
    protected InputSource mInputSource;

    /**
     * Stores the node we want to read the OMObject from. <p>
     */
    protected Node mNode;

    /**
     * Constructor. <p>
     *
     * @param document the document to read using the reader.
     */
    public OMDOMReader(Document document) {
        mNode = (Node) document;
    }

    /**
     * Constructor. <p>
     *
     * @param fragment the document fragment to read using the reader.
     */
    public OMDOMReader(DocumentFragment fragment) {
        mNode = (Node) fragment;
    }

    /**
     * Constructor. <p>
     *
     * @param node the node to read using the reader.
     */
    public OMDOMReader(Node node) {
        mNode = node;
    }

    /**
     * Constructor. <p>
     *
     * @param inputSource the InputSource to read from using this reader.
     */
    public OMDOMReader(InputSource inputSource) {
        mInputSource = inputSource;
    }

    /**
     * Constructor. <p>
     *
     * @param string the string to read from using this reader.
     */
    public OMDOMReader(String string) {
        mInputSource = new InputSource(new StringReader(string));
    }

    /**
     * Read an OpenMath object. <p>
     * <p/>
     * <p/>
     * 1. If we are a document then get the nodelist that contains an
     * OMOBJ. Take the first of the list and read that object.
     * <p/>
     * 2. Or if we are a document fragment assume the first child is
     * the OMObject.
     * <p/>
     * 3. Or just assume the node contains the OMObject.
     * </p>
     *
     * @return the OpenMath object.
     * @throws IOException when a problem arises while reading.
     */
    public OMObject readObject() throws IOException {
        if (mInputSource != null) {
            try {
                DocumentBuilderFactory factory =
                        org.episteme.core.io.SecureXMLFactory.createSecureNamespaceAwareDocumentBuilderFactory();
                factory.setIgnoringElementContentWhitespace(true);
                factory.setValidating(false);

                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(mInputSource);

                mNode = (Node) document;
            } catch (Exception exception) {
                throw new IOException("Unable to construct DOM: " +
                        exception.getMessage());
            }
        }

        if (mNode instanceof Document) {
            Document tDocument = (Document) mNode;
            NodeList tNodeList = tDocument.getElementsByTagName("OMOBJ");

            if (tNodeList.getLength() >= 1) {
                return readNode(tNodeList.item(0));
            } else {
                return readNode(tDocument.getFirstChild());
            }
        } else if (mNode instanceof DocumentFragment) {
            DocumentFragment tFragment = (DocumentFragment) mNode;

            if (tFragment.getFirstChild() != null) {
                return readNode(tFragment.getFirstChild());
            }
        } else {
            return readNode(mNode);
        }

        throw new IOException("Unable to read OMObject from DOM");
    }

    /**
     * Utility method to get the index from
     * the element-array. <p>
     *
     * @param name the name to check for.
     * @return -1 when element was not found, the index if it was found.
     */
    private int getElementIndex(String name) {
        for (int i = 0; i < sOMObjects.length; i++) {
            if (sOMObjects[i].equals(name)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Read a DOM Node. <p>
     * <p/>
     * <p/>
     * Make sure we have a normalized Node. See spec for more details.
     * </p>
     *
     * @param fNode the node to read from
     * @return the OpenMath object read.
     * @throws IOException when a problem arises while reading.
     */
    private OMObject readNode(Node fNode) throws IOException {
        fNode.normalize();

        OMObject tObject = null;

        switch (getElementIndex(fNode.getNodeName())) {
            case 0: /* OMOBJ */ {
                Node tChild = fNode.getFirstChild();

                while (tChild != null) {
                    if ((getElementIndex(tChild.getNodeName()) >= 1)
                            && (getElementIndex(tChild.getNodeName()) <= 14)) {
                        return readNode(tChild);
                    }

                    tChild = tChild.getNextSibling();
                }
            }

            case 1: /* OMA  */ {
                OMApplication tApplication = new OMApplication();
                Node tChild = fNode.getFirstChild();

                while (tChild != null) {
                    if ((getElementIndex(tChild.getNodeName()) >= 1)
                            && (getElementIndex(tChild.getNodeName()) <= 10)) {
                        OMObject tElement = readNode(tChild);
                        tApplication.addElement(tElement);
                    }

                    tChild = tChild.getNextSibling();
                }
                tObject = tApplication;
                break;
            }

            case 2: /* OMATTR */ {
                OMAttribution tAttribution = new OMAttribution();
                Node tChild = fNode.getFirstChild();

                while (getElementIndex(tChild.getNodeName()) != 12) {
                    tChild = tChild.getNextSibling();
                }

                Node tChild2 = tChild.getFirstChild();
                while (tChild2 != null) {
                    OMObject tKey = null;
                    OMObject tValue = null;

                    while ((getElementIndex(tChild2.getNodeName()) < 0)
                            || (getElementIndex(tChild2.getNodeName()) > 10)) {
                        tChild2 = tChild2.getNextSibling();

                        if (tChild2 == null) {
                            break;
                        }
                    }

                    if (tChild2 == null) {
                        break;
                    }

                    tKey = readNode(tChild2);

                    while ((getElementIndex(tChild2.getNodeName()) < 0)
                            || (getElementIndex(tChild2.getNodeName()) > 10)) {
                        tChild2 = tChild2.getNextSibling();

                        if (tChild2 == null) {
                            break;
                        }
                    }

                    if (tChild2 == null) {
                        break;
                    }

                    tValue = readNode(tChild2);
                    tAttribution.put(tKey, tValue);

                    tChild2 = tChild2.getNextSibling();
                }

                tChild = fNode.getFirstChild();

                while (getElementIndex(tChild.getNodeName()) != 12) {
                    tChild = tChild.getNextSibling();
                }

                OMObject tConstructor = null;

                while (tChild != null) {
                    if ((getElementIndex(tChild.getNodeName()) >= 1)
                            && (getElementIndex(tChild.getNodeName()) <= 10)) {
                        tConstructor = readNode(tChild);

                        break;
                    }

                    tChild = tChild.getNextSibling();
                }

                tAttribution.setConstructor(tConstructor);
                tObject = tAttribution;

                break;
            }

            case 3: /* OMB    */ {
                OMByteArray tByteArray = new OMByteArray();
                Node tChild = fNode.getFirstChild();

                tByteArray.setByteArray(tChild.getNodeValue());
                tObject = tByteArray;

                break;
            }

            case 4: /* OMBIND */ {
                OMBinding tBinding = new OMBinding();
                Node tChild = fNode.getFirstChild();

                while (tChild != null) {
                    if ((getElementIndex(tChild.getNodeName()) >= 1)
                            && (getElementIndex(tChild.getNodeName()) <= 10)) {
                        tBinding.setBinder(readNode(tChild));

                        break;
                    }

                    tChild = tChild.getNextSibling();
                }

                if (tChild != null) {
                    while (getElementIndex(tChild.getNodeName()) != 11) {
                        tChild = tChild.getNextSibling();
                        if (tChild == null) break;
                    }
                }

                Node tChild2 = (tChild != null) ? tChild.getFirstChild() : null;

                while (tChild2 != null) {
                    OMObject tVariable = null;

                    while ((getElementIndex(tChild2.getNodeName()) < 0)
                            || (getElementIndex(tChild2.getNodeName()) > 10)) {
                        tChild2 = tChild2.getNextSibling();

                        if (tChild2 == null) {
                            break;
                        }
                    }

                    if (tChild2 == null) {
                        break;
                    }

                    tVariable = readNode(tChild2);
                    tBinding.addVariable(tVariable);

                    tChild2 = tChild2.getNextSibling();
                }

                tChild = fNode.getFirstChild();

                while (getElementIndex(tChild.getNodeName()) != 11) {
                    tChild = tChild.getNextSibling();
                }

                while (tChild != null) {
                    if ((getElementIndex(tChild.getNodeName()) >= 1)
                            && (getElementIndex(tChild.getNodeName()) <= 10)) {
                        tBinding.setBody(readNode(tChild));
                        break;
                    }
                    tChild = tChild.getNextSibling();
                }

                tObject = tBinding;
                break;
            }

            case 5: /* OME    */ {
                OMError tError = new OMError();
                Node tChild = fNode.getFirstChild();

                while (tChild != null) {
                    if ((getElementIndex(tChild.getNodeName()) >= 1)
                            && (getElementIndex(tChild.getNodeName()) <= 10)) {
                        OMObject tElement = readNode(tChild);
                        tError.setSymbol((OMSymbol) tElement);

                        break;
                    }

                    tChild = tChild.getNextSibling();
                }

                if (tChild != null) {
                   tChild = tChild.getNextSibling();
                }

                while (tChild != null) {
                    if ((getElementIndex(tChild.getNodeName()) >= 1)
                            && (getElementIndex(tChild.getNodeName()) <= 10)) {
                        OMObject tElement = readNode(tChild);
                        tError.addElement(tElement);
                    }

                    tChild = tChild.getNextSibling();
                }

                tObject = tError;

                break;
            }

            case 6: /* OMF    */ {
                tObject = new OMFloat();

                break;
            }

            case 7: /* OMI    */ {
                OMInteger tInteger = new OMInteger();
                Node tChild = fNode.getFirstChild();

                tInteger.setInteger(tChild.getNodeValue());
                tObject = tInteger;

                break;
            }

            case 8: /* OMS    */ {
                tObject = new OMSymbol();

                break;
            }

            case 9: /* OMSTR  */ {
                OMString tString = new OMString();
                Node tChild = fNode.getFirstChild();

                tString.setString(tChild.getNodeValue());
                tObject = tString;

                break;
            }

            case 10: /* OMV    */ {
                tObject = new OMVariable();

                break;
            }

            case 13: /* OMR */ {
                tObject = new OMReference();
                break;
            }

            case 14: /* OMFOREIGN */ {
                tObject = new OMForeign();
                break;
            }

            default:
                throw new IOException("Unable to read OMObject from DOM: "
                        + fNode.getNodeName());
        }

        NamedNodeMap tAttributes = fNode.getAttributes();

        for (int i = 0; i < tAttributes.getLength(); i++) {
            Node tNode = tAttributes.item(i);

            if (tNode.getPrefix() == null) {
                tObject.setAttribute(tNode.getNodeName(),
                        tNode.getNodeValue());
            } else {
                tObject.setAttribute(tNode.getPrefix() + ":"
                        + tNode.getNodeName(), tNode.getNodeValue());
            }
        }

        return tObject;
    }
}

