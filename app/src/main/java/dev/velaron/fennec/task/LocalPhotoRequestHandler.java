package dev.velaron.fennec.task;

import android.content.Context;
import android.graphics.Bitmap;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;

import dev.velaron.fennec.db.Stores;

public class LocalPhotoRequestHandler extends RequestHandler {

    private Context mContext;

    public LocalPhotoRequestHandler(Context context) {
        this.mContext = context;
    }

    @Override
    public boolean canHandleRequest(Request data) {
        return data.uri != null && data.uri.getScheme() != null && data.uri.getScheme().equals("content");
    }

    @Override
    public RequestHandler.Result load(Request data, int arg1) {
        long imageId = Long.parseLong(data.uri.getLastPathSegment());

        Bitmap bm = Stores.getInstance()
                .localPhotos()
                .getImageThumbnail(imageId);

        return new RequestHandler.Result(bm, Picasso.LoadedFrom.DISK);
    }
}