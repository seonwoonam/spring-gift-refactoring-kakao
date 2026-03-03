package gift.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import gift.dto.CategoryRequest;
import gift.model.Category;
import gift.repository.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("GET /api/categories - 카테고리 목록을 조회한다")
    void getCategories() throws Exception {
        categoryRepository.save(new Category("테스트카테고리", "#FF0000", "https://example.com/img.jpg", "설명"));

        mockMvc.perform(get("/api/categories"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("POST /api/categories - 카테고리를 생성한다")
    void createCategory() throws Exception {
        var request = new CategoryRequest("신규카테고리", "#00FF00", "https://example.com/new.jpg", "새로운 카테고리");

        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("신규카테고리"))
            .andExpect(jsonPath("$.color").value("#00FF00"))
            .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("POST /api/categories - name이 빈 문자열이면 400을 반환한다")
    void createCategoryWithBlankName() throws Exception {
        var request = new CategoryRequest("", "#00FF00", "https://example.com/new.jpg", "설명");

        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/categories/{id} - 카테고리를 수정한다")
    void updateCategory() throws Exception {
        var saved = categoryRepository.save(new Category("원본", "#FF0000", "https://example.com/old.jpg", "원본 설명"));
        var request = new CategoryRequest("수정됨", "#0000FF", "https://example.com/new.jpg", "수정된 설명");

        mockMvc.perform(put("/api/categories/" + saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("수정됨"))
            .andExpect(jsonPath("$.color").value("#0000FF"));
    }

    @Test
    @DisplayName("PUT /api/categories/{id} - 존재하지 않는 카테고리를 수정하면 404를 반환한다")
    void updateNonExistentCategory() throws Exception {
        var request = new CategoryRequest("수정됨", "#0000FF", "https://example.com/new.jpg", "설명");

        mockMvc.perform(put("/api/categories/999999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/categories/{id} - 카테고리를 삭제한다")
    void deleteCategory() throws Exception {
        var saved = categoryRepository.save(new Category("삭제대상", "#FF0000", "https://example.com/img.jpg", "설명"));

        mockMvc.perform(delete("/api/categories/" + saved.getId()))
            .andExpect(status().isNoContent());
    }
}
