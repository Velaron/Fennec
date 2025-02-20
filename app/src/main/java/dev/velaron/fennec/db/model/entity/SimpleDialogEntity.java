package dev.velaron.fennec.db.model.entity;

public class SimpleDialogEntity {

    private final int peerId;

    private String title;

    private int unreadCount;

    private String photo50;

    private String photo100;

    private String photo200;

    private int inRead;

    private int outRead;

    private MessageEntity pinned;

    private int lastMessageId;

    private boolean isGroupChannel;

    private int acl;

    public SimpleDialogEntity setAcl(int acl) {
        this.acl = acl;
        return this;
    }

    public int getAcl() {
        return acl;
    }

    public SimpleDialogEntity setLastMessageId(int lastMessageId) {
        this.lastMessageId = lastMessageId;
        return this;
    }

    public int getLastMessageId() {
        return lastMessageId;
    }

    public SimpleDialogEntity setPinned(MessageEntity pinned) {
        this.pinned = pinned;
        return this;
    }

    public MessageEntity getPinned() {
        return pinned;
    }

    public SimpleDialogEntity(int peerId) {
        this.peerId = peerId;
    }

    public int getPeerId() {
        return peerId;
    }

    public String getTitle() {
        return title;
    }

    public SimpleDialogEntity setTitle(String title) {
        this.title = title;
        return this;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public SimpleDialogEntity setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
        return this;
    }

    public String getPhoto50() {
        return photo50;
    }

    public SimpleDialogEntity setPhoto50(String photo50) {
        this.photo50 = photo50;
        return this;
    }

    public String getPhoto100() {
        return photo100;
    }

    public SimpleDialogEntity setPhoto100(String photo100) {
        this.photo100 = photo100;
        return this;
    }

    public String getPhoto200() {
        return photo200;
    }

    public SimpleDialogEntity setPhoto200(String photo200) {
        this.photo200 = photo200;
        return this;
    }

    public int getInRead() {
        return inRead;
    }

    public SimpleDialogEntity setInRead(int inRead) {
        this.inRead = inRead;
        return this;
    }

    public int getOutRead() {
        return outRead;
    }

    public SimpleDialogEntity setOutRead(int outRead) {
        this.outRead = outRead;
        return this;
    }

    public boolean isGroupChannel() {
        return isGroupChannel;
    }

    public SimpleDialogEntity setGroupChannel(boolean groupChannel) {
        isGroupChannel = groupChannel;
        return this;
    }
}