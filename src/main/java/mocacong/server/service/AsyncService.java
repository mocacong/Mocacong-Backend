package mocacong.server.service;

import lombok.RequiredArgsConstructor;
import mocacong.server.repository.FavoriteRepository;
import mocacong.server.service.event.MemberEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class AsyncService {

    private final FavoriteRepository favoriteRepository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener
    public void deleteFavoritesWhenMemberDeleted(MemberEvent event) {
        favoriteRepository.deleteAllByMemberIdIsNull();
    }
}
