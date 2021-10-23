package dev.velaron.fennec.db.model;

import dev.velaron.fennec.db.model.entity.MessageEntity;

public final class PeerPatch {

    private ReadTo inRead;

    private ReadTo outRead;

    private Unread unread;

    private LastMessage lastMessage;

    private Pin pin;

    private Title title;

    private final int id;

    public PeerPatch(int id) {
        this.id = id;
    }

    public PeerPatch withInRead(int id){
        this.inRead = new ReadTo(id);
        return this;
    }

    public PeerPatch withOutRead(int id){
        this.outRead = new ReadTo(id);
        return this;
    }

    public PeerPatch withUnreadCount(int count){
        this.unread = new Unread(count);
        return this;
    }

    public PeerPatch withLastMessage(int id){
        this.lastMessage = new LastMessage(id);
        return this;
    }

    public Pin getPin() {
        return pin;
    }

    public PeerPatch withPin(MessageEntity pinned){
        this.pin = new Pin(pinned);
        return this;
    }

    public PeerPatch withTitle(String title){
        this.title = new Title(title);
        return this;
    }

    public Title getTitle() {
        return title;
    }

    public LastMessage getLastMessage() {
        return lastMessage;
    }

    public Unread getUnread() {
        return unread;
    }

    public int getId() {
        return id;
    }

    public ReadTo getInRead() {
        return inRead;
    }

    public ReadTo getOutRead() {
        return outRead;
    }

    public static final class Title {

        private final String title;

        Title(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }
    }

    public static final class Unread {

        private final int count;

        Unread(int count) {
            this.count = count;
        }

        public int getCount() {
            return count;
        }
    }

    public static final class Pin {

        private final MessageEntity pinned;

        Pin(MessageEntity pinned) {
            this.pinned = pinned;
        }

        public MessageEntity getPinned() {
            return pinned;
        }
    }

    public static final class LastMessage {

        private final int id;

        LastMessage(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    public static final class ReadTo {

        private final int id;

        private ReadTo(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }
}