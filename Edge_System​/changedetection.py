import os
import cv2
import pathlib
import requests
from datetime import datetime
import json
from websocket import create_connection
class ChangeDetection:
    result_prev = []
    HOST = "https://van133.pythonanywhere.com/"
    username = 'van133'
    password = 'vanvan12'
    token = 'b37773d5ad2d82b029b1a2b21d4b92d70ce1eb6b'
    title = 'test'
    text = 'test'
    detected_objects = [] 
    ws = None  # 웹소켓 객체


    def __init__(self, names):
        self.result_prev = [0 for _ in range(len(names))]
        # res = requests.post(
        #     self.HOST + '/api-token-auth/',
        #     {'username': self.username, 'password': self.password}
        # )
        # res.raise_for_status()
        # self.token = res.json()['token']  # 토큰 저장
        print(self.token)

                # 웹소켓 연결
        try:
            self.ws = create_connection("ws://https://van133.pythonanywhere.com/ws/chat/")
            print("WebSocket connection established")
        except Exception as e:
            print(f"WebSocket connection failed: {e}")

    def add(self, names, detected_current, save_dir, image):
        self.title = ''
        self.text = ''
        change_flag = 0  # 변화 감지 플래그

        for i in range(len(self.result_prev)):
            if self.result_prev[i] == 0 and detected_current[i] == 1:
                change_flag = 1
                self.title = names[i]
                self.text += names[i] + ", "

        self.result_prev = detected_current[:]  # 객체 검출 상태 저장

        if change_flag == 1:
            self.send(save_dir, image)

    def send(self, save_dir, image):
        now = datetime.now()
        today = datetime.now()
        save_path = pathlib.Path(
            os.getcwd()
        ) / save_dir / 'detected' / str(today.year) / str(today.month) / str(today.day)
        save_path.mkdir(parents=True, exist_ok=True)

        full_path = save_path / '{0}-{1}-{2}-{3}.jpg'.format(
            today.hour, today.minute, today.second, today.microsecond
        )

        dst = cv2.resize(image, dsize=(320, 240), interpolation=cv2.INTER_AREA)
        cv2.imwrite(str(full_path), dst)

        # 인증이 필요한 요청에 아래의 headers를 붙임
        headers = {'Authorization': 'Token ' + self.token, 'Accept': 'application/json'}

        # Post Create
        data = {
            'title': self.title,
            'text': self.text + "발견된 객체수 : "+str(len(self.detected_objects)) + " 위치 : "+str(json.dumps(self.detected_objects)),
            'created_date': now.isoformat(),
            'published_date': now.isoformat(),
        }
        files = {'image': open(full_path, 'rb')}
        print(data)
        res = requests.post(self.HOST + '/api_root/Post/', data=data, files=files, headers=headers)
        print(res)

        self.websocket_send()



    def websocket_send(self):
                # 웹소켓으로 메시지 전송
        if self.ws:
            try:
                ws_message = {f"message":"앞에 "+str(self.title)+"이 있습니다."}
                self.ws.send(json.dumps(ws_message))
                print("Message sent via WebSocket:", ws_message)
            except Exception as e:
                print(f"WebSocket send failed: {e}")


    def update_detected_objects(self, names, detections, image_shape):
    
        self.detected_objects = []  # 감지된 객체 초기화

        for det in detections:
            x1, y1, x2, y2, conf, cls_idx = det[:6]  # 바운딩 박스 좌표와 클래스/확신도
            x1, y1, x2, y2 = map(int, [x1, y1, x2, y2])  # 좌표를 정수로 변환
            obj_class = names[int(cls_idx)]  # 클래스 이름
            self.detected_objects.append({
                "class": obj_class,
                "confidence": float(conf),
                "coordinates": {
                    "x1": x1, "y1": y1, "x2": x2, "y2": y2
                },
            })
