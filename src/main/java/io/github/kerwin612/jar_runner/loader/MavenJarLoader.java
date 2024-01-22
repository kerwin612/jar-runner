package io.github.kerwin612.jar_runner.loader;

import org.apache.maven.cli.MavenCli;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public final class MavenJarLoader extends JarLoader {

    public Path doLoad(Path jarPath) throws IOException {
        JarFile jarFile = new JarFile(jarPath.toFile());
        Path tmpLoadedPath = Files.createTempDirectory("maven_jar_loading.");
        tmpLoadedPath.toFile().deleteOnExit();

        String rootPath = tmpLoadedPath.toFile().getCanonicalPath();
        File libsFile = tmpLoadedPath.resolve("libs").toFile();
        System.setProperty(MavenCli.MULTIMODULE_PROJECT_DIRECTORY, rootPath);

        List<JarEntry> collect = jarFile.stream().filter(e -> e.getName().endsWith("pom.xml")).collect(Collectors.toList());
        collect.forEach(e -> {
            try {
                Path pomPath = tmpLoadedPath.resolve("pom.xml");
                pomPath.toFile().deleteOnExit();
                Files.copy(jarFile.getInputStream(e), pomPath, StandardCopyOption.REPLACE_EXISTING);
                new MavenCli().doMain(new String[]{"org.apache.maven.plugins:maven-dependency-plugin:3.6.1:copy-dependencies", String.format("-DoutputDirectory=%s", libsFile.getCanonicalPath())}, rootPath, System.out, System.err);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        return libsFile.toPath();
    }

}
