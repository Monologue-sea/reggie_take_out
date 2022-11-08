package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/category")
public class DishFlavorController {
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/list")
    public R<List<Category>> list(Category category){
        //条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //条件：类型不为空，传入类型和Category表一致，按照sort排序，否则按照创建时间排序
        queryWrapper.eq(category.getType()!=null,Category::getType,category.getType())
                .orderByDesc(Category::getSort).orderByDesc(Category::getCreateTime);
        List<Category> list = categoryService.list(queryWrapper);
        return R.success(list);
    }
}
