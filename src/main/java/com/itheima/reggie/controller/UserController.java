package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;

import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        //获取发送过来的手机号
        String phone = user.getPhone();
        if(!(phone.isEmpty())){
            //获取验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("验证码为：{}",code);
            //发送短信
            //SMSUtils.sendMessage("瑞吉外卖","",phone,code);
            //session.setAttribute(phone,code);

            //将验证码缓存到redis中，设置过期时间为5分钟
            redisTemplate.opsForValue().set(phone,code,5, TimeUnit.MINUTES);

            return R.success("验证码已发送！");
        }
        return R.error("验证码发送失败！");
    }

    @PostMapping("/login")
    public R<User> login(@RequestBody HashMap map,HttpSession session){
        //log.info(map.toString());
        //得到手机号
        String phone = map.get("phone").toString();
        //得到code
        String code = map.get("code").toString();

        //得到保存的code
        //Object codeInfo = session.getAttribute(phone);
        Object codeInfo= redisTemplate.opsForValue().get(phone);

        if( codeInfo != null && codeInfo.equals(code)){
            //条件构造器
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);
            //查询user
            User user = userService.getOne(queryWrapper);
            //如果是新用户直接注册
            if(user == null){
                //创建新用户
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            session.setAttribute("user",user.getId());
            //删除redis中数据
            redisTemplate.delete(phone);
            return R.success(user);
        }
           return R.error("登陆失败");
    }
}
