package com.shop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shop.common.AuthContext;
import com.shop.common.BizException;
import com.shop.dto.PasswordChangeRequest;
import com.shop.dto.ProfileUpdateRequest;
import com.shop.entity.User;
import com.shop.entity.UserChangeLog;
import com.shop.mapper.UserMapper;
import com.shop.mapper.UserChangeLogMapper;
import com.shop.vo.ProfileLimitVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户服务：管理员查看全平台用户、个人信息修改（每月限额）
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    private final UserChangeLogMapper userChangeLogMapper;

    private final BCryptPasswordEncoder passwordEncoder;

    /** 每月可修改个人信息/密码的总次数 */
    public static final long MAX_MONTHLY_CHANGES = 2;

    public static final String CHANGE_TYPE_INFO = "INFO";

    public static final String CHANGE_TYPE_PASSWORD = "PASSWORD";

    public List<User> listAll() {
        return userMapper.selectList(
                new LambdaQueryWrapper<User>().orderByDesc(User::getCreateTime)
        );
    }

    /**
     * 当月已修改次数
     */
    public long countChangeThisMonth(Long userId) {
        LocalDateTime firstDayOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        Long count = userChangeLogMapper.selectCount(
                new LambdaQueryWrapper<UserChangeLog>()
                        .eq(UserChangeLog::getUserId, userId)
                        .ge(UserChangeLog::getCreateTime, firstDayOfMonth)
        );
        return count == null ? 0 : count;
    }

    /**
     * 当前用户当月修改额度
     */
    public ProfileLimitVO getProfileLimit() {
        long used = countChangeThisMonth(AuthContext.getUserId());
        return new ProfileLimitVO(used, MAX_MONTHLY_CHANGES, Math.max(0, MAX_MONTHLY_CHANGES - used));
    }

    /**
     * 修改个人信息（每月限额）
     */
    public User updateProfile(ProfileUpdateRequest request) {
        Long userId = AuthContext.getUserId();
        ensureCanChange(userId);

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        user.setNickname(request.getNickname());
        user.setAvatar(StringUtils.hasText(request.getAvatar()) ? request.getAvatar() : null);
        userMapper.updateById(user);

        logChange(userId, CHANGE_TYPE_INFO);
        return user;
    }

    /**
     * 修改账号密码（每月限额，需校验原密码）
     */
    public void changePassword(PasswordChangeRequest request) {
        Long userId = AuthContext.getUserId();
        ensureCanChange(userId);

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BizException("原密码不正确");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);

        logChange(userId, CHANGE_TYPE_PASSWORD);
    }

    /**
     * 校验当月修改次数是否已达上限
     */
    private void ensureCanChange(Long userId) {
        if (countChangeThisMonth(userId) >= MAX_MONTHLY_CHANGES) {
            throw new BizException("本月修改次数已达上限（2次），请下月再试");
        }
    }

    private void logChange(Long userId, String changeType) {
        UserChangeLog log = new UserChangeLog();
        log.setUserId(userId);
        log.setChangeType(changeType);
        log.setCreateTime(LocalDateTime.now());
        userChangeLogMapper.insert(log);
    }
}
