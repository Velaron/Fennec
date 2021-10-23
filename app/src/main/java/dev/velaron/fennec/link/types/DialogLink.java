package dev.velaron.fennec.link.types;

public class DialogLink extends AbsLink {

    public int peerId;

    public DialogLink(int peerId) {
        super(DIALOG);
        this.peerId = peerId;
    }

    @Override
    public String toString() {
        return "DialogLink{" +
                "peerId=" + peerId +
                '}';
    }
}
