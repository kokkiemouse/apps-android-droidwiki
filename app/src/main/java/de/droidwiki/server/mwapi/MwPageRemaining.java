package de.droidwiki.server.mwapi;

import de.droidwiki.page.Page;
import de.droidwiki.page.Section;
import de.droidwiki.server.PageRemaining;

import com.google.gson.annotations.Expose;

import android.support.annotation.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Gson POJO for loading remaining page content.
 */
public class MwPageRemaining implements PageRemaining {
    @Expose @Nullable private Mobileview mobileview;

    @Override
    public void mergeInto(Page page) {
        page.addRemainingSections(getSections());
    }

    private List<Section> getSections() {
        if (mobileview != null) {
            return mobileview.getSections();
        } else {
            return Collections.emptyList();
        }
    }


    /**
     * Almost everything is in this inner class.
     */
    public static class Mobileview {
        @Expose
        private List<Section> sections;

        @Nullable
        public List<Section> getSections() {
            return sections;
        }
    }
}