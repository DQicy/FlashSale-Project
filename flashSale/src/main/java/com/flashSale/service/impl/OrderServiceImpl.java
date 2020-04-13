package com.flashSale.service.impl;

import com.flashSale.dao.OrderDOMapper;
import com.flashSale.dao.SequenceDOMapper;
import com.flashSale.dataobject.OrderDO;
import com.flashSale.dataobject.SequenceDO;
import com.flashSale.error.BusinessException;
import com.flashSale.error.EmBusinessError;
import com.flashSale.service.ItemService;
import com.flashSale.service.OrderService;
import com.flashSale.service.UserService;
import com.flashSale.service.model.ItemModel;
import com.flashSale.service.model.OrderModel;
import com.flashSale.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import sun.jvm.hotspot.jdi.StringReferenceImpl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderDOMapper orderDOMapper;

    @Autowired
    private SequenceDOMapper sequenceDOMapper;

    @Override
    @Transactional
    //创建订单
        public OrderModel createOrder(Integer userId, Integer itemId, Integer promoId, Integer amount) throws BusinessException {
            //1.校验下单状态， 下单的商品是否存在，用户是否合法，购买数量是否正确

        //首先检验商品是否存在
        ItemModel itemModel =  itemService.getItemById(itemId);
        if(itemModel == null){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "商品信息不存在");
        }
        //检验用户是否合法
        UserModel userModel = userService.getUserById(userId);
        if(userModel == null){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "用户信息不存在");
        }
        //检验用户下单数量（不能太多，也不能太少）
        if(amount <= 0 || amount > 99){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "数量信息不正确");
        }

        //校验活动信息
        if(promoId != null){
            //(1)校验对应活动是否存在这个适用商品
            if(promoId.intValue() != itemModel.getPromoModel().getId()){
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "活动信息不正确");
            //(2)校验活动是否正在进行中
            }else if(itemModel.getPromoModel().getStatus().intValue() != 2){
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "活动还未开始");
            }
        }

            //2.两种常见的下单方法： （1）落单减库存，（2）支付减库存(这种方法无法保证支付成功之后，库存还有，
            // 这种方式是无法保证用户不会超卖商家的商品,这种情况的话商家需要给用户退款)
            //落单减库存的话，会发生用户恶意多次下单的情况，而支付减库存使得商家会注意到这个，但退款会比较麻烦
            //本项目中采用落单减库存的方法，即用户只要下单就将库存锁住（一定能下单成功）
        boolean result = itemService.decreaseStock(itemId, amount);
        if(! result){
            throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH);
        }

            //3.订单入库
        OrderModel orderModel = new OrderModel();
        orderModel.setUserId(userId);
        orderModel.setItemId(itemId);
        orderModel.setAmount(amount);
        if(promoId != null){
            //活动情况下的itemprice
            orderModel.setItemPrice(itemModel.getPromoModel().getPromoItemPrice());
        }else{
            //如果非活动,则直接取平销信息
            orderModel.setItemPrice(itemModel.getPrice());
        }

        //orderModel.setOrderPrice(itemModel.getPrice().multiply(new BigDecimal(amount)));
        orderModel.setOrderPrice(orderModel.getItemPrice().multiply(new BigDecimal(amount)));
        orderModel.setPromoId(promoId);

        //生成交易流水号，订单号
        orderModel.setId(generateOrderNo());

        OrderDO orderDO = convertFromOrderModel(orderModel);
        orderDOMapper.insertSelective(orderDO);

        //加上商品的销量
        itemService.increaseSales(itemId, amount);

        //4.返回前端

        return orderModel;

        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        private String generateOrderNo(){
            //订单号有16位
            StringBuilder stringBuilder = new StringBuilder();
            //前8位为时间信息，年月日 jdk8中使用localdatetime
            LocalDateTime now = LocalDateTime.now();
            String nowDate = now.format(DateTimeFormatter.ISO_DATE).replace("-", "");
            stringBuilder.append(nowDate);

            //中间6位为自增序列(用这个自增序列保证在相同时间点的订单不重复)

            //获取当前sequence
            int sequence = 0;
            SequenceDO sequenceDO = sequenceDOMapper.getSequenceByName("order_info");
            sequence = sequenceDO.getCurrentValue();
            sequenceDO.setCurrentValue(sequenceDO.getCurrentValue() + sequenceDO.getStep());
            sequenceDOMapper.updateByPrimaryKeySelective(sequenceDO);

            //将其凑足6位
            String sequenceStr = String.valueOf(sequence);
            for(int i = 0; i < 6 - sequenceStr.length(); i++){
                stringBuilder.append(0);
            }
            stringBuilder.append(sequenceStr);

            //最后2位为分库分表位（00 ~ 99) 用作之后的分库拆表等等
            stringBuilder.append("00");
            return  stringBuilder.toString();
        }

        private OrderDO convertFromOrderModel(OrderModel orderModel){
        if(orderModel == null)
            return null;
        OrderDO orderDO = new OrderDO();
        BeanUtils.copyProperties(orderModel, orderDO);
        orderDO.setItemPrice(orderModel.getItemPrice().doubleValue());
        orderDO.setOrderPrice(orderModel.getOrderPrice().doubleValue());
        return orderDO;
        }
}
