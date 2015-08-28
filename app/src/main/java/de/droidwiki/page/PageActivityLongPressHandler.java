package de.droidwiki.page;

import android.support.annotation.NonNull;

import de.droidwiki.WikipediaApp;
import de.droidwiki.history.HistoryEntry;
import de.droidwiki.savedpages.RefreshSavedPageTask;
import de.droidwiki.util.ClipboardUtil;
import de.droidwiki.util.FeedbackUtil;
import de.droidwiki.util.ShareUtils;

import java.util.List;

public abstract class PageActivityLongPressHandler implements PageLongPressHandler.ContextMenuListener {
    @NonNull private final PageActivity activity;

    public PageActivityLongPressHandler(@NonNull PageActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onOpenLink(PageTitle title, HistoryEntry entry) {
        activity.displayNewPage(title, entry);
    }

    @Override
    public void onOpenInNewTab(PageTitle title, HistoryEntry entry) {
        activity.displayNewPage(title, entry, PageActivity.TabPosition.NEW_TAB_BACKGROUND, false);
    }

    @Override
    public void onCopyLink(PageTitle title) {
        copyLink(title.getCanonicalUri());
        showCopySuccessMessage();
    }

    @Override
    public void onShareLink(PageTitle title) {
        ShareUtils.shareText(activity, title);
    }

    @Override
    public void onSavePage(PageTitle title) {
        spawnSavePageTask(title);
    }

    private void copyLink(String url) {
        ClipboardUtil.setPlainText(activity, null, url);
    }

    private void showCopySuccessMessage() {
        FeedbackUtil.showMessage(activity, de.droidwiki.R.string.address_copied);
    }

    private void spawnSavePageTask(@NonNull final PageTitle title) {
        new RefreshSavedPageTask(WikipediaApp.getInstance(), title) {
            @Override
            public void onFinish(List<Section> result) {
                super.onFinish(result);

                if (!activity.isDestroyed()) {
                    activity.showPageSavedMessage(title.getDisplayText(), true);
                }
            }

            @Override
            public void onCatch(Throwable caught) {
                if (!activity.isDestroyed()) {
                    FeedbackUtil.showError(activity, caught);
                }
            }
        }.execute();
    }
}
