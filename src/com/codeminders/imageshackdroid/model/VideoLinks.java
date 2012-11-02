package com.codeminders.imageshackdroid.model;

/**
 * @author Igor Giziy <linsalion@gmail.com>
 */
public class VideoLinks extends Links {
    private String frame_link;
    private String frame_html;
    private String frame_bb;
    private String frame_bb2;
    private String video_embed;


    public String getFrame_link() {
        return frame_link;
    }

    public void setFrame_link(String frame_link) {
        this.frame_link = frame_link;
    }

    public String getFrame_html() {
        return frame_html;
    }

    public void setFrame_html(String frame_html) {
        this.frame_html = frame_html;
    }

    public String getFrame_bb() {
        return frame_bb;
    }

    public void setFrame_bb(String frame_bb) {
        this.frame_bb = frame_bb;
    }

    public String getFrame_bb2() {
        return frame_bb2;
    }

    public void setFrame_bb2(String frame_bb2) {
        this.frame_bb2 = frame_bb2;
    }

    public String getVideo_embed() {
        return video_embed;
    }

    public void setVideo_embed(String video_embed) {
        this.video_embed = video_embed;
    }

    public String[] getLinks() {
        return new String[]{
                getAd_link(),
                getImage_link(),
                getThumb_link(),
                getThumb_bb(),
                getThumb_bb2(),
                getThumb_html(),
                frame_link,
                frame_bb,
                frame_bb2,
                frame_html,
                video_embed,
                getYfrog_link(),
                getYfrog_thumb()
        };
    }

}
