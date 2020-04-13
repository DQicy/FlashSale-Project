package com.flashSale.service;

import com.flashSale.error.BusinessException;
import com.flashSale.service.model.OrderModel;

public interface OrderService {

    //方法1.通过前端url上传过来的秒杀活动id,然后下单接口内校验对应id是否属于对应商品求活动已开始
    //方法2.直接在下单接口内判断对应的商品是否存在秒杀活动,若存在进行中的秒杀活动,则以秒杀价格下单

    //创建一个订单
    OrderModel createOrder(Integer userId, Integer itemId, Integer promoId,Integer amount) throws BusinessException;
}
