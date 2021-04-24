package com.abid.k8.crd.config;

import java.io.FileReader;
import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.kubernetes.client.extended.controller.Controller;
import io.kubernetes.client.extended.controller.ControllerManager;
import io.kubernetes.client.extended.controller.builder.ControllerBuilder;
import io.kubernetes.client.extended.controller.reconciler.Reconciler;
import io.kubernetes.client.extended.controller.reconciler.Request;
import io.kubernetes.client.extended.controller.reconciler.Result;
import io.kubernetes.client.informer.SharedInformerFactory;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.CoreApi;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.KubeConfig;

@Configuration
public class K8Config {

    //String kubeConfigPath = System.getenv("HOME") + "/.kube/config";
    String kubeConfigPath = System.getenv("HOME") + "/.kube/config";

    @Bean
    public ApiClient apiClient() throws IOException {
        //return Config.defaultClient();
        return ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
    }


    @Bean
    public CoreV1Api coreV1Api() throws IOException {
        io.kubernetes.client.openapi.Configuration.setDefaultApiClient(apiClient());
        return new CoreV1Api();
    }

}
