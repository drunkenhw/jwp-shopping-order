package cart.integration;

import static org.assertj.core.api.Assertions.assertThat;

import cart.domain.CartItem;
import cart.domain.Item;
import cart.domain.Member;
import cart.domain.Product;
import cart.dto.OrderDetailResponse;
import cart.dto.OrderProductResponse;
import cart.dto.OrderRequest;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import org.springframework.http.MediaType;

public class OrderIntegrationTestFixture {

    public static void 주문_조회_응답_검증(
            ExtractableResponse<Response> 응답,
            Long 주문_id,
            int 배송비,
            int 총_가격,
            OrderProductResponse... 주문_상품_응답
    ) {
        List<OrderProductResponse> responses = Arrays.asList(주문_상품_응답);
        OrderDetailResponse response = new OrderDetailResponse(주문_id, null, null, 배송비, BigDecimal.valueOf(총_가격),
                responses);
        OrderDetailResponse actual = 응답.as(OrderDetailResponse.class);
        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringExpectedNullFields()
                .isEqualTo(response);
    }

    public static OrderProductResponse 주문_상품_응답(CartItem 장바구니) {
        Item item = 장바구니.getItem();
        Product product = item.getProduct();
        return new OrderProductResponse(
                product.getId(),
                product.getPrice().getValue(),
                product.getName(),
                product.getImageUrl(),
                장바구니.getQuantity()
        );
    }

    public static ExtractableResponse<Response> 주문_요청(Member 사용자, int 배송비, int 총_가격, Long... 장바구니_ID) {
        List<Long> ids = Arrays.asList(장바구니_ID);
        OrderRequest request = new OrderRequest(ids, 배송비, BigDecimal.valueOf(총_가격));
        return RestAssured.given()
                .auth().preemptive().basic(사용자.getEmail(), 사용자.getPassword())
                .body(request)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .post("/orders")
                .then()
                .extract();

    }

    public static ExtractableResponse<Response> 주문_상세_조회_요청(Member 사용자, Long 주문_ID) {
        return RestAssured.given()
                .auth().preemptive().basic(사용자.getEmail(), 사용자.getPassword())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/orders/{id}", 주문_ID)
                .then()
                .extract();
    }
}