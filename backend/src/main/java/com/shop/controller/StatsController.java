package com.shop.controller;

import com.shop.common.Result;
import com.shop.service.StatsService;
import com.shop.vo.ChartItemVO;
import com.shop.vo.SalesTrendVO;
import com.shop.vo.StatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理员数据大盘接口
 */
@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping
    public Result<StatsVO> stats() {
        return Result.success(statsService.getStats());
    }

    /** 条形图：商品销量 TopN（默认 8） */
    @GetMapping("/product-sales")
    public Result<List<ChartItemVO>> productSales(@RequestParam(defaultValue = "8") int limit) {
        return Result.success(statsService.getProductSalesTopN(limit));
    }

    /** 折线图：近 N 天销售趋势（默认 7，最大 90） */
    @GetMapping("/sales-trend")
    public Result<List<SalesTrendVO>> salesTrend(@RequestParam(defaultValue = "7") int days) {
        return Result.success(statsService.getDailySalesTrend(days));
    }

    /** 饼图：各品类销售占比 */
    @GetMapping("/category-distribution")
    public Result<List<ChartItemVO>> categoryDistribution() {
        return Result.success(statsService.getCategoryDistribution());
    }
}