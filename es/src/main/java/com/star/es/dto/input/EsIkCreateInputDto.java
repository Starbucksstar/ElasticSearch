package com.star.es.dto.input;

import com.alibaba.fastjson.JSONObject;


public class EsIkCreateInputDto {
    private String index;
    private String indexType;
    private String[] ikNames;
    private JSONObject value;

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

    public String[] getIkNames() {
        return ikNames;
    }

    public void setIkNames(String[] ikNames) {
        this.ikNames = ikNames;
    }

    public JSONObject getValue() {
        return value;
    }

    public void setValue(JSONObject value) {
        this.value = value;
    }

}
