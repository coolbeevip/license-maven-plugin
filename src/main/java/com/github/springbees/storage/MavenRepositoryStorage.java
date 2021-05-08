package com.github.springbees.storage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.springbees.pojo.DependencyEntry;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

public class MavenRepositoryStorage {

  final ObjectMapper jsonMapper = new ObjectMapper();
  DB db;
  ConcurrentMap<String,String> dependencies;
  private static MavenRepositoryStorage INSTANCE;

  public synchronized static MavenRepositoryStorage getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new MavenRepositoryStorage();
    }
    return INSTANCE;
  }

  public void put(DependencyEntry licenseEntry) throws JsonProcessingException {
    if (!exits(licenseEntry)) {
      dependencies.put(
        getKey(licenseEntry.getGroupId(), licenseEntry.getArtifactId(), licenseEntry.getVersion()),
        jsonMapper.writeValueAsString(licenseEntry));
    } else if (!licenseEntry.getLicense().isEmpty()) {
      dependencies.put(
        getKey(licenseEntry.getGroupId(), licenseEntry.getArtifactId(), licenseEntry.getVersion()),
        jsonMapper.writeValueAsString(licenseEntry));
    }
  }

  public DependencyEntry get(String groupId, String artifactId, String version)
    throws JsonProcessingException {
    String value = dependencies.get(getKey(groupId, artifactId, version));
    return jsonMapper.readValue(value, DependencyEntry.class);
  }

  public Stream<String> stream(){
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

  public void open(String file){
    db = DBMaker.fileDB(file).checksumHeaderBypass().make();
    dependencies = db.hashMap("dependencies")
      .keySerializer(Serializer.STRING)
      .valueSerializer(Serializer.STRING)
      .createOrOpen();
  }

  public void close(){
    db.commit();
    db.close();
  }
}
