package com.github.springbees;

import java.nio.file.Path;
import java.util.List;
import org.apache.maven.plugin.logging.Log;

public interface DependencyExport {

  void export(Log log,List<String> notices, Path path);
}
