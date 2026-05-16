package web.controller.admin;

import web.annotation.Info;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import common.constant.ErrorConstant;
import common.constant.StatusConstant;
import common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pojo.dto.setmeal.SetmealDTO;
import pojo.dto.setmeal.SetmealPageQueryDTO;
import pojo.entity.Setmeal;
import pojo.entity.SetmealDish;
import service.ISevcive.SetmealDishService;
import service.ISevcive.SetmealService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/setmeals")
public class AdminSetmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;

    @Info(desc = "添加套餐")
    @PostMapping
    @CacheEvict(cacheNames = "setmeal", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public Result add(@RequestBody SetmealDTO setmealDTO) {
        LambdaQueryWrapper<Setmeal> setmealWrapper = new LambdaQueryWrapper<>();
        setmealWrapper.eq(Setmeal::getName, setmealDTO.getName());
        Setmeal check_setmeal = setmealService.getOne(setmealWrapper);
        if (check_setmeal != null) {
            return Result.error("套餐名称已存在");
        }
        List<SetmealDish> check_setmealDishes = setmealDTO.getSetmealDishes();
        if (check_setmealDishes == null || check_setmealDishes.isEmpty()) {
            return Result.error("套餐菜品不能为空");
        }
        Setmeal setmeal = Setmeal.builder()
                .categoryId(setmealDTO.getCategoryId()).name(setmealDTO.getName())
                .price(setmealDTO.getPrice()).description(setmealDTO.getDescription()).image(setmealDTO.getImage())
                .status(setmealDTO.getStatus())
                .build();
        setmealService.save(setmeal);

        List<SetmealDish> setmealDishesList = check_setmealDishes.stream()
                .map((SetmealDish dto) -> SetmealDish.builder().setmealId(setmeal.getId())
                        .dishId(dto.getDishId()).name(dto.getName()).price(dto.getPrice()).copies(dto.getCopies())
                        .build())
                .collect(Collectors.toList());
        setmealDishService.saveBatch(setmealDishesList);
        return Result.success("@PostMapping::"+setmeal.getId());
    }
    @Info(desc = "查询detail")
    @GetMapping("/{id}")
    @Cacheable(cacheNames = "setmeal", key = "#id")
    public Result get(@PathVariable Long id) {
        LambdaQueryWrapper<Setmeal> setmealWrapper = new LambdaQueryWrapper<>();
        setmealWrapper.eq(Setmeal::getId, id);
        Setmeal setmeal = setmealService.getOne(setmealWrapper);
        if (setmeal == null) {
            return Result.error("套餐"+ ErrorConstant.NULL_ERROR);
        }
        LambdaQueryWrapper<SetmealDish> setmealDishWrapper = new LambdaQueryWrapper<>();
        setmealDishWrapper.eq(SetmealDish::getSetmealId, id);
        setmealDishWrapper.orderByAsc(SetmealDish::getDishId);
        List<SetmealDish> setmealDishList = setmealDishService.list(setmealDishWrapper);
        return Result.success("@GetMapping::"+id+"::"+setmeal+"::"+setmealDishList);
    }
    @GetMapping
    public Result page(SetmealPageQueryDTO setmealPageQueryDTO) {
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Setmeal::getStatus, setmealPageQueryDTO.getStatus());
        queryWrapper.eq(Setmeal::getCategoryId, setmealPageQueryDTO.getCategoryId());
        queryWrapper.like(Setmeal::getName, setmealPageQueryDTO.getName());
        IPage<Setmeal> setmealIPage = new Page<>(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        IPage<Setmeal> pageResult =  setmealService.page(setmealIPage, queryWrapper);
        return Result.success(pageResult);
    }
    @PutMapping("/{id}/status/{status}")
    @CacheEvict(cacheNames = "setmeal", key = "#id")
    @Info(desc = "更改套餐状态")
    public Result updateStatus(@PathVariable Long id, @PathVariable Long status) {
        Setmeal setmeal = Setmeal.builder().id(id).status(status).build();
        setmealService.updateById(setmeal);
        return Result.success("@PutMapping::"+id+"::"+(status == StatusConstant.ENABLE?"启用成功":"停用成功"));
    }
    @PutMapping
    @CacheEvict(cacheNames = "setmeal", key = "#setmealDTO.id")
    public Result update(@RequestBody SetmealDTO setmealDTO) {
        Setmeal setmeal = Setmeal.builder()
                .id(setmealDTO.getId()).categoryId(setmealDTO.getCategoryId()).name(setmealDTO.getName())
                .price(setmealDTO.getPrice()).description(setmealDTO.getDescription()).image(setmealDTO.getImage())
                .status(setmealDTO.getStatus())
                .build();
        setmealService.updateById(setmeal);
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        List<SetmealDish> setmealDishList = setmealDishes.stream()
                .map(dto -> SetmealDish.builder()
                        .setmealId(setmealDTO.getId())
                        .dishId(dto.getDishId().longValue())
                        .name(dto.getName())
                        .price(dto.getPrice())
                        .copies(dto.getCopies().longValue())
                        .build())
                .collect(Collectors.toList());
        setmealDishService.saveBatch(setmealDishList);
        return Result.success("@PutMapping::"+setmealDTO.getId());
    }
    @DeleteMapping
    @CacheEvict(cacheNames = "setmeal", allEntries = true)
    public Result delete(@RequestParam List<Long> ids) {
        setmealService.removeByIds(ids);
        return Result.success("@DeleteMapping::"+ids);
    }
}