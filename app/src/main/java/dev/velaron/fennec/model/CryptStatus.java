package dev.velaron.fennec.model;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

/**
 * Created by admin on 08.10.2016.
 * phoenix
 */
@IntDef({CryptStatus.NO_ENCRYPTION, CryptStatus.ENCRYPTED, CryptStatus.DECRYPTED, CryptStatus.DECRYPT_FAILED})
@Retention(RetentionPolicy.SOURCE)
public @interface CryptStatus {
    int NO_ENCRYPTION = 0;
    int ENCRYPTED = 1;
    int DECRYPTED = 2;
    int DECRYPT_FAILED = 3;
}
