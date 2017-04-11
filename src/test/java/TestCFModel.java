/**
 * Created by thongpv87 on 10/04/2017.
 */
public class TestCFModel {
    private static String TRAIN_SET = "/home/thongpv3/IdeaProjects/ColaborativeFiltering/src/test/java/data/ml-100k/u1.base";
    private static String TEST_SET = "/home/thongpv3/IdeaProjects/ColaborativeFiltering/src/test/java/data/ml-100k/u1.test";

    private static void test1() {
        DataSet<Rating> dataSet = new DataSet<>();
        dataSet.map(TRAIN_SET, (line)->{
            String[] sarr = line.split("\\s+");
            try {
                return new Rating(Integer.parseInt(sarr[0]), Integer.parseInt(sarr[1]), Double.parseDouble(sarr[2]));
            } catch (Exception ex) {
                System.err.println("An error while converting a text line into data");
                return null;
            }
        });

        DataSet<Rating> testSet = new DataSet<>();
        testSet.map(TEST_SET, (line)->{
            String[] sarr = line.split("\\s+");
            try {
                return new Rating(Integer.parseInt(sarr[0]), Integer.parseInt(sarr[1]), Double.parseDouble(sarr[2]));
            } catch (Exception ex) {
                System.err.println("An error while converting a text line into data");
                return null;
            }
        });
        try {
            CFModel model = CFModel.train(dataSet, 10);

            System.out.println(String.format("%1$10s - %2$10s - %3$10s - %3$10s", "UserId", "ItemId", "Real", "Predicted"));
            testSet.forEach((r)-> {
                double rated = model.predict(r.user(), r.item());
                //print different
                System.out.println(String.format("%1$10d - %2$10d - %3$10.1f - %4$10.1f", r.user(), r.item(), r.rate(), rated));
            });

            //print different
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void test2() {
        String path = "/home/thongpv3/IdeaProjects/ColaborativeFiltering/src/test/java/data/debug-set/tests.csv";
        DataSet<Rating> dataSet = new DataSet<>();
        dataSet.map(path, (line)->{
            String[] sarr = line.split(", ");
            try {
                return new Rating(Integer.parseInt(sarr[0]), Integer.parseInt(sarr[1]), Double.parseDouble(sarr[2]));
            } catch (Exception ex) {
                System.err.println("An error while converting a text line into data");
                return null;
            }
        });

        CFModel model = CFModel.train2(dataSet, 2);
    }

    public static void main(String[] args) {
        test2();
    }
}
