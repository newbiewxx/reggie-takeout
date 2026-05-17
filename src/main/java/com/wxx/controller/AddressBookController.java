package com.wxx.controller;

import com.wxx.common.BaseContext;
import com.wxx.common.R;
import com.wxx.domain.AddressBook;
import com.wxx.service.AddressBookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("addressBook")
@RequiredArgsConstructor
public class AddressBookController {

    private final AddressBookService addressBookService;

    /**
     * 查询当前用户地址列表
     */
    @GetMapping("/list")
    public R<List<AddressBook>> list() {
        Long userId = BaseContext.getCurrentUserId();
        log.info("查询地址列表 - userId={}", userId);

        List<AddressBook> list = addressBookService.lambdaQuery()
                .eq(AddressBook::getUserId, userId)
                .orderByDesc(AddressBook::getUpdateTime)
                .list();

        return R.success(list);
    }

    /**
     * 新增地址
     */
    @PostMapping
    public R<String> save(@RequestBody AddressBook addressBook) {
        Long userId = BaseContext.getCurrentUserId();
        log.info("新增地址 - userId={}, consignee={}, phone={}", userId, addressBook.getConsignee(), addressBook.getPhone());

        addressBook.setUserId(userId);
        addressBookService.save(addressBook);
        return R.success("新增地址成功");
    }

    /**
     * 修改地址
     */
    @PutMapping
    public R<String> update(@RequestBody AddressBook addressBook) {
        log.info("修改地址 - ID={}", addressBook.getId());

        if (addressBook.getId() == null) {
            return R.error("修改失败，地址ID不能为空");
        }

        addressBookService.updateById(addressBook);
        return R.success("修改地址成功");
    }

    /**
     * 删除地址
     */
    @DeleteMapping
    public R<String> delete(Long ids) {
        log.info("删除地址 - ID={}", ids);

        if (ids == null) {
            return R.error("删除失败，地址ID不能为空");
        }

        addressBookService.removeById(ids);
        return R.success("删除成功");
    }

    /**
     * 查询默认地址
     */
    @GetMapping("/default")
    public R<AddressBook> getDefault() {
        Long userId = BaseContext.getCurrentUserId();
        log.info("查询默认地址 - userId={}", userId);

        AddressBook addressBook = addressBookService.lambdaQuery()
                .eq(AddressBook::getUserId, userId)
                .eq(AddressBook::getIsDefault, 1)
                .one();

        return R.success(addressBook);
    }

    /**
     * 设置默认地址
     */
    @PutMapping("/default")
    public R<String> setDefault(@RequestBody AddressBook addressBook) {
        Long userId = BaseContext.getCurrentUserId();
        log.info("设置默认地址 - userId={}, addressId={}", userId, addressBook.getId());

        if (addressBook.getId() == null) {
            return R.error("地址ID不能为空");
        }

        // 先将该用户所有地址取消默认
        addressBookService.lambdaUpdate()
                .eq(AddressBook::getUserId, userId)
                .eq(AddressBook::getIsDefault, 1)
                .set(AddressBook::getIsDefault, 0)
                .update();

        // 再将指定地址设为默认
        addressBook.setIsDefault(1);
        addressBookService.updateById(addressBook);

        return R.success("设置默认地址成功");
    }
}
