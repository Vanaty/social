package iranga.mg.social.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IceServerDto {
    private List<String> urls;
    private String username;
    private String credential;
    private String credentialType = "password";
}
