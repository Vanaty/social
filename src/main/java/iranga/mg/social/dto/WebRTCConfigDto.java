package iranga.mg.social.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WebRTCConfigDto {
    private List<IceServerDto> iceServers;
    private long timestamp;
    private int ttl;
}
