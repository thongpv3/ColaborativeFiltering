package vn.edu.hust.soict.it4040;

import com.google.common.collect.Multimap;
import org.la4j.Matrix;
import org.la4j.Vector;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Created by thongpv87 on 12/04/2017.
 */
public class Utils {
    public static void printMatrix(Matrix m) {
        System.out.println();
        for (int i = 0; i < m.rows(); i++) {
            Vector row = m.getRow(i);
            row.forEach((d) -> {
                System.out.print(String.format("%1$8.2f", d));
            });
            System.out.println();
        }
        System.out.println();
    }

    public static <K,V> void printMultiMapAsMatrix(Multimap<K, V> map, int spacing) {
        String format = "%"+spacing +"s";
        System.out.println();
        map.keySet().forEach((key)->{
            map.get(key).forEach((value)->{
                System.out.print(String.format(format, value.toString()));
            });
            System.out.println();
        });
        System.out.println();
    }

    public static <T> void mapFileIntoSet(Set<T> set, String fileName, Function<String, T> mapToData) {
        Path path = Paths.get(fileName);
        try (Stream<String> lines = Files.lines(path)) {
            lines.forEach((s)->{
                T data = null;
                try {
                    data = mapToData.apply(s);
                } catch (Exception ex) {
                    System.err.println("Error to parse a line");
                }
                if (data != null)
                    set.add(data);
            });
        } catch (IOException ex) {
            System.err.print("Could open file or file format is not valid with your mapToData function");
        }
    }

    public static void prettyPrintln(int width, Object... objects) {
        StringBuilder formatStr = new StringBuilder();
        for (int i=0; i<objects.length; i++)
            formatStr.append("%"+width+"s");
        System.out.println(String.format(formatStr.toString(), objects));
    }
}