package com.wxx.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxx.domain.Employee;
import com.wxx.mapper.EmployeeMapper;
import com.wxx.service.EmployeeService;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {
}
