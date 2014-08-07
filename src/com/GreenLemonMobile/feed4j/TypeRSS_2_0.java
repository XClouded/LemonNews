package com.GreenLemonMobile.feed4j;

import com.GreenLemonMobile.feed4j.bean.Feed;
import com.GreenLemonMobile.feed4j.bean.FeedEnclosure;
import com.GreenLemonMobile.feed4j.bean.FeedHeader;
import com.GreenLemonMobile.feed4j.bean.FeedImage;
import com.GreenLemonMobile.feed4j.bean.FeedItem;
import com.GreenLemonMobile.feed4j.bean.RawElement;
import com.GreenLemonMobile.feed4j.bean.RawNode;
import com.GreenLemonMobile.feed4j.bean.RawText;
import com.GreenLemonMobile.feed4j.html.HTMLFragmentHelper;
import com.GreenLemonMobile.feed4j.html.HTMLOptimizer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;

/**
 * RSS 2.0 feed parser.
 * 
 * @author Carlo Pelliccia
 */
class TypeRSS_2_0 extends TypeAbstract {

	private static final String NS_DC = "http://purl.org/dc/elements/1.1/";
	
	/**
	 * This method parses a dom4j Document representation assuming it is RSS 2.0
	 * feed.
	 * 
	 * @param source
	 *            The source URL for the feed.
	 * @param document
	 *            The dom4j Document representation of the XML representing the
	 *            feed.
	 * @return The Feed object representing the feed parsed contents.
	 */
	public static Feed feed(URL source, Document document) {
		// Root element.
		Element root = document.getDocumentElement();
		// Root element namespace URI.
		String nsuri = root.getNamespaceURI();
		// The return value.
		Feed feed = new Feed();
		// Start from the header.
		FeedHeader header = new FeedHeader();
		header.setURL(source);
		// Search for the "channel" element.
		Element channel = (Element) document.getElementsByTagName("channel").item(0);
		if (channel != null) {
			// Header raw-population from "channel" element.
			populateRawElement(header, channel);
			// Search between the raw elements and build non-raw data.
			for (int i = 0; i < header.getNodeCount(); i++) {
				RawNode node = header.getNode(i);
				if (node instanceof RawElement) {
					RawElement element = (RawElement) node;
					String ensuri = element.getNamespaceURI();
					String ename = element.getName();
					String evalue = element.getValue();
					if (evalue != null) {
						// Textual element.
						if ((ensuri == null || nsuri == null) || ensuri.equals(nsuri)) {
							if (ename.equals("title")) {
								header.setTitle(evalue);
							} else if (ename.equals("description")) {
								header.setDescription(evalue);
							} else if (ename.equals("link")) {
								try {
									header.setLink(new URL(evalue));
								} catch (MalformedURLException e) {
									;
								}
							} else if (ename.equals("pubDate")) {
								try {
									header
											.setPubDate(Constants.RFC_822_DATE_FORMAT
													.parse(evalue));
								} catch (ParseException e) {
									;
								}
							} else if (ename.equals("language")) {
								if (isValidLanguageCode(evalue)) {
									header.setLanguage(evalue);
								}
							}
						}
					} else {
                        if (ename.equals("item")) {
                            // FeedItem!
                            FeedItem item = handleItem(source, element);
                            if (item != null) {
                                feed.addItem(item);
                            }
                        } else if (ename.equals("image")) {
                            // Channel image.
                            header.setImage(handleImage(element));
                        }
					}
				}
			}
		}
		// Remove from the header every raw "item" element.
		RawElement[] rawitems = header.getElements(nsuri, "item");
		for (int i = 0; i < rawitems.length; i++) {
			header.removeNode(rawitems[i]);
		}
		// Remove from the header every raw "image" element.
		RawElement[] rawimages = header.getElements(nsuri, "image");
		for (int i = 0; i < rawimages.length; i++) {
			header.removeNode(rawimages[i]);
		}
		// Link the header.
		feed.setHeader(header);
		// Well done!
		return feed;
	}

	/**
	 * Item parser.
	 */
	private static FeedItem handleItem(URL source, RawElement rawItem) {
		// Namespace URI.
		String nsuri = rawItem.getNamespaceURI();
		// Build teh return value.
		FeedItem item = new FeedItem();
		// Raw population.
		populateRawElement(item, rawItem);
		// Non-raw population.
		for (int i = 0; i < item.getNodeCount(); i++) {
			RawNode node = item.getNode(i);
			if (node instanceof RawElement) {
				RawElement element = (RawElement) node;
				String ensuri = element.getNamespaceURI();
				String ename = element.getName();
				String evalue = element.getValue();
				if (evalue == null && element.getNodeCount() > 0) {
				    evalue = "";
				    for (int j = 0; j < element.getNodeCount(); ++j) {
				        evalue += ((RawText)element.getNode(j)).getText();
				    }
				}
				if (evalue != null) {
					// Textual element.
					if ((nsuri == null || ensuri == null) || ensuri.equals(nsuri)) {
						// In RSS namespace.
						if (ename.equals("title")) {
							item.setTitle(evalue);
						} else if (ename.equals("link")) {
							try {
								item.setLink(new URL(evalue));
							} catch (MalformedURLException e) {
								;
							}
						} else if (ename.equals("description")) {
							evalue = HTMLOptimizer.optimize(evalue);
							if (evalue.length() > 0) {
								item.setDescriptionAsHTML(evalue);
								item.setDescriptionAsText(HTMLFragmentHelper
										.fromHTMLtoTextPlain(evalue));
							}
						} else if (ename.equals("comments")) {
							try {
								item.setComments(new URL(evalue));
							} catch (MalformedURLException e) {
								;
							}
						} else if (ename.equals("guid")) {
							String isPermaLink = element.getAttributeValue(
									ensuri, "isPermaLink");
							if (isPermaLink != null
									&& isPermaLink.equals("true")) {
								try {
									item.setLink(new URL(evalue));
								} catch (MalformedURLException e) {
									;
								}
							}
							item.setGUID(evalue);
						} else if (ename.equals("author")) {
							item.setAuthor(evalue);
						} else if (ename.equals("pubDate")) {
							try {
								item.setPubDate(Constants.RFC_822_DATE_FORMAT
										.parse(evalue));
							} catch (ParseException e) {
								;
							}
						}
					} else if (ensuri.equals(NS_DC)) {
						if (ename.equals("creator")) {
							item.setCreator(evalue);
						}
					}
				} else {
					if (ename.equals("enclosure") && (ensuri != null && ensuri.equals(nsuri))) {
						FeedEnclosure enclosure = handleEnclosure(element);
						if (enclosure != null) {
							item.addEnclosure(enclosure);
						}
					}
				}
			}
		}
		// Valid?
		if (item.getTitle() == null || item.getLink() == null) {
			// No, return null.
			return null;
		}
		// A GUID for the item.
		String rssGuid = item.getGUID();
		if (rssGuid == null) {
			rssGuid = item.getLink().toExternalForm();
		}
		item.setGUID(buildGUID(source.hashCode(), rssGuid.hashCode()));
		// Remove every "enclosure" element from the raw ones, since they have
		// been handled.
		RawElement[] enclosures = item.getElements(nsuri, "enclosure");
		for (int i = 0; i < enclosures.length; i++) {
			item.removeNode(enclosures[i]);
		}
		// Well done.
		return item;
	}

	/**
	 * Attachments handler.
	 */
	private static FeedEnclosure handleEnclosure(RawElement rawEnclosure) {
		// Namespace URI.
		String nsuri = rawEnclosure.getNamespaceURI();
		// Build the object.
		FeedEnclosure enclosure = new FeedEnclosure();
		// Raw population.
		populateRawElement(enclosure, rawEnclosure);
		// Non-raw population, starting from the URL.
		String value = enclosure.getAttributeValue(nsuri, "url");
		if (value != null) {
			try {
				enclosure.setURL(new URL(value));
			} catch (MalformedURLException e) {
				;
			}
		}
		// MIME type.
		value = enclosure.getAttributeValue(nsuri, "type");
		if (value != null) {
			enclosure.setMimeType(value);
		}
		// File size.
		value = enclosure.getAttributeValue(nsuri, "length");
		if (value != null) {
			long length = -1;
			try {
				length = Long.parseLong(value);
			} catch (NumberFormatException e) {
				;
			}
			if (length > 0) {
				enclosure.setLength(length);
			}
		}
		// Solid?
		if (enclosure.getURL() == null || enclosure.getMimeType() == null) {
			return null;
		}
		// Well done!
		return enclosure;
	}

	/**
	 * Channel image handler.
	 */
	private static FeedImage handleImage(RawElement rawImage) {
		// Namespace URI.
		String nsuri = rawImage.getNamespaceURI();
		// Build the object.
		FeedImage image = new FeedImage();
		// Raw population.
		populateRawElement(image, rawImage);
		// Non-raw population.
		String value = image.getElementValue(nsuri, "url");
		if (value != null) {
			try {
				image.setURL(new URL(value));
			} catch (MalformedURLException e) {
				;
			}
		}
		value = image.getElementValue(nsuri, "description");
		if (value != null) {
			image.setDescription(value);
		}
		value = image.getElementValue(nsuri, "width");
		if (value != null) {
			int intvalue = 0;
			try {
				intvalue = Integer.parseInt(value);
			} catch (NumberFormatException e) {
				;
			}
			if (intvalue > 0) {
				image.setWidth(intvalue);
			}
		}
		value = image.getElementValue(nsuri, "height");
		if (value != null) {
			int intvalue = 0;
			try {
				intvalue = Integer.parseInt(value);
			} catch (NumberFormatException e) {
				;
			}
			if (intvalue > 0) {
				image.setHeight(intvalue);
			}
		}
		// Solid?
		if (image.getURL() == null) {
			return null;
		}
		// Well done!
		return image;
	}

}
