package com.github.springbees;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class DependencyLicenseListMojoTest {

  Parse parse = new ParseMvnRepository();

  @Test
  public void licenseTest() {
    Parse parse = new ParseMvnRepository();
    LicensesRepository licensesRepository = parse
        .parseLicense("org.mockito", "mockito-core", "2.23.4");
    licensesRepository.getLicenseEntries().stream().forEach(entry -> {
      assertEquals(entry.toString(), "org.mockito:mockito-core:2.23.4[The MIT License]");
    });
  }

  @Test
  public void duplicateLicenseTest() {
    LicensesRepository licensesRepository = parse
        .parseLicense("mysql", "mysql-connector-java", "8.0.15");
    licensesRepository.getLicenseEntries().stream().forEach(entry -> {
      assertEquals(entry.toString(),
          "mysql:mysql-connector-java:8.0.15[The GNU General Public License, v2 with FOSS exception]");
    });
  }

  @Test
  public void downloadTest() {
    List<String> notices = new ArrayList<>();
    Path path = Paths.get("data/dependencies_license");
    Parse parse = new ParseMvnRepository();
    LicensesRepository licensesRepository = parse
        .parseLicense("org.mockito", "mockito-core", "2.23.4");
    licensesRepository.download(path,notices);
  }
}