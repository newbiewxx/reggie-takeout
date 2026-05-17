package com.wxx.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxx.domain.User;
import com.wxx.mapper.UserMapper;
import com.wxx.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
