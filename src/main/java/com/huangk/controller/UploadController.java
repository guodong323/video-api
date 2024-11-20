package com.huangk.controller;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.file.PathUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.huangk.base.R;
import com.huangk.entity.Video;
import com.huangk.service.IVideoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.UUID;
import java.nio.file.StandardCopyOption;


import static cn.hutool.core.img.ImgUtil.*;

/**
 * @author zhanghao
 */
@RestController
@Slf4j
public class UploadController {

    @Value("${file.upload.path}")
    private String path;

    @Resource
    private IVideoService videoService;

    @PostMapping("/upload")
    public R<String> create(@RequestPart(name = "file") MultipartFile file,
                            @RequestPart(name = "cover") MultipartFile cover,
                            @RequestParam(required = false) String title,
                            @RequestParam(required = false) String tag) {
        try {
            String fileName = file.getOriginalFilename();
            String suffix = FileUtil.getSuffix(fileName);
            if (!ListUtil.of("mp4", "mov").contains(suffix)) {
                return R.fail("Unsupported video file type: " + suffix);
            }

            Path videoDir = Paths.get(path, "video", DateUtil.date().toDateStr());
            if (!FileUtil.exist(videoDir.toString())) {
                PathUtil.mkdir(videoDir);
            }

            String uniqueVideoFileName = UUID.randomUUID() + "." + suffix;
            Path videoFilePath = Paths.get(videoDir.toString(), uniqueVideoFileName);

            IoUtil.copy(file.getInputStream(), Files.newOutputStream(videoFilePath));

            Path imageDir = Paths.get(path, "image", DateUtil.date().toDateStr());
            if (!FileUtil.exist(imageDir.toString())) {
                PathUtil.mkdir(imageDir);
            }

            String uniqueCoverFileName = UUID.randomUUID() + ".jpg";
            Path coverFilePath = Paths.get(imageDir.toString(), uniqueCoverFileName);

            IoUtil.copy(cover.getInputStream(), Files.newOutputStream(coverFilePath));

            Video video = new Video();
            video.setCreatedAt(new Date());
            video.setUpdatedAt(new Date());
            video.setTitle(title);
            video.setTag(tag);
            video.setVideoDuration(getVideoDuration(videoFilePath.toString()));

            video.setVideoPath(CharSequenceUtil.removePrefix(videoFilePath.toString(), path));
            video.setCoverPath(CharSequenceUtil.removePrefix(coverFilePath.toString(), path));
            video.setVideoSize((int) file.getSize());

            videoService.save(video);

            return R.ok(video.getVideoPath());
        } catch (IOException e) {
            log.error("File upload failed", e);
            return R.fail("File upload failed: " + e.getMessage());
        }
    }

    private double getVideoDuration(String videoFilePath) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "ffmpeg", "-i", videoFilePath, "-f", "null", "-"
            );
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("Duration:")) {
                        String durationStr = line.split("Duration:")[1].trim().split(",")[0];
                        return parseDuration(durationStr);
                    }
                }
            }
        } catch (IOException e) {
            log.error("Error while fetching video duration: ", e);
        }
        return 0.0;
    }

    private double parseDuration(String durationStr) {
        String[] parts = durationStr.split(":");
        double hours = Double.parseDouble(parts[0]);
        double minutes = Double.parseDouble(parts[1]);
        double seconds = Double.parseDouble(parts[2]);
        return hours * 3600 + minutes * 60 + seconds;
    }
}
