package web.controller.admin;

import common.constant.JwtClaimsConstant;
import common.localContextHolder.ThreadLocalContextHolder;
import common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;
import pojo.dto.order.OrdersDTO;
import pojo.entity.Orders;
import service.ISevcive.OrderService;

import java.util.Map;

@Repository
@RequestMapping("/admin/orders")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("get/{id}")
    public Result get(@PathVariable Long id) {
        return Result.success();
    }
    @GetMapping("/statistics")
    public Result statistics() {
        return Result.success();
    }
    @GetMapping("/conditionSearch")
    public Result conditionSearch() {
        return Result.success();
    }
    @PutMapping("/confirm")
    public Result confirm(OrdersDTO ordersDTO) {
        return Result.success();
    }@PutMapping("/cancel")
    public Result cancel(OrdersDTO ordersDTO) {
        return Result.success();
    }@PutMapping("/delivery")
    public Result delivery(OrdersDTO ordersDTO) {
        return Result.success();
    }@PutMapping("/complete")
    public Result complete(OrdersDTO ordersDTO) {
        return Result.success();
    }@PutMapping("/refund")
    public Result refund(OrdersDTO ordersDTO) {
        return Result.success();
    }
}
