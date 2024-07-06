# JAR兼容性测试目录说明

本目录是用于测试一系列JAR包之间兼容性的专用环境。通过使用`CompatibilityDetector`工具，可以验证不同JAR包集合的兼容性状态。

## 目录结构

- `compatible`: 此目录包含一组高度兼容的JAR包。这些JAR包经过测试，确认它们之间不存在兼容性问题。
- `conflict`: 此目录包含一组存在部分不兼容的JAR包。这些JAR包可能因某些方法或类不存在而导致兼容性问题。

## 测试工具

`CompatibilityDetector`是一个Java应用程序，用于分析指定目录下JAR包的兼容性。

### 兼容性测试命令

#### 高度兼容JAR包测试

要测试`compatible`目录下的JAR包集合，使用以下命令：

```bash
java -jar build/libs/CompatibilityDetector-1.0-SNAPSHOT.jar --target /Path/to/jar-compatibility-detector/test/compatible --javaVersion 8
```

#### 存在不兼容JAR包测试

要测试`conflict`目录下的JAR包集合，使用以下命令：

```bash
java -jar build/libs/CompatibilityDetector-1.0-SNAPSHOT.jar --target /Path/to/jar-compatibility-detector/test/conflict --javaVersion 8
```

### 参数说明

- `--target <path>`: 指定需要分析的JAR包所在的目录路径。
- `--javaVersion <version>`: 指定工具使用的JRE版本。此处示例中使用的是Java 8。

## 使用指南

1. 确保您已经构建了`CompatibilityDetector`工具，并且JAR文件位于`build/libs/`目录下。
2. 根据需要测试的JAR包集合（兼容或存在冲突），修改测试命令中的`--target`参数，指向相应的测试目录。
3. 执行相应的测试命令，开始兼容性分析。
4. 查看工具输出的结果，确定JAR包集合的兼容性状态。

## 注意事项

- 替换测试命令中的`/Path/to/jar-compatibility-detector/test/`为实际的测试目录路径。
- 确保JRE版本与您的测试环境相匹配。
- 分析可能需要一些时间，具体取决于JAR包的数量和复杂性。
