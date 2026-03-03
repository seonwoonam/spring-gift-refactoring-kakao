package gift.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import gift.auth.JwtProvider;
import gift.dto.WishRequest;
import gift.model.Category;
import gift.model.Member;
import gift.model.Product;
import gift.model.Wish;
import gift.repository.CategoryRepository;
import gift.repository.MemberRepository;
import gift.repository.ProductRepository;
import gift.repository.WishRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class WishControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private WishRepository wishRepository;

    private Member member;
    private String token;
    private Product product;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(new Member("wish-test@example.com", "password"));
        token = jwtProvider.createToken(member.getEmail());

        var category = categoryRepository.save(new Category("테스트", "#FF0000", "https://example.com/img.jpg", "설명"));
        product = productRepository.save(new Product("위시상품", 10000, "https://example.com/img.jpg", category));
    }

    @Test
    @DisplayName("POST /api/wishes - 위시리스트에 상품을 추가한다")
    void addWish() throws Exception {
        var request = new WishRequest(product.getId());

        mockMvc.perform(post("/api/wishes")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.productId").value(product.getId()))
            .andExpect(jsonPath("$.name").value("위시상품"));
    }

    @Test
    @DisplayName("POST /api/wishes - 이미 추가된 상품을 다시 추가하면 기존 항목을 반환한다")
    void addDuplicateWish() throws Exception {
        wishRepository.save(new Wish(member.getId(), product));
        var request = new WishRequest(product.getId());

        mockMvc.perform(post("/api/wishes")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.productId").value(product.getId()));
    }

    @Test
    @DisplayName("POST /api/wishes - 존재하지 않는 상품을 추가하면 404를 반환한다")
    void addWishForNonExistentProduct() throws Exception {
        var request = new WishRequest(999999L);

        mockMvc.perform(post("/api/wishes")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/wishes - 인증 없이 추가하면 401을 반환한다")
    void addWishWithoutAuth() throws Exception {
        var request = new WishRequest(product.getId());

        mockMvc.perform(post("/api/wishes")
                .header("Authorization", "Bearer invalid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/wishes - 내 위시리스트를 조회한다")
    void getWishes() throws Exception {
        wishRepository.save(new Wish(member.getId(), product));

        mockMvc.perform(get("/api/wishes")
                .header("Authorization", "Bearer " + token)
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].productId").value(product.getId()));
    }

    @Test
    @DisplayName("GET /api/wishes - 인증 없이 조회하면 401을 반환한다")
    void getWishesWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/wishes")
                .header("Authorization", "Bearer invalid-token"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /api/wishes/{id} - 내 위시를 삭제한다")
    void removeWish() throws Exception {
        var wish = wishRepository.save(new Wish(member.getId(), product));

        mockMvc.perform(delete("/api/wishes/" + wish.getId())
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/wishes/{id} - 다른 사람의 위시를 삭제하면 403을 반환한다")
    void removeOtherMembersWish() throws Exception {
        var otherMember = memberRepository.save(new Member("other@example.com", "password"));
        var otherWish = wishRepository.save(new Wish(otherMember.getId(), product));

        mockMvc.perform(delete("/api/wishes/" + otherWish.getId())
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/wishes/{id} - 존재하지 않는 위시를 삭제하면 404를 반환한다")
    void removeNonExistentWish() throws Exception {
        mockMvc.perform(delete("/api/wishes/999999")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/wishes/{id} - 인증 없이 삭제하면 401을 반환한다")
    void removeWishWithoutAuth() throws Exception {
        var wish = wishRepository.save(new Wish(member.getId(), product));

        mockMvc.perform(delete("/api/wishes/" + wish.getId())
                .header("Authorization", "Bearer invalid-token"))
            .andExpect(status().isUnauthorized());
    }
}
