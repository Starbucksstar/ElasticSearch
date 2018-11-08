package com.star.es.advice;

import com.star.es.constants.EsCommonReturnCode;
import com.star.es.dto.output.SimpleOutputDto;
import com.star.es.exception.GtgjEsException;
import com.star.es.service.tool.ElasticsearchToolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author gaoxing
 * @date 2018-09-19
 */

@ControllerAdvice
public class ControllerExceptionAdvice {
    protected Logger logger = LoggerFactory.getLogger(ControllerExceptionAdvice.class);
    private static final String ERROR_MSG ="系统异常，请稍后再试";
    private static final String ERROR_CODE ="Fail";

    /**
     * @param e
     * @return
     * @ExceptionHandler去捕获这个类所有的异常
     * @ControllerAdvice，不用任何的配置，只要把这个类放在项目中，Spring能扫描到的地方。就可以实现全局异常的回调。
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public SimpleOutputDto exceptionHandler(Throwable e) {
        SimpleOutputDto out = new SimpleOutputDto();
        if (e instanceof GtgjEsException) {
            GtgjEsException exception = (GtgjEsException) e;
            logger.error("ES操作异常,异常码={}，异常信息={}",exception.getEsReturnCode().getCode(),exception.getEsReturnCode().getMsg(),exception);
            out.setMsg(exception.getEsReturnCode().getMsg());
            out.setCode(exception.getEsReturnCode().getCode());
        } else {
            logger.error("系统异常,异常信息={}", e.getMessage(), e);
            out.setMsg(e.getMessage());
            out.setCode(EsCommonReturnCode.UNKNOW_ERROR.getCode());
        }
        return out;
    }

}