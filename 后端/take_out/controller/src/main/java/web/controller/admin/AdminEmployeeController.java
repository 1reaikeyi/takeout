package web.controller.admin;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import common.constant.JwtClaimsConstant;
import common.constant.ErrorConstant;
import common.constant.PasswordConstant;
import common.constant.StatusConstant;
import common.localContextHolder.ThreadLocalContextHolder;
import common.properties.JwtProperties;
import common.result.Result;
import common.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import pojo.dto.employee.EmoployeePageDTO;
import pojo.dto.employee.EmployeeDTO;
import pojo.dto.employee.EmployeeLoginDTO;
import pojo.dto.employee.PasswordEditDTO;
import pojo.entity.Employee;
import service.ISevcive.EmployeeService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/employees")
@Transactional(rollbackFor = Exception.class)
public class AdminEmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    @PostMapping("/register")
    public Result register(@RequestBody EmployeeDTO employeeDTO) {
        Employee check_employee = employeeService.loadByEmployeeName(employeeDTO.getUserName());
        if (check_employee != null) {
            throw new RuntimeException(ErrorConstant.USERNAME_EXIST);
        }
        // 密码加密
        employeeDTO.setPassword(DigestUtils.md5DigestAsHex(employeeDTO.getPassword().getBytes()));
        Employee employee = Employee.builder()
                .userName(employeeDTO.getUserName()).name(employeeDTO.getName())
                .phone(employeeDTO.getPhone()).sex(employeeDTO.getSex()).idNumber(employeeDTO.getIdNumber())
                .password(employeeDTO.getPassword()).status(StatusConstant.ENABLE)
                .build();
        employeeService.save(employee);
        return Result.success("@PostMapping::注册成功::"+employee.getId());
    }


    @PostMapping("/login")
    public Result login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        Employee employee = employeeService.login(employeeLoginDTO);
        if (employee == null) {
            throw new RuntimeException(ErrorConstant.PASSWORD_ERROR);
        }
        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        Long empId = employee.getId();
        String empName = employee.getUserName();
        claims.put(JwtClaimsConstant.EMP_ID, empId);
        claims.put(JwtClaimsConstant.EMPNAME, empName);
        ThreadLocalContextHolder.set(claims);
        String token = JwtUtil.createJWT(jwtProperties.getAdminSecretKey(), jwtProperties.getAdminTtl(), claims);
        return Result.success("@PostMapping::token::"+token);
    }
    @PostMapping("/logout")
    public Result logout() {
        ThreadLocalContextHolder.remove();
        return Result.success("@PostMapping::退出成功");
    }

    @PostMapping
    public Result addEmployee(@RequestBody EmployeeDTO employeeDTO) {
        Employee check_employee = employeeService.loadByEmployeeName(employeeDTO.getUserName());
        if (check_employee != null) {
            throw new RuntimeException(ErrorConstant.USERNAME_EXIST);
        }
        if (employeeDTO.getPassword() == null) {
            String password = PasswordConstant.PASSWORD;
            employeeDTO.setPassword(DigestUtils.md5DigestAsHex(password.getBytes()));
        }
        if (employeeDTO.getPassword() != null) {
            employeeDTO.setPassword(DigestUtils.md5DigestAsHex(employeeDTO.getPassword().getBytes()));
        }
        Map<String, Object> claims = ThreadLocalContextHolder.get();
        String currentId = claims.get(JwtClaimsConstant.EMP_ID).toString();
        Long empId = Long.parseLong(currentId);
        Employee employee = Employee.builder()
                .userName(employeeDTO.getUserName()).name(employeeDTO.getName()).phone(employeeDTO.getPhone()).sex(employeeDTO.getSex()).idNumber(employeeDTO.getIdNumber())
                .password(employeeDTO.getPassword()).status(StatusConstant.ENABLE)
                .build();
        employeeService.save(employee);
        return Result.success("@PostMapping::添加成功::"+employee.getId());
    }
    @GetMapping("/{id}")
    public Result getEmployee(@PathVariable Long id) {
        Employee employee = employeeService.getById(id);
        return Result.success(employee);
    }
    @GetMapping
    public Result page(EmoployeePageDTO emoployeePageDTO) {
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getStatus, StatusConstant.ENABLE)
                .eq(Employee::getUserName, emoployeePageDTO.getUserName());
        IPage<Employee> employeeIPage = new Page<>(emoployeePageDTO.getPage(),
                emoployeePageDTO.getPageSize());
        IPage<Employee> page = employeeService.page(employeeIPage, queryWrapper);
        return Result.success(page);
    }

    @PutMapping("/{id}/status/{status}")
    public Result status(@PathVariable Long id, @PathVariable Long status) {
        Map<String, Object> claims = ThreadLocalContextHolder.get();
        String currentId = claims.get(JwtClaimsConstant.EMP_ID).toString();
        Long empId = Long.parseLong(currentId);
        Employee employee = Employee.builder()
                .id(id).status(status)
                .build();
        employeeService.updateById(employee);
        return Result.success("@PutMapping::"+id+"::状态为::"+(status == StatusConstant.ENABLE ? "启用" : "禁用"));
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody EmployeeDTO employeeDTO) {
        Employee employee = Employee.builder()
                .id(id).password(employeeDTO.getPassword())
                .userName(employeeDTO.getUserName()).name(employeeDTO.getName()).phone(employeeDTO.getPhone()).sex(employeeDTO.getSex()).idNumber(employeeDTO.getIdNumber())
                .build();
        employeeService.updateById(employee);
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getStatus, StatusConstant.ENABLE)
                        .eq(Employee::getUserName, employeeDTO.getUserName());
        List<Employee> check_employee = employeeService.list(queryWrapper);
        if (check_employee.size() > 1) {
            throw new RuntimeException(ErrorConstant.USERNAME_EXIST);
        }
        return Result.success("@PutMapping::更新成功::"+id);
    }

    @PutMapping("/password")
    public Result updateStatus(@RequestBody PasswordEditDTO passwordDTO) {
        Employee employee = employeeService.getById(passwordDTO.getEmpId());
        employee.setPassword(DigestUtils.md5DigestAsHex(passwordDTO.getConfirmPassword().getBytes()));
        employeeService.updateById(employee);
        return Result.success("@PutMapping::password::"+passwordDTO.getEmpId());
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        employeeService.removeById(id);
        return Result.success("@DeleteMapping::去除id::"+id);
    }
    @DeleteMapping
    public Result delete(@RequestParam List<Long> ids) {
        employeeService.removeByIds(ids);
        return Result.success("@DeleteMapping::删除ids::"+ids);
    }
}