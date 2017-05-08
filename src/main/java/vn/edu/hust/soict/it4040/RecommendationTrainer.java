package vn.edu.hust.soict.it4040;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.la4j.Matrix;
import org.la4j.Vector;
import org.la4j.matrix.dense.Basic2DMatrix;
import org.la4j.matrix.sparse.CRSMatrix;
import spire.math.IntNumber;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by thongpv87 on 12/04/2017.
 */
public class RecommendationTrainer {
    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();


    public static CollaborativeFilteringModel trainCSM(Set<Rating> ratings, int similars) {
        HashMap<Integer, User> users = new HashMap<>();
        HashMap<Integer, Item> items = new HashMap<>();

        final AtomicInteger maxUserId = new AtomicInteger(0);
        final AtomicInteger maxItemId = new AtomicInteger(0);

        ratings.forEach(rating -> {
            if (rating.user() > maxUserId.get())
                maxUserId.set(rating.user());
            if (rating.item() > maxItemId.get())
                maxItemId.set(rating.item());
            users.put(rating.user(), new User(rating.user()));
            items.put(rating.item(), new Item(rating.item()));
        });

        int nUser = maxUserId.get()+1;
        int nItem = maxItemId.get()+1;

        Matrix data = new CRSMatrix(nUser, nItem);
        ratings.forEach(r -> {
            data.set(r.user(), r.item(), r.rate());
        });

//        Utils.printMatrix(data);
        //todo parallel
        //PHASE 1 - STANDARDIZATION
        for (int i = 0; i < data.rows(); i++) {
            Vector row = data.getRow(i);

            int t = 0;
            for (int j = 0; j < data.columns(); j++) {
                if (row.get(j) != 0) {
                    t++;
                }
            }
            double avg = 0;
            if (t != 0)
                avg = row.sum() / t;

            for (int j = 0; j < nItem; j++) {
                if (row.get(j) != 0) {
                    row.set(j, row.get(j) - avg);
                }
            }
            if (avg != 0)
                data.setRow(i, row);
        }

//        Utils.printMatrix(data);

        Matrix simMatrix = new Basic2DMatrix(nUser, nUser);
        for (int i = 0; i < simMatrix.rows(); i++) {
            Vector rowi = data.getRow(i);
            for (int j = i + 1; j < nUser; j++) {
                Vector rowj = data.getRow(j);
                //scalar product of two vector
                double innerProduct = rowi.innerProduct(rowj);
                simMatrix.set(i, j, innerProduct / rowi.norm() / rowj.norm());
                simMatrix.set(j, i, innerProduct / rowi.norm() / rowj.norm());
            }
        }

//        Utils.printMatrix(simMatrix);

        //Map simMatrix to other data structure for better performance for predict operation
        ListMultimap<Integer, Pair<Integer, Double>> simInfo = ArrayListMultimap.create();
        simMatrix.each((i, j, d) -> {
            //ignore same user id
            if (i==j)
                return;
            //We need to check because of user id can be spare, not continuous
            if (users.containsKey(i) && users.containsKey(j))
                simInfo.put(i, new Pair<>(j, d));
        });
        //sort simInfo
        simInfo.keySet().forEach(
                (key) -> simInfo.get(key).sort((p1, p2) -> 0 - p1.t2().compareTo(p2.t2())));

//        Utils.printMultiMapAsMatrix(simInfo, 30);

        return new CosineSimilarityModel(users, items, ratings, simInfo, similars);
    }

    public static CollaborativeFilteringModel trainMFM() {
        return null;
    }
}