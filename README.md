# 生成 Maven 项目依赖报告
[![Apache License 2](https://img.shields.io/badge/license-ASF2-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.txt)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.coolbeevip/license-maven-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.coolbeevip/license-maven-plugin/)

支持生成 CSV, NOTICE, POM 格式的依赖报告，并尝试分析依赖的 LICENSE 信息

导出 CSV 格式

```shell
mvn io.github.coolbeevip:license-maven-plugin:1.13.0:dependency-license-export -Dformat=csv
```

导出 TXT 格式

```shell
mvn io.github.coolbeevip:license-maven-plugin:1.13.0:dependency-license-export -Dformat=txt
```

导出 POM 格式

```shell
mvn io.github.coolbeevip:license-maven-plugin:1.13.0:dependency-license-export -Dformat=pom
```

导出时忽略本部分依赖

```shell
mvn io.github.coolbeevip:license-maven-plugin:1.13.0:dependency-license-export -Dformat=csv -DignoreGroupIds=org.my
```

导出时尝试分析依赖 License

```shell script
mvn io.github.coolbeevip:license-maven-plugin:1.13.0:dependency-license-export -Dformat=csv -Dlicense=true
```

[more](https://coolbeevip.github.io/posts/maven/maven-export-dependencies-analyse-license/)
