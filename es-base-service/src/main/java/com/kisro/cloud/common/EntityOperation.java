package com.kisro.cloud.common;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import lombok.Data;

import java.util.function.Supplier;

/**
 * @author Kisro
 * @since 2022/11/7
 **/
@Data
public class EntityOperation<T extends BaseMapper<?>> {
    private T mapper;

    private ExecuteTask task;

    private Supplier<T> supplier;

    private EntityOperation() {
    }

    public static <T extends BaseMapper<?>> EntityOperation<T> load(T mapper) {
        EntityOperation<T> entityOperation = new EntityOperation<>();
        entityOperation.setMapper(mapper);
        return entityOperation;
    }

    public <T extends BaseMapper<?>> EntityOperation<T> handler(ExecuteTask task) {
        this.task = task;
        return (EntityOperation<T>) this;
    }

    // 1. load加载mapper
    // 2. 加载更新逻辑
    // 3. 执行更新
    public <T extends BaseMapper<?>> EntityOperation<T> update() {
        task.handle();
        return (EntityOperation<T>) this;
    }

}
