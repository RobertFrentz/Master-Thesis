package org.example;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class FileSystemMap<K, V> extends AbstractMap<K, V> {

    private final Path directory;

    public FileSystemMap(String directoryPath) throws IOException {
        this.directory = Paths.get(directoryPath);
        if (!Files.exists(directory)) {
            Files.createDirectory(directory);
        }
    }

    @Override
    public V get(Object key) {
        Path filePath = directory.resolve(key.toString());
        if (Files.exists(filePath)) {
            try {
                return readValueFromFile(filePath);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public V put(K key, V value) {
        Path filePath = directory.resolve(key.toString());
        try {
            V oldValue = null;
            if (Files.exists(filePath)) {
                oldValue = readValueFromFile(filePath);
            }
            writeValueToFile(filePath, value);
            return oldValue;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> entries = new HashSet<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path file : stream) {
                K key = (K) file.getFileName().toString();
                V value = readValueFromFile(file);
                entries.add(new SimpleEntry<>(key, value));
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return entries;
    }

    private V readValueFromFile(Path filePath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath.toFile()))) {
            return (V) ois.readObject();
        }
    }

    private void writeValueToFile(Path filePath, V value) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath.toFile()))) {
            oos.writeObject(value);
        }
    }
}

