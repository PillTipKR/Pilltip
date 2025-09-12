// author : mireutale
// description : JPA AttributeConverter를 사용한 암호화/복호화 컨버터

package com.oauth2.Util.Encryption;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Converter
@Component
public class EncryptionConverter implements AttributeConverter<String, String> {

    @Autowired
    private EncryptionUtil encryptionUtil;

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return attribute;
        }
        
        try {
            return encryptionUtil.encrypt(attribute);
        } catch (Exception e) {
            // 암호화 실패 시 원본 데이터 반환 (로그 기록)
            System.err.println("암호화 실패: " + e.getMessage());
            return attribute;
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return dbData;
        }
        
        // 이미 복호화된 데이터인지 확인
        if (!encryptionUtil.isEncrypted(dbData)) {
            return dbData;
        }
        
        try {
            return encryptionUtil.decrypt(dbData);
        } catch (Exception e) {
            // 복호화 실패 시 원본 데이터 반환 (로그 기록)
            System.err.println("복호화 실패: " + e.getMessage());
            return dbData;
        }
    }
} 