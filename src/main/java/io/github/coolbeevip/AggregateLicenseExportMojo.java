/**
 * Copyright © 2020 Lei Zhang (zhanglei@apache.org)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.coolbeevip;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.coolbeevip.exports.ExportDependencyToCsv;
import io.github.coolbeevip.exports.ExportDependencyToPom;
import io.github.coolbeevip.exports.ExportDependencyToTxt;
import io.github.coolbeevip.parse.ParseMavenCentralRepositorySearch;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilderException;
import org.apache.maven.shared.dependency.graph.DependencyNode;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Mojo(name = "dependency-license-export", defaultPhase = LifecyclePhase.VERIFY, threadSafe = true, aggregator = true)
public class AggregateLicenseExportMojo extends AbstractMojo {

  final static String CACHE_FILE_NAME = "mvnrepository.mapdb";

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

  @Parameter(property = "ignoreGroupIds")
  String ignoreGroupIds;

  @Parameter(property = "timeout", defaultValue = "60")
  int timeout;

  @Parameter(property = "deep", defaultValue = "100")
  int deep;

  DependencyParse parse;

  DependencyExport export;

  @Component
  private DependencyGraphBuilder dependencyGraphBuilder;
  ArtifactFilter scopeArtifactFilter = new ScopeArtifactFilter(Artifact.SCOPE_COMPILE);

  @Override
  public void execute() {
    if (format.equals("csv")) {
      getLog().info("Export Format CSV");
      export = new ExportDependencyToCsv(project);
    } else if (format.equals("txt")) {
      getLog().info("Export Format TXT");
      export = new ExportDependencyToTxt(project);
    } else if (format.equals("pom")) {
      getLog().info("Export Format POM");
      export = new ExportDependencyToPom(project);
    }

    getLog().info("Analyse License timeout " + this.timeout + " sec.");
    getLog().info("Analyse Dependency Level " + this.deep);

    List<String> ignoreGroupIdList = new ArrayList<>();
    if (ignoreGroupIds != null) {
      ignoreGroupIdList = Arrays.asList(ignoreGroupIds.split(","));
      getLog().info("Ignore GroupIds " + ignoreGroupIds);
    }

    String cache = System.getProperty("user.home") + File.separator + ".m2" + File.separator
        + CACHE_FILE_NAME;

    parse = new ParseMavenCentralRepositorySearch(Optional.of(getLog()), Boolean.valueOf(license),
        timeout);
    parse.open(cache);

    try {
      List<String> notices = new ArrayList<>();
      Path path = Paths.get(projectBuildDirectory + "/distribute");

      Map<String, Dependency> exportDependencies = new HashMap<>();
      List<String> finalIgnoreGroupIdList = ignoreGroupIdList;

      reactorProjects.stream().forEach(project -> {
        List<Dependency> dependencies = project.getDependencies();
        dependencies.stream()
            .filter(d -> !finalIgnoreGroupIdList.stream()
                .filter(groupId -> d.getGroupId().startsWith(groupId)).findAny().isPresent())
            .filter(d -> !project.getGroupId().equals(d.getGroupId()))
            .filter(d -> scope == null || scope.isEmpty() || scope.equals(d.getScope()))
            .filter(d -> !d.getScope().equals("provided"))
            .forEach(dependency -> {
              getLog().info("===> " + String.format("%s:%s:%s:%s", dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion(), dependency.getScope()));
              parse.parseLicense(dependency.getGroupId(),
                  dependency.getArtifactId(),
                  dependency.getVersion(),
                  dependency.getScope());

              String key = getKey(dependency.getGroupId(), dependency.getArtifactId(),
                  dependency.getVersion());
              if (!exportDependencies.containsKey(key)) {
                exportDependencies.put(key, dependency);
              }
            });

        // 本项目依赖的下级依赖
        try {
          dependencyGraphBuilder.buildDependencyGraph(project, scopeArtifactFilter)
              .getChildren().stream().forEach(dependencyNode -> {

                //  深度递归分析依赖
                List<DependencyNode> deepDependencyNodes = new ArrayList<>();
                recursionDependency(deep, dependencyNode.getChildren(), deepDependencyNodes);

                deepDependencyNodes.stream()
                    .filter(n -> !n.getArtifact().getScope().equals("provided"))
                    .forEach(node -> {
                  Dependency dependency = new Dependency();
                  dependency.setGroupId(node.getArtifact().getGroupId());
                  dependency.setArtifactId(node.getArtifact().getArtifactId());
                  dependency.setVersion(node.getArtifact().getVersion());
                  dependency.setScope(node.getArtifact().getScope());
                  parse.parseLicense(dependency.getGroupId(),
                      dependency.getArtifactId(),
                      dependency.getVersion(),
                      dependency.getScope());
                  String key = getKey(dependency.getGroupId(), dependency.getArtifactId(),
                      dependency.getVersion());
                  if (!exportDependencies.containsKey(key)) {
                    getLog().info(">>> " + key);
                    exportDependencies.put(key, dependency);
                  }
                });
              });
        } catch (DependencyGraphBuilderException e) {
          e.printStackTrace();
        }
      });
      getLog().info(">>> Size " + exportDependencies.size());
      export.export(exportDependencies, getLog(), notices, path);
    } finally {
      parse.close();
    }
  }

  private String getKey(String groupId, String artifactId, String version) {
    return groupId + ":" + artifactId + ":" + version;
  }

  private void recursionDependency(int deep, List<DependencyNode> dependencyNodes,
                                   List<DependencyNode> deepDependencyNodes) {
    if (deep > 0) {
      int finalDeep = deep - 1;
      dependencyNodes.forEach(dependencyNode -> {
        deepDependencyNodes.addAll(dependencyNode.getChildren());
        recursionDependency(finalDeep, dependencyNode.getChildren(), deepDependencyNodes);
      });
    }
  }
}
