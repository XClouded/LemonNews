
package com.GreenLemonMobile.feed4j;

import com.GreenLemonMobile.feed4j.bean.Feed;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * The feed parser. It can parse RSS 1.0, RSS 2.0, Atom 0.3 and Atom 1.0.
 * 
 * @author Carlo Pelliccia
 */
public class FeedParser {

    /**
     * Gets the feed from an URL and parses it.
     * 
     * @param url The feed URL.
     * @param is The feed content input stream
     * @return A Feed object containing the information extracted from the feed.
     * @throws FeedIOException I/O error during conetnts retrieving.
     * @throws FeedXMLParseException The document retrieved is not valid XML.
     * @throws UnsupportedFeedException The XML retrieved does not represents a
     *             feed whose kind is known by the parser.
     */
    public static Feed parse(URL url, InputStream is) throws FeedIOException,
            FeedXMLParseException, UnsupportedFeedException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            // Esegue il parsing iniziale del documento XML.
            DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            Document document = documentBuilder.parse(is);
            // Cerca il modulo di interpretazione del feed.
            int code = FeedRecognizer.recognizeFeed(document);
            switch (code) {
                case FeedRecognizer.RSS_1_0:
                    return TypeRSS_1_0.feed(url, document);
                case FeedRecognizer.RSS_2_0:
                    return TypeRSS_2_0.feed(url, document);
                case FeedRecognizer.ATOM_0_3:
                    return TypeAtom_0_3.feed(url, document);
                case FeedRecognizer.ATOM_1_0:
                    return TypeAtom_1_0.feed(url, document);
                default:
                    throw new UnsupportedFeedException();
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            throw new FeedXMLParseException(e);
        } catch (SAXException e) {
            e.printStackTrace();
            throw new FeedXMLParseException(e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new FeedIOException(e);
        } /*
           * catch (Exception e) { e.printStackTrace(); throw new
           * UnsupportedFeedException(); }
           */
    }
    
    private static String checkXmlTag(String xmlContent) {        
        String xmlEncodeTag = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
        
        if (xmlContent.startsWith("<?xml") || (xmlContent.length() >= 10 && xmlContent.substring(0, 10).contains("<?xml"))) {
            if (!(xmlContent.startsWith(xmlEncodeTag) || xmlContent.startsWith(xmlEncodeTag.toLowerCase()))) {
                xmlContent = xmlContent.substring(xmlContent.indexOf("?>") + 2);
                xmlContent = xmlEncodeTag + xmlContent;
            }
        } else {
            xmlContent = xmlEncodeTag + xmlContent;
        }
        return xmlContent;
    }

    public static Feed parse(URL url, String feedContent) throws FeedIOException,
            FeedXMLParseException, UnsupportedFeedException {
        InputStream stream = null;
        try {
            String encodeType = "UTF-8";
            feedContent = checkXmlTag(feedContent);
            stream = new ByteArrayInputStream(feedContent.getBytes(encodeType));
            return parse(url, stream);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stream != null)
                    stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static Feed parse(URL url) throws FeedIOException,
            FeedXMLParseException, UnsupportedFeedException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            // Esegue il parsing iniziale del documento XML.
            DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            Document document = documentBuilder.parse(url.toString());
            // Cerca il modulo di interpretazione del feed.
            int code = FeedRecognizer.recognizeFeed(document);
            switch (code) {
                case FeedRecognizer.RSS_1_0:
                    return TypeRSS_1_0.feed(url, document);
                case FeedRecognizer.RSS_2_0:
                    return TypeRSS_2_0.feed(url, document);
                case FeedRecognizer.ATOM_0_3:
                    return TypeAtom_0_3.feed(url, document);
                case FeedRecognizer.ATOM_1_0:
                    return TypeAtom_1_0.feed(url, document);
                default:
                    throw new UnsupportedFeedException();
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            throw new FeedXMLParseException(e);
        } catch (SAXException e) {
            e.printStackTrace();
            throw new FeedXMLParseException(e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new FeedIOException(e);
        }
    }
}
