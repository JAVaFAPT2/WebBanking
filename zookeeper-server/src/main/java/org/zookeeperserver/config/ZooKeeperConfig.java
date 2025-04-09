package org.zookeeperserver.config;


import org.apache.zookeeper.server.ServerConfig;
import org.apache.zookeeper.server.ZooKeeperServerMain;
import org.apache.zookeeper.server.admin.AdminServer;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ZooKeeperConfig {
    private static final Logger logger = LoggerFactory.getLogger(ZooKeeperConfig.class);

    @Value("${zookeeper.client-port:2181}")
    private int clientPort;

    @Value("${zookeeper.data-dir:./data/zookeeper}")
    private String dataDir;

    @Value("${zookeeper.tick-time:2000}")
    private int tickTime;

    private ExecutorService executor;
    private EmbeddedZooKeeper embeddedZooKeeper;

    @PostConstruct
    public void init() {
        try {
            Properties properties = new Properties();
            properties.setProperty("dataDir", dataDir);
            properties.setProperty("clientPort", String.valueOf(clientPort));
            properties.setProperty("tickTime", String.valueOf(tickTime));

            // Create data directory if it doesn't exist
            File dataDirFile = new File(dataDir);
            if (!dataDirFile.exists()) {
                dataDirFile.mkdirs();
            }

            QuorumPeerConfig quorumConfig = new QuorumPeerConfig();
            quorumConfig.parseProperties(properties);

            ServerConfig serverConfig = new ServerConfig();
            serverConfig.readFrom(quorumConfig);

            embeddedZooKeeper = new EmbeddedZooKeeper(serverConfig);
            executor = Executors.newSingleThreadExecutor();
            executor.submit(embeddedZooKeeper);

            logger.info("Started embedded ZooKeeper server on port {}", clientPort);
        } catch (Exception e) {
            logger.error("Failed to start embedded ZooKeeper server", e);
            throw new RuntimeException("Failed to start embedded ZooKeeper server", e);
        }
    }

    @PreDestroy
    public void destroy() {
        if (embeddedZooKeeper != null) {
            embeddedZooKeeper.shutdown();
        }
        if (executor != null) {
            executor.shutdown();
        }
        logger.info("Embedded ZooKeeper server stopped");
    }

    private static class EmbeddedZooKeeper implements Runnable {
        private final ServerConfig serverConfig;
        private ZooKeeperServerMain zooKeeperServer;
        private volatile boolean isRunning = false;

        public EmbeddedZooKeeper(ServerConfig serverConfig) {
            this.serverConfig = serverConfig;
        }

        @Override
        public void run() {
            try {
                isRunning = true;
                zooKeeperServer = new ZooKeeperServerMain();
                zooKeeperServer.runFromConfig(serverConfig);
            } catch (IOException | AdminServer.AdminServerException e) {
                if (isRunning) {
                    logger.error("ZooKeeper server encountered an error", e);
                } else {
                    logger.info("ZooKeeper server shutdown");
                }
            }
        }

        public void shutdown() {
            isRunning = false;
            if (zooKeeperServer != null) {
                try {
                    zooKeeperServer.close();
                } catch (Exception e) {
                    logger.warn("Error during ZooKeeper shutdown", e);
                }
            }
        }
    }
}
