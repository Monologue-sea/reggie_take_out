package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    //保存套餐分类信息
    void saveSetmeal(SetmealDto setmealDto);
    //删除套餐及其关联信息
    void removeWithSetmeal(List<Long> ids);
}
