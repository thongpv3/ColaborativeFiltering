package vn.edu.hust.soict.it4040.CosineSimilarity;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import org.la4j.*;
import org.la4j.Vector;
import org.la4j.matrix.dense.Basic2DMatrix;
import org.la4j.matrix.sparse.CRSMatrix;
import vn.edu.hust.soict.it4040.Utils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by thongpv87 on 10/04/2017.
 */

public class CFModel {
    //set thread count to available processors
    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();
    private HashMap<Pair<Integer, Integer>, Double> rated;
    private HashMap<Pair<Integer, Integer>, Double> predicted;
    private int similars;
    ListMultimap<Integer, Integer> ratedUsersOfItem;

    private Set<User> userSet;
    private Set<Item> itemSet;
    private ListMultimap<Integer, Pair<Integer, Double>> simMatrix;


    public CFModel(Set<User> userSet, Set<Item> itemSet, Set<Rating> ratings, ListMultimap<Integer, Pair<Integer, Double>> simMatrix, int similars) {
        this.similars = similars;
        this.userSet = userSet;
        this.itemSet = itemSet;
        this.simMatrix = simMatrix;

        this.rated = new HashMap<>();
        this.predicted = new HashMap<>();
        ratedUsersOfItem = LinkedListMultimap.create();

        ratings.forEach(rating -> {
            rated.put(new Pair<>(rating.user(), rating.item()), rating.rate());
            ratedUsersOfItem.put(rating.item(), rating.user());
        });
    }

    public static CFModel train(Set<Rating> trainingSet, int similars) {
        //todo check minUserId and minItemId
        Set<User> userSet = new HashSet<>();
        Set<Item> itemSet = new HashSet<>();
        trainingSet.forEach(rating -> {
            userSet.add(new User(rating.user()));
            itemSet.add(new Item(rating.item()));
        });


        int nUser = userSet.stream().max((u1, u2)->u1.compare(u2)).get().id()+1;
        int nItem = itemSet.stream().max((i1, i2)->i1.compare(i2)).get().id()+1;

        Matrix data = new CRSMatrix(nUser, nItem);
        trainingSet.forEach(r -> {
            data.set(r.user(), r.item(), r.rate());
        });

        Utils.printMatrix(data);
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

        Utils.printMatrix(data);

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

        Utils.printMatrix(simMatrix);

        //Map simMatrix to other data structure for better performance for predict operation
        ListMultimap<Integer, Pair<Integer, Double>> simInfo = ArrayListMultimap.create();
        final HashSet<User> checkSet = new HashSet<>();
        simMatrix.each((i, j, d) -> {
            //We need to check because of user id can be spare, not continuous
            checkSet.clear();
            checkSet.add(new User(i));
            checkSet.add(new User(j));
            if (userSet.containsAll(checkSet))
                simInfo.put(i, new Pair<>(j, d));
        });
        //sort simInfo
        simInfo.keySet().forEach(
                (key) -> simInfo.get(key).sort((p1, p2) -> 0 - p1.t2().compareTo(p2.t2())));

        Utils.printMultiMapAsMatrix(simInfo, 30);

        return new CFModel(userSet, itemSet, trainingSet, simInfo, similars);
    }

    public double predict(int userId, int itemId) {
        Pair<Integer, Integer> pair = new Pair<>(userId, itemId);
        Double result;
        result = (rated.get(pair) != null) ? rated.get(pair) : predicted.get(pair);
        if (result != null)
            return result;

        List<Pair<Integer,Double>> sims = simMatrix.get(userId).stream()
                .filter((p)-> ratedUsersOfItem.get(itemId).contains(p.t1()))
                .limit(similars).collect(Collectors.toList());

        double s = 0;
        double t = 0;
        for (Pair<Integer, Double> p: sims) {
            s += p.t2();
            double rate = rated.get(new Pair<>(p.t1(), itemId));
            t += rate*p.t2();
        }

        result = (s==0) ? 0 : t/s;
        return result;
    }


    public HashMap<Pair<Integer, Integer>, Double> predicted() {
        return predicted;
    }
}
