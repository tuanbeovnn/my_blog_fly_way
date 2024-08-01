package com.myblogbackend.blog.services.impl;

import com.myblogbackend.blog.exception.commons.BlogRuntimeException;
import com.myblogbackend.blog.exception.commons.ErrorCode;
import com.myblogbackend.blog.mapper.CommentMapper;
import com.myblogbackend.blog.models.CommentEntity;
import com.myblogbackend.blog.pagination.PageList;
import com.myblogbackend.blog.pagination.PaginationPage;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final PostRepository postRepository;
    private final CommentMapper commentMapper;
    private final UsersRepository usersRepository;
    private final CommentRepository commentRepository;
    private static final Logger logger = LogManager.getLogger(PostServiceImpl.class);

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
        var createdComment = commentRepository.save(commentEntity);
        logger.info("Created the comment for post ID {} by user ID {}",
                commentRequest.getPostId(), signedInUser.getId());
        return commentMapper.toCommentResponse(createdComment);
    }

    @Override
    public PageList<CommentResponse> getListCommentsByPostId(final Pageable pageable, final UUID postId) {
//        var post = postRepository.findById(postId)
//                .orElseThrow(() -> new BlogRuntimeException(ErrorCode.ID_NOT_FOUND));
//
//        // Fetch only parent comments with pagination
//        List<CommentEntity> parentCommentsPage = commentRepository.findParentCommentsByPostIdAndStatusTrue(postId, pageable);
//        var commentResponses = mapPostEntitiesToCommentResponses(parentCommentsPage);
//
//
//        logger.info("Retrieved comments for post ID {}", postId);
//        // Return the paginated result with comments
//        return buildPaginatingResponse(commentResponses, pageable.getPageSize(), pageable.getPageNumber(), parentCommentsPage.size());


        var post = postRepository.findById(postId)
                .orElseThrow(() -> new BlogRuntimeException(ErrorCode.ID_NOT_FOUND));

        // Fetch only parent comments with pagination
        List<CommentEntity> parentCommentsPage = commentRepository.findParentCommentsByPostIdAndStatusTrue(postId, pageable);

        // Extract parent comment IDs
        List<UUID> parentCommentIds = parentCommentsPage.stream()
                .map(CommentEntity::getId)
                .collect(Collectors.toList());

        // Fetch replies for the parent comments
        List<CommentEntity> allReplies = new ArrayList<>();
        if (!parentCommentIds.isEmpty()) {
            allReplies = commentRepository.findAllByParentCommentIdIn(parentCommentIds);
        }

        // Create a map of parent comments and their replies
        Map<UUID, CommentResponse> commentResponseMap = new HashMap<>();
        List<CommentResponse> parentComments = new ArrayList<>();

        // Convert parent comments to response
        for (CommentEntity commentEntity : parentCommentsPage) {
            CommentResponse commentResponse = commentMapper.toCommentResponse(commentEntity);
            commentResponse.setReplies(new ArrayList<>()); // Initialize replies
            commentResponseMap.put(commentResponse.getId(), commentResponse);
            parentComments.add(commentResponse);
        }

        // Convert replies to response and map them
        for (CommentEntity replyEntity : allReplies) {
            CommentResponse replyResponse = commentMapper.toCommentResponse(replyEntity);
            CommentResponse parentResponse = commentResponseMap.get(replyEntity.getParentComment().getId());
            if (parentResponse != null) {
                parentResponse.getReplies().add(replyResponse);
            }
        }

        logger.info("Retrieved comments for post ID {}", postId);

        return buildPaginatingResponse(parentComments, pageable.getPageSize(), pageable.getPageNumber(), parentCommentsPage.size());

    }

    private List<CommentResponse> mapPostEntitiesToCommentResponses(final List<CommentEntity> commentEntities) {
        return commentEntities.stream()
                .map(commentMapper::toCommentResponse)
                .toList();
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

    private static PaginationPage<CommentResponse> getCommentResponsePaginationPage(final Integer offset,
                                                                                    final Integer limited,
                                                                                    final List<CommentResponse> commentResponses,
                                                                                    final Page<CommentEntity> comments) {
        return new PaginationPage<CommentResponse>()
                .setRecords(commentResponses)
                .setOffset(offset)
                .setLimit(limited)
                .setTotalRecords(comments.getTotalElements());
    }


    private PageList<CommentResponse> buildPaginatingResponse(final List<CommentResponse> responses,
                                                              final int pageSize,
                                                              final int currentPage,
                                                              final long total) {
        return PageList.<CommentResponse>builder()
                .records(responses)
                .limit(pageSize)
                .offset(currentPage)
//                .totalRecords(total)
                .totalPage((int) Math.ceil(total * 1.0 / pageSize))
                .build();
    }


}
