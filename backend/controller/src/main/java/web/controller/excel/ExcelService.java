package web.controller.excel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.write.metadata.WriteSheet;
import common.excel.UserStatistics;
import common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pojo.entity.User;
import service.ISevcive.UserService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ExcelService {
    @Autowired
    private UserService userService;

    /**
     * write
     * @return
     */
    public Result dowrite() {
        File file = new File("云/excel/report.xlsx");
        String filePath = file.getAbsolutePath();
        File checkFile = new File(filePath);
        if (!checkFile.exists()) {
            checkFile.mkdirs();
        }
        System.out.println("文件路径::" + filePath);
        List<User> userList = userService.list();
        List<UserStatistics> userStatisticsList = userList.stream()
                .map(user -> UserStatistics.builder()
                        .id(user.getId())
                        .name(user.getUserName())
                        .build())
                .toList();
        ExcelWriter excelWriter = EasyExcel.write(filePath, UserStatistics.class).build();
        WriteSheet writeSheet = EasyExcel.writerSheet("用户信息表").build();
        excelWriter.write(userStatisticsList, writeSheet);
        // 完成写入
        excelWriter.finish();
        return Result.success(filePath);
    }
    /**
     * 读取
     */
    public void doread() {
        File file = new File("云/excel/report.xlsx");
        String filePath = file.getAbsolutePath();

        List<UserStatistics> dataList = new ArrayList<>();
        EasyExcel.read(filePath, UserStatistics.class, new AnalysisEventListener<UserStatistics>() {
            @Override
            public void invoke(UserStatistics data, AnalysisContext context) {
                if (data.getId() == null) {
                   throw new RuntimeException("文件为null");
                }
                dataList.add(data);
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
