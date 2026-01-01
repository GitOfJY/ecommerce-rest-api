package com.jy.shoppy.domain.review.mapper;

import com.jy.shoppy.domain.review.dto.CommentResponse;
import com.jy.shoppy.domain.review.entity.ReviewComment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReviewCommentMapper {

    @Mapping(source = "id", target = "commentId")
    @Mapping(source = "review.id", target = "reviewId")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "content", target = "content")
    @Mapping(source = "user.role.name", target = "userRole")
    @Mapping(source = "parentComment.id", target = "parentCommentId")
    @Mapping(target = "depth", expression = "java(comment.getDepth())")
    @Mapping(target = "childComments", ignore = true)
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    CommentResponse toResponse(ReviewComment comment);

    List<CommentResponse> toResponseList(List<ReviewComment> comments);
}
