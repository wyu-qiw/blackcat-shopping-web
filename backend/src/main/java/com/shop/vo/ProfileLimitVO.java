package com.shop.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 个人中心每月修改次数额度
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileLimitVO {

    /** 当月已用次数 */
    private Long used;

    /** 每月可修改总次数 */
    private Long total;

    /** 当月剩余次数 */
    private Long remaining;
}
