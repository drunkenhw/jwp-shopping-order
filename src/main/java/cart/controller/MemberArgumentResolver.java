package cart.controller;

import cart.domain.Member;
import cart.domain.repository.MemberRepository;
import cart.dto.AuthMember;
import cart.exception.AuthenticationException;
import cart.exception.ExceptionType;
import cart.exception.MemberException;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class MemberArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String BASIC_AUTH_PREFIX = "Basic ";

    private final MemberRepository memberRepository;

    public MemberArgumentResolver(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthPrincipal.class) &&
                parameter.getParameterType().equals(AuthMember.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        String authorization = webRequest.getHeader(HttpHeaders.AUTHORIZATION);
        validateAuthorizationHeader(authorization);

        String[] credentials = decode(authorization);
        String email = credentials[0];
        String password = credentials[1];

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberException(ExceptionType.INVALID_LOGIN_INFO));
        if (!member.checkPassword(password)) {
            throw new MemberException(ExceptionType.INVALID_LOGIN_INFO);
        }
        return new AuthMember(member.getId(), member.getEmail());
    }

    private void validateAuthorizationHeader(String authorization) {
        if (authorization == null) {
            throw new AuthenticationException("Authorization 헤더가 없습니다.");
        }
        if (!authorization.startsWith(BASIC_AUTH_PREFIX)) {
            throw new AuthenticationException("잘못된 인증 방식입니다.");
        }
    }

    private String[] decode(String authorization) {
        String token = authorization.substring(BASIC_AUTH_PREFIX.length());
        String decodeToken = decodeToken(token);
        return decodeToken.split(":");
    }

    private String decodeToken(String token) {
        try {
            return new String(Base64.decodeBase64(token));
        } catch (IllegalStateException e) {
            throw new AuthenticationException("토큰을 복호화 할 수 없습니다.");
        }
    }
}
