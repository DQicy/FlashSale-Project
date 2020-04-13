package com.flashSale.error;

public interface CommonError {
    public int getErrCode();

    public String getErrMsg();

    public CommonError setErrmsg(String errMsg);

}
