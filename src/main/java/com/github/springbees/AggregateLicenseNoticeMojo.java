package com.github.springbees;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;


/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@Mojo(name = "dependency-license-notice", defaultPhase = LifecyclePhase.VERIFY, threadSafe = true, aggregator = true)
public class AggregateLicenseNoticeMojo
    extends AbstractMojo {

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

  Parse parse = new ParseMvnRepository();

  List<LicensesRepository> licensesRepositories = new ArrayList<>();

  public void execute() {
    List<String> notices = new ArrayList<>();
    Path path = Paths.get(projectBuildDirectory + "/distribute");
    reactorProjects.stream().forEach(project -> {
      List<Dependency> dependencies = project.getDependencies();
      dependencies.parallelStream()
          .filter(d -> !project.getGroupId().equals(d.getGroupId()))
          .filter(d -> scope == null || scope.isEmpty() || scope.equals(d.getScope()))
          .forEach(dependency -> {
            getLog().info(
                "NOTICE parse " + dependency.getGroupId() + ":" + dependency.getArtifactId() + ":"
                    + dependency.getVersion());
            LicensesRepository licensesRepository = parse
                .parseLicense(dependency.getGroupId(), dependency.getArtifactId(),
                    dependency.getVersion());
            licensesRepositories.add(licensesRepository);
          });
    });
    licensesRepositories.stream().forEach(lic -> lic.saveNotices(notices));
    if(!Files.exists(path)){
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
  }

//  protected MavenSession session;
//  protected void registerEventMonitor()
//  {
//    session.getEventDispatcher().addEventMonitor(
//        new EventMonitor() {
//
//          @Override
//          public void endEvent(String eventName, String target, long arg2) {
//            if (eventName.equals("reactor-execute"))
//              printSummary();
//          }
//
//          @Override
//          public void startEvent(String eventName, String target, long arg2) {}
//
//          @Override
//          public void errorEvent(String eventName, String target, long arg2, Throwable arg3) {}
//
//
//        }
//    );
//  }
//
//  protected void printSummary(){
//    getLog().info("summary======="+note.size());
//  }
}
