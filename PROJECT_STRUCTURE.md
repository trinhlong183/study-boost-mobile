# Study Boost Mobile - Project Structure

## Cấu trúc thư mục dự án

```
app/src/main/java/com/example/miniproject/
├── activities/          # Các Activity chính của ứng dụng
│   ├── MainActivity.java           # Activity chính - màn hình đăng nhập
│   ├── BottomTabActivity.java      # Activity quản lý bottom navigation
│   ├── RegisterActivity.java       # Activity đăng ký tài khoản
│   └── AIChatActivity.java         # Activity chat với AI (full-featured)
│
├── fragments/           # Các Fragment được sử dụng trong BottomTabActivity
│   ├── HomeFragment.java           # Fragment trang chủ
│   ├── AIFragment.java             # Fragment AI assistant (basic)
│   ├── PomodoroFragment.java       # Fragment timer Pomodoro
│   └── ProfileFragment.java        # Fragment thông tin cá nhân
│
├── adapters/            # Các Adapter cho RecyclerView
│   ├── ChatAdapter.java            # Adapter cho chat messages (basic)
│   ├── AIChatAdapter.java          # Adapter cho AI chat messages (enhanced)
│   └── ChatRoomAdapter.java        # Adapter cho danh sách chat rooms
│
├── models/              # Các model/entity classes
│   ├── Message.java                # Model cho tin nhắn chat
│   └── ChatRoom.java               # Model cho phòng chat
│
├── services/            # Các service classes xử lý business logic
│   ├── ChatService.java            # Service xử lý chat với Appwrite
│   └── HttpService.java            # Service xử lý HTTP requests tới n8n
│
├── api/                 # Các helper class cho API integration
│   └── AppwriteHelper.java         # Helper cho Appwrite SDK
│
└── utils/               # Các utility classes (empty for now)
```

## Mô tả chức năng các thư mục

### Activities
- **MainActivity**: Màn hình đăng nhập chính của ứng dụng
- **BottomTabActivity**: Quản lý navigation giữa các fragment chính
- **RegisterActivity**: Xử lý đăng ký tài khoản người dùng
- **AIChatActivity**: Chat với AI đầy đủ tính năng (sidebar, chat rooms, loading animation)

### Fragments
- **HomeFragment**: Trang chủ hiển thị thông tin tổng quan
- **AIFragment**: Chat AI cơ bản (embedded trong bottom tab)
- **PomodoroFragment**: Timer Pomodoro để quản lý thời gian học
- **ProfileFragment**: Hiển thị và chỉnh sửa thông tin cá nhân

### Adapters
- **ChatAdapter**: Adapter cơ bản cho tin nhắn chat
- **AIChatAdapter**: Adapter nâng cao với loading animation và styling
- **ChatRoomAdapter**: Adapter hiển thị danh sách phòng chat

### Models
- **Message**: Model chứa thông tin tin nhắn (id, content, user, timestamp, loading state)
- **ChatRoom**: Model chứa thông tin phòng chat (id, title, user, timestamps)

### Services
- **ChatService**: Tương tác với Appwrite database (CRUD chat rooms và messages)
- **HttpService**: Gửi requests tới n8n webhook để xử lý AI

### API
- **AppwriteHelper**: Singleton helper quản lý Appwrite client và authentication

## Luồng hoạt động chính

1. **Đăng nhập**: MainActivity → AppwriteHelper
2. **Navigation**: BottomTabActivity quản lý các Fragment
3. **AI Chat (Basic)**: AIFragment → ChatService → Appwrite
4. **AI Chat (Advanced)**: AIChatActivity → HttpService → n8n → AI Response
5. **Data Flow**: Models ↔ Services ↔ Adapters ↔ UI

## Dependencies chính

- **Appwrite SDK**: Backend-as-a-Service cho authentication và database
- **OkHttp**: HTTP client cho n8n integration
- **Gson**: JSON parsing
- **Material Design**: UI components
- **RecyclerView**: Hiển thị danh sách chat

## Lưu ý

- Cấu trúc này tuân theo kiến trúc MVC/MVP pattern
- Tách biệt rõ ràng giữa UI, Business Logic và Data Layer
- Dễ dàng mở rộng và bảo trì
- Imports đã được cập nhật theo cấu trúc mới
