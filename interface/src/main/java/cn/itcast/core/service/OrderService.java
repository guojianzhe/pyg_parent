package cn.itcast.core.service;

import cn.itcast.core.pojo.log.PayLog;
import cn.itcast.core.pojo.order.Order;

public interface OrderService {

    void add(Order order);

    PayLog getPayLogByusername(String username);

    void updatePayStatus(String username);
}
