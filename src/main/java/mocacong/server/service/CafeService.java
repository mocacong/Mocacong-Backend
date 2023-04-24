package mocacong.server.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
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
import mocacong.server.service.event.MemberEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CafeService {

    private final CafeRepository cafeRepository;
    private final MemberRepository memberRepository;
    private final ScoreRepository scoreRepository;
    private final ReviewRepository reviewRepository;
    private final FavoriteRepository favoriteRepository;
    private final EntityManager em;

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
        Long favoriteId = favoriteRepository.findFavoriteIdByCafeIdAndMemberId(cafe.getId(), member.getId())
                .orElse(null);
        List<CommentResponse> commentResponses = findCommentResponses(cafe, member);
        return new FindCafeResponse(
                favoriteId != null,
                favoriteId,
                cafe.findAverageScore(),
                scoreByLoginUser != null ? scoreByLoginUser.getScore() : null,
                cafeDetail.getStudyTypeValue(),
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
        em.flush();
        cafe.updateCafeDetails();

        return CafeReviewResponse.of(cafe.findAverageScore(), cafe);
    }

    private void checkAlreadySaveCafeReview(Cafe cafe, Member member) {
        reviewRepository.findIdByCafeIdAndMemberId(cafe.getId(), member.getId())
                .ifPresent(review -> {
                    throw new AlreadyExistsCafeReview();
                });
    }

    private void saveCafeDetails(CafeReviewRequest request, Cafe cafe, Member member) {
        Score score = new Score(request.getMyScore(), member, cafe);
        scoreRepository.save(score);
        CafeDetail cafeDetail = new CafeDetail(
                StudyType.from(request.getMyStudyType()),
                Wifi.from(request.getMyWifi()),
                Parking.from(request.getMyParking()),
                Toilet.from(request.getMyToilet()),
                Desk.from(request.getMyDesk()),
                Power.from(request.getMyPower()),
                Sound.from(request.getMySound())
        );
        Review review = new Review(member, cafe, cafeDetail);
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

        return CafeReviewUpdateResponse.of(cafe.findAverageScore(), cafe);
    }

    private void updateCafeReviewDetails(CafeReviewUpdateRequest request, Cafe cafe, Member member) {
        Review review = reviewRepository.findByCafeIdAndMemberId(cafe.getId(), member.getId())
                .orElseThrow(NotFoundReviewException::new);
        Score score = scoreRepository.findByCafeIdAndMemberId(cafe.getId(), member.getId())
                .orElseThrow(NotFoundReviewException::new);
        CafeDetail updatedCafeDetail = new CafeDetail(
                StudyType.from(request.getMyStudyType()),
                Wifi.from(request.getMyWifi()),
                Parking.from(request.getMyParking()),
                Toilet.from(request.getMyToilet()),
                Desk.from(request.getMyDesk()),
                Power.from(request.getMyPower()),
                Sound.from(request.getMySound())
        );

        score.updateScore(request.getMyScore());
        review.updateReview(updatedCafeDetail);
    }

    @EventListener
    public void updateReviewWhenMemberDelete(MemberEvent event) {
        Long memberId = event.getMember()
                .getId();
        reviewRepository.findAllByMemberId(memberId)
                .forEach(Review::removeMember);
        scoreRepository.findAllByMemberId(memberId)
                .forEach(Score::removeMember);
    }

    public CafeFilterResponse filterCafesByStudyType(String studyTypeValue, CafeFilterRequest requestBody) {
        List<Cafe> cafes = cafeRepository.findByStudyTypeValue(StudyType.from(studyTypeValue));
        Set<String> filteredCafeMapIds = cafes.stream()
                .map(Cafe::getMapId)
                .collect(Collectors.toSet());

        List<String> filteredIds = requestBody.getMapIds().stream()
                .filter(filteredCafeMapIds::contains)
                .collect(Collectors.toList());

        return new CafeFilterResponse(filteredIds);
    }
}
