package dev.velaron.fennec.link.types;

public class FaveLink extends AbsLink {

    public static final String SECTION_VIDEOS = "likes_video";
    public static final String SECTION_PHOTOS = "likes_photo";
    public static final String SECTION_POSTS = "likes_posts";
    public static final String SECTION_PAGES = "pages";
    public static final String SECTION_LINKS = "links";

    public String section;

    public FaveLink(String section) {
        super(FAVE);
        this.section = section;
    }

    public FaveLink(){
        this(null);
    }

    @Override
    public String toString() {
        return "FaveLink{" +
                "section='" + section + '\'' +
                '}';
    }
}
