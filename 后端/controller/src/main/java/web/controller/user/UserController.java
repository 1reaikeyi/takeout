package web.controller.user;

import common.constant.JwtClaimsConstant;
import common.localContextHolder.ThreadLocalContextHolder;
import common.properties.JwtProperties;
import common.result.Result;
import common.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pojo.dto.user.UserDTO;
import pojo.dto.user.UserLogInDTO;
import pojo.entity.User;
import service.ISevcive.UserService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user/users")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtProperties jwtProperties;
    @PostMapping("/register")
    public Result register(@RequestBody UserDTO userDTO) {
        // 检查用户名是否已存在
        User checkUser = userService.loadByUsername(userDTO.getUserName());
        System.out.println(userDTO);
        String password = DigestUtils.md5DigestAsHex(userDTO.getOpenId().getBytes());
        User user = User.builder()
                .userName(userDTO.getUserName()).phone(userDTO.getPhone()).sex(userDTO.getSex()).idNumber(userDTO.getIdNumber()).avatar(userDTO.getAvatar())
                .openId(password)
                .build();
        userService.save(user);
        return Result.success("@PostMapping::注册成功::"+user.getId());
    }
    @PostMapping("/login")
    public Result login(@RequestBody UserLogInDTO userLogInDTO) {
        System.out.println(userLogInDTO);
        User user = userService.login(userLogInDTO);
        System.out.println(user);
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        claims.put(JwtClaimsConstant.USERNAME, user.getUserName());
        ThreadLocalContextHolder.set(claims);
        String token = JwtUtil.createJWT(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(),
                claims);
        return Result.success(token);
    }
    @PostMapping("/logout")
    public Result logout() {
        return Result.success("@PostMapping::退出成功");
    }
}