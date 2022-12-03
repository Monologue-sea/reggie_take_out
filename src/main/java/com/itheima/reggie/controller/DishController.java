package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        //清除缓存
        String key = "dish-" + dishDto.getCategoryId() + "-1";
        redisTemplate.delete(key);
        dishService.saveDish(dishDto);
        return R.success("添加成功！");
    }

    /**
     * 菜品分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> selectPage(int page,int pageSize,String name){
        //分页构造器
        Page<Dish> pageInfo = new Page<>(page,pageSize);
        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null,Dish::getName,name)
                        .orderByDesc(Dish::getUpdateTime);
        dishService.page(pageInfo,queryWrapper);

        //菜品分类名称展示
        Page<DishDto> dtoPageInfo = new Page<>(page,pageSize);
        //将pageInfo除dish外的其他值赋值给dishDto
        BeanUtils.copyProperties(pageInfo,dtoPageInfo,"records");
        //得到所有菜品列表
        List<Dish> dishes = pageInfo.getRecords();
        //创建对应的dishDto列表
        List<DishDto> list = new ArrayList<>();
        //遍历菜品列表
        for (Dish dish : dishes) {
            //创建dishDto对象
            DishDto dishDto = new DishDto();
            //将dish信息复制给dishDto
            BeanUtils.copyProperties(dish,dishDto);
            //得到菜品分类id
            Long categoryId = dish.getCategoryId();
            //根据菜品分类id查找菜品分类
            Category category = categoryService.getById(categoryId);
            //得到分类名称
            if(category != null){
                String categoryName = category.getName();
                //设置dishDto的菜品分类名称
                dishDto.setCategoryName(categoryName);
            }
            list.add(dishDto);
        }
        dtoPageInfo.setRecords(list);
        return R.success(dtoPageInfo);
    }

    /**
     * 显示菜品信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    //因为需要返回一个列表给页面展示数据，所以需要返回DTO
    public R<DishDto> alterDish(@PathVariable Long id){
        DishDto dishDto = dishService.selectDishInfo(id);
        return R.success(dishDto);
    }

    /**
     * 更新菜品信息
     * @param dishDto
     * @return
     */
    @PutMapping
    //不需要向页面返回dto对象，只需要更新数据库信息，所以不用返回dto对象
    public R<String> update(@RequestBody DishDto dishDto){
        //清除缓存
        String key = "dish-" + dishDto.getCategoryId() + "-1";
        redisTemplate.delete(key);
        dishService.updateDish(dishDto);
        return R.success("修改菜品成功！");
    }

    /**
     * 根据条件查询对应菜品数据
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        List<DishDto> dishDtoList = null;
        //设置key
        String key = "dish-" + dish.getCategoryId() + "-" + dish.getStatus();
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);
        //如果存在，直接返回
        if(dishDtoList != null){
            return R.success(dishDtoList);
        }

        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null,Dish::getCategoryId,dish.getCategoryId())
                .eq(Dish::getStatus,1)
                .orderByAsc(Dish::getSort)
                .orderByDesc(Dish::getUpdateTime);
        List<Dish> dishList = dishService.list(queryWrapper);

        dishDtoList = dishList.stream().map((dishItem)->{
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(dishItem,dishDto);
            //设置dishDto的categoryName
            Long categoryId = dishItem.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if(category != null){
                dishDto.setCategoryName(category.getName());
            }
            //设置dishDto的flavors（口味）
            LambdaQueryWrapper<DishFlavor> flavorWrapper = new LambdaQueryWrapper<>();
            flavorWrapper.eq(DishFlavor::getDishId,dish.getId());
            List<DishFlavor> flavors = dishFlavorService.list(flavorWrapper);
            dishDto.setFlavors(flavors);
            return dishDto;
        }).collect(Collectors.toList());
        // 如果不存在将dishDtoList存储value中
        redisTemplate.opsForValue().set(key,dishDtoList,60, TimeUnit.MINUTES);
        return R.success(dishDtoList);
    }

    /*
    设置linux的静态IP：
        修改/etc/sysconfig/network-scripts/ifcfg-ens33，修改内容如下：
            BROWSER_ONLY="no"后添加
            BOOTPROTO="static"
            IPADDR="192.168.164.100"
            NETMASK="255.255.255.0"
            GATEWAY="192.168.164.2"
            DNS1="192.168.164.2"
     */

    /*
    Spring Cache：@EnableCaching--开启缓存功能（启动类注解）
                  @Cacheable--在方法执行前spring先查看缓存中是否有数据，如果有数据，则直接返回缓存数据，若没有数据，则调用方法并将方法的返回值放入缓存中（方法注解）
                  @CachePut（value，key）--将方法的返回值放入缓存中（方法注解）
                  @CacheEvict：将一条或者多条数据从缓存中删除（方法注解）

    使用：1.只需要导入相关的maven坐标，spring-boot-starter-cache
         2.key的获取可以使用表达式语言--#参数，result：返回值，形参名
         3.value：缓存的名称，每个缓存下面可以有多个key，key：缓存的key，condition：条件，满足条件时才缓存数据
     */

    /*
    主从复制：（主库master，从库slave）
            过程：① 将改变记录到二进制日志中（binary log）    ② slave将master的日志拷贝到它的中继日志（relay log）     ③ slave重做中继日志中的事件，将改变应用到自己的数据库中
            主库的配置步骤：
              第一步：修改mysql数据库的配置文件/etc/my.cnf
                   [mysqld]
                   log-bin = mysql-bin  #启用二进制日志
                   server-id=100  #服务器唯一id
              第二步：重启mysql--systemctl restart mysqld
              第三步：登录mysql数据库，执行 grant replication slave on *.* to 'xiaowang'@'%' identified by 'Root@123456'
              第四步：登录数据库 show master status
            从库的配置步骤：
              第一步：修改mysql数据库的配置文件/etc/my.cnf
                   [mysqld]
                   server-id = 101 --- 服务器id
              第二步：重启mysql--systemctl restart mysqld
              第三步：登录数据库，执行sql
              第四步：查看从库状态：show slave status；
     */

    /*
    读写分离：
          sharding-jdbc：轻量级的java框架
          步骤：
              1.导入maven坐标
              2.在配置文件中配置读写分离规则
              3.在配置文件中允许bean定义覆盖配置项
     */
}
