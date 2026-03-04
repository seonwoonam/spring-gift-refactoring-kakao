package gift.service;

import gift.client.KakaoMessageClient;
import gift.model.Member;
import gift.model.Option;
import gift.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class KakaoNotificationService {
    private static final Logger log = LoggerFactory.getLogger(KakaoNotificationService.class);
    private final KakaoMessageClient kakaoMessageClient;

    public KakaoNotificationService(KakaoMessageClient kakaoMessageClient) {
        this.kakaoMessageClient = kakaoMessageClient;
    }

    public void sendOrderNotification(Member member, Order order, Option option) {
        if (member.getKakaoAccessToken() == null) {
            return;
        }
        try {
            var product = option.getProduct();
            kakaoMessageClient.sendToMe(member.getKakaoAccessToken(), order, product);
        } catch (Exception e) {
            log.warn("Failed to send Kakao notification for order {}. message={}", order.getId(), e.getMessage());
        }
    }
}
