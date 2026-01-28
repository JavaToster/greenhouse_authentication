package com.example.greenhouse.util.redis;

import org.springframework.stereotype.Component;

@Component
public class RedisKeyCreator {
    private static final String CHALLENGE_PREFIX = "challenge:";
    private static final String EMAIL_VERIFY_PREFIX = "email-verify:";

    public String createChallengeKey(String challengeId){
        return CHALLENGE_PREFIX+challengeId;
    }

    public String createEmailVerifyKey(long telegramId){
        return EMAIL_VERIFY_PREFIX+telegramId;
    }
}
