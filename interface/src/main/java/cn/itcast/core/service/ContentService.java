package cn.itcast.core.service;

import cn.itcast.core.pojo.ad.Content;
import cn.itcast.core.pojo.entity.PageResult;

import java.util.List;

public interface ContentService {

    List<Content> findAll();

    void add(Content category);

    Content findOne(Long id);

    void update(Content category);

    void delete(long[] ids);

    PageResult findPage(Content category, Integer page, Integer rows);

    List<Content> findByCategoryId(Long categoryId);

    List<Content> findByCategoryIdFromRedis(Long categoryId);
}