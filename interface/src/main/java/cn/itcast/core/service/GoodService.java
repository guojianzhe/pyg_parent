package cn.itcast.core.service;

import cn.itcast.core.pojo.entity.GoodsEntity;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.good.Goods;

public interface GoodService {
    void add(GoodsEntity goodsEntity);

    PageResult findPage(Goods goods, Integer page, Integer rows);
}
