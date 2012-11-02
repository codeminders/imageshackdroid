package com.codeminders.imageshackdroid;

import android.util.Log;
import android.util.Xml;
import com.codeminders.imageshackdroid.model.ImageLinks;
import com.codeminders.imageshackdroid.model.Links;
import com.codeminders.imageshackdroid.model.VideoLinks;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.InputStream;


/**
 * @author Igor Giziy <linsalion@gmail.com>
 */
public class LinkParser extends DefaultHandler {
    private StringBuilder value = new StringBuilder();
    private Links links;


    public LinkParser(int type) {
        if (type == Constants.TYPE_IMAGE) {
            links = new ImageLinks();
        } else {
            links = new VideoLinks();
        }
    }

    public Links parse(InputStream xml) {
        try {
            Xml.parse(xml, Xml.Encoding.ISO_8859_1, this);
            xml.close();
        } catch (Exception e) {
            Log.d(Constants.TAG, e.toString());
        }

        return links;
    }


    public void startElement(String uri, String local_name, String raw_name, Attributes amap) throws SAXException {
        value.delete(0, value.length());
    }

    public void endElement(String uri, String local_name, String raw_name) throws SAXException {
        if (links instanceof ImageLinks) {
            if (local_name.equals("image_link")) {
                links.setImage_link(value.toString());
            } else if (local_name.equals("image_html")) {
                ((ImageLinks) links).setImage_html(value.toString());
            } else if (local_name.equals("image_bb")) {
                ((ImageLinks) links).setImage_bb(value.toString());
            } else if (local_name.equals("image_bb2")) {
                ((ImageLinks) links).setImage_bb2(value.toString());
            } else if (local_name.equals("thumb_link")) {
                links.setThumb_link(value.toString());
            } else if (local_name.equals("thumb_html")) {
                links.setThumb_html(value.toString());
            } else if (local_name.equals("thumb_bb")) {
                links.setThumb_bb(value.toString());
            } else if (local_name.equals("thumb_bb2")) {
                links.setThumb_bb2(value.toString());
            } else if (local_name.equals("yfrog_link")) {
                links.setYfrog_link(value.toString());
            } else if (local_name.equals("yfrog_thumb")) {
                links.setYfrog_thumb(value.toString());
            } else if (local_name.equals("ad_link")) {
                links.setAd_link(value.toString());
            }
        } else {
            if (local_name.equals("image_link")) {
                links.setImage_link(value.toString());
            } else if (local_name.equals("thumb_link")) {
                links.setThumb_link(value.toString());
            } else if (local_name.equals("thumb_html")) {
                links.setThumb_html(value.toString());
            } else if (local_name.equals("thumb_bb")) {
                links.setThumb_bb(value.toString());
            } else if (local_name.equals("thumb_bb2")) {
                links.setThumb_bb2(value.toString());
            } else if (local_name.equals("yfrog_link")) {
                links.setYfrog_link(value.toString());
            } else if (local_name.equals("yfrog_thumb")) {
                links.setYfrog_thumb(value.toString());
            } else if (local_name.equals("frame_link")) {
                ((VideoLinks) links).setFrame_link(value.toString());
            } else if (local_name.equals("frame_html")) {
                ((VideoLinks) links).setFrame_html(value.toString());
            } else if (local_name.equals("frame_bb")) {
                ((VideoLinks) links).setFrame_bb(value.toString());
            } else if (local_name.equals("frame_bb2")) {
                ((VideoLinks) links).setFrame_bb2(value.toString());
            } else if (local_name.equals("video_embed")) {
                ((VideoLinks) links).setVideo_embed(value.toString());
            } else if (local_name.equals("ad_link")) {
                links.setAd_link(value.toString());
            }
        }
    }

    public void startDocument() throws SAXException {
    }

    public void endDocument() throws SAXException {
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        value.append(ch, start, length);
    }

}
