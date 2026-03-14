package com.example.MobileAppBackend.service;


import com.example.MobileAppBackend.dto.create.CreateCommentRequest;
import com.example.MobileAppBackend.model.Comment;
import com.example.MobileAppBackend.model.User;
import com.example.MobileAppBackend.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final ModelMapper modelMapper;

    public Comment createComment(CreateCommentRequest createCommentRequest) {
        if(!createCommentRequest.getAuthorId().equals(getCurrentUserId())) {
            log.error("You are not allowed to edit this comment");
            throw new RuntimeException("You are not allowed to edit this comment");
        }

        Comment comment = modelMapper.map(createCommentRequest, Comment.class);
        comment.setAuthorId(getCurrentUserId());
        log.debug("New comment created: {}", comment);
        log.info("New comment created");
        return commentRepository.save(comment);
    }

    public List<Comment> getAllUserComments(String author_id){
        List<Comment> comments = commentRepository.findCommentsByAuthorId(author_id);
        return comments;
    }


    public List<Comment> getAllPostComments(String post_id){
        List<Comment> comments = commentRepository.findCommentsByPostId(post_id);
        return comments;
    }

    public Comment editComment(String id, CreateCommentRequest createCommentRequest){

        Comment existingComment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if(!existingComment.getAuthorId().equals(getCurrentUserId())) {
            log.error("You are not allowed to edit this comment");
            throw new RuntimeException("You are not allowed to edit this comment");
        }
        Comment comment =  modelMapper.map(createCommentRequest, Comment.class);

        Optional.ofNullable(comment.getAuthorId()).ifPresent(existingComment::setAuthorId);
        Optional.ofNullable(comment.getPostId()).ifPresent(existingComment::setPostId);
        Optional.ofNullable(comment.getText()).ifPresent(existingComment::setText);
        Optional.ofNullable(comment.getCreated_at()).ifPresent(existingComment::setCreated_at);
        log.debug("Edited comment created: {}", comment);
        log.info("Comment updated successfully");
        return commentRepository.save(existingComment);

    }
    public void deleteComment(String id){
        Optional<Comment> optionalComment = commentRepository.findById(id);
        Comment comment = optionalComment.get();
        if(!comment.getAuthorId().equals(getCurrentUserId())) {
            throw new RuntimeException("You are not allowed to remove this comment");
        }
        optionalComment.ifPresent(commentRepository::delete);
        log.info("Comment deleted successfully");
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        return currentUser.getId();
    }

}
