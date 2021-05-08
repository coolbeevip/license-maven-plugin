package com.github.springbees;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class DependencyLicenseListMojoTest {

  final static Parse parse = new ParseSonaTypeRepository();
  final static MavenRepositoryStore store = MavenRepositoryStore.getInstance();

  @Test
  public void licenseTest() throws JsonProcessingException {
    parse.parseLicense(Optional.empty(), "org.mockito", "mockito-core", "2.23.4");
    LicenseEntry entry = store.get("org.mockito", "mockito-core", "2.23.4");
    assertEquals(entry.toString(), "org.mockito:mockito-core:2.23.4[The MIT License]");
  }

  @Test
  public void duplicateLicenseTest() throws JsonProcessingException {
    parse.parseLicense(Optional.empty(), "mysql", "mysql-connector-java", "8.0.15");
    LicenseEntry entry = store.get("mysql", "mysql-connector-java", "8.0.15");
    assertEquals(entry.toString(),
      "mysql:mysql-connector-java:8.0.15[The GNU General Public License, v2 with FOSS exception]");
  }

  @BeforeClass
  public static void beforeClass() {
    parse.open("mvnrepository.test.mapdb");
  }

  @AfterClass
  public static void afterClass() {
    parse.close();
  }
}
