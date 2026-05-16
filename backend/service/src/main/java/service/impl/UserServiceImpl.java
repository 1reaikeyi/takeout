package service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import common.constant.ErrorConstant;
import common.exception.AccountNotFoundException;
import common.exception.PasswordErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import pojo.dto.user.UserLogInDTO;
import pojo.entity.User;
import service.ISevcive.UserService;
import repository.mapper.UserMapper;

import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Override
    public User loadByUsername(String userName) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserName, userName);
        User user = userMapper.selectOne(queryWrapper);
        return user;
    }
    @Autowired
    private RedisTemplate<String, Object> securityCodeMap;
    public void addSecurityCode() {
        String secrctCode = "asdfghjkl";
        securityCodeMap.opsForValue().set("SECURITY_CODE", secrctCode, 5, TimeUnit.MINUTES);
    }

    @Override
    public User login(UserLogInDTO userLogInDTO) {
        String userName = userLogInDTO.getUserName();
        String password = userLogInDTO.getPassword();
        addSecurityCode();
        // TODO 后期需要进行md5加密，然后再进行比对
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        //1、根据用户名查询数据库中的数据
        User user = loadByUsername(userName);
        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (user == null) {
            //账号不存在
            throw new AccountNotFoundException(ErrorConstant.ACCOUNT_NOT_EXIST);
        }
        //密码比对
        if (!password.equals(user.getOpenId())) {
            //密码错误
            throw new PasswordErrorException(ErrorConstant.PASSWORD_ERROR);
        }
        //验证码比对
        String securityCode = (String) securityCodeMap.opsForValue().get("SECURITY_CODE");
        if (!securityCode.equals(userLogInDTO.getSecurityCode())) {
            //验证码错误
            throw new PasswordErrorException(ErrorConstant.SECURITY_CODE_ERROR);
        }
        //3、返回实体对象
        return user;
    }
}
