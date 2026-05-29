# Using Episteme in your Project

Episteme is published to GitHub Packages. To use it, you need to configure your build tool to include the GitHub repository.

## 1. Repository Configuration

### Maven
Add this to your `pom.xml` or `settings.xml`:

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/episteme-hpc/Episteme</url>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>
```

> [!NOTE]
> To download packages from GitHub, you may need to authenticate with a Personal Access Token (PAT) in your `settings.xml`.

### Gradle
Add this to your `build.gradle`:

```gradle
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/episteme-hpc/Episteme")
        credentials {
            username = project.findProperty("gpr.user") ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.key") ?: System.getenv("GITHUB_TOKEN")
        }
    }
}
```

## 2. Dependencies

### Maven
```xml
<dependency>
    <groupId>org.episteme</groupId>
    <artifactId>episteme-core</artifactId>
    <version>1.0.0-beta1</version>
    <!-- Optional: Multi-JDK classifiers -->
    <classifier>jdk25</classifier> 
</dependency>
```

### Gradle
```gradle
implementation 'org.episteme:episteme-core:1.0.0-beta1'
// Or with classifier
implementation 'org.episteme:episteme-core:1.0.0-beta1:jdk25'
```

## 3. Thin vs Fat JARs

- **Thin JAR** (default): Only contains the Episteme code. You must manage dependencies yourself. This is the recommended way for library usage.
- **Fat JAR** (classifier: `fat`): Contains Episteme and all its dependencies bundled together. Use this for standalone execution (e.g., the Worker or Server) without worrying about the classpath.

### Consuming the Fat JAR (CLI)
```bash
java -jar episteme-worker-1.0.0-beta1-fat.jar
```

