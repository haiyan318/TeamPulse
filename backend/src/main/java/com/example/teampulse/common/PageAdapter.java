package com.example.teampulse.common;

import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

/**
 * MyBatis-Plus IPage → 项目 PageResult 适配器。
 *
 * <p>PageResult 字段冻结为 pageNum / pageSize / total / list，
 * 与 MP 的 current / size / records / pages 口径不同，必须通过此工具转换。</p>
 */
public final class PageAdapter {

    private PageAdapter() {
    }

    /**
     * 直接转换：IPage&lt;T&gt; → PageResult&lt;T&gt;。
     */
    public static <T> PageResult<T> toPageResult(IPage<T> page) {
        return PageResult.of(
                (int) page.getCurrent(),
                (int) page.getSize(),
                page.getTotal(),
                page.getRecords()
        );
    }

    /**
     * 带记录映射的转换：IPage&lt;?&gt; + 自定义 list → PageResult&lt;T&gt;。
     *
     * <p>典型场景：Service 层查出 PO 分页后，转换为 VO 列表再包装。</p>
     */
    public static <T> PageResult<T> toPageResult(IPage<?> page, List<T> list) {
        return PageResult.of(
                (int) page.getCurrent(),
                (int) page.getSize(),
                page.getTotal(),
                list
        );
    }
}
