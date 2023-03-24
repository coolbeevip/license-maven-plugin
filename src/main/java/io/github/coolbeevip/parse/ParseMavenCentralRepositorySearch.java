/**
 * Copyright © 2020 Lei Zhang (zhanglei@apache.org)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.coolbeevip.parse;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.coolbeevip.DependencyParse;
import io.github.coolbeevip.pojo.DependencyEntry;
import io.github.coolbeevip.storage.MavenRepositoryStorage;
import org.apache.maven.plugin.logging.Log;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author zhanglei
 */
public class ParseMavenCentralRepositorySearch implements DependencyParse {

  final MavenRepositoryStorage store;
  final WebDriver driver;
  final SecureRandom random = new SecureRandom();
  final Optional<Log> log;
  public static final String baseUrl = "https://central.sonatype.com/artifact/";
  final Boolean licenseEnabled;
  final Set<String> skips = new HashSet<>();
  final int timeout;

  public ParseMavenCentralRepositorySearch(Optional<Log> log, Boolean license, int timeout) {
    this.log = log;
    this.store = MavenRepositoryStorage.getInstance(log);
    this.timeout = timeout;
    this.licenseEnabled = license;
    log.ifPresent(l -> {
      if (this.licenseEnabled) {
        l.info("License Analyse Enabled");
      } else {
        l.info("License Analyse Disabled");
      }
    });
    if (this.licenseEnabled) {
      this.driver = new ChromeDriver(DesiredCapabilities.chrome());
      this.driver.manage().timeouts().implicitlyWait(timeout, TimeUnit.SECONDS);
    } else {
      this.driver = null;
    }
  }

  @Override
  public MavenRepositoryStorage parseLicense(String groupId, String artifactId,
                                             String version, String scope) {
    DependencyEntry cacheDependencyEntry = null;
    if (store.exits(groupId, artifactId, version)) {
      try {
        DependencyEntry dependencyEntry = store.get(groupId, artifactId, version);
        if (!dependencyEntry.getLicense().isEmpty()) {
          cacheDependencyEntry = dependencyEntry;
        }
      } catch (JsonProcessingException e) {
        if (log.isPresent()) {
          log.get().error(e.getMessage(), e);
        }
      }
    }
    if (cacheDependencyEntry == null) {
      DependencyEntry dependencyEntry = new DependencyEntry();
      dependencyEntry.setGroupId(groupId);
      dependencyEntry.setArtifactId(artifactId);
      dependencyEntry.setVersion(version);
      dependencyEntry.setScope(scope);

      String skipKey = dependencyEntry.getGroupId() + ":" + dependencyEntry.getArtifactId() + ":" + dependencyEntry.getVersion();
      if (!skips.contains(skipKey)) {
        if (log.isPresent()) {
          String info = String.format("Analyse %s:%s:%s:%s",
              dependencyEntry.getGroupId(),
              dependencyEntry.getArtifactId(),
              dependencyEntry.getVersion(),
              dependencyEntry.getScope());
          log.get().info(info);
        }

        String url = baseUrl + groupId + "/" + artifactId + "/" + version + "/jar";
        String organization = "";
        String organizationUrl = "";
        String homePage = "";
        String licenseName = null;
        String licenseUrl = null;

        try {
          if (licenseEnabled) {
            driver.get(url);
            WebElement licenseElement = driver.findElement(By.xpath("//li[@data-test=\"license\"]"));
            if (licenseElement != null) {
              licenseName = licenseElement.getText();
            }
            WebElement projectElement = driver.findElement(By.xpath("//a[@data-test=\"project-url\"]"));
            if (projectElement != null) {
              homePage = projectElement.getAttribute("href");
            }
            WebElement organizationElement = driver.findElement(By.xpath("//label[@data-test=\"component-namespace\"]/span"));
            if (organizationElement != null) {
              organization = organizationElement.getText();
            }

            /*
            WebElement webElement = driver.findElement(By.tagName("app-artifact-description"));
            if (webElement.isDisplayed()) {
              List<WebElement> trList = webElement.findElements(By.tagName("tr"));
              for (WebElement tr : trList) {
                WebElement th = tr.findElement(By.tagName("th"));
                WebElement td = tr.findElement(By.tagName("td"));
                if (th.getText().startsWith("Organization")) {
                  organization = td.getText();
                } else if (th.getText().startsWith("Home page")) {
                  homePage = td.getText();
                } else if (th.getText().startsWith("Source code")) {
                  organizationUrl = td.getText();
                  licenseUrl = td.getText();
                } else if (th.getText().startsWith("Licenses")) {
                  licenseName = td.getText();
                }
              }
            }*/
            try {
              TimeUnit.MILLISECONDS.sleep(random.nextInt(500));
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          }
          dependencyEntry.setOrganization(organization);
          dependencyEntry.setOrganizationUrl(organizationUrl);
          dependencyEntry.setHomePage(homePage);
          if (licenseName != null) {
            dependencyEntry.addLicense(licenseName, licenseUrl);
          }

          store.put(dependencyEntry);
          if (log.isPresent()) {
            String info = String.format("Analyse %s:%s:%s[%s]",
                dependencyEntry.getGroupId(),
                dependencyEntry.getArtifactId(),
                dependencyEntry.getVersion(),
                dependencyEntry.getLicense().keySet().stream().collect(Collectors.joining(",")));
            log.get().info(info);
          }
        } catch (Exception ex) {
          if (log.isPresent()) {
            String info = String.format("Analyse Failed %s:%s:%s",
                dependencyEntry.getGroupId(),
                dependencyEntry.getArtifactId(),
                dependencyEntry.getVersion());
            log.get().info(info);
            skipKey = dependencyEntry.getGroupId() + ":" + dependencyEntry.getArtifactId() + ":" + dependencyEntry.getVersion();
            skips.add(skipKey);
          }
        }
      }
    } else {
      if (log.isPresent()) {
        String info = String.format("Cache Hint %s:%s:%s[%s]",
            cacheDependencyEntry.getGroupId(),
            cacheDependencyEntry.getArtifactId(),
            cacheDependencyEntry.getVersion(),
            cacheDependencyEntry.getLicense().keySet().stream().collect(Collectors.joining(",")));
        log.get().info(info);
      }
    }
    return store;
  }

  @Override
  public void open(String dbfile) {
    store.open(dbfile);
  }

  @Override
  public void close() {
    if (driver != null) {
      driver.close();
    }
    store.close();
  }
}
