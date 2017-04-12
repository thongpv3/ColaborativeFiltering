package vn.edu.hust.soict.it4040;

/**
 * Created by thongpv87 on 10/04/2017.
 */
public class TestDataSet {
    //todo Fix FILE_PATH into your local path
    private static String FILE_PATH = "/home/thongpv87/IdeaProjects/ColaborativeFiltering/src/test/java/data/ml-latest-small/ratings.csv";
    public static void main(String[] args) {
        DataSet<Rating> dataSet = new DataSet<>();
        dataSet.map(FILE_PATH, (line)->{
            String[] sarr = line.split(",");
            try {
                Rating r = new Rating(Integer.parseInt(sarr[0]), Integer.parseInt(sarr[1]), Double.parseDouble(sarr[2]));
                return r;
            } catch (Exception ex) {
                System.err.println("An error while converting a text line into data");
                return null;
            }
        });

        dataSet.forEach((r)->{
            System.out.println(r);
        });

    }
}
