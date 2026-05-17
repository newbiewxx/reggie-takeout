package com.wxx.controller;

import com.wxx.common.BaseContext;
import com.wxx.common.R;
import com.wxx.domain.ShoppingCart;
import com.wxx.service.ShoppingCartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("shoppingCart")
@RequiredArgsConstructor
public class ShoppingCartController {

    private final ShoppingCartService shoppingCartService;

    /**
     * 查看购物车
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list() {
        Long userId = BaseContext.getCurrentUserId();
        log.info("查看购物车 - userId={}", userId);

        List<ShoppingCart> list = shoppingCartService.lambdaQuery()
                .eq(ShoppingCart::getUserId, userId)
                .orderByDesc(ShoppingCart::getCreateTime)
                .list();

        return R.success(list);
    }

    /**
     * 添加购物车
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {
        Long userId = BaseContext.getCurrentUserId();
        log.info("添加购物车 - userId={}, dishId={}, setmealId={}",
                userId, shoppingCart.getDishId(), shoppingCart.getSetmealId());

        shoppingCart.setUserId(userId);

        // 查询当前菜品或套餐是否已在购物车
        ShoppingCart cartOne = shoppingCartService.lambdaQuery()
                .eq(ShoppingCart::getUserId, userId)
                .eq(shoppingCart.getDishId() != null, ShoppingCart::getDishId, shoppingCart.getDishId())
                .eq(shoppingCart.getSetmealId() != null, ShoppingCart::getSetmealId, shoppingCart.getSetmealId())
                .eq(shoppingCart.getDishFlavor() != null, ShoppingCart::getDishFlavor, shoppingCart.getDishFlavor())
                .one();

        if (cartOne != null) {
            // 已存在，数量加一
            cartOne.setNumber(cartOne.getNumber() + 1);
            shoppingCartService.updateById(cartOne);
        } else {
            // 不存在，新增
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            cartOne = shoppingCart;
        }

        return R.success(cartOne);
    }

    /**
     * 减少购物车商品数量
     */
    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart) {
        Long userId = BaseContext.getCurrentUserId();
        log.info("减少购物车商品 - userId={}, dishId={}, setmealId={}",
                userId, shoppingCart.getDishId(), shoppingCart.getSetmealId());

        // 查询购物车中的对应记录
        ShoppingCart cartOne = shoppingCartService.lambdaQuery()
                .eq(ShoppingCart::getUserId, userId)
                .eq(shoppingCart.getDishId() != null, ShoppingCart::getDishId, shoppingCart.getDishId())
                .eq(shoppingCart.getSetmealId() != null, ShoppingCart::getSetmealId, shoppingCart.getSetmealId())
                .one();

        if (cartOne == null) {
            return R.error("购物车中未找到该商品");
        }

        if (cartOne.getNumber() > 1) {
            // 数量减一
            cartOne.setNumber(cartOne.getNumber() - 1);
            shoppingCartService.updateById(cartOne);
        } else {
            // 数量为 1 时删除该记录
            shoppingCartService.removeById(cartOne.getId());
            cartOne.setNumber(0);
        }

        return R.success(cartOne);
    }

    /**
     * 清空购物车
     */
    @DeleteMapping("/clean")
    public R<String> clean() {
        Long userId = BaseContext.getCurrentUserId();
        log.info("清空购物车 - userId={}", userId);

        shoppingCartService.lambdaUpdate()
                .eq(ShoppingCart::getUserId, userId)
                .remove();

        return R.success("清空购物车成功");
    }
}
