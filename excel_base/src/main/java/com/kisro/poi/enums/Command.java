package com.kisro.poi.enums;

import com.nex.bu1.util.ListEx;

import java.util.List;

/**
 * @author zoutao
 * @since 2023/4/17
 **/
public interface Command {
    String VEHICLE_LOGIN = "车辆登入";
    String VEHICLE_LOGOUT = "车辆登出";
    String REALTIME_DATA = "实时信息上报";
    String REALTIME_REUPLOAD_DATA = "补发信息上报";
    String LOCATION_DATA = "位置信息汇报";
    String LOCATION_REUPLOAD_DATA = "位置信息盲区批量上传";
    List<String> REAL_DATA_LIST = ListEx.newArrayList(REALTIME_DATA, REALTIME_REUPLOAD_DATA);
    List<String> LOCATION_DATA_LIST = ListEx.newArrayList(LOCATION_DATA, LOCATION_REUPLOAD_DATA);
    List<String> NO_ACC_COMMAND_LIST = ListEx.newArrayList(VEHICLE_LOGIN,VEHICLE_LOGOUT,REALTIME_DATA,REALTIME_REUPLOAD_DATA);

    String LOCATION_FILE = "位置信息";
    String REALTIME_FILE = "实时信息";
    String LOGIN_LOGOUT_FILE = "登入登出";
}
