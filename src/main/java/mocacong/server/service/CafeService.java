package mocacong.server.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mocacong.server.domain.Cafe;
import mocacong.server.domain.Member;
import mocacong.server.domain.Score;
import mocacong.server.domain.StudyType;
import mocacong.server.dto.request.CafeRegisterRequest;
import mocacong.server.dto.response.FindCafeResponse;
import mocacong.server.dto.response.ReviewResponse;
import mocacong.server.exception.notfound.NotFoundCafeException;
import mocacong.server.exception.notfound.NotFoundMemberException;
import mocacong.server.repository.CafeRepository;
import mocacong.server.repository.MemberRepository;
import mocacong.server.repository.ScoreRepository;
import mocacong.server.repository.StudyTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CafeService {

    private static final String SOLO_STUDY_TYPE = "solo";
    private static final String GROUP_STUDY_TYPE = "group";
    private static final String BOTH_STUDY_TYPE = "both";

    private final CafeRepository cafeRepository;
    private final MemberRepository memberRepository;
    private final StudyTypeRepository studyTypeRepository;
    private final ScoreRepository scoreRepository;

    public void save(CafeRegisterRequest request) {
        Cafe cafe = new Cafe(request.getId(), request.getName());
        cafeRepository.findByMapId(request.getId())
                .ifPresentOrElse(
                        cafe1 -> {
                        },
                        () -> cafeRepository.save(cafe)
                );
    }

    @Transactional(readOnly = true)
    public FindCafeResponse findCafeByMapId(String email, String mapId) {
        Cafe cafe = cafeRepository.findByMapId(mapId)
                .orElseThrow(NotFoundCafeException::new);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(NotFoundMemberException::new);
        Score scoreByLoginUser = scoreRepository.findByCafeIdAndMemberId(cafe.getId(), member.getId())
                .orElse(null);
        String studyType = findMostFrequentStudyTypes(cafe.getId());

        List<ReviewResponse> reviewResponses = cafe.getReviews()
                .stream()
                .map(ReviewResponse::from)
                .collect(Collectors.toList());

        // TODO: 코멘트 기능 추가 시에 변경할 것
        return new FindCafeResponse(
                cafe.findAverageScore(),
                scoreByLoginUser != null ? scoreByLoginUser.getScore() : null,
                studyType,
                reviewResponses.size(),
                reviewResponses,
                0,
                new ArrayList<>()
        );
    }

    private String findMostFrequentStudyTypes(Long cafeId) {
        List<StudyType> soloStudyTypes = studyTypeRepository.findAllByCafeIdAndStudyTypeValue(cafeId, SOLO_STUDY_TYPE);
        List<StudyType> groupStudyTypes = studyTypeRepository.findAllByCafeIdAndStudyTypeValue(cafeId, GROUP_STUDY_TYPE);

        if (isEmptyStudyTypes(soloStudyTypes, groupStudyTypes)) {
            return null;
        }

        if (soloStudyTypes.size() > groupStudyTypes.size()) {
            return SOLO_STUDY_TYPE;
        }
        if (soloStudyTypes.size() < groupStudyTypes.size()) {
            return GROUP_STUDY_TYPE;
        }
        return BOTH_STUDY_TYPE;
    }

    private boolean isEmptyStudyTypes(List<StudyType> soloStudyTypes, List<StudyType> groupStudyTypes) {
        return soloStudyTypes.isEmpty() && groupStudyTypes.isEmpty();
    }
}
