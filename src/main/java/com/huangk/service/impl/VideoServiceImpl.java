package com.huangk.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huangk.entity.Video;
import com.huangk.mapper.VideoMapper;
import com.huangk.service.IVideoService;
import org.springframework.stereotype.Service;

@Service
public class VideoServiceImpl extends ServiceImpl<VideoMapper, Video> implements IVideoService {
}
