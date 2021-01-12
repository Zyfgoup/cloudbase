package com.zyfgoup.service.impl;

import com.aliyun.vod.upload.impl.UploadVideoImpl;
import com.aliyun.vod.upload.req.UploadStreamRequest;
import com.aliyun.vod.upload.resp.UploadStreamResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.vod.model.v20170321.DeleteVideoRequest;
import com.aliyuncs.vod.model.v20170321.DeleteVideoResponse;
import com.aliyuncs.vod.model.v20170321.GetVideoPlayAuthRequest;
import com.aliyuncs.vod.model.v20170321.GetVideoPlayAuthResponse;
import com.zyfgoup.service.VodService;
import com.zyfgoup.utils.AliyunVodSDKUtil;
import com.zyfgoup.utils.VodConstantPropertiesUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @Author Zyfgoup
 * @Date 2021/1/11 13:44
 * @Description
 */
@Service
@Slf4j
public class VodServiceImpl implements VodService {
    @Override
    public String uploadVideo(MultipartFile file) {
        try {
            InputStream inputStream = file.getInputStream();
            String originalFilename = file.getOriginalFilename();
            String title = originalFilename.substring(0, originalFilename.lastIndexOf("."));

            UploadStreamRequest request = new UploadStreamRequest(
                    VodConstantPropertiesUtil.ACCESS_KEY_ID,
                    VodConstantPropertiesUtil.ACCESS_KEY_SECRET,
                    title, originalFilename, inputStream);

            UploadVideoImpl uploader = new UploadVideoImpl();
            UploadStreamResponse response = uploader.uploadStream(request);

            //如果设置回调URL无效，不影响视频上传，可以返回VideoId同时会返回错误码。
            // 其他情况上传失败时，VideoId为空，此时需要根据返回错误码分析具体错误原因
            String videoId = response.getVideoId();
            if (!response.isSuccess()) {
                String errorMessage = "阿里云上传错误：" + "code：" + response.getCode() + ", message：" + response.getMessage();
                log.warn(errorMessage);
                if (StringUtils.isEmpty(videoId)) {
                    //抛出自定义异常
                    log.error("抛出异常 上传失败");
                }
            }

            return videoId;
        } catch (IOException e) {
            //抛出自定义异常
            return null;
        }

    }

    @Override
    public String getVideoPlayAuth(String videoId) throws ClientException {
        try {
            //初始化
            DefaultAcsClient client = AliyunVodSDKUtil.initVodClient(
                    VodConstantPropertiesUtil.ACCESS_KEY_ID,
                    VodConstantPropertiesUtil.ACCESS_KEY_SECRET);

            //请求
            GetVideoPlayAuthRequest request = new GetVideoPlayAuthRequest();
            request.setVideoId(videoId);

            //响应
            GetVideoPlayAuthResponse response = client.getAcsResponse(request);

            //得到播放凭证
            String playAuth = response.getPlayAuth();

            return playAuth;
        } catch (ClientException e) {
            e.printStackTrace();
           return null;
        }
    }


    @Override
    public Boolean deleteVodById(String videoSourceId) {
        try {
            DefaultAcsClient client = AliyunVodSDKUtil.initVodClient(
                    VodConstantPropertiesUtil.ACCESS_KEY_ID,
                    VodConstantPropertiesUtil.ACCESS_KEY_SECRET);

            DeleteVideoResponse response = new DeleteVideoResponse();
            DeleteVideoRequest request = new DeleteVideoRequest();
            //支持传入多个视频ID，多个用逗号分隔
            request.setVideoIds(videoSourceId);

            response = client.getAcsResponse(request);

            return true;
        } catch (Exception e) {
            log.warn("ErrorMessage = " + e.getLocalizedMessage());
            return false;
        }

    }

    @Override
    public Boolean removeVideoList(List videoIdList) {
        try {
            //初始化
            DefaultAcsClient client = AliyunVodSDKUtil.initVodClient(
                    VodConstantPropertiesUtil.ACCESS_KEY_ID,
                    VodConstantPropertiesUtil.ACCESS_KEY_SECRET);

            //创建请求对象
            //一次只能批量删20个
            String str = org.apache.tomcat.util.buf.StringUtils.join(videoIdList,',');
            DeleteVideoRequest request = new DeleteVideoRequest();
            request.setVideoIds(str);

            //获取响应
            DeleteVideoResponse response = client.getAcsResponse(request);

            System.out.print("RequestId = " + response.getRequestId() + "\n");

            return true;
        } catch (ClientException e) {
           //抛出自定义异常
            log.error("批量删除视频失败");
            return false;
        }
    }

}
