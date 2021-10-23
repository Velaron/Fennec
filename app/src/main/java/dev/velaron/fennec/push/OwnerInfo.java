package dev.velaron.fennec.push;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import dev.velaron.fennec.R;
import dev.velaron.fennec.domain.IOwnersRepository;
import dev.velaron.fennec.domain.Repository;
import dev.velaron.fennec.model.Community;
import dev.velaron.fennec.model.Owner;
import dev.velaron.fennec.model.User;
import dev.velaron.fennec.util.Optional;
import io.reactivex.Single;

/**
 * Created by ruslan.kolbasa on 21.10.2016.
 * phoenix
 */
public class OwnerInfo {

    private final Owner owner;
    private final Bitmap avatar;

    private OwnerInfo(@NonNull Owner owner, Bitmap avatar) {
        this.owner = owner;
        this.avatar = avatar;
    }

    @NonNull
    public User getUser() {
        return (User) owner;
    }

    @NonNull
    public Owner getOwner() {
        return owner;
    }

    @NonNull
    public Community getCommunity() {
        return (Community) owner;
    }

    public Bitmap getAvatar() {
        return avatar;
    }

    public static Single<OwnerInfo> getRx(@NonNull Context context, int accountId, int ownerId) {
        final Context app = context.getApplicationContext();
        IOwnersRepository interactor = Repository.INSTANCE.getOwners();

        return interactor.getBaseOwnerInfo(accountId, ownerId, IOwnersRepository.MODE_ANY)
                .flatMap(owner -> Single.fromCallable(() -> NotificationUtils.loadRoundedImage(app, owner.get100photoOrSmaller(), R.drawable.ic_avatar_unknown))
                        .map(Optional::wrap)
                        .onErrorReturnItem(Optional.empty())
                        .map(optional -> new OwnerInfo(owner, optional.get())));
    }
}