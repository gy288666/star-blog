package com.blog.module.setting.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.module.setting.entity.BlogSetting;
import com.blog.module.setting.mapper.SettingMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** 键值设置：读走内存缓存（写入时失效），量小无并发问题。 */
@Service
@RequiredArgsConstructor
public class SettingService {

    /** 公开设置白名单：/api/settings/public 只返回这些键。 */
    private static final List<String> PUBLIC_KEYS = List.of(
            "siteTitle", "siteSubtitle", "siteLogo", "footerText", "icpText",
            "bannerTitle", "bannerSubtitle", "bannerImage", "bannerSource", "bannerBgColor",
            "bannerTypingEffect", "allowComment", "darkModeDefault",
            "pageBackgroundImage", "pageBackgroundImageDark", "pageBackgroundOpacity");

    private static final Map<String, String> DEFAULTS = Map.of(
            "siteTitle", "我的博客",
            "allowComment", "1",
            "darkModeDefault", "light",
            "bannerTypingEffect", "0");

    private final SettingMapper settingMapper;

    private volatile Map<String, String> cache;

    private Map<String, String> loadAll() {
        Map<String, String> local = cache;
        if (local == null) {
            local = settingMapper.selectList(null).stream()
                    .collect(Collectors.toMap(BlogSetting::getSettingKey,
                            s -> s.getSettingValue() == null ? "" : s.getSettingValue()));
            cache = local;
        }
        return local;
    }

    public String get(String key, String defaultValue) {
        String v = loadAll().get(key);
        return v == null || v.isBlank() ? defaultValue : v;
    }

    /** 公开设置（带默认值兜底）。 */
    public Map<String, String> publicMap() {
        Map<String, String> all = loadAll();
        Map<String, String> out = new HashMap<>();
        for (String key : PUBLIC_KEYS) {
            out.put(key, all.getOrDefault(key, DEFAULTS.getOrDefault(key, "")));
        }
        return out;
    }

    public Map<String, String> all() {
        return new HashMap<>(loadAll());
    }

    @Transactional
    public void save(Map<String, String> settings) {
        for (Map.Entry<String, String> e : settings.entrySet()) {
            BlogSetting exist = settingMapper.selectOne(new LambdaQueryWrapper<BlogSetting>()
                    .eq(BlogSetting::getSettingKey, e.getKey()));
            if (exist == null) {
                BlogSetting s = new BlogSetting();
                s.setSettingKey(e.getKey());
                s.setSettingValue(e.getValue());
                settingMapper.insert(s);
            } else {
                exist.setSettingValue(e.getValue());
                settingMapper.updateById(exist);
            }
        }
        cache = null;
    }
}
