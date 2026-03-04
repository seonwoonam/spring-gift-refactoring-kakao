package gift.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import gift.dto.OptionRequest;
import gift.model.Category;
import gift.model.Option;
import gift.model.Product;
import gift.repository.CategoryRepository;
import gift.repository.OptionRepository;
import gift.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private OptionRepository optionRepository;

    private Product product;

    @BeforeEach
    void setUp() {
        var category = categoryRepository.save(new Category("전자기기", "#1E90FF", "https://example.com/img.jpg", "설명"));
        product = productRepository.save(new Product("테스트상품", 10000, "https://example.com/img.jpg", category));
    }

    @Test
    @DisplayName("GET /api/products/{productId}/options - 옵션 목록을 조회한다")
    void getOptions() throws Exception {
        optionRepository.save(new Option(product, "옵션A", 10));
        optionRepository.save(new Option(product, "옵션B", 20));

        mockMvc.perform(get("/api/products/" + product.getId() + "/options"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("GET /api/products/{productId}/options - 존재하지 않는 상품의 옵션을 조회하면 404를 반환한다")
    void getOptionsForNonExistentProduct() throws Exception {
        mockMvc.perform(get("/api/products/999999/options"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/products/{productId}/options - 옵션을 생성한다")
    void createOption() throws Exception {
        var request = new OptionRequest("새옵션", 100);

        mockMvc.perform(post("/api/products/" + product.getId() + "/options")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("새옵션"))
            .andExpect(jsonPath("$.quantity").value(100));
    }

    @Test
    @DisplayName("POST /api/products/{productId}/options - 중복된 옵션명으로 생성하면 400을 반환한다")
    void createDuplicateOption() throws Exception {
        optionRepository.save(new Option(product, "기존옵션", 10));
        var request = new OptionRequest("기존옵션", 20);

        mockMvc.perform(post("/api/products/" + product.getId() + "/options")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/products/{productId}/options - 허용되지 않는 문자가 포함된 옵션명이면 400을 반환한다")
    void createOptionWithInvalidName() throws Exception {
        var request = new OptionRequest("옵션!@#", 10);

        mockMvc.perform(post("/api/products/" + product.getId() + "/options")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/products/{productId}/options - 50자를 초과하는 옵션명이면 400을 반환한다")
    void createOptionWithLongName() throws Exception {
        var request = new OptionRequest("a".repeat(51), 10);

        mockMvc.perform(post("/api/products/" + product.getId() + "/options")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/products/{productId}/options - 존재하지 않는 상품에 옵션을 생성하면 404를 반환한다")
    void createOptionForNonExistentProduct() throws Exception {
        var request = new OptionRequest("옵션", 10);

        mockMvc.perform(post("/api/products/999999/options")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/products/{productId}/options/{optionId} - 옵션이 2개 이상이면 삭제할 수 있다")
    void deleteOption() throws Exception {
        var option1 = optionRepository.save(new Option(product, "옵션A", 10));
        optionRepository.save(new Option(product, "옵션B", 20));

        mockMvc.perform(delete("/api/products/" + product.getId() + "/options/" + option1.getId()))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/products/{productId}/options/{optionId} - 옵션이 1개뿐이면 삭제할 수 없다")
    void deleteLastOption() throws Exception {
        var option = optionRepository.save(new Option(product, "유일한옵션", 10));

        mockMvc.perform(delete("/api/products/" + product.getId() + "/options/" + option.getId()))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/products/{productId}/options/{optionId} - 다른 상품의 옵션을 삭제하면 404를 반환한다")
    void deleteOptionFromDifferentProduct() throws Exception {
        var category = categoryRepository.save(new Category("다른카테고리", "#FF0000", "https://example.com/img.jpg", "설명"));
        var otherProduct = productRepository.save(new Product("다른상품", 5000, "https://example.com/img.jpg", category));
        var otherOption = optionRepository.save(new Option(otherProduct, "다른옵션", 10));

        // product에는 최소 2개 옵션이 있어야 삭제 시도가 가능
        optionRepository.save(new Option(product, "옵션A", 10));
        optionRepository.save(new Option(product, "옵션B", 20));

        mockMvc.perform(delete("/api/products/" + product.getId() + "/options/" + otherOption.getId()))
            .andExpect(status().isNotFound());
    }
}
