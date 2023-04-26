package com.kisro.poi.service;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.afterturn.easypoi.excel.entity.params.ExcelExportEntity;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.kisro.poi.domain.ImportVehicleInfo;
import com.kisro.poi.domain.VehicleInOem;
import com.kisro.poi.enums.Command;
import com.kisro.poi.payload.AccOriginMsg;
import com.nex.bu1.json.JsonEx;
import com.nex.bu1.util.DateEx;
import com.nex.bu1.util.ListEx;
import com.nex.bu1.util.MapEx;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * @author zoutao
 * @since 2023/4/6
 **/
@Slf4j
@Service
public class FileService {
    String vin = "";
    String date = "";
    List<AccOriginMsg> dataList = new ArrayList<>(5000);
    String[] headers = {"VIN", "命令标识", "平台接收时间", "终端采集时间", "ACC状态"};
    //    List<Integer> normalIndexList = ListEx.newArrayList(0,1,2,3);
//    List<Integer> locationIndexList = ListEx.newArrayList(0,1,2,3,92);
    private static Map<String, String> HEAD_MAP = MapEx.newHashMap();

    static {
        HEAD_MAP.put("vin", "VIN");
        HEAD_MAP.put("date", "统计日期");
        HEAD_MAP.put("receivableCount", "有效应收报文数");
        HEAD_MAP.put("receivedCount", "有效已收报文数");
        HEAD_MAP.put("totalCount", "累计已收报文数");
//        HEAD_MAP.put("lossRate", "丢包率");
    }

    public void importFile(MultipartFile file) {

        try {
            List<ImportVehicleInfo> list = ExcelImportUtil.importExcel(file.getInputStream(), ImportVehicleInfo.class, null);
            System.out.println(JsonEx.toJsonString(list));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String importVehicleData(MultipartFile file) {
        List<VehicleInOem> dataList = ListEx.newArrayList();
        ImportParams importParams = new ImportParams();
        importParams.setHeadRows(1);
        try {
            dataList = ExcelImportUtil.importExcel(file.getInputStream(), VehicleInOem.class, importParams);
        } catch (Exception e) {
            log.warn("解析错误: {}", e.getMessage());
        }
        return JsonEx.toJsonString(dataList);
    }


    public void transformFiles() throws Exception {
        ClassPathResource resource = new ClassPathResource("transform");
        File file = resource.getFile();
        System.out.println(file);
        List<AccOriginMsg> dataList = Lists.newArrayListWithCapacity(5000);
        recursionFile(file, dataList);
    }

    private void recursionFile(File file, List<AccOriginMsg> dataList) throws Exception {
        File[] files = file.listFiles();
        String fileName = file.getName();
        // vin 文件夹
        if (fileName.length() == 17) {
            vin = fileName;
        }
        // 日期文件夹
        if (fileName.length() == 10) {
            date = fileName.replaceAll("-", "");
        }
        assert files != null;
        for (int i = 0; i < files.length; i++) {
            File fi = files[i];
            System.out.println(fi);
            String name = fi.getName();
            if (fi.isDirectory()) {
                recursionFile(fi, dataList);
            } else {
                // 处理文件
                // 1. 创建空的统计xlsx文件, vin+年月日.xlsx
                String statFileName = vin + "_" + date + ".xlsx";
//                createXlsxFile(statFileName);
                // 3. 读取解析后的报文文件
                if (name.contains(Command.LOCATION_FILE)) {
                    dataList.addAll(readXlsxFile(fi, 2));
                } else {
                    dataList.addAll(readXlsxFile(fi, 1));
                }
                System.gc();
                // 读取到最后一个文件,整合进行导出
                if (i == files.length - 1) {
                    // 按照采集时间进行升序排序
                    dataList.sort(Comparator.comparing(AccOriginMsg::getAcquisitionTime));
                    // 导出xlsx文件
//                    exportXlsxFile(statFileName, dataList);
                    exportExcel(statFileName, dataList);
                    // 置空
                    dataList = Lists.newArrayList();
                }
            }
        }
    }

    /**
     * 导出统计文件
     *
     * @param statFileName
     * @param dataList
     */
    private void exportXlsxFile(String statFileName, List<AccOriginMsg> dataList) throws IOException {
        FileOutputStream stream = null;
        Workbook workbook = null;
        try {
            stream = new FileOutputStream(statFileName);
            workbook = new XSSFWorkbook(statFileName);
            createSheet(workbook, statFileName, dataList);
            workbook.write(stream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // todo 关闭流 并重置状态对象
            if (workbook != null) {
                workbook.close();
            }
            if (stream != null) {
                stream.close();
            }
        }
    }

    private void exportExcel(String statFileName, List<AccOriginMsg> dataList) throws IOException {
        ExportParams params = new ExportParams();
        List<ExcelExportEntity> entityList = getExportEntityList();
        File file = new File(statFileName);
        FileOutputStream fos = new FileOutputStream(file);
        try (Workbook workbook = ExcelExportUtil.exportExcel(params, entityList, dataList)) {
            workbook.write(fos);
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<ExcelExportEntity> getExportEntityList() {
        List<ExcelExportEntity> entityList = ListEx.newArrayList();
        HEAD_MAP.forEach((k, v) -> {
            ExcelExportEntity exportEntity = new ExcelExportEntity(v, k);
            entityList.add(exportEntity);
        });
        return entityList;
    }

    private void createSheet(Workbook workbook, String title, List<AccOriginMsg> dataList) {
        Sheet sheet = workbook.createSheet(title);
        // 创建表头
        Row headRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headRow.createCell(i);
            cell.setCellValue(headers[i]);
            sheet.autoSizeColumn(i);
        }
        // 创建数据体
        for (int i = 0; i < dataList.size(); i++) {
            Row row = sheet.createRow(i + 1);
            AccOriginMsg data = dataList.get(i);
            JSONObject obj = JSONObject.parseObject(JSONObject.toJSONString(data));
            Iterator<Map.Entry<String, Object>> iterator = obj.entrySet().iterator();
            int j = 0;
            while (iterator.hasNext()) {
                Map.Entry<String, Object> next = iterator.next();
                Cell cell = row.createCell(j);
                Object valueObj = Optional.ofNullable(next)
                        .map(Map.Entry::getValue)
//                        .map(Object::toString)
                        .orElse(null);
                assert next != null;
                String key = next.getKey();
                if ("平台接收时间".equals(key) || "终端采集时间".equals(key)) {
                    // 时间格式转换
                    Date dateObj = (Date) valueObj;
                    cell.setCellValue(dateObj);
                } else {
                    cell.setCellValue(valueObj.toString());
                }
                j += 1;
            }
        }
    }

    private void createXlsxFile(String statFileName, List<AccOriginMsg> dataList) {
        ExportParams exportParams = new ExportParams();
        exportParams.setCreateHeadRows(true);
        Workbook sheets = ExcelExportUtil.exportExcel(exportParams, AccOriginMsg.class, dataList);
        try (FileOutputStream stream = new FileOutputStream(statFileName)) {
            sheets.write(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取报文文件并解析
     *
     * @param file
     * @param startRow 起始行，从0开始
     * @return
     */
    private List<AccOriginMsg> readXlsxFile(File file, int startRow) throws Exception {
        List<AccOriginMsg> list = ListEx.newArrayList();
        Workbook workbook = null;
        try {
            workbook = WorkbookFactory.create(file);
//            workbook = new HSSFWorkbook(new FileInputStream(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert workbook != null;
        Sheet sheet = workbook.getSheetAt(0);
        // 实际行数
        int rows = sheet.getPhysicalNumberOfRows();
        String name = file.getName();
        AccOriginMsg msg = null;
        boolean locationHead = name.contains(Command.LOCATION_FILE);
        String acquisitionTime = null;
        String receiveTime = null;
        Row row;
        for (int i = startRow; i < rows; i++) {
            // 读取行
            row = sheet.getRow(i);
            msg = new AccOriginMsg();
            String vin = null;
            try {
                vin = row.getCell(0).getStringCellValue();
            } catch (Exception e) {
                break;
            }
            msg.setVin(vin);
            msg.setCommand(row.getCell(1).getStringCellValue());
            receiveTime = row.getCell(2).getStringCellValue();
            msg.setReceiveTime(DateEx.parse(receiveTime));
            acquisitionTime = row.getCell(3).getStringCellValue();
            msg.setAcquisitionTime(DateEx.parse(acquisitionTime));
            if (name.contains(Command.REALTIME_FILE) && list.contains(msg)) {
                continue;
            }
            if (locationHead) {
                msg.setAccFlag(row.getCell(92).getStringCellValue());
            }
            // 实时数据去重
            list.add(msg);
            msg = null;
        }
        try {
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        sheet = null;
        file = null;
        return list;
    }


}
