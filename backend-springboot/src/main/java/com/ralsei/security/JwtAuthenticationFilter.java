package com.ralsei.security;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ralsei.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        String username = null;

        try {
            username = jwtService.extractUsername(jwt);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // Bắt lỗi hết hạn: Ném thẳng mã 401 về cho Frontend Axios xử lý Refresh Token
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Access Token da het han!\"}");
            return; 
        } catch (Exception e) {
            // Bắt các trường hợp token bị xào nấu, sai chữ ký...
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Token khong hop le!\"}");
            return;
        }

        // Nếu có username và chưa được cấu hình Authentication trong Context
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            if (!jwtService.isTokenExpired(jwt)) {
                
                // Trích xuất danh sách roles trực tiếp từ ruột Token, không gọi DB!
                List<String> roles = jwtService.extractRoles(jwt);
                
                // Chuyển đổi chuỗi String thành cấu trúc GrantedAuthority của Spring Security
                List<SimpleGrantedAuthority> authorities = roles.stream()
                // Dấu :: là tham chiếu nha mấy má. anh linh nói tìm hiểu đoạn lab211 r=))) 
                        .map(role -> {
                            // Kiểm tra nếu chưa có tiền tố ROLE_ thì thêm vào
                            String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                            return new SimpleGrantedAuthority(roleWithPrefix);
                        })
                        .collect(Collectors.toList());

                // Tạo đối tượng Authentication với Principal đơn giản (chỉ cần username) và quyền hạn thật từ Token
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        username, null, authorities);
                        
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // Đút vào Context, mở khóa cho API
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        // ko comment thằng nào đọc cái này chắc tẩu hỏa nhập ma=)))))))
        filterChain.doFilter(request, response);
    }
    // địt mẹ nếu mình chơi C# thì đéo khổ như này r=)) con mẹ nó 1 đấm còn cái loz này làm kinh bỏ mẹ=))))))
}