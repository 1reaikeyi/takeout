package web.metaHandler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import common.constant.AutoFillConstant;
import common.constant.JwtClaimsConstant;
import common.localContextHolder.ThreadLocalContextHolder;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class AutoMetaObjectHandler implements MetaObjectHandler {
    private Long getId() {
        Map<String, Object> claims = ThreadLocalContextHolder.get();
        if (claims == null) {
            return 0L;
        }
        String currentUserId = claims.get(JwtClaimsConstant.EMP_ID).toString();
        return Long.parseLong(currentUserId);
    }

    @Override
    public void insertFill(MetaObject metaObject) {
        this.setFieldValByName(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.now(), metaObject);
        this.setFieldValByName(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.now(), metaObject);

        Long id = getId();
        this.setFieldValByName(AutoFillConstant.SET_CREATE_USER, id, metaObject);
        this.setFieldValByName(AutoFillConstant.SET_UPDATE_USER, id, metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.setFieldValByName(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.now(), metaObject);

        Long id = getId();
        this.setFieldValByName(AutoFillConstant.SET_UPDATE_USER, id, metaObject);
    }
}