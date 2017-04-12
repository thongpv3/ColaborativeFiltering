package vn.edu.hust.soict.it4040;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;

/**
 * Created by thongpv87 on 12/04/2017.
 */
public class TestCosineSimilarityModel {
    private static String TRAIN_SET = "/home/thongpv87/IdeaProjects/ColaborativeFiltering/src/test/java/data/ml-100k/u1.base";
    private static String TEST_SET = "/home/thongpv87/IdeaProjects/ColaborativeFiltering/src/test/java/data/ml-100k/u1.test";
    private static String RESULT_OUTPUT = "/home/thongpv87/IdeaProjects/ColaborativeFiltering/src/test/java/result/ml-100k1.result";

    private static void test1() {
        String path = "/home/thongpv87/IdeaProjects/ColaborativeFiltering/src/test/java/data/debug-set/tests.csv";
        HashSet<Rating> ratings = new HashSet<>();
        Utils.mapFileIntoSet(ratings, path, s -> {
            String[] sarr = s.split(", ");
            return new Rating(Integer.parseInt(sarr[1]), Integer.parseInt(sarr[0]), Double.parseDouble(sarr[2]));
        });

        CollaborativeFilteringModel model = RecommendationTrainer.trainCSM(ratings, 2);
        System.out.println(model.predict(1, 5));
    }

    private static void test2() {
        //load train set and test set
        HashSet<Rating> trainSet = new HashSet<>();
        HashSet<Rating> testSet = new HashSet<>();
        Utils.mapFileIntoSet(trainSet, TRAIN_SET, s -> {
            String[] sarr = s.split("\\s+"); //regular expression skip space
            return new Rating(Integer.parseInt(sarr[0]), Integer.parseInt(sarr[1]), Double.parseDouble(sarr[2]));
        });
        Utils.mapFileIntoSet(testSet, TEST_SET, s -> {
            String[] sarr = s.split("\\s+");
            return new Rating(Integer.parseInt(sarr[0]), Integer.parseInt(sarr[1]), Double.parseDouble(sarr[2]));
        });

        CollaborativeFilteringModel model = RecommendationTrainer.trainCSM(trainSet, 5);

        final DoubleWrapper var = new DoubleWrapper();

        FileOutputStream of;
        try {
            of = new FileOutputStream(RESULT_OUTPUT);

            of.write(Utils.prettyFormat(20, "UserId", "ItemId", "Rate", "Predict").getBytes());
            of.write("\n".getBytes());
            testSet.forEach(rating -> {
                double predict = model.predict(rating.user(), rating.item());
                double err = predict-rating.rate();
                var.add(err*err);
                try {
                    of.write(Utils.prettyFormat(20, rating.user(), rating.item(), rating.rate(), predict).getBytes());
                    of.write("\n".getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            of.close();
        } catch (FileNotFoundException e) {
            System.err.println("Can't open output stream: " + RESULT_OUTPUT);
        } catch (IOException e) {
            e.printStackTrace();
        }


        double rmse = Math.sqrt(var.getValue()/testSet.size());
        System.out.println("Root Mean Squared Error (RMSE): " + rmse);
    }
    public static void main(String[] args) {
//        test1();
        test2();
    }
}
