package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 保存套餐分类信息
     * @param setmealDto
     */
    @Override
    @Transactional
    public void saveSetmeal(SetmealDto setmealDto) {
        //保存套餐信息
        this.save(setmealDto);

        //保存套餐菜品信息
        List<SetmealDish> mealDishes = setmealDto.getSetmealDishes();
        mealDishes= mealDishes.stream().map((mealDish) -> {
            mealDish.setSetmealId(setmealDto.getId());
            return mealDish;
        }).collect(Collectors.toList());

//        for (SetmealDish setmealDish : setmealDishes) {
//            setmealDish.setSetmealId(setmealDto.getId());
//        }

        setmealDishService.saveBatch(mealDishes);
    }

    /**
     * 删除套餐及其关联信息
     * @param ids
     */
    @Override
    public void removeWithSetmeal(List<Long> ids) {
        /*
        函数使用语法：select  函数(列名) from 表名；
        删除通用语法：DELETE FROM 表名称 WHERE 列名称 = 值；
        sql：select count(*) from setmeal where id in(1,2,3) and status = 1;
         */
        /*
         * 删除套餐
         */
        //查询是否有正在售卖的套餐
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId, ids)
                .eq(Setmeal::getStatus, 1);
        int count = this.count(queryWrapper);
        //有正在出售的套餐抛异常
        if(count > 0 ){
            throw new CustomException("当前套餐正在出售，无法删除");
        }
        this.removeByIds(ids);

        /*
         * 删除套餐关联信息
         */
        //sql：delete from setmeal_dish where setmeal_id in(1,2,3)
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(SetmealDish::getSetmealId,ids);
        setmealDishService.remove(lambdaQueryWrapper);
    }
}
