package com.github.springbees;

import com.github.springbees.storage.MavenRepositoryStorage;
import java.util.Optional;
import org.apache.maven.plugin.logging.Log;

public interface Parse {

  void open(String dbfile);

  void close();

  MavenRepositoryStorage parseLicense(Optional<Log> log, String groupId, String artifactId,
    String version);

}
