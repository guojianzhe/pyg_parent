package cn.itcast.core.service;

import cn.itcast.core.pojo.entity.BuyerCart;

import java.util.List;

public interface CartService {


    public List<BuyerCart> addItemToCartList(List<BuyerCart> cartList,Long itemId,Integer num);

    void setCartListToRedis(String username, List<BuyerCart> cartList);

    public List<BuyerCart> getCartListFromRedis(String username);

    List<BuyerCart> mergeCookieCartListToRedisCartList(List<BuyerCart> cookieCartList, List<BuyerCart> redisCartList);
}
