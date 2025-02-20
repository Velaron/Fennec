package dev.velaron.fennec.api.model.longpoll;

public class InputMessagesSetReadUpdate extends AbsLongpollEvent {

    public InputMessagesSetReadUpdate() {
        super(ACTION_SET_INPUT_MESSAGES_AS_READ);
    }

    public int peer_id;
    public int local_id;
    public int unread_count;

    public int getPeerId() {
        return peer_id;
    }

    public int getLocalId() {
        return local_id;
    }

    public int getUnreadCount() {
        return unread_count;
    }
}