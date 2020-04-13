package com.flashSale.controller;

import com.flashSale.error.BusinessException;
import com.flashSale.error.EmBusinessError;
import com.flashSale.response.CommonReturnType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class BaseController {
    public static final String CONTENT_TYPE_FORMED = "application/x-www-form-urlencoded";

    //定义ExceptionHandler解决未被controller层吸收的exception
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Object handlerException(HttpServletRequest request, Exception ex){
        Map<String, Object> responseData = new HashMap<>();
        //判断是否为业务逻辑上的错误
        if(ex instanceof BusinessException){
            BusinessException businessException = (BusinessException)ex;
            responseData.put("errCode", businessException.getErrCode());
            responseData.put("errMsg", businessException.getErrMsg());

        }else{

            responseData.put("errCode", EmBusinessError.UKNOWN_ERROR.getErrCode());
            responseData.put("errMsg", EmBusinessError.UKNOWN_ERROR.getErrMsg());
        }

        return CommonReturnType.create(responseData, "fail");

    }
}
