package com.abid.k8.crd.runner;

import java.time.Duration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.abid.k8.crd.model.MWJob;
import com.abid.k8.crd.model.MWJobList;
import com.abid.k8.crd.reconciler.DefaultReconciler;
import io.kubernetes.client.extended.controller.Controller;
import io.kubernetes.client.extended.controller.ControllerManager;
import io.kubernetes.client.extended.controller.LeaderElectingController;
import io.kubernetes.client.extended.controller.builder.ControllerBuilder;
import io.kubernetes.client.extended.controller.reconciler.Reconciler;
import io.kubernetes.client.extended.controller.reconciler.Request;
import io.kubernetes.client.extended.leaderelection.LeaderElectionConfig;
import io.kubernetes.client.extended.leaderelection.LeaderElector;
import io.kubernetes.client.extended.leaderelection.resourcelock.EndpointsLock;
import io.kubernetes.client.informer.SharedIndexInformer;
import io.kubernetes.client.informer.SharedInformerFactory;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.BatchV1Api;
import io.kubernetes.client.openapi.models.V1Job;
import io.kubernetes.client.openapi.models.V1JobList;
import io.kubernetes.client.util.CallGeneratorParams;

@Slf4j
@RequiredArgsConstructor
@Component
public class DefaultRunner implements ApplicationRunner {
    private final ApiClient apiClient;
    private final BatchV1Api batchV1Api;
    private LeaderElectingController leaderElectingController;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Starting...");
        log.info("Resolving namespace");
        String podNamespace = System.getenv("POD_NAMESPACE");
        String namespace = podNamespace == null ? "default" : podNamespace;
        SharedInformerFactory informerFactory = new SharedInformerFactory(apiClient);
        log.info("Resolved namespace to={}", namespace);


        log.info("Initialising informer for V1Job");
        SharedIndexInformer<V1Job> jobInformer = null;
        try {
            jobInformer = informerFactory.sharedIndexInformerFor((CallGeneratorParams params) ->
                    batchV1Api.listNamespacedJobCall(namespace, null, false, null, null,
                            null, 100, params.resourceVersion, null, params.timeoutSeconds,
                            params.watch, null), V1Job.class, V1JobList.class);

        } catch (Exception ex) {
            log.error("Failed to initialize informer for V1Job due to={}", ex.getMessage(), ex);
            return;
        }

        log.info("Initialising informer for MWJob");
        SharedIndexInformer<MWJob> mwJobInformer = null;
        try {
            mwJobInformer = informerFactory.sharedIndexInformerFor((CallGeneratorParams params) ->
                    batchV1Api.listNamespacedJobCall(namespace, null, false, null, null,
                            null, 100, params.resourceVersion, null, params.timeoutSeconds,
                            params.watch, null), MWJob.class, MWJobList.class);

        } catch (Exception ex) {
            log.error("Failed to initialize informer for MWJob due to={}", ex.getMessage(), ex);
            return;
        }

        log.info("Starting all informers");
        informerFactory.startAllRegisteredInformers();

        log.info("Initialising reconciler for MWJob");
        Reconciler defaultReconciler = new DefaultReconciler(mwJobInformer);

        log.info("Initialising controller for V1Job");
        Controller v1JobController = ControllerBuilder
                .defaultBuilder(informerFactory)
                .watch(requestWorkQueue -> ControllerBuilder.controllerWatchBuilder(V1Job.class, requestWorkQueue)
                        .withWorkQueueKeyFunc((V1Job job) -> new Request(job.getMetadata().getNamespace(), job.getMetadata().getName()))
                        .withOnAddFilter((V1Job job) -> false)
                        .withOnDeleteFilter((V1Job job, Boolean stateUnknown) -> false)
                        .withOnUpdateFilter((V1Job oldJob, V1Job newJob) -> false)
                        .build()
                )
                .withReconciler(defaultReconciler)
                .withWorkerCount(2)
                .withReadyFunc(jobInformer::hasSynced)
                .build();


        log.info("Initialising controller for MWJob");
        Controller mwJobController = ControllerBuilder
                .defaultBuilder(informerFactory)
                .watch(requestWorkQueue -> ControllerBuilder.controllerWatchBuilder(MWJob.class, requestWorkQueue)
                        .withWorkQueueKeyFunc((MWJob job) -> new Request(job.getMetadata().getNamespace(), job.getMetadata().getName()))
                        .withOnAddFilter((MWJob job) -> false) //TODO  handle add
                        .withOnDeleteFilter((MWJob job, Boolean stateUnknown) -> false) //TODO handle delete
                        .withOnUpdateFilter((MWJob oldJob, MWJob newJob) -> false) //TODO handle update
                        .build()
                )
                .withReconciler(defaultReconciler)
                .withWorkerCount(2)
                .withReadyFunc(jobInformer::hasSynced)
                .build();

        log.info("Initialising controller manager");
        ControllerManager controllerManager = ControllerBuilder.controllerManagerBuilder(informerFactory)
                .addController(v1JobController)
                .addController(mwJobController)
                .build();

        log.info("Initialising LeaderElectingController");
        leaderElectingController = new LeaderElectingController(
                new LeaderElector(
                        new LeaderElectionConfig(new EndpointsLock(namespace, "leader-election", "mwJob"),
                                Duration.ofMillis(60000), Duration.ofMillis(8000), Duration.ofMillis(5000))
                ), controllerManager);
        leaderElectingController.run();
    }
}
