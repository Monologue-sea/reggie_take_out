package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;

import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 员工管理
 */
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
    //@ResponseBody：将java对象转为json格式的数据
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

    /**
     * 添加员工
     * @param request
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest request,@RequestBody Employee employee){
        //1.赋初始密码值（MD5加密）
        String password = "12345";
        employee.setPassword(DigestUtils.md5DigestAsHex(password.getBytes()));
        //2.设置创建时间
        //employee.setCreateTime(LocalDateTime.now());
        //3.设置更新时间
        //employee.setUpdateTime(LocalDateTime.now());
        //4.设置创建人id
        //Long empId = (Long)request.getSession().getAttribute("employee");
        //employee.setCreateUser(empId);
        //5.设置更新人id
        //employee.setUpdateUser(empId);
        //6.存储用户
        employeeService.save(employee);
        return R.success("添加成功！");
    }

    /**
     * 员工信息分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        //构造分页构造器
        Page pageInfo = new Page(page,pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper();
        wrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);
        //设置排序条件
        wrapper.orderByDesc(Employee::getUpdateTime);
        employeeService.page(pageInfo,wrapper);
        return R.success(pageInfo);
    }

    /**
     * 更新员工信息
     * @param request
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest request,@RequestBody Employee employee){
        //修改更新时间
        //log.info(employee.toString());
        //employee.setUpdateTime(LocalDateTime.now());
        //获取当前用户id
        //Long userId = (Long)request.getSession().getAttribute("employee");
        //修改更新人
        //employee.setUpdateUser(userId);
        //完成跟新操作
        employeeService.updateById(employee);
         return R.success("更新成功！");
    }

    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> redactEmployee(@PathVariable Long id){
        //根据id查询员工信息
        Employee employee = employeeService.getById(id);
        if(employee != null){
            return R.success(employee);
        }
        return R.error("没有查询到对应用户信息");
    }
}
