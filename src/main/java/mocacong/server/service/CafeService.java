package mocacong.server.service;

import lombok.RequiredArgsConstructor;
import mocacong.server.domain.*;
import mocacong.server.domain.cafedetail.*;
import mocacong.server.dto.request.CafeRegisterRequest;
import mocacong.server.dto.request.CafeReviewRequest;
import mocacong.server.dto.response.CafeReviewResponse;
import mocacong.server.dto.response.FindCafeResponse;
import mocacong.server.dto.response.ReviewResponse;
import mocacong.server.exception.badrequest.AlreadyExistsCafeReview;
import mocacong.server.exception.notfound.NotFoundCafeException;
import mocacong.server.exception.notfound.NotFoundMemberException;
import mocacong.server.exception.notfound.NotFoundReviewException;
import mocacong.server.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    private final ReviewRepository reviewRepository;

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

    @Transactional
    public CafeReviewResponse saveCafeReview(String email, String mapId, CafeReviewRequest request) {
        Cafe cafe = cafeRepository.findByMapId(mapId)
                .orElseThrow(NotFoundCafeException::new);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(NotFoundMemberException::new);
        checkAlreadySaveCafeReview(cafe, member);

        saveCafeDetails(request, cafe, member);

        cafe.updateCafeDetails();
        String updatedStudyType = findMostFrequentStudyTypes(cafe.getId());

        return CafeReviewResponse.of(cafe.findAverageScore(), updatedStudyType, cafe);
    }

    private void checkAlreadySaveCafeReview(Cafe cafe, Member member) {
        reviewRepository.findIdByCafeIdAndMemberId(cafe.getId(), member.getId())
                .ifPresent(review -> {
                    throw new AlreadyExistsCafeReview();
                });
    }

    private String findMostFrequentStudyTypes(Long cafeId) {
        List<String> soloStudyTypeValues = studyTypeRepository.findAllByCafeIdAndStudyTypeValue(cafeId, SOLO_STUDY_TYPE);
        List<String> groupStudyTypeValues = studyTypeRepository.findAllByCafeIdAndStudyTypeValue(cafeId, GROUP_STUDY_TYPE);

        if (isEmptyStudyTypes(soloStudyTypeValues, groupStudyTypeValues)) {
            return null;
        }

        if (soloStudyTypeValues.size() > groupStudyTypeValues.size()) {
            return SOLO_STUDY_TYPE;
        }
        if (soloStudyTypeValues.size() < groupStudyTypeValues.size()) {
            return GROUP_STUDY_TYPE;
        }
        return BOTH_STUDY_TYPE;
    }

    private boolean isEmptyStudyTypes(List<String> soloStudyTypeValues, List<String> groupStudyTypeValues) {
        return soloStudyTypeValues.isEmpty() && groupStudyTypeValues.isEmpty();
    }

    private void saveCafeDetails(CafeReviewRequest request, Cafe cafe, Member member) {
        Score score = new Score(request.getMyScore(), member, cafe);
        scoreRepository.save(score);
        StudyType studyType = new StudyType(member, cafe, request.getMyStudyType());
        studyTypeRepository.save(studyType);
        CafeDetail cafeDetail = new CafeDetail(
                Wifi.from(request.getMyWifi()),
                Parking.from(request.getMyParking()),
                Toilet.from(request.getMyToilet()),
                Desk.from(request.getMyDesk()),
                Power.from(request.getMyPower()),
                Sound.from(request.getMySound())
        );
        Review review = new Review(member, cafe, studyType, cafeDetail);
        reviewRepository.save(review);
    }

<<<<<<< HEAD
    @Transactional
    public CafeReviewResponse updateCafeReview(String email, String mapId, CafeReviewRequest request) {
        Cafe cafe = cafeRepository.findByMapId(mapId)
                .orElseThrow(NotFoundCafeException::new);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(NotFoundMemberException::new);

        updateCafeReviewDetails(request, cafe, member);
        cafe.updateCafeDetails();
        String updatedStudyType = findMostFrequentStudyTypes(cafe.getId());

        return CafeReviewResponse.of(cafe.findAverageScore(), updatedStudyType, cafe);
    }

    private void updateCafeReviewDetails(CafeReviewRequest request, Cafe cafe, Member member) {
        Review review = reviewRepository.findByCafeIdAndMemberId(cafe.getId(), member.getId());
        if (review == null) {
            throw new NotFoundReviewException();
        }

        Score score = scoreRepository.findByCafeIdAndMemberId(cafe.getId(), member.getId())
                .orElseThrow(NotFoundReviewException::new);
        score.setScore(request.getMyScore());
        scoreRepository.save(score);

        StudyType studyType = review.getStudyType();
        studyType.setStudyTypeValue(request.getMyStudyType());
        studyTypeRepository.save(studyType);

        CafeDetail cafeDetail = review.getCafeDetail();
=======
    public void updateCafeDetails(Long cafeId, CafeReviewRequest request, Member member) {
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new NotFoundCafeException());
        checkExistingCafeReview(cafe, member);

        Score score = scoreRepository.findByMemberAndCafe(member, cafe);
        score.setScore(request.getMyScore());
        scoreRepository.save(score);

        StudyType studyType = studyTypeRepository.findByMemberAndCafe(member, cafe);
        studyType.setStudyTypeValue(request.getMyStudyType());
        studyTypeRepository.save(studyType);

        CafeDetail cafeDetail = cafe.getCafeDetail();
>>>>>>> 91436bb (feat: 카페 세부정보 수정 비즈니스 로직 구현)
        cafeDetail.setWifi(Wifi.from(request.getMyWifi()));
        cafeDetail.setParking(Parking.from(request.getMyParking()));
        cafeDetail.setToilet(Toilet.from(request.getMyToilet()));
        cafeDetail.setDesk(Desk.from(request.getMyDesk()));
        cafeDetail.setPower(Power.from(request.getMyPower()));
        cafeDetail.setSound(Sound.from(request.getMySound()));
<<<<<<< HEAD
        reviewRepository.save(review);
=======
        cafe.setCafeDetail(cafeDetail);
        cafeRepository.save(cafe);
    }

    private void checkExistingCafeReview(Cafe cafe, Member member) {
        if (!reviewRepository.findIdByCafeIdAndMemberId(cafe.getId(), member.getId()).isPresent()) {
            throw new NotFoundReviewException();
        }
>>>>>>> 91436bb (feat: 카페 세부정보 수정 비즈니스 로직 구현)
    }
}
