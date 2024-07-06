# Jar兼容性检测工具

这是一个用于检测Java Archive (JAR) 包之间兼容性的工具。它可以帮助开发者分析在不同JAR包集合中评估不同JAR包组合的兼容性。

## 功能特点

- 分析指定JAR包或目录下所有JAR包中的**类方法调用**，检测方法调用中是否存在未定义的被调用者。
- 分析指定JAR包或目录下所有JAR包中的**类字段引用**，检测字段引用中是否存在未定义的被引用字段。
- 分析指定JAR包或目录下所有JAR包中的**注解引用**，检测注解引用中是否存在未定义的被引用注解。
- 支持多种JRE版本，确保分析的准确性。

## 使用方法

要使用此工具，您需要在命令行中运行以下命令，并传入相应的参数（注意，运行目录需要有JREs，以提高检测准确性！）：

```bash
java -jar CompatibilityDetector-1.0-SNAPSHOT.jar --target <path> --javaVersion <version> --package <package>
```

### 参数说明

- `--target <path>`: 指定需要分析的JAR包或包含JAR包的目录的路径。
- `--javaVersion <version>`: 指定工具使用的JRE版本。可取值为3、4、5、6、7、8。
- `--package <package>`: 指定需要特别关注的目标包。例如，如果您想检测`logback`的兼容性，可以使用`--package ch.qos.logback`。
- `--jar <jar>`: 指定需要特别关注的目标jar。例如，如果您想检测`logback.jar`的兼容性，可以使用`--jar /path/to/jars/logback.jar`。

## 示例

假设您想检测位于`/path/to/jars`目录下所有JAR包的兼容性，并且特别关注`logback`包，您可以使用以下命令：

```bash
java -jar CompatibilityDetector.jar --target /path/to/jars --javaVersion 8 --package ch.qos.logback
```
or
```bash
java -jar CompatibilityDetector.jar --target /path/to/jars --javaVersion 8 --jar /path/to/jars/logback.jar
```

## 兼容性分析

工具将分析所有指定JAR包中的类方法调用，并报告任何不兼容的情况，例如调用了不存在的方法或类。

## 注意事项

- 确保指定的JAR包或目录路径正确无误。
- 根据需要选择合适的JRE版本进行分析。
- 工具可能需要一些时间来完成分析，具体取决于JAR包的数量和大小。

## 许可

本工具遵循[MIT许可证](LICENSE)。

## 贡献

如果您有任何建议或想要贡献代码，请提交Pull Request或创建Issue。

## 联系我们

如有任何问题或需要帮助，请通过[电子邮件](mailto:your.email@example.com)与我们联系。

---
**Jar兼容性检测工具** - 确保您的Java项目兼容性无误
```

请根据您的实际工具名称和细节，适当修改上述模板中的占位符和示例命令。如果需要包含许可证文件，请确保在项目根目录下创建一个名为`LICENSE`的文件，并填入相应的许可证文本。