package com.flashSale.error;


//采用的设计模式： 包装器业务异常类实现
public class BusinessException extends Exception implements CommonError {

    private CommonError commonError;

    //直接接收EmBusinessError的传参，用于构造业务异常
    public BusinessException(CommonError commonError){
        super();
        this.commonError = commonError;

    }

    //接收自定义errMsg的方式来构造业务异常
    public BusinessException(CommonError commonError, String errMsg){
        super();
        this.commonError = commonError;
        this.commonError.setErrmsg(errMsg);

    }


    @Override
    public int getErrCode() {
        return this.commonError.getErrCode();
    }

    @Override
    public String getErrMsg() {
        return this.commonError.getErrMsg();
    }

    @Override
    public CommonError setErrmsg(String errMsg) {
        this.commonError.setErrmsg(errMsg);
        return this;
    }
}
