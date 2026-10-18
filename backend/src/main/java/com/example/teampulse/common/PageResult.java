package com.example.teampulse.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页结果载体。
 *
 * <p>字段冻结为 pageNum / pageSize / total / list 四个，
 * 禁止新增 current、size、records、pages 等字段，也禁止改名。
 * 入参约定：pageNum 默认 1、pageSize 默认 10、上限 100（由分页查询 DTO 校验）。</p>
 *
 * <p>典型用法：{@code Result<PageResult<UserVO>>}。</p>
 *
 * @param <T> 列表元素类型
 */
@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
public class PageResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 当前页码，从 1 开始。 */
    private int pageNum;

    /** 每页条数，取值 1-100。 */
    private int pageSize;

    /** 总记录数。 */
    private long total;

    /** 当前页数据，永不为 null。 */
    private List<T> list = Collections.emptyList();

    /**
     * 构造分页结果，参数顺序与字段声明顺序一致。
     *
     * @param pageNum  当前页码
     * @param pageSize 每页条数
     * @param total    总记录数
     * @param list     当前页数据，null 归一为空列表
     */
    public static <T> PageResult<T> of(int pageNum, int pageSize, long total, List<T> list) {
        PageResult<T> pageResult = new PageResult<>();
        pageResult.setPageNum(pageNum);
        pageResult.setPageSize(pageSize);
        pageResult.setTotal(total);
        pageResult.setList(list == null ? Collections.<T>emptyList() : list);
        return pageResult;
    }
}
