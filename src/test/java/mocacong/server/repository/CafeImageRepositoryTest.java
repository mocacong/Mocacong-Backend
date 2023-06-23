package mocacong.server.repository;

import mocacong.server.domain.Cafe;
import mocacong.server.domain.CafeImage;
import mocacong.server.domain.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@RepositoryTest
public class CafeImageRepositoryTest {

    @Autowired
    private CafeImageRepository cafeImageRepository;
    @Autowired
    private CafeRepository cafeRepository;
    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("내가 올린 카페 이미지부터 조회한다")
    void findAllByCafeIdAndIsUsedOrderByCafeImageIdDesc() throws IOException {
        Pageable pageable = PageRequest.of(0, 5);
        Cafe cafe = cafeRepository.save(new Cafe("1", "케이카페"));
        Member member1 = memberRepository.save(new Member("kth@naver.com", "abcd1234", "케이",
                "010-1234-5678"));
        Member member2 = memberRepository.save(new Member("dla@naver.com", "abcd1234", "메리",
                "010-1234-5678"));
        CafeImage cafeImage1 = new CafeImage("test_img.jpg", true, cafe, member1);
        CafeImage cafeImage2 = new CafeImage("test_img2.jpg", true, cafe, member1);
        CafeImage cafeImage3 = new CafeImage("test_img.jpg", true, cafe, member2);
        CafeImage cafeImage4 = new CafeImage("test_img.jpg", true, cafe, member1);
        CafeImage cafeImage5 = new CafeImage("test_img2.jpg", true, cafe, member1);
        CafeImage cafeImage6 = new CafeImage("test_img.jpg", true, cafe, member2);
        cafeImageRepository.save(cafeImage1);
        cafeImageRepository.save(cafeImage2);
        cafeImageRepository.save(cafeImage3);
        cafeImageRepository.save(cafeImage4);
        cafeImageRepository.save(cafeImage5);
        cafeImageRepository.save(cafeImage6);

        Slice<CafeImage> actual = cafeImageRepository.findAllByCafeIdAndIsUsedOrderByCafeImageId(cafe.getId(),
                member1.getId(), pageable); // member1로 카페 이미지 조회
        List<CafeImage> cafeImages = actual.getContent();

        assertAll(
                () -> assertThat(cafeImages).hasSize(5),
                () -> assertThat(cafeImages.get(0)).isEqualTo(cafeImage1),
                () -> assertThat(cafeImages.get(1)).isEqualTo(cafeImage2),
                () -> assertThat(cafeImages.get(2)).isEqualTo(cafeImage4),
                () -> assertThat(cafeImages.get(3)).isEqualTo(cafeImage5),
                () -> assertThat(cafeImages.get(4)).isEqualTo(cafeImage3)
        );
    }
}
