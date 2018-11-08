package com.star.es.constants;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class EsReturnCode implements Serializable {

    private static Map<String, EsReturnCode> map = new HashMap<String, EsReturnCode>();
    private String code;
    private String msg;

    public static EsReturnCode create(String code, String msg) {
        if (map.containsKey(code)) {
            throw new IllegalArgumentException("Es系统不能存在相同的返回码，code" + code);
        }
        EsReturnCode esReturnCode = new EsReturnCode(code, msg);
        map.put(code, esReturnCode);
        return esReturnCode;
    }

    public EsReturnCode(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
