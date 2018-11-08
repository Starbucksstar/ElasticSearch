package com.star.es.service.tool;

import java.net.InetAddress;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.star.es.service.util.StringUtil;
import com.star.es.bean.ESConfigBean;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @author gaoxing
 * @date 2018-09-19
 */
@Component
public class ESTransportClient implements FactoryBean<TransportClient>, InitializingBean, DisposableBean {
    private static Logger logger = LoggerFactory.getLogger(ESTransportClient.class);
    @Autowired
    private ESConfigBean eSConfigBean;
    private TransportClient client;
    private boolean isNormalWork;

    public ESTransportClient() {

    }

    public List<DiscoveryNode> buildClient() throws Exception {
        logger.info("EsConfig info ={}", eSConfigBean.toString());
        this.client = new PreBuiltTransportClient(this.settings());
        Assert.hasText(eSConfigBean.getClusterNodes(), "[Assertion failed] clusterNodes settings missing.");
        String[] strings;
        int count = (strings = eSConfigBean.getClusterNodes().split(",")).length;
        for (int i = 0; i < count; i++) {
            String clusterNode = strings[i];
            String hostName = StringUtil.substringBeforeLast(clusterNode, ":");
            String port = StringUtil.substringAfterLast(clusterNode, ":");
            Assert.hasText(hostName, "[Assertion failed] missing host name in 'clusterNodes'");
            Assert.hasText(port, "[Assertion failed] missing port in 'clusterNodes'");
            logger.info("adding transport node : " + clusterNode);
            this.client.addTransportAddress(new TransportAddress(InetAddress.getByName(hostName), Integer.parseInt(port)));
        }
        //查看集群信息
        List<DiscoveryNode> connectedNodes = this.client.connectedNodes();
        for (DiscoveryNode discoveryNode : connectedNodes) {
            logger.info("集群内DiscoveryNode info ={}", JSON.toJSONString(discoveryNode));
        }
        this.isNormalWork = true;
        return connectedNodes;
    }

    private Settings settings() {
        return Settings.builder()
                .put("cluster.name", eSConfigBean.getClusterName())
                .put("client.transport.sniff", eSConfigBean.getClientTransportSniff())
                .put("client.transport.ignore_cluster_name", eSConfigBean.getClientIgnoreClusterName())
                .put("client.transport.ping_timeout", eSConfigBean.getClientPingTimeout())
                .put("client.transport.nodes_sampler_interval", eSConfigBean.getClientNodesSamplerInterval())
                .build();
    }

    @Override
    public void destroy() throws Exception {
        try {
            logger.info("Closing elasticSearch  client");
            if (this.client != null) {
                this.client.close();
            }
        } catch (Exception var2) {
            logger.error("Error closing ElasticSearch client: ", var2);
        }

    }

    @Override
    public TransportClient getObject() throws Exception {
        return this.client;
    }

    @Override
    public Class<TransportClient> getObjectType() {
        return TransportClient.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info("Build TransportClient is start.");
        this.buildClient();
    }

    public ESConfigBean getEsConfigBean() {
        return eSConfigBean;
    }

    public void setEsConfigBean(ESConfigBean eSConfigBean) {
        this.eSConfigBean = eSConfigBean;
    }

    public TransportClient getClient() {
        return client;
    }

    public void setClient(TransportClient client) {
        this.client = client;
    }

    public boolean isNormalWork() {
        return isNormalWork;
    }

    public void setNormalWork(boolean normalWork) {
        isNormalWork = normalWork;
    }

    @Override
    public String toString() {
        return "ESTransportClient{" +
                "clusterNodes='" + eSConfigBean.getClusterNodes() + '\'' +
                ", clusterName='" + eSConfigBean.getClusterName() + '\'' +
                ", clientTransportSniff=" + eSConfigBean.getClientTransportSniff() +
                ", clientIgnoreClusterName=" + eSConfigBean.getClientIgnoreClusterName() +
                ", clientPingTimeout='" + eSConfigBean.getClientPingTimeout() + '\'' +
                ", clientNodesSamplerInterval='" + eSConfigBean.getClientNodesSamplerInterval() + '\'' +
                ", client=" + client +
                ", isNormalWork=" + isNormalWork +
                '}';
    }

}
