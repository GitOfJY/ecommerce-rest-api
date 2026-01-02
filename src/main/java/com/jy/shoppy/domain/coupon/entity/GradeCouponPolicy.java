package com.jy.shoppy.domain.coupon.entity;

import com.jy.shoppy.domain.coupon.entity.type.IssueType;
import com.jy.shoppy.domain.user.entity.UserGrade;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "grade_coupon_policies")
public class GradeCouponPolicy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "grade_id")
    private Long gradeId;

    @Column(name = "coupon_template_id")
    private Long couponTemplateId;

    @Enumerated(EnumType.STRING)
    @Column(name = "issue_type", length = 20)
    private IssueType issueType;

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_id", insertable = false, updatable = false)
    private UserGrade grade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_template_id", insertable = false, updatable = false)
    private Coupon couponTemplate;
}
