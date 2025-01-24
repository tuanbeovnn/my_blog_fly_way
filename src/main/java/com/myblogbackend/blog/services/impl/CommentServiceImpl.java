package com.myblogbackend.blog.services.impl;

import com.myblogbackend.blog.exception.commons.BlogRuntimeException;
import com.myblogbackend.blog.exception.commons.ErrorCode;
import com.myblogbackend.blog.mapper.CommentMapper;
import com.myblogbackend.blog.models.CommentEntity;
import com.myblogbackend.blog.pagination.PageList;
import com.myblogbackend.blog.repositories.CommentRepository;
import com.myblogbackend.blog.repositories.PostRepository;
import com.myblogbackend.blog.repositories.UsersRepository;
import com.myblogbackend.blog.request.CommentRequest;
import com.myblogbackend.blog.response.CommentResponse;
import com.myblogbackend.blog.services.CommentService;
import com.myblogbackend.blog.utils.JWTSecurityUtil;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private static final Logger logger = LogManager.getLogger(PostServiceImpl.class);
    private final PostRepository postRepository;
    private final CommentMapper commentMapper;
    private final UsersRepository usersRepository;
    private final CommentRepository commentRepository;

    @Override
    public CommentResponse createComment(final CommentRequest commentRequest) {
        var signedInUser = JWTSecurityUtil.getJWTUserInfo().orElseThrow();
        var post = postRepository.findById(commentRequest.getPostId())
                .orElseThrow(() -> new BlogRuntimeException(ErrorCode.ID_NOT_FOUND));
        // If the comment has a parent comment, retrieve it from the database
        CommentEntity parentComment = null;
        if (commentRequest.getParentCommentId() != null) {
            parentComment = commentRepository.findById(commentRequest.getParentCommentId())
                    .orElseThrow(() -> new BlogRuntimeException(ErrorCode.PARENT_COMMENT_NOT_FOUND));
        }
        var commentEntity = commentMapper.toCommentEntity(commentRequest);
        var userFound = usersRepository.findById(signedInUser.getId()).orElseThrow();
        commentEntity.setUser(userFound);
        commentEntity.setPost(post);
        commentEntity.setParentComment(parentComment);
        commentEntity.setStatus(true);
        commentEntity.setCreatedBy(signedInUser.getName());
        var createdComment = commentRepository.saveAndFlush(commentEntity);
        logger.info("Created the comment for post ID {} by user ID {}",
                commentRequest.getPostId(), signedInUser.getId());
        return commentMapper.toCommentResponse(createdComment);
    }

    @Override
    public PageList<CommentResponse> retrieveCommentByPostIdV2(final Pageable pageable, final UUID postId) {
        var parentCommentsPage = commentRepository.findParentCommentsByPostIdAndStatusTrue(postId, pageable);
        return getCommentResponsePageList(pageable, parentCommentsPage);
    }

    @Override
    public PageList<CommentResponse> retrieveChildCommentByParentId(final UUID parentId, final Pageable pageable) {
        var childCommentsPage = commentRepository.findChildCommentsByParentId(parentId, pageable);
        return getCommentResponsePageList(pageable, childCommentsPage);
    }

    @Transactional
    @Override
    public CommentResponse updateComment(final UUID commentId, final CommentRequest commentRequest) {
        var signedInUser = JWTSecurityUtil.getJWTUserInfo().orElseThrow();
        var existingComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BlogRuntimeException(ErrorCode.COMMENT_NOT_FOUND));
        if (!existingComment.getUser().getId().equals(signedInUser.getId())) {
            throw new BlogRuntimeException(ErrorCode.UNABLE_EDIT);
        }
        existingComment.setContent(commentRequest.getContent());
        var updatedComment = commentRepository.save(existingComment);
        logger.info("Updated the comment with ID {} by user ID {}",
                commentId, signedInUser.getId());
        return commentMapper.toCommentResponse(updatedComment);
    }

    @Transactional
    @Override
    public void disableComment(final UUID commentId) {
        var signedInUser = JWTSecurityUtil.getJWTUserInfo().orElseThrow();
        // Fetch the comment by ID and owner user ID
        var comment = commentRepository
                .findByIdAndUserId(commentId, signedInUser.getId())
                .orElseThrow(() -> new BlogRuntimeException(ErrorCode.ID_NOT_FOUND));

        logger.info("Disabling post successfully by id {}", comment);
        comment.setStatus(false);
        commentRepository.save(comment);
        // Disable child comments recursively
        disableChildComments(comment);
    }


    private void disableChildComments(final CommentEntity parentComment) {
        // Fetch all child comments of the parent comment
        var childComments = commentRepository.findByParentComment(parentComment);
        // Disable each child comment and save them
        childComments.forEach(childComment -> {
            childComment.setStatus(false);
            commentRepository.save(childComment);
            // Recursively disable child comments of the current child comment
            disableChildComments(childComment);
        });
    }

    private PageList<CommentResponse> buildPaginatingResponse(final List<CommentResponse> responses,
                                                              final int pageSize,
                                                              final int currentPage,
                                                              final long total) {
        return PageList.<CommentResponse>builder()
                .records(responses)
                .limit(pageSize)
                .offset(currentPage)
                .totalRecords(total)
                .totalPage((int) Math.ceil(total * 1.0 / pageSize))
                .build();
    }

    public PageList<CommentResponse> getCommentResponsePageList(final Pageable pageable, final Page<CommentEntity> parentCommentsPage) {
        var response = parentCommentsPage.getContent().stream()
                .map(item -> {
                    var commentResponse = commentMapper.toCommentResponse(item);
                    // Calculate total child comments
                    int totalChildComments = commentRepository.countByParentCommentIdAndStatusTrue(item.getId());
                    commentResponse.setTotalChildComment(totalChildComments);
                    // Determine if there are child comments
                    commentResponse.setIsHasChildComment(totalChildComments > 0);
                    commentResponse.setParentId(item.getParentComment() != null ? item.getParentComment().getId() : null);
                    return commentResponse;
                }).toList();
        return buildPaginatingResponse(response, pageable.getPageSize(), pageable.getPageNumber(), parentCommentsPage.getTotalElements());
    }


}
