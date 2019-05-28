package cn.itcast.core.service;

import cn.itcast.core.pojo.ad.Content;
import cn.itcast.core.pojo.entity.PageResult;

import java.util.List;

public interface ContentService {
    List<Content> findAll();

    void add(Content content);

    Content findOne(Long id);

    void update(Content content);

    void delete(long[] ids);

    PageResult findPage(Content content, Integer page, Integer rows);

    public List<Content> findByCategoryId(Long categoryId);

    public List<Content> findByCategoryIdFromRedis(Long categoryId);
}
