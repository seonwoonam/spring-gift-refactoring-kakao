package gift.dto;

import jakarta.validation.constraints.NotNull;

public record WishRequest(@NotNull Long productId) {
}
