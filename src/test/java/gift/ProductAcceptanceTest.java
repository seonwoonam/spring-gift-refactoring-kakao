package gift;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

class ProductAcceptanceTest extends AcceptanceTest {

    private static final long CATEGORY_ID = 1L;
    private static final long NON_EXISTENT_CATEGORY_ID = 999L;

    @Test
    @Sql(scripts = "/sql/category-data.sql", executionPhase = BEFORE_TEST_METHOD)
    void 상품을_생성하면_목록에서_조회된다() {
        // When
        given()
                .contentType("application/json")
                .body("""
                    {"name": "아메리카노", "price": 4500, "imageUrl": "https://example.com/img.jpg", "categoryId": %d}
                    """.formatted(CATEGORY_ID))
        .when()
                .post("/api/products")
        .then()
                .statusCode(201);

        // Then
        given()
        .when()
                .get("/api/products")
        .then()
                .statusCode(200)
                .body("content", hasSize(1))
                .body("content[0].name", equalTo("아메리카노"));
    }

    @Test
    void 존재하지_않는_카테고리로_상품을_생성하면_실패한다() {
        // When & Then
        given()
                .contentType("application/json")
                .body("""
                    {"name": "아메리카노", "price": 4500, "imageUrl": "https://example.com/img.jpg", "categoryId": %d}
                    """.formatted(NON_EXISTENT_CATEGORY_ID))
        .when()
                .post("/api/products")
        .then()
                .statusCode(404);
    }
}
