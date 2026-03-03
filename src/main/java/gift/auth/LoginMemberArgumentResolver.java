package gift.auth;

import gift.exception.UnauthorizedException;
import gift.model.Member;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {
    private final AuthenticationResolver authenticationResolver;

    public LoginMemberArgumentResolver(AuthenticationResolver authenticationResolver) {
        this.authenticationResolver = authenticationResolver;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginMember.class)
            && Member.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(
        MethodParameter parameter,
        ModelAndViewContainer mavContainer,
        NativeWebRequest webRequest,
        WebDataBinderFactory binderFactory
    ) {
        String authorization = webRequest.getHeader("Authorization");
        if (authorization == null) {
            throw new UnauthorizedException("Authorization header is required.");
        }

        Member member = authenticationResolver.extractMember(authorization);
        if (member == null) {
            throw new UnauthorizedException("Invalid or expired token.");
        }

        return member;
    }
}
