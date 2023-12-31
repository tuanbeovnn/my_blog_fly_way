package com.myblogbackend.blog.services.impl;

import com.myblogbackend.blog.exception.commons.BlogRuntimeException;
import com.myblogbackend.blog.exception.commons.ErrorCode;
import com.myblogbackend.blog.mapper.CommentMapper;
import com.myblogbackend.blog.pagination.OffsetPageRequest;
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
import org.springframework.stereotype.Service;

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
        try {
            //get the user signed in
            var signedInUser = JWTSecurityUtil.getJWTUserInfo().orElseThrow();
            //find the post by post id
            var post = postRepository.findById(commentRequest.getPostId())
                    .orElseThrow(() -> new BlogRuntimeException(ErrorCode.ID_NOT_FOUND));
            //convert comment request to comment entity
            var commentEntity = commentMapper.toCommentEntity(commentRequest);
            //find user based on signedInUser id
            var userFound = usersRepository.findById(signedInUser.getId()).orElseThrow();
            //set the user to the comment
            commentEntity.setUser(userFound);
            //set the post to the comment
            commentEntity.setPost(post);
            //save comment
            var createdComment = commentRepository.save(commentEntity);
            //add logger
            logger.info("Create the comment of a post id by user successfully {}", commentRequest.getPostId());
            //convert to comment response
            return commentMapper.toCommentResponse(createdComment);
        } catch (Exception e) {
            logger.info("Failed to create the comment by user id and post id", e);
            throw new RuntimeException("Failed to create the comment by user id and post id");
        }
    }

    @Override
    public PaginationPage<CommentResponse> getListCommentsByPostId(final Integer offset, final Integer limited, final UUID postId) {
        try {
            //create the pageable by OffsetPageRequest class
            var pageable = new OffsetPageRequest(offset, limited);
            //find list of comments by post id and pageable
            var commentEntityList = commentRepository.findAllByPostId(postId, pageable);
            //stream and map to return list of comment response
            var commentResponseList = commentEntityList
                    .getContent()
                    .stream()
                    .map(item ->
                            commentMapper.toCommentResponse(item))
                    .collect(Collectors.toList());
            //add logger
            logger.info("Get list of comment by post id successfully {}", postId);
            //create the PaginationPage instance, set records, offset, limit and total
            return new PaginationPage<CommentResponse>()
                    .setRecords(commentResponseList)
                    .setOffset(commentEntityList.getNumber())
                    .setLimit(commentEntityList.getSize())
                    .setTotalRecords(commentEntityList.getTotalElements());
        } catch (Exception e) {
            logger.info("Failed to get list of comments by post id", e);
            throw new RuntimeException("Failed to get comment list by post id");
        }
    }
}
