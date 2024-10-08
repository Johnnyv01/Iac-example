package com.myorg;

import software.amazon.awscdk.Fn;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ecs.Cluster;
import software.amazon.awscdk.services.ecs.ContainerImage;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck;
import software.constructs.Construct;

import java.util.HashMap;
import java.util.Map;

import static software.amazon.awscdk.Duration.seconds;

public class ServiceStack extends Stack {

    public ServiceStack(final Construct scope, final String id, final Cluster cluster) {
        this(scope, id, null, cluster);
    }

    public ServiceStack(final Construct scope, final String id, final StackProps props, Cluster cluster) {
        super(scope, id, props);

        Map<String, String> autenticacao = new HashMap<>();
        autenticacao.put("SPRING_DATASOURCE_URL", "jdbc:mysql://" + Fn.importValue("pedidos-db-endpoint") + ":3306/alurafood-pedidos?createDatabaseIfNotExist=true");
        autenticacao.put("SPRING_DATASOURCE_USERNAME", "admin");
        autenticacao.put("SPRING_DATASOURCE_PASSWORD", Fn.importValue("pedidos-db-senha"));


        // Create a load-balanced Fargate service and make it public
        ApplicationLoadBalancedFargateService service = ApplicationLoadBalancedFargateService.Builder
                .create(this, "AluraService")
                .serviceName("alura-service-ola")
                .cluster(cluster) // Required
                .cpu(512) // Default is 256
                .desiredCount(1) // Default is 1
                .listenerPort(8080)
                .assignPublicIp(true)
                .taskImageOptions(
                        ApplicationLoadBalancedTaskImageOptions.builder()
                                .image(ContainerImage.fromRegistry("jacquelineoliveira/pedidos-ms"))
                                .containerPort(8080)
                                .containerName("app_ola")
                                .environment(autenticacao)
                                .build())
                .memoryLimitMiB(1024) // Default is 512
                .publicLoadBalancer(true) // Default is true
                .build();

        // Configure health check
        service.getTargetGroup().configureHealthCheck(HealthCheck.builder()
                .port("8080")
                .path("/pedidos")
                .interval(seconds(30))
                .healthyHttpCodes("200-499")
                .unhealthyThresholdCount(2)
                .build());

    }
}
