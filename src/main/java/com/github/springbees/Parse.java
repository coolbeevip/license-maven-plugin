package com.github.springbees;

import java.util.Optional;
import org.apache.maven.plugin.logging.Log;

public interface Parse {

  LicensesRepository parseLicense(Optional<Log> log, String groupId, String artifactId, String version);
}
