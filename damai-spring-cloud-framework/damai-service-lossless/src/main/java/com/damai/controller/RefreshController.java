package com.damai.controller;


import com.damai.refresh.custom.NacosCustom;
import com.damai.refresh.custom.NacosAndRibbonCustom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/refresh")
public class RefreshController {

    @Autowired(required = false)
    private NacosAndRibbonCustom nacosAndRibbonCustom;
    
    @Autowired(required = false)
    private NacosCustom nacosCustom;
    
    
    /**
     * 更新并拉取服务列表
     * */
    @RequestMapping(value = "/refreshNacosAndRibbonCache", method = RequestMethod.POST)
    public Boolean refreshNacosAndRibbonCache() {
        if (nacosAndRibbonCustom != null) {
            return nacosAndRibbonCustom.refreshNacosAndRibbonCache();
        }
        return false;

    }

    /**
     * 获取ribbon和nacos缓存服务列表
     * */
    @RequestMapping(value = "/getNacosAndRibbonCacheList", method = RequestMethod.POST)
    public Map<String,?> getNacosAndRibbonCacheList() {
        if (nacosAndRibbonCustom != null) {
            return nacosAndRibbonCustom.getNacosAndRibbonCacheList();
        }
        return new HashMap<>(2);
    }

    /**
     * 从nacos主动下线
     * */
    @RequestMapping(value = "/logoutService", method = RequestMethod.POST)
    public Boolean logoutService(HttpServletRequest request){
        String localhostName = "localhost";
        String localhostAddress = "127.0.0.1";
        if (!(localhostName.equalsIgnoreCase(request.getServerName()) || localhostAddress.equalsIgnoreCase(request.getServerName()))) {
            return false;
        }
        if (nacosCustom != null) {
            return nacosCustom.logoutService();
        }
        return false;
    }
}
