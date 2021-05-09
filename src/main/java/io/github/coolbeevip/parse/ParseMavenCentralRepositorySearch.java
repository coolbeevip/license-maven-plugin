/**
 * Copyright © 2020 Lei Zhang (zhanglei@apache.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.coolbeevip.parse;

import io.github.coolbeevip.DependencyParse;
import io.github.coolbeevip.pojo.DependencyEntry;
import io.github.coolbeevip.storage.MavenRepositoryStorage;
import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.apache.maven.plugin.logging.Log;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * @author zhanglei
 */
public class ParseMavenCentralRepositorySearch implements DependencyParse {

  final MavenRepositoryStorage store;
  final WebDriver driver;
  final SecureRandom random = new SecureRandom();
  final Optional<Log> log;
  final String baseUrl = "https://search.maven.org/artifact/";
  final Boolean licenseEnabled;

  public ParseMavenCentralRepositorySearch(Optional<Log> log, Boolean license) {
    this.log = log;
    this.store = MavenRepositoryStorage.getInstance(log);
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
      this.driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
    } else {
      this.driver = null;
    }
  }

  @Override
  public MavenRepositoryStorage parseLicense(String groupId, String artifactId,
      String version) {

    if (!store.exits(groupId, artifactId, version)) {
      DependencyEntry dependencyEntry = new DependencyEntry();
      dependencyEntry.setGroupId(groupId);
      dependencyEntry.setArtifactId(artifactId);
      dependencyEntry.setVersion(version);

      String url = baseUrl + groupId + "/" + artifactId + "/" + version + "/jar";
      String organization = "";
      String organizationUrl = "";
      String homePage = "";
      String licenseName = "";
      String licenseUrl = "";

      try {
        if (licenseEnabled) {
          driver.get(url);
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
          }
        }
        dependencyEntry.setOrganization(organization);
        dependencyEntry.setOrganizationUrl(organizationUrl);
        dependencyEntry.setHomePage(homePage);
        dependencyEntry.addLicense(licenseName, licenseUrl);
        store.put(dependencyEntry);
      } catch (Exception ex) {
        ex.printStackTrace();
      }

      try {
        TimeUnit.MILLISECONDS.sleep(random.nextInt(500));
      } catch (InterruptedException e) {
        e.printStackTrace();
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
