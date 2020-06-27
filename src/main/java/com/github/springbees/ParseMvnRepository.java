package com.github.springbees;

import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class ParseMvnRepository implements Parse {

  final String baseUrl = "https://mvnrepository.com/artifact/";

  public LicensesRepository parseLicense(String groupId, String artifactId, String version) {
    LicensesRepository licensesRepository = new LicensesRepository();
    String url =
        baseUrl + groupId + "/" + artifactId + "/" + version;
    try {
      Document doc = Jsoup.connect(url)
          .timeout(4000)
          .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
          .get();
      Elements licensesBody = doc.select(".version-section h2:contains(Licenses)").next()
          .select("tbody tr");
      String homePage = doc.select("#maincontent table tbody tr th:contains(HomePage)").next()
          .select("a").attr("href");
      String organization = doc.select("#maincontent table tbody tr th:contains(Organization)").next()
          .select("a").text();
      String organizationUrl = doc.select("#maincontent table tbody tr th:contains(Organization)").next()
          .select("a").attr("href");
      licensesBody.forEach(td -> {
        try {
          String licenseName = td.select("td").first().text();
          String licenseUrl = td.select("td a").attr("href");
          LicenseEntry element = new LicenseEntry();
          element.setGroupId(groupId);
          element.setArtifactId(artifactId);
          element.setVersion(version);
          element.setOrganization(organization);
          element.setOrganizationUrl(organizationUrl);
          element.setHomePage(homePage);
          element.addLicense(licenseName, licenseUrl);
          licensesRepository.add(element);
        } catch (Exception e) {
          throw new RuntimeException(groupId + ":" + artifactId + ":" + version + " fail", e);
        }
      });
    } catch (IOException e) {
      throw new RuntimeException(url + " parse fail", e);
    }
    return licensesRepository;
  }
}