package dev.velaron.fennec.domain

import dev.velaron.fennec.Injection
import dev.velaron.fennec.db.Stores
import dev.velaron.fennec.domain.impl.MessagesRepository
import dev.velaron.fennec.domain.impl.OwnersRepository
import dev.velaron.fennec.domain.impl.WallsRepository
import dev.velaron.fennec.settings.Settings

object Repository {
    val owners: IOwnersRepository by lazy {
        OwnersRepository(Injection.provideNetworkInterfaces(), Stores.getInstance().owners())
    }

    val walls: IWallsRepository by lazy {
        WallsRepository(Injection.provideNetworkInterfaces(), Stores.getInstance(), owners)
    }

    val messages: IMessagesRepository by lazy {
        MessagesRepository(Settings.get().accounts(),
                Injection.provideNetworkInterfaces(),
                owners,
                Injection.provideStores(),
                Injection.provideUploadManager())
    }
}