package io.github.kerwin612.jar_runner.loader;


import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public abstract class JarLoader {

    public URL[] load(Path jarPath) throws IOException {
        return load(jarPath, null, false);
    }

    public URL[] load(Path jarPath, Path loadedPath) throws IOException {
        return load(jarPath, loadedPath, false);
    }

    public URL[] load(Path jarPath, boolean override) throws IOException {
        return load(jarPath, null, override);
    }

    public URL[] load(Path jarPath, Path loadedPath, boolean override) throws IOException {
        URL jarURL = jarPath.toUri().toURL();

        if (loadedPath == null) {
            loadedPath = Paths.get(System.getProperty("java.io.tmpdir"), String.format("%s%s%s", "jar_runner.", md5(jarPath), ".cache"));
        } else if (override) {
            Files.deleteIfExists(loadedPath);
        }

        if (!Files.exists(loadedPath) || override) {
            Files.move(doLoad(jarPath), loadedPath, StandardCopyOption.REPLACE_EXISTING);
        }

        File[] libs = loadedPath.toFile().listFiles();
        URL[] urls = new URL[libs.length + 1];
        for (int i = 0, j = libs.length; i < j; i++) {
            urls[i] = libs[i].toURI().toURL();
        }

        urls[urls.length - 1] = jarURL;
        return urls;
    }

    abstract Path doLoad(Path jarPath) throws IOException;

    private static String md5(Path filePath) throws IOException {
        try {
            return new BigInteger(1, MessageDigest.getInstance("MD5").digest(Files.readAllBytes(filePath))).toString(16);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
