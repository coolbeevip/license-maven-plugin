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

package io.github.coolbeevip;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.coolbeevip.exports.ExportDependencyToCsv;
import io.github.coolbeevip.exports.ExportDependencyToTxt;
import io.github.coolbeevip.parse.ParseMavenCentralRepositorySearch;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name = "dependency-license-export", defaultPhase = LifecyclePhase.VERIFY, threadSafe = true, aggregator = true)
public class AggregateLicenseExportMojo extends AbstractMojo {

  final ObjectMapper jsonMapper = new ObjectMapper();

  @Parameter(defaultValue = "${project}", required = true, readonly = true)
  MavenProject project;

  @Parameter(property = "project.build.directory")
  String projectBuildDirectory;

  @Parameter(property = "user.dir")
  String userDirectory;

  @Parameter(property = "scope")
  String scope;

  @Parameter(property = "reactorProjects", readonly = true, required = true)
  private List<MavenProject> reactorProjects;

  @Parameter(property = "format", defaultValue = "csv")
  String format;

  @Parameter(property = "license", defaultValue = "false")
  String license;

  private List<String> ignoreGroupIds = new ArrayList<>();

  DependencyParse parse;

  DependencyExport export;

  @Override
  public void execute() {
    if (format.equals("csv")) {
      getLog().info("Export Format CSV");
      export = new ExportDependencyToCsv();
    } else if (format.equals("txt")) {
      getLog().info("Export Format TXT");
      export = new ExportDependencyToTxt();
    }
    String cache = System.getProperty("user.home") + File.separator + ".m2" + File.separator
        + "mvnrepository.mapdb";
    parse = new ParseMavenCentralRepositorySearch(Optional.of(getLog()), Boolean.valueOf(license));
    parse.open(cache);
    try {
      List<String> notices = new ArrayList<>();
      Path path = Paths.get(projectBuildDirectory + "/distribute");
      reactorProjects.stream().forEach(project -> {
        List<Dependency> dependencies = project.getDependencies();
        dependencies.stream()
            .filter(d -> !project.getGroupId().equals(d.getGroupId()))
            .filter(d -> scope == null || scope.isEmpty() || scope.equals(d.getScope()))
            .forEach(dependency -> {
              parse.parseLicense(dependency.getGroupId(),
                  dependency.getArtifactId(),
                  dependency.getVersion());
            });
      });

      export.export(getLog(), notices, path);
    } finally {
      parse.close();
    }
  }

}
