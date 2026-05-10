package com.zoo.admin.service;

import com.zoo.admin.util.JwtUtil;
import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaptchaService {

    private final Map<String, CaptchaSession> sessionStore = new ConcurrentHashMap<>();
    private static final List<String> ANIMAL_IMAGES = Arrays.asList(
        "panda", "tiger", "lion", "elephant", "giraffe",
        "monkey", "bear", "deer", "fox", "rabbit",
        "koala", "dolphin", "owl", "penguin", "zebra"
    );

    @Getter
    public static class CaptchaSession {
        private final String sessionId;
        private final String targetAnimal;
        private final int targetPosition;
        private final List<String> animalOptions;
        private final long expiresAt;

        public CaptchaSession(String sessionId, String targetAnimal, int targetPosition, List<String> animalOptions) {
            this.sessionId = sessionId;
            this.targetAnimal = targetAnimal;
            this.targetPosition = targetPosition;
            this.animalOptions = animalOptions;
            this.expiresAt = System.currentTimeMillis() + 5 * 60 * 1000;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }

    public Map<String, Object> generateCaptcha() {
        String sessionId = UUID.randomUUID().toString();
        Random random = new Random();

        List<String> shuffled = new ArrayList<>(ANIMAL_IMAGES);
        Collections.shuffle(shuffled);

        int animalCount = 4;
        List<String> options = shuffled.subList(0, animalCount);
        int targetIndex = random.nextInt(animalCount);
        String targetAnimal = options.get(targetIndex);

        CaptchaSession session = new CaptchaSession(sessionId, targetAnimal, targetIndex, options);
        sessionStore.put(sessionId, session);

        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", sessionId);
        result.put("targetAnimal", targetAnimal);
        result.put("animals", options);
        result.put("expiresAt", session.getExpiresAt());

        return result;
    }

    public boolean validateCaptcha(String sessionId, int selectedIndex) {
        CaptchaSession session = sessionStore.remove(sessionId);
        if (session == null || session.isExpired()) {
            return false;
        }
        return session.getTargetPosition() == selectedIndex;
    }

    public void cleanupExpired() {
        sessionStore.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
}
