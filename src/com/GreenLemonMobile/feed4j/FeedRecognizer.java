package com.GreenLemonMobile.feed4j;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * A feed recognizer. It can recognize RSS 1.0, RSS 2.0, Atom 0.3 and Atom 1.0.
 * 
 * @author Carlo Pelliccia
 */
class FeedRecognizer extends TypeAbstract {

	/**
	 * Unknown feed type.
	 */
	public static final int UNKNOWN = -1;

	/**
	 * RSS 1.0.
	 */
	public static final int RSS_1_0 = 0;

	/**
	 * RSS 2.0.
	 */
	public static final int RSS_2_0 = 1;

	/**
	 * Atom 0.3.
	 */
	public static final int ATOM_0_3 = 2;

	/**
	 * Atom 1.0.
	 */
	public static final int ATOM_1_0 = 3;

	/**
	 * It analyzes a XML document representation and return a costant suggesting
	 * the type of the feed in the document.
	 * 
	 * @param document
	 *            The XML document representation.
	 * @return UNKNOWN, RSS_1_0, RSS_2_0, ATOM_0_3 or ATOM_1_0, depending on the
	 *         type recognized.
	 */
	public static int recognizeFeed(Document document) {
		Element root = document.getDocumentElement();
		if (root == null) {
			return UNKNOWN;
		}
		String nsuri = root.getNamespaceURI();
		String name = root.getNodeName();
		if ("rss".equals(name)) {
			String version = root.getAttribute("version");
			if (version == null || version.equals("2.0")
					|| version.equals("0.91") || version.equals("0.92")) {
				return RSS_2_0;
			}
		} else if ("RDF".equals(name) && (nsuri == null || Constants.RDF_NS_URI.equals(nsuri))) {
			for (int index = 0; index < root.getChildNodes().getLength(); ++index) {
				Element element = (Element) root.getChildNodes().item(index);
				String elNsUri = element.getNamespaceURI();
				if (elNsUri == null ||Constants.RSS_1_0_NS_URI.equals(elNsUri)) {
					return RSS_1_0;
				}
			}
		} else if (name.equals("feed")) {
			String version = root.getAttribute("version");
			if (version == null || version.equals("1.0")
					|| Constants.ATOM_NS_URI.equals(nsuri)) {
				return ATOM_1_0;
			} else if (version.equals("0.3")
					|| Constants.ATOM_0_3_NS_URI.equals(nsuri)) {
				return ATOM_0_3;
			}
		}
		return UNKNOWN;
	}

}
