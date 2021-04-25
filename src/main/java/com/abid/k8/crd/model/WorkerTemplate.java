package com.abid.k8.crd.model;

import lombok.Data;

import io.kubernetes.client.openapi.models.V1JobSpec;

@Data
public class WorkerTemplate {
    private V1JobSpec spec;
}
