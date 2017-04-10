/**
 * Created by thongpv87 on 10/04/2017.
 */
public class TestCFModel {
    private static String FILE_PATH = "/home/thongpv87/IdeaProjects/ColaborativeFiltering/src/test/java/debug-set/tests.csv";

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

        try {
            CFModel model = CFModel.train(dataSet, 2);
            double predict =model.predict(1,5);
            System.out.println(predict);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
