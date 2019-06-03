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
import cn.itcast.core.pojo.good.GoodsDesc;
import cn.itcast.core.pojo.good.GoodsQuery;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.item.ItemQuery;
import cn.itcast.core.pojo.seller.Seller;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
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

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private ActiveMQTopic topicPageAndSolrDestination;

    @Autowired
    private ActiveMQQueue queueSolrDeleteDestination;

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

    @Override
    public GoodsEntity findOne(Long id) {
        //根据商品id查询商品对象
        Goods goods = goodsDao.selectByPrimaryKey(id);
        //根据商品id查询商品详情对象
        GoodsDesc goodsDesc =  goodsDescDao.selectByPrimaryKey(id);
        //根据商品id查询库存集合对象
        ItemQuery query = new ItemQuery();
        query.createCriteria().andGoodsIdEqualTo(id);

        List<Item> items = itemDao.selectByExample(query);
        //4.将以上查询到的结果对象封装到GoodsEntity中返回

        GoodsEntity entity = new GoodsEntity();
        entity.setGoods(goods);
        entity.setGoodsDesc(goodsDesc);
        entity.setItemList(items);



        return entity;
    }

    @Override
    public void update(GoodsEntity goodsEntity) {
        //1.修改商品对象
        goodsDao.updateByPrimaryKeySelective(goodsEntity.getGoods());
        //2.修改商品详情对象
        goodsDescDao.updateByPrimaryKeySelective(goodsEntity.getGoodsDesc());
        //3.根据商品id删除对应的库存集合数据

        ItemQuery query = new ItemQuery();
        query.createCriteria().andGoodsIdEqualTo(goodsEntity.getGoods().getId());

        itemDao.deleteByExample(query);
        //4.添加库存集合数据
        insertItem(goodsEntity);



    }

    @Override
    public void delete(final Long id) {
        /**
         * 1.到数据库中对商品进行逻辑删除
         */
        Goods goods = new Goods();
        goods.setId(id);
        goods.setIsDelete("1");
        goodsDao.updateByPrimaryKeySelective(goods);


        /**
         * 2.将商品id坐位消息发送给消息服务器
         */

        jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {

                TextMessage textMessage = session.createTextMessage(String.valueOf(id));

                return textMessage;
            }
        });

    }

    @Override
    public void updateStatus(final Long id, String status) {
        /**
         * 1.根据商品id到数据库中将商品的上架状态改变
         */

        //1.根据商品id修改商品对象状态码
        Goods goods = new Goods();
        goods.setId(id);
        goods.setAuditStatus(status);
        goodsDao.updateByPrimaryKeySelective(goods);

        //根据商品id修改库存状态码
        Item item = new Item();
        item.setStatus(status);

        ItemQuery query = new ItemQuery();

        query.createCriteria().andGoodsIdEqualTo(id);
        itemDao.updateByExampleSelective(item,query);

        /**
         * 2.将商品id作为消息发送给消息服务器
         */

        if("1".equals(status)){
            jmsTemplate.send(topicPageAndSolrDestination, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    TextMessage textMessage = session.createTextMessage(String.valueOf(id));
                    return textMessage;
                }
            });

        }

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



}
