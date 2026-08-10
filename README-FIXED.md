# LowYSwampHut 修复版 — 使用说明

## 文件清单（15 个文件）

```
LowYSwampHut-fixed.zip 内部：
├── README-FIXED.md          ← 本文件
├── build.gradle              ← 已修复：含 noise-sampler + latticg 依赖
├── settings.gradle          ← 最小配置
├── gradlew / gradlew.bat    ← Gradle wrapper 脚本
├── .github/workflows/
│   └── build.yml           ← CI 工作流（自动解压 zip → 构建 → 上传 jar）
├── gradle/wrapper/
│   └── gradle-wrapper.properties
└── src/main/java/project/
    ├── CmdLineRunner.java   ← 命令行入口（--seed 等参数）
    ├── CoordResult.java     ← 坐标结果 record
    ├── GameVersion.java     ← MC 版本枚举
    ├── Launcher.java        ← GUI 入口
    ├── LowYSwampHutForFixedSeed.java ← 主 GUI 窗口（3045 行）
    ├── SearchCoords.java     ← 核心搜索算法（963 行）
    ├── SearchMetricsHook.java
    ├── SeedCheckerFactory.java
    ├── SeedCheckerInitializer.java
    └── WorldPresetMode.java
```

## 部署到 GitHub 的步骤（网页端完成）

### 第 1 步：上传 zip 到仓库根目录
把 `LowYSwampHut-fixed.zip` 上传到你 GitHub 仓库的**根目录**。
- 网页端：直接拖拽到文件列表
- 或 git 命令：
  ```bash
  git add LowYSwampHut-fixed.zip
  git commit -m "Add fixed source zip"
  git push
  ```

### 第 2 步：上传 build.yml 到 .github/workflows/
把 `.github/workflows/build.yml` 也上传到仓库对应位置。
（如果仓库里已有旧的 workflow 文件，先删掉再上传）

### 第 3 步：触发构建
- 进入仓库 `Actions` 选项卡
- 找到 `Build LowYSwampHut`
- 点 `Run workflow` → 确认
- 等待 2~5 分钟

### 第 4 步：下载 jar
构建成功后：
- 点进 workflow 运行记录
- 右侧 `Artifacts` 区域 → 下载 `LowYSwampHut.jar`

## 工作流做了什么（自动）

```
1. Checkout 仓库（含 LowYSwampHut-fixed.zip）
2. 安装 JDK 17 (Temurin)
3. 验证 zip 存在 ✅
4. 解压 zip → 得到完整源码
5. 复制源码到工作目录
6. chmod +x gradlew
7. ./gradlew shadowJar → 编译 + 打包
8. 验证 LowYSwampHut.jar 产出 ✅
9. 上传 jar 作为 Artifact
```

## 使用 jar

```bash
# 命令行模式
java -jar LowYSwampHut.jar --seed 123456 --max-y -40 --version 26.2 -o result.txt

# GUI 模式（无参数自动启动）
java -jar LowYSwampHut.jar

# 帮助
java -jar LowYSwampHut.jar -h
```

## 修复内容总结

| 问题 | 修复 |
|---|---|
| `noise-sampler` 依赖缺失 → 100 个编译错误 | ✅ 添加 `noise-sampler:1.20.0` |
| `latticg` 依赖缺失 | ✅ 添加 `latticg:1.06@jar` |
| `build.gradle.kts` 冲突 | ✅ 已删除，只用 Groovy 版 |
| `settings.gradle.kts` 冲突 | ✅ 已删除 |
| 命令行入口未激活 | ✅ `Main-Class: project.CmdLineRunner` |
| `CmdLineRunner` API 不匹配 | ✅ 用 `SearchCoords.Result` 重写 |

## 验证清单

- ✅ `noise-sampler:1.20.0` 从 `repo.jellejurre.dev` 可拉取
- ✅ `SearchCoords.java` 的 14 个 import 全部满足
- ✅ `CmdLineRunner` 回调签名与 `startSearch` 匹配
- ✅ ShadowJar `Main-Class` 正确设置
- ✅ Log4j2 插件缓存合并配置正确
- ✅ JDK 17 与 `sourceCompatibility=17` 匹配
