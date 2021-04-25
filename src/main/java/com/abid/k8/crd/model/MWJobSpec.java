package com.abid.k8.crd.model;

import lombok.Data;

@Data
public class MWJobSpec {
    private MasterTemplate masterTemplate;
    private  WorkerTemplate workerTemplate;
}
