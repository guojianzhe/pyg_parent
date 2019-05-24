package cn.itcast.core.service;

import cn.itcast.core.pojo.ad.ContentCategory;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.good.Brand;

import java.util.List;
import java.util.Map;

public interface CategoryService {

    public PageResult findPage(ContentCategory contentCategory, Integer page, Integer rows);

    public void add(ContentCategory contentCategory);

    public ContentCategory findOne(Long id);

    public void update(ContentCategory contentCategory);

    public void delete(long[] ids);

    List<ContentCategory> findAll();
}
