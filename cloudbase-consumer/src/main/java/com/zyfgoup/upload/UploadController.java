package com.zyfgoup.upload;

import com.zyfgoup.entity.Result;
import org.apache.commons.io.FileUtils;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @Author Zyfgoup
 * @Date 2021/1/5 13:31
 * @Description
 */
@RestController
public class UploadController {

    //可以在配置文件中配置
    private String filePath = "D:\\cloudstudy-video\\video";

    private String filePathTemp = "D:\\cloudstudy-video\\temp";

    /**
     * 分片上传
     */
    @PostMapping("/consumer/upload")
    public Result upload(HttpServletRequest request, Chunk chunk) throws IOException {

        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (isMultipart) {
            MultipartFile file = chunk.getFile();

            if (file == null) {
                throw new RuntimeException();
            }

            Integer chunkNumber = chunk.getChunkNumber();
            if (chunkNumber == null) {
                chunkNumber = 0;
            }

            File outFile = new File(filePathTemp + File.separator + chunk.getIdentifier(), chunkNumber + ".part");

            InputStream inputStream = file.getInputStream();
            FileUtils.copyInputStreamToFile(inputStream, outFile);
        }

        return Result.succ(null);
    }

    /**
     * 合并所有分片
     */
    @GetMapping("/consumer/merge")
    public Result mergeFile(String filename, String guid) throws Exception {

        File file = new File(filePathTemp + File.separator + guid);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null && files.length > 0) {
                File partFile = new File(filePath + File.separator + filename);
                if(partFile.exists()){
                    //文件已存在
                }

                for (int i = 1; i <= files.length; i++) {
                    File s = new File(filePathTemp + File.separator + guid, i + ".part");
                    FileOutputStream destTempfos = new FileOutputStream(partFile, true);
                    FileUtils.copyFile(s, destTempfos);
                    destTempfos.close();
                }
                FileUtils.deleteDirectory(file);
            }
        }

        return Result.succ(null);
    }
}
