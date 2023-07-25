package mocacong.server.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cafe_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CafeImage extends BaseTime {

    private static final int REPORT_CAFE_IMAGE_THRESHOLD_COUNT = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cafe_image_id")
    private Long id;

    @Column(name = "img_url")
    private String imgUrl;

    @Column(name = "is_used")
    private Boolean isUsed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cafe_id")
    private Cafe cafe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "cafeImage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Report> reports = new ArrayList<>();

    @Column(name = "is_masked")
    private boolean isMasked;

    public CafeImage(String imgUrl, Boolean isUsed, Cafe cafe, Member member) {
        this.imgUrl = imgUrl;
        this.isUsed = isUsed;
        this.cafe = cafe;
        this.member = member;
        this.isMasked = false;
    }

    public boolean isOwned(Member member) {
        return this.member != null && this.member.equals(member);
    }

    public void updateImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public void removeMember() {
        this.member = null;
    }

    public boolean hasAlreadyReported(Member member) {
        return this.reports.stream()
                .anyMatch(report -> report.getReporter().equals(member));
    }

    public void addReport(Report report) {
        reports.add(report);
    }

    public int getReportsCount() {
        return reports.size();
    }

    public boolean isDeletedMember() {
        return member == null;
    }

    public boolean isSavedByMember(Member member) {
        return this.member != null && this.member.equals(member);
    }

    public boolean isReportThresholdExceeded() {
        return getReportsCount() >= REPORT_CAFE_IMAGE_THRESHOLD_COUNT;
    }

    public boolean isDeletedAuthor() {
        return isDeletedMember() && isReportThresholdExceeded();
    }

    public void updateIsMasked(boolean isMasked) {
        this.isMasked = isMasked;
    }

    public void maskCafeImage() {
        this.isUsed = false;
    }
}
