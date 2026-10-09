package com.yitai.manager.service;

import com.yitai.model.dto.system.AssginRoleDto;
import com.yitai.model.dto.system.LoginDto;
import com.yitai.model.dto.system.SysUserDto;
import com.yitai.model.entity.system.SysUser;
import com.yitai.model.vo.system.LoginVo;
import com.yitai.model.vo.system.ValidateCodeVo;
import com.github.pagehelper.PageInfo;

public interface SysUserService {
    // 新增：生成验证码接口
    ValidateCodeVo generateValidateCode();

    //用户登录
    LoginVo login(LoginDto loginDto);

    //获取当前登录用户信息
    SysUser getUserInfo(String token);

    //用户退出
    void logout(String token);

    //1 用户条件分页查询接口
    PageInfo<SysUser> findByPage(Integer pageNum, Integer pageSize, SysUserDto sysUserDto);

    //2 用户添加
    void saveSysUser(SysUser sysUser);

    //3 用户修改
    void updateSysUser(SysUser sysUser);

    //4 用户删除
    void deleteById(Long userId);

    //用户分配角色
    void doAssign(AssginRoleDto assginRoleDto);
}
