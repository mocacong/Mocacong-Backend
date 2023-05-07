package mocacong.server.repository;

import mocacong.server.domain.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByMemberId(Long memberId);

    Slice<Comment> findAllByCafeId(Long cafeId, Pageable pageRequest);

    Slice<Comment> findAllByCafeIdAndMemberId(Long cafeId, Long memberId, Pageable pageRequest);
}
