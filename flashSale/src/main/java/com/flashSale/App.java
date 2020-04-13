package com.flashSale;

import com.flashSale.dao.UserDOMapper;
import com.flashSale.dataobject.UserDO;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Hello world!
 *
 */
//@EnableAutoConfiguration
//下面这个注解，将com.flashSale根目录的包依次进行扫描，例如servce，component等spring特定的注解
@SpringBootApplication(scanBasePackages = {"com.flashSale"})
@RestController
@MapperScan("com.flashSale.dao")
public class App 
{
    @Autowired
    private UserDOMapper userDOMapper;


    @RequestMapping("/")
    public String home(){

        UserDO userDO = userDOMapper.selectByPrimaryKey(1);
        if(userDO == null){
            return "用户对象不存在";
        }else{
            return userDO.getName();
        }

    }
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        SpringApplication.run(App.class, args);
    }


}
