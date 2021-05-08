package com.github.springbees;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

@Mojo(name = "dependency-license-notice", defaultPhase = LifecyclePhase.VERIFY, threadSafe = true, aggregator = true)
public class AggregateLicenseNoticeMojo extends AbstractMojo {

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

  Parse parse = new ParseSonaTypeRepository();

  @Override
  public void execute() {
    if (dbfile == null) {
      dbfile = "mvnrepository.mapdb";
    }
    parse.open(dbfile);
    try {
      List<String> notices = new ArrayList<>();
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

      MavenRepositoryStore store = MavenRepositoryStore.getInstance();
      store.stream().sorted().map(v -> {
        try {
          return jsonMapper.readValue(v, LicenseEntry.class);
        } catch (JsonProcessingException e) {
          return null;
        }
      }).forEach(entry -> {
        if (entry != null) {
          notices.add("===========================================================================");
          notices.add("Includes content from " + (entry.getOrganization().trim().length() == 0 ? entry.getGroupId() : entry.getOrganization()));
          notices.add(entry.getOrganizationUrl().trim().length() == 0 ? entry.getHomePage() : entry.getOrganizationUrl());
          String lic = entry.getLicense().entrySet().stream().map(entry1 -> entry1.getKey() + " (" + entry1.getValue() + ")").collect(Collectors.joining(","));
          notices.add("* " + entry.getArtifactId() + ", Version " + entry.getVersion() + " (" + entry.getHomePage() + ") under " + lic);
          notices.add("");
          getLog().info("license:"+entry.getGroupId() + ":" + entry.getArtifactId() + ":" + entry.getVersion());
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
        new FileWriter(path.toAbsolutePath() + "/NOTICE"))) {
        for (String l : notices) {
          writer.write(l + "\r\n");
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      getLog().info("write " + path.toAbsolutePath() + "/NOTICE");
    } finally {
      parse.close();
    }
  }

}
