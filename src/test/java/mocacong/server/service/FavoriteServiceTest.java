package mocacong.server.service;

import mocacong.server.domain.Cafe;
import mocacong.server.domain.Favorite;
import mocacong.server.domain.Member;
import mocacong.server.dto.response.FavoriteSaveResponse;
import mocacong.server.exception.badrequest.AlreadyExistsFavorite;
import mocacong.server.exception.notfound.NotFoundFavoriteException;
import mocacong.server.repository.CafeRepository;
import mocacong.server.repository.FavoriteRepository;
import mocacong.server.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ServiceTest
class FavoriteServiceTest {

    @Autowired
    private FavoriteService favoriteService;
    @Autowired
    private FavoriteRepository favoriteRepository;
    @Autowired
    private CafeRepository cafeRepository;
    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("회원이 카페를 즐겨찾기 등록한다")
    void save() {
        Member member = new Member("kth990303@naver.com", "encodePassword", "케이", "010-1234-5678");
        memberRepository.save(member);
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);

        FavoriteSaveResponse actual = favoriteService.save(member.getEmail(), cafe.getMapId());

        List<Favorite> favorites = favoriteRepository.findAll();
        assertAll(
                () -> assertThat(favorites).hasSize(1),
                () -> assertThat(favorites.get(0).getId()).isEqualTo(actual.getFavoriteId())
        );
    }

    @Test
    @DisplayName("이미 즐겨찾기 등록한 카페에 즐겨찾기를 등록하면 예외를 반환한다")
    void saveDuplicate() {
        Member member = new Member("kth990303@naver.com", "encodePassword", "케이", "010-1234-5678");
        memberRepository.save(member);
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);
        favoriteService.save(member.getEmail(), cafe.getMapId());

        assertThatThrownBy(() -> favoriteService.save(member.getEmail(), cafe.getMapId()))
                .isInstanceOf(AlreadyExistsFavorite.class);
    }

    @Test
    @DisplayName("회원이 카페를 즐겨찾기 삭제한다")
    void delete() {
        Member member = new Member("kth990303@naver.com", "encodePassword", "케이", "010-1234-5678");
        memberRepository.save(member);
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);
        Favorite favorite = new Favorite(member, cafe);
        favoriteRepository.save(favorite);

        favoriteService.delete(member.getEmail(), cafe.getMapId());

        assertThat(favoriteRepository.findById(favorite.getId())).isEmpty();
    }

    @Test
    @DisplayName("회원이 즐겨찾기를 안한 카페 즐겨찾기 삭제할 경우 예외를 던진다")
    void deleteWithException() {
        Member member = new Member("kth990303@naver.com", "encodePassword", "케이", "010-1234-5678");
        memberRepository.save(member);
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);

        assertThrows(NotFoundFavoriteException.class,
                () -> favoriteService.delete(member.getEmail(), cafe.getMapId()));
    }

    @Test
    @DisplayName("회원이 한 카페를 동시에 여러 번 즐겨찾기 등록 시도해도 한 번만 등록된다")
    void saveFavoriteWithConcurrent() throws InterruptedException {
        Member member = new Member("kth990303@naver.com", "encodePassword", "케이", "010-1234-5678");
        memberRepository.save(member);
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        CountDownLatch latch = new CountDownLatch(3);
        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < 3; i++) {
            executorService.execute(() -> {
                try {
                    favoriteService.save(member.getEmail(), cafe.getMapId());
                } catch (AlreadyExistsFavorite e) {
                    exceptions.add(e); // 중복 예외를 리스트에 추가
                }
                latch.countDown();
            });
        }
        latch.await();

        List<Favorite> favorites = favoriteRepository.findAll();
        assertAll(
                () -> assertThat(!exceptions.isEmpty()).isTrue(),
                () -> assertThat(favorites).hasSize(1)
        );
    }
}
