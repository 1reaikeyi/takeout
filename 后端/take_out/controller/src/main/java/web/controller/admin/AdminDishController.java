package web.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import common.constant.JwtClaimsConstant;
import common.constant.StatusConstant;
import common.localContextHolder.ThreadLocalContextHolder;
import common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pojo.dto.dish.DishDTO;
import pojo.dto.dish.DishPageQueryDTO;
import pojo.entity.Dish;
import pojo.entity.DishFlavor;
import service.ISevcive.DishFlavorService;
import service.ISevcive.DishService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/admin/dishes")
@Transactional(rollbackFor = Exception.class)
public class AdminDishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String DISH_CACHE_KEY = "dish:id:";
    private static final int EXISTS_TIME = 50;
    private static final String NULL_PLACEHOLDER = "__NULL__";

    @PostMapping
    public Result add(@RequestBody DishDTO dishDTO) {
        // 在保存菜品前检查名称是否已存在
        LambdaQueryWrapper<Dish> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(Dish::getName, dishDTO.getName());
        if (dishService.exists(checkWrapper)) {
            return Result.error("菜品名称已存在");
        }
        if (dishDTO.getFlavors() == null || dishDTO.getFlavors().isEmpty()) {
            return Result.error("菜品口味不能为空");
        }
        Dish dish = Dish.builder()
                .name(dishDTO.getName()).categoryId(dishDTO.getCategoryId()).price(dishDTO.getPrice()).image(dishDTO.getImage()).description(dishDTO.getDescription())
                .status(dishDTO.getStatus())
                .build();
        dishService.save(dish);
        Long dishId = dish.getId();
        List<DishFlavor> flavorList = dishDTO.getFlavors().stream()
                .map((DishFlavor f) -> DishFlavor.builder()
                        .dishId(dishId)
                        .name(f.getName())
                        .value(f.getValue())
                        .build())
                .toList();
        dishFlavorService.saveBatch(flavorList);
        // 清空缓存
        redisTemplate.delete(DISH_CACHE_KEY + dishId);
        return Result.success("@PostMapping::添加成功::"+dishId);
    }

    @GetMapping
    public Result getAllDish(DishPageQueryDTO dishPageQueryDTO) {
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Dish::getName, dishPageQueryDTO.getName());
        IPage<Dish> iPage = new Page<>(dishPageQueryDTO.getPage(),
                dishPageQueryDTO.getPageSize());
        IPage<Dish> pageResult = dishService.page(iPage, queryWrapper);
        return Result.success(pageResult);
    }
    @GetMapping("/{id}")
    public Result getDishById(@PathVariable Long id) {
        // 1. 拼接完整的 Redis Key
        String key = DISH_CACHE_KEY + id;
        // 2. 先从 Redis 查询缓存（依赖全局序列化配置直接反序列化为 DishDTO）
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            if (NULL_PLACEHOLDER.equals(cached)) {
                // 缓存类型异常：删除后走数据库兜底并回写缓存
                redisTemplate.delete(key);
            }
            if (cached instanceof DishDTO dto) {
                return Result.success(dto);
            }
        }
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, id).select(DishFlavor::getName,DishFlavor::getValue);
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);
        Dish dish = dishService.getById(id);
        if (dish == null) {
            // 缓存空值，防止缓存穿透（Redis 不支持存 null，用占位符）
            redisTemplate.opsForValue().set(key, NULL_PLACEHOLDER, EXISTS_TIME, TimeUnit.MINUTES);
            return Result.error("菜品不存在");
        }
        DishDTO dishDTO = DishDTO.builder()
                .id(dish.getId()).name(dish.getName()).price(dish.getPrice()).image(dish.getImage()).description(dish.getDescription())
                .categoryId(dish.getCategoryId()).status(dish.getStatus()).flavors(flavors)
                .build();
        redisTemplate.opsForValue().set(key, dishDTO, EXISTS_TIME, TimeUnit.MINUTES);
        return Result.success(dishDTO);
    }
    @GetMapping("/category/{categoryId}")
    public Result getAllDishByCategoryId(@PathVariable Long categoryId) {
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dish::getCategoryId, categoryId).select(Dish::getName);
        return Result.success(dishService.list(queryWrapper));
    }
    @PutMapping("/{id}/status/{status}")
    public Result updateStatus(@PathVariable Long id, @PathVariable Long status) {
        Dish dish = Dish.builder().id(id).status(status).build();
        dishService.updateById(dish);
        // 清空缓存
        redisTemplate.delete(DISH_CACHE_KEY + id);
        return Result.success("@PutMapping::"+id+"::状态::"+(status == 1 ? "起售成功" : "停售成功"));
    }
    @PutMapping
    public Result updateDish(@RequestBody DishDTO dishDTO) {
        // 在保存菜品前检查名称是否已存在
        LambdaQueryWrapper<Dish> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(Dish::getId, dishDTO.getId());
        if (!dishService.exists(checkWrapper)) {
            return Result.error("更新失败，菜品不存在");
        }
        if (dishDTO.getFlavors() == null || dishDTO.getFlavors().isEmpty()) {
            return Result.error("菜品口味不能为空");
        }
        Dish dish = Dish.builder()
                .id(dishDTO.getId())
                .name(dishDTO.getName()).categoryId(dishDTO.getCategoryId()).price(dishDTO.getPrice())
                .image(dishDTO.getImage()).description(dishDTO.getDescription())
                .status(dishDTO.getStatus())
                .build();
        dishService.updateById(dish);
        Long dishId = dish.getId();
        LambdaQueryWrapper<DishFlavor> flavorRemove = new LambdaQueryWrapper<>();
        flavorRemove.eq(DishFlavor::getDishId, dishId);
        dishFlavorService.remove(flavorRemove);
        List<DishFlavor> flavorEntities = dishDTO.getFlavors().stream()
                .map(f -> DishFlavor.builder()
                        .dishId(dishId)
                        .name(f.getName())
                        .value(f.getValue())
                        .build())
                .toList();
        dishFlavorService.saveBatch(flavorEntities);
        // 删除菜品缓存
        redisTemplate.delete(DISH_CACHE_KEY + dish.getId());
        return Result.success("@PutMapping::"+dish.getId());
    }
    @DeleteMapping
    public Result deleteDish(@RequestParam List<Long> ids) {
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Dish::getId, ids).eq(Dish::getStatus, StatusConstant.ENABLE);
        List<Dish> dishList = dishService.list(queryWrapper);
        if(!dishList.isEmpty()){
            return Result.error("不能删除已启用的菜品");
        }
        // 删除菜品口味
        LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(DishFlavor::getDishId, ids);
        dishFlavorService.remove(wrapper);
        // 删除菜品缓存
        ids.forEach(id -> redisTemplate.delete(DISH_CACHE_KEY + id));
        return Result.success("@DeleteMapping::删除成功::"+ids);
    }
}