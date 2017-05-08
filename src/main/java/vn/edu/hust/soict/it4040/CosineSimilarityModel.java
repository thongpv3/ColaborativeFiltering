package vn.edu.hust.soict.it4040;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import org.apache.commons.lang.mutable.MutableDouble;
import org.apache.commons.lang.mutable.MutableInt;

import java.util.*;
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

    //variable for global baseline estimate
    private double micro;
    private HashMap<Integer, Double> baseXs;
    private HashMap<Integer, Double> baseYs;
    public CosineSimilarityModel(HashMap<Integer, User> users, HashMap<Integer, Item> items, Set<Rating> ratings, ListMultimap<Integer, Pair<Integer, Double>> simMatrix, int similars) {
        this.similars = similars;
        this.users = users;
        this.items = items;
        this.simMatrix = simMatrix;

        this.rated = new HashMap<>();
        this.predicted = new HashMap<>();
        ratedUsersOfItem = LinkedListMultimap.create();

        baseXs = new HashMap<>();
        HashMap<Integer, Integer> t1 = new HashMap<>();
        baseYs = new HashMap<>();
        HashMap<Integer, Integer> t2 = new HashMap<>();
        final MutableDouble total = new MutableDouble(0);
        final MutableInt numTotal = new MutableInt(0);
        ratings.forEach(rating -> {
            rated.put(new Pair<>(rating.user(), rating.item()), rating.rate());
            ratedUsersOfItem.put(rating.item(), rating.user());
            double d1 = baseXs.getOrDefault(rating.user(), 0.0);
            int x1 = t1.getOrDefault(rating.user(), 0);
            double d2 = baseYs.getOrDefault(rating.item(), 0.0);
            int x2 = t2.getOrDefault(rating.item(), 0);
            baseXs.put(rating.user(), d1 + rating.rate());
            baseYs.put(rating.item(), d2 + rating.rate());
            t1.put(rating.user(), x1 + 1);
            t2.put(rating.item(), x2 + 1);
            total.add(rating.rate());
            numTotal.add(1);
        });

        micro = (numTotal.intValue() != 0) ? total.doubleValue()/numTotal.intValue() : 0;
        baseXs.forEach((k,v)->{
            int x1 = t1.getOrDefault(k, 0);
            baseXs.put(k, x1 != 0 ? v/x1 : 0.0);
        });

        baseYs.forEach((k,v)->{
            int x2 = t2.getOrDefault(k, 0);
            baseYs.put(k, x2 != 0 ? v/x2 : 0.0);
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

        double bxi = micro + baseXs.getOrDefault(user, micro) - micro + baseYs.getOrDefault(item, micro) - micro;

        double s = 0;
        double t = 0;
        for (Pair<Integer, Double> p: sims) {
            s += p.t2();
            double rate = rated.get(new Pair<>(p.t1(), item));
            double bxj = micro + baseXs.getOrDefault(p.t1(), micro) - micro + baseYs.getOrDefault(item, micro) - micro;
            t += (rate - bxj) * p.t2();
        }

        result = bxi + ((s==0) ? 0 : t/s);
        //predicted.put...
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
