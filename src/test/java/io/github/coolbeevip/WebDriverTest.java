package io.github.coolbeevip;

import io.github.coolbeevip.parse.ParseMavenCentralRepositorySearch;
import org.hamcrest.CoreMatchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;

public class WebDriverTest {

  static WebDriver driver;

  @BeforeClass
  public static void setupClass() {
    driver = new ChromeDriver(DesiredCapabilities.chrome());
    driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
  }

  @AfterClass
  public static void tearDownClass() {
    driver.close();
  }

  @Test
  public void test() {
    String url = ParseMavenCentralRepositorySearch.baseUrl + "org.apache.commons/commons-csv/1.10.0/jar";
    driver.get(url);
    WebElement licenseElement = driver.findElement(By.xpath("//li[@data-test=\"license\"]"));
    assertThat(licenseElement.getText(), CoreMatchers.is("Apache-2.0"));

    WebElement projectElement = driver.findElement(By.xpath("//a[@data-test=\"project-url\"]"));
    assertThat(projectElement.getAttribute("href"), CoreMatchers.is("https://commons.apache.org/proper/commons-csv/"));

    WebElement organizationElement = driver.findElement(By.xpath("//label[@data-test=\"component-namespace\"]/span"));
    assertThat(organizationElement.getText(), CoreMatchers.is("org.apache.commons"));
  }
}
