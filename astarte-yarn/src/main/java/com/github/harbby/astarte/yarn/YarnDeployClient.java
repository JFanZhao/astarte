package com.github.harbby.astarte.yarn;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.api.records.ApplicationSubmissionContext;
import org.apache.hadoop.yarn.api.records.ContainerLaunchContext;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.client.api.YarnClientApplication;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.apache.hadoop.yarn.util.Records;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class YarnDeployClient
{
    public static final String YARN_APP_TYPE = "Astarte";

    public void deploy()
            throws IOException, YarnException
    {
        String hadoopConfDir = System.getProperty("HADOOP_CONF_DIR");
        Configuration configuration = loadHadoopConfig(hadoopConfDir);

        try (YarnClient yarnClient = initYarnClient(configuration)) {
            final YarnClientApplication yarnApplication = yarnClient.createApplication();

            ContainerLaunchContext amContainer = startAMContainer();
            // Setup CLASSPATH and environment variables for ApplicationMaster
            final Map<String, String> appMasterEnv = new HashMap<>();
            amContainer.setEnvironment(appMasterEnv);

            ApplicationSubmissionContext appContext = yarnApplication.getApplicationSubmissionContext();

            Resource capability = Records.newRecord(Resource.class);
            capability.setMemorySize(1024);
            capability.setVirtualCores(1);

            appContext.setResource(capability);
            appContext.setApplicationType(YARN_APP_TYPE);
            appContext.setAMContainerSpec(amContainer);

            yarnClient.submitApplication(appContext);
        }
    }

    private static ContainerLaunchContext startAMContainer()
    {
        ContainerLaunchContext amContainer = Records.newRecord(ContainerLaunchContext.class);
        final String amCommand = "/data/lib/jdk8/bin/java -cp ./:* " + AstarteYarnAppDriver.class.getName();
        amContainer.setCommands(Collections.singletonList(amCommand));
        return amContainer;
    }

    private static YarnClient initYarnClient(Configuration configuration)
    {
        YarnClient yarnClient = YarnClient.createYarnClient();
        yarnClient.init(configuration);
        yarnClient.start();
        return yarnClient;
    }

    private static Configuration loadHadoopConfig(String hadoopConfDir)
    {
        Configuration hadoopConf = new Configuration();
        //---create hadoop conf
        hadoopConf.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem");
        Stream.of("yarn-site.xml", "core-site.xml", "hdfs-site.xml").forEach(file -> {
            File site = new File(hadoopConfDir, file);
            if (site.exists() && site.isFile()) {
                hadoopConf.addResource(new org.apache.hadoop.fs.Path(site.toURI()));
            }
            else {
                throw new RuntimeException(site + " not exists");
            }
        });
        return hadoopConf;
    }
}
