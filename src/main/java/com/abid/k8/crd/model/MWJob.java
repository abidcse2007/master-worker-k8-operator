package com.abid.k8.crd.model;

import lombok.Data;

import io.kubernetes.client.openapi.models.V1ObjectMeta;

@Data
public class MWJob {
    private String apiVersion;
    private String kind;
    private V1ObjectMeta metadata;
    private MWJobSpec mwJobSpec;
}
