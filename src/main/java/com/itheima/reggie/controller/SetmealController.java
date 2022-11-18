package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        //log.info(setmealDto.toString());
        setmealService.saveSetmeal(setmealDto);
        return R.success("套餐添加成功！");
    }

    /**
     * 套餐管理分页信息
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        //套餐信息
        //分页构造器
        Page<Setmeal> pageInfo = new Page<>(page,pageSize);
        //条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name !=null,Setmeal::getName,name);
        setmealService.page(pageInfo,queryWrapper);

        //套餐分类信息
        //分页构造器
        Page<SetmealDto> pageSelectInfo = new Page<>();
        //将pageinfo里除setmeal外的值复制给pageSelectInfo
        BeanUtils.copyProperties(pageInfo,pageSelectInfo,"records");
        //得到所有菜品套餐
        List<Setmeal> setmeals = pageInfo.getRecords();
        //创建一个集合来接收setmealdto
        List<SetmealDto> list = new ArrayList<>();
        //遍历每一个套餐
        for (Setmeal setmeal : setmeals) {
            // 创建dto对象
            SetmealDto setmealDto = new SetmealDto();
            //将setmeal的信息复制给dto(设置dto的setmealDishes)
            BeanUtils.copyProperties(setmeal,setmealDto);
            //根据套餐的分类id得到分类信息
            Long categoryId = setmeal.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if(category != null){
                //设置dto的categoryName
                setmealDto.setCategoryName(category.getName());
            }
            //添加到list集合
            list.add(setmealDto);
        }
        //设置Page的records（相当于替换了用setmealDto集合替换了之前pageInfo里的setmeal）
        pageSelectInfo.setRecords(list);

        /**
         * steam流遍历
         */
//        List<SetmealDto> list = setmeals.stream().map((setmeal)->{
//            //创建dto对象
//            SetmealDto setmealDto = new SetmealDto();
//            //将setmeal的信息复制给dto
//            BeanUtils.copyProperties(setmeal,setmealDto);
//            //设置套餐分类id
//            Long categoryId = setmeal.getCategoryId();
//            Category category = categoryService.getById(categoryId);
//            if(category !=null){
//                //设置dto的套餐分类名
//                setmealDto.setCategoryName(category.getName());
//            }
//            return setmealDto;
//        }).collect(Collectors.toList());
//        pageSelectInfo.setRecords(list);

        return R.success(pageSelectInfo);
    }

    /*
      路径变量：http://localhost:8080/dish/1590628913590149121，需要使用"/{变量名}"来接收
      携带参数：http://localhost:8080/setmeal?ids=1415580119015145474，....1415580119015145475，集合需要在形参中使用@RequestParam来接收
     */

    /**
     * 删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        setmealService.removeWithSetmeal(ids);
        return R.success("套餐删除成功！");
    }

    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal){
        //条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() != null,Setmeal::getCategoryId,setmeal.getCategoryId())
                .eq(setmeal.getStatus() != null,Setmeal::getStatus,setmeal.getStatus())
                .orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> list = setmealService.list(queryWrapper);
        return R.success(list);
    }
}
