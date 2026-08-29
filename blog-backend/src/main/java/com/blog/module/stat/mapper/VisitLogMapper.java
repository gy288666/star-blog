package com.blog.module.stat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.module.stat.entity.BlogVisitLog;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface VisitLogMapper extends BaseMapper<BlogVisitLog> {

    /** 按天统计访问量。 */
    @Select("SELECT DATE(visit_time) AS vdate, COUNT(*) AS cnt FROM blog_visit_log " +
            "WHERE visit_time >= #{start} GROUP BY DATE(visit_time)")
    List<Map<String, Object>> selectDailyTrend(@Param("start") LocalDateTime start);
}
