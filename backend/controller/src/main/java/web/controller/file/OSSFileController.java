package web.controller.file;

import common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/local")
@Slf4j
public class OSSFileController {
    @PostMapping
    public Result uploadFile(@RequestParam("file") MultipartFile file) {
        return Result.success(file);
    }
    @GetMapping
    public void downloadFile(@RequestParam("file") MultipartFile file) {

    }
}
