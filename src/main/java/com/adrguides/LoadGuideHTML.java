//        Guidebook is an Android application that reads audioguides using Text-to-Speech services.
//        Copyright (C) 2013  Adrián Romero Corchado
//
//        This program is free software: you can redistribute it and/or modify
//        it under the terms of the GNU General Public License as published by
//        the Free Software Foundation, either version 3 of the License, or
//        (at your option) any later version.
//
//        This program is distributed in the hope that it will be useful,
//        but WITHOUT ANY WARRANTY; without even the implied warranty of
//        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//        GNU General Public License for more details.
//
//        You should have received a copy of the GNU General Public License
//        along with this program.  If not, see <http://www.gnu.org/licenses/>.

package com.adrguides;

import android.content.Context;
import android.util.Log;

import com.adrguides.model.Guide;
import com.adrguides.model.Place;
import com.adrguides.model.Section;
import com.adrguides.utils.GuidesException;

import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by adrian on 18/09/13.
 */
public class LoadGuideHTML extends LoadGuide {

    public LoadGuideHTML(Context context, int imagesize) {
        super(context, imagesize);
    }

    private Guide guide;
    private String guidebookimage;
    private String chapterimage;

    @Override
    protected Guide load_imp(URL address, String text) throws Exception {


        Document doc = Jsoup.parse(text);

        guide = new Guide();
        guidebookimage = null;
        chapterimage = null;

        // address
        Elements canonical = doc.head().getElementsByAttributeValue("rel", "canonical");
        if (canonical.size() == 1 && canonical.get(0).tagName().equals("link")) {
            guide.setAddress(new URL(address, canonical.get(0).attr("href")).toString());
        } else {
            guide.setAddress(address.toString());
        }

        // Title if exists
        guide.setTitle(doc.title());

        // Language
        String lang = doc.attr("lang");
        if (lang == null || lang.equals("")) {
            Elements metalang = doc.head().getElementsByAttributeValue("http-equiv", "Content-Language");
            if (metalang.size() == 1 && metalang.get(0).tagName().equals("meta")) {
                setLanguage(metalang.get(0).attr("content"));
            }
        } else {
            setLanguage(lang);
        }

        navigateElement(address, doc.body());

        return guide;
    }

    private void setLanguage(String lang) {
        String[] loc = lang.split("-");
        guide.setLanguage(loc.length > 0 ? loc[0] : "");
        guide.setLanguage(loc.length > 1 ? loc[1] : "");
        guide.setLanguage(loc.length > 2 ? loc[2] : "");
    }

    private void navigateElement(URL address, Element elem) throws GuidesException {
        ArrayList places;

        if (elem.hasClass("guidebook_title")) {
            guide.setTitle(elem.text());
        } else if (elem.hasClass("guidebook_chapter")) {

            Place place = new Place();
            place.setId(elem.hasAttr("data-guidebook-id") ? elem.attr("data-guidebook-id") : null);
            place.setTitle(elem.text());
            guide.getPlaces().add(place);
            chapterimage = null;

        } else if (elem.hasClass("guidebook_image")) {
            if (guide.getPlaces().size() == 0) {
                guidebookimage = loadReferencedImage(address, elem);
            } else {
                chapterimage = loadReferencedImage(address, elem);
            }
        } else if (elem.hasClass("guidebook_paragraph")) {
            processParagraph(elem, loadLinkedImage(address, elem), true);
        } else if (elem.hasClass("guidebook_section")) {
            processParagraph(elem, loadLinkedImage(address, elem), false);
        } else {
            navigateChildren(address, elem);
        }
    }

    private void addSection(String text, String read, String loadedimage) throws GuidesException {

        if (guide.getPlaces().size() == 0) {
            throw new GuidesException(R.string.ex_cannotaddtext, "Cannot add text to an empty guide.");
        }
        Log.d("com.adrguides.LoadGuideHTML", "adding text -> " + text);
        if (text != null && read != null) {
            String texttrimmed = text.trim();
            String readtrimmed = read.trim();

            if (!texttrimmed.equals("")) {
                Place place = guide.getPlaces().get(guide.getPlaces().size() - 1);
                Section section;
                if (place.getSections().size() == 0
                        || !place.getSections().get(place.getSections().size() - 1).getText().equals("")) {
                    section = new Section();
                    place.getSections().add(section);
                } else { // if section has image but no text add it.
                    section = place.getSections().get(place.getSections().size() - 1);
                }
                section.setText(texttrimmed);
                section.setRead(readtrimmed.equals(texttrimmed) ? null : readtrimmed);
                section.setImageURL(loadedimage);
            }
        }
    }

    private String loadLinkedImage(URL address, Element elem) throws GuidesException {
        try {
            return loadReferencedImage(address, elem);
        } catch (GuidesException e) {
            if (chapterimage != null) {
                return chapterimage;
            } else if (guidebookimage != null) {
                return guidebookimage;
            } else {
                return null;
            }
        }
    }

    private String loadReferencedImage(URL address, Element elem) throws GuidesException {
        if (elem == null) {
            throw new GuidesException(R.string.ex_imagenotfound, "Refered image has not been found.");
        } else if ("img".equals(elem.tagName())) {
            return loadImage(address, elem.attr("src"));
        } else if (elem.hasAttr("data-guidebook-image")) {
            return loadLinkedImage(address, elem.ownerDocument().getElementById(elem.attr("data-guidebook-image")));
        } else {
            Pattern p = Pattern.compile("background\\s*\\:\\s*url\\s*\\(('|\"?)?+(.*)\\1\\)");
            Matcher m = p.matcher(elem.attr("style"));
            if (m.find()) {
                return loadImage(address, m.group(2));
            } else {
                throw new GuidesException(R.string.ex_imagenotfound, "Refered image has not been found.");
            }
        }
    }

    private void navigateChildren(URL address, Element content) throws GuidesException {
        Elements children = content.children();
        for (Element e : children) {
            navigateElement(address, e);
        }
    }
    private void processParagraph(Element elem, String srcimage, boolean divide) throws GuidesException {
        final StringBuilder accum = new StringBuilder();
        final StringBuilder accumread = new StringBuilder();

        processParagraph(accum, accumread, elem, srcimage, divide);

        if (accum.length() > 0) {
            addSection(accum.toString(), accumread.toString(), srcimage);
            accum.delete(0, accum.length());
            accumread.delete(0, accumread.length());
        }
    }

    private void processParagraph(StringBuilder accum, StringBuilder accumread, Element elem, String srcimage, boolean divide) throws GuidesException {

        for (Node node : elem.childNodes()) {
            if (node instanceof TextNode) {
                TextNode textNode = (TextNode) node;
                appendNormalisedText(accum, accumread, textNode, srcimage, divide);
            } else if (node instanceof Element) {
                Element element = (Element) node;
                if (accum.length() > 0 &&
                        (element.isBlock() || element.tagName().equals("br")) &&
                        !(accum.length() != 0 && accum.charAt(accum.length() - 1) == ' ')) {
                    accum.append(" ");
                    accumread.append(" ");
                } else if (element.hasAttr("data-guidebook-speech")) {
                    accum.append(element.text());
                    accumread.append(element.attr("data-guidebook-speech"));
                } else {
                    // recursion
                    processParagraph(accum, accumread, element, srcimage, divide);
                }
            }
        }
    }

    private void appendNormalisedText(StringBuilder accum, StringBuilder accumread, TextNode textNode, String srcimage, boolean divide) throws GuidesException {
        String text = textNode.getWholeText();

        if (!preserveWhitespace(textNode.parent())) {
            text = StringUtil.normaliseWhitespace(text);
            if (accum.length() != 0 && accum.charAt(accum.length() - 1) == ' ')
                text = text.replaceFirst("^\\s+", "");
        }

        if (divide) {
            // Divide text by . and ;
            int start = 0;
            while (start < text.length()) {
                int i = text.indexOf(". ", start);
                int j = text.indexOf("; ", start);
                if (i < 0 || j < 0) {
                    i = Math.max(i, j);
                } else {
                    i = Math.min(i, j);
                }
                if (i < 0) {
                    accum.append(text.substring(start));
                    accumread.append(text.substring(start));
                    start = text.length();
                } else {
                    accum.append(text.substring(start, i + 2));
                    accumread.append(text.substring(start, i + 2));
                    addSection(accum.toString(), accumread.toString(), srcimage);
                    accum.delete(0, accum.length());
                    accumread.delete(0, accumread.length());
                    start = i + 1;
                }
            }
        } else {
            accum.append(text);
            accumread.append(text);
        }
    }

    private boolean preserveWhitespace(Node node) {
        // looks only at this element and one level up, to prevent recursion & needless stack searches
        if (node != null && node instanceof Element) {
            Element element = (Element) node;
            return element.tag().preserveWhitespace() ||
                    element.parent() != null && element.parent().tag().preserveWhitespace();
        }
        return false;
    }
}
