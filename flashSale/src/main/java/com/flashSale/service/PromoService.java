package com.flashSale.service;

import com.flashSale.service.model.PromoModel;

//秒杀活动服务提供的功能
public interface PromoService {
    //通过商品itemid来查这个商品的秒杀信息（获取即将开始或者正在进行的秒杀活动）
    PromoModel getPromoByitemId(Integer itemId);

}
