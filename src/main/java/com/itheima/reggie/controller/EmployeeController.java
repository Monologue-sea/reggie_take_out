package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 登录功能
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    //@RequestBody:用来接收前端传给后端的JSON字符串
    public R<Employee> employeeLogin(HttpServletRequest request,@RequestBody Employee employee){
        //1.查询用户，并验证是否有此用户
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();    //查询条件构造器
        wrapper.eq(Employee::getUsername, employee.getUsername());    //查询页面传入用户名是否在数据库中
        Employee emp = employeeService.getOne(wrapper);    //查询到的用户
        if(emp == null){
            return R.error("该用户不存在");
        }
        //2.对比密码，密码是否正确
        String password = DigestUtils.md5DigestAsHex(employee.getPassword().getBytes());
        if(!password.equals(emp.getPassword())){
            return R.error("密码错误");
        }
        //3.对比状态是否异常
        if(emp.getStatus() == 0){
            return R.error("账号状态异常");
        }
        //4.将用户名存到session中
        request.getSession().setAttribute("employee",emp.getId());

        return R.success(emp);
    }

    /**
     * 退出登录
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        request.removeAttribute("employee");
        return R.success("退出登录");
    }
}
