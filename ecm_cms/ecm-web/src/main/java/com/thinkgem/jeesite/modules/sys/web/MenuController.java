/**
 * Copyright &copy; 2012-2013 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.thinkgem.jeesite.modules.sys.web;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.camelot.openplatform.util.SysProperties;
import com.thinkgem.jeesite.common.utils.StringUtils;
import com.thinkgem.jeesite.common.web.BaseController;
import com.thinkgem.jeesite.modules.sys.entity.Menu;
import com.thinkgem.jeesite.modules.sys.service.SystemService;

/**
 * 菜单Controller
 * @author ThinkGem
 * @version 2013-3-23
 */
@Controller
@RequestMapping(value = "${adminPath}/sys/menu")
public class MenuController extends BaseController {

	@Autowired
	private SystemService systemService;
	
	@ModelAttribute("menu")
	public Menu get(@RequestParam(required=false) String id) {
		if (StringUtils.isNotBlank(id)){
			return systemService.getMenu(id);
		}else{
			return new Menu();
		}
	}

	@RequiresPermissions("sys:menu:view")
	@RequestMapping(value = {"list", ""})
	public String list(Model model) {
		List<Menu> list = Lists.newArrayList();
		List<Menu> sourcelist = systemService.findAllMenu();
		Menu.sortList(list, sourcelist, "1");
        model.addAttribute("list", list);
		return "modules/sys/menuList";
	}

	@RequiresPermissions("sys:menu:view")
	@RequestMapping(value = "form")
	public String form(Menu menu, Model model) {
		if (menu.getParent()==null||menu.getParent().getId()==null){
			menu.setParent(new Menu("1"));
		}
		menu.setParent(systemService.getMenu(menu.getParent().getId()));
		model.addAttribute("menu", menu);
		return "modules/sys/menuForm";
	}
	
	@RequiresPermissions("sys:menu:edit")
	@RequestMapping(value = "save")
	public String save(Menu menu, Model model, RedirectAttributes redirectAttributes) {
		if(SysProperties.isDemoMode()){
			addMessage(redirectAttributes, "演示模式，不允许操作！");
			return "redirect:"+SysProperties.getAdminPath()+"/sys/menu/";
		}
		if (!beanValidator(model, menu)){
			return form(menu, model);
		}
		systemService.saveMenu(menu);
		addMessage(redirectAttributes, "保存菜单'" + menu.getName() + "'成功");
		return "redirect:"+SysProperties.getAdminPath()+"/sys/menu/";
	}
	
	@RequiresPermissions("sys:menu:edit")
	@RequestMapping(value = "delete")
	public String delete(String id, RedirectAttributes redirectAttributes) {
		if(SysProperties.isDemoMode()){
			addMessage(redirectAttributes, "演示模式，不允许操作！");
			return "redirect:"+SysProperties.getAdminPath()+"/sys/menu/";
		}
		if (Menu.isRoot(id)){
			addMessage(redirectAttributes, "删除菜单失败, 不允许删除顶级菜单或编号为空");
		}else{
			systemService.deleteMenu(id);
			addMessage(redirectAttributes, "删除菜单成功");
		}
		return "redirect:"+SysProperties.getAdminPath()+"/sys/menu/";
	}

	@RequiresUser
	@RequestMapping(value = "tree")
	public String tree() {
		return "modules/sys/menuTree";
	}
	
	/**
	 * 同步工作流权限数据
	 */
	@RequiresPermissions("sys:menu:edit")
	@RequestMapping(value = "synToActiviti")
	public String synToActiviti(RedirectAttributes redirectAttributes) {
		if(SysProperties.isDemoMode()){
			addMessage(redirectAttributes, "演示模式，不允许操作！");
			return "redirect:"+SysProperties.getAdminPath()+"/sys/menu/";
		}
		systemService.synToActiviti();
    	addMessage(redirectAttributes, "同步工作流权限数据成功!");
		return "redirect:"+SysProperties.getAdminPath()+"/sys/menu/";
	}
	
	
	/**
	 * 批量修改菜单排序
	 */
	@RequiresPermissions("sys:menu:edit")
	@RequestMapping(value = "updateSort")
	public String updateSort(String[] ids, Integer[] sorts, RedirectAttributes redirectAttributes) {
		if(SysProperties.isDemoMode()){
			addMessage(redirectAttributes, "演示模式，不允许操作！");
			return "redirect:"+SysProperties.getAdminPath()+"/sys/menu/";
		}
    	int len = ids.length;
    	Menu[] menus = new Menu[len];
    	for (int i = 0; i < len; i++) {
    		menus[i] = systemService.getMenu(ids[i]);
    		menus[i].setSort(sorts[i]);
    		systemService.saveMenu(menus[i]);
    	}
    	addMessage(redirectAttributes, "保存菜单排序成功!");
		return "redirect:"+SysProperties.getAdminPath()+"/sys/menu/";
	}
	
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "treeData")
	public List<Map<String, Object>> treeData(@RequestParam(required=false) String extId, HttpServletResponse response) {
		response.setContentType("application/json; charset=UTF-8");
		List<Map<String, Object>> mapList = Lists.newArrayList();
		List<Menu> list = systemService.findAllMenu();
		for (int i=0; i<list.size(); i++){
			Menu e = list.get(i);
			if (extId == null || (extId!=null && !extId.equals(e.getId()) && e.getParentIds().indexOf(","+extId+",")==-1)){
				Map<String, Object> map = Maps.newHashMap();
				map.put("id", e.getId());
				map.put("pId", e.getParent()!=null?e.getParent().getId():0);
				map.put("name", e.getName());
				mapList.add(map);
			}
		}
		return mapList;
	}
    @RequiresUser
    @ResponseBody
    @RequestMapping(value = "menuData")
    public List<Map<String, Object>> menuData(@RequestParam(required=false) String parentId, HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        List<Map<String, Object>> mapList = Lists.newArrayList();
        List<Menu> list = systemService.findAllMenu();
        for (Menu menu:list){
            if(null==parentId||"".equals(parentId)){
                Map<String, Object> map = Maps.newHashMap();
                map.put("id", menu.getId());
                map.put("pId", menu.getParent()==null?"":menu.getParent().getId());
                map.put("name", menu.getName());
//                map.put("parent",menu.getParent());
                map.put("isShow",menu.getIsShow());
                map.put("href",menu.getHref());
                map.put("icon",menu.getIcon());
//                map.put("children",menu.getChildList());
                mapList.add(map);
            }else{
                if(menu.getParentIds().indexOf(","+parentId+",")!=-1){
                    Map<String, Object> map = Maps.newHashMap();
                    map.put("id", menu.getId());
                    map.put("pId", menu.getParent()==null?"":menu.getParent().getId());
                    map.put("name", menu.getName());
//                    map.put("parent",menu.getParent());
                    map.put("isShow",menu.getIsShow());
                    map.put("href",menu.getHref());
//                    map.put("children",menu.getChildList());
                    map.put("icon",menu.getIcon());
                    mapList.add(map);
                }
            }


        }
        return mapList;
    }
}
