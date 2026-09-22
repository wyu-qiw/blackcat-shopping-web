package com.shop.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 管理员处理退款申请请求参数
 */
@Data
public class RefundHandleRequest {

    /** true 同意退款（订单变为已退款） / false 驳回退款（订单恢复已支付） */
    @NotNull(message = "请指定退款处理结果")
    private Boolean approved;
}
