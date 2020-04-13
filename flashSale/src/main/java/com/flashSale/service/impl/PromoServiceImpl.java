package com.flashSale.service.impl;

import com.flashSale.dao.PromoDOMapper;
import com.flashSale.dataobject.PromoDO;
import com.flashSale.service.PromoService;
import com.flashSale.service.model.PromoModel;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PromoServiceImpl implements PromoService {

    @Autowired
    private PromoDOMapper promoDOMapper;

    @Override
    //功能： 获取对应商品的秒杀活动信息
    public PromoModel getPromoByitemId(Integer itemId) {
        PromoDO promoDO = promoDOMapper.selectByItemId(itemId);

        //将dataObject--> model
        PromoModel promoModel = convertFromDataObject(promoDO);

        if(promoModel == null)
            return null;
        //判断当前时间是否秒杀活动即将开始或正在进行
        DateTime now = new DateTime();
        if(promoModel.getStartDate().isAfterNow()){
            promoModel.setStatus(1);
        }else if(promoModel.getEndDate().isBeforeNow()){
            promoModel.setStatus(3);
        }else{
            promoModel.setStatus(2);
        }

        return promoModel;
    }

    private PromoModel convertFromDataObject(PromoDO promoDO){
        if(promoDO == null){
            return null;
        }
        PromoModel promoModel = new PromoModel();
        BeanUtils.copyProperties(promoDO, promoModel);
        promoModel.setPromoItemPrice(new BigDecimal(promoDO.getPromoItemPrice()));

        //注意: model中我们定义的是joda-time的类型,而promoDO给我们生成的是Java的工具类time
        promoModel.setStartDate(new DateTime(promoDO.getStartDate()));
        promoModel.setEndDate(new DateTime(promoDO.getEndDate()));
        return promoModel;
    }
}
