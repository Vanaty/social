package iranga.mg.social.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import iranga.mg.social.config.CoturnConfig;
import iranga.mg.social.dto.IceServerDto;
import iranga.mg.social.dto.WebRTCConfigDto;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class WebRTCService {

    @Autowired
    private CoturnConfig coturnConfig;

    @Value("${webrtc.ice-servers.public.google:true}")
    private boolean useGoogleStun;

    public WebRTCConfigDto getWebRTCConfiguration(String username) {
        List<IceServerDto> iceServers = new ArrayList<>();

        // Always add Google STUN servers first (primary)
        if (useGoogleStun) {
            iceServers.add(new IceServerDto(
                List.of(
                    "stun:stun.l.google.com:19302",
                    "stun:stun1.l.google.com:19302",
                    "stun:stun2.l.google.com:19302"
                ),
                null, null, null
            ));
            iceServers.add(new IceServerDto(
                List.of(
                    "stun:stun3.l.google.com:19302",
                    "stun:stun4.l.google.com:19302"
                ),
                null, null, null
            ));
            iceServers.add(new IceServerDto(
                List.of(
                    "turn:openrelay.metered.ca:80",
                    "turn:openrelay.metered.ca:443",
                    "turn:openrelay.metered.ca:443?transport=tcp"
                ),
                "openrelayproject",
                "openrelayproject",
                "password"
            ));
        }

        if (coturnConfig.isEnabled()) {
            // Add local COTURN TURN server only (no local STUN since we use Google's)
            TurnCredentials credentials = generateTurnCredentials(username);
            iceServers.add(new IceServerDto(
                List.of(
                    "turn:" + coturnConfig.getTurn().getHost() + ":" + coturnConfig.getTurn().getPort() + "?transport=udp",
                    "turn:" + coturnConfig.getTurn().getHost() + ":" + coturnConfig.getTurn().getPort() + "?transport=tcp"
                ),
                credentials.getUsername(),
                credentials.getPassword(),
                "password"
            ));
        }

        log.info("Generated WebRTC config with {} ICE servers for user: {}", iceServers.size(), username);

        return new WebRTCConfigDto(
            iceServers,
            Instant.now().getEpochSecond(),
            (int) coturnConfig.getTurn().getTtl()
        );
    }

    private TurnCredentials generateTurnCredentials(String username) {
        if (!coturnConfig.getTurn().getSecret().isEmpty()) {
            // Time-limited credentials using shared secret
            long timestamp = Instant.now().getEpochSecond() + coturnConfig.getTurn().getTtl();
            String turnUsername = timestamp + ":" + username;
            String turnPassword = generateHmacSha1(turnUsername, coturnConfig.getTurn().getSecret());
            return new TurnCredentials(turnUsername, turnPassword);
        } else {
            // Static credentials (for development)
            return new TurnCredentials(
                coturnConfig.getTurn().getUsername(),
                coturnConfig.getTurn().getPassword()
            );
        }
    }

    private String generateHmacSha1(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA1");
            mac.init(secretKeySpec);
            byte[] digest = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest);
        } catch (Exception e) {
            log.error("Error generating HMAC-SHA1: ", e);
            return "";
        }
    }

    private static class TurnCredentials {
        private final String username;
        private final String password;

        public TurnCredentials(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() { return username; }
        public String getPassword() { return password; }
    }
}
