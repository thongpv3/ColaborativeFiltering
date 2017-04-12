package vn.edu.hust.soict.it4040;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by thongpv87 on 12/04/2017.
 */
public class CosineSimilarityModel implements CollaborativeFilteringModel {
    private HashMap<Pair<Integer, Integer>, Double> rated;
    private HashMap<Pair<Integer, Integer>, Double> predicted;
    private int similars;
    ListMultimap<Integer, Integer> ratedUsersOfItem;

    HashMap<Integer, User> users;
    HashMap<Integer, Item> items;

    private ListMultimap<Integer, Pair<Integer, Double>> simMatrix;

    public CosineSimilarityModel(HashMap<Integer, User> users, HashMap<Integer, Item> items, Set<Rating> ratings, ListMultimap<Integer, Pair<Integer, Double>> simMatrix, int similars) {
        this.similars = similars;
        this.users = users;
        this.items = items;
        this.simMatrix = simMatrix;

        this.rated = new HashMap<>();
        this.predicted = new HashMap<>();
        ratedUsersOfItem = LinkedListMultimap.create();

        ratings.forEach(rating -> {
            rated.put(new Pair<>(rating.user(), rating.item()), rating.rate());
            ratedUsersOfItem.put(rating.item(), rating.user());
        });
    }

    @Override
    public double predict(int user, int item) {
        Pair<Integer, Integer> pair = new Pair<>(user, item);
        Double result;
        result = (rated.get(pair) != null) ? rated.get(pair) : predicted.get(pair);
        if (result != null)
            return result;

        List<Pair<Integer,Double>> sims = simMatrix.get(user).stream()
                .filter((p)-> ratedUsersOfItem.get(item).contains(p.t1()))
                .limit(similars).collect(Collectors.toList());

        double s = 0;
        double t = 0;
        for (Pair<Integer, Double> p: sims) {
            s += p.t2();
            double rate = rated.get(new Pair<>(p.t1(), item));
            t += rate*p.t2();
        }

        result = (s==0) ? 0 : t/s;
        return result;
    }

    @Override
    public Rating[] recommendProducts(int user, int num) {
        return new Rating[0];
    }

    @Override
    public Rating[] recommendUsers(int item, int num) {
        return new Rating[0];
    }
}
