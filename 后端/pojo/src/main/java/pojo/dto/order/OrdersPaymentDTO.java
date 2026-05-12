package pojo.dto.order;

import lombok.Data;
import java.io.Serializable;

/**
 * 订单支付DTO
 */
@Data
public class OrdersPaymentDTO implements Serializable {
    /**
     * 订单id
     */
    private Long id;

    /**
     * 付款方式
     */
    private Long payMethod;

}