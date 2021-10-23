package dev.velaron.fennec.domain;

import java.util.List;

import dev.velaron.fennec.model.Message;
import io.reactivex.SingleTransformer;

/**
 * Created by admin on 05.09.2017.
 * phoenix
 */
public interface IMessagesDecryptor {
    /**
     * Предоставляет RX-трансформер для дешифровки сообщений
     * @param accountId идентификатор аккаунта
     * @return RX-трансформер
     */
    SingleTransformer<List<Message>, List<Message>> withMessagesDecryption(int accountId);
}