package com.github.springbees;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.springbees.parse.ParseMavenCentralRepositorySearch;
import com.github.springbees.pojo.DependencyEntry;
import com.github.springbees.storage.MavenRepositoryStorage;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name = "dependency-license-csv", defaultPhase = LifecyclePhase.VERIFY, threadSafe = true, aggregator = true)
public class AggregateLicenseCsvMojo extends AbstractMojo {

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

  @Parameter(property = "dbfile")
  String dbfile;

  private List<String> ignoreGroupIds = new ArrayList<>();

  Parse parse = new ParseMavenCentralRepositorySearch();

  @Override
  public void execute() {
    if (dbfile == null) {
      dbfile = "mvnrepository.mapdb";
    }
    getLog().info("db file: " + dbfile);
    parse.open(dbfile);
    try {
      List<String> notices = new ArrayList<>();
      notices.add("groupId\tartifactId\tversion\tlicense\torganization\turl");
      Path path = Paths.get(projectBuildDirectory + "/distribute");
      reactorProjects.stream().forEach(project -> {
        List<Dependency> dependencies = project.getDependencies();
        dependencies.stream()
          .filter(d -> !project.getGroupId().equals(d.getGroupId()))
          .filter(d -> scope == null || scope.isEmpty() || scope.equals(d.getScope()))
          .forEach(dependency -> {
            parse.parseLicense(Optional.of(getLog()), dependency.getGroupId(),
              dependency.getArtifactId(),
              dependency.getVersion());
          });
      });

      MavenRepositoryStorage store = MavenRepositoryStorage.getInstance();
      store.stream().sorted().map(v -> {
        try {
          return jsonMapper.readValue(v, DependencyEntry.class);
        } catch (JsonProcessingException e) {
          return null;
        }
      }).forEach(entry -> {
        if (entry != null) {
          String line = String
            .format("%s\t%s\t%s\t%s\t%s\t%s", entry.getGroupId(), entry.getArtifactId(),
              entry.getVersion(),
              entry.getLicense().keySet().stream().collect(Collectors.joining("/")),
              entry.getOrganization(),
              entry.getHomePage());
          notices.add(line);
          getLog().info(line);
        }
      });

      if (!Files.exists(path)) {
        try {
          Files.createDirectories(path);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      try (BufferedWriter writer = new BufferedWriter(
        new FileWriter(path.toAbsolutePath() + "/NOTICE.CSV"))) {
        for (String l : notices) {
          writer.write(l + "\r\n");
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      getLog().info("write " + path.toAbsolutePath() + "/NOTICE.CSV");
    } finally {
      parse.close();
    }
  }

}
