package web.controller.file;

import common.result.Result;
import common.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StreamUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping("/local")
@Slf4j
public class LocalFileController {
    //本地存储
    @PostMapping
    public Result upload(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        File file_local = new File("云");
        String path = file_local.getAbsolutePath();
        File checkFile = new File(path);
        if (!checkFile.exists()) {
            checkFile.mkdirs();
        }
        log.info("文件路径::{}"+path);
        String saveName = UUID.randomUUID().toString() + "." + originalFilename;
        try {
            file.transferTo(new File(path + "/" + saveName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("文件上传成功: {}", path + "::" + saveName);
        return Result.success("@PostMapping::" + path+"::" + saveName);
    }
    /**
     * 下载文件
     */
    @GetMapping
    public void download(String fileName, HttpServletResponse response) {
        File file_local = new File("云");
        String path = file_local.getAbsolutePath() + "/" + fileName;
        try {
            File checkFile = new File(path);
            if (!checkFile.exists()) {
                throw new IllegalArgumentException(path + "文件不存在");
            }
            response.setContentType("application/octet-stream");
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
            response.setHeader("Content-Disposition", "attachment;filename=" + encodedFileName);
            try (InputStream fileInputStream = new FileInputStream(checkFile)) {
                StreamUtils.copy(fileInputStream, response.getOutputStream());
                response.flushBuffer();
                log.info("文件下载成功: {}", path);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}