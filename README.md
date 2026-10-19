# TeamPulse

面向课程小组项目的**进度风险预警与贡献分析**系统。

当前处于地基阶段：仓库、数据库脚本、后端公共层（统一返回体 / 错误码 / 全局异常）、持久层骨架（MyBatis-Plus + 11 表实体）、安全与认证（白名单 + JWT 认证过滤器 + 注册/登录/me 最小闭环 + 401/403 统一返回体）、跨域配置与前端骨架已就绪，业务接口与页面尚未开始，详见「[当前进度 / 待办](#当前进度--待办)」。

## 技术栈

- 后端：Spring Boot 3.2.5 / Java 21 / Maven 3.9.x
- 持久层：MyBatis-Plus 3.5.7 / MySQL Connector-J 8.3.0
- 安全：Spring Security 6.2.4 / jjwt 0.12.5
- 数据库：MySQL 8.4.x LTS（utf8mb4 / utf8mb4_0900_ai_ci）
- 前端：Vue 3.4.27 / Vue Router 4.3.2 / Pinia 2.1.7
- 请求与图表：Axios 1.7.2 / ECharts 5.5.0
- 构建：Vite 5.2.11 / @vitejs/plugin-vue 5.0.4

分页实现已确定为 MyBatis-Plus（`IPage` + `PageAdapter` 转换为 `PageResult`，见「接口约定」）。前端 UI 组件库尚未选型（`package.json` 目前不含 element-plus），确定后回写本节。

## 环境要求

| 组件 | 版本 / 要求 | 验证命令 |
| --- | --- | --- |
| JDK | 21（LTS） | `java -version` |
| Maven | 3.9.x（≥ 3.6.3） | `mvn -v` |
| Node.js / npm | 24 LTS / 11.x | `node -v`、`npm -v` |
| MySQL | 8.4.x LTS | `mysql --version` |
| 工具 | IDEA 2023.3+ 或 VS Code、DBeaver、Chrome / Edge | — |

JDK 21 由 `backend/pom.xml` 的 `java.version` 锁定；IDEA 需开启 Lombok 注解处理，否则实体类 setter/getter 会报红。

端口：后端 `8080`（context-path `/api`）、前端 `5173`（`/api` 代理到 8080）、MySQL `3306`。

国内网络首次拉取依赖较慢时，先按「依赖安装加速（国内镜像源）」配置镜像源。

## 快速开始

命令均在仓库根 `TeamPulse/` 执行；Windows 建议用 Git Bash（PowerShell 不支持 `<` 重定向）。国内网络建议先按「依赖安装加速（国内镜像源）」配置镜像，再执行 `mvn` / `npm install`。

### 1. 数据库初始化

脚本在 `backend/src/main/resources/db/`：`schema.sql` 含建库 + 11 张表 + 1 个视图，`seed.sql` 为种子数据。权限划分是**段0 用 root**、**段1–段5 用业务账号 `teampulse`**；推荐在 DBeaver 里分段执行。

```bash
# 1) 建库（root）
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS teampulse DEFAULT CHARACTER SET utf8mb4 DEFAULT COLLATE utf8mb4_0900_ai_ci;"

# 2) 建表 + 视图（root 整份执行；库已存在时段0 自动跳过）
mysql -u root -p --default-character-set=utf8mb4 < backend/src/main/resources/db/schema.sql

# 3) 种子数据（业务账号）
mysql -u teampulse -p --default-character-set=utf8mb4 teampulse < backend/src/main/resources/db/seed.sql

# 4) 校验：应为 11 表 + 1 视图、角色 3 条
mysql -u teampulse -p -D teampulse -e "SHOW FULL TABLES; SELECT COUNT(*) FROM sys_role;"
```

- 脚本**可重跑**，会先 `DROP` 再重建，**仅限本地开发库**；结构口径以脚本为准：11 张表 + 1 个视图 + **24 个非主键索引**（7 唯一 + 17 普通）。
- 仓库不存放任何数据库口令；`application-dev.yml` 的数据源由各人本地填写，命令一律用 `-p` 交互输入。

### 2. 后端启动

```bash
cd backend
mvn spring-boot:run            # 开发期直接运行
# 或：mvn clean package -DskipTests && java -jar target/TeamPulse-0.0.1-SNAPSHOT.jar
```

- 首次运行前：在仓库根执行 `cp backend/src/main/resources/application-dev.example.yml backend/src/main/resources/application-dev.yml`，把其中的 `CHANGE_ME` 换成本机数据库口令（该文件不入库）。
- 成功标志：日志出现 `Tomcat started on port 8080 (http) with context path '/api'`；接口基址 `http://localhost:8080/api`。
- `SecurityConfig` 已就绪：`/auth/login`、`/auth/register`、`/doc.html`、`/v3/api-docs/**` 等在白名单内，其余请求需认证。JWT 过滤器（`JwtAuthenticationFilter`）已接入：先 `POST /auth/login` 拿 token，再带 `Authorization: Bearer <token>` 访问受保护路径；无 / 非法 / 过期 token 一律返回 401（统一返回体）。

### 3. 前端启动

```bash
cd frontend
npm install
npm run dev
```

- 打开 http://localhost:5173；`vite.config.js` 已把 `/api` 代理到 `http://localhost:8080`，`VITE_API_BASE_URL=/api`。
- **页面空白属正常**：`router/index.js` 的 `routes` 仍是空数组，登录页尚未实现。
- `npm run build` 产物在 `frontend/dist/`；首次 `npm install` 会生成 `package-lock.json`，建议提交。

### 4. 管理员账号

`seed.sql` 会创建管理员账号 `admin` 并绑定 `ADMIN` 角色。**脚本中的密码是 BCrypt 占位符，直接执行无法登录**，仓库也不分发任何可用口令；需要时按 `schema.sql` 附录 A 在本地生成哈希并 `UPDATE sys_user`，明文只在自己终端输入。

> 这是对《前置任务》3.3「初始密码」一项的有意偏离：仓库不留存可用口令。

## 依赖安装加速（国内镜像源）

国内网络直连 Maven Central 与 npm 官方源常见超时、中断或速度极慢。本节只调整**依赖下载地址**，不改变版本解析——版本仍由 `backend/pom.xml`、`frontend/package.json` 与 `package-lock.json` 锁定。

### 适用范围

| 场景 | 包管理器 | 默认源 | 本节覆盖 |
| --- | --- | --- | --- |
| 后端 `mvn` 拉取 Spring Boot / MyBatis-Plus / Knife4j / jjwt | Maven | Maven Central | 是 |
| 前端 `npm install` 拉取 Vue / Vite / ECharts | npm | registry.npmjs.org | 是 |
| JDK / MySQL / IDEA / DBeaver 安装包 | — | 官方下载站 | 否（非包管理器，可自行用国内镜像站下载安装包） |
| `docs/*.py` 按需安装的 Python 包（非构建必需） | pip | PyPI | 可选（见文末附注） |

镜像配置属于**本机用户级配置**，不进仓库：`~/.m2/settings.xml`、`~/.npmrc` 均不要提交。若团队需统一 npm 源，可在 `frontend/.npmrc` 写入 `registry=...` 并提交（不含任何凭据）。

### 一、Maven（后端）

修改用户级 `~/.m2/settings.xml`（Windows：`C:\Users\<用户名>\.m2\settings.xml`；不存在则新建）：

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0">
  <mirrors>
    <mirror>
      <id>aliyunmaven</id>
      <name>阿里云公共仓库</name>
      <url>https://maven.aliyun.com/repository/public</url>
      <mirrorOf>central</mirrorOf>
    </mirror>
  </mirrors>
</settings>
```

- `mirrorOf` 建议用 `central`（只镜像中央仓库，最稳妥）。写成 `*` 会镜像所有仓库；若团队有私有仓库，须用 `*,!private-repo` 排除，否则私有依赖会 404。
- IDEA 用户：`Settings → Build, Execution, Deployment → Build Tools → Maven → User settings file` 指向同一份 `settings.xml`，否则可能仍走自带 Maven 的全局配置。
- 验证：`mvn help:effective-settings` 能看到该 mirror；或 `mvn -X` 日志出现 `Downloading from aliyunmaven: https://maven.aliyun.com/...`。
- 切换后仍慢：先 `mvn -U` 强制刷新；必要时删除 `~/.m2/repository` 下对应依赖目录重下。

备选镜像（只改 `<url>`）：

| 镜像 | URL |
| --- | --- |
| 阿里云 | `https://maven.aliyun.com/repository/public` |
| 华为云 | `https://repo.huaweicloud.com/repository/maven/` |
| 腾讯云 | `https://mirrors.tencent.com/nexus/repository/maven-public/` |

### 二、npm（前端）

推荐淘宝 npmmirror。注意：**旧域名 `registry.npm.taobao.org` 已停用**（证书过期），不要再用。

```bash
npm config set registry https://registry.npmmirror.com/
npm config get registry          # 应输出 https://registry.npmmirror.com/
```

- 该命令写入用户级 `~/.npmrc`，对所有项目生效；只对本项目生效可在 `frontend/.npmrc` 写 `registry=https://registry.npmmirror.com/`（可提交）。
- 临时用一次、不改全局：`npm install --registry=https://registry.npmmirror.com/`。
- **lock 文件提示**：`package-lock.json` 的 `resolved` 记录生成时的下载地址。若切换镜像后安装仍走旧地址或变慢，可删除 `node_modules` 后重新 `npm install`，或该次安装临时加 `--registry`。`npm ci` 严格按 lock 安装，适合 CI。
- pnpm / yarn 若使用：`pnpm config set registry ...`；Yarn Classic `yarn config set registry ...`，Yarn Berry 写 `.yarnrc.yml` 的 `npmRegistryServer`。

备选镜像：

| 镜像 | URL |
| --- | --- |
| npmmirror（淘宝） | `https://registry.npmmirror.com/` |
| 阿里云 | `https://npm.aliyun.com/` |
| 腾讯云 | `https://mirrors.cloud.tencent.com/npm/` |
| 华为云 | `https://repo.huaweicloud.com/repository/npm/` |
| 清华 TUNA | `https://mirrors.tuna.tsinghua.edu.cn/npm/` |

### 三、镜像失效或不可信时

1. **切换**：改 `<url>`（Maven）或 `npm config set registry <另一个>`（npm），优先选上表中的机构镜像，不要用来源不明的私有镜像。
2. **回退官方源**：Maven 注释或删除 `<mirror>` 块（也可用 `mvn -s <另一份 settings.xml>` 临时切换）；npm 执行 `npm config set registry https://registry.npmjs.org/` 或 `npm config delete registry`。
3. **单个依赖 404 / 同步滞后**：镜像同步有延迟（各源约 10–30 分钟）。先换镜像；仍缺失时，npm 可对该次安装指定官方源，Maven 可临时注释 mirror 或为该依赖单独加 `<repository>`。
4. **完整性校验**：npm 依据 `package-lock.json` 的 `integrity`（sha512）校验，建议提交 lock；Maven 默认不强制校验和，若出现 `Checksum validation failed`，删除对应本地目录重下，**不要**为绕过而关闭校验。
5. **配置不生效**：确认改的是用户级 `~/.m2/settings.xml`（不是 `$MAVEN_HOME/conf/settings.xml`）；IDEA 是否指向同一份；`npm config get registry` 是否被项目级 `frontend/.npmrc` 覆盖（项目级优先）。

> 附注（可选）：`docs/*.py` 如需安装 Python 包，可执行 `pip config set global.index-url https://pypi.tuna.tsinghua.edu.cn/simple`。本项目构建不依赖 Python 包，跳过不影响后端与前端启动。

## 团队与分支

| 模块 | 负责人 | 负责表 / 视图 | 分支 |
| --- | --- | --- | --- |
| 用户与权限 | A | `sys_user`、`sys_role`、`sys_user_role` | `feature-a-foundation` |
| 课程 / 项目 / 小组 | B | `course`、`project`、`project_group`、`group_member` | `feature-b-group` |
| 任务与看板 | C | `task`、`task_assignment`、`task_dependency`、`task_status_log` | `feature-c-task` |
| 总览 + 测试 + 部署 | D | `v_project_progress`（视图） | `feature-d-report` |

A 额外负责 PR 合并与契约冻结；D 额外负责测试与部署。

```
main        ← 只接受来自 dev 的 PR
dev         ← 每日集成分支，全员合并目标
├── feature-a-foundation   A：框架 + 用户权限
├── feature-b-group        B：课程 / 项目 / 小组
├── feature-c-task         C：任务与看板
└── feature-d-report       D：总览 + 测试 + 部署
```

- **每日合并窗口**：待确认后回填（由 A 统一处理 PR）。
- 提交信息：`type(scope): 简述`，`type` 取 `feat` / `fix` / `docs` / `refactor` / `test` / `chore`；PR 标题 `[模块] 说明`，至少 1 人 Review。
- 禁止直接向 `main` 推送；`main` 只接受来自 `dev` 的 PR。

## 接口约定

本节只冻结**协议**，接口清单见《前置任务》7.2 与后续 Apifox 项目。

- 基址 `http://localhost:8080/api`，前端统一通过 `/api` 代理访问。
- 认证：请求头 `Authorization: Bearer <token>`，有效期由 `team-pulse.jwt.expire-minutes` 控制（当前 720 分钟）。
- 统一返回体（成功示例，`data` 可为 null）：

```json
{
  "code": 200,
  "message": "success",
  "data": null,
  "timestamp": 1760000000000
}
```

- 四个字段固定存在；`data` 为 null 时**仍输出该键**，`timestamp` 为 epoch 毫秒；`data` 内部为 null 的字段受全局 `non_null` 策略影响不会输出。
- 业务判断只看 `code === 200`，非 200 按失败处理，提示取 `message`。

错误码号段：

| 号段 | 归属 | 已定义示例 | HTTP 状态 |
| --- | --- | --- | --- |
| 200 / 400 / 401 / 403 / 404 / 500 | 通用 | 成功、参数错、未登录、无权限、不存在、服务端错 | 与 code 同值 |
| 1001–1099 | A 用户与权限 | 1001 用户名已存在、1002 密码错误 | 1001 → 409，1002 → 400 |
| 1101–1199 | B 课程 / 项目 / 小组 | 1101 小组名称重复 | 409 |
| 1201–1299 | C 任务管理 | 1201 非法状态流转、1202 前置任务未完成 | 409 |
| 1301–1399 | D 总览统计 | 1301 项目不存在 | 404 |
| 其他 1xxx | — | — | 400 |

> `1002 密码错误` 映射 400 而不是 401：401 专留给令牌缺失 / 过期 / 非法，避免登录页输错密码被误判为登录态失效。

分页与其他约定：

- 入参 `pageNum`（默认 1）、`pageSize`（默认 10，上限 100）；出参 `{ pageNum, pageSize, total, list }`，空页 `list` 为 `[]`；字段名冻结，不用 `current` / `size` / `records`。**分页实现：MyBatis-Plus IPage + PageAdapter 转换，出参保持 PageResult 冻结字段**。
- 时间格式 `yyyy-MM-dd HH:mm:ss`，时区 `Asia/Shanghai`。
- 任务状态 `NOT_STARTED` / `IN_PROGRESS` / `REVIEWING` / `DONE` / `PAUSED` / `CANCELLED`；角色 `STUDENT` / `ASSISTANT` / `ADMIN`。
- **未认证 401 / 无权限 403 也走统一返回体**：这两类由 Security 过滤器链直接输出（`RestAuthenticationEntryPoint` / `RestAccessDeniedHandler`），不经过 `GlobalExceptionHandler`，响应体同样是 `{ code, message, data, timestamp }`，`data` 为 `null`。

## 当前进度 / 待办

当前可运行范围：后端能启动、前端能打开、数据库脚本能建出完整结构；**业务接口与页面尚未开始**。下面按《前置任务》9.x 的客观检查项自评，「未达成」只表示尚未开始。5 分钟验收路径：按「快速开始」跑完 —— 后端日志出现 Tomcat 8080、前端能打开 5173、`SHOW FULL TABLES` 为 11 表 + 1 视图、浏览器或 Apifox 访问 `http://localhost:8080/api/auth/me` 返回 401 统一返回体，即地基通过。

**已完成**：仓库与分支（`main` / `dev` / 4 条 `feature-*`）；数据库脚本（11 表 + 1 视图 + 24 个非主键索引）；后端公共层（`Result`、`ErrorCode` 12 个常量、`PageResult`、`BusinessException`、`GlobalExceptionHandler`）；持久层（MyBatis-Plus 3.5.7：分页插件、逻辑删除、`createTime` / `updateTime` 自动填充，11 表 Entity / Mapper / Service 骨架）；安全与认证（`SecurityConfig` 白名单 + `STATELESS` + 401/403 统一返回体；`JwtUtil` + `JwtAuthenticationFilter` + `UserDetailsServiceImpl` + `AuthController` 注册/登录/me 最小闭环）；跨域（`CorsConfig` 按 `team-pulse.cors.allowed-origins` 放行 5173）；接口文档（Knife4j 4.5.0 + springdoc，`Knife4jConfig` 四分组：认证与用户 / 课程项目小组 / 任务与看板 / 总览统计，访问 `http://localhost:8080/api/doc.html`）；后端测试（`GlobalExceptionHandlerTest` 9 用例 + `JwtUtilTest` 3 用例，standalone MockMvc 不连库）；前端骨架（Vue / Router / Pinia / Axios 已挂载，`request.js` 自动注入 `Bearer` token）。

自测基线（Apifox「开发环境」，前置 URL `http://localhost:8080/api`，请求路径不再带 `/api`）：`POST /auth/register` → **200**；`POST /auth/login` → **200** 返回 `data.token`；`GET /auth/me`（带 Token）→ **200**；`GET /auth/me`（无 Token）→ **401** `code=401`；`OPTIONS /auth/me`（带 `Origin: http://localhost:5173`）→ **200** 且含 `Access-Control-Allow-Origin`。

**进行中**：后端业务层（`entity` / `mapper` / `service` 骨架已就绪，除 `/auth/*` 外的业务接口尚未开始）；前端页面（`routes` 为空数组，`views` / `components` 无页面）；工程化（未安装 `node_modules`，无 lock 文件）。

**待开始**：《前置任务》7.2 的 24 个业务接口（B 课程/项目/小组、C 任务看板、D 总览）；任务看板、总览图表、按角色渲染的菜单与登录页（P3-05）；集成测试与部署。

| 检查项 | 判定 | 依据 |
| --- | --- | --- |
| `Result` 被至少一个接口使用 | 达成 | `AuthController` 三个接口均返回 `Result` |
| `SecurityConfig` 白名单 `/auth/login` | 达成 | `/api/auth/login` 不带 token 可达（非 401） |
| 受保护路径未认证返回 401 | 达成 | `/api/auth/me` 无 Token → 401 + `code=401` |
| JWT 过滤器 + 注册/登录/me 闭环 | 达成 | 登录返回 token，带 token 访问 `/auth/me` → 200 |
| CORS 放行 5173 | 达成 | `OPTIONS /auth/me` 带 Origin 返回 `Access-Control-Allow-Origin` |
| Knife4j 四分组 | 达成 | `/api/doc.html` 可访问，4 个分组（认证与用户 / 课程项目小组 / 任务与看板 / 总览统计） |
| element-plus | 未达成 | `package.json` 无该依赖（前端组件库选型待定） |
| 登录页可访问 | 未达成 | `routes = []`（登录页属 P3-05） |
| 11 表 / 1 视图 / 种子数据 | 脚本就绪，库实例待验证 | 执行「快速开始」后 `SHOW FULL TABLES` |
| README 七项齐备 | 达成 | 本文件 |

近期动作：**A** 已交付认证闭环（JWT + 注册/登录/me + CORS），下一步补 Knife4j（P2-10）并组织契约冻结；**B / C** 各落 1 个列表接口（验收：分页与错误码符合「接口约定」）；**D / 全员** 安装依赖并提交 lock 文件，用 `v_project_progress` 做总览查询原型。

## 项目结构

```
TeamPulse/
├── backend/                     Spring Boot 3.2.5 + Java 21（pom.xml + src）
│   └── src/
│       ├── main/java/com/example/teampulse/
│       │   ├── TeamPulseApplication.java
│       │   ├── common/          Result / ErrorCode / PageResult / PageAdapter（已实现）
│       │   ├── config/          MybatisPlusConfig / SecurityConfig / JwtAuthenticationFilter / CorsConfig / RestAuthenticationEntryPoint / RestAccessDeniedHandler（已实现）
│       │   ├── controller/      接口层（AuthController 已实现，其余按模块分文件）
│       │   ├── dto/             入参对象（RegisterRequest / LoginRequest）
│       │   ├── entity/          数据库实体（MyBatis-Plus 注解）
│       │   ├── mapper/          Mapper 接口（继承 BaseMapper<T>）
│       │   ├── service/         Service 接口（继承 IService<T>）
│       │   │   └── impl/        Service 实现（继承 ServiceImpl<Mapper, T>）
│       │   ├── security/        UserDetailsServiceImpl（用户+角色加载）
│       │   ├── exception/       BusinessException / GlobalExceptionHandler（已实现）
│       │   ├── util/            JwtUtil（已实现）
│       │   └── vo/              出参对象（UserVO / LoginVO）
│       ├── main/resources/      application*.yml、db/{schema,seed}.sql、mapper/
│       └── test/java/           TeamPulseApplicationTests、exception/GlobalExceptionHandlerTest、util/JwtUtilTest
├── frontend/                    Vue 3 + Vite（src 下 api/router/stores 为骨架，views/components 为空）
├── docs/                        设计文档、ER 图、数据库脚本、文档生成脚本
└── README.md
```

## 相关文档

- `docs/数据库顶层设计.docx`：表结构、索引策略与字段说明；`docs/er_diagram.png` / `.vsdx`：ER 图导出图与源文件。
- `docs/schema.sql`、`docs/seed.sql`：与 `backend/src/main/resources/db/` 下同名文件内容一致，修改时需同步。
- `docs/draw_er_diagram.py` 为 ER 图生成脚本，不参与构建；`docs/ddl_v0.3.sql`、`docs/patch_versions.py` 为本地追溯文件，未纳入版本库。
- 接口文档（Knife4j）：后端启动后访问 `http://localhost:8080/api/doc.html`（端口以 `application-dev.yml` 实际配置为准）；Apifox 可从 `http://localhost:8080/api/v3/api-docs` 导入 OpenAPI JSON。
- `docs/test/后端自测记录.md`：P2-11 自测留档模板（M2 验收证据），跑完 Apifox 自测后填写。
- `docs/test/apifox-self-test.openapi.json`：**零配置** Apifox 自测文件（内置 dev token，导入即用）；含可用 token，已加入 `.gitignore`，请勿提交。
- 本 README 位于 git 仓库根 `TeamPulse/`；工作区上级目录 `SoftwareEngineering/` 不在仓库内。

## 常见问题

1. **8080 被占用**：`netstat -ano | findstr :8080` 找到 PID 后 `taskkill /PID <pid> /F`；macOS / Linux 用 `lsof -i :8080`。
2. **后端报数据库连接失败**：确认 MySQL 已启动、`teampulse` 库已创建、`application-dev.yml` 中的连接信息与本机一致。
3. **访问后端一直返回 401**：`SecurityConfig` 已启用认证，未带 `Authorization: Bearer <token>` 时受保护路径一律 401，属预期。先 `POST /api/auth/login` 拿 token，再带 `Authorization: Bearer <token>` 访问。若访问 `/api/auth/login` 也返回 401，多半是白名单被改成了 `/api/auth/login`——白名单路径不含 context-path。
4. **`admin` 登录失败**：`seed.sql` 中密码是占位符，需按「快速开始 · 管理员账号」本地生成后更新。
5. **前端空白或 404**：确认通过 http://localhost:5173 访问而不是直接打开 `index.html`，空白页是路由为空所致。
