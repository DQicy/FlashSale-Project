package com.flashSale.validator;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.xml.validation.Validator;
import java.util.Set;

@Component
public class ValidatorImpl implements InitializingBean {
    private javax.validation.Validator validator;
    //实现校验方法并返回校验结果
    public ValidationResult validate(Object bean){
        final ValidationResult result = new ValidationResult();
        //如果bean里面的一些参数违背了validation里面的规则，那么set里面就会有这个值
        Set<ConstraintViolation<Object>> constraintViolationSet = validator.validate(bean);
        if(constraintViolationSet.size() > 0){
            //说明有错误
            result.setHasErrors(true);
            //遍历这个constraintViolationSet
            //使用lamba表达式
            constraintViolationSet.forEach(constraintViolation->{
                String errMsg = constraintViolation.getMessage();
                String propertyName = constraintViolation.getPropertyPath().toString();
                result.getErrorMsgMap().put(propertyName, errMsg);
            });

        }
        return  result;
    }
    //当spring将这个bean加载完之后，回调下面的函数
    @Override
    public void afterPropertiesSet() throws Exception {
        //将hibernate validator通过工厂的初始化方式使其实例化
        //实现了一个javax接口的validator校验器
        this.validator =  Validation.buildDefaultValidatorFactory().getValidator();
    }
}
