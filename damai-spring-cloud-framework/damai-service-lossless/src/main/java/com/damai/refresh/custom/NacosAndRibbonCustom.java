package com.damai.refresh.custom;

import java.util.HashMap;
import java.util.Map;

public class NacosAndRibbonCustom {

    private NacosCustom nacosCustom;

    private RibbonCustom ribbonCustom;

    public NacosAndRibbonCustom(NacosCustom nacosCustom, RibbonCustom ribbonCustom){
        this.nacosCustom = nacosCustom;
        this.ribbonCustom = ribbonCustom;
    }

    public boolean refreshNacosAndRibbonCache(){
        nacosCustom.clearNacosCache();
        ribbonCustom.updateRibbonCache();
        return true;
    }

    public Map getNacosAndRibbonCacheList() {
        Map<String,?> nacosCache = nacosCustom.getNacosCache();
        Map<String,?> ribbonCache = ribbonCustom.getRibbonCache();
        Map<String,Map<String,?>> map = new HashMap(8);
        map.put("nacosCache",nacosCache);
        map.put("ribbonCache",ribbonCache);
        return map;
    }
}
