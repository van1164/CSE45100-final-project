import json
from channels.generic.websocket import AsyncWebsocketConsumer

class ChatConsumer(AsyncWebsocketConsumer):
    async def connect(self):
        # 모든 클라이언트를 'chat' 그룹에 추가
        self.group_name = 'chat'
        await self.channel_layer.group_add(
            self.group_name,
            self.channel_name
        )
        await self.accept()

    async def disconnect(self, close_code):
        # 그룹에서 클라이언트를 제거
        await self.channel_layer.group_discard(
            self.group_name,
            self.channel_name
        )

    async def receive(self, text_data):
        data = json.loads(text_data)
        message = data.get('message', '')

        # 그룹에 메시지 브로드캐스트
        await self.channel_layer.group_send(
            self.group_name,
            {
                'type': 'chat_message',
                'message': message
            }
        )

    async def chat_message(self, event):
        # 그룹의 모든 클라이언트에 메시지 전송
        message = event['message']
        await self.send(text_data=json.dumps({
            'message': message
        }))