package dev.velaron.fennec.view.emoji;

import dev.velaron.fennec.model.StickerSet;

public class StickerSection extends AbsSection {

    public StickerSet stickerSet;

    public StickerSection(StickerSet set) {
        super(TYPE_STICKER);
        this.stickerSet = set;
    }
}
