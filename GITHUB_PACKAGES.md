# 使用 GitHub Packages 安装 Aether

由于 Aether 尚未发布到 Maven Central，你可以通过 GitHub Packages 使用。

## 配置 Maven

在 `pom.xml` 中添加 GitHub Packages 仓库：

```xml
<repositories>
    <repository>
        <id>github</id>
        <name>GitHub Packages</name>
        <url>https://maven.pkg.github.com/1lovevv/aether</url>
    </repository>
</repositories>
```

## 配置认证

在 `~/.m2/settings.xml` 中添加 GitHub 认证：

```xml
<settings>
    <servers>
        <server>
            <id>github</id>
            <username>你的GitHub用户名</username>
            <password>你的GitHub Personal Access Token</password>
        </server>
    </servers>
</settings>
```

### 获取 GitHub Token

1. 访问 https://github.com/settings/tokens
2. 点击 **Generate new token**
3. 勾选 `read:packages` 权限
4. 生成并复制 Token

## 使用依赖

```xml
<dependency>
    <groupId>com.aether</groupId>
    <artifactId>aether-spring-boot-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## 替代方案：本地构建

如果以上方式太麻烦，可以直接 clone 源码本地构建：

```bash
git clone https://github.com/1lovevv/aether.git
cd aether
mvn clean install
```

然后在你的项目中直接引用（无需配置额外仓库）。
