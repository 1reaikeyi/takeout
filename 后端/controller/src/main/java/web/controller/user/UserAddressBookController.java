package web.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import common.constant.JwtClaimsConstant;
import common.localContextHolder.ThreadLocalContextHolder;
import common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pojo.entity.AddressBook;
import service.ISevcive.AddressBookService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user/AddressBooks")
public class UserAddressBookController {
    @Autowired
    private AddressBookService AddressBookService;
    @PostMapping
    public Result add(@RequestBody AddressBook AddressBook) {
       Map<String, Object> map = ThreadLocalContextHolder.get();
       String currentUserId = map.get(JwtClaimsConstant.USER_ID).toString();
       Long userId = Long.parseLong(currentUserId);
       AddressBook.setUserId(userId);
       if(AddressBook.getIsDefault() == null){
           AddressBook.setIsDefault(0L);
       }
       AddressBookService.save(AddressBook);
       if(AddressBook.getIsDefault() == 1){
           AddressBook.setIsDefault(1L);
       }
       return Result.success("@PostMapping::添加成功::"+AddressBook.getId());
    }
    @GetMapping
    public Result list() {
        Map<String, Object> map = ThreadLocalContextHolder.get();
        String currentUserId = map.get(JwtClaimsConstant.USER_ID).toString();
        Long userId = Long.parseLong(currentUserId);
        LambdaQueryWrapper<AddressBook> Wrapper = new LambdaQueryWrapper<AddressBook>();
        Wrapper.eq(AddressBook::getUserId, userId)
                .orderByDesc(AddressBook::getIsDefault)
                .orderByAsc(AddressBook::getId);
        List<AddressBook> AddressBookList = AddressBookService.list(Wrapper);
        if(AddressBookList.isEmpty()){
            return Result.error("没有添加地址，地址为空");
        }
        return Result.success(AddressBookList);
    }
    @GetMapping("/{id}")
    public Result get(@PathVariable("id") Long id) {
        AddressBook AddressBook = AddressBookService.getById(id);
        if(AddressBook == null){
            return Result.error("没有这个地址");
        }
        return Result.success(AddressBook);
    }
    @GetMapping("/default")
    public Result defult() {
        Map<String, Object> map = ThreadLocalContextHolder.get();
        String currentUserId = map.get(JwtClaimsConstant.USER_ID).toString();
        Long userId = Long.parseLong(currentUserId);
        LambdaQueryWrapper<AddressBook> Wrapper = new LambdaQueryWrapper<AddressBook>();
        Wrapper.eq(AddressBook::getIsDefault, 1)
                .eq(AddressBook::getUserId, userId);
        AddressBook AddressBook = AddressBookService.getOne(Wrapper);
        if(AddressBook == null){
            return Result.error("没有默认地址");
        }
        return Result.success(AddressBook);
    }
    @PutMapping
    public Result update(@RequestBody AddressBook AddressBook) {
        Map<String, Object> map = ThreadLocalContextHolder.get();
        String currentUserId = map.get(JwtClaimsConstant.USER_ID).toString();
        Long userId = Long.parseLong(currentUserId);
        AddressBook.setUserId(userId);
        AddressBookService.updateById(AddressBook);
        return Result.success("@PutMapping::更新成功::"+AddressBook.getId());
    }
    @PutMapping("/default/{defaultId}")
    public Result updateDefault(@PathVariable("defaultId") Long defaultId) {
        Map<String, Object> map = ThreadLocalContextHolder.get();
        String currentUserId = map.get(JwtClaimsConstant.USER_ID).toString();
        Long userId = Long.parseLong(currentUserId);
        LambdaQueryWrapper<AddressBook> Wrapper = new LambdaQueryWrapper<AddressBook>();
        Wrapper.eq(AddressBook::getIsDefault, 1)
                .eq(AddressBook::getUserId, userId)
                .eq(AddressBook::getId, defaultId);
        AddressBook AddressBook = AddressBookService.getOne(Wrapper);
        if(AddressBook == null){
            return Result.error("没有默认地址");
        }
        AddressBook.setIsDefault(1L);
        AddressBook.setId(defaultId);
        AddressBookService.updateById(AddressBook);
        return Result.success("@PutMapping::默认地址更新成功::"+defaultId);
    }
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable("id") Long id) {
        AddressBookService.removeById(id);
        return Result.success("@DeleteMapping::删除成功::"+id);
    }
}
