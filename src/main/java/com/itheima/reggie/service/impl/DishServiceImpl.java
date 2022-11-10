package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 新增菜品和口味
     * @param dishDto
     */
    @Override
    @Transactional
    //@Transactional注解用于添加事务，需要在启动类添加@EnableTransactionManagement
    public void saveDish(DishDto dishDto) {
        //保存菜品
        this.save(dishDto);
        //保存菜品口味
        List<DishFlavor> dishFlavors = dishDto.getFlavors();
        //获取菜品id
        Long dishId = dishDto.getId();
        //将id加入口味表

//        for循环中一般不要出现
//        for (DishFlavor dishFlavor : dishFlavors) {
//            dishFlavor.setDishId(dishId);
//            dishFlavorService.save(dishFlavor);
//        }

        dishFlavors = dishFlavors.stream().map((dishFlavor) -> {
            dishFlavor.setDishId(dishId);
            return dishFlavor;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(dishFlavors);

    }
}
