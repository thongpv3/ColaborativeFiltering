import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Created by thongpv87 on 10/04/2017.
 */

public class DataSet<T> {
    private ArrayList<T> dataSet;

    public DataSet() {
        dataSet = new ArrayList<T>();
    }

    public void add(T data) {
        dataSet.add(data);
    }

    public void map(String fileName, Function<String, T> mapToData) {
        Path path = Paths.get(fileName);
        try (Stream<String> lines = Files.lines(path)) {
            lines.forEach((s)->{
                T data = mapToData.apply(s);
                if (data != null)
                    dataSet.add(data);
            });
        } catch (IOException ex) {
            System.err.print("Could open file or file format is not valid with your mapToData function");
        }
    }

    public void forEach(Consumer<T> doWork) {
        dataSet.forEach(doWork);
    }

    Stream<T> stream() {
        return dataSet.stream();
    }

}
