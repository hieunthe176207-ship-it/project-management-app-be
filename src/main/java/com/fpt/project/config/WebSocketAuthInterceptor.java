package com.fpt.project.config;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Interceptor cho kênh inbound của STOMP:
 * - KHÔNG chặn HTTP handshake.
 * - Khi client gửi frame STOMP CONNECT, đọc header "Authorization: Bearer <jwt>".
 * - Decode JWT -> tạo Authentication -> set vào StompHeaderAccessor (Principal).
 * - Nhờ đó dùng được /user/queue/... và kiểm soát quyền trong @MessageMapping nếu cần.
 */
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtDecoder jwtDecoder;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor acc = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(acc.getCommand())) {
            // Header "Authorization" trong STOMP CONNECT (native header)
            String auth = acc.getFirstNativeHeader("Authorization");
            if (auth != null && auth.startsWith("Bearer ")) {
                String token = auth.substring(7).trim();
                try {
                    Jwt jwt = jwtDecoder.decode(token);

                    // Tên người dùng: thường để sub/email
                    String username = jwt.getSubject();

                    // Lấy roles/authorities nếu bạn có claim (ví dụ "roles": ["USER","ADMIN"])
                    Collection<SimpleGrantedAuthority> authorities = extractAuthorities(jwt);

                    Authentication authentication =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);

                    // Gắn Principal vào phiên STOMP
                    acc.setUser(authentication);

                } catch (Exception e) {
                    // Token sai/hết hạn -> có 2 lựa chọn:
                    // 1) Ném exception để server đóng kết nối:
                    // throw new IllegalArgumentException("Invalid JWT in STOMP CONNECT", e);

                    // 2) Cho phép anonymous (không set user) -> tuỳ nhu cầu:
                    // (ở đây mình chọn KHÔNG ném để tránh cắt kết nối ngay,
                    //  bạn có thể đổi sang throw nếu muốn bắt buộc xác thực)
                }
            } else {
                // Không có Authorization -> cho phép anonymous (tuỳ nhu cầu)
                // Nếu muốn bắt buộc có token, hãy throw ở đây.
            }
        }
        return message;
    }

    /**
     * Chuyển các claim roles/authorities (nếu có) thành SimpleGrantedAuthority.
     * Tuỳ theo payload JWT của bạn mà đổi key/format cho phù hợp.
     */
    @SuppressWarnings("unchecked")
    private Collection<SimpleGrantedAuthority> extractAuthorities(Jwt jwt) {
        // Ví dụ các khả năng phổ biến:
        Object rolesObj =
                jwt.getClaims().get("roles") != null ? jwt.getClaims().get("roles") :
                        jwt.getClaims().get("authorities");

        if (rolesObj instanceof List<?> list) {
            return (Collection) list.stream()
                    .map(String::valueOf)
                    .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }
        // Không có roles -> trả về rỗng
        return List.of();
    }
}
