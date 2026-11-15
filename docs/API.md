# 接口手册（API Reference）

> 本系统为**服务端渲染（SSR）应用**，接口均为 Spring MVC 的 HTML 端点，返回 Thymeleaf 视图名或 302 重定向，
> 而非 JSON REST 接口。表单提交使用 `application/x-www-form-urlencoded`。

- 服务地址（dev）：`http://localhost:8080`
- 服务地址（test）：`http://localhost:8081`
- 统一前缀：无（`server.servlet.context-path` 未设置）
- 会话方式：`HttpSession` + `JSESSIONID` Cookie

---

## 一、接口总览

| # | 方法 | 路径 | 功能 | 鉴权 | 返回 |
|---|---|---|---|---|---|
| 1 | GET | `/`、`/index` | 首页 | ✔ | 视图 `index` |
| 2 | GET | `/login` | 登录页 | — | 视图 `login` |
| 3 | POST | `/doLogin` | 登录提交 | — | 302 `/index` 或视图 `login` |
| 4 | GET | `/logout` | 退出登录 | ✔ | 302 `/login` |
| 5 | GET | `/task` | 任务列表（分页 + 条件查询） | ✔ | 视图 `task` |
| 6 | GET | `/task/add` | 新增表单 | ✔ | 视图 `add_update` |
| 7 | GET | `/task/update/{id}` | 编辑表单 | ✔ | 视图 `add_update` 或 302 `/task` |
| 8 | POST | `/task/save` | 保存（新增/修改） | ✔ | 302 `/task` 或视图 `add_update` |
| 9 | POST | `/task/delete` | 删除任务 | ✔ | 302 `/task` |

> **鉴权图例**：`✔` 表示需要登录态，由 `LoginInterceptor` 拦截，未登录时返回 **302 → `/login`**；
> `—` 表示免登录访问（白名单）。
> 白名单：`/login`、`/doLogin`、`/logout`、`/css/**`、`/js/**`、`/images/**`、`/webjars/**`、`/favicon.ico`、`/error`、`/error/**`。

---

## 二、登录与会话

### 2.1 会话属性

登录成功后写入 `HttpSession` 的三个属性，模板与控制器共同依赖：

| 属性名 | 常量 | 类型 | 说明 |
|---|---|---|---|
| `flag` | `LoginInterceptor.SESSION_FLAG` | `Boolean` | 登录标记，拦截器只认它 |
| `userId` | `LoginInterceptor.SESSION_USER_ID` | `Long` | 当前用户主键，任务归属与数据过滤依赖它 |
| `loginUser` | `LoginInterceptor.SESSION_LOGIN_USER` | `String` | 当前用户名，仅用于页面展示 |

会话超时：`30m`（`server.servlet.session.timeout`），Cookie 带 `http-only: true`。

### 2.2 GET `/login` — 登录页

**请求参数**：无

**模型属性**：

| 属性 | 类型 | 说明 |
|---|---|---|
| `form` | `LoginForm`（空对象） | 页面用 `th:object="${form}"` 绑定，必须预先放入否则渲染报错 |

**响应**：视图 `login`

### 2.3 POST `/doLogin` — 登录提交

**请求参数**（表单）：

| 字段 | 类型 | 必填 | 约束 | 说明 |
|---|---|---|---|---|
| `username` | String | 是 | `@NotBlank`、`@Size(max=50)` | 用户名 |
| `password` | String | 是 | `@NotBlank`、`@Size(min=6, max=100)` | 口令 |

**处理逻辑**：

1. `@Valid` 校验失败 → 返回视图 `login`，错误通过 `th:errors` 在字段旁回显，已输入内容保留；
2. 校验通过 → `UserBussImpl#login` 按 `username` 精确查询；
3. 查不到或口令不匹配 → 模型属性 `error = "用户名或密码错误"`，返回视图 `login`；
4. 匹配成功 → 写入 `flag` / `userId` / `loginUser`，302 重定向 `/index`。

**响应**：

| 场景 | 结果 |
|---|---|
| 校验失败 / 账号或密码错误 | 视图 `login` |
| 登录成功 | `302 Location: /index` |

**示例**：

```bash
curl -c cookie.txt -X POST http://localhost:8080/doLogin \
  -d "username=admin" -d "password=123456" -i
```

```powershell
$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession
Invoke-WebRequest -Uri "http://localhost:8080/doLogin" -Method Post `
  -Body @{ username = "admin"; password = "123456" } -WebSession $session
```

**预置账号**：`admin / 123456`

### 2.4 GET `/logout` — 退出登录

**请求参数**：无

**处理逻辑**：`session.invalidate()` 使会话失效。

**响应**：`302 Location: /login`

---

## 三、任务管理

### 3.1 GET `/task` — 任务列表

**请求参数**（Query，全部可选）：

| 参数 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| `current` | long | `1` | 当前页码，从 1 开始；小于 1 时按 1 处理 |
| `size` | long | `10` | 每页条数；小于 1 时按 1 处理；单页上限保护 500 |
| `keyword` | String | — | 标题或内容模糊匹配（`LIKE %kw%`），空白串视为不过滤 |
| `status` | Integer | — | `0` 未完成 / `1` 已完成；不传表示不限制 |

> 数据范围：查询强制带上 `user_id = session.userId`，即只返回当前登录用户的任务。

**模型属性**：

| 属性 | 类型 | 说明 |
|---|---|---|
| `page` | `IPage<Task>` | 分页对象，含 `current` / `size` / `total` / `pages` |
| `tasks` | `List<Task>` | 等价于 `page.getRecords()` |
| `keyword` | String | 回显到搜索框 |
| `status` | Integer | 回显到状态下拉框 |

**响应**：视图 `task`

**分页字段说明**：

| 字段 | 含义 | 页面用途 |
|---|---|---|
| `page.current` | 当前页码 | 「第 N 页」/ 上一页、下一页是否可点 |
| `page.size` | 每页条数 | 分页链接透传 |
| `page.total` | 总记录数 | 「共 M 条」 |
| `page.pages` | 总页数 | `pages > 1` 时才渲染分页栏 |

**示例**：

```bash
curl -b cookie.txt "http://localhost:8080/task?current=1&size=10&keyword=报告&status=0"
```

### 3.2 GET `/task/add` — 新增表单

**请求参数**：无

**模型属性**：

| 属性 | 值 | 说明 |
|---|---|---|
| `task` | `new Task()` | 空实体，用于表单绑定 |
| `opt` | `"add"` | 决定标题显示「新增任务」、保存时走 add 分支 |

**响应**：视图 `add_update`

### 3.3 GET `/task/update/{id}` — 编辑表单

**路径参数**：

| 参数 | 类型 | 说明 |
|---|---|---|
| `id` | Long | 任务主键 |

**请求参数**（Query）：

| 参数 | 默认值 | 说明 |
|---|---|---|
| `opt` | `"update"` | 页面标题与保存分支标识 |

**处理逻辑**：`TaskBuss#get(id)` 查不到记录时 302 重定向 `/task`。

**模型属性**：`task`（回显的实体）、`opt`

**响应**：视图 `add_update` 或 `302 Location: /task`

**示例**：`GET /task/update/5?opt=update`

### 3.4 POST `/task/save` — 保存（新增 / 修改）

**请求参数**（表单）：

| 字段 | 类型 | 必填 | 约束 | 说明 |
|---|---|---|---|---|
| `opt` | String | 是 | `add` / `update` | 决定新增还是修改 |
| `id` | Long | 修改时必填 | — | 主键；新增时为空 |
| `title` | String | 是 | `@NotBlank`、`@Size(max=120)` | 任务标题 |
| `content` | String | 否 | `@Size(max=1000)` | 任务内容 |
| `priority` | Integer | 否 | 1 低 / 2 中 / 3 高 | 缺省由业务层补 2 |
| `status` | Integer | 否 | 0 未完成 / 1 已完成 | 缺省由业务层补 0 |
| `taskDate` | LocalDate | 否 | `yyyy-MM-dd` | 截止日期，格式错误会导致绑定失败 |

**处理逻辑**：

1. `@Valid` 校验失败 → 重新放入 `opt` 并返回视图 `add_update`，错误字段下方提示，已填内容保留；
2. `opt=add` → 补 `userId = session.userId` 后调用 `taskBuss.add(task)`，Flash 提示「新增成功」；
3. `opt!=add` → 调用 `taskBuss.update(task)`，Flash 提示「修改成功」；
4. 302 重定向 `/task`。

**响应**：

| 场景 | 结果 |
|---|---|
| 校验失败 | 视图 `add_update` |
| 保存成功 | `302 Location: /task` |

**示例**：

```bash
curl -b cookie.txt -X POST http://localhost:8080/task/save \
  -d "opt=add" -d "title=写实验报告" -d "content=环节二技术文档" \
  -d "priority=3" -d "status=0" -d "taskDate=2026-11-20"
```

### 3.5 POST `/task/delete` — 删除任务

**请求参数**（表单）：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `id` | Long | 是 | 待删除任务主键 |

**处理逻辑**：`taskBuss.remove(id)`，Flash 提示「删除成功」，302 重定向 `/task`。

> 使用 POST 而非 GET，避免浏览器预加载或爬虫误触发删除。

**响应**：`302 Location: /task`

---

## 四、数据字典

### 4.1 任务优先级 `priority`

| 值 | 含义 | 页面展示 |
|---|---|---|
| 1 | 低 | 低 |
| 2 | 中（默认） | 中 |
| 3 | 高 | 高 |

### 4.2 任务状态 `status`

| 值 | 含义 | 页面展示 |
|---|---|---|
| 0 | 未完成（默认） | 未完成（`todo` 样式） |
| 1 | 已完成 | 已完成（`done` 样式） |

### 4.3 实体字段映射

| 实体属性 | 数据库列 | 映射方式 |
|---|---|---|
| `Task.id` | `id` | `@TableId(type = IdType.AUTO)` |
| `Task.taskDate` | `task_date` | `@TableField("task_date")` + 自动驼峰 |
| `Task.userId` | `user_id` | `@TableField("user_id")` |
| `User.realName` | `real_name` | `@TableField("real_name")` |
| 表名 | `task` / `sys_user` | `@TableName` |

---

## 五、错误码与异常响应

系统没有独立的 JSON 错误体，所有异常统一渲染为视图 `error`（`@ControllerAdvice` 处理）。

| HTTP 状态 | 触发条件 | 处理类/方法 | 页面提示 |
|---|---|---|---|
| `302` | 未登录访问受保护路径 | `LoginInterceptor#preHandle` | 重定向 `/login` |
| `400` | 表单校验失败（`BindException`） | `handleBindException` | 「提交的数据不符合要求，请检查后重新填写。」 |
| `400` | 业务参数非法（`IllegalArgumentException`） | `handleIllegalArgument` | 异常自带的 `message`，如「任务标题不能为空」 |
| `500` | 数据库不可用、空指针等未预期异常 | `handleException` | 「系统开小差了，请稍后重试或联系管理员。」 |

**错误页模型属性**：

| 属性 | 说明 |
|---|---|
| `status` | 状态码（400 / 500） |
| `message` | 面向用户的一句话提示 |
| `path` | 出错请求路径 |

> 页面**不暴露堆栈**；`log.warn` / `log.error` 仅在服务端日志中记录 `path` 与异常明细。

---

## 六、校验规则汇总

### 6.1 登录表单 `LoginForm`

| 字段 | 规则 | 违反时提示 |
|---|---|---|
| `username` | 非空白、长度 ≤ 50 | 用户名不能为空 / 用户名长度不能超过 50 个字符 |
| `password` | 非空白、长度 6–100 | 密码不能为空 / 密码长度必须在 6 - 100 个字符之间 |

### 6.2 任务实体 `Task`

| 字段 | 规则 | 违反时提示 |
|---|---|---|
| `title` | 非空白、长度 ≤ 120 | 任务标题不能为空 / 标题长度不能超过 120 个字符 |
| `content` | 长度 ≤ 1000 | 内容长度不能超过 1000 个字符 |
| `taskDate` | `yyyy-MM-dd` | 格式不符会导致绑定失败 |

> 前端 `required` / `maxlength` 只是体验优化；**服务端 `@Valid + BindingResult` 才是唯一生效的校验**。

---

## 七、联调建议顺序

1. `GET /login` 确认页面可访问（免登录白名单生效）；
2. `POST /doLogin` 用 `admin / 123456` 登录，确认拿到 `JSESSIONID` 并 302 到 `/index`；
3. `GET /task` 带 Cookie 访问，确认列表与分页信息；
4. 依次验证新增 → 编辑 → 删除 → 条件查询；
5. 清空 Cookie 再访问 `/task`，确认被重定向到 `/login`；
6. 关闭 MySQL 后访问 `/task`，确认渲染统一错误页而非堆栈。
