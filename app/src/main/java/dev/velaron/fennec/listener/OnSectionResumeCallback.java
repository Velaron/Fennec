package dev.velaron.fennec.listener;

import dev.velaron.fennec.model.drawer.SectionMenuItem;

public interface OnSectionResumeCallback {
    void onSectionResume(SectionMenuItem sectionDrawerItem);
    void onChatResume(int accountId, int peerId, String title, String imgUrl);
    void onClearSelection();
}
