from fastapi import FastAPI
from fastapi.responses import FileResponse, JSONResponse
from fastapi.middleware.cors import CORSMiddleware
import os
import uvicorn
import socket

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"], 
    allow_credentials=True,
    allow_methods=["*"],   
    allow_headers=["*"],
)

# Cấu hình CORS để Android App kết nối không bị chặn
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Đường dẫn gốc tới thư mục runs
BASE_RUNS_DIR = r"D:\LUAN_VAN\MaHoa\runs"

@app.get("/images/{stage}/{image_name}")
def get_game_image(stage: str, image_name: str):
    image_path = os.path.join(BASE_RUNS_DIR, stage, "game_images", image_name)
    
    if os.path.exists(image_path) and os.path.isfile(image_path):
        return FileResponse(image_path)
    
    return JSONResponse(
        status_code=404, 
        content={"error": f"Không tìm thấy ảnh {image_name} tại {image_path}"}
    )

if __name__ == "__main__":
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80)) 
        current_ip = s.getsockname()[0]
        s.close()
    except Exception:
        current_ip = "0.0.0.0" 

    print("==================================================")
    print(f"IMAGE SERVER ĐANG CHẠY TRÊN IP: http://{current_ip}:8005")
    print("==================================================")

    uvicorn.run(app, host=current_ip, port=8005)