package web.controller.timetask;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pojo.entity.Orders;
import service.ISevcive.OrderService;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderService orderService;
    /**
     * 帮助处理订单
     */
//    @Scheduled 使用 6 字段 cron 表达式（秒 分 时 日 月 周）
    //每10分钟触发一次
    @Scheduled(cron = "0 */10 * * * ?")
    public void processTimeoutOrder(){
        LambdaQueryWrapper<Orders> wrapper1 = new LambdaQueryWrapper<>();
        wrapper1.eq(Orders::getStatus, 2);
        List<Orders> ordersList = orderService.list(wrapper1);
        if (!ordersList.isEmpty()) {
            log.info("有待处理订单：{}", ordersList);
        }
        LambdaQueryWrapper<Orders> wrapper2 = new LambdaQueryWrapper<>();
        wrapper2.eq(Orders::getDeliveryStatus,0);
        List<Orders> ordersList2 = orderService.list(wrapper2);
        if (!ordersList2.isEmpty()) {
            for (Orders orders : ordersList2) {
                LocalDateTime now = LocalDateTime.now();
                if (orders.getStartDeliveryTime() != null && now.isAfter(orders.getStartDeliveryTime())) {
                    log.info("id为{}订单需要开始派送了", orders.getId());
                }
            }
        }
    }

}
