package nhom8.example.quizz.security;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-seconds}")
    private long expirationSeconds;

    private SecretKeySpec keySpec;

    @PostConstruct
    public void init() {
        this.keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    public String generateToken(Integer userId, String role) {
        long now = Instant.now().getEpochSecond();

        String headerJson = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String payloadJson = "{\"sub\":" + userId + ",\"role\":\"" + escapeJson(role) + "\",\"iat\":" + now + ",\"exp\":" + (now + expirationSeconds) + "}";

        String headerEncoded = base64UrlEncode(headerJson);
        String payloadEncoded = base64UrlEncode(payloadJson);

        String signature = sign(headerEncoded + "." + payloadEncoded);
        return headerEncoded + "." + payloadEncoded + "." + signature;
    }

    public Map<String, Object> validateAndParseClaims(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return null;

            String headerPart = parts[0];
            String payloadPart = parts[1];
            String signaturePart = parts[2];

            String expectedSignature = sign(headerPart + "." + payloadPart);
            if (!expectedSignature.equals(signaturePart)) return null;

            String payloadJson = new String(Base64.getUrlDecoder().decode(payloadPart), StandardCharsets.UTF_8);
            long exp = extractLong(payloadJson, "exp");
            if (exp <= 0) return null;
            if (Instant.now().getEpochSecond() > exp) return null;

            long sub = extractLong(payloadJson, "sub");
            String role = extractString(payloadJson, "role");

            Map<String, Object> claims = new HashMap<>();
            claims.put("exp", exp);
            claims.put("sub", sub);
            claims.put("role", role);
            return claims;
        } catch (Exception e) {
            return null;
        }
    }

    public AuthUserContext toAuthUser(String token) {
        Map<String, Object> claims = validateAndParseClaims(token);
        if (claims == null) return null;

        Object sub = claims.get("sub");
        Object role = claims.get("role");
        if (!(sub instanceof Number) || !(role instanceof String)) return null;

        return new AuthUserContext(((Number) sub).intValue(), (String) role);
    }

    private String sign(String content) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(keySpec);
            byte[] raw = mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        } catch (Exception e) {
            throw new RuntimeException("Không tạo được chữ ký JWT", e);
        }
    }

    private String base64UrlEncode(Map<String, Object> data) {
        return base64UrlEncode(data != null ? data.toString() : "");
    }

    private String base64UrlEncode(String json) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    private static long extractLong(String json, String key) {
        Pattern p = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(\\d+)");
        Matcher m = p.matcher(json);
        if (!m.find()) return -1;
        return Long.parseLong(m.group(1));
    }

    private static String extractString(String json, String key) {
        Pattern p = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"(.*?)\"");
        Matcher m = p.matcher(json);
        if (!m.find()) return null;
        return m.group(1);
    }

    private static String escapeJson(String val) {
        if (val == null) return "";
        return val.replace("\"", "\\\"");
    }
}

