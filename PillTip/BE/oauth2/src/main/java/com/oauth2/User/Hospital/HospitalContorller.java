package com.oauth2.User.Hospital;

import com.oauth2.Account.Dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/hospital")
@RequiredArgsConstructor
public class HospitalContorller {
    private final HospitalService hospitalService;
    private final HospitalRepository hospitalRepository;
    private final HospitalAccessTokenService tokenService;

    // 병원 등록
    @PostMapping("")
    public ResponseEntity<ApiResponse<Hospital>> createHospital(@RequestBody HospitalRequest request) {
        // 동일한 이름과 주소의 병원이 이미 존재하는지 확인
        boolean existsSame = hospitalRepository.findAll().stream()
            .anyMatch(h -> h.getName().equals(request.getName()) && h.getAddress().equals(request.getAddress()));
        if (existsSame) {
            return ResponseEntity.badRequest().body(ApiResponse.error(HospitalMessageConstants.HOSPITAL_ALREADY_EXISTS, null));
        }
        // hospitalCode는 주소 기반으로 자동 생성
        String hospitalCode = hospitalService.generateHospitalCode(request.getAddress());
        if (hospitalRepository.findByHospitalCode(hospitalCode).isPresent()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(HospitalMessageConstants.HOSPITAL_CODE_ALREADY_EXISTS, null));
        }
        Hospital hospital = Hospital.builder()
                .hospitalCode(hospitalCode)
                .name(request.getName())
                .address(request.getAddress())
                .build();
        Hospital saved = hospitalRepository.save(hospital);

        tokenService.generateDailyToken(hospitalCode);

        return ResponseEntity.ok(ApiResponse.success(HospitalMessageConstants.HOSPITAL_CREATE_SUCCESS, saved));
    }

    // 병원 수정
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Hospital>> updateHospital(@PathVariable Long id, @RequestBody HospitalRequest request) {
        try {
            Hospital hospital = hospitalRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException(HospitalMessageConstants.HOSPITAL_NOT_FOUND));
            hospital.setName(request.getName());
            hospital.setAddress(request.getAddress());
            Hospital updated = hospitalRepository.save(hospital);
            tokenService.generateDailyToken(hospital.getHospitalCode());
            return ResponseEntity.ok(ApiResponse.success(HospitalMessageConstants.HOSPITAL_UPDATE_SUCCESS, updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(HospitalMessageConstants.HOSPITAL_NOT_FOUND, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(HospitalMessageConstants.HOSPITAL_UPDATE_FAILED, null));
        }
    }

    // 병원 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteHospital(@PathVariable Long id) {
        if (!hospitalRepository.existsById(id)) {
            return ResponseEntity.badRequest().body(ApiResponse.error(HospitalMessageConstants.HOSPITAL_NOT_FOUND, null));
        }
        Hospital hospital = hospitalRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(HospitalMessageConstants.HOSPITAL_NOT_FOUND));
        hospitalRepository.deleteById(id);
        tokenService.deleteTokenByHospitalCode(hospital.getHospitalCode());
        return ResponseEntity.ok(ApiResponse.success(HospitalMessageConstants.HOSPITAL_DELETE_SUCCESS, null));
    }

    // 병원 이름으로 병원 id, hospitalCode 조회
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<java.util.List<HospitalSimpleResponse>>> searchHospitalByName(@RequestParam String name) {
        java.util.List<HospitalSimpleResponse> result = hospitalRepository.findAll().stream()
            .filter(h -> h.getName().contains(name))
            .map(h -> new HospitalSimpleResponse(h.getId(), h.getHospitalCode(), h.getName(), h.getAddress()))
            .toList();
        return ResponseEntity.ok(ApiResponse.success(HospitalMessageConstants.HOSPITAL_SEARCH_SUCCESS, result));
    }
    
    // 병원별 일일 접근 토큰 조회
    @GetMapping("/{hospitalCode}/access-token")
    public ResponseEntity<ApiResponse<String>> getAccessToken(@PathVariable String hospitalCode) {
        try {
            System.out.println("=== 접근 토큰 조회 시작 ===");
            System.out.println("요청된 hospitalCode: " + hospitalCode);
            
            // 1. 병원 존재 여부 확인
            var hospitalOpt = hospitalRepository.findByHospitalCode(hospitalCode);
            if (!hospitalOpt.isPresent()) {
                System.out.println("병원을 찾을 수 없음: " + hospitalCode);
                return ResponseEntity.badRequest().body(ApiResponse.error("해당 병원을 찾을 수 없습니다.", null));
            }
            System.out.println("병원 찾음: " + hospitalOpt.get().getName());
            
            // 2. 토큰 조회 또는 생성
            var tokenOpt = tokenService.getCurrentTokenByHospitalCode(hospitalCode);
            String accessToken;
            
            if (tokenOpt.isPresent()) {
                accessToken = tokenOpt.get();
                System.out.println("기존 토큰 사용: " + accessToken.substring(0, 8) + "...");
            } else {
                System.out.println("토큰이 없어서 새로 생성");
                accessToken = tokenService.generateDailyToken(hospitalCode);
                System.out.println("새 토큰 생성: " + accessToken.substring(0, 8) + "...");
            }
            
            System.out.println("=== 접근 토큰 조회 성공 ===");
            return ResponseEntity.ok(ApiResponse.success("접근 토큰 조회 성공", accessToken));
        } catch (Exception e) {
            System.out.println("=== 접근 토큰 조회 실패 ===");
            System.out.println("에러 메시지: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(ApiResponse.error("접근 토큰 조회에 실패했습니다: " + e.getMessage(), null));
        }
    }

    // 병원 등록/수정 요청 DTO
    public static class HospitalRequest {
        private String name;
        private String address;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }
    }

    public static class HospitalSimpleResponse {
        private Long id;
        private String hospitalCode;
        private String name;
        private String address;
        public HospitalSimpleResponse(Long id, String hospitalCode, String name, String address) {
            this.id = id;
            this.hospitalCode = hospitalCode;
            this.name = name;
            this.address = address;
        }
        public Long getId() { return id; }
        public String getHospitalCode() { return hospitalCode; }
        public String getName() { return name; }
        public String getAddress() { return address; }
    }
}
