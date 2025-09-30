package com.oauth2.mlInference.Service;

import com.oauth2.mlInference.Dto.PredictionResponse;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;

@Service
public class mlInferenceService {

    private final RestTemplate restTemplate = new RestTemplate();

    public PredictionResponse predict(File imageFile) {
        // 기본값은 로컬 Docker 포트로 접근. 컨테이너 간 통신 시 ML_INFERENCE_URL 환경변수로 오버라이드
        String url = System.getenv().getOrDefault("ML_INFERENCE_URL", "http://localhost:8000/predict");

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(imageFile));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity =
                new HttpEntity<>(body, headers);

        ResponseEntity<PredictionResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                PredictionResponse.class
        );

        return response.getBody();
    }

    public ResponseEntity<String> predictRaw(File imageFile) {
        String url = System.getenv().getOrDefault("ML_INFERENCE_URL", "http://localhost:8000/predict");

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(imageFile));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        return restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
    }
}
