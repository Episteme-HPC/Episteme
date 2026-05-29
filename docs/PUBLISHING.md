# 🚀 Publishing Episteme to GitHub Packages

This guide explains how to publish the Episteme framework to the GitHub Maven Registry.

## 🛠️ Prerequisites

1.  **GitHub Personal Access Token (PAT)**:
    - Go to **Settings** > **Developer settings** > **Personal access tokens** > **Tokens (classic)**.
    - Generate a new token with at least `write:packages` and `read:packages` scopes.
    - **Save this token securely**; you won't be able to see it again.

---

## 🤖 Option A: Automated Release (Recommended)

The project is configured with a GitHub Action (`release.yml`) that automates the entire process.

### 1. Trigger via Tag (Manual Push)
Pushing a version tag will automatically trigger a build and publication of both JDK 21 and JDK 25 versions.

```bash
git tag v1.0.0-beta2
git push origin v1.0.0-beta2
```

### 2. Trigger via GitHub UI
1. Go to the **Actions** tab in your repository.
2. Select the **Release** workflow on the left.
3. Click **Run workflow**.
4. (Optional) Specify a version if you want to override the one in `pom.xml`.

---

## 💻 Option B: Manual Publication (Local Machine)

If you need to publish manually from your development environment:

### 1. Configure `settings.xml`
Add the following to your `~/.m2/settings.xml` file (create it if it doesn't exist):

```xml
<settings>
  <servers>
    <server>
      <id>github</id>
      <username>YOUR_GITHUB_USERNAME</username>
      <password>YOUR_PERSONAL_ACCESS_TOKEN</password>
    </server>
  </servers>
</settings>
```

### 2. Run Deploy Command
Run the following command from the project root:

```bash
# To publish the JDK 25 version with fat jars
mvn clean deploy -DskipTests -Prelease-jdk25,fat-jar

# To publish the JDK 21 version
mvn clean deploy -DskipTests -Prelease-jdk21,fat-jar
```

---

## 📦 Consuming the Package

To use Episteme in another project, add the following to its `pom.xml`:

### 1. Add Repository
```xml
<repositories>
    <repository>
        <id>github</id>
        <name>GitHub episteme-hpc Apache Maven Packages</name>
        <url>https://maven.pkg.github.com/episteme-hpc/Episteme</url>
    </repository>
</repositories>
```

### 2. Add Dependency
```xml
<dependency>
    <groupId>io.github.episteme-hpc</groupId>
    <artifactId>episteme-core</artifactId>
    <version>1.0.0-beta2</version>
</dependency>
```

> [!IMPORTANT]
> Since this is a private/internal registry, consumers also need to have their `settings.xml` configured with a PAT (even if just for `read:packages`).

