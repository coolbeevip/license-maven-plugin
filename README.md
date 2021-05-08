# Maven 项目依赖报告

支持生成 CSV，NOTICE 格式的依赖报告

## 导出依赖报告

> 在你的项目根目录下执行

* format 导出格式 csv,notice
* dbfile 缓存文件

导出 CSV 格式

```
./mvnw clean com.github.spring-bees:license-maven-plugin:1.0.0-SNAPSHOT:dependency-license-export -Dformat=csv -Ddbfile=/Users/zhanglei/github/license-maven-plugin/db/mvnrepository.mapdb
```

导出 NOTICE 格式

```
./mvnw clean com.github.spring-bees:license-maven-plugin:1.0.0-SNAPSHOT:dependency-license-export -Dformat=notice -Ddbfile=/Users/zhanglei/github/license-maven-plugin/db/mvnrepository.mapdb
```

## 导出依赖报告(含 License 信息)

此项目使用 [selenium](https://github.com/SeleniumHQ/selenium) 从 [Maven Central Repository](https://search.maven.org/) 分析依赖的 License

安装 selenium 支持

```
brew install --cask chromedriver
```

导出带 LICENSE 信息的 CSV 格式

```
./mvnw clean com.github.spring-bees:license-maven-plugin:1.0.0-SNAPSHOT:dependency-license-export -Dformat=csv -Ddbfile=/Users/zhanglei/github/license-maven-plugin/db/mvnrepository.mapdb
```


