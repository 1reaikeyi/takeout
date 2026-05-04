package web.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import common.constant.JwtClaimsConstant;
import common.localContextHolder.ThreadLocalContextHolder;
import common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pojo.dto.order.OrdersCancelDTO;
import pojo.dto.order.OrdersDTO;
import pojo.dto.order.OrdersPageQueryDTO;
import pojo.entity.AddressBook;
import pojo.entity.OrderDetail;
import pojo.entity.Orders;
import pojo.entityenum.DeliveryStatus;
import pojo.entityenum.OrderStatus;
import pojo.entityenum.PayStatusEnum;
import service.ISevcive.AddressBookService;
import service.ISevcive.OrderDetailService;
import service.ISevcive.OrderService;
import web.controller.websocket.WebSocketServer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user/orders")
public class UserOrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private AddressBookService addressBookService;
    @Autowired
    private WebSocketServer webServer;

    @PostMapping("/submit")
    public Result submit(@RequestBody OrdersDTO orderDTO) {

        Map<String, Object> claims = ThreadLocalContextHolder.get();
        String currentId = claims.get(JwtClaimsConstant.USER_ID).toString();
        String currentName = claims.get(JwtClaimsConstant.USERNAME).toString();
        Long userId = Long.parseLong(currentId);
        LambdaQueryWrapper<AddressBook> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(AddressBook::getUserId, userId)
                .eq(AddressBook::getIsDefault, 1);
        AddressBook addressBook = addressBookService.getOne(lambdaQueryWrapper);
        if (addressBook == null) {
            return Result.error("请选择地址");
        }
        Orders orders = Orders.builder()
                .userId(userId).userName(currentName)
                .phone(addressBook.getPhone()).addressBookId(addressBook.getId()).address(addressBook.getDetail()).consignee(addressBook.getConsignee())
                .orderTime(LocalDateTime.now()).payMethod(orderDTO.getPayMethod()).amount(orderDTO.getAmount()).remark(orderDTO.getRemark())
                .status(OrderStatus.PENDING).payStatus(PayStatusEnum.UNPAID).startDeliveryTime(LocalDateTime.now())
                .build();
        orderService.save(orders);
        List<OrderDetail> orderDetailList = orderDTO.getOrderDetails().stream()
                    .map((OrderDetail orderDetail) -> {return orderDetail.builder()
                            .orderId(orders.getId()).name(orderDetail.getName())
                            .dishId(orderDetail.getDishId()).setmealId(orderDetail.getSetmealId()).dishFlavor(orderDetail.getDishFlavor())
                            .number(orderDetail.getNumber()).amount(orderDetail.getAmount()).image(orderDetail.getImage())
                            .packAmount(orderDetail.getPackAmount()).tablewareNumber(orderDetail.getTablewareNumber())
                            .build();})
                    .toList();
        orderDetailService.saveBatch(orderDetailList);
        return Result.success("@PostMapping::" + orders.getId());
    }
    @PostMapping("/repetition/{id}")
    public Result repetition(@PathVariable Long id) {
        Orders oldOrders = orderService.getById(id);
        if (oldOrders == null) {
            return Result.error("订单不存在");
        }
        Orders newOrders = Orders.builder()
                .userId(oldOrders.getUserId()).userName(oldOrders.getUserName())
                .phone(oldOrders.getPhone()).addressBookId(oldOrders.getAddressBookId()).address(oldOrders.getAddress()).consignee(oldOrders.getConsignee())
                .orderTime(LocalDateTime.now()).payMethod(oldOrders.getPayMethod()).amount(oldOrders.getAmount()).remark(oldOrders.getRemark())
                .status(OrderStatus.PENDING).payStatus(PayStatusEnum.UNPAID).startDeliveryTime(LocalDateTime.now())
                .build();
        orderService.save(newOrders);
        List<OrderDetail> orderDetailList = orderDetailService.list(new LambdaQueryWrapper<OrderDetail>()
                .eq(OrderDetail::getOrderId, oldOrders.getId()));
        orderDetailService.saveBatch(orderDetailList);
        return Result.success("@PostMapping::" + newOrders.getId());
    }
    @GetMapping("/get/{id}")
    public Result get(@PathVariable Long id) {
        Orders orders = orderService.getById(id);
        if (orders == null) {
            return Result.error("订单不存在");
        }
        return Result.success(orders);
    }
    @GetMapping("/history")
    public Result history(OrdersPageQueryDTO ordersPageQueryDTO) {
        Map<String, Object> claims = ThreadLocalContextHolder.get();
        String currentId = claims.get(JwtClaimsConstant.USER_ID).toString();
        Long userId = Long.parseLong(currentId);
        LambdaQueryWrapper<Orders> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Orders::getUserId, userId)
                .between(Orders::getOrderTime, ordersPageQueryDTO.getBeginTime(), ordersPageQueryDTO.getEndTime())
                .orderByDesc(Orders::getId);
        IPage<Orders> iPage = new Page<>(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        IPage<Orders> ordersPageResult = orderService.page(iPage, lambdaQueryWrapper);
        if (ordersPageResult.getRecords() == null) {
            return Result.error("暂无订单");
        }
        return Result.success(ordersPageResult.getRecords());
    }
    @PostMapping("/reminder/{id}")
    public String reminder(@PathVariable Long id) {
        return "订单"+id+"需要加急处理";
    }

    @PutMapping("/cancel")
    public Result cancel(@RequestBody OrdersCancelDTO ordersCancelDTO) {
        LambdaQueryWrapper<Orders> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Orders::getId, ordersCancelDTO.getId())
                .ne(Orders::getStatus, OrderStatus.CANCELLED);
        Orders orders = orderService.getOne(lambdaQueryWrapper);
        if (orders == null) {
            return Result.error("订单不存在");
        }
        orders.setStatus(OrderStatus.CANCELLED);
        orders.setCancelTime(LocalDateTime.now());
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orderService.updateById(orders);

        return Result.success();
    }
}
