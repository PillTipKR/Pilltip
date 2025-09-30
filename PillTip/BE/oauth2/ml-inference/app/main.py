from fastapi import FastAPI, UploadFile, File
from ultralytics import YOLO
import numpy as np
import cv2

app = FastAPI()

# 미리 학습된 YOLOv8 모델 로드
# best.pt 파일은 Docker 빌드 시 /app/best.pt로 복사해둘 예정
model = YOLO("best.pt")

# 영어 클래스명을 한글 라벨로 매핑
KOREAN_LABELS = {
    "Epinare": "에피나레정",
    "Tiapran": "티아프란정",
    "Biometics": "비오메틱스캡슐",
    "Bacillipo": "바실리포미스캡슐",
    "Cratin_20mg": "크라틴정 20mg",
    "Cratin_10mg": "크라틴정 10mg",
    "Cratin_5mg": "크라틴정 5mg",
    "LactoNQ": "락토엔큐캡슐",
    "Anagre": "아나그레캡슐 0.5mg",
    "Mucoone": "뮤코원캡슐(에르도스테인)",
    "Eldomin": "엘도민캡슐 300mg",
    "Biumi": "비우미정 500mg",
    "Andomin300": "앤도민300프리미엄연질캡슐",
    "Eldosin": "엘도스인캡슐(에르도스테인)",
    "RanopenSemi": "라노펜세미정",
    "NewErdote": "뉴에르도테캡슐",
    "Liprega_75mg": "리프레가캡슐 75mg",
    "Ricelton_6mg": "리셀톤캡슐 6.0mg",
    "Vearotan_50mg": "베아로탄정 50mg",
    "Beatus": "베아투스정",
    "Pitarotin_2mg": "피타로틴정 2mg",
    "Lukio_10mg": "루키오정10밀리그램(몬테루카스트나트륨)",
    "Ducarb_30_5mg": "듀카브정30/5밀리그램",
    "Ducarb_30_10mg": "듀카브정30/10밀리그램",
}

@app.post("/predict")
async def predict(file: UploadFile = File(...)):
    contents = await file.read()
    nparr = np.frombuffer(contents, np.uint8)
    img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
    if img is None:
        return {"error": "Image decode failed"}

    # 추론 실행
    results = model.predict(img)
    probs = results[0].probs
    classes = model.names

    # 가장 높은 확률의 클래스
    top_idx = int(probs.top1)
    confidence = float(probs.top1conf)

    predicted_en = classes[top_idx]
    predicted_ko = KOREAN_LABELS.get(predicted_en, predicted_en)

    # Java DTO(PredictionResponse) 구조에 맞게 results 배열로 반환
    return {
        "results": [
            {
                "class": predicted_ko,
                "confidence": confidence,
            }
        ]
    }
