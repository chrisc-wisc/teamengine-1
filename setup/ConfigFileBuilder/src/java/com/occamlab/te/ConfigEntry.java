package com.occamlab.te;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.occamlab.te.util.DomUtils;

public class ConfigEntry {
    public String organization = null;
    public String standard = null;
    public String version = null;
    public String revision = null;
    public QName suite = null;
    public String title = null;
    public String description = null;
    public String link = null;
    public String dataLink = null;
    public List<QName> profiles = new ArrayList<QName>();
    public List<String> profileTitles = new ArrayList<String>();
    public List<String> profileDescriptions = new ArrayList<String>();
    public List<File> sources = new ArrayList<File>();
    public File resources;
    public String webdir;
    
    ConfigEntry(File file) throws Exception {
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = db.parse(file);
        Element config = DomUtils.getElementByTagName(doc, "config");
        Element organizationEl = DomUtils.getElementByTagName(config, "organization");
        if (organizationEl != null) {
            organization = DomUtils.getElementByTagName(organizationEl, "name").getTextContent();
        }
        Element standardEl = DomUtils.getElementByTagName(config, "standard");
        if (standardEl != null) {
            standard = DomUtils.getElementByTagName(standardEl, "name").getTextContent();
        }
        Element versionEl = DomUtils.getElementByTagName(config, "version");
        if (versionEl != null) {
            version = DomUtils.getElementByTagName(versionEl, "name").getTextContent();
        }
        Element revisionEl = DomUtils.getElementByTagName(config, "revision");
        if (revisionEl != null) {
          revision = DomUtils.getElementByTagName(revisionEl, "name").getTextContent();
        }
        Element suiteEl = DomUtils.getElementByTagName(config, "suite");
        if (suiteEl != null) {
            String localName = DomUtils.getElementByTagName(suiteEl, "local-name").getTextContent();
            String namespaceUri = DomUtils.getElementByTagName(suiteEl, "namespace-uri").getTextContent();
            String prefix = DomUtils.getElementByTagName(suiteEl, "prefix").getTextContent();
            suite = new QName(namespaceUri, localName, prefix);
            Element titleEl = DomUtils.getElementByTagName(suiteEl, "title");
            if (titleEl != null) title = titleEl.getTextContent();
            Element descriptionEl = DomUtils.getElementByTagName(suiteEl, "description");
            if (descriptionEl != null) description = descriptionEl.getTextContent();
//            link = file.getParentFile().getName();
            for (Element linkEl : DomUtils.getElementsByTagName(suiteEl, "link")) {
                String value = linkEl.getTextContent();
                if ("data".equals(linkEl.getAttribute("linkType"))) {
                    dataLink = file.getParentFile().getName() + "/" + value;
                } else if (value.startsWith("data/")) {
                    dataLink = file.getParentFile().getName() + "/" + value;
                } else {
                    link = value;
                }
            }
        }
        for (Element profileEl : DomUtils.getElementsByTagName(config, "profile")) {
            String localName = DomUtils.getElementByTagName(profileEl, "local-name").getTextContent();
            String namespaceUri = DomUtils.getElementByTagName(profileEl, "namespace-uri").getTextContent();
            String prefix = DomUtils.getElementByTagName(profileEl, "prefix").getTextContent();
            profiles.add(new QName(namespaceUri, localName, prefix));
            Element titleEl = DomUtils.getElementByTagName(profileEl, "title");
            profileTitles.add(titleEl == null ? "" : titleEl.getTextContent());
            Element descriptionEl = DomUtils.getElementByTagName(profileEl, "description");
            profileDescriptions.add(descriptionEl == null ? "" : descriptionEl.getTextContent());
        }
        for (Element sourceEl : DomUtils.getElementsByTagName(config, "source")) {
            sources.add(new File(file.getParentFile(), sourceEl.getTextContent()));
        }
        Element resourcesEl = DomUtils.getElementByTagName(config, "resources");
        if (resourcesEl != null) {
            resources = new File(file.getParentFile(), resourcesEl.getTextContent());
        }
        webdir = file.getParentFile().getName();
        Element webEl = DomUtils.getElementByTagName(config, "web");
        if (webEl != null) {
            String dirname = webEl.getAttribute("dirname");
            if (dirname.length() > 0) {
                webdir = dirname;
            }
        }
    }
    
    void add(ConfigEntry config) {
        profiles.addAll(config.profiles);
        profileTitles.addAll(config.profileTitles);
        profileDescriptions.addAll(config.profileDescriptions);
        sources.addAll(config.sources);
    }
}
