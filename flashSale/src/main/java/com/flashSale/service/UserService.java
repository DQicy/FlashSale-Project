package com.flashSale.service;

import com.flashSale.error.BusinessException;
import com.flashSale.service.model.UserModel;

public interface UserService {

    //通过用户ID来获取用户对象的方法
    UserModel getUserById(Integer id);

    //用户信息的注册
    void register(UserModel userModel) throws BusinessException;

    //telephone 代表用户注册的手机， password代表用户加密后的密码
    UserModel validateLogin(String telephone, String encrptPassword) throws BusinessException;
}
