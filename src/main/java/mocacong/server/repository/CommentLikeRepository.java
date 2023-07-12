package mocacong.server.repository;

import mocacong.server.domain.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike,Long> {
    @Query("select cl.id " +
            "from CommentLike cl " +
            "join cl.comment c " +
            "join cl.member m " +
            "where c.id = :commentId and m.id = :memberId")
    Optional<Long> findCommentLikeIdByCommentIdAndMemberId(Long memberId, Long commentId);

    List<CommentLike> findAllByMemberId(Long memberId);
}
