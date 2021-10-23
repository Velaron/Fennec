package dev.velaron.fennec.api.impl;

import dev.velaron.fennec.api.IServiceProvider;
import dev.velaron.fennec.api.TokenType;
import dev.velaron.fennec.api.interfaces.IPagesApi;
import dev.velaron.fennec.api.model.VKApiWikiPage;
import dev.velaron.fennec.api.services.IPagesService;
import io.reactivex.Single;

/**
 * Created by admin on 08.01.2017.
 * phoenix
 */
class PagesApi extends AbsApi implements IPagesApi {

    PagesApi(int accountId, IServiceProvider provider) {
        super(accountId, provider);
    }

    @Override
    public Single<VKApiWikiPage> get(int ownerId, int pageId, Boolean global, Boolean sitePreview, String title, Boolean needSource, Boolean needHtml) {
        return provideService(IPagesService.class, TokenType.USER)
                .flatMap(service -> service
                        .get(ownerId, pageId, integerFromBoolean(global), integerFromBoolean(sitePreview),
                                title, integerFromBoolean(needSource), integerFromBoolean(needHtml))
                        .map(extractResponseWithErrorHandling()));
    }
}
