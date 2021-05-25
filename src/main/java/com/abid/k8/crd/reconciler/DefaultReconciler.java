package com.abid.k8.crd.reconciler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


import com.abid.k8.crd.model.MWJob;
import io.kubernetes.client.extended.controller.reconciler.Reconciler;
import io.kubernetes.client.extended.controller.reconciler.Request;
import io.kubernetes.client.extended.controller.reconciler.Result;
import io.kubernetes.client.informer.SharedIndexInformer;
import io.kubernetes.client.informer.cache.Lister;

@Slf4j
public class DefaultReconciler implements Reconciler {
    private Lister<MWJob> mwJobLister;

    public DefaultReconciler(SharedIndexInformer<MWJob> mwJobInformer) {
        this.mwJobLister = new Lister<MWJob>(mwJobInformer.getIndexer());
    }

    @Override
    public Result reconcile(Request request) {
        log.info("Reconciling MWJob for name={} namespace={}", request.getName(), request.getNamespace());
        return null;
    }
}
