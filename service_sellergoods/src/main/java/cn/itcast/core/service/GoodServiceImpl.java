package cn.itcast.core.service;

import cn.itcast.core.dao.good.BrandDao;
import cn.itcast.core.dao.good.GoodsDao;
import cn.itcast.core.dao.good.GoodsDescDao;
import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.pojo.entity.GoodsEntity;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsQuery;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.seller.Seller;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.rmi.MarshalledObject;
import java.util.*;

@Service
@Transactional
public class GoodServiceImpl implements GoodService {

    @Resource
    private GoodsDao goodsDao;
    @Resource
    private GoodsDescDao goodsDescDao;

    @Resource
    private ItemDao itemDao;
    @Autowired
    private ItemCatDao itemCatDao;

    @Autowired
    private BrandDao brandDao;

    @Autowired
    private SellerDao sellerDao;

    @Override
    public void add(GoodsEntity goodsEntity) {
        //1.保存商品详情对象

        goodsEntity.getGoods().setAuditStatus("0");
        goodsDao.insertSelective(goodsEntity.getGoods());

        //2.保存商品详情对象
        Long id = goodsEntity.getGoods().getId();
        goodsEntity.getGoodsDesc().setGoodsId(id);
        goodsDescDao.insertSelective(goodsEntity.getGoodsDesc());
        //3.保存库存集合对象

        insertItem(goodsEntity);

    }

    @Override
    public PageResult findPage(Goods goods, Integer page, Integer rows) {
        PageHelper.startPage(page,rows);

        GoodsQuery query = new GoodsQuery();
        GoodsQuery.Criteria criteria = query.createCriteria();
        if(goods!=null){
            if(goods.getGoodsName()!=null&&!"".equals(goods.getGoodsName())){
                criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
            }

            if(goods.getAuditStatus()!=null&&!"".equals(goods.getAuditStatus())){

                criteria.andAuditStatusEqualTo(goods.getAuditStatus());
            }
            if(goods.getSellerId()!=null&&!"".equals(goods.getSellerId())&&!"admin".equals(goods.getSellerId())){
                criteria.andSellerIdEqualTo(goods.getSellerId());
            }

        }
        Page<Goods> goodsList = (Page<Goods>)goodsDao.selectByExample(query);


        return new PageResult(goodsList.getTotal(),goodsList.getResult());
    }






    private Item setItemValue(GoodsEntity goodsEntity,Item item){


        //商品ID
        item.setGoodsId(goodsEntity.getGoods().getId());
        //创建时间
        item.setCreateTime(new Date());
        //修改时间
        item.setUpdateTime(new Date());
        //库存状态 默认为0 未审核
        item.setStatus("0");
        //分类Id 库存使用商品的第三级分类作为库存分类
        item.setCategoryid(goodsEntity.getGoods().getCategory3Id());
        //分类名称

        ItemCat itemCat = itemCatDao.selectByPrimaryKey(goodsEntity.getGoods().getCategory3Id());

        item.setCategory(itemCat.getName());
        //品牌名称
        Brand brand = brandDao.selectByPrimaryKey(goodsEntity.getGoods().getBrandId());

        item.setBrand(brand.getName());
        //卖家名称
        Seller seller = sellerDao.selectByPrimaryKey(goodsEntity.getGoods().getSellerId());
        item.setSeller(seller.getName());
        //实例图片
        String itemImages = goodsEntity.getGoodsDesc().getItemImages();

        List<Map> maps = JSON.parseArray(itemImages, Map.class);

        if(maps!=null){
            String url = String.valueOf(maps.get(0).get("url"));
            item.setImage(url);
        }

        return item;

    }


    /**
     * 保存库存数据
     */
    private void insertItem(GoodsEntity goodsEntity){
        //是否勾选规格单选框
        if("1".equals(goodsEntity.getGoods().getIsEnableSpec())){

            List<Item> itemList = goodsEntity.getItemList();
            if(itemList!=null){
                for (Item item : itemList) {
                    //库存标题,由商品名+规格组成具体的库存标题,供消费者搜索使用,可以搜索的更精细
                    String title = goodsEntity.getGoods().getGoodsName();
                    //从库存对象中获取前端传入的json格式规格的字符串,例如{"机身内存":"16G","网络":"联通3G"}
                    String specJsonStr = item.getSpec();

                    //将Json对象转换成Map对象
                    Map specMap = JSON.parseObject(specJsonStr, Map.class);
                    Collection values = specMap.values();
                    for (Object value : values) {
                        title+=" "+value;
                    }


                    item.setTitle(title);

                   setItemValue(goodsEntity, item);

                    itemDao.insertSelective(item);
                }
            }

        }else{

            Item item = new Item();
            //价格
            item.setPrice(new BigDecimal("9999999999"));
            //库存量
            item.setNum(0);
            //初始化规格
            item.setSpec("{}");
            //标题
            item.setTitle(goodsEntity.getGoods().getGoodsName());
            //
            setItemValue(goodsEntity,item);

            itemDao.insertSelective(item);
        }

    }
}
