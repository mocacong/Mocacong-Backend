package mocacong.server.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mocacong.server.exception.badrequest.InvalidReportReasonException;

import java.util.Arrays;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public enum ReportReason {

    FISHING_HARASSMENT_SPAM("fishing_harassment_spam"),
    LEAKING_FRAUD("leaking_fraud"),
    PORNOGRAPHY("pornography"),
    INAPPROPRIATE_CONTENT("inappropriate_content"),
    INSULT("insult"),
    COMMERCIAL_AD("commercial_ad"),
    POLITICAL_CONTENT("political_content");

    private String value;

    public static ReportReason from(String value) {
        return Arrays.stream(values())
                .filter(it -> Objects.equals(it.value, value))
                .findFirst()
                .orElseThrow(InvalidReportReasonException::new);
    }
}
