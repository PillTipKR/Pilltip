// author : mireutale
// description : 데이터 암호화/복호화 유틸리티

package com.oauth2.Util.Encryption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class EncryptionUtil {

    @Value("${encryption.secret-key:defaultSecretKeyForPillTip2024}")
    private String secretKeyString;

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;
    
    private SecretKey cachedKey = null;

    /**
     * 데이터를 암호화합니다.
     * @param plainText 암호화할 평문 데이터
     * @return Base64로 인코딩된 암호화된 데이터
     * @throws Exception 암호화 중 오류 발생 시
     */
    public String encrypt(String plainText) throws Exception {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }

        // AES 키 생성, 32바이트 키 생성
        SecretKey key = getOrCreateKey();
        
        // 랜덤 IV 생성, 12바이트 랜덤 IV 생성
        byte[] iv = generateIV();
        
        // GCM 파라미터 설정, 16 * 8 = 128비트 태그 길이 설정
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        
        // 암호화 수행, 암호화 모드 설정
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);
        
        // 암호화 수행, 평문을 바이트 배열로 변환하여 암호화
        byte[] encryptedData = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        
        // 12바이트 IV와 암호화된 데이터를 결합한 바이트 배열 생성
        byte[] combined = new byte[iv.length + encryptedData.length];

        // iv를 combined에 복사
        System.arraycopy(iv, 0, combined, 0, iv.length);

        // 암호화된 데이터를 combined에 복사
        System.arraycopy(encryptedData, 0, combined, iv.length, encryptedData.length);
        
        // Base64로 인코딩, 12바이트 IV와 암호화된 데이터를 결합한 바이트 배열을 Base64로 인코딩
        return Base64.getEncoder().encodeToString(combined);
    }

    /**
     * 암호화된 데이터를 복호화합니다.
     * @param encryptedText Base64로 인코딩된 암호화된 데이터
     * @return 복호화된 평문 데이터
     * @throws Exception 복호화 중 오류 발생 시
     */
    public String decrypt(String encryptedText) throws Exception {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }

        // Base64 디코딩
        byte[] combined = Base64.getDecoder().decode(encryptedText);
        
        // IV와 암호화된 데이터 분리
        byte[] iv = new byte[GCM_IV_LENGTH];
        byte[] encryptedData = new byte[combined.length - GCM_IV_LENGTH];
        
        System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH);
        System.arraycopy(combined, GCM_IV_LENGTH, encryptedData, 0, encryptedData.length);
        
        // AES 키 생성
        SecretKey key = getOrCreateKey();
        
        // GCM 파라미터 설정
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        
        // 복호화 수행
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);
        
        byte[] decryptedData = cipher.doFinal(encryptedData);
        return new String(decryptedData, StandardCharsets.UTF_8);
    }

    /**
     * 키를 가져오거나 생성합니다. application.properties의 secret-key를 사용합니다.
     * @return SecretKey 객체
     * @throws Exception 키 생성 중 오류 발생 시
     */
    private SecretKey getOrCreateKey() throws Exception {
        if (cachedKey != null) {
            return cachedKey;
        }
        
        // application.properties의 secret-key를 사용하여 키 생성
        cachedKey = deriveKeyFromSecretString();
        return cachedKey;
    }

    /**
     * AES 키를 생성합니다.
     * @return SecretKey 객체
     * @throws Exception 키 생성 중 오류 발생 시
     */
    private SecretKey deriveKeyFromSecretString() throws Exception {
        // 시크릿 키 문자열을 바이트 배열로 변환
        byte[] keyBytes = secretKeyString.getBytes(StandardCharsets.UTF_8);
        
        // SHA-256을 사용하여 32바이트 키 생성
        java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(keyBytes);
        
        // 32바이트로 자르기 (AES-256)
        byte[] aesKey = new byte[32];
        System.arraycopy(hash, 0, aesKey, 0, 32);
        
        return new SecretKeySpec(aesKey, "AES");
    }

    /**
     * 랜덤 IV를 생성합니다.
     * @return 12바이트 랜덤 IV
     */
    private byte[] generateIV() {
        byte[] iv = new byte[GCM_IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        return iv;
    }

    /**
     * 데이터가 암호화되어 있는지 확인합니다.
     * @param data 확인할 데이터
     * @return 암호화된 데이터이면 true, 아니면 false
     */
    public boolean isEncrypted(String data) {
        if (data == null || data.isEmpty()) {
            return false;
        }
        
        try {
            // Base64 디코딩 시도
            byte[] decoded = Base64.getDecoder().decode(data);
            // 최소 길이 확인 (IV + 최소 암호화 데이터)
            return decoded.length >= GCM_IV_LENGTH + 1;
        } catch (Exception e) {
            return false;
        }
    }

    // 암호화/복호화 유틸 단독 실행용 main 메서드
    public static void main(String[] args) throws Exception {
        EncryptionUtil util = new EncryptionUtil();
        util.secretKeyString = System.getenv().getOrDefault("ENCRYPTION_SECRET_KEY", "defaultSecretKeyForPillTip2024");
        String plain = "test_login_id";
        String encrypted = util.encrypt(plain);
        System.out.println("[EncryptionUtil] 암호화 결과: " + encrypted);
        String decrypted = util.decrypt(encrypted);
        System.out.println("[EncryptionUtil] 복호화 결과: " + decrypted);
        // DB에 저장된 암호문 복호화 테스트
        String dbEncrypted = "IAEhqQ+WVLnkr4WoXDX/7bUeB9T+ydj2+guY5CzvM3oo";
        try {
            String dbDecrypted = util.decrypt(dbEncrypted);
            System.out.println("[EncryptionUtil] DB 암호문 복호화 결과: " + dbDecrypted);
        } catch (Exception e) {
            System.out.println("[EncryptionUtil] DB 암호문 복호화 실패: " + e.getMessage());
        }
    }
} 