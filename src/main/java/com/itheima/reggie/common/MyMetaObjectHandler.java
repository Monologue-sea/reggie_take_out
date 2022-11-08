package com.itheima.reggie.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class MyMetaObjectHandler implements MetaObjectHandler {
    //插入时执行
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("插入时执行操作");
        log.info(metaObject.toString());
        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("createUser", BaseContext.getThreadId());
        metaObject.setValue("updateUser", BaseContext.getThreadId());
    }

    //更新时执行
    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("更新时执行操作");
        log.info(metaObject.toString());
        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("updateUser", BaseContext.getThreadId());
    }
}
