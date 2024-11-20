package com.huangk.controller;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.servlet.ServletUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huangk.base.R;
import com.huangk.entity.Video;
import com.huangk.service.IImageService;
import com.huangk.service.IVideoService;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static jdk.nashorn.internal.runtime.regexp.joni.Config.log;


@RestController
@Slf4j
@RequestMapping("/video")
public class VideoController {

    @Value("${file.upload.path}")
    private String basePath;

    @Autowired
    private IVideoService videoService;

    @Autowired
    private IImageService imageService;

    @GetMapping("/videos")
    public R<List<Video>> getAllVideos() {
        var queryWrapper = videoService.lambdaQuery();
        queryWrapper.orderByDesc(Video::getUpdatedAt);
        List<Video> videos = queryWrapper.list();
        return R.ok(videos);
    }

    @PostMapping()
    public R<Void> saveOrUpdate(@RequestBody Video video) {
        videoService.saveOrUpdate(video);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> del(@PathVariable("id") Integer id) {
        videoService.removeById(id);
        return R.ok();
    }

    @GetMapping("/play/{id}")
    public ResponseEntity<Resource> streamVideoById(
            @PathVariable Long id,
            @RequestHeader(value = "Range", required = false) String rangeHeader) {
        try {
            Video video = videoService.getById(id);
            if (video == null || video.getVideoPath() == null) {
                log.error("Video not found with ID: {}", id);
                return ResponseEntity.notFound().build();
            }

            Path videoPath = Paths.get(basePath, video.getVideoPath());
            if (!Files.exists(videoPath)) {
                log.error("Video file not found at path: {}", videoPath);
                return ResponseEntity.notFound().build();
            }

            File videoFile = videoPath.toFile();
            long fileSize = videoFile.length();

            long rangeStart = 0;
            long rangeEnd = fileSize - 1;
            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                String[] ranges = rangeHeader.substring("bytes=".length()).split("-");
                try {
                    rangeStart = Long.parseLong(ranges[0]);
                    if (ranges.length > 1 && !ranges[1].isEmpty()) {
                        rangeEnd = Long.parseLong(ranges[1]);
                    }
                } catch (NumberFormatException e) {
                    log.error("Invalid Range Header: {}", rangeHeader);
                }
            }

            rangeEnd = Math.min(rangeEnd, fileSize - 1);
            if (rangeStart > rangeEnd || rangeStart < 0 || rangeEnd >= fileSize) {
                log.error("Invalid range: start={}, end={}, fileSize={}", rangeStart, rangeEnd, fileSize);
                return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                        .header("Content-Range", "bytes */" + fileSize)
                        .build();
            }

            long contentLength = rangeEnd - rangeStart + 1;

            byte[] buffer = new byte[(int) contentLength];
            int totalBytesRead = 0;
            try (InputStream inputStream = new BufferedInputStream(new FileInputStream(videoFile))) {
                inputStream.skip(rangeStart);
                while (totalBytesRead < contentLength) {
                    int bytesRead = inputStream.read(buffer, totalBytesRead, (int) (contentLength - totalBytesRead));
                    if (bytesRead == -1) {
                        break;
                    }
                    totalBytesRead += bytesRead;
                }
            }

            ByteArrayResource resource = new ByteArrayResource(buffer);

            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .header("Content-Type", "video/mp4")
                    .header("Accept-Ranges", "bytes")
                    .header("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + fileSize)
                    .contentLength(contentLength)
                    .body(resource);

        } catch (IOException e) {
            log.error("Error streaming video for ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/image/{id}")
    public ResponseEntity<ByteArrayResource> getImageById(@PathVariable Long id) {
        log.info("Requesting image for video ID: {}", id);
        try {
            String filePathStr = imageService.getCoverPathByVideoId(id);
            Path filePath = Paths.get(filePathStr);

            if (!Files.exists(filePath)) {
                log.error("File not found for ID: {}, Path: {}", id, filePath);
                return ResponseEntity.notFound().build();
            }

            byte[] fileBytes = Files.readAllBytes(filePath);
            ByteArrayResource resource = new ByteArrayResource(fileBytes);

            String mimeType = Files.probeContentType(filePath);
            if (mimeType == null) {
                mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            log.info("Returning image for ID: {} with MIME type: {}", id, mimeType);

            return ResponseEntity.ok()
                    .contentLength(fileBytes.length)
                    .contentType(MediaType.parseMediaType(mimeType))
                    .body(resource);

        } catch (IllegalArgumentException e) {
            log.error("Invalid path for ID: {}", id, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Unexpected error for ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
