package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
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
}
