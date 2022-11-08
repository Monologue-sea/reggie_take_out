package com.itheima.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.sql.SQLIntegrityConstraintViolationException;

@Slf4j
@ControllerAdvice(annotations = {RestController.class, Controller.class})
@ResponseBody
public class GlobalExceptionHandle {

    /**
     * 账号重复异常处理
     *
     * @param exception
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> SQLException(SQLIntegrityConstraintViolationException exception) {
        //获取异常信息
        String message = exception.getMessage();
        //异常处理
        if (message.contains("Duplicate entry")) {    //contains（xxx）：判断是否包含该字符串
            String[] s = message.split(" ");
            String msg = s[2];
            return R.error(msg + "已存在");
        }
        return R.error("未知错误");
    }

    /**
     * 菜品关联异常处理
     * @param exception
     * @return
     */
    @ExceptionHandler(CustomException.class)
    public R<String> CustomException(CustomException exception) {
        //获取异常信息
        String message = exception.getMessage();
        return R.error(message);
    }
}
