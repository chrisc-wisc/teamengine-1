/****************************************************************************

 The contents of this file are subject to the Mozilla Public License
 Version 1.1 (the "License"); you may not use this file except in
 compliance with the License. You may obtain a copy of the License at
 http://www.mozilla.org/MPL/ 

 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 the specific language governing rights and limitations under the License. 

 The Original Code is TEAM Engine.

 The Initial Developer of the Original Code is Northrop Grumman Corporation
 jointly with The National Technology Alliance.  Portions created by
 Northrop Grumman Corporation are Copyright (C) 2005-2006, Northrop
 Grumman Corporation. All Rights Reserved.

 Contributor(s): No additional contributors to date

 ****************************************************************************/
package com.occamlab.te.parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.occamlab.te.TECore;


/**
 * Parses an HTTP response message and produces a DOM Document representation of
 * the message content.
 * 
 */
public class HTTPParser {
    public static final String PARSERS_NS = "http://www.occamlab.com/te/parsers";
    public static final String EOS_ERR = "Error in multipart stream.  End of stream reached and with no closing boundary delimiter line";

    private static void append_headers(URLConnection uc, Element e) {
        Document doc = e.getOwnerDocument();
        Element headers = doc.createElement("headers");
        e.appendChild(headers);

        for (int i = 0;; i++) {
            String headerKey = uc.getHeaderFieldKey(i);
            String headerValue = uc.getHeaderField(i);
            if (headerKey == null) {
                if (headerValue == null)
                    break;
            } else {
                Element header = doc.createElement("header");
                headers.appendChild(header);
                header.setAttribute("name", headerKey);
                header.appendChild(doc.createTextNode(headerValue));
            }
        }
    }

    private static Node select_parser(int partnum, String mime, Element instruction) {
        NodeList instructions = instruction.getElementsByTagNameNS(PARSERS_NS, "parse");
        for (int i = 0; i < instructions.getLength(); i++) {
            Element parse = (Element) instructions.item(i);
            if (partnum != 0) {
                String part_i = parse.getAttribute("part");
                if (part_i.length() > 0) {
                    int n = Integer.parseInt(part_i);
                    if (n != partnum) {
                        continue;
                    }
                }
            }
            if (mime != null) {
                String mime_i = parse.getAttribute("mime");
                if (mime_i.length() > 0) {
                    String[] mime_parts = mime_i.split(";\\s*");
                    if (!mime.startsWith(mime_parts[0])) {
                        continue;
                    }
                    boolean ok = true;
                    for (int j = 1; j < mime_parts.length; j++) {
                        if (mime.indexOf(mime_parts[j]) < 0) {
                            ok = false;
                            break;
                        }
                    }
                    if (!ok) {
                        continue;
                    }
                }
            }
            NodeList children = parse.getChildNodes();
            for (int j = 0; j < children.getLength(); j++) {
                if (children.item(j).getNodeType() == Node.ELEMENT_NODE) {
                    return children.item(j);
                }
            }
        }
        return null;
    }

    private static boolean queue_equals(int[] queue, int qPos, int qLen,
            int[] value) {
        for (int i = 0; i < qLen; i++) {
            if (queue[(i + qPos) % qLen] != value[i]) {
                return false;
            }
        }
        return true;
    }

    private static File create_part_file(Reader in, String boundary)
            throws Exception {
        File temp = File.createTempFile("$te_", ".tmp");
        RandomAccessFile raf = new RandomAccessFile(temp, "rw");
        int qLen = boundary.length() + 2;
        int[] boundary_queue = new int[qLen];
        boundary_queue[0] = '-';
        boundary_queue[1] = '-';
        for (int i = 0; i < boundary.length(); i++) {
            boundary_queue[i + 2] = boundary.charAt(i);
        }
        int[] queue = new int[qLen];
        for (int i = 0; i < qLen; i++) {
            queue[i] = in.read();
            if (queue[i] == -1) {
                break;
            }
        }
        int qPos = 0;
        while (!queue_equals(queue, qPos, qLen, boundary_queue)) {
            raf.write(queue[qPos]);
            queue[qPos] = in.read();
            if (queue[qPos] == -1) {
                throw new Exception(EOS_ERR);
            }
            qPos = (qPos + 1) % qLen;
        }
        raf.close();
        return temp;
    }

    public static Document parse(URLConnection uc, Element instruction,
            PrintWriter logger, TECore core) throws Throwable {
        uc.connect();
        String mime = uc.getContentType();
        boolean multipart = (mime != null && mime.startsWith("multipart"));

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.newDocument();
        Element root = doc.createElement(multipart ? "multipart-response" : "response");

        if (uc.getHeaderFieldKey(0) == null) {
            String status_line = uc.getHeaderField(0);
            String status_array[] = status_line.split("\\s");
            Element status = doc.createElement("status");
            if (status_array.length > 0) {
                status.setAttribute("protocol", status_array[0]);
            }
            if (status_array.length > 1) {
                status.setAttribute("code", status_array[1]);
            }
            if (status_array.length > 2) {
                status.appendChild(doc.createTextNode(status_array[2]));
            }
            root.appendChild(status);
        }

        append_headers(uc, root);

        Transformer t = TransformerFactory.newInstance().newTransformer();

        if (multipart) {
            String mime2 = mime + ";";
            int start = mime2.indexOf("boundary=") + 9;
            char endchar = ';';
            if (mime2.charAt(start) == '"') {
                start++;
                endchar = '"';
            }
            int end = mime2.indexOf(endchar, start);
            String boundary = mime2.substring(start, end);
            BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
            File temp = create_part_file(in, boundary);
            temp.delete();
            String line = in.readLine();
            int num = 1;
            while (!line.endsWith("--")) {
                String contentType = "text/plain";
                Element part = doc.createElement("part");
                part.setAttribute("num", Integer.toString(num));
                Element headers = doc.createElement("headers");
                line = in.readLine();
                while (line.length() > 0) {
                    Element header = doc.createElement("header");
                    int colon = line.indexOf(":");
                    String name = line.substring(0, colon);
                    String value = line.substring(colon + 1).trim();
                    if (name.toLowerCase().equals("content-type")) {
                        contentType = value;
                    }
                    header.setAttribute("name", name);
                    header.appendChild(doc.createTextNode(value));
                    headers.appendChild(header);
                    line = in.readLine();
                }
                part.appendChild(headers);
                temp = create_part_file(in, boundary);
                URLConnection pc = temp.toURI().toURL().openConnection();
                pc.setRequestProperty("Content-type", mime);
                Node parser = select_parser(num, contentType, instruction);
                Element response_e = core.parse(pc, parser);
                temp.delete();
                Element parser_e = (Element) (response_e.getElementsByTagName("parser").item(0));
                if (parser_e != null) {
                    logger.print(parser_e.getTextContent());
                }
                Element content = (Element) (response_e.getElementsByTagName("content").item(0));
                t.transform(new DOMSource(content), new DOMResult(part));
                root.appendChild(part);
                line = in.readLine();
                num++;
            }
        } else {
            Node parser = select_parser(0, uc.getContentType(), instruction);
            Element response_e = core.parse(uc, parser);
            Element parser_e = (Element) (response_e.getElementsByTagName("parser").item(0));
            if (parser_e != null) {
                logger.print(parser_e.getTextContent());
            }
            Element content = (Element) (response_e.getElementsByTagName("content").item(0));
            t.transform(new DOMSource(content), new DOMResult(root));
        }
        doc.appendChild(root);
        return doc;
    }
}
