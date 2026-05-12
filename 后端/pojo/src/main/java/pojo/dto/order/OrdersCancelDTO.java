package pojo.dto.order;

import lombok.Data;

import java.io.Serializable;

/**
 * 订单取消DTO
 */
@Data
public class OrdersCancelDTO implements Serializable {

    /**
     * 订单id
     */
    private Long id;
    
    /**
     * 订单取消原因
     */
    private String cancelReason;

}