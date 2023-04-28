package com.kisro.poi.util;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.enmus.ExcelType;
import com.kisro.poi.payload.AccStatInfo;
import com.kisro.poi.payload.LossRateExport;
import com.nex.bu1.util.ListEx;
import com.nex.bu1.util.MapEx;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.Workbook;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * @author zoutao
 * @date 2023/4/28
 */
public class ExportUtils {
    public static void exportMultiSheet(List<Map<String, Object>> sheetList, String fileName, HttpServletResponse response) {
        ServletOutputStream sos = null;
        Workbook workbook = null;
        try {
            workbook = ExcelExportUtil.exportExcel(sheetList, ExcelType.HSSF);
            setResponse(response, fileName);
            sos = response.getOutputStream();
            workbook.write(sos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (sos != null) {
                try {
                    sos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void setResponse(HttpServletResponse response, String fn) throws UnsupportedEncodingException {
        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode(fn, "UTF-8"));
    }
}
