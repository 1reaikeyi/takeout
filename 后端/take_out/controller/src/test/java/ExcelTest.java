import com.alibaba.excel.EasyExcel;

import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.write.metadata.WriteSheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.junit.jupiter.api.Test;

import pojo.excel.UserStatistics;
import web.TakeOutApplication;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ExcelTest {
    @Test
    public void doWrite() {
        File file = new File("src/main/resources/static/excel/report.xlsx");
        String filePath = file.getAbsolutePath();
        File checkFile = new File(filePath);
        if (!checkFile.exists()) {
            checkFile.mkdirs();
        }
        System.out.println("文件路径::" + filePath);
        // 1. 构造测试数据
        List<UserStatistics> dataList = new ArrayList<>();
        for (int i = 1; i < 7; i++) {
            UserStatistics user = new UserStatistics();
            user.setId((long) i);
            user.setName("张三" + i);
            user.setAge(20 + i);
            user.setEmail("zhangsan" + i + "@qq.com");
            dataList.add(user);
        }

        ExcelWriter excelWriter = EasyExcel.write(filePath, UserStatistics.class).build();
        WriteSheet writeSheet = EasyExcel.writerSheet("用户信息表").build();
        excelWriter.write(dataList, writeSheet);
//        // 获取Sheet对象
//        Sheet sheet = excelWriter.writeContext().writeSheetHolder().getSheet();
//        // 合并第10行的前10个单元格, // 起始行 (第10行，索引为9) // 结束行 (第10行，索引为9) // 起始列 (第1列，索引为0)// 结束列 (第10列，索引为9)
//        CellRangeAddress region = new CellRangeAddress(9, 9, 0, 9);
//        sheet.addMergedRegion(region);
//        // 在合并的单元格中写入"统计"
//        Row row = sheet.getRow(9);
//        if (row == null) {
//            row = sheet.createRow(9);
//        }
//        Cell cell = row.createCell(0);
//        cell.setCellValue("统计");

        // 完成写入
        excelWriter.finish();
        System.out.println("Excel 写入成功！路径::" + filePath);
    }
    @Test
    public void Read() {
        File file = new File("src/main/resources/static/excel/report.xlsx");
        String filePath = file.getAbsolutePath();

        List<UserStatistics> dataList = new ArrayList<>();
        EasyExcel.read(filePath, UserStatistics.class, new AnalysisEventListener<UserStatistics>() {
            @Override
            public void invoke(UserStatistics data, AnalysisContext context) {
                if (data.getId() != null) {
                    dataList.add(data);
                }
            }
            @Override
            public void onException(Exception exception, AnalysisContext context) {
                System.out.println("解析异常，跳过该行: " + exception.getMessage());
            }
            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                System.out.println("读取完成，共 " + dataList.size() + " 条数据");
            }
        }).sheet().doRead();

        for (UserStatistics user : dataList) {
            System.out.println(user);
        }
    }
}
