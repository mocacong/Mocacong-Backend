package mocacong.server.service;

import mocacong.server.domain.Cafe;
import mocacong.server.dto.request.CafeRegisterRequest;
import mocacong.server.repository.CafeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ServiceTest
class CafeServiceTest {

    @Autowired
    private CafeService cafeService;
    @Autowired
    private CafeRepository cafeRepository;

    @Test
    @DisplayName("등록되지 않은 카페를 성공적으로 등록한다")
    void cafeSave() {
        CafeRegisterRequest request = new CafeRegisterRequest("20", "메리네 카페");

        cafeService.cafeSave(request);

        List<Cafe> actual = cafeRepository.findAll();
        assertThat(actual).hasSize(1);
    }

    @Test
    @DisplayName("등록되어 있는 카페는 등록하지 않는다")
    void cafeSaveDuplicate() {
        CafeRegisterRequest request1 = new CafeRegisterRequest("20", "메리네 카페");
        cafeService.cafeSave(request1);

        CafeRegisterRequest request2 = new CafeRegisterRequest("20", "카페");
        cafeService.cafeSave(request2);

        List<Cafe> actual = cafeRepository.findAll();
        assertThat(actual).hasSize(1);
    }
}
