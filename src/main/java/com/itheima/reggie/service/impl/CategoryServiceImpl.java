package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.mapper.CategoryMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private DishService dishService;

    @Override
    public void remove(Long id) {

        //setmeal
        //条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(Setmeal::getCategoryId, id);
        //根据id查询，是否有关联
        int count1 = setmealService.count(queryWrapper1);
        if(count1 > 0){
          //如果有关联记录，抛异常
           throw  new CustomException("当前有业务关联，删除失败");
        }

        //Dish
        LambdaQueryWrapper<Dish> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.eq(Dish::getCategoryId, id);
        //根据id查询，是否有关联
        int count2 = dishService.count(queryWrapper2);
        if(count2 > 0){
            //如果有关联记录，抛异常
            throw  new CustomException("当前有业务关联，删除失败");
        }

        //没有关联记录则直接删除
        super.removeById(id);
    }
}
