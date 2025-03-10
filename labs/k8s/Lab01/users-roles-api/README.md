# Users Roles API

## Kịch bản

Xây dựng một WEB API để quản lý Users và Roles, có tên "SecuritySystem".

- Tính năng: CRUD cho Users, Roles, gán Role cho user, check role của user, Login và sinh ra JWT token.  

  Entities cho Portgresql:
  - User: Id, UserName, PasswordUserNameHash, Email, FirstName, LastName, Roles, IsActive.
  - Role: Id, RoleName, Description.  

  Entities cho Redis:
  - Auth: LoginId (hash string không trùng), UserId, Token, ExpiredDate.

  Functions:
  - User: List (hỗ trợ search, sort, filter), Create, Update, Delete, Active
  - Role: List (hỗ trợ search, sort, filter), Create, Update, Delete.
  - Auth:
    - Login sinh ra JWT token bao gồm (LoginId, format [UserId-RoleId|RoleId|RoleId|...] và base64 chuỗi này) thời hạn 1 ngày.
    - Kiểm tra Role bằng UserId.
    - Kiểm tra Token tồn tại.  

  *(Primary key sử dụng int64)*  

- Management Configuration:  

  Sử dụng Vault để tải configuration theo từng môi trường rồi load lên ứng dụng.

- Công nghệ: .Net 9, Redis storage, Postgresql, Vault by hashicorp, Clean Code và Clean Architectures.

- Áp dụng logging. (mục đích sẽ có tool thu thập log).

- Áp dụng Swaggers.

- Phân quyên trên các API.

- Đóng gói và triển khai ứng dụng vào docker, docker-compose.

- Chú ý:
  - Các thông tin của vault: VAULT_ADDRESS, VAULT_TOKEN, VAULT_PATH đưa ra biến môi trường dễ triển khai lên docker.
  - Đường dẫn log file cũng đưa ra biến môi trường.
  - Tạo dữ liệu default qua EF Migration:
    - User: Id: 1, UserName: admin, PasswordUserNameHash: (Sẽ hash với password "admin"), Email:admin@default.system, FirstName: "Admin", LastName: "Super", Roles: [1], IsActive: true.
    - Role:
      - Id: 1, RoleName: Full, Desciption: "Allow full access."
      - Id: 2, RoleName: Read User, Desciption: "Allow get list of user and get user."

## Cấu trúc dự án.

```text
SecuritySystem/
├── src/
│   ├── Domain/
│   │   ├── Entities/
│   │   │   ├── User.cs
│   │   │   ├── Role.cs
│   │   │   └── Auth.cs
│   │   └── Interfaces/
│   │       ├── IUserRepository.cs
│   │       ├── IRoleRepository.cs
│   │       └── IAuthRepository.cs
│   ├── Application/
│   │   ├── DTOs/
│   │   │   ├── UserDto.cs
│   │   │   └── RoleDto.cs
│   │   ├── Services/
│   │   │   ├── IUserService.cs
│   │   │   ├── IRoleService.cs
│   │   │   └── IAuthService.cs
│   │   └── Implementations/
│   │       ├── UserService.cs
│   │       ├── RoleService.cs
│   │       └── AuthService.cs
│   ├── Infrastructure/
│   │   ├── Data/
│   │   │   ├── Postgres/
│   │   │   │   ├── AppDbContext.cs
│   │   │   │   ├── UserRepository.cs
│   │   │   │   └── RoleRepository.cs
│   │   │   └── Redis/
│   │   │       ├── RedisService.cs
│   │   │       └── AuthRepository.cs
│   │   └── External/
│   │       └── VaultService.cs
│   ├── Presentation/
│   │   ├── Controllers/
│   │   │   ├── UsersController.cs
│   │   │   ├── RolesController.cs
│   │   │   └── AuthController.cs
│   │   └── Program.cs
├── Dockerfile
├── docker-compose.yml
└── .gitignore
```
