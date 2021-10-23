package dev.velaron.fennec.push;

import java.io.IOException;

/**
 * Created by admin on 07.10.2017.
 * Phoenix-for-VK
 */
public interface IGcmTokenProvider {
    String getToken() throws IOException;
}