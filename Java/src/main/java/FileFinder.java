import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class FileFinder {

    public static File findMyFile(String fileNameToFind) throws IOException {

        File rootDirectory = new File(System.getProperty("user.dir"));
        final List<File> foundFiles = new ArrayList<>();
        try (Stream<Path> walkStream = Files.walk(rootDirectory.toPath())) {
            walkStream.filter(p -> p.toFile().isFile())
                    .forEach(f -> {
                        if (f.toString().endsWith(fileNameToFind)) {
                            foundFiles.add(f.toFile());
                        }
                    });
        }
        return foundFiles.getFirst();
    }


}
