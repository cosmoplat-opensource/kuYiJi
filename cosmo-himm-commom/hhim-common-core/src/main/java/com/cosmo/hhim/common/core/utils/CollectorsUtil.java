/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.utils;

import com.cosmo.hhim.common.core.funInterface.ToBigDecimalFunction;
import org.apache.poi.ss.formula.functions.T;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 */
public class CollectorsUtil {
    static final Set<Collector.Characteristics> CH_NOID = Collections.emptySet();

    private CollectorsUtil() {
    }

    @SuppressWarnings("unchecked")
    private static <I, R> Function<I, R> castingIdentity() {
        return i -> (R) i;
    }

    static class CollectorImpl<T, A, R> implements Collector<T, A, R> {
        private final Supplier<A> supplier;
        private final BiConsumer<A, T> accumulator;
        private final BinaryOperator<A> combiner;
        private final Function<A, R> finisher;
        private final Set<Characteristics> characteristics;

        CollectorImpl(Supplier<A> supplier, BiConsumer<A, T> accumulator, BinaryOperator<A> combiner,
                      Function<A, R> finisher, Set<Characteristics> characteristics) {
            this.supplier = supplier;
            this.accumulator = accumulator;
            this.combiner = combiner;
            this.finisher = finisher;
            this.characteristics = characteristics;
        }

        CollectorImpl(Supplier<A> supplier, BiConsumer<A, T> accumulator, BinaryOperator<A> combiner,
                      Set<Characteristics> characteristics) {
            this(supplier, accumulator, combiner, castingIdentity(), characteristics);
        }

        @Override
        public BiConsumer<A, T> accumulator() {
            return accumulator;
        }

        @Override
        public Supplier<A> supplier() {
            return supplier;
        }

        @Override
        public BinaryOperator<A> combiner() {
            return combiner;
        }

        @Override
        public Function<A, R> finisher() {
            return finisher;
        }

        @Override
        public Set<Characteristics> characteristics() {
            return characteristics;
        }
    }

    //求和方法
    public static <T> Collector<T, ?, BigDecimal> summingBigDecimal(ToBigDecimalFunction<? super T> mapper) {
        return new CollectorImpl<>(
                () -> new BigDecimal[]{BigDecimal.ZERO},
                (a, t) -> { a[0] = a[0].add(mapper.applyAsBigDecimal(t)); },
                (a, b) -> { a[0] = a[0].add(b[0]) ; return a; },
                a -> a[0], CH_NOID);
    }

    //求最大值
    public static <T> Collector<T, ?, BigDecimal> maxBy(ToBigDecimalFunction<? super T> mapper) {
        return new CollectorImpl<>(
                () -> new BigDecimal[]{new BigDecimal(Long.MIN_VALUE)},
                (a, t) -> { a[0] = a[0].max(mapper.applyAsBigDecimal(t)); },
                (a, b) -> { a[0] = a[0].max(b[0]) ; return a; },
                a -> a[0], CH_NOID);
    }

    //求最小值
    public static <T> Collector<T, ?, BigDecimal> minBy(ToBigDecimalFunction<? super T> mapper) {
        return new CollectorImpl<>(
                () -> new BigDecimal[]{new BigDecimal(Long.MAX_VALUE)},
                (a, t) -> { a[0] = a[0].min(mapper.applyAsBigDecimal(t)); },
                (a, b) -> { a[0] = a[0].min(b[0]) ; return a; },
                a -> a[0], CH_NOID);
    }

    //求平均值
    public static <T> Collector<T, ?, BigDecimal> averagingBigDecimal(ToBigDecimalFunction<? super T> mapper, int newScale, int roundingMode) {
        return new CollectorImpl<>(
                () -> new BigDecimal[]{BigDecimal.ZERO,BigDecimal.ZERO},
                (a, t) -> { a[0] = a[0].add(mapper.applyAsBigDecimal(t)); a[1] = a[1].add(BigDecimal.ONE); },
                (a, b) -> { a[0] = a[0].add(b[0]) ; return a; },
                a -> a[0].divide(a[1],BigDecimal.ROUND_HALF_UP).setScale(newScale, roundingMode), CH_NOID);
    }

    //List分页
    public static <T> ListPageInfo<T> listPage(List<T> params, int size, int pageNum) {
        int totalPage = params.size() / size + (params.size() % size > 0 ? 1 : 0);//总页数
        Integer nextPage = pageNum + 1;//下一页
        pageNum = pageNum - 1;
        int fromIndex = pageNum * size;//开始下标
        int endIndex = (pageNum + 1) * size > params.size() ? params.size() : (pageNum + 1) * size;//结束下标
        if (nextPage > totalPage) {
            nextPage = null;
        }
        return new ListPageInfo(totalPage, nextPage, fromIndex, endIndex, params.subList(fromIndex, endIndex));
    }

    public static class ListPageInfo<T> {
        int totalPage;//总页数
        Integer nextPage;//下一页
        int fromIndex;//开始下标
        int endIndex;//结束下标
        List<T> rows;//参数

        public ListPageInfo(int totalPage, Integer nextPage, int fromIndex, int endIndex, List<T> rows) {
            this.totalPage = totalPage;
            this.nextPage = nextPage;
            this.fromIndex = fromIndex;
            this.endIndex = endIndex;
            this.rows = rows;
        }

        public int getTotalPage() {
            return totalPage;
        }

        public void setTotalPage(int totalPage) {
            this.totalPage = totalPage;
        }

        public Integer getNextPage() {
            return nextPage;
        }

        public void setNextPage(Integer nextPage) {
            this.nextPage = nextPage;
        }

        public int getFromIndex() {
            return fromIndex;
        }

        public void setFromIndex(int fromIndex) {
            this.fromIndex = fromIndex;
        }

        public int getEndIndex() {
            return endIndex;
        }

        public void setEndIndex(int endIndex) {
            this.endIndex = endIndex;
        }

        public List<T> getRows() {
            return rows;
        }

        public void setRows(List<T> rows) {
            this.rows = rows;
        }
    }

}
