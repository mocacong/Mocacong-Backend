package mocacong.server.repository;

import mocacong.server.config.BaseTimeConfig;
import mocacong.server.domain.Cafe;
import mocacong.server.domain.Member;
import mocacong.server.domain.StudyType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(BaseTimeConfig.class)
class StudyTypeRepositoryTest {

    @Autowired
    private StudyTypeRepository studyTypeRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private CafeRepository cafeRepository;

    @Test
    @DisplayName("특정 카페의 모든 solo 타입 studyTypeValue 를 조회한다")
    void findAllByCafeIdAndSolo() {
        Cafe savedCafe = cafeRepository.save(new Cafe("1", "케이카페"));
        Member savedMember1 = memberRepository.save(new Member("kth@naver.com", "abcd1234", "케일", "010-1234-5671"));
        Member savedMember2 = memberRepository.save(new Member("kth2@naver.com", "abcd1234", "케이", "010-1234-5672"));
        Member savedMember3 = memberRepository.save(new Member("kth3@naver.com", "abcd1234", "케삼", "010-1234-5673"));
        Member savedMember4 = memberRepository.save(new Member("kth4@naver.com", "abcd1234", "케사", "010-1234-5674"));
        studyTypeRepository.save(new StudyType(savedMember1, savedCafe, "solo"));
        studyTypeRepository.save(new StudyType(savedMember2, savedCafe, "solo"));
        studyTypeRepository.save(new StudyType(savedMember3, savedCafe, "solo"));
        studyTypeRepository.save(new StudyType(savedMember4, savedCafe, "group"));

        List<String> actual = studyTypeRepository.findAllByCafeIdAndStudyTypeValue(savedCafe.getId(), "solo");

        assertThat(actual).hasSize(3);
    }

    @Test
    @DisplayName("특정 카페의 모든 group 타입 studyTypeValue 를 조회한다")
    void findAllByCafeIdAndGroup() {
        Cafe savedCafe = cafeRepository.save(new Cafe("1", "케이카페"));
        Member savedMember1 = memberRepository.save(new Member("kth@naver.com", "abcd1234", "케일", "010-1234-5671"));
        Member savedMember2 = memberRepository.save(new Member("kth2@naver.com", "abcd1234", "케이", "010-1234-5672"));
        Member savedMember3 = memberRepository.save(new Member("kth3@naver.com", "abcd1234", "케삼", "010-1234-5673"));
        Member savedMember4 = memberRepository.save(new Member("kth4@naver.com", "abcd1234", "케사", "010-1234-5674"));
        studyTypeRepository.save(new StudyType(savedMember1, savedCafe, "solo"));
        studyTypeRepository.save(new StudyType(savedMember2, savedCafe, "solo"));
        studyTypeRepository.save(new StudyType(savedMember3, savedCafe, "group"));
        studyTypeRepository.save(new StudyType(savedMember4, savedCafe, "group"));

        List<String> actual = studyTypeRepository.findAllByCafeIdAndStudyTypeValue(savedCafe.getId(), "group");

        assertThat(actual).hasSize(2);
    }
}
