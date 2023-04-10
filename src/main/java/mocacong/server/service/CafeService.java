package mocacong.server.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mocacong.server.domain.*;
import mocacong.server.domain.cafedetail.*;
import mocacong.server.dto.request.CafeFilterRequest;
import mocacong.server.dto.request.CafeRegisterRequest;
import mocacong.server.dto.request.CafeReviewRequest;
import mocacong.server.dto.request.CafeReviewUpdateRequest;
import mocacong.server.dto.response.*;
import mocacong.server.exception.badrequest.AlreadyExistsCafeReview;
import mocacong.server.exception.notfound.NotFoundCafeException;
import mocacong.server.exception.notfound.NotFoundMemberException;
import mocacong.server.exception.notfound.NotFoundReviewException;
import mocacong.server.repository.*;
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
    private final ReviewRepository reviewRepository;
    private final FavoriteRepository favoriteRepository;

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
        CafeDetail cafeDetail = cafe.getCafeDetail();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(NotFoundMemberException::new);
        Score scoreByLoginUser = scoreRepository.findByCafeIdAndMemberId(cafe.getId(), member.getId())
                .orElse(null);
        String studyType = findMostFrequentStudyTypes(cafe.getId());
        Long favoriteId = favoriteRepository.findFavoriteIdByCafeIdAndMemberId(cafe.getId(), member.getId())
                .orElse(null);
        List<CommentResponse> commentResponses = findCommentResponses(cafe, member);
        return new FindCafeResponse(
                favoriteId != null,
                favoriteId,
                cafe.findAverageScore(),
                scoreByLoginUser != null ? scoreByLoginUser.getScore() : null,
                studyType,
                cafeDetail.getWifiValue(),
                cafeDetail.getParkingValue(),
                cafeDetail.getToiletValue(),
                cafeDetail.getPowerValue(),
                cafeDetail.getSoundValue(),
                cafeDetail.getDeskValue(),
                cafe.getReviews().size(),
                commentResponses.size(),
                commentResponses
        );
    }

    private List<CommentResponse> findCommentResponses(Cafe cafe, Member member) {
        return cafe.getComments()
                .stream()
                .map(comment -> {
                    if (comment.isWrittenByMember(member)) {
                        return new CommentResponse(member.getImgUrl(), member.getNickname(), comment.getContent(), true);
                    } else {
                        return new CommentResponse(comment.getWriterImgUrl(), comment.getWriterNickname(), comment.getContent(), false);
                    }
                })
                .collect(Collectors.toList());
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
                request.getMyWifi() == null ? null : Wifi.from(request.getMyWifi()),
                request.getMyParking() == null ? null : Parking.from(request.getMyParking()),
                request.getMyToilet() == null ? null : Toilet.from(request.getMyToilet()),
                request.getMyDesk() == null ? null : Desk.from(request.getMyDesk()),
                request.getMyPower() == null ? null : Power.from(request.getMyPower()),
                request.getMySound() == null ? null : Sound.from(request.getMySound())
        );
        Review review = new Review(member, cafe, studyType, cafeDetail);
        reviewRepository.save(review);
    }

    @Transactional
    public CafeReviewUpdateResponse updateCafeReview(String email, String mapId, CafeReviewUpdateRequest request) {
        Cafe cafe = cafeRepository.findByMapId(mapId)
                .orElseThrow(NotFoundCafeException::new);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(NotFoundMemberException::new);

        updateCafeReviewDetails(request, cafe, member);
        cafe.updateCafeDetails();
        String updatedStudyType = findMostFrequentStudyTypes(cafe.getId());

        return CafeReviewUpdateResponse.of(cafe.findAverageScore(), updatedStudyType, cafe);
    }

    private void updateCafeReviewDetails(CafeReviewUpdateRequest request, Cafe cafe, Member member) {
        Review review = reviewRepository.findByCafeIdAndMemberId(cafe.getId(), member.getId())
                .orElseThrow(NotFoundReviewException::new);
        Score score = scoreRepository.findByCafeIdAndMemberId(cafe.getId(), member.getId())
                .orElseThrow(NotFoundReviewException::new);

        CafeDetail updatedCafeDetail = new CafeDetail(
                request.getMyWifi() == null ? null : Wifi.from(request.getMyWifi()),
                request.getMyParking() == null ? null : Parking.from(request.getMyParking()),
                request.getMyToilet() == null ? null : Toilet.from(request.getMyToilet()),
                request.getMyDesk() == null ? null : Desk.from(request.getMyDesk()),
                request.getMyPower() == null ? null : Power.from(request.getMyPower()),
                request.getMySound() == null ? null : Sound.from(request.getMySound())
        );

        score.updateScore(request.getMyScore());
        review.updateStudyType(request.getMyStudyType());
        review.updateReview(updatedCafeDetail);
    }

    public CafeFilterResponse filterCafesByStudyType(String studyTypeValue, CafeFilterRequest requestBody) {
        List<Cafe> cafes = cafeRepository.findByStudyTypeValue(studyTypeValue);
        Set<String> filteredCafeMapIds = cafes.stream()
                .map(Cafe::getMapId)
                .collect(Collectors.toSet());

        List<String> filteredIds = requestBody.getMapIds().stream()
                .filter(filteredCafeMapIds::contains)
                .collect(Collectors.toList());

        return new CafeFilterResponse(filteredIds);
    }
}
