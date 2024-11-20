package com.huangk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.huangk.entity.Video;
import com.huangk.mapper.VideoMapper;
import com.huangk.service.IImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ImageServiceImpl implements IImageService {

    @Value("${file.upload.path}")
    private String basePath;

    @Autowired
    private VideoMapper videoMapper;

    @Override
    public String getCoverPathByVideoId(Long id) {
        QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("cover_path").eq("id", id);

        Video video = videoMapper.selectOne(queryWrapper);
        if (video == null || video.getCoverPath() == null || video.getCoverPath().isEmpty()) {
            throw new IllegalArgumentException("Cover path not found for video ID: " + id);
        }

        Path coverPath = Paths.get(video.getCoverPath());
        if (coverPath.isAbsolute()) {
            coverPath = Paths.get(basePath, video.getCoverPath());
        }

        System.out.println(coverPath);
        if (!Files.exists(coverPath)) {
            throw new IllegalArgumentException("Cover file not found at path: " + coverPath);
        }

        return coverPath.toString();
    }
}
