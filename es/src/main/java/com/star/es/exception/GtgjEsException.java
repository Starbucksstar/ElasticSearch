package com.star.es.exception;

import com.star.es.constants.EsCommonReturnCode;
import com.star.es.constants.EsReturnCode;

/**
 * @author gaoxing
 * @date 2018-09-19
 */
public class GtgjEsException extends RuntimeException {
    private EsReturnCode esReturnCode;
    private ExceptionLevel level = ExceptionLevel.WARN;
    private String overrideMag;

    public static enum ExceptionLevel {
        DEBUG, INFO, ERROR, WARN;

        private ExceptionLevel() {
        }
    }

    public GtgjEsException(EsReturnCode esReturnCode){
        this.esReturnCode = esReturnCode;
    }

    public GtgjEsException(EsReturnCode esReturnCode,String overrideMag){
        this.esReturnCode = esReturnCode;
        esReturnCode.setMsg(overrideMag);
    }

    public GtgjEsException(String overrideMag){
        this.esReturnCode = EsCommonReturnCode.ERR_ES_UNKNOW_ERROR;
        esReturnCode.setMsg(overrideMag);
    }


    public EsReturnCode getEsReturnCode() {
        return esReturnCode;
    }

    public void setEsReturnCode(EsReturnCode esReturnCode) {
        this.esReturnCode = esReturnCode;
    }

    public ExceptionLevel getLevel() {
        return level;
    }

    public void setLevel(ExceptionLevel level) {
        this.level = level;
    }

    public String getOverrideMag() {
        return overrideMag;
    }

    public void setOverrideMag(String overrideMag) {
        this.overrideMag = overrideMag;
    }
}
