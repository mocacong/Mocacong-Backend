package mocacong.server.repository;

import java.util.List;
import mocacong.server.domain.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByMemberId(Long memberId);

    Slice<Comment> findAllByCafeId(Long cafeId, Pageable pageRequest);
}
