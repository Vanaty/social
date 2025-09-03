package iranga.mg.social.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String username;
    private Long userId;
    private String tokenType = "Bearer";
    private Long expiresIn;

    public AuthResponse(String token) {
        this.token = token;
    }
}
