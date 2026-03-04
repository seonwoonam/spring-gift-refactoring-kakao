package gift;

import gift.auth.JwtProvider;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import static io.restassured.RestAssured.given;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

class GiftAcceptanceTest extends AcceptanceTest {

    // SQL 스크립트에 정의된 테스트 픽스처
    private static final String SENDER_EMAIL = "sender@test.com";
    private static final long TALL_OPTION_ID = 1L;
    private static final int INITIAL_STOCK = 10;
    private static final long NON_EXISTENT_OPTION_ID = 999L;

    @Autowired
    private JwtProvider jwtProvider;

    private String senderToken() {
        return jwtProvider.createToken(SENDER_EMAIL);
    }

    @Test
    @Sql(scripts = {"/sql/category-data.sql", "/sql/product-data.sql", "/sql/option-data.sql", "/sql/member-data.sql"}, executionPhase = BEFORE_TEST_METHOD)
    void 선물을_보내면_재고가_차감된다() {
        // When: 재고 10개 중 7개를 주문
        주문_보내기(senderToken(), TALL_OPTION_ID, 7).then().statusCode(201);

        // Then: 나머지 5개를 주문하면 실패해야 한다 (잔여 3개 < 요청 5개)
        주문_보내기(senderToken(), TALL_OPTION_ID, 5).then().statusCode(400);
    }

    @Test
    @Sql(scripts = {"/sql/category-data.sql", "/sql/product-data.sql", "/sql/option-data.sql", "/sql/member-data.sql"}, executionPhase = BEFORE_TEST_METHOD)
    void 재고보다_많은_수량으로_선물을_보내면_실패한다() {
        // When & Then: 재고 10개인데 11개를 주문
        주문_보내기(senderToken(), TALL_OPTION_ID, INITIAL_STOCK + 1).then().statusCode(400);
    }

    @Test
    @Sql(scripts = {"/sql/category-data.sql", "/sql/product-data.sql", "/sql/option-data.sql", "/sql/member-data.sql"}, executionPhase = BEFORE_TEST_METHOD)
    void 존재하지_않는_옵션으로_선물을_보내면_실패한다() {
        // When & Then
        주문_보내기(senderToken(), NON_EXISTENT_OPTION_ID, 1).then().statusCode(404);
    }

    @Test
    @Sql(scripts = {"/sql/category-data.sql", "/sql/product-data.sql", "/sql/option-data.sql", "/sql/member-data.sql"}, executionPhase = BEFORE_TEST_METHOD)
    void 선물을_보낸_후_동일_옵션으로_다시_보내면_누적_차감된다() {
        // When: 3개 주문 후 4개 추가 주문 (총 7개 차감)
        주문_보내기(senderToken(), TALL_OPTION_ID, 3).then().statusCode(201);
        주문_보내기(senderToken(), TALL_OPTION_ID, 4).then().statusCode(201);

        // Then: 잔여 3개이므로 4개 요청은 실패해야 한다
        주문_보내기(senderToken(), TALL_OPTION_ID, 4).then().statusCode(400);

        // 잔여 3개이므로 3개 요청은 성공해야 한다
        주문_보내기(senderToken(), TALL_OPTION_ID, 3).then().statusCode(201);
    }

    private Response 주문_보내기(String token, long optionId, int quantity) {
        String body = """
                {
                    "optionId": %d,
                    "quantity": %d,
                    "message": "테스트 선물"
                }
                """.formatted(optionId, quantity);

        return given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(body)
        .when()
                .post("/api/orders");
    }
}
