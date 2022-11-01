package com.kisro.cloud.im;

/**
 * @author Kisro
 * @since 2022/10/31
 **/
public interface UserService {
    /**
     * 登录
     *
     * @param username 用户名
     * @param password 密码
     * @return 登录成功返回 true, 否则返回 false
     */
    boolean login(String username, String password);
}
