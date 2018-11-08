package com.star.es.service.tool;

import java.io.IOException;
import java.net.ConnectException;
import java.util.*;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import com.star.es.constants.EsCommonReturnCode;
import com.star.es.exception.GtgjEsException;
import com.star.es.service.thread.ClientResumeThread;
import com.star.es.service.util.ObjectChangeUtil;
import org.apache.lucene.index.Term;
import org.elasticsearch.ElasticsearchTimeoutException;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeAction;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeRequestBuilder;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.sort.SortOrder;
import org.nlpcn.es4sql.SearchDao;
import org.nlpcn.es4sql.exception.SqlParseException;
import org.nlpcn.es4sql.query.SqlElasticSearchRequestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author gaoxing
 * @date 2018-09-19
 */
@Service
public class ElasticsearchToolService {
    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchToolService.class);
    @Autowired
    private ESTransportClient esTransportClient;

    public ElasticsearchToolService() {
    }

    /**
     * 创建带有IK分词的索引和type
     *
     * @param index
     * @param type
     * @param ikName
     * @param jsonObject
     */
    public void createIKIndexType(String index, String type, String[] ikName, JSONObject jsonObject) {
        try {
            XContentBuilder builder = createIKIndexAndMapping(ikName, jsonObject);
            TransportClient client = this.esTransportClient.getObject();
            if (!this.isIndexExist(index)) {
                //创建index
                client.admin().indices().prepareCreate(index).execute().actionGet();
            }
            //创建mapping
            PutMappingRequest putMappingRequest = Requests.putMappingRequest(index).type(type).source(builder);
            client.admin().indices().putMapping(putMappingRequest).actionGet();
        } catch (Exception e) {
            deleteIndex(index);
            try {
                doneException(esTransportClient, e);
            } catch (Exception e1) {
                throw new GtgjEsException(EsCommonReturnCode.ERR_ES_CREATE_IK_ERROR);
            }
        }
    }

    /**
     * 通过XcontentBuilder创建IK分词
     *
     * @param ikNames
     * @param jsonObject
     * @return
     */
    public XContentBuilder createIKIndexAndMapping(String[] ikNames, JSONObject jsonObject) {
        try {
            XContentBuilder builder = XContentFactory.jsonBuilder();
            builder.startObject();
            builder.startObject("properties");
            /**
             * 默认es文本会存储在_source中
             * store=true 独立存储某个字段：适用于经常搜索,频繁使用的场景
             *注意：独立存储的字段越多，索引越大，检索速度越慢
             *
             */
            for (Map.Entry<String, Object> set : jsonObject.entrySet()) {
                if (isEquals(ikNames, set.getKey())) {
                    builder.startObject(set.getKey()).field("type", set.getValue()).field("store", "true").field("analyzer", "ik_max_word").endObject(); //.field("search_analyzer", "ik_smart")
                } else{
                    builder.startObject(set.getKey()).field("type", set.getValue()).endObject();
                }
            }
            builder.endObject();
            builder.endObject();
            return builder;
        } catch (IOException e) {
            logger.error("ik创建mapping异常，异常信息={}", e.getMessage(), e);
            throw new GtgjEsException(EsCommonReturnCode.ERR_ES_CREATE_IK_ERROR, "Ik创建mapping异常");
        }
    }

    private boolean isEquals(String[] ikNames, String key) {
        for (int i = 0; i < ikNames.length; i++) {
            if (ikNames[i].toString().equals(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 单一添加---JSONObject
     * [已验证]
     *
     * @param index
     * @param indexType
     * @param primaryKey
     * @param jsonObj
     * @throws Exception
     */
    public void addJson(String index, String indexType, String primaryKey, JSONObject jsonObj) {
        try {
            TransportClient client = esTransportClient.getObject();
            this.volicationParam(index, indexType, primaryKey);
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            bulkRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);//立即更新数据
            IndexRequestBuilder lrb = client.prepareIndex(index, indexType, jsonObj.get(primaryKey).toString()).setSource(jsonObj);
            bulkRequest.add(lrb);
            BulkResponse bulkResponse = (BulkResponse) bulkRequest.execute().actionGet();
            if (bulkResponse.hasFailures()) {
                throw new InternalError("Elasticsearch新增数据失败 " + bulkResponse.buildFailureMessage());
            }
        } catch (Exception e) {
            try {
                this.doneException(esTransportClient, e);
            } catch (Exception e1) {
                throw new GtgjEsException(EsCommonReturnCode.ERR_ES_ADD);
            }
        }
    }

    /**
     * 添加单条数据
     * 自动生成ID主键
     * @param index
     * @param indexType
     * @param jsonObject
     */
    public void addJson(String index,String indexType,JSONObject jsonObject){
        try {
            TransportClient client = esTransportClient.getObject();
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            bulkRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);//立即更新数据
            IndexRequestBuilder lrb = client.prepareIndex(index,indexType).setSource(jsonObject.toJSONString(), XContentType.JSON);
            bulkRequest.add(lrb);
            BulkResponse bulkResponse = (BulkResponse) bulkRequest.execute().actionGet();
            if (bulkResponse.hasFailures()) {
                throw new InternalError("Elasticsearch新增数据失败 " + bulkResponse.buildFailureMessage());
            }
        } catch (Exception e) {
            try {
                this.doneException(esTransportClient, e);
            } catch (Exception e1) {
                throw new GtgjEsException(EsCommonReturnCode.ERR_ES_ADD);
            }
        }
    }

    /**
     * 批量添加--List<JSONObject>
     * [已验证]
     *
     * @param index
     * @param indexType
     * @param primaryKey
     * @param listjson
     * @throws Exception
     */
    public void addListJson(String index, String indexType, String primaryKey, List<JSONObject> listjson) {
        try {
            TransportClient client = esTransportClient.getObject();
            this.volicationParam(index, indexType, primaryKey);
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            bulkRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);//立即更新数据
            Iterator iterator = listjson.iterator();

            while (iterator.hasNext()) {
                JSONObject jsonObject = (JSONObject) iterator.next();
                IndexRequestBuilder lrb = client.prepareIndex(index, indexType, jsonObject.get(primaryKey).toString()).setSource(jsonObject);
                bulkRequest.add(lrb);
            }
            BulkResponse bulkResponse = (BulkResponse) bulkRequest.execute().actionGet();
            if (bulkResponse.hasFailures()) {
                throw new InternalError("Elasticsearch新增数据失败 " + bulkResponse.buildFailureMessage());
            }
        } catch (Exception e) {
            try {
                this.doneException(esTransportClient, e);
            } catch (Exception e1) {
                throw new GtgjEsException(EsCommonReturnCode.ERR_ES_ADD);
            }
        }
    }

    /**
     * 分页查询--结果集为Map<String,Object>
     * [已验证]
     *
     * @param index
     * @param indexType
     * @param currentPage
     * @param pageSize
     * @param qb
     * @return
     * @throws Exception
     */
    public Map<String, Object> queryListPage(String index, String indexType, int currentPage, int pageSize, QueryBuilder qb) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        List<JSONObject> list = new ArrayList<JSONObject>();
        long totalCount = 0L;
        try {
            TransportClient client = esTransportClient.getObject();
            this.volicationParam(index, indexType);
            SearchResponse response = (SearchResponse) client.prepareSearch(new String[]{index}).setTypes(new String[]{indexType}).setQuery(qb).setFrom((currentPage - 1) * pageSize).setSize(pageSize).execute().actionGet();
            SearchHits hits = response.getHits();
            totalCount = hits.getTotalHits();
            Iterator iterator = hits.iterator();
            while (iterator.hasNext()) {
                SearchHit searchHit = (SearchHit) iterator.next();
                list.add((JSONObject) JSONObject.toJSON(searchHit.getSourceAsMap()));
            }
            resultMap.put("dataList", list);
            resultMap.put("totalCount", totalCount);
        } catch (Exception e) {
            try {
                this.doneException(esTransportClient, e);
            } catch (Exception e1) {
                throw new GtgjEsException(EsCommonReturnCode.ERR_ES_QUERY);
            }
        }
        return resultMap;
    }

    /**
     * 通过索引和类型查询ES数据
     * [已验证]
     *
     * @param index     索引
     * @param indexType 类型
     * @param size      返回结果数
     * @return
     * @throws Exception
     */
    public List<JSONObject> queryListJsonByIndexType(String index, String indexType, int size) {
        List<JSONObject> list = new ArrayList<JSONObject>();
        try {
            TransportClient client = esTransportClient.getObject();
            this.volicationParam(index, indexType);
            SearchResponse response = (SearchResponse) client.prepareSearch(new String[]{index}).setTypes(new String[]{indexType}).setSearchType(SearchType.QUERY_THEN_FETCH).setSize(size).setScroll(TimeValue.timeValueMinutes(2L)).setExplain(true).execute().actionGet();
            SearchHits hits = response.getHits();
            Iterator iterator = hits.iterator();
            while (iterator.hasNext()) {
                SearchHit searchHit = (SearchHit) iterator.next();
                list.add((JSONObject) JSONObject.toJSON(searchHit.getSourceAsMap()));
            }
        } catch (Exception e) {
            try {
                this.doneException(esTransportClient, e);
            } catch (Exception e1) {
                throw new GtgjEsException(EsCommonReturnCode.ERR_ES_QUERY);
            }
        }
        return list;
    }

    /**
     * 通过QueryBuilder查询指定条件的数据
     * [已验证]
     * 注意:ES默认分词工具是只支持英文，如果需要中文模糊查询，必须安装ik分词插件
     *
     * @param index
     * @param indexType
     * @param qb           -- MatchAllQueryBuilder、WildcardQueryBuilder、BooleanQueryBuilder等
     * @param size--搜索结果大小
     * @return
     * @throws Exception
     */
    public List<JSONObject> queryListJsonByQb(String index, String indexType, QueryBuilder qb, int size) {
        List<JSONObject> list = new ArrayList<JSONObject>();
        try {
            TransportClient client = esTransportClient.getObject();
            this.volicationParam(index, indexType);
            SearchResponse response = (SearchResponse) client.prepareSearch(new String[]{index}).setTypes(new String[]{indexType}).setQuery(qb).setSearchType(SearchType.QUERY_THEN_FETCH).setSize(size).setScroll(TimeValue.timeValueMinutes(2L)).setExplain(true).execute().actionGet();
            SearchHits hits = response.getHits();
            Iterator iterator = hits.iterator();
            while (iterator.hasNext()) {
                SearchHit searchHit = (SearchHit) iterator.next();
                list.add((JSONObject) JSONObject.toJSON(searchHit.getSourceAsMap()));
            }
        } catch (Exception e) {
            try {
                this.doneException(esTransportClient, e);
            } catch (Exception e1) {
                throw new GtgjEsException(EsCommonReturnCode.ERR_ES_QUERY);
            }
        }
        return list;
    }


    /**
     * 通过QueryBuilder实现--多个条件查询
     * [已验证]
     *
     * @param index
     * @param indexType
     * @param size--搜索结果大小
     * @return
     * @throws Exception
     * @Param map 例如；key--content  value--*晚点*
     */
    public List<JSONObject> queryListJsonByMoreQb(String index, String indexType, Map<String, String> map, int size) {
        List<JSONObject> list = new ArrayList<JSONObject>();
        try {
            TransportClient client = esTransportClient.getObject();
            BoolQueryBuilder booleanQueryBuilder = QueryBuilders.boolQuery();
            for (Map.Entry<String,String> in : map.entrySet()) {
                booleanQueryBuilder.must(QueryBuilders.wildcardQuery(in.getKey(), in.getValue()));
            }
            this.volicationParam(index, indexType);
            SearchResponse response = (SearchResponse) client.prepareSearch(new String[]{index}).setTypes(new String[]{indexType}).setQuery(booleanQueryBuilder).setSearchType(SearchType.QUERY_THEN_FETCH).setSize(size).setScroll(TimeValue.timeValueMinutes(2L)).setExplain(true).execute().actionGet();
            SearchHits hits = response.getHits();
            Iterator iterator = hits.iterator();
            while (iterator.hasNext()) {
                SearchHit searchHit = (SearchHit) iterator.next();
                list.add((JSONObject) JSONObject.toJSON(searchHit.getSourceAsMap()));
            }
        } catch (Exception e) {
            try {
                this.doneException(esTransportClient, e);
            } catch (Exception e1) {
                throw new GtgjEsException(EsCommonReturnCode.ERR_ES_QUERY);
            }
        }
        return list;
    }


    /**
     * 查询分页+模糊搜索+排序
     *
     * @param index
     * @param indexType
     * @param qb
     * @param pageNow
     * @param pageSize
     * @param sortName
     * @param sortOrder
     * @return
     * @throws Exception
     */
    public List<JSONObject> queryPageListFuzzyQb(String index, String indexType, QueryBuilder qb, int pageNow, int pageSize, String sortName, SortOrder sortOrder) {
        List<JSONObject> list = new ArrayList<JSONObject>();
        try {
            TransportClient client = esTransportClient.getObject();
            this.volicationParam(index, indexType);
            SearchResponse response = (SearchResponse) client.prepareSearch(new String[]{index}).setTypes(new String[]{indexType}).setQuery(qb).setFrom((pageNow - 1) * pageSize).setSize(pageSize).addSort(sortName, sortOrder).execute().actionGet();
            SearchHits hits = response.getHits();
            Iterator iterator = hits.iterator();

            while (iterator.hasNext()) {
                SearchHit searchHit = (SearchHit) iterator.next();
                list.add((JSONObject) JSONObject.toJSON(searchHit.getSourceAsMap()));
            }
        } catch (Exception e) {
            try {
                this.doneException(esTransportClient, e);
            } catch (Exception e1) {
                throw new GtgjEsException(EsCommonReturnCode.ERR_ES_QUERY);
            }
        }
        return list;
    }


    /**
     * 通过qb排序条件查询
     * [已验证]
     *
     * @param index
     * @param indexType
     * @param qb
     * @param sortName
     * @return
     * @throws Exception 【ES官方文档说明】--》异常：Fielddata is disabled on text fields by default
     *                   在脚本中排序、聚合和访问字段值需要与搜索不同的访问模式
     *                   大多数字段可以使用索引时间、磁盘上的doc_values，但是文本字段不支持doc_values
     *                   在操作Sort等聚合函数之前，在head中使用
     *                   http://localhost:9200/social/_mapping/comment/  【put】
     *                   {
     *                   "properties": {
     *                   "commentId": {
     *                   "type": "text",
     *                   "fielddata": true
     *                   }
     *                   }
     *                   }
     *                   需要先将 text field 的 fielddata 属性设置为 true：
     */
    public List<JSONObject> queryPointsListBySortQb(String index, String indexType, QueryBuilder qb, int size, Map<String, SortOrder> sortOrderMap) {
        List<JSONObject> list = new ArrayList<JSONObject>();
        try {
            TransportClient client = esTransportClient.getObject();
            this.volicationParam(index, indexType);
            SearchRequestBuilder searchRequestBuilder = client.prepareSearch(new String[]{index});
            searchRequestBuilder.setTypes(new String[]{indexType}).setQuery(qb).setSize(size);
            for (Map.Entry<String, SortOrder> map : sortOrderMap.entrySet()) {
                searchRequestBuilder.addSort(map.getKey(), map.getValue());
            }
            SearchResponse response = (SearchResponse) searchRequestBuilder.execute().actionGet();
            SearchHits hits = response.getHits();
            Iterator iterator = hits.iterator();

            while (iterator.hasNext()) {
                SearchHit searchHit = (SearchHit) iterator.next();
                list.add((JSONObject) JSONObject.toJSON(searchHit.getSourceAsMap()));
            }
        } catch (Exception e) {
            try {
                this.doneException(esTransportClient, e);
            } catch (Exception e1) {
                throw new GtgjEsException(EsCommonReturnCode.ERR_ES_QUERY);
            }
        }

        return list;
    }

    /**
     * 组合查询+分页
     * @param index
     * @param indexType
     * @param qb
     * @param pageNow
     * @param pageSize
     * @param sortOrderMap
     * @return
     */
    public List<JSONObject> queryListByAssembleQb(String index, String indexType, QueryBuilder qb, int pageNow, int pageSize, Map<String, SortOrder> sortOrderMap) {
        List<JSONObject> list = new ArrayList<JSONObject>();
        try {
            TransportClient client = esTransportClient.getObject();
            this.volicationParam(index, indexType);
            if(pageNow <=0){
                pageNow = 1;
            }
            SearchRequestBuilder searchRequestBuilder = client.prepareSearch(new String[]{index});
            searchRequestBuilder.setTypes(new String[]{indexType}).setQuery(qb).setFrom((pageNow-1)*pageSize).setSize(pageSize);
            for (Map.Entry<String, SortOrder> map : sortOrderMap.entrySet()) {
                searchRequestBuilder.addSort(map.getKey(), map.getValue());
            }
            SearchResponse response = (SearchResponse) searchRequestBuilder.execute().actionGet();
            SearchHits hits = response.getHits();
            Iterator iterator = hits.iterator();

            while (iterator.hasNext()) {
                SearchHit searchHit = (SearchHit) iterator.next();
                list.add((JSONObject) JSONObject.toJSON(searchHit.getSourceAsMap()));
            }
        } catch (Exception e) {
            try {
                this.doneException(esTransportClient, e);
            } catch (Exception e1) {
                throw new GtgjEsException(EsCommonReturnCode.ERR_ES_QUERY);
            }
        }

        return list;
    }

    /**
     * 查询到索引和类型在ES中总数--[命中数]
     * [已验证]
     *
     * @param index
     * @param indexType
     * @param qb
     * @return
     * @throws Exception
     */
    public long queryCountByQb(String index, String indexType, QueryBuilder qb) {
        try {
            TransportClient client = esTransportClient.getObject();
            this.volicationParam(index, indexType);
            SearchResponse response = (SearchResponse) client.prepareSearch(new String[]{index}).setTypes(new String[]{indexType}).setQuery(qb).setExplain(true).execute().actionGet();
            SearchHits hits = response.getHits();
            return hits.getTotalHits();
        } catch (Exception e) {
            try {
                this.doneException(esTransportClient, e);
            } catch (Exception e1) {
                throw new GtgjEsException(EsCommonReturnCode.ERR_ES_QUERY);
            }
            return 0L;
        }
    }

    /**
     * 聚合查询
     * AbstractAggregationBuilder聚合语句
     * 聚合方式如下：
     * Metrics Aggregations
     * Bucket Aggregations
     * Pipeline Aggregations
     * Matrix Aggregations
     *
     * @param index
     * @param indexType
     * @param qb
     * @param aggregation
     * @return
     * @throws Exception
     */
    public Aggregations queryAggregation(String index, String indexType, QueryBuilder qb, AbstractAggregationBuilder aggregation) {
        try {
            TransportClient client = esTransportClient.getObject();
            this.volicationParam(index, indexType);
            SearchResponse response = (SearchResponse) client.prepareSearch(new String[]{index}).setTypes(new String[]{indexType}).setQuery(qb).addAggregation(aggregation).setExplain(true).execute().actionGet();
            return response.getAggregations();
        } catch (Exception e) {
            try {
                this.doneException(esTransportClient, e);
            } catch (Exception e1) {
                throw new GtgjEsException(EsCommonReturnCode.ERR_ES_AGGREGATIONS_QUERY);
            }
            return null;
        }
    }

    /**
     * 通过index indexType id查询记录
     * [已验证]
     *
     * @param index
     * @param indexType
     * @param id
     * @return
     * @throws Exception
     */
    public JSONObject queryById(String index, String indexType, String id) {
        try {
            TransportClient client = esTransportClient.getObject();
            this.volicationParam(index, indexType);
            GetResponse response = (GetResponse) client.prepareGet(index, indexType, id).get();
            JSONObject jsonObject = (JSONObject) JSONObject.toJSON(response.getSource());
            return jsonObject;
        } catch (Exception e) {
            try {
                this.doneException(esTransportClient, e);
            } catch (Exception e1) {
                throw new GtgjEsException(EsCommonReturnCode.ERR_ES_QUERY);
            }
            return null;
        }
    }

    /**
     * 通过index、indexType、id删除记录
     * [已验证]
     *
     * @param index
     * @param indexType
     * @param id
     * @return
     * @throws Exception
     */
    public boolean deleteById(String index, String indexType, String id) {
        try {
            TransportClient client = esTransportClient.getObject();
            DeleteResponse response = (DeleteResponse) client.prepareDelete(index, indexType, id).execute().actionGet();
            response.setForcedRefresh(true);//立即更新数据
            if (response.getResult().getOp() == 2) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            try {
                this.doneException(esTransportClient, e);
            } catch (Exception e1) {
                throw new GtgjEsException(EsCommonReturnCode.ERR_ES_DELETE);
            }
            return false;
        }
    }


    /**
     * 获取ES健康状态
     *
     * @return
     */
    public boolean checkEsStatus() {
        boolean result = false;
        try {
            ((ClusterHealthResponse) esTransportClient.getObject().admin().cluster().health(new ClusterHealthRequest()).actionGet()).status();
            result = true;
        } catch (Exception e) {
            logger.error("获取Es集群健康状态异常，异常信息={}", e.getMessage(), e);
        }
        return result;
    }

    /**
     * 校验入参
     *
     * @param index
     * @param indexType
     * @param primaryKey
     */
    private void volicationParam(String index, String indexType, String primaryKey) {
        this.volicationParam(index, indexType);
        if (primaryKey == null || primaryKey.equals("")) {
            throw new GtgjEsException(EsCommonReturnCode.ERR_INPUT_VALIDATION_REJECTED, "入参primaryKey不能为空 ");
        }
    }

    private void volicationParam(String index, String indexType) {
        if (index != null && !index.equals("")) {
            if (indexType == null || indexType.equals("")) {
                throw new GtgjEsException(EsCommonReturnCode.ERR_INPUT_VALIDATION_REJECTED, "入参indexType不能为空 ");
            }
        } else {
            throw new GtgjEsException(EsCommonReturnCode.ERR_INPUT_VALIDATION_REJECTED, "入参index不能为空 ");
        }
    }

    /**
     * es-sql查询
     *
     * @param client
     * @param sql
     * @return
     * @throws SqlParseException
     */
    private SearchHits querySearchHitsBySql(Client client, String sql) {
        try {
            SearchDao searchDao = new SearchDao(client);
            SqlElasticSearchRequestBuilder select = (SqlElasticSearchRequestBuilder) searchDao.explain(sql).explain();
            return ((SearchResponse) select.get()).getHits();
        } catch (Exception e) {
            logger.error("es-sql查询搜索结果命中集异常,异常信息={}", e.getMessage(), e);
        }
        return null;
    }

    /**
     * 获取Aggregations
     *
     * @param client
     * @param query
     * @return
     * @throws SqlParseException
     */
    private Aggregations queryAggregations(Client client, String query) throws Exception {
        SearchDao searchDao = new SearchDao(client);
        SqlElasticSearchRequestBuilder select = (SqlElasticSearchRequestBuilder) searchDao.explain(query).explain();
        return ((SearchResponse) select.get()).getAggregations();
    }

    /**
     * 通过sql查询aggregations
     * @param sql
     * @return
     */
    public List<JSONObject> queryAggregations(String sql,String types){
        List<JSONObject>  list = new ArrayList<JSONObject>();
        Client client = null;
        try
        {
            client = esTransportClient.getObject();
            Aggregations aggregations= queryAggregations(client,sql);
            Terms terms = aggregations.get(types);
            if(terms == null){
            	return null;
			}
            //获取桶聚合的结果
            List<? extends Terms.Bucket> buckets = terms.getBuckets();
            for(Terms.Bucket bucket:buckets){
                JSONObject  jsonObject  = new JSONObject();
                //获取模糊分组查询结果
                jsonObject.put(types,bucket.getKey());
                jsonObject.put("count",bucket.getDocCount());
                list.add(jsonObject);
            }
        }
        catch (Exception e)
        {
            try {
                this.doneException(esTransportClient, e);
            } catch (Exception e1) {
                throw new GtgjEsException(EsCommonReturnCode.ERR_ES_QUERY);
            }
        }
        return list;
    }


    /**
     * 通过sql查询命中数
     * @param sql
     * @return
     */
    public long queryCountBySql(String sql){
        long count = 0L;
        try
        {
            Client client = esTransportClient.getObject();
            SearchHits searchHits = this.querySearchHitsBySql(client,sql);
            if(searchHits  !=  null){
                count = searchHits.getTotalHits();
            }
        }catch (Exception e){
            try {
                this.doneException(esTransportClient, e);
            } catch (Exception e1) {
                throw new GtgjEsException(EsCommonReturnCode.ERR_ES_QUERY);
            }
        }
        return count;
    }


    /**
     * 通过es-sql 查询记录集合
     *
     * @param sql
     * @return
     */
    public JSONObject queryList(String sql) {
		JSONObject jsonObject = new JSONObject();
        ArrayList list = new ArrayList();
        try {
            Client client = esTransportClient.getObject();
            SearchHits hits = this.querySearchHitsBySql(client, sql);
            jsonObject.put("listSize",hits.totalHits);
            Iterator iterator = hits.iterator();

            while (iterator.hasNext()) {
                SearchHit searchHit = (SearchHit) iterator.next();
                list.add(JSONObject.toJSON(searchHit.getSourceAsMap()));
            }
			jsonObject.put("list",list);

        } catch (Exception e) {
            try {
                this.doneException(esTransportClient, e);
            } catch (Exception e1) {
                throw new GtgjEsException(EsCommonReturnCode.ERR_ES_QUERY);
            }
        }
        return jsonObject;
    }

    /**
     * 异常修复
     *
     * @param esTransportClient
     * @param e
     */
    private void doneException(ESTransportClient esTransportClient, Exception e) throws Exception {
        try {
            if (e instanceof NoNodeAvailableException || e instanceof ElasticsearchTimeoutException || e instanceof ConnectException) {
                synchronized (esTransportClient) {
                    if (esTransportClient.isNormalWork()) {
                        esTransportClient.setNormalWork(false);
                        (new ClientResumeThread(esTransportClient)).start();
                        logger.info("ES集群节点 " + esTransportClient.getEsConfigBean().getClusterNodes() + " 出现异常：" + e.toString() + " , 修复线程已启动");
                    }
                }
            }
        } catch (Exception e1) {
            logger.error("修复Es集群节点连接异常，异常信息={}", e1.getMessage(), e1);
            throw new GtgjEsException(EsCommonReturnCode.ERR_ES_UNKNOW_ERROR, e1.getMessage());
        }

        if (e instanceof NoNodeAvailableException) {
            throw new GtgjEsException(EsCommonReturnCode.ERR_ES_NO_NODE_AVAILABLE);
        } else if (e instanceof ElasticsearchTimeoutException) {
            throw new GtgjEsException(EsCommonReturnCode.ERR_ES_TIME_OUT);
        } else if (e instanceof ConnectException) {
            throw new GtgjEsException(EsCommonReturnCode.ERR_ES_CONNECT_ERROR);
        } else {
            throw e;
        }
    }


    /**
     * 判断索引是否存在
     *
     * @param index
     * @return
     * @throws Exception
     */
    public boolean isIndexExist(String index) throws Exception {
        try {
            return esTransportClient.getObject().admin().indices().prepareExists(index).execute().actionGet().isExists();
        } catch (Exception e) {
            doneException(esTransportClient, e);
        }
        return false;
    }

    /**
     * 判断类型是否存在
     *
     * @param index
     * @param type
     * @return
     */
    public boolean isTypeExist(String index, String type) throws Exception {
        try {
            return isIndexExist(index)
                    ? esTransportClient.getObject().admin().indices().prepareTypesExists(index).setTypes(type).execute().actionGet().isExists()
                    : false;
        } catch (Exception e) {
            doneException(esTransportClient, e);
        }
        return false;
    }


    /**
     * 批量删除数据
     * 通过index+type-->查询所有id,通过id删除
     * [已验证]
     *
     * @param index
     * @param type
     * @param size
     * @return
     * @throws Exception
     */
    public boolean deleteBulkData(String index, String type, int size){
        try {
            if (isTypeExist(index, type)) {
                BulkRequestBuilder bulkRequest = this.esTransportClient.getObject().prepareBulk();
                SearchResponse response = this.esTransportClient.getObject().prepareSearch(index).setTypes(type)
                        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                        .setFrom(0).setSize(size).setExplain(true).execute().actionGet();
                for (SearchHit hit : response.getHits()) {
                    String id = hit.getId();
                    bulkRequest.add(this.esTransportClient.getObject().prepareDelete(index, type, id).request());
                }
                BulkResponse bulkResponse = bulkRequest.get();
                if (bulkResponse.hasFailures()) {
                    for (BulkItemResponse item : bulkResponse.getItems()) {
                        logger.error(item.getFailureMessage());
                    }
                    throw new InternalError("Elasticsearch批量删除数据失败 " + bulkResponse.buildFailureMessage());
                } else {
                    logger.info("Elasticsearch批量删除数据成功");
                    return true;
                }
            }
        } catch (Exception e) {
            try {
                doneException(esTransportClient, e);
            } catch (Exception e1) {
                if (e1 instanceof GtgjEsException) {
                    throw new GtgjEsException(((GtgjEsException) e1).getEsReturnCode());
                } else {
                    throw new GtgjEsException(e1.getMessage());
                }
            }
        }
        return true;
    }

    /**
     * 删除索引--[生产勿用]
     * [已验证]
     *
     * @param index
     * @return
     */
    public boolean deleteIndex(String index) {
        try {
            IndicesExistsRequest inExistsRequest = new IndicesExistsRequest(index);
            TransportClient client = this.esTransportClient.getObject();

            IndicesExistsResponse inExistsResponse = client.admin().indices().exists(inExistsRequest).actionGet();
            if (inExistsResponse.isExists()) {
                DeleteIndexResponse dResponse = client.admin().indices().prepareDelete(index).execute().actionGet();
                if (dResponse.isAcknowledged()) {
                    logger.info("Delete the index name=" + index + " is success!");
                    return true;
                } else {
                    logger.info("Delete the index name=" + index + " is fail!");
                    return false;
                }
            } else {
                logger.info("The index is not exists!");
                throw new GtgjEsException(EsCommonReturnCode.ERR_ES_DELETE);
            }
        } catch (Exception e) {
            try {
                doneException(esTransportClient, e);
            } catch (Exception e1) {
                if (e1 instanceof GtgjEsException) {
                    throw new GtgjEsException(((GtgjEsException) e1).getEsReturnCode());
                } else {
                    throw new GtgjEsException(e1.getMessage());
                }
            }
        }
        return false;
    }

    /**
     * 获取IK分词结果
     *
     * @param indexName 索引名
     * @param text      --需要分词的文本
     * @param type      1--ik_smart 最细粒度
     *                  2--ik_max_word  最粗粒度
     * @return
     */
    public List<String> queryAnalyzeResult(String indexName, String text, int type) {
        List<String> result = new ArrayList<String>();
        TransportClient client = null;
        try {
            client = this.esTransportClient.getObject();
            IndicesAdminClient indicesAdminClient = client.admin().indices();
            AnalyzeRequestBuilder requestBuilder = new AnalyzeRequestBuilder(indicesAdminClient, AnalyzeAction.INSTANCE, indexName, text);
            switch (type) {
                case 1:
                    requestBuilder.setAnalyzer("ik_smart");
                    break;
                case 2:
                    requestBuilder.setAnalyzer("ik_max_word");
                    break;
                default:
                    requestBuilder.setAnalyzer("ik_smart");
                    break;
            }
            List<AnalyzeResponse.AnalyzeToken> list = requestBuilder.execute().actionGet().getTokens();
            for (AnalyzeResponse.AnalyzeToken analyzeToken : list) {
                result.add(analyzeToken.getTerm());
            }
        } catch (Exception e) {
            try {
                doneException(esTransportClient, e);
            } catch (Exception e1) {
                throw new GtgjEsException(EsCommonReturnCode.ERR_ES_IK_RESULT_ERROR);
            }
        }
        return result;
    }

}
