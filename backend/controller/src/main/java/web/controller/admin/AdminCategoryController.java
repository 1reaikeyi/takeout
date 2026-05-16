package web.controller.admin;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import common.constant.StatusConstant;
import common.localContextHolder.ThreadLocalContextHolder;
import common.result.Result;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pojo.dto.category.CategoryDTO;
import pojo.dto.category.CategoryPageQueryDTO;
import pojo.entity.Category;
import service.ISevcive.CategoryService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/categories")
@Transactional(rollbackFor = Exception.class)
public class AdminCategoryController {
    @Autowired
    private CategoryService categoryService;
    @PostMapping
    public Result add(@RequestBody CategoryDTO categoryDTO){
        //根据类型查询分类
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getName, categoryDTO.getName());
        Category check_category = categoryService.getOne(wrapper);
        if(check_category!=null){
            throw new RuntimeException("分类名称已存在");
        }
        Category newCategory = Category.builder()
                .name(categoryDTO.getName()).sort(categoryDTO.getSort()).type(categoryDTO.getType())
                .status(categoryDTO.getStatus())
                .build();
        categoryService.save(newCategory);
        return Result.success("@PostMapping::添加成功::"+newCategory.getId());
    }
    @GetMapping
    public Result page(CategoryPageQueryDTO categoryPageQueryDTO){
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(Category::getSort)
                .eq(Category::getType, categoryPageQueryDTO.getType())
                .eq(Category::getStatus, StatusConstant.ENABLE)
                .like(Category::getName, categoryPageQueryDTO.getName());
        IPage<Category> iPage = new Page<>(categoryPageQueryDTO.getPage(), categoryPageQueryDTO.getPageSize());
        IPage<Category> pageResult =  categoryService.page(iPage, queryWrapper);
        return Result.success(pageResult);
    }
    @GetMapping("/type")
    public Result type(@RequestParam Long type){
        //根据类型查询分类
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getType, type);
        queryWrapper.orderByAsc(Category::getSort);
        List<Category> list = categoryService.list(queryWrapper);
        return Result.success(list);
    }
    @PutMapping("/{id}/status/{status}")
    public Result update(@PathVariable("id") Long id, @PathVariable Long status){
        Category category = new Category();
        category.setId(id);
        category.setStatus(status);
        categoryService.updateById(category);
        return Result.success("@PutMapping::"+id+"::状态::"+(status == StatusConstant.ENABLE ? "启用成功" : "禁用成功"));
    }
    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody Category category){
        category.setId(id);
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getName, category.getName())
                .ne(Category::getId, id);
        Category check_category = categoryService.getOne(queryWrapper);
        if(check_category!=null){
            throw new RuntimeException("分类名称已存在");
        }
        categoryService.updateById(category);
        return Result.success("@PutMapping::"+id);
    }
    @DeleteMapping("/{id}")
    public Result<String> deleteById(@PathVariable Long id){
        categoryService.removeById(id);
        return Result.success("@DeleteMapping::删除成功::"+id);
    }
}