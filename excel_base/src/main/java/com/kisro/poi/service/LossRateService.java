//package com.kisro.poi.service;
//
//import com.kisro.poi.enums.Command;
//import com.kisro.poi.payload.AccOriginMsg;
//import com.kisro.poi.payload.AccStatInfo;
//import com.kisro.poi.payload.LossRateResult;
//import com.nex.bu1.lang.ObjEx;
//import com.nex.bu1.lang.StrEx;
//import lombok.RequiredArgsConstructor;
//import org.apache.commons.collections4.CollectionUtils;
//import org.springframework.stereotype.Service;
//
//import java.io.IOException;
//import java.util.Date;
//
///**
// * @author zoutao
// * @since 2023/4/25
// **/
//@Service
//@RequiredArgsConstructor
//public class LossRateService {
//    private static final String ON = "开";
//    private static final String OFF = "关";
//    private final HBaseService hBaseService;
//
//    public LossRateResult singleCarStat(String vin, Date date){
//        beforeReset();
//        // 1. 加载数据
//        // 后续可从HBase中获取
//        dataList = loadDataFromHBase(vin,commandList, date);
//
//        System.out.println(dataList.size());
//        LossRateResult result = new LossRateResult();
//        int maxSize = dataList.size();
//        AccStatInfo statInfo = null;
////        AccStatInfo preStatInfo = new AccStatInfo();;
//        for (nextIndex = 0; nextIndex < maxSize; nextIndex++) {
//            AccOriginMsg msg = dataList.get(nextIndex);
//            String accFlag = msg.getAccFlag();
//            Date acquisitionTime = msg.getAcquisitionTime();
//            String commandStr = msg.getCommand();
//            // 统计累计已收实时与补发
//            if (StrEx.equals(Command.REALTIME_DATA, commandStr) || StrEx.equals(Command.REALTIME_REUPLOAD_DATA, commandStr)) {
//                totalCount += 1;
//            }
//            // 2. 查询下一个ACC=ON的位置报文，标记为起始点
//            if (ObjEx.isNull(statInfo)) {
//                // 位置报文，且为ON档
//                if (Command.LOCATION_DATA_LIST.contains(commandStr) && StrEx.equals(ON, accFlag)) {
//                    statInfo = new AccStatInfo();
//                    statInfo.setStartIndex(nextIndex);
//                    statInfo.setStartDate(acquisitionTime);
//                }
//            } else {
//                // 结束区间
//                if (Command.LOCATION_DATA_LIST.contains(commandStr) && StrEx.equals(OFF, accFlag)) {
//                    statInfo.setEndIndex(nextIndex);
//                    statInfo.setEndDate(acquisitionTime);
//                    statInfo.setLogoutDate(acquisitionTime);
//                    statInfoList.add(statInfo);
//                    // 保存区间信息
////                    BeanUtils.copyProperties(statInfo, preStatInfo);
//                    statInfo = null;
////                } else if (StrEx.equals(Command.VEHICLE_LOGOUT, commandStr) || StrEx.equals(Command.VEHICLE_LOGIN, commandStr)) {
//                } else if (StrEx.equals(Command.VEHICLE_LOGOUT, commandStr)) {
//                    // 碰到登出, 结束区间统计
//                    statInfo.setEndIndex(nextIndex);
//                    statInfo.setEndDate(acquisitionTime);
//                    statInfo.setLogoutDate(acquisitionTime);
//                    statInfoList.add(statInfo);
//                    //
////                    BeanUtils.copyProperties(statInfo, preStatInfo);
//                    statInfo = null;
//                } else if (Command.REAL_DATA_LIST.contains(commandStr)){
//                    // 实时信息与补发信息
////                    statInfo.setReceivedCount(statInfo.getReceivedCount() + 1);
//                } else if(StrEx.equals(Command.VEHICLE_LOGIN, commandStr)){
//                    // 上个区间正常登出，衔接补发|位置盲区，再衔接登入
////                    if(preStatInfo.getLogoutDate() != null){
////                        continue;
////                    }
//                    // 异常下线
//                    statInfo.setEndIndex(nextIndex);
//                    statInfo.setEndDate(acquisitionTime);
//
//                    statInfoList.add(statInfo);
////                    preStatInfo = new AccStatInfo();
//                    statInfo = null;
//                }
//            }
//        }
//        // 5. 计算区间应收与已收报文数
//        postProcessData();
//        result.setReceivableCount(totalReceivableCount);
//        result.setReceivedCount(totalReceivedCount);
//        int realtimeDataSize = loadDataFromHBase(vin, realtimeList, date).size();
//        result.setTotalCount((long)realtimeDataSize);
//        if(CollectionUtils.isNotEmpty(dataList)){
//            try {
//                exportOriginMsg(vin,date,dataList);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        afterReset();
//        return result;
//    }
//}
