package com.kisro.cloud.util;

/**
 * @author Kisro
 * @since 2022/11/7
 **/
public class Asserts {
    private boolean hasPreChecked = true;

    public static Asserts build() {
        return new Asserts();
    }

    public Asserts preCheck(boolean condition) {
        if (condition) {
            hasPreChecked = true;
            return this;
        }
        hasPreChecked = false;
        return this;
    }

    public Asserts isTrue(boolean condition, IError iError) {
        if (hasPreChecked) {
            if (condition) {
                throw new BizException(IError.NOT_FOUND);
            }
        }
        return this;
    }
}
