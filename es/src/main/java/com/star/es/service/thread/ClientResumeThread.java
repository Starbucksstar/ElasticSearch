package com.star.es.service.thread;

import com.star.es.service.tool.ESTransportClient;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author gaoxing
 * @date 2018-09-19
 */
public class ClientResumeThread extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(ClientResumeThread.class);
    private ESTransportClient tclient;

    public ClientResumeThread(ESTransportClient tclient) {
        this.tclient = tclient;
    }

    @Override
    public void run() {
        while (true) {
            try {
                List<DiscoveryNode> list = this.tclient.buildClient();
                ((ClusterHealthResponse) this.tclient.getObject().admin().cluster().health(new ClusterHealthRequest()).actionGet()).status();
                ESTransportClient var1 = this.tclient;
                synchronized (this.tclient) {
                    this.tclient.setNormalWork(true);
                }
                String nodes = "";
                for (DiscoveryNode discoveryNode : list) {
                    nodes += discoveryNode.getAddress().getAddress() + ":" + discoveryNode.getAddress().getPort();
                }

                logger.info("已修复ES node " + nodes + ",已经加入集群");
                return;
            } catch (Exception e) {
                ESTransportClient var2 = this.tclient;
                synchronized (this.tclient) {
                    try {
                        logger.info("ES node " + this.tclient.getEsConfigBean().getClusterNodes() + " 正在修复中...");
                        this.tclient.wait(10000L);
                    } catch (Exception e1) {
                        logger.error("ES node is crash......");
                    }
                }
            }
        }
    }
}
