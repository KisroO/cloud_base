package com.kisro.cloud.pojo.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * @author zoutao
 * @since 2023/4/10
 **/
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PlatformInfo {

    /**
     * 平台登陆验证
     */
    @NotNull
    private Boolean verification;

    /**
     * 地址
     */
    @NotNull
    private String serverHost;

    /**
     * 端口
     */
    @NotNull
    private Integer serverPort;

    /**
     * 用户名
     */
    private String serverUsername;

    /**
     * 密码
     */
    private String serverPassword;

    /**
     * 需要删除的信息类型标志
     */
    @NotNull
    private Set<String> deleteType;

    /**
     * 备注
     */
    private String remark;

}
