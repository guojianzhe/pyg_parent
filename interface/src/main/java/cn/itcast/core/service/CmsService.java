package cn.itcast.core.service;

import java.io.IOException;
import java.util.Map;

public interface CmsService {

    public void createStaticPage(Long goodsId, Map<String,Object> rootMap) throws Exception;


    public Map<String,Object> findGoodsData(Long goodsId);
}
