package cn.itcast.core.service;

import cn.itcast.core.dao.good.GoodsDao;
import cn.itcast.core.dao.good.GoodsDescDao;
import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsDesc;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.item.ItemQuery;
import com.alibaba.dubbo.config.annotation.Service;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.servlet.ServletContext;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CmsServiceImpl implements CmsService,ServletContextAware {
    @Autowired
    private GoodsDao goodsDao;
    @Autowired
    private GoodsDescDao goodsDescDao;
    @Autowired
    private ItemDao itemDao;
    @Autowired
    private ItemCatDao itemCatDao;

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    private ServletContext servletContext;


    @Override
    public void createStaticPage(Long goodsId, Map<String, Object> rootMap) throws Exception {
        //1.获取模板初始化对象
        Configuration configuration = freeMarkerConfigurer.getConfiguration();
        //2.获取模板对象
        Template template = configuration.getTemplate("item.ftl");
        //3.创建输出流,指定生成静态页面的位置和名称
        String path = goodsId+".html";
        System.out.println("====="+path);
        String realPath = getRealPath(path);

        Writer out = new OutputStreamWriter(new FileOutputStream(new File(realPath)),"utf-8");
        //4.生成
        template.process(rootMap,out);
        //5.关闭流
        out.close();
    }

    /**
     * 将相对路径转换成绝对路径
     * @param path 相对路径
     * @return
     */
    private String getRealPath(String path){
        String realPath = servletContext.getRealPath(path);
        System.out.println("====="+realPath);
        return realPath;
    }

    @Override
    public Map<String, Object> findGoodsData(Long goodsId) {
        Map<String, Object> maps= new HashMap<>();
        //1.获取商品数据
        Goods goods = goodsDao.selectByPrimaryKey(goodsId);
        //2.获取商品详情数据
        GoodsDesc goodsDesc = goodsDescDao.selectByPrimaryKey(goodsId);
        //3.获取库存集合数据
        ItemQuery query = new ItemQuery();
        query.createCriteria().andGoodsIdEqualTo(goodsId);
        List<Item> itemList =   itemDao.selectByExample(query);

        //4.获取商品对应的分类数据
        if(goods!=null){
            ItemCat itemCat1 = itemCatDao.selectByPrimaryKey(goods.getCategory1Id());
            ItemCat itemCat2 = itemCatDao.selectByPrimaryKey(goods.getCategory2Id());
            ItemCat itemCat3 = itemCatDao.selectByPrimaryKey(goods.getCategory3Id());
            maps.put("itemCat1",itemCat1.getName());
            maps.put("itemCat2",itemCat2.getName());
            maps.put("itemCat3",itemCat3.getName());
        }


        //5.将商品的所有数据封装成map返回
        maps.put("goods",goods);
        maps.put("goodsDesc",goodsDesc);
        maps.put("itemList",itemList);

        return maps;
    }

    /**
     * 由于当前项目是service项目,没有配置springMvc所以没有初始或servletContext对象
     * 但是我们这个项目配置了spring,spring中serviceContextAware接口,这个接口用servletContext对象
     * 这个是spring初始化好的,所以我们实现ServletContextAware就扣,目的就是使用里面的servletContext对象给我们当前类上的servletContext对象赋值
     * @param servletContext
     */
    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
}
