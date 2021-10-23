package dev.velaron.fennec.mvp.presenter.search;

import android.os.Bundle;

import androidx.annotation.Nullable;

import java.util.List;

import dev.velaron.fennec.domain.IDocsInteractor;
import dev.velaron.fennec.domain.InteractorFactory;
import dev.velaron.fennec.fragment.search.criteria.DocumentSearchCriteria;
import dev.velaron.fennec.fragment.search.nextfrom.IntNextFrom;
import dev.velaron.fennec.model.Document;
import dev.velaron.fennec.mvp.view.search.IDocSearchView;
import dev.velaron.fennec.util.Pair;
import dev.velaron.fennec.util.Utils;
import io.reactivex.Single;

/**
 * Created by Ruslan Kolbasa on 06.10.2017.
 * Phoenix-for-VK
 */
public class DocsSearchPresenter extends AbsSearchPresenter<IDocSearchView, DocumentSearchCriteria, Document, IntNextFrom> {

    private final IDocsInteractor docsInteractor;

    public DocsSearchPresenter(int accountId, @Nullable DocumentSearchCriteria criteria, @Nullable Bundle savedInstanceState) {
        super(accountId, criteria, savedInstanceState);
        this.docsInteractor = InteractorFactory.createDocsInteractor();
    }

    @Override
    IntNextFrom getInitialNextFrom() {
        return new IntNextFrom(0);
    }

    @Override
    boolean isAtLast(IntNextFrom startFrom) {
        return startFrom.getOffset() == 0;
    }

    @Override
    Single<Pair<List<Document>, IntNextFrom>> doSearch(int accountId, DocumentSearchCriteria criteria, IntNextFrom startFrom) {
        final int offset = startFrom.getOffset();
        final IntNextFrom nextFrom = new IntNextFrom(50 + offset);
        return docsInteractor.search(accountId, criteria, 50, offset)
                .map(documents -> Pair.Companion.create(documents, nextFrom));
    }

    @Override
    DocumentSearchCriteria instantiateEmptyCriteria() {
        return new DocumentSearchCriteria("");
    }

    @Override
    boolean canSearch(DocumentSearchCriteria criteria) {
        return Utils.nonEmpty(criteria.getQuery());
    }
}