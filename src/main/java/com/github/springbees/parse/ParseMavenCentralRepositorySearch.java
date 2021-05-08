package com.github.springbees.parse;

import com.github.springbees.storage.MavenRepositoryStorage;
import com.github.springbees.Parse;
import com.github.springbees.pojo.DependencyEntry;
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
public class ParseMavenCentralRepositorySearch implements Parse {

  final MavenRepositoryStorage store = MavenRepositoryStorage.getInstance();
  final WebDriver driver = new ChromeDriver(DesiredCapabilities.chrome());
  final SecureRandom random = new SecureRandom();

  final String baseUrl = "https://search.maven.org/artifact/";

  public ParseMavenCentralRepositorySearch() {
    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
    final SecureRandom random = new SecureRandom();
  }

  @Override
  public MavenRepositoryStorage parseLicense(Optional<Log> log, String groupId, String artifactId,
    String version) {

    if(!store.exits(groupId,artifactId,version)){
      DependencyEntry licenseEntry = new DependencyEntry();
      licenseEntry.setGroupId(groupId);
      licenseEntry.setArtifactId(artifactId);
      licenseEntry.setVersion(version);

      String url = baseUrl + groupId + "/" + artifactId + "/" + version + "/jar";
      String organization = "";
      String organizationUrl = "";
      String homePage = "";
      String licenseName = "";
      String licenseUrl = "";
      try{
        driver.get(url);
        WebElement webElement = driver.findElement(By.tagName("app-artifact-description"));
        if(webElement.isDisplayed()){
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

        licenseEntry.setOrganization(organization);
        licenseEntry.setOrganizationUrl(organizationUrl);
        licenseEntry.setHomePage(homePage);
        licenseEntry.addLicense(licenseName, licenseUrl);
        store.put(licenseEntry);
      }catch (Exception ex){
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
  public void close(){
    driver.close();
    store.close();
  }
}
