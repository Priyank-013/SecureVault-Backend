package com.example.securevault.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    StringRedisTemplate redisTemplate;

    @Mock
    ValueOperations<String, String> valueOps;

    @Mock
    EmailService emailService;

    @InjectMocks
    OtpService otpService;

    private static final String EMAIL = "test@gmail.com";

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @Test
    void generateAndSend_savesOtpInRedisAndSendsMail() {
        otpService.generateAndSend(EMAIL);
    
        ArgumentCaptor<String> savedOtp = ArgumentCaptor.forClass(String.class);
        verify(valueOps).set(eq("otp:" + EMAIL), savedOtp.capture(), eq(5L), eq(TimeUnit.MINUTES));

        assertTrue(savedOtp.getValue().matches("\\d{6}"), "OTP should be exactly 6 digits");
        verify(emailService).sendOtp(EMAIL, savedOtp.getValue());
    }

    @Test
    void verify_correctOtp_passesAndDeletesKeys() {
        when(valueOps.get("otp:" + EMAIL)).thenReturn("123456");
        when(valueOps.increment("otp_attempts:" + EMAIL)).thenReturn(1L);

        assertDoesNotThrow(() -> otpService.verify(EMAIL, "123456"));

        verify(redisTemplate).delete("otp:" + EMAIL);
        verify(redisTemplate).delete("otp_attempts:" + EMAIL);
    }

    @Test
    void verify_expiredOtp_throws() {
        when(valueOps.get("otp:" + EMAIL)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> otpService.verify(EMAIL, "123456"));

        assertTrue(ex.getMessage().contains("expired"));
    }

    @Test
    void verify_wrongOtp_throwsWithRemainingAttempts() {
        when(valueOps.get("otp:" + EMAIL)).thenReturn("123456");
        when(valueOps.increment("otp_attempts:" + EMAIL)).thenReturn(1L);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> otpService.verify(EMAIL, "000000"));

        assertEquals("Invalid OTP. 4 attempts remaining.", ex.getMessage());
        verify(redisTemplate, never()).delete("otp:" + EMAIL);
    }

    @Test
    void verify_tooManyAttempts_locksOtp() {
        when(valueOps.get("otp:" + EMAIL)).thenReturn("123456");
        when(valueOps.increment("otp_attempts:" + EMAIL)).thenReturn(6L);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> otpService.verify(EMAIL, "123456"));

        assertTrue(ex.getMessage().contains("locked"));
        verify(redisTemplate).delete("otp:" + EMAIL);
    }
}