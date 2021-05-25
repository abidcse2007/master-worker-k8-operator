package com.abid.k8.crd.service;

import javax.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;

/***
 * Prints all pods. This is a dummy service to verify  connection with K8 cluster.
 * This should be disabled in production
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DummyPodService {
    private final CoreV1Api api;

    @PostConstruct
    public void init() throws ApiException {
        V1PodList list =
                api.listPodForAllNamespaces(null, null, null, null, null, null, null, null, null, null);
        for (V1Pod item : list.getItems()) {
            log.info(item.getMetadata().getName());
        }
    }
}
