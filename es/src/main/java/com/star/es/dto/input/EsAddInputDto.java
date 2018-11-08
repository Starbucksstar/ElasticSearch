package com.star.es.dto.input;

import com.alibaba.fastjson.JSONObject;

import java.util.List;

public class EsAddInputDto {
    private String index;
    private String indexType;
    private String primaryKey;
    private List<JSONObject> value;

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getIndexType() {
        return indexType;
    }

    public void setIndexType(String indexType) {
        this.indexType = indexType;
    }

    public String getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
    }

    public List<JSONObject> getValue() {
        return value;
    }

    public void setValue(List<JSONObject> value) {
        this.value = value;
    }
}
