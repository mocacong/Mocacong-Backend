package mocacong.server.service;

import lombok.RequiredArgsConstructor;
import mocacong.server.domain.Cafe;
import mocacong.server.domain.Favorite;
import mocacong.server.domain.Member;
import mocacong.server.dto.response.FavoriteSaveResponse;
import mocacong.server.exception.badrequest.AlreadyExistsFavorite;
import mocacong.server.exception.notfound.NotFoundCafeException;
import mocacong.server.exception.notfound.NotFoundFavoriteException;
import mocacong.server.exception.notfound.NotFoundMemberException;
import mocacong.server.repository.CafeRepository;
import mocacong.server.repository.FavoriteRepository;
import mocacong.server.repository.MemberRepository;
import mocacong.server.service.event.DeleteMemberEvent;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final MemberRepository memberRepository;
    private final CafeRepository cafeRepository;

    @CacheEvict(key = "#mapId", value = "cafePreviewCache")
    @Transactional
    public FavoriteSaveResponse save(String email, String mapId) {
        Cafe cafe = cafeRepository.findByMapId(mapId)
                .orElseThrow(NotFoundCafeException::new);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(NotFoundMemberException::new);
        validateDuplicateFavorite(cafe.getId(), member.getId());

        try {
            Favorite favorite = new Favorite(member, cafe);
            return new FavoriteSaveResponse(favoriteRepository.save(favorite).getId());
        } catch (DataIntegrityViolationException e) {
            throw new AlreadyExistsFavorite();
        }
    }

    private void validateDuplicateFavorite(Long cafeId, Long memberId) {
        favoriteRepository.findFavoriteIdByCafeIdAndMemberId(cafeId, memberId).ifPresent(fav -> {
            throw new AlreadyExistsFavorite();
        });
    }

    @CacheEvict(key = "#mapId", value = "cafePreviewCache")
    @Transactional
    public void delete(String email, String mapId) {
        Cafe cafe = cafeRepository.findByMapId(mapId).orElseThrow(NotFoundCafeException::new);
        Member member = memberRepository.findByEmail(email).orElseThrow(NotFoundMemberException::new);
        Long favoriteId = favoriteRepository.findFavoriteIdByCafeIdAndMemberId(cafe.getId(), member.getId())
                .orElseThrow(NotFoundFavoriteException::new);

        favoriteRepository.deleteById(favoriteId);
    }

    @EventListener
    public void deleteAllWhenMemberDelete(DeleteMemberEvent event) {
        Member member = event.getMember();
        favoriteRepository.findAllByMemberId(member.getId()).forEach(Favorite::removeMember);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener
    public void deleteFavoritesWhenMemberDeleted(DeleteMemberEvent event) {
        favoriteRepository.deleteAllByMemberIdIsNull();
    }
}
