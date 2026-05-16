package pojo.entityenum;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Data;


public enum OrderStatus {
    /**
     * 订单状态：1待付款 2待接单 3已接单 4派送中 5已完成 6已取消 7退款
     */
    PENDING(1L, "待付款"),
    ACCEPTED(2L, "待接单"),
    DELIVERED(3L, "已接单"),
    DELIVERING(4L, "派送中"),
    COMPLETED(5L, "已完成"),
    CANCELLED(6L, "已取消"),
    REFUNDED(7L, "退款");
    @EnumValue
    private Long status;
    private String name;
    private OrderStatus(Long status, String name) {
        this.status = status;
        this.name = name;
    }

}
