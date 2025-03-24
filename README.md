📸 QR Code Scanner App
QR Code Scanner App là ứng dụng Flutter giúp quét mã QR bằng cách tích hợp camera native (Android CameraX và iOS AVCaptureSession). Ứng dụng sử dụng MethodChannel để giao tiếp giữa Flutter và native code, đồng thời lưu lịch sử quét vào SQLite.

--------------------------------------------------------------------------------------------------------------------------------------------------

🚀 Tính năng

📷 Quét QR Code bằng camera native (Android & iOS)

📊 Hiển thị thông tin QR sau khi quét thành công

💾 Lưu lịch sử quét vào cơ sở dữ liệu SQLite

🔄 Cập nhật real-time kết quả quét bằng EventChannel

⚡ Tối ưu hiệu suất bằng Native Thread (Android) & GCD (iOS)

--------------------------------------------------------------------------------------------------------------------------------------------------

🛠️ Công nghệ sử dụng

Flutter: Framework chính

Android CameraX (Kotlin): Tích hợp camera quét QR trên Android

AVCaptureSession (Swift): Tích hợp camera quét QR trên iOS

MethodChannel & EventChannel: Giao tiếp giữa Flutter và native code

SQLite (FFI): Lưu trữ lịch sử quét QR

GCD (iOS) & Native Thread (Android): Tối ưu hiệu suất quét

--------------------------------------------------------------------------------------------------------------------------------------------------

📜 Giấy phép
Dự án này được phát hành theo giấy phép MIT License. Xem chi tiết tại LICENSE.

--------------------------------------------------------------------------------------------------------------------------------------------------

💬 Liên hệ
GitHub: @dvnthn168

Email: dvnthn168@gmail.com

🔥 Happy Coding!
