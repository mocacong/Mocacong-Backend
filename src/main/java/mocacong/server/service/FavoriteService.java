package mocacong.server.service;

import lombok.RequiredArgsConstructor;
import mocacong.server.domain.Cafe;
import mocacong.server.domain.Favorite;
import mocacong.server.domain.Member;
import mocacong.server.dto.response.FavoriteSaveResponse;
import mocacong.server.exception.notfound.NotFoundCafeException;
import mocacong.server.exception.notfound.NotFoundMemberException;
import mocacong.server.repository.CafeRepository;
import mocacong.server.repository.FavoriteRepository;
import mocacong.server.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final MemberRepository memberRepository;
    private final CafeRepository cafeRepository;

    @Transactional
    public FavoriteSaveResponse save(String email, String mapId) {
        Cafe cafe = cafeRepository.findByMapId(mapId)
                .orElseThrow(NotFoundCafeException::new);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(NotFoundMemberException::new);
        Favorite favorite = new Favorite(member, cafe);
        return new FavoriteSaveResponse(favoriteRepository.save(favorite).getId());
    }
}
