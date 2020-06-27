package com.github.springbees;

public interface Parse {

  LicensesRepository parseLicense(String groupId, String artifactId, String version);
}
