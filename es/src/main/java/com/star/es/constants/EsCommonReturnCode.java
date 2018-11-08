package com.star.es.constants;

import java.io.Serializable;

public abstract interface EsCommonReturnCode {

    public static final EsReturnCode SUCCESS = EsReturnCode.create("SC0000", "成功");
    public static final EsReturnCode Fail = EsReturnCode.create("FA1111", "失败");
    public static final EsReturnCode UNKNOW_ERROR = EsReturnCode.create("UE1111", "系统繁忙，请稍后再试");
    public static final EsReturnCode ERR_INPUT_VALIDATION_REJECTED = EsReturnCode.create("EI1111", "请求参数格式有误，校验未通过");
    public static final EsReturnCode ERR_ES_QUERY = EsReturnCode.create("EQ1111", "Es查询数据异常");
    public static final EsReturnCode ERR_ES_AGGREGATIONS_QUERY = EsReturnCode.create("EQ1110", "Es聚合查询异常");
    public static final EsReturnCode ERR_ES_ADD = EsReturnCode.create("EA1111", "Es添加数据异常");
    public static final EsReturnCode ERR_ES_DELETE = EsReturnCode.create("ED1111", "Es数据异常");
    public static final EsReturnCode ERR_ES_NODE_CONNECT_FAIL = EsReturnCode.create("EF1111", "Es集群node连接失败");
    public static final EsReturnCode ERR_ES_CREATE_IK_ERROR = EsReturnCode.create("EK1111", "创建Ik分词的index和type异常");
    public static final EsReturnCode ERR_ES_IK_RESULT_ERROR = EsReturnCode.create("EK1110", "获取Ik分词结果异常");
    public static final EsReturnCode ERR_ES_UNKNOW_ERROR = EsReturnCode.create("EN1111", "Es操作异常，请联系开发人员");
    public static final EsReturnCode ERR_ES_NO_NODE_AVAILABLE = EsReturnCode.create("EN1110","Es集群无用node，请检查Es集群是否启动");
    public static final EsReturnCode ERR_ES_TIME_OUT= EsReturnCode.create("ET1111","Es集群连接超时，请稍后重试");
    public static final EsReturnCode ERR_ES_CONNECT_ERROR= EsReturnCode.create("EC1111","Es集群连接异常，请稍后重试");

}
