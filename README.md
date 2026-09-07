# ZC Center Spring Boot SDK

适用于 Spring Boot 3.x（Java 17+）的中台 SAPI 服务端 SDK，提供 HMAC-SHA256 请求签名、AES-256-GCM 加解密、响应验签和现有业务接口封装。

协议与 [`docs/sapi/`](../../docs/sapi/README.md)、ThinkPHP SDK 保持一致。

## 环境要求

- Java 17+
- Spring Boot 3.2+（可选，用于自动配置；亦可纯 Java 使用 `ZcCenterClient`）
- Jackson Databind

`app_secret` 只能保存在生态应用服务端，禁止写入前端代码、客户端安装包或日志。

## 安装

Maven 坐标（发布到 Maven Central 后可直接引用；发布前请先 `mvn clean install`）：

```xml
<dependency>
  <groupId>io.github.763606865</groupId>
  <artifactId>zc-center-spring-boot-sdk</artifactId>
  <version>1.0.0</version>
</dependency>
```

### 安装到本地 Maven 仓库

```bash
mvn clean install -DskipTests
```

发布到 Maven Central（需本机已配置 `~/.m2/settings.xml` 中的 `central` Token，并激活 GPG）：

```bash
mvn clean deploy -P release
```

### 或使用系统 Path（多模块 / 相对路径）

在生态项目中增加：

```xml
<repositories>
  <!-- 若使用私有 Nexus，请改成内网源 -->
</repositories>
```

本机联调适合 `mvn install`；CI / 部署机拿不到这份源码时，应改用私有 Git 或内网 Maven 源。

## 配置

`application.yml`：

```yaml
zc-center:
  base-url: https://zc-center.example.com
  app-key: 中台分配的app_key
  app-secret: 中台创建应用时仅展示一次的app_secret
  encryption: true
  timeout-seconds: 10
  connect-timeout-seconds: 3
  verify-ssl: true
  debug: false
  # 题目上报（可选）
  report-enabled: false
  report-bank-uuid: ""
  report-bank-code: ""
```

中台联调环境关闭了 `SAPI_ENCRYPTION_ENABLED` 时，生态项目必须同步设置 `encryption: false`。生产环境双方都必须开启加密。

`report-enabled: true` 时必须配置 `report-bank-uuid` 或 `report-bank-code`。

设置 `debug: true` 后，每次 SAPI 调用会写入 INFO 日志（含请求参数与响应载荷）。生产请保持 `false`。

引入依赖且配置了 `zc-center.base-url` 后，自动注册 `ZcCenterClient` Bean。

## 使用

### 依赖注入

```java
import com.zccenter.sdk.ZcCenterClient;
import com.zccenter.sdk.SapiResponse;
import org.springframework.stereotype.Service;

@Service
public class CenterService {
    private final ZcCenterClient center;

    public CenterService(ZcCenterClient center) {
        this.center = center;
    }

    public Map<String, Object> ping() {
        SapiResponse response = center.ping().send("hello");
        return response.getPayload();
    }
}
```

### 纯 Java 手动创建

```java
ZcCenterProperties props = new ZcCenterProperties();
props.setBaseUrl("https://zc-center.example.com");
props.setAppKey("...");
props.setAppSecret("...");
props.setEncryption(true);

ZcCenterClient center = new ZcCenterClient(props);
```

### 用户注册

```java
SapiResponse response = center.user().register("13800138000", "+86", "示例用户", null);

@SuppressWarnings("unchecked")
Map<String, Object> user = (Map<String, Object>) response.getDataAsMap().get("user");
String uuid = (String) user.get("uuid");
```

### 企业上报与职工

```java
import com.zccenter.sdk.api.EnterpriseApi;

Map<String, Object> reported = center.enterprise().report(Map.of(
    "name", "示例科技有限公司",
    "code", "example_tech",
    "credit_code", "91110000MA01234567",
    "admin_mobile", "13800138000"
)).getDataAsMap();

center.enterprise().addMember(Map.of(
    "enterprise_uuid", ((Map<?,?>) reported.get("enterprise")).get("uuid"),
    "mobile", "13900139000",
    "role", EnterpriseApi.ROLE_MEMBER
));
```

### 题库 / 题目

```java
center.questionBank().list(Map.of("page", 1, "page_size", 20));
center.question().search(Map.of("keyword", "导数", "page", 1));
center.question().detail(questionUuid, false);
```

题目上报需开启 `report-enabled`：

```java
center.question().report(Map.of(
    "type", QuestionApi.TYPE_SINGLE,
    "stem", "1+1等于多少？",
    "options", List.of(
        Map.of("key", "A", "content", "1"),
        Map.of("key", "B", "content", "2")
    ),
    "answer", "B"
));
```

### 自定义接口

```java
public class ClassroomApi extends AbstractApi {
    public ClassroomApi(ZcCenterClient client) {
        super(client);
    }

    public SapiResponse syncCourse(Map<String, Object> payload) {
        return post("/sapi/classroom/course/sync", payload);
    }
}

ClassroomApi classroom = center.api(ClassroomApi.class);
```

## 异常

| 异常 | 含义 |
| --- | --- |
| `SapiException` | SDK 基类异常 |
| `ApiException` | 业务错误（含 `businessCode` / `httpStatus`） |
| `SignatureException` | 签名校验失败 |
| `CryptoException` | 加解密失败 |
| `TransportException` | 网络或响应解析失败 |

## 本地测试

```bash
mvn test
```

## 相关文档

- [SAPI 接口文档](../../docs/sapi/README.md)
- [ThinkPHP SDK](../thinkphp/README.md)
