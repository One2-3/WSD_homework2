package com.example.bookstore.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    boolean existsByUserIdAndCommentId(Long userId, Long commentId);

    Optional<CommentLike> findByUserIdAndCommentId(Long userId, Long commentId);

    void deleteByCommentId(Long commentId);

    void deleteByCommentIdIn(Collection<Long> commentIds);

    @Query("select cl.commentId from CommentLike cl where cl.userId = :userId and cl.commentId in :commentIds")
    List<Long> findLikedCommentIds(Long userId, Collection<Long> commentIds);
}
