# 生成 Maven 项目依赖报告

支持生成 CSV，NOTICE 格式的依赖报告，并尝试分析依赖的 LICENSE 信息

## 1. 导出依赖报告（不分析 LICENSE 信息）

> 在你的项目根目录下执行

#### 1.1 导出 CSV 格式

```
./mvnw io.github.coolbeevip:license-maven-plugin:1.2.0:dependency-license-export -Dformat=csv
```

生成 [NOTICE.CSV](samples/NOTICE.CSV) 文件样例 

#### 1.2 导出 TXT 格式

```
./mvnw io.github.coolbeevip:license-maven-plugin:1.2.0:dependency-license-export -Dformat=txt
```

生成 [NOTICE.TXT](samples/NOTICE.TXT) 文件

## 2. 导出依赖报告（分析 LICENSE 信息）

此项目使用 [selenium](https://github.com/SeleniumHQ/selenium) 从 [Maven Central Repository](https://search.maven.org/) 分析依赖的 License

在 MacOS 下安装 selenium 支持，更对支持参见[ChromeDriver](https://github.com/SeleniumHQ/selenium/wiki/ChromeDriver)

```
brew install --cask chromedriver
```

#### 2.1 开启导出参数

只需要在导出依赖报告时增加 `-Dlicense=true` 参数即可，[NOTICE-LICENSE.CSV](samples/NOTICE-LICENSE.CSV) ，[NOTICE-LICENSE.TXT](samples/NOTICE-LICENSE.TXT)

