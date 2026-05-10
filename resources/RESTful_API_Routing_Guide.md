# RESTful API 路由设计规范

## 🎯 问题背景

在开发过程中，经常遇到以下错误：

```
org.springframework.web.servlet.resource.NoResourceFoundException: 
No static resource api/member/courses/1 for request '/api/member/courses/1'.
```

**根本原因：**
- 前端访问了某个路径（如 `/api/member/courses/1`）
- 但后端 Controller 中没有对应的处理方法
- Spring MVC 找不到匹配的 `@RequestMapping`，将其当作静态资源处理
- 导致 `NoResourceFoundException` 异常

## ✅ 解决方案

### 核心原则：**完整的 RESTful 路由覆盖**

对于每个资源（Resource），应该提供完整的 CRUD 操作接口，避免路由空缺。

## 📋 标准 RESTful 路由模板

以课程资源为例：

| HTTP 方法 | 路径 | 说明 | 是否必需 |
|-----------|------|------|----------|
| GET | `/api/member/courses` | 获取课程列表（分页） | ✅ 必需 |
| GET | `/api/member/courses/{id}` | 获取课程详情 | ✅ 必需 |
| POST | `/api/member/courses` | 创建课程 | 可选 |
| PUT | `/api/member/courses/{id}` | 更新课程 | 可选 |
| DELETE | `/api/member/courses/{id}` | 删除课程 | 可选 |

### ⚠️ 常见错误示例

**错误做法：**
```java
@RestController
@RequestMapping("/api/member/courses")
public class MemberCourseController {
    
    // ❌ 只有子路径，没有根路径和详情路径
    @GetMapping("/bookings")
    public List<CourseBookingResponse> getMyBookings() { ... }
    
    @PostMapping("/book")
    public CourseBookingResponse bookCourse() { ... }
}
```

**问题：**
- 访问 `/api/member/courses` → ❌ NoResourceFoundException
- 访问 `/api/member/courses/1` → ❌ NoResourceFoundException

**正确做法：**
```java
@RestController
@RequestMapping("/api/member/courses")
public class MemberCourseController {
    
    // ✅ 根路径 - 获取列表
    @GetMapping
    public Page<CourseResponse> getCourses(...) { ... }
    
    // ✅ 详情路径 - 获取单个资源
    @GetMapping("/{courseId}")
    public CourseResponse getCourseDetail(@PathVariable Integer courseId) { ... }
    
    // ✅ 子路径 - 其他业务操作
    @GetMapping("/bookings")
    public List<CourseBookingResponse> getMyBookings() { ... }
    
    @PostMapping("/book")
    public CourseBookingResponse bookCourse() { ... }
}
```

## 🔍 路由匹配规则

Spring MVC 的路由匹配优先级：

1. **精确匹配** > **路径变量匹配** > **通配符匹配**
2. **具体路径** > **根路径**

示例：
```
/api/member/courses           → @GetMapping              (根路径)
/api/member/courses/1         → @GetMapping("/{id}")     (路径变量)
/api/member/courses/bookings  → @GetMapping("/bookings") (子路径)
/api/member/courses/book      → @PostMapping("/book")    (子路径)
```

## 🛡️ 预防措施

### 1. 设计阶段：完整的路由规划

在创建 Controller 之前，先规划好所有需要的路由：

```java
/**
 * 会员中心 - 课程报名接口
 * 
 * 路由清单：
 * GET    /api/member/courses              - 获取课程列表
 * GET    /api/member/courses/{id}         - 获取课程详情
 * GET    /api/member/courses/bookings     - 获取我的报名
 * POST   /api/member/courses/book         - 报名课程
 * POST   /api/member/courses/bookings/{id}/pay - 支付报名
 */
@RestController
@RequestMapping("/api/member/courses")
public class MemberCourseController {
    // ...
}
```

### 2. 开发阶段：立即添加基础路由

创建 Controller 时，**第一时间**添加根路径和详情路径：

```java
@RestController
@RequestMapping("/api/resource")
public class ResourceController {
    
    // 第一步：添加根路径（列表）
    @GetMapping
    public Page<ResourceResponse> list(...) { ... }
    
    // 第二步：添加详情路径
    @GetMapping("/{id}")
    public ResourceResponse detail(@PathVariable Integer id) { ... }
    
    // 第三步：添加其他业务路由
    // ...
}
```

### 3. 测试阶段：全面的路由测试

使用 Postman 或 curl 测试所有可能的路径：

```bash
# 测试根路径
curl http://localhost:8080/api/member/courses

# 测试详情路径
curl http://localhost:8080/api/member/courses/1

# 测试子路径
curl http://localhost:8080/api/member/courses/bookings

# 测试不存在的路径（应该返回 404，而不是 NoResourceFoundException）
curl http://localhost:8080/api/member/courses/invalid
```

### 4. 错误处理：统一异常处理

确保 `GlobalExceptionHandler` 能正确处理 `NoResourceFoundException`：

```java
@ExceptionHandler(NoResourceFoundException.class)
public ResponseEntity<Result<Void>> handleNoResourceFound(NoResourceFoundException ex) {
    log.warn("资源未找到: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Result.error(ResultCode.RESOURCE_NOT_FOUND));
}
```

## 📝 实际案例

### 案例 1：会员端课程模块

**完整的路由设计：**

```java
@RestController
@RequestMapping("/api/member/courses")
public class MemberCourseController {
    
    // ✅ 获取课程列表（分页）
    @GetMapping
    public ResponseEntity<Result<Page<CourseResponse>>> getCourses(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer storeId) {
        // ...
    }
    
    // ✅ 获取课程详情
    @GetMapping("/{courseId}")
    public ResponseEntity<Result<CourseResponse>> getCourseDetail(
            @PathVariable Integer courseId) {
        // ...
    }
    
    // ✅ 获取我的课程报名列表
    @GetMapping("/bookings")
    public ResponseEntity<Result<List<CourseBookingResponse>>> getMyCourseBookings() {
        // ...
    }
    
    // ✅ 报名课程
    @PostMapping("/book")
    public ResponseEntity<Result<CourseBookingResponse>> bookCourse(
            @Valid @RequestBody CourseBookingRequest request) {
        // ...
    }
    
    // ✅ 支付课程报名
    @PostMapping("/bookings/{bookingId}/pay")
    public ResponseEntity<Result<CourseBookingResponse>> payCourseBooking(
            @PathVariable Integer bookingId,
            @Valid @RequestBody PaymentRequest request) {
        // ...
    }
}
```

**路由映射表：**

| 请求 | 映射方法 | 说明 |
|------|---------|------|
| `GET /api/member/courses` | `getCourses()` | 课程列表 |
| `GET /api/member/courses/1` | `getCourseDetail(1)` | 课程详情 |
| `GET /api/member/courses/bookings` | `getMyCourseBookings()` | 我的报名 |
| `POST /api/member/courses/book` | `bookCourse()` | 报名课程 |
| `POST /api/member/courses/bookings/1/pay` | `payCourseBooking(1)` | 支付报名 |

### 案例 2：其他模块参考

**会员预约模块：**
```java
@RestController
@RequestMapping("/api/member/appointments")
public class MemberAppointmentController {
    
    @GetMapping                          // 预约列表
    @GetMapping("/{id}")                 // 预约详情
    @PostMapping                         // 创建预约
    @PutMapping("/{id}")                 // 更新预约
    @DeleteMapping("/{id}")              // 取消预约
    @PostMapping("/{id}/pay")            // 支付预约
}
```

**订单模块：**
```java
@RestController
@RequestMapping("/api/member/orders")
public class MemberOrderController {
    
    @GetMapping                          // 订单列表
    @GetMapping("/{id}")                 // 订单详情
    @PostMapping                         // 创建订单
    @PostMapping("/{id}/pay")            // 支付订单
    @PostMapping("/{id}/cancel")         // 取消订单
}
```

## 🚀 快速检查清单

在提交代码前，检查以下内容：

- [ ] 根路径是否有处理方法？（`GET /api/resource`）
- [ ] 详情路径是否有处理方法？（`GET /api/resource/{id}`）
- [ ] 所有子路径是否都有对应的 `@RequestMapping`？
- [ ] 是否使用了正确的 HTTP 方法？（GET/POST/PUT/DELETE）
- [ ] 路径变量是否正确标注了 `@PathVariable`？
- [ ] 是否添加了角色权限控制？（`@PreAuthorize`）
- [ ] 是否进行了全面的路由测试？

## 💡 最佳实践总结

1. **先设计，后编码**：在写代码前先规划好所有路由
2. **基础路由优先**：先实现列表和详情，再实现其他业务路由
3. **保持一致性**：同一模块内的路由风格保持一致
4. **及时测试**：每添加一个路由就立即测试
5. **文档同步**：路由变更后及时更新 API 文档
6. **错误友好**：确保未找到的路由返回友好的 404 提示

## 🔗 相关资源

- [Spring MVC 路由匹配机制](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-ann-requestmapping)
- [RESTful API 设计指南](https://restfulapi.net/)
- [HTTP 状态码规范](https://httpstatuses.com/)

---

**记住：** 完整的路由覆盖是避免 `NoResourceFoundException` 的关键！
