package com.yitai.manager.utils;

import com.yitai.model.entity.system.SysMenu;

import java.util.*;

//封装树形菜单数据
public class MenuHelper {
    // 优化版：构建树形菜单（时间复杂度 O(n)）
    public static List<SysMenu> buildTree(List<SysMenu> sysMenuList) {
        // 新增：入参判空，避免NPE
        if (sysMenuList == null || sysMenuList.isEmpty()) {
            return Collections.emptyList();
        }
        // 1. 构建 parentId -> 子菜单列表的哈希映射（核心优化点）
        Map<Long, List<SysMenu>> parentIdToChildrenMap = new HashMap<>();
        for (SysMenu menu : sysMenuList) {
            // 按 parentId 分组，key=parentId，value=该父菜单下的所有子菜单
            parentIdToChildrenMap.computeIfAbsent(menu.getParentId(), k -> new ArrayList<>()).add(menu);
        }

        // 2. 筛选根菜单（parentId=0），递归构建子菜单
        List<SysMenu> trees = new ArrayList<>();
        // 直接从映射表获取根菜单，无需遍历所有菜单
        List<SysMenu> rootMenus = parentIdToChildrenMap.getOrDefault(0L, Collections.emptyList());
        for (SysMenu rootMenu : rootMenus) {
            findChildrenOptimize(rootMenu, parentIdToChildrenMap);
            trees.add(rootMenu);
        }
        return trees;
    }

    // 优化版：递归查找子菜单（通过哈希表直接获取，无需遍历）
    private static void findChildrenOptimize(SysMenu parentMenu, Map<Long, List<SysMenu>> parentIdToChildrenMap) {
        // 初始化 children 集合（避免空指针）
        parentMenu.setChildren(new ArrayList<>());

        // 从哈希表直接获取当前菜单的所有子菜单（O(1) 查找）
        List<SysMenu> children = parentIdToChildrenMap.getOrDefault(parentMenu.getId(), Collections.emptyList());
        for (SysMenu child : children) {
            // 递归处理子菜单的下级菜单
            findChildrenOptimize(child, parentIdToChildrenMap);
            parentMenu.getChildren().add(child);
        }
    }
}
