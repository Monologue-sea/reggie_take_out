package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;

public interface DishService extends IService<Dish> {
    void saveDish(DishDto dishDto);
    //根据id查询菜品信息和对应的口味信息
    DishDto selectDishInfo(Long id);
    //修改菜品信息
    void updateDish(DishDto dishDto);
}
