package com.star.es.dto.output;

public class CommonOutPutDto<T> extends SimpleOutputDto {
    private T data;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
