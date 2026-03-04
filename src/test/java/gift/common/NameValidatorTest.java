package gift.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NameValidatorTest {

    @Nested
    @DisplayName("상품 이름 검증 (maxLength=15, noKakao 포함)")
    class ProductName {

        private final NameValidator validator = NameValidator.of("Product name",
            NameValidator.maxLength(15),
            NameValidator.allowedCharacters(),
            NameValidator.noKakao()
        );

        @ParameterizedTest
        @ValueSource(strings = {"상품", "Product", "상품123", "상품 (A)", "테스트[1]", "A+B", "A-B", "A&B", "A/B", "A_B"})
        @DisplayName("허용된 문자로 구성된 이름은 에러가 없다")
        void validNames(String name) {
            List<String> errors = validator.validate(name);

            assertThat(errors).isEmpty();
        }

        @Test
        @DisplayName("최대 15자까지 허용된다")
        void exactlyMaxLength() {
            String name = "가나다라마바사아자차카타파";  // 13자
            List<String> errors = validator.validate(name);

            assertThat(errors).isEmpty();
        }

        @Test
        @DisplayName("공백을 포함한 15자 이름도 허용된다")
        void maxLengthWithSpaces() {
            String name = "가 나 다 라 마 바 사아"; // 15자 (공백 포함)
            List<String> errors = validator.validate(name);

            assertThat(errors).isEmpty();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("null이나 빈 문자열이면 에러가 발생한다")
        void nullOrEmpty(String name) {
            List<String> errors = validator.validate(name);

            assertThat(errors).isNotEmpty();
            assertThat(errors.get(0)).contains("required");
        }

        @Test
        @DisplayName("공백만으로 구성된 이름이면 에러가 발생한다")
        void blankName() {
            List<String> errors = validator.validate("   ");

            assertThat(errors).isNotEmpty();
            assertThat(errors.get(0)).contains("required");
        }

        @Test
        @DisplayName("16자 이상이면 길이 초과 에러가 발생한다")
        void exceedsMaxLength() {
            String name = "가나다라마바사아자차카타파하거너"; // 16자
            List<String> errors = validator.validate(name);

            assertThat(errors).anyMatch(e -> e.contains("15 characters"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"상품!!", "상품@", "상품#", "상품$", "상품%", "상품^"})
        @DisplayName("허용되지 않는 특수 문자가 포함되면 에러가 발생한다")
        void invalidSpecialChars(String name) {
            List<String> errors = validator.validate(name);

            assertThat(errors).anyMatch(e -> e.contains("invalid special characters"));
        }

        @Test
        @DisplayName("카카오가 포함된 이름은 noKakao 규칙이 있으면 에러가 발생한다")
        void kakaoNotAllowedWithNoKakaoRule() {
            List<String> errors = validator.validate("카카오선물");

            assertThat(errors).anyMatch(e -> e.contains("카카오"));
        }

        @Test
        @DisplayName("카카오가 포함된 이름은 noKakao 규칙이 없으면 에러가 없다")
        void kakaoAllowedWithoutNoKakaoRule() {
            NameValidator withoutKakao = NameValidator.of("Product name",
                NameValidator.maxLength(15),
                NameValidator.allowedCharacters()
            );
            List<String> errors = withoutKakao.validate("카카오선물");

            assertThat(errors).isEmpty();
        }

        @Test
        @DisplayName("길이 초과와 허용되지 않는 문자를 동시에 검증한다")
        void lengthAndCharacterErrors() {
            String name = "abcdefghijklmnop!"; // 17자 + 특수문자
            List<String> errors = validator.validate(name);

            assertThat(errors).hasSizeGreaterThanOrEqualTo(2);
        }
    }

    @Nested
    @DisplayName("옵션 이름 검증 (maxLength=50, noKakao 미포함)")
    class OptionName {

        private final NameValidator validator = NameValidator.of("Option name",
            NameValidator.maxLength(50),
            NameValidator.allowedCharacters()
        );

        @ParameterizedTest
        @ValueSource(strings = {"옵션A", "Option 1", "블루 / 256GB", "사이즈(L)", "색상[레드]", "A+B", "A-B", "A&B", "A_B"})
        @DisplayName("허용된 문자로 구성된 이름은 에러가 없다")
        void validNames(String name) {
            List<String> errors = validator.validate(name);

            assertThat(errors).isEmpty();
        }

        @Test
        @DisplayName("최대 50자까지 허용된다")
        void exactlyMaxLength() {
            String name = "a".repeat(50);
            List<String> errors = validator.validate(name);

            assertThat(errors).isEmpty();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("null이나 빈 문자열이면 에러가 발생한다")
        void nullOrEmpty(String name) {
            List<String> errors = validator.validate(name);

            assertThat(errors).isNotEmpty();
            assertThat(errors.get(0)).contains("required");
        }

        @Test
        @DisplayName("공백만으로 구성된 이름이면 에러가 발생한다")
        void blankName() {
            List<String> errors = validator.validate("   ");

            assertThat(errors).isNotEmpty();
            assertThat(errors.get(0)).contains("required");
        }

        @Test
        @DisplayName("51자 이상이면 길이 초과 에러가 발생한다")
        void exceedsMaxLength() {
            String name = "a".repeat(51);
            List<String> errors = validator.validate(name);

            assertThat(errors).anyMatch(e -> e.contains("50 characters"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"옵션!", "옵션@", "옵션#", "옵션$", "옵션%"})
        @DisplayName("허용되지 않는 특수 문자가 포함되면 에러가 발생한다")
        void invalidSpecialChars(String name) {
            List<String> errors = validator.validate(name);

            assertThat(errors).anyMatch(e -> e.contains("invalid special characters"));
        }

        @Test
        @DisplayName("길이 초과와 허용되지 않는 문자를 동시에 검증한다")
        void lengthAndCharacterErrors() {
            String name = "!".repeat(51);
            List<String> errors = validator.validate(name);

            assertThat(errors).hasSizeGreaterThanOrEqualTo(2);
        }
    }
}
