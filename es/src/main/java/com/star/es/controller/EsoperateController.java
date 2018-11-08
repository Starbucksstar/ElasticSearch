package com.star.es.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.star.es.dto.input.EsIkCreateInputDto;
import com.star.es.dto.output.CommonOutPutDto;
import com.star.es.dto.output.SimpleOutputDto;
import com.star.es.service.tool.ElasticsearchToolService;
import com.star.es.dto.input.EsAddInputDto;
import com.star.es.exception.GtgjEsException;
import org.apache.lucene.queryparser.xml.builders.BooleanQueryBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.WildcardQueryBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author gaoxing
 * @date 2018-09-19
 */

@RestController
@RequestMapping("/es")
public class EsoperateController {
    private Logger logger = LoggerFactory.getLogger(EsoperateController.class);
    @Autowired
    private ElasticsearchToolService elasticsearchToolService;

    @RequestMapping(value = "/gtgj/ikCreate", method = RequestMethod.POST)
    public Object ikCreate(@RequestBody @Valid JSONObject object) {
        EsIkCreateInputDto inputDto = validateEsIkInputDto(object);
        elasticsearchToolService.createIKIndexType(inputDto.getIndex(), inputDto.getIndexType(), inputDto.getIkNames(), inputDto.getValue());
        return new SimpleOutputDto("创建ik分词成功");
    }

    /**
     * 添加单条数据
     * 有主键
     * @param object
     * @return
     */
    @RequestMapping(value = "/gtgj/addJson", method = RequestMethod.POST)
    public Object addJson(@RequestBody @Valid JSONObject object) {
        EsAddInputDto inputDto = validateAddOneInputDTO(object);
        elasticsearchToolService.addJson(inputDto.getIndex(), inputDto.getIndexType(), inputDto.getPrimaryKey(), object.getJSONObject("value"));
        return new SimpleOutputDto("添加单条数据成功");
    }

    /**
     * 添加单条数据
     * 无主键
     * @param object
     * @return
     */
    @RequestMapping(value = "/gtgj/addData", method = RequestMethod.POST)
    public Object addData(@RequestBody @Valid JSONObject object){
        EsAddInputDto inputDto = validateAddDataInputDTO(object);
        elasticsearchToolService.addJson(inputDto.getIndex(),inputDto.getIndexType(),object.getJSONObject("value"));
        return new SimpleOutputDto("添加单条数据成功");
    }


    /**
     * 添加多条数据
     *
     * @param object
     * @return
     */
    @RequestMapping(value = "/gtgj/addListJson", method = RequestMethod.POST)
    public Object addListJson(@RequestBody @Valid JSONObject object) {
        EsAddInputDto inputDto = validateAddInputDTO(object);
        elasticsearchToolService.addListJson(inputDto.getIndex(), inputDto.getIndexType(), inputDto.getPrimaryKey(), inputDto.getValue());
        return new SimpleOutputDto("添加多条数据成功");
    }

    /**
     * 查询单个对应id的数据
     *
     * @param object
     * @return
     */
    @RequestMapping(value = "/gtgj/queryData", method = RequestMethod.POST)
    public Object queryData(@RequestBody @Valid JSONObject object) {
        CommonOutPutDto commonOutPutDto = new CommonOutPutDto();
        validateQueryInputDto(object);
        JSONObject jsonObject = elasticsearchToolService.queryById(object.getString("index"), object.getString("indexType"), object.getString("id"));
        commonOutPutDto.setData(jsonObject);
        commonOutPutDto.setMsg("查询Es数据成功");
        return commonOutPutDto;
    }

    /**
     * 查询多条数据
     *
     * @param object
     * @return
     */
    @RequestMapping(value = "/gtgj/queryListData", method = RequestMethod.POST)
    public Object queryListData(@RequestBody @Valid JSONObject object) {
        CommonOutPutDto commonOutPutDto = new CommonOutPutDto();
        validateQueryInputDto(object);
        List<JSONObject> jsonObject = elasticsearchToolService.queryListJsonByIndexType(object.getString("index"), object.getString("indexType"), Integer.parseInt(object.getString("size")));
        commonOutPutDto.setData(jsonObject);
        commonOutPutDto.setMsg("查询Es数据成功");
        return commonOutPutDto;
    }

    /**
     * 单个条件模糊查询
     *
     * @param object
     * @return
     */
    @RequestMapping(value = "/gtgj/queryFuzzyData", method = RequestMethod.POST)
    public Object queryFuzzyData(@RequestBody @Valid JSONObject object) {
        CommonOutPutDto commonOutPutDto = new CommonOutPutDto();
        JSONObject qbObject = validateQbQueryInputDto(object);
        WildcardQueryBuilder wildcardQueryBuilder = QueryBuilders.wildcardQuery(qbObject.getString("key"), qbObject.getString("value"));
        List<JSONObject> jsonObject = elasticsearchToolService.queryListJsonByQb(object.getString("index"), object.getString("indexType"), wildcardQueryBuilder, Integer.parseInt(object.getString("size")));
        commonOutPutDto.setData(jsonObject);
        commonOutPutDto.setMsg("单条件模糊查询Es数据成功");
        return commonOutPutDto;
    }


    /**
     * 多个条件模糊查询
     *
     * @param object
     * @return
     */
    @RequestMapping(value = "/gtgj/queryMoreFuzzyData", method = RequestMethod.POST)
    public Object queryMoreFuzzyData(@RequestBody @Valid JSONObject object) {
        CommonOutPutDto commonOutPutDto = new CommonOutPutDto();
        Map<String, String> map = validateQbMoreQueryInputDto(object);
        List<JSONObject> jsonObject = elasticsearchToolService.queryListJsonByMoreQb(object.getString("index"), object.getString("indexType"), map, Integer.parseInt(object.getString("size")));
        commonOutPutDto.setData(jsonObject);
        commonOutPutDto.setMsg("多条件模糊查询Es数据成功");
        return commonOutPutDto;
    }

    /**
     * 多条件模糊查询+范围+精确查询（组合查询）
     *
     * @param object
     * @return
     */
    @RequestMapping(value = "/gtgj/queryAssembleData", method = RequestMethod.POST)
    public Object  queryMoreFuzzyTimeData(@RequestBody @Valid JSONObject object) {
        CommonOutPutDto commonOutPutDto = new CommonOutPutDto();
        List<JSONObject> jsonObject = elasticsearchToolService.queryListByAssembleQb(
                object.getString("index"),
                object.getString("indexType"),
                validateAllQb(object),
                Integer.parseInt(object.getString("pageNow")),
                Integer.parseInt(object.getString("pageSize")),
                getSortMap(object)
        );
        commonOutPutDto.setData(jsonObject);
        commonOutPutDto.setMsg("组合查询Es数据成功");
        return commonOutPutDto;
    }

    private Map<String, SortOrder> getSortMap(@Valid JSONObject object) {
        Map<String, SortOrder> map = new ConcurrentHashMap<>();
        JSONArray jsonArray = object.getJSONArray("sortQb");
        if (jsonArray != null && jsonArray.size() > 0) {
            for (int i = 0; i < jsonArray.size(); i++) {
                map.put(jsonArray.getJSONObject(i).getString("sortName"), jsonArray.getJSONObject(i).getString("sortType").equals("desc") == true ? SortOrder.DESC : SortOrder.ASC);
            }
        }
        if (map.size() == 0) {
            map.put("updateTime", SortOrder.DESC);
        }
        return map;
    }

    /**
     * 多个条件模糊+排序查询
     *
     * @param object
     * @return
     */
    @RequestMapping(value = "/gtgj/queryFuzzySortData", method = RequestMethod.POST)
    public Object queryFuzzySortData(@RequestBody @Valid JSONObject object) {
        CommonOutPutDto commonOutPutDto = new CommonOutPutDto();
        Map<String, String> map = validateQbMoreQueryInputDto(object);
        BoolQueryBuilder booleanQueryBuilder = QueryBuilders.boolQuery();
        for (Map.Entry<String,String> in : map.entrySet()) {
            booleanQueryBuilder.must(QueryBuilders.wildcardQuery(in.getKey(), in.getValue()));
        }
        Map<String, SortOrder> map1 = new HashMap<>();
        map1.put(object.getString("sortName"),
                object.getString("sortType").equals("desc") == true ? SortOrder.DESC : SortOrder.ASC);

        List<JSONObject> jsonObject = elasticsearchToolService.queryPointsListBySortQb(
                object.getString("index"),
                object.getString("indexType"),
                booleanQueryBuilder,
                Integer.parseInt(object.getString("size")),
                map1);
        commonOutPutDto.setData(jsonObject);
        commonOutPutDto.setMsg("多条件模糊查询Es数据成功");

        return commonOutPutDto;
    }

    /**
     * 多个条件模糊+排序+分页查询
     *
     * @param object
     * @return
     */
    @RequestMapping(value = "/gtgj/queryFuzzySortPageData", method = RequestMethod.POST)
    public Object queryFuzzySortPageData(@RequestBody @Valid JSONObject object) {
        CommonOutPutDto commonOutPutDto = new CommonOutPutDto();
        Map<String, String> map = validateQbMoreQueryInputDto(object);
        BoolQueryBuilder booleanQueryBuilder = QueryBuilders.boolQuery();
        for (Map.Entry<String,String> in : map.entrySet()) {
            booleanQueryBuilder.must(QueryBuilders.wildcardQuery(in.getKey(), in.getValue()));
        }
        List<JSONObject> jsonObject = elasticsearchToolService.queryPageListFuzzyQb(
                object.getString("index"),
                object.getString("indexType"),
                booleanQueryBuilder,
                Integer.parseInt(object.getString("pageNow")),
                Integer.parseInt(object.getString("size")),
                object.getString("sortName"),
                object.getString("sortType").equals("desc") == true ? SortOrder.DESC : SortOrder.ASC);
        commonOutPutDto.setData(jsonObject);
        commonOutPutDto.setMsg("多条件模糊查询Es数据成功");

        return commonOutPutDto;
    }

    /**
     * 查询ik分词结果
     *
     * @param jsonObject
     * @return
     */
    @RequestMapping(value = "/gtgj/queryIKAnalyze")
    public Object queryIKAnalyzeResult(@RequestBody @Valid JSONObject jsonObject) {
        CommonOutPutDto commonOutPutDto = new CommonOutPutDto();
        List<String> list = elasticsearchToolService.queryAnalyzeResult(jsonObject.getString("indexName"), jsonObject.getString("text"), jsonObject.getInteger("type") == null ? 1 : jsonObject.getInteger("type"));
        commonOutPutDto.setData(list);
        return commonOutPutDto;
    }

    /**
     * 批量删除数据
     * @param jsonObject
     * @return
     */
    @RequestMapping(value = "/gtgj/deleteDataByBulk", method = RequestMethod.POST)
    public Object deleteDataByBulk(@RequestBody @Valid JSONObject jsonObject){
        CommonOutPutDto commonOutPutDto = new CommonOutPutDto();
        boolean flag = elasticsearchToolService.deleteBulkData(jsonObject.getString("index"),jsonObject.getString("indexType"),jsonObject.getInteger("size"));
        if (flag) {
            commonOutPutDto.setData(Boolean.TRUE);
            commonOutPutDto.setMsg("批量删除数据成功");
        } else {
            commonOutPutDto.setData(Boolean.TRUE);
            commonOutPutDto.setMsg("批量删除数据失败");
        }
        return commonOutPutDto;
    }

    /**
     * 根据id主键删除数据
     * @param jsonObject
     * @return
     */
    @RequestMapping(value = "/gtgj/deleteDataById", method = RequestMethod.POST)
    public Object deleteDataById(@RequestBody @Valid JSONObject jsonObject){
        CommonOutPutDto commonOutPutDto = new CommonOutPutDto();
        boolean flag = elasticsearchToolService.deleteById(jsonObject.getString("index"),jsonObject.getString("indexType"),jsonObject.getString("id"));
        if (flag) {
            commonOutPutDto.setData(Boolean.TRUE);
            commonOutPutDto.setMsg("删除数据成功");
        } else {
            commonOutPutDto.setData(Boolean.TRUE);
            commonOutPutDto.setMsg("删除数据失败");
        }
        return commonOutPutDto;
    }


    @RequestMapping(value = "/gtgj/deleteIndex", method = RequestMethod.POST)
    public Object deleteIndex(@RequestBody @Valid JSONObject jsonObject) {
        CommonOutPutDto commonOutPutDto = new CommonOutPutDto();
        boolean flag = elasticsearchToolService.deleteIndex(jsonObject.getString("index"));
        if (flag) {
            commonOutPutDto.setData(Boolean.TRUE);
            commonOutPutDto.setMsg("删除索引成功");
        } else {
            commonOutPutDto.setData(Boolean.TRUE);
            commonOutPutDto.setMsg("删除索引失败");
        }
        return commonOutPutDto;
    }


    /**
     * 校验添加数据入参
     *
     * @param object
     * @return
     */
    public static EsAddInputDto validateAddInputDTO(JSONObject object) {
        if (StringUtils.isEmpty(object.getString("index")) || StringUtils.isEmpty(object.getString("indexType")) || StringUtils.isEmpty(object.getString("primaryKey")) || StringUtils.isEmpty(object.getString("value"))) {
            throw new IllegalArgumentException("请求参数有误,请检查后重试！");
        } else {
            EsAddInputDto esAddInputDto = new EsAddInputDto();
            esAddInputDto.setIndex(object.getString("index"));
            esAddInputDto.setIndexType(object.getString("indexType"));
            esAddInputDto.setPrimaryKey(object.getString("primaryKey"));
            JSONArray jsonArray = object.getJSONArray("value");
            List<JSONObject> list = new ArrayList<JSONObject>();
            for (int i = 0; i < jsonArray.size(); i++) {
                list.add(jsonArray.getJSONObject(i));
            }
            esAddInputDto.setValue(list);
            return esAddInputDto;
        }
    }

    /**
     * 校验单条数据添加
     *
     * @param object
     * @return
     */
    public static EsAddInputDto validateAddOneInputDTO(JSONObject object) {
        if (StringUtils.isEmpty(object.getString("index")) || StringUtils.isEmpty(object.getString("indexType")) || StringUtils.isEmpty(object.getString("primaryKey")) || StringUtils.isEmpty(object.getString("value"))) {
            throw new IllegalArgumentException("请求参数有误,请检查后重试！");
        } else {
            EsAddInputDto esAddInputDto = new EsAddInputDto();
            esAddInputDto.setIndex(object.getString("index"));
            esAddInputDto.setIndexType(object.getString("indexType"));
            esAddInputDto.setPrimaryKey(object.getString("primaryKey"));
            return esAddInputDto;
        }
    }

    public static EsAddInputDto validateAddDataInputDTO(JSONObject object) {
        if (StringUtils.isEmpty(object.getString("index")) || StringUtils.isEmpty(object.getString("indexType")) || StringUtils.isEmpty(object.getString("value"))) {
            throw new IllegalArgumentException("请求参数有误,请检查后重试！");
        } else {
            EsAddInputDto esAddInputDto = new EsAddInputDto();
            esAddInputDto.setIndex(object.getString("index"));
            esAddInputDto.setIndexType(object.getString("indexType"));
            return esAddInputDto;
        }
    }

    /**
     * 校验查询数据入参
     *
     * @param object
     */
    public static void validateQueryInputDto(JSONObject object) {
        if (StringUtils.isEmpty(object.getString("index")) || StringUtils.isEmpty(object.getString("indexType"))) {
            throw new IllegalArgumentException("请求参数有误,请检查后重试！");
        }
        if (StringUtils.isEmpty(object.getString("id")) && StringUtils.isEmpty(object.getString("size"))) {
            throw new IllegalArgumentException("请求参数有误,请检查后重试！");
        }
    }


    /**
     * 模糊查询入参校验
     *
     * @param object
     */
    public static JSONObject validateQbQueryInputDto(JSONObject object) {
        if (StringUtils.isEmpty(object.getString("index")) || StringUtils.isEmpty(object.getString("indexType")) || StringUtils.isEmpty(object.getString("qb")) || StringUtils.isEmpty(object.getString("size"))) {
            throw new IllegalArgumentException("请求参数有误,请检查后重试！");
        }
        JSONObject jsonObject = object.getJSONObject("qb");
        if (StringUtils.isEmpty(jsonObject.get("key")) || StringUtils.isEmpty(jsonObject.get("value"))) {
            throw new IllegalArgumentException("请求参数有误,请检查后重试！");
        }
        return jsonObject;
    }

    /**
     * 多条件模糊查询入参校验
     *
     * @param object
     * @return
     */
    public static Map<String, String> validateQbMoreQueryInputDto(JSONObject object) {
        if (StringUtils.isEmpty(object.getString("index")) || StringUtils.isEmpty(object.getString("indexType")) || StringUtils.isEmpty(object.getString("qb")) || StringUtils.isEmpty(object.getString("size"))) {
            throw new IllegalArgumentException("请求参数有误,请检查后重试！");
        }
        JSONArray jsonArray = object.getJSONArray("qb");
        Map<String, String> map = new ConcurrentHashMap<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            String key = jsonArray.getJSONObject(i).getString("key");
            String value = jsonArray.getJSONObject(i).getString("value");
            if (!StringUtils.isEmpty(key) && !StringUtils.isEmpty(value)) {
                map.put(key, value);
            }
        }
        if (map.size() > 0) {
            return map;
        } else {
            throw new IllegalArgumentException("请求参数有误,请检查后重试！");
        }
    }

    public static BoolQueryBuilder validateAllQb(JSONObject object) {
        if (StringUtils.isEmpty(object.getString("index")) || StringUtils.isEmpty(object.getString("indexType")) || StringUtils.isEmpty(object.getString("pageSize"))|| StringUtils.isEmpty(object.getString("pageNow"))) {
            throw new IllegalArgumentException("请求参数有误,请检查后重试！");
        }

        BoolQueryBuilder booleanQueryBuilder = QueryBuilders.boolQuery();

        //模糊查询
        JSONArray jsonArray = object.getJSONArray("wildQb");
        if (jsonArray != null && jsonArray.size() > 0) {
            Map<String, String> map = new ConcurrentHashMap<>();
            for (int i = 0; i < jsonArray.size(); i++) {
                String key = jsonArray.getJSONObject(i).getString("key");
                String value = jsonArray.getJSONObject(i).getString("value");
                if (!StringUtils.isEmpty(key) && !StringUtils.isEmpty(value)) {
                    map.put(key, value);
                }
            }
            if (map.size() > 0) {
                for (Map.Entry<String,String> in: map.entrySet()) {
                    booleanQueryBuilder.must(QueryBuilders.wildcardQuery(in.getKey(), in.getValue()));
                }
            }
        }

        //范围查询
        JSONObject wildQb = object.getJSONObject("rangeQb");
        if (!StringUtils.isEmpty(wildQb)) {
            booleanQueryBuilder.must(QueryBuilders.rangeQuery(wildQb.getString("rangeName")).from(wildQb.getString("start")).to(wildQb.getString("end")));
        }

        //精确查询
        JSONArray jsonArray1 = object.getJSONArray("termQb");
        if (jsonArray1 != null && jsonArray1.size() > 0) {
            Map<String, String> map1 = new ConcurrentHashMap<>();
            for (int i = 0; i < jsonArray1.size(); i++) {
                String key = jsonArray1.getJSONObject(i).getString("key");
                String value = jsonArray1.getJSONObject(i).getString("value");
                if (!StringUtils.isEmpty(key) && !StringUtils.isEmpty(value)) {
                    map1.put(key, value);
                }
            }
            if (map1.size() > 0) {
                for (Map.Entry<String,String> in : map1.entrySet()) {
                    booleanQueryBuilder.must(QueryBuilders.termsQuery(in.getKey(), in.getValue()));
                }
            }
        }

        return booleanQueryBuilder;
    }


    /**
     * 校验es ik分词入参
     *
     * @param jsonObject
     * @return
     */
    public static EsIkCreateInputDto validateEsIkInputDto(JSONObject jsonObject) {
        if (StringUtils.isEmpty(jsonObject.getString("index")) || StringUtils.isEmpty(jsonObject.getString("indexType")) || StringUtils.isEmpty(jsonObject.getString("ikNames")) || StringUtils.isEmpty(jsonObject.getString("value"))) {
            throw new IllegalArgumentException("请求参数有误,请检查后重试！");
        }
        EsIkCreateInputDto esIkCreateInputDto = new EsIkCreateInputDto();
        esIkCreateInputDto.setIndex(jsonObject.getString("index"));
        esIkCreateInputDto.setIndexType(jsonObject.getString("indexType"));
        esIkCreateInputDto.setIkNames(jsonObject.getString("ikNames").split(","));
        esIkCreateInputDto.setValue(jsonObject.getJSONObject("value"));

        return esIkCreateInputDto;
    }


}
