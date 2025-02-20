package dev.velaron.fennec.db.model.entity;

/**
 * Created by admin on 04.09.2017.
 * phoenix
 */
public class DialogEntity extends Entity {

    private final int peerId;

    private String title;

    private int unreadCount;

    private String photo50;

    private String photo100;

    private String photo200;

    private int inRead;

    private int outRead;

    private MessageEntity message;

    private MessageEntity pinned;

    private int lastMessageId;

    private int acl;

    private boolean isGroupChannel;

    public DialogEntity setAcl(int acl) {
        this.acl = acl;
        return this;
    }

    public int getAcl() {
        return acl;
    }

    public DialogEntity(int peerId) {
        this.peerId = peerId;
    }

    public DialogEntity setInRead(int inRead) {
        this.inRead = inRead;
        return this;
    }

    public DialogEntity setPinned(MessageEntity pinned) {
        this.pinned = pinned;
        return this;
    }

    public MessageEntity getPinned() {
        return pinned;
    }

    public int getInRead() {
        return inRead;
    }

    public int getOutRead() {
        return outRead;
    }

    public DialogEntity setOutRead(int outRead) {
        this.outRead = outRead;
        return this;
    }

    public int getPeerId() {
        return peerId;
    }

    public String getTitle() {
        return title;
    }

    public DialogEntity setTitle(String title) {
        this.title = title;
        return this;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public DialogEntity setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
        return this;
    }

    public String getPhoto50() {
        return photo50;
    }

    public DialogEntity setPhoto50(String photo50) {
        this.photo50 = photo50;
        return this;
    }

    public SimpleDialogEntity simplify(){
        return new SimpleDialogEntity(peerId)
                .setTitle(title)
                .setPhoto200(photo200)
                .setPhoto100(photo100)
                .setPhoto50(photo50)
                .setOutRead(outRead)
                .setInRead(inRead)
                .setUnreadCount(unreadCount)
                .setPinned(pinned)
                .setLastMessageId(lastMessageId)
                .setAcl(acl)
                .setGroupChannel(isGroupChannel);
    }

    public String getPhoto100() {
        return photo100;
    }

    public DialogEntity setPhoto100(String photo100) {
        this.photo100 = photo100;
        return this;
    }

    public String getPhoto200() {
        return photo200;
    }

    public DialogEntity setPhoto200(String photo200) {
        this.photo200 = photo200;
        return this;
    }

    public MessageEntity getMessage() {
        return message;
    }

    public DialogEntity setMessage(MessageEntity message) {
        this.message = message;
        return this;
    }

    public int getLastMessageId() {
        return lastMessageId;
    }

    public DialogEntity setLastMessageId(int lastMessageId) {
        this.lastMessageId = lastMessageId;
        return this;
    }

    public boolean isGroupChannel() {
        return isGroupChannel;
    }

    public DialogEntity setGroupChannel(boolean groupChannel) {
        isGroupChannel = groupChannel;
        return this;
    }
}