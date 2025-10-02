package iranga.mg.social.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import iranga.mg.social.dto.WebRTCConfigDto;
import iranga.mg.social.service.WebRTCService;

@RestController
@RequestMapping("/api/webrtc")
@Tag(name = "WebRTC Management", description = "APIs for WebRTC configuration")
public class WebRTCController {

    @Autowired
    private WebRTCService webRTCService;

    @GetMapping("/config")
    @Operation(summary = "Get WebRTC configuration including ICE servers")
    public ResponseEntity<WebRTCConfigDto> getWebRTCConfig(@AuthenticationPrincipal UserDetails userDetails) {
        WebRTCConfigDto config = webRTCService.getWebRTCConfiguration(userDetails.getUsername());
        System.out.println("WebRTC Config: " + config);
        return ResponseEntity.ok(config);
    }
}
