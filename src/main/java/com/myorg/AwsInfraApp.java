package com.myorg;

import software.amazon.awscdk.App;

public class AwsInfraApp {
    public static void main(final String[] args) {
        App app = new App();

        VpcStack vpc = new VpcStack(app, "Vpc");
        ClusterStack cluster = new ClusterStack(app, "Cluster", vpc.getVpc());
        cluster.addDependency(vpc);

        RdsStack rdsStack = new RdsStack(app, "Rds", vpc.getVpc());
        rdsStack.addDependency(vpc);

        ServiceStack serviceStack = new ServiceStack(app, "Service", cluster.getCluster());
        serviceStack.addDependency(cluster);
        serviceStack.addDependency(rdsStack);

        app.synth();
    }
}

