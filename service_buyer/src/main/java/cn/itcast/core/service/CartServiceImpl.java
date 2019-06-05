package cn.itcast.core.service;


import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.entity.BuyerCart;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.order.OrderItem;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private ItemDao itemDao;

    /**
     * 将商品加入到这个人的现有的购物车列表中
     * @param cartList    这个人现有的购物车列表
     * @param itemId       商品库存id
     * @param num           购买数量
     * @return
     */

    @Override
    public List<BuyerCart> addItemToCartList(List<BuyerCart> cartList, Long itemId, Integer num) {

        //1. 根据商品SKU ID查询SKU商品信息

        Item item = itemDao.selectByPrimaryKey(itemId);

        //2. 判断商品是否存在不存在, 抛异常
        if(item==null){
            throw new RuntimeException("此商品不存在!");
        }
        //3. 判断商品状态是否为1已审核, 状态不对抛异常
        if(!"1".equals(item.getStatus())){
            throw new RuntimeException("此商品审核未通过,不允许购买!");
        }
        //4.获取商家ID
        String sellerId = item.getSeller();
        //5.根据商家ID查询购物车列表中是否存在该商家的购物车
        BuyerCart buyerCart = findBuyerBySellerId(cartList, sellerId);

        //6.判断如果购物车列表中不存在该商家的购物车
        if(buyerCart==null){
            //6.a.1 新建购物车对象
            buyerCart = new BuyerCart();
            //创建购物项集合
            List<OrderItem> orderItemList = new ArrayList<>();
            //创建购物项
            OrderItem orderItem = createOrderItem(item, num);
            //将购物项加入到购物集合中
            //将购物项集合加入到购物车中

            orderItemList.add(orderItem);
            buyerCart.setOrderItemList(orderItemList);
            //6.a.2 将新建的购物车对象添加到购物车列表
            cartList.add(buyerCart);
        }else {
            //6.b.1如果购物车列表中存在该商家的购物车 (查询购物车明细列表中是否存在该商品)
            List<OrderItem> orderItemList = buyerCart.getOrderItemList();
            //6.b.2判断购物车明细是否为空
            //6.b.3为空，新增购物车明细
            //6.b.4不为空，在原购物车明细上添加数量，更改金额
            //6.b.5如果购物车明细中数量操作后小于等于0，则移除
            //6.b.6如果购物车中购物车明细列表为空,则移除
        }



        //7. 返回购物车列表对象

        return null;
    }

    /**
     * 从当前购物项集合中查询是否存在这个商品,存在则返回这个购物项对象,不存在则返回null
     * @param orderItemList
     * @return
     */
    private OrderItem findOrderItemByItemId(List<OrderItem> orderItemList){
        if(orderItemList!=null){

        }


    }

    /**
     * 创建购物项对象
     * @param item 库存对象
     * @param num 购买数量
     * @return
     */
    private OrderItem createOrderItem(Item item,Integer num){
        if(num<=0){
            throw new RuntimeException("购买数量非法!");
        }

        OrderItem orderItem = new OrderItem();
        //购买数量
        orderItem.setNum(num);
        //商品id
        orderItem.setGoodsId(item.getGoodsId());
        //库存id
        orderItem.setItemId(item.getId());
        //实例图片
        orderItem.setPicPath(item.getImage());
        //单价
        orderItem.setPrice(item.getPrice());
        //卖家id
        orderItem.setSellerId(item.getSellerId());
        //商品库存标题
        orderItem.setTitle(item.getTitle());
        //总价 = 单价乘以数量
        orderItem.setTotalFee(item.getPrice().multiply(new BigDecimal(num)));
        return orderItem;

    }


    /**
     * 查询次购物车中有没有这个卖家的购物车对象
     * @param sellerId
     * @return
     */
    private BuyerCart findBuyerBySellerId(List<BuyerCart> cartList,String sellerId){

        if(cartList!=null){
            for (BuyerCart buyerCart : cartList) {
                if(buyerCart.getSellerId().equals(sellerId)){
                    return buyerCart;
                }

            }
        }


        return null;
    }

    @Override
    public void setCartListToRedis(String username, List<BuyerCart> cartList) {

    }

    @Override
    public List<BuyerCart> getCartListFromRedis(String username) {
        return null;
    }

    @Override
    public List<BuyerCart> mergeCookieCartListToRedisCartList(List<BuyerCart> cookieCartList, List<BuyerCart> redisCartList) {
        return null;
    }
}
