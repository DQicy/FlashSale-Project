package com.flashSale.service.impl;

import com.flashSale.dao.UserDOMapper;
import com.flashSale.dao.UserPasswordDOMapper;
import com.flashSale.dataobject.UserDO;
import com.flashSale.dataobject.UserPasswordDO;
import com.flashSale.error.BusinessException;
import com.flashSale.error.EmBusinessError;
import com.flashSale.service.UserService;
import com.flashSale.service.model.UserModel;
import com.flashSale.validator.ValidationResult;
import com.flashSale.validator.ValidatorImpl;
import org.apache.catalina.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDOMapper userDOMapper;
    @Autowired
    private UserPasswordDOMapper userPasswordDOMapper;

    @Autowired
    private ValidatorImpl validator;
    @Override
    public UserModel getUserById(Integer id) {
        //调用userDOMapper来获取到对应的用户dataobject
        UserDO userDO= userDOMapper.selectByPrimaryKey(id);
        if(userDO == null)
            return null;
        //通过用户id获取对应的用户加密密码信息
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());
        return convertFromDataObject(userDO, userPasswordDO);
    }

    @Override
    @Transactional
    public void register(UserModel userModel) throws BusinessException {
            if(userModel == null){
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
            }
//            if(StringUtils.isEmpty(userModel.getName())
//            ||userModel.getGender() == null
//            ||userModel.getAge() == null
//            ||StringUtils.isEmpty(userModel.getTelephone())){
//                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
//            }

           ValidationResult result = validator.validate(userModel);
            if(result.isHasErrors()){
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, result.getErrMsg());
                //throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);

            }

            //将userModel拆分成userDO和userPasswordDO两块，分别存进两个表中
           UserDO userDO = convertFromModel(userModel);
            try{
                userDOMapper.insertSelective(userDO);
            }catch (DuplicateKeyException ex){
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "手机号不能重复注册");
            }
            userModel.setId(userDO.getId());

            UserPasswordDO userPasswordDO = converPasswordFromModel(userModel);
            userPasswordDOMapper.insertSelective(userPasswordDO);

            return;

    }

    @Override
    public UserModel validateLogin(String telephone, String encrptPassword) throws BusinessException {
        //通过用户的手机获取用户的信息
        UserDO userDO = userDOMapper.selectByTelephone(telephone);
        if(userDO == null){
            throw new BusinessException(EmBusinessError.USER_NOT_EXIST);
        }
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());

        UserModel userModel = convertFromDataObject(userDO, userPasswordDO);

        //比对用户信息内加密的密码是否和传输进来的密码相匹配
        if(! StringUtils.equals(encrptPassword, userModel.getEncrptPassword())){
            throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL);
        }
        return userModel;
    }

    private UserPasswordDO converPasswordFromModel(UserModel userModel){
        if(userModel == null){
            return null;
        }
        UserPasswordDO userPasswordDO = new UserPasswordDO();
        //由于userpasswordDO中的字段比较少，直接通过set字段的方式即可
        userPasswordDO.setEncrptPassword(userModel.getEncrptPassword());
        userPasswordDO.setUserId(userModel.getId());
        return userPasswordDO;
    }

    //实现model->dataObject的方法
    private UserDO convertFromModel(UserModel userModel){
        if(userModel == null){
            return null;
        }
        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(userModel, userDO);
        return userDO;
    }

    private UserModel convertFromDataObject(UserDO userDO, UserPasswordDO userPasswordDO){
        if(userDO == null)
            return null;
        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(userDO, userModel);

        if(userPasswordDO != null) {
            userModel.setEncrptPassword(userPasswordDO.getEncrptPassword());
        }
        return userModel;
    }
}
