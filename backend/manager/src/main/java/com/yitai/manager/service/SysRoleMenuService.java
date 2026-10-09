package com.yitai.manager.service;

import com.yitai.model.dto.system.AssginMenuDto;

import java.util.Map;

public interface SysRoleMenuService {

    //1 查询所有菜单 和 查询角色分配过菜单id列表
    Map<String, Object> findSysRoleMenuByRoleId(Long roleId);

    //分配菜单
    void doAssign(AssginMenuDto assginMenuDto);
}
