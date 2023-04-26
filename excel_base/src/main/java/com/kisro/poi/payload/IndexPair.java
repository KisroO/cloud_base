package com.kisro.poi.payload;

import lombok.Data;
import lombok.ToString;

/**
 * 目前仅用于存储Integer类型
 *
 * @author zoutao
 * @since 2023/4/18
 **/
@Data
@ToString
public class IndexPair {
    private Integer left;
    private Integer right;

    public static IndexPair of(Integer left, Integer right) {
        IndexPair pair = new IndexPair();
        pair.setLeft(left);
        pair.setRight(right);
        return pair;
    }

    /**
     * 校验target 是否在 此 Pair区间中
     *
     * @param target
     * @return
     */
    public boolean checkBound(Integer target) {
        return target < left || target > right;
    }


}
