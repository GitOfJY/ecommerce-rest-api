package com.jy.shoppy.domain.review.entity;

import com.jy.shoppy.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "review_comments")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private ReviewComment parentComment;

    @Builder.Default
    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewComment> childComments = new ArrayList<>();

    public static ReviewComment create(Review review, User user, String content, ReviewComment parentComment) {
        return ReviewComment.builder()
                .review(review)
                .user(user)
                .content(content)
                .parentComment(parentComment)
                .build();
    }

    public void update(String content) {
        this.content = content;
    }

    public int getDepth() {
        int depth = 0;
        ReviewComment parent = this.parentComment;
        while (parent != null) {
            depth++;
            parent = parent.getParentComment();
        }
        return depth;
    }
}
