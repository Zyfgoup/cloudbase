package com.zyfgoup.service;

import com.aliyuncs.exceptions.ClientException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @Author Zyfgoup
 * @Date 2021/1/11 13:44
 * @Description
 */
public interface VodService {

    String uploadVideo(MultipartFile file);

    String getVideoPlayAuth(String videoId) throws ClientException;

    Boolean deleteVodById(String videoSourceId);

    /**
     * 批量删除
     * @param videoIdList
     * @return
     */
    Boolean removeVideoList(List videoIdList);
}
