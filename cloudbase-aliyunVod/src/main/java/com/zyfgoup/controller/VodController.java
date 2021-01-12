package com.zyfgoup.controller;

import com.aliyuncs.exceptions.ClientException;
import com.zyfgoup.entity.Result;
import com.zyfgoup.service.VodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


/**
 * @Author Zyfgoup
 * @Date 2021/1/11 13:43
 * @Description
 */
@CrossOrigin
@RestController
public class VodController {

    @Autowired
    private VodService vodService;

    /**
     * 上传视频到阿里云的视频点播  只能上传视频
     * @param file
     * @return
     * @throws Exception
     */
    @PostMapping("/video/upload")
    public Result uploadVideo(MultipartFile file) throws Exception {
        String videoSourceId = vodService.uploadVideo(file);
        return com.zyfgoup.entity.Result.succ(videoSourceId);
    }

    @GetMapping("/video/playauth/{videoId}")
    public Result getVideoPlayAuth(
            @PathVariable String videoId) throws ClientException {
        String playAuth = vodService.getVideoPlayAuth(videoId);
        return Result.succ(playAuth);
    }

    /**
     * 根据视频ID删除视频
     * @return
     */
    @DeleteMapping("/video/{videoSourceId}")
    public Result deleteVideoById(@PathVariable String videoSourceId){
        Boolean flag = vodService.deleteVodById(videoSourceId);
        if(flag){
            return Result.succ(null);
        }
        return Result.fail("删除视频失败");
    }


    /**
     * 批量删除视频
     * @return
     */
    @DeleteMapping("/video/removeList")
    public Result removeVideoList(
            @RequestParam("videoIdList") List videoIdList){
        Boolean flag = vodService.removeVideoList(videoIdList);
        if(flag){
            return Result.succ(null);
        }
        return Result.fail("批量删除视频失败");
    }
}
