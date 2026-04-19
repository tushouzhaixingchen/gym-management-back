// src/main/java/com/gym/management/service/MemberService.java

package com.gym.management.service;

import com.gym.management.dto.request.admin.*;
import com.gym.management.dto.request.member.ChangePasswordRequest;
import com.gym.management.dto.request.member.MemberProfileUpdateRequest;
import com.gym.management.dto.response.*;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;

/**
 * 会员管理服务接口
 * 职责划分：
 * 1. 基础 CRUD (增删改查)
 * 2. 核心业务逻辑 (开卡/续费、充值、次卡调整、签到)
 */
public interface MemberService {

    // ================= 查询服务 =================

    /**
     * 获取会员列表（分页）
     * @param request 查询条件 (包含页码、大小、筛选条件)
     * @return Spring Data Page 对象，包含会员响应列表
     */
    Page<MemberResponse> queryMembers(MemberQueryRequest request);

    /**
     * 获取会员详情
     * @param id 会员ID
     * @return 会员详细信息
     */
    MemberResponse getMemberDetail(Integer id);

    /**
     * 根据 ID 获取会员实体对象
     * @param id 会员ID
     * @return 会员实体
     */
    com.gym.management.entity.Member getMemberById(Integer id);

    // ================= 基础维护 (CRUD) =================

    /**
     * 新增会员 (注册)
     * ⚠️ 此方法通常只创建基础档案，不包含复杂的开卡逻辑
     * 若需直接办卡，建议使用 openOrRenewCard 方法
     * @param request 创建请求
     * @return 创建后的会员信息
     */
    MemberResponse createMember(MemberCreateRequest request);

    /**
     * 编辑会员基础信息
     * 仅更新姓名、电话、备注等非核心业务字段
     * @param id 会员ID
     * @param request 更新请求 (部分字段)
     * @return 更新后的会员信息
     */
    MemberResponse updateMember(Integer id, MemberUpdateRequest request);

    /**
     * 删除会员 (逻辑删除)
     * 将状态标记为删除，或移入历史表，不物理删除数据
     * @param id 会员ID
     */
    void deleteMember(Integer id);

    // ================= 核心业务操作 =================

    /**
     * 开卡 / 续费 / 变更卡类型
     * 处理复杂的卡片逻辑：
     * - 期限卡：计算新的到期时间
     * - 次卡：增加总次数和剩余次数
     * - 贵宾卡：设置永久有效或特殊标识
     * @param id 会员ID
     * @param request 开卡/续费请求 (包含卡类型、时长/次数、金额等)
     * @return 更新后的会员信息
     */
    MemberResponse openOrRenewCard(Integer id, CardOperationRequest request);

    /**
     * 账户充值
     * 增加会员余额，并记录充值流水
     * @param id 会员ID
     * @param amount 充值金额
     * @param remark 备注
     * @return 更新后的会员信息
     */
    MemberResponse rechargeBalance(Integer id, BigDecimal amount, String remark);

    /**
     * 调整次卡次数
     * 用于赠送次数、扣除错误记录等特殊情况
     * @param id 会员ID
     * @param times 调整次数 (正数增加，负数减少)
     * @param reason 调整原因
     * @return 更新后的会员信息
     */
    MemberResponse adjustTimes(Integer id, Integer times, String reason);

    /**
     * 变更会员状态
     * 手动冻结、解冻或标记过期
     * @param id 会员ID
     * @param request 状态变更请求 (目标状态 + 原因)
     * @return 更新后的会员信息
     */
    MemberResponse updateMemberStatus(Integer id, MemberStatusRequest request);

    /**
     * 会员签到 (入场)
     * 核心逻辑：
     * 1. 校验会员状态是否允许入场
     * 2. 如果是次卡，扣除一次剩余次数
     * 3. 记录签到日志 (Visit Log)
     * 4. 更新最后签到时间
     * @param id 会员ID
     * @param request 签到请求 (可包含门店ID，用于跨店签到校验)
     * @return 签到结果及最新会员信息
     */
    MemberVisitResponse memberVisit(Integer id, MemberVisitRequest request);

    /**
     * 重置会员密码
     * 生成随机密码并强制下次修改
     * @param id 会员 ID
     * @return 包含临时密码的会员响应
     */
    MemberResponse resetMemberPassword(Integer id);

    // ================= 会员个人中心服务 =================

    /**
     * 获取会员个人信息
     * @param memberId 会员ID
     * @return 会员个人信息
     */
    MemberProfileVO getMemberProfile(Integer memberId);

    /**
     * 更新会员个人信息
     * 会员只能修改自己的基础信息（姓名、性别、邮箱、生日、备注）
     * @param memberId 会员ID
     * @param request 更新请求
     * @return 更新后的会员个人信息
     */
    MemberProfileVO updateMemberProfile(Integer memberId, MemberProfileUpdateRequest request);

    /**
     * 会员修改密码
     * 验证旧密码，更新为新密码，并将 is_initial_password 设置为 2
     * @param memberId 会员ID
     * @param request 修改密码请求（旧密码和新密码）
     */
    void changePassword(Integer memberId, ChangePasswordRequest request);
}
