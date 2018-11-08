package com.star.es.bean;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
public class ESConfigBean {

    @Value("${cluster.nodes}")
    private String clusterNodes;
    @Value("${cluster.name}")
    private String clusterName;
    @Value("${client.transport.sniff}")
    private Boolean clientTransportSniff;
    @Value("${client.transport.ignore_cluster_name}")
    private Boolean clientIgnoreClusterName;
    @Value("${client.transport.ping_timeout}")
    private String clientPingTimeout;
    @Value("${client.transport.nodes_sampler_interval}")
    private String clientNodesSamplerInterval;

    public ESConfigBean() {
    }

    public String getClusterNodes() {
        return clusterNodes;
    }

    public void setClusterNodes(String clusterNodes) {
        this.clusterNodes = clusterNodes;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public Boolean getClientTransportSniff() {
        return clientTransportSniff;
    }

    public void setClientTransportSniff(Boolean clientTransportSniff) {
        this.clientTransportSniff = clientTransportSniff;
    }

    public Boolean getClientIgnoreClusterName() {
        return clientIgnoreClusterName;
    }

    public void setClientIgnoreClusterName(Boolean clientIgnoreClusterName) {
        this.clientIgnoreClusterName = clientIgnoreClusterName;
    }

    public String getClientPingTimeout() {
        return clientPingTimeout;
    }

    public void setClientPingTimeout(String clientPingTimeout) {
        this.clientPingTimeout = clientPingTimeout;
    }

    public String getClientNodesSamplerInterval() {
        return clientNodesSamplerInterval;
    }

    public void setClientNodesSamplerInterval(String clientNodesSamplerInterval) {
        this.clientNodesSamplerInterval = clientNodesSamplerInterval;
    }

    @Override
    public String toString() {
        return "ESConfigBean{" +
                "clusterNodes='" + clusterNodes + '\'' +
                ", clusterName='" + clusterName + '\'' +
                ", clientTransportSniff=" + clientTransportSniff +
                ", clientIgnoreClusterName=" + clientIgnoreClusterName +
                ", clientPingTimeout='" + clientPingTimeout + '\'' +
                ", clientNodesSamplerInterval='" + clientNodesSamplerInterval + '\'' +
                '}';
    }
}
