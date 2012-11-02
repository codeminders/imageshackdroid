package com.codeminders.imageshackdroid.activities;

import java.util.LinkedList;

import android.app.Activity;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.RelativeLayout.LayoutParams;

import com.codeminders.imageshackdroid.*;
import com.codeminders.imageshackdroid.db.DataHelper;
import com.codeminders.imageshackdroid.model.Links;
import com.codeminders.imageshackdroid.model.VideoLinks;

/**
 * @author Igor Giziy <linsalion@gmail.com>
 */
public class LinksActivity extends Activity {
    private boolean multiselect = false;
    private int[][] namesId = new int[][]{
            {
                    R.string.ad_link,
                    R.string.image_link,
                    R.string.image_bb,
                    R.string.image_bb2,
                    R.string.image_html,
                    R.string.thumb_link,
                    R.string.thumb_bb,
                    R.string.thumb_bb2,
                    R.string.thumb_html,
                    R.string.yfrog_link,
                    R.string.yfrog_thumb
            },
            {
                    R.string.ad_link,
                    R.string.direct_link,
                    R.string.thumb_link,
                    R.string.thumb_bb,
                    R.string.thumb_bb2,
                    R.string.thumb_html,
                    R.string.frame_link,
                    R.string.frame_bb,
                    R.string.frame_bb2,
                    R.string.frame_html,
                    R.string.video_embed,
                    R.string.yfrog_link,
                    R.string.yfrog_thumb
            }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.links);

        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
        TextView titleText = (TextView) findViewById(R.id.title);

        Bundle bundel = getIntent().getExtras();
        if (bundel != null) {
            int pos = bundel.getInt("position");

            LinkedList<Links> linksList = UploadService.getLinks();
            Links link = null;
            for (Links link1 : linksList) {
                if (link1.getId() == pos) {
                    link = link1;
                    break;
                }
            }

            if (link == null) {
                DataHelper dataHelper = new DataHelper(this);
                link = dataHelper.getLink(pos);
            }

            LinearLayout scrollLayout = (LinearLayout) findViewById(R.id.linkslayout);
            titleText.setText(link.getName());

            int num = 0;
            if (link instanceof VideoLinks) {
                num = 1;
            }

            String[] links = link.getLinks();
            for (int i = 0; i < links.length; i++) {
                TextView textView = new TextView(this);
                textView.setText(getString(namesId[num][i]));
                scrollLayout.addView(textView);

                RelativeLayout relativeLayout = new RelativeLayout(this);
                relativeLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

                scrollLayout.addView(relativeLayout);

                EditText editText = new EditText(this);
                LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                params.addRule(RelativeLayout.LEFT_OF, 1020 + i * 3);
                editText.setLayoutParams(params);
                editText.setId(1019 + i * 3);
                editText.setSingleLine();
                editText.setFocusable(false);
                editText.setLongClickable(false);
                editText.setText(links[i]);
                relativeLayout.addView(editText);

                Button copyButton = new Button(this);
                LayoutParams params2 = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                params2.addRule(RelativeLayout.ALIGN_LEFT);
                params2.addRule(RelativeLayout.LEFT_OF, 1021 + i * 3);
                copyButton.setLayoutParams(params2);
                copyButton.setText(getString(R.string.btn_copy));
                copyButton.setId(1020 + i * 3);
                copyButton.setOnClickListener(copyListener);
                relativeLayout.addView(copyButton);

                Button shareButton = new Button(this);
                LayoutParams params3 = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                params3.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                shareButton.setLayoutParams(params3);
                shareButton.setText(getString(R.string.btn_share));
                shareButton.setId(1021 + i * 3);
                shareButton.setOnClickListener(shareListener);
                relativeLayout.addView(shareButton);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.links_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (multiselect) {
            menu.findItem(R.id.links_multiselect).setVisible(false);
            menu.findItem(R.id.links_copy).setVisible(true);
            menu.findItem(R.id.links_share).setVisible(true);
        } else {
            menu.findItem(R.id.links_multiselect).setVisible(true);
            menu.findItem(R.id.links_copy).setVisible(false);
            menu.findItem(R.id.links_share).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.links_multiselect:
                multiselect = true;
                prepareView();
                return true;
            case R.id.links_copy:
                multiselect = false;
                copy(prepareView());

                return true;
            case R.id.links_share:
                multiselect = false;
                Utils.share(this, prepareView());

                return true;
        }
        return false;
    }

    private String prepareView() {
        LinearLayout linearLayout = (LinearLayout) findViewById(1019).getParent().getParent();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < linearLayout.getChildCount() / 2; i++) {
            if (multiselect) {
                ((RelativeLayout) findViewById(1019 + i * 3).getParent()).removeViews(1, 2);
                LayoutParams params3 = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                params3.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                params3.addRule(RelativeLayout.ALIGN_TOP);
                CheckBox cb = new CheckBox(this);
                cb.setLayoutParams(params3);
                cb.setId(1020 + i * 3);
                findViewById(1019 + i * 3).setFocusable(false);
                findViewById(1019 + i * 3).setLongClickable(false);
                ((RelativeLayout) findViewById(1019 + i * 3).getParent()).addView(cb);
            } else {
                if (((CheckBox)findViewById(1020 + i * 3)).isChecked()) {
                     if (stringBuilder.length()>0) {
                         stringBuilder.append("\n");
                     }
                     stringBuilder.append(((EditText)findViewById(1019 + i * 3)).getText());
                }
                ((RelativeLayout) findViewById(1019 + i * 3).getParent()).removeViewAt(1);
                findViewById(1019 + i * 3).setFocusable(true);
                findViewById(1019 + i * 3).setLongClickable(false);

                Button copyButton = new Button(this);
                LayoutParams params2 = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                params2.addRule(RelativeLayout.ALIGN_LEFT);
                params2.addRule(RelativeLayout.LEFT_OF, 1021 + i * 3);
                copyButton.setLayoutParams(params2);
                copyButton.setText(getString(R.string.btn_copy));
                copyButton.setId(1020 + i * 3);
                copyButton.setOnClickListener(copyListener);
                ((RelativeLayout) findViewById(1019 + i * 3).getParent()).addView(copyButton);

                Button shareButton = new Button(this);
                LayoutParams params3 = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                params3.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                shareButton.setLayoutParams(params3);
                shareButton.setText(getString(R.string.btn_share));
                shareButton.setId(1021 + i * 3);
                shareButton.setOnClickListener(shareListener);
                ((RelativeLayout) findViewById(1019 + i * 3).getParent()).addView(shareButton);
            }
        }
        return stringBuilder.toString();
    }

    private void copy(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        clipboard.setText(text);
        Toast.makeText(LinksActivity.this, getString(R.string.copied), Toast.LENGTH_LONG).show();
    }

    public OnClickListener copyListener = new OnClickListener() {
        public void onClick(View v) {
            EditText linkText = (EditText) findViewById(v.getId() - 1);
            copy(linkText.getText().toString());
        }
    };

    public OnClickListener shareListener = new OnClickListener() {
        public void onClick(View v) {
            EditText linkText = (EditText) findViewById(v.getId() - 2);
            Utils.share(LinksActivity.this, linkText.getText().toString());
        }
    };

}
