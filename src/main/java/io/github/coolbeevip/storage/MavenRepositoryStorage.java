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

package io.github.coolbeevip.storage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.coolbeevip.pojo.DependencyEntry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;
import org.apache.maven.plugin.logging.Log;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

public class MavenRepositoryStorage {

  private static MavenRepositoryStorage INSTANCE;
  final ObjectMapper jsonMapper = new ObjectMapper();
  final Optional<Log> log;
  DB db;
  ConcurrentMap<String, String> dependencies = new ConcurrentHashMap<>();

  public MavenRepositoryStorage(Optional<Log> log) {
    this.log = log;
  }

  public synchronized static MavenRepositoryStorage getInstance(Optional<Log> log) {
    if (INSTANCE == null) {
      INSTANCE = new MavenRepositoryStorage(log);
    }
    return INSTANCE;
  }

  public synchronized static MavenRepositoryStorage getInstance() {
    return INSTANCE;
  }

  public void put(DependencyEntry licenseEntry) throws JsonProcessingException {
    if (!exits(licenseEntry)) {
      dependencies.put(
          getKey(licenseEntry.getGroupId(), licenseEntry.getArtifactId(),
              licenseEntry.getVersion()),
          jsonMapper.writeValueAsString(licenseEntry));
    } else if (!licenseEntry.getLicense().isEmpty()) {
      dependencies.put(
          getKey(licenseEntry.getGroupId(), licenseEntry.getArtifactId(),
              licenseEntry.getVersion()),
          jsonMapper.writeValueAsString(licenseEntry));
    }
  }

  public DependencyEntry get(String groupId, String artifactId, String version)
      throws JsonProcessingException {
    String value = dependencies.get(getKey(groupId, artifactId, version));
    return jsonMapper.readValue(value, DependencyEntry.class);
  }

  public Stream<String> stream() {
    return dependencies.values().stream();
  }

  public boolean exits(String groupId, String artifactId, String version) {
    return dependencies.containsKey(getKey(groupId, artifactId, version));
  }

  private boolean exits(DependencyEntry licenseEntry) {
    return dependencies.containsKey(
        getKey(licenseEntry.getGroupId(), licenseEntry.getArtifactId(), licenseEntry.getVersion()));
  }

  private String getKey(String groupId, String artifactId, String version) {
    return groupId + ":" + artifactId + ":" + version;
  }

  public void open(String file) {
    log.ifPresent(l -> l.info("Use Cache File " + file));
    db = DBMaker.fileDB(file).checksumHeaderBypass().make();
    dependencies = db.hashMap("dependencies")
        .keySerializer(Serializer.STRING)
        .valueSerializer(Serializer.STRING)
        .createOrOpen();
  }

  public void close() {
    db.commit();
    db.close();
  }
}
