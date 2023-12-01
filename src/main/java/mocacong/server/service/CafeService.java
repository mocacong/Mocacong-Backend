package mocacong.server.service;

import lombok.RequiredArgsConstructor;
import mocacong.server.domain.*;
import mocacong.server.domain.cafedetail.*;
import mocacong.server.dto.request.*;
import mocacong.server.dto.response.*;
import mocacong.server.exception.badrequest.AlreadyExistsCafeReview;
import mocacong.server.exception.badrequest.DuplicateCafeException;
import mocacong.server.exception.badrequest.ExceedCageImagesTotalCountsException;
import mocacong.server.exception.notfound.NotFoundCafeException;
import mocacong.server.exception.notfound.NotFoundCafeImageException;
import mocacong.server.exception.notfound.NotFoundMemberException;
import mocacong.server.exception.notfound.NotFoundReviewException;
import mocacong.server.repository.*;
import mocacong.server.service.event.DeleteMemberEvent;
import mocacong.server.service.event.DeleteNotUsedImagesEvent;
import mocacong.server.support.AwsS3Uploader;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CafeService {

    private static final int CAFE_IMAGES_PER_MEMBER_LIMIT_COUNTS = 3;
    private static final int CAFE_SHOW_PAGE_COMMENTS_LIMIT_COUNTS = 3;
    private final CafeRepository cafeRepository;
    private final MemberRepository memberRepository;
    private final ScoreRepository scoreRepository;
    private final CommentRepository commentRepository;
    private final ReviewRepository reviewRepository;
    private final FavoriteRepository favoriteRepository;
    private final CafeImageRepository cafeImageRepository;
    private final EntityManager em;
    private final AwsS3Uploader awsS3Uploader;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public void save(CafeRegisterRequest request) {
        Cafe cafe = new Cafe(request.getId(), request.getName());

        try {
            cafeRepository.save(cafe);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateCafeException();
        }
    }

    @Transactional(readOnly = true)
    public FindCafeResponse findCafeByMapId(Long memberId, String mapId) {
        Cafe cafe = cafeRepository.findByMapId(mapId)
                .orElseThrow(NotFoundCafeException::new);
        CafeDetail cafeDetail = cafe.getCafeDetail();
        Member member = memberRepository.findById(memberId)
                .orElseThrow(NotFoundMemberException::new);
        Score scoreByLoginUser = scoreRepository.findByCafeIdAndMemberId(cafe.getId(), member.getId())
                .orElse(null);
        Long favoriteId = favoriteRepository.findFavoriteIdByCafeIdAndMemberId(cafe.getId(), member.getId())
                .orElse(null);
        List<CommentResponse> commentResponses = findCommentResponses(cafe, member);
        List<CafeImageResponse> cafeImageResponses = findCafeImageResponses(cafe, member);
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
                cafe.getComments().size(),
                commentResponses,
                cafeImageResponses
        );
    }

    @Cacheable(key = "#mapId", value = "cafePreviewCache", cacheManager = "cafeCacheManager")
    @Transactional(readOnly = true)
    public PreviewCafeResponse previewCafeByMapId(Long memberId, String mapId) {
        Cafe cafe = cafeRepository.findByMapId(mapId)
                .orElseThrow(NotFoundCafeException::new);
        CafeDetail cafeDetail = cafe.getCafeDetail();
        Member member = memberRepository.findById(memberId)
                .orElseThrow(NotFoundMemberException::new);
        Long favoriteId = favoriteRepository.findFavoriteIdByCafeIdAndMemberId(cafe.getId(), member.getId())
                .orElse(null);
        return new PreviewCafeResponse(
                favoriteId != null,
                cafe.findAverageScore(),
                cafeDetail.getStudyTypeValue(),
                cafe.getReviews().size()
        );
    }

    private List<CommentResponse> findCommentResponses(Cafe cafe, Member member) {
        return cafe.getComments()
                .stream()
                .limit(CAFE_SHOW_PAGE_COMMENTS_LIMIT_COUNTS)
                .map(comment -> {
                    if (comment.isWrittenByMember(member)) {
                        return new CommentResponse(comment.getId(), member.getImgUrl(), member.getNickname(),
                                comment.getContent(), comment.getLikeCounts(), true);
                    } else {
                        return new CommentResponse(comment.getId(), comment.getWriterImgUrl(),
                                comment.getWriterNickname(), comment.getContent(), comment.getLikeCounts(), false);
                    }
                })
                .collect(Collectors.toList());
    }

    private List<CafeImageResponse> findCafeImageResponses(Cafe cafe, Member member) {
        Pageable pageable = PageRequest.of(0, 5);
        Slice<CafeImage> cafeImages = cafeImageRepository.
                findAllByCafeIdAndIsUsedOrderByCafeImageId(cafe.getId(), member.getId(), pageable);

        return cafeImages
                .getContent()
                .stream()
                .map(cafeImage -> {
                    Boolean isMe = cafeImage.isOwned(member);
                    return new CafeImageResponse(cafeImage.getId(), cafeImage.getImgUrl(), isMe);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MyFavoriteCafesResponse findMyFavoriteCafes(Long memberId, Integer page, int count) {
        Slice<Cafe> myFavoriteCafes = cafeRepository.findByMyFavoriteCafes(memberId, PageRequest.of(page, count));
        List<MyFavoriteCafeResponse> responses = myFavoriteCafes
                .getContent()
                .stream()
                .map(cafe -> new MyFavoriteCafeResponse(cafe.getMapId(), cafe.getName(), cafe.getStudyType(), cafe.findAverageScore()))
                .collect(Collectors.toList());
        return new MyFavoriteCafesResponse(myFavoriteCafes.isLast(), responses);
    }

    @Transactional(readOnly = true)
    public MyReviewCafesResponse findMyReviewCafes(Long memberId, Integer page, int count) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(NotFoundMemberException::new);

        Slice<MyReviewCafeResponse> myReviewCafes =
                cafeRepository.findMyReviewCafesById(member.getId(), PageRequest.of(page, count));
        List<MyReviewCafeResponse> responses = myReviewCafes.getContent();
        return new MyReviewCafesResponse(myReviewCafes.isLast(), responses);
    }

    @Transactional(readOnly = true)
    public MyCommentCafesResponse findMyCommentCafes(Long memberId, int page, int count) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(NotFoundMemberException::new);
        Slice<Comment> comments = commentRepository.findByMemberId(member.getId(), PageRequest.of(page, count));

        List<MyCommentCafeResponse> responses = comments.stream()
                .map(comment -> new MyCommentCafeResponse(
                        comment.getCafe().getMapId(),
                        comment.getCafe().getName(),
                        comment.getCafe().getStudyType(),
                        comment.getContent()
                ))
                .collect(Collectors.toList());
        return new MyCommentCafesResponse(comments.isLast(), responses);
    }

    @CacheEvict(key = "#mapId", value = "cafePreviewCache")
    @Transactional
    public CafeReviewResponse saveCafeReview(Long memberId, String mapId, CafeReviewRequest request) {
        Cafe cafe = cafeRepository.findByMapId(mapId)
                .orElseThrow(NotFoundCafeException::new);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(NotFoundMemberException::new);
        checkAlreadySaveCafeReview(cafe, member);

        saveCafeDetails(request, cafe, member);
        em.flush();
        cafe.updateCafeDetails();

        return CafeReviewResponse.of(cafe.findAverageScore(), cafe, member);
    }

    private void checkAlreadySaveCafeReview(Cafe cafe, Member member) {
        reviewRepository.findIdByCafeIdAndMemberId(cafe.getId(), member.getId())
                .ifPresent(review -> {
                    throw new AlreadyExistsCafeReview();
                });
    }

    private void saveCafeDetails(CafeReviewRequest request, Cafe cafe, Member member) {
        Score score = new Score(request.getMyScore(), member, cafe);
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
        try {
            scoreRepository.save(score);
            reviewRepository.save(review);
        } catch (DataIntegrityViolationException e) {
            throw new AlreadyExistsCafeReview();
        }
    }

    @Transactional(readOnly = true)
    public CafeMyReviewResponse findMyCafeReview(Long memberId, String mapId) {
        Cafe cafe = cafeRepository.findByMapId(mapId)
                .orElseThrow(NotFoundCafeException::new);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(NotFoundMemberException::new);

        Review review = reviewRepository.findByCafeIdAndMemberId(cafe.getId(), member.getId())
                .orElse(null);
        Score score = scoreRepository.findByCafeIdAndMemberId(cafe.getId(), member.getId())
                .orElse(null);
        return CafeMyReviewResponse.of(score, review);
    }

    @CacheEvict(key = "#mapId", value = "cafePreviewCache")
    @Transactional
    public CafeReviewUpdateResponse updateCafeReview(Long memberId, String mapId, CafeReviewUpdateRequest request) {
        Cafe cafe = cafeRepository.findByMapId(mapId)
                .orElseThrow(NotFoundCafeException::new);
        Member member = memberRepository.findById(memberId)
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
    public void updateReviewWhenMemberDelete(DeleteMemberEvent event) {
        Long memberId = event.getMember()
                .getId();
        reviewRepository.findAllByMemberId(memberId)
                .forEach(Review::removeMember);
        scoreRepository.findAllByMemberId(memberId)
                .forEach(Score::removeMember);
    }

    public CafeFilterStudyTypeResponse filterCafesByStudyType(String studyTypeValue,
                                                              CafeFilterStudyTypeRequest request) {
        List<Cafe> cafes = cafeRepository.findByStudyTypeValue(StudyType.from(studyTypeValue));
        cafes.addAll(cafeRepository.findByStudyTypeValue(StudyType.BOTH));
        Set<String> filteredCafeMapIds = cafes.stream()
                .map(Cafe::getMapId)
                .collect(Collectors.toSet());

        List<String> filteredIds = request.getMapIds().stream()
                .filter(filteredCafeMapIds::contains)
                .collect(Collectors.toList());

        return new CafeFilterStudyTypeResponse(filteredIds);
    }

    public CafeFilterFavoritesResponse filterCafesByFavorites(Long memberId, CafeFilterFavoritesRequest request) {
        List<String> mapIds = request.getMapIds();
        List<String> filteredIds = cafeRepository.findNearCafeMapIdsByMyFavoriteCafes(memberId, mapIds);

        return new CafeFilterFavoritesResponse(filteredIds);
    }

    @Transactional
    public CafeImagesSaveResponse saveCafeImage(Long memberId, String mapId, List<MultipartFile> cafeImages) {
        Cafe cafe = cafeRepository.findByMapId(mapId)
                .orElseThrow(NotFoundCafeException::new);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(NotFoundMemberException::new);

        validateOwnedCafeImagesCounts(cafe, member, cafeImages);
        List<CafeImageSaveResponse> cafeImageSaveResponses = new ArrayList<>();
        for (MultipartFile cafeImage : cafeImages) {
            String imgUrl = awsS3Uploader.uploadImage(cafeImage);
            CafeImage uploadedCafeImage = new CafeImage(imgUrl, true, cafe, member);
            cafeImageSaveResponses.add(new CafeImageSaveResponse(cafeImageRepository.save(uploadedCafeImage).getId()));
        }
        return new CafeImagesSaveResponse(cafeImageSaveResponses);
    }

    private void validateOwnedCafeImagesCounts(Cafe cafe, Member member, List<MultipartFile> requestCafeImages) {
        List<CafeImage> currentOwnedCafeImages = cafe.getCafeImages()
                .stream()
                .filter(cafeImage -> cafeImage.isOwned(member))
                .collect(Collectors.toList());
        if (currentOwnedCafeImages.size() + requestCafeImages.size() > CAFE_IMAGES_PER_MEMBER_LIMIT_COUNTS) {
            throw new ExceedCageImagesTotalCountsException();
        }
    }

    @Transactional(readOnly = true)
    public CafeImagesResponse findCafeImages(Long memberId, String mapId, Integer page, int count) {
        Cafe cafe = cafeRepository.findByMapId(mapId)
                .orElseThrow(NotFoundCafeException::new);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(NotFoundMemberException::new);
        Pageable pageable = PageRequest.of(page, count);
        Slice<CafeImage> cafeImages = cafeImageRepository.
                findAllByCafeIdAndIsUsedOrderByCafeImageId(cafe.getId(), member.getId(), pageable);

        List<CafeImageResponse> responses = cafeImages
                .getContent()
                .stream()
                .map(cafeImage -> {
                    Boolean isMe = cafeImage.isOwned(member);
                    return new CafeImageResponse(cafeImage.getId(), cafeImage.getImgUrl(), isMe);
                })
                .collect(Collectors.toList());

        return new CafeImagesResponse(cafeImages.isLast(), responses);
    }

    @Transactional
    public void updateCafeImage(Long memberId, String mapId, Long cafeImageId, MultipartFile cafeImg) {
        Cafe cafe = cafeRepository.findByMapId(mapId)
                .orElseThrow(NotFoundCafeException::new);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(NotFoundMemberException::new);
        CafeImage cafeImage = cafeImageRepository.findById(cafeImageId)
                .orElseThrow(NotFoundCafeImageException::new);
        String beforeImgUrl = cafeImage.getImgUrl();

        String newImgUrl = awsS3Uploader.uploadImage(cafeImg);
        cafeImage.updateImgUrl(newImgUrl);

        CafeImage notUseImage = new CafeImage(beforeImgUrl, false, cafe, member);
        cafeImageRepository.save(notUseImage);
    }

    @EventListener
    public void updateCafeImagesWhenMemberDelete(DeleteMemberEvent event) {
        Member member = event.getMember();
        cafeImageRepository.findAllByMemberId(member.getId())
                .forEach(CafeImage::removeMember);
    }

    @Transactional
    public void deleteNotUsedCafeImages() {
        List<CafeImage> cafeImages = cafeImageRepository.findAllByIsUsedFalseAndIsMaskedFalse();
        List<String> imgUrls = cafeImages.stream()
                .map(CafeImage::getImgUrl)
                .collect(Collectors.toList());
        applicationEventPublisher.publishEvent(new DeleteNotUsedImagesEvent(imgUrls));

        List<Long> ids = cafeImages.stream()
                .map(CafeImage::getId)
                .collect(Collectors.toList());
        cafeImageRepository.deleteAllByIdInBatch(ids);
    }
}
