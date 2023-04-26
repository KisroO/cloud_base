package com.kisro.cloud.common;

import lombok.Data;

/**
 * @author Kisro
 * @since 2022/11/7
 **/
@Data
public class UpdateRequest<T> {
    private T request;

    private UpdateRequest() {
    }

    public static <T> UpdateRequest<T> of(T updateRequest) {
        UpdateRequest<T> request = new UpdateRequest<>();
        request.setRequest(updateRequest);
        return request;
    }
}
