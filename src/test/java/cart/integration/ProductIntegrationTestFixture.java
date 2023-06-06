package cart.integration;

import cart.dto.ProductRequest;
import cart.dto.ProductResponse;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
public class ProductIntegrationTestFixture {

    public static ExtractableResponse<Response> 상품_등록을_요청한다(String 상품명, int 금액, String 이미지_주소) {
        ProductRequest request = new ProductRequest(상품명, 금액, 이미지_주소);
        return RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .post("/products")
                .then().log().all()
                .extract();
    }

    public static ExtractableResponse<Response> 상품_전체_조회를_요청한다() {
        return RestAssured.given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/products")
                .then()
                .extract();
    }

    public static ExtractableResponse<Response> 상품_조회를_요청한다(Long 상품_ID) {
        return RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/products/{id}", 상품_ID)
                .then().log().all()
                .extract();
    }

    public static ExtractableResponse<Response> 상품_수정을_요청한다(Long 상품_ID, String 수정할_상품명, int 수정할_금액, String 수정할_이미지_주소) {
        ProductRequest request = new ProductRequest(수정할_상품명, 수정할_금액, 수정할_이미지_주소);
        return RestAssured.given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .put("/products/{id}", 상품_ID)
                .then()
                .extract();
    }

    public static ExtractableResponse<Response> 상품_삭제를_요청한다(Long 상품_ID) {
        return RestAssured.given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .delete("/products/{id}", 상품_ID)
                .then()
                .extract();
    }

    public static void 상품_조회_응답을_검증한다(ExtractableResponse<Response> 응답, ProductResponse product) {
        ProductResponse response = 응답.as(ProductResponse.class);
        assertThat(response).usingRecursiveComparison()
                .isEqualTo(product);
    }

    public static ProductResponse 상품_정보를_반환한다(Long 상품_ID, String 상품명, int 금액, String 이미지_주소) {
        return new ProductResponse(상품_ID, 상품명, 금액, 이미지_주소);
    }

    public static void 상품_전체_조회_응답을_검증한다(ExtractableResponse<Response> 응답, ProductResponse... products) {
        List<ProductResponse> responses = Arrays.asList(products);
        assertThat(응답.jsonPath().getList(".", ProductResponse.class))
                .usingRecursiveComparison()
                .isEqualTo(responses);
    }

}
