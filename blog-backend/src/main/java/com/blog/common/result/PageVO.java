package com.blog.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/** 契约规定的统一分页结构 {records,total,page,size}，不直接暴露 MyBatis-Plus 的 Page。 */
@Data
@AllArgsConstructor
public class PageVO<T> {

    private List<T> records;
    private long total;
    private long page;
    private long size;

    public static <E> PageVO<E> of(IPage<E> p) {
        return new PageVO<>(p.getRecords(), p.getTotal(), p.getCurrent(), p.getSize());
    }

    public static <E, V> PageVO<V> of(IPage<E> p, Function<E, V> mapper) {
        List<V> list = p.getRecords().stream().map(mapper).collect(Collectors.toList());
        return new PageVO<>(list, p.getTotal(), p.getCurrent(), p.getSize());
    }
}
