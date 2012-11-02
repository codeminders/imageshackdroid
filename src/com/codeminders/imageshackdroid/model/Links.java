package com.codeminders.imageshackdroid.model;

import java.util.Date;

/**
 * @author Igor Giziy <linsalion@gmail.com>
 */
public abstract class Links {
    private int id;
    private String name;
    private Date date;
    private String size;

    private String image_link;
    private String thumb_link;
    private String thumb_html;
    private String thumb_bb;
    private String thumb_bb2;
    private String yfrog_link;
    private String yfrog_thumb;
    private String ad_link;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getImage_link() {
        return image_link;
    }

    public void setImage_link(String image_link) {
        this.image_link = image_link;
    }

    public String getThumb_link() {
        return thumb_link;
    }

    public void setThumb_link(String thumb_link) {
        this.thumb_link = thumb_link;
    }

    public String getThumb_html() {
        return thumb_html;
    }

    public void setThumb_html(String thumb_html) {
        this.thumb_html = thumb_html;
    }

    public String getThumb_bb() {
        return thumb_bb;
    }

    public void setThumb_bb(String thumb_bb) {
        this.thumb_bb = thumb_bb;
    }

    public String getThumb_bb2() {
        return thumb_bb2;
    }

    public void setThumb_bb2(String thumb_bb2) {
        this.thumb_bb2 = thumb_bb2;
    }

    public String getYfrog_link() {
        return yfrog_link;
    }

    public void setYfrog_link(String yfrog_link) {
        this.yfrog_link = yfrog_link;
    }

    public String getYfrog_thumb() {
        return yfrog_thumb;
    }

    public void setYfrog_thumb(String yfrog_thumb) {
        this.yfrog_thumb = yfrog_thumb;
    }

    public String getAd_link() {
        return ad_link;
    }

    public void setAd_link(String ad_link) {
        this.ad_link = ad_link;
    }

    public abstract String[] getLinks();

}
