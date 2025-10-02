# COTURN Setup for Social App

## Overview

This configuration uses:
- **Google STUN servers** for NAT traversal (primary)
- **Local COTURN server** for TURN relay (fallback)

## Docker Setup

### 1. Start Services
```bash
docker-compose up -d
```

### 2. Verify COTURN
```bash
# Check if COTURN is running
docker logs coturn

# Test STUN server
stun-client stun://localhost:3478
```

## Configuration Details

### STUN Servers (Google)
- `stun:stun.l.google.com:19302`
- `stun:stun1.l.google.com:19302`
- `stun:stun2.l.google.com:19302`

### TURN Server (Local COTURN)
- **Host**: localhost:3478
- **Username**: socialapp
- **Password**: socialapp123
- **Transport**: UDP/TCP

## WebRTC Integration

### Client-side Usage
```javascript
const config = await fetch('/api/webrtc/config');
const peerConnection = new RTCPeerConnection(config);
```

### Server-side Endpoint
```
GET /api/webrtc/config
Authorization: Bearer <jwt-token>
```

Returns:
```json
{
  "iceServers": [
    {
      "urls": ["stun:stun.l.google.com:19302"],
      "username": null,
      "credential": null
    },
    {
      "urls": [
        "turn:localhost:3478?transport=udp",
        "turn:localhost:3478?transport=tcp"
      ],
      "username": "socialapp",
      "credential": "socialapp123"
    }
  ],
  "timestamp": 1234567890,
  "ttl": 3600
}
```

## Production Notes

1. **Security**: Update COTURN credentials
2. **Networking**: Configure external IP for COTURN
3. **TLS**: Enable TLS/DTLS for production
4. **Firewall**: Open ports 3478 and 49160-49200

## Troubleshooting

### Common Issues
1. **COTURN not accessible**: Check Docker ports mapping
2. **WebRTC fails**: Verify ICE servers in browser console
3. **Media not flowing**: Check firewall/NAT settings

### Debug Commands
```bash
# Check COTURN logs
docker logs coturn -f

# Test TURN server
turnutils_uclient -t -T -u socialapp -w socialapp123 localhost
```
