package com.star.es.dto.output;

import java.io.Serializable;

public  class SimpleOutputDto implements Serializable {

    public static final String DEFAULT_RETURN_CODE = "success";
    private static final String DEFAULT_RETURN_MSG = "成功";

    private String code = DEFAULT_RETURN_CODE;
    private String msg = DEFAULT_RETURN_MSG;
    private String timeStamp = String.valueOf(System.currentTimeMillis());

    public SimpleOutputDto() {
    }

    public SimpleOutputDto(String msg) {
        this.msg = msg;
    }

    public SimpleOutputDto(String code, String msg) {
        super();
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

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public String toString() {
        return "SimpleOutputDto{" +
                "code='" + code + '\'' +
                ", msg='" + msg + '\'' +
                ", timeStamp=" + timeStamp +
                '}';
    }
}