package kr.suhsaechan.mapsee.common.util;

import kr.suhsaechan.mapsee.common.exception.CustomException;
import kr.suhsaechan.mapsee.common.exception.constant.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CommonUtil {

    /**
     * 보안 문자열 마스킹 처리
     * 문자열이 null이거나 길이가 8 미만인 경우 전체를 마스킹 처리합니다.
     * 그렇지 않은 경우 앞 4글자만 노출하고 나머지는 마스킹 처리합니다.
     *
     * @param secureString 마스킹할 보안 문자열
     * @return 마스킹 처리된 문자열
     */
    public String maskSecureString(String secureString) {
        if (secureString == null || secureString.length() < 8) {
            return "****";
        }
        return secureString.substring(0, 4) + "****";
    }

    /**
     * URL 길이 검증
     * URL이 지정된 최대 길이를 초과하는 경우 예외를 발생시킵니다.
     *
     * @param url 검증할 URL
     * @param maxLength 허용 최대 길이
     * @throws CustomException URL이 최대 길이를 초과하는 경우
     */
    public void validateUrlLength(String url, int maxLength) {
        if (url != null && url.length() > maxLength) {
            log.error("URL length exceeds maximum allowed length. URL length: {}, Max: {}",
                url.length(), maxLength);
            throw new CustomException(ErrorCode.URL_TOO_LONG);
        }
    }
}
