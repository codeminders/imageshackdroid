package com.codeminders.imageshackdroid.model;

/**
 * @author Igor Giziy <linsalion@gmail.com>
 */
public class ImageLinks extends Links {
    private String image_html;
    private String image_bb;
    private String image_bb2;


    public String getImage_html() {
        return image_html;
    }

    public void setImage_html(String image_html) {
        this.image_html = image_html;
    }

    public String getImage_bb() {
        return image_bb;
    }

    public void setImage_bb(String image_bb) {
        this.image_bb = image_bb;
    }

    public String getImage_bb2() {
        return image_bb2;
    }

    public void setImage_bb2(String image_bb2) {
        this.image_bb2 = image_bb2;
    }

    public String[] getLinks() {
        return new String[]{
                getAd_link(),
                getImage_link(),
                image_bb,
                image_bb2,
                image_html,
                getThumb_link(),
                getThumb_bb(),
                getThumb_bb2(),
                getThumb_html(),
                getYfrog_link(),
                getYfrog_thumb()
        };
    }

}
