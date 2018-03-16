package dynamicplan;

import java.util.*;

/**
 * 最少金币问题，贪心算法无法解决
 * 问题：现在需要有金币25 1枚， 20 2枚， 5 5枚， 1 10枚，需要得到金币总额41，计算出得到最少金币的方案
 * 贪心算法： 25+5*3+1=5枚
 * 动态规划： 20*2 + 1 = 3枚
 */
public class CoinMain {

    // 已经计算过的计划
    private static Map<Integer, CoinPlan> gPlans = new HashMap<Integer, CoinPlan>();

    private static Map<Integer, List<List<Coin>>> gCoinPlans = new HashMap<Integer, List<List<Coin>>>();

    public static void main(String[] args) {
        List<Coin> coins = new ArrayList<Coin>(5);
        coins.add(new Coin(30, 1));
        coins.add(new Coin(25, 1));
        coins.add(new Coin(20, 2));
        coins.add(new Coin(10, 3));
        coins.add(new Coin(5, 5));
        coins.add(new Coin(1, 10));
        /*CoinPlan plan = new CoinPlan();
        plan.remainCoins = coins;
        plan.value = 41;
        produce(plan);
        printPlan(plan, 0);*/
        List<List<Coin>> plans = minPlan(coins, 41);
        for (List<Coin> plan : plans) {
            for (Coin coin : plan) {
                System.out.print(coin.value + ",");
            }
            System.out.println();
        }
    }

    private static void printPlan(CoinPlan coinPlan, int tabSize) {
        for (int i = 0; i < tabSize; i++) {
            System.out.print('\t');
        }
        System.out.println(coinPlan.value + (coinPlan.coin == null ? "" : " coin:" + coinPlan.coin.value) + " sub:");
        if (coinPlan.subPlans == null || coinPlan.subPlans.isEmpty()) {
            return;
        }
        for (CoinPlan sub : coinPlan.subPlans) {
            printPlan(sub, tabSize + 1);
        }
    }

    private static CoinPlan produce(CoinPlan plan) {
        int value = plan.value;
        if (value <= 0) {
            return plan;
        }
        if (gPlans.containsKey(value)) {
            return gPlans.get(value);
        }
        gPlans.put(value, plan);
        plan.subPlans = new ArrayList<CoinPlan>();
        List<Coin> coins = plan.remainCoins;
        for (Coin coin : coins) {
            CoinPlan sub = new CoinPlan();
            sub.coin = coin;
            sub.remainCoins = copy(coins, coin.value);
            sub.value = value - coin.value;
            Iterator<Coin> it = sub.remainCoins.iterator();
            while (it.hasNext()) {
                Coin tmp = it.next();
                if (tmp.value == coin.value) {
                    tmp.count--;
                    if (tmp.count == 0) {
                        it.remove();
                    }
                    break;
                }
            }
            plan.subPlans.add(produce(sub));
        }
        return plan;
    }

    private static List<Coin> copy(List<Coin> coins, int value) {
        List<Coin> result = new ArrayList<Coin>(coins.size());
        for (Coin coin : coins) {
            if (coin.value > value) {
                continue;
            }
            result.add(new Coin(coin.value, coin.count));
        }
        return result;
    }

    private static CoinPlan produceMin(CoinPlan coinPlan) {
        if (coinPlan.minPlans != null) {
            return coinPlan;
        }

        return coinPlan;
    }

    /**
     * 金币
     */
    private static class Coin {
        private int value;
        private int count;

        public Coin(int value, int count) {
            this.value = value;
            this.count = count;
        }
    }

    /**
     * 当前金币值的计划，及最佳个数
     */
    private static class CoinPlan {
        private int value;
        private Coin coin;
        private List<Coin> remainCoins;
        private List<CoinPlan> subPlans;
        private List<CoinPlan> minPlans;
    }

    private static List<List<Coin>> minPlan(List<Coin> coins, int value) {
        if (gCoinPlans.containsKey(value)) {
            return gCoinPlans.get(value);
        }
        if (coins.isEmpty()) {
            return null;
        }
        boolean first = true;
        int size = 0;
        List<Coin> copyCoins = copy(coins, value);
        List<List<Coin>> tmpCoinPlans = new LinkedList<List<Coin>>();
        for (Coin coin : copyCoins) {
            List<Coin> tmpCoinPoints = copy(coins, value);
            Iterator<Coin> it = tmpCoinPoints.iterator();
            while (it.hasNext()) {
                Coin tmp = it.next();
                if (tmp.value == coin.value) {
                    tmp.count--;
                    if (tmp.count == 0) {
                        it.remove();
                    }
                    break;
                }
            }
            int tmpSize = 1;
            int nextValue = value - coin.value;
            List<List<Coin>> tmpNextCoins = new LinkedList<List<Coin>>();
            if (nextValue > 0) {
                List<List<Coin>> c = minPlan(tmpCoinPoints, nextValue);
                if (c == null || c.isEmpty()) {
                    continue;
                }
                tmpSize = c.get(0).size() + 1;
                for (List<Coin> co : c) {
                    List<Coin> tmp = new LinkedList<Coin>();
                    tmp.add(coin);
                    tmp.addAll(co);
                    tmpNextCoins.add(tmp);
                }
            } else if (nextValue == 0) {
                List<Coin> tmp = new LinkedList<Coin>();
                tmp.add(coin);
                tmpNextCoins.add(tmp);
            }
            if (first) {
                size = tmpSize;
                first = false;
                tmpCoinPlans = tmpNextCoins;
            } else if (tmpSize == size) {
                tmpCoinPlans.addAll(tmpNextCoins);
            } else if (tmpSize < size) {
                size = tmpSize;
                tmpCoinPlans = tmpNextCoins;
            }
        }
        gCoinPlans.put(value, tmpCoinPlans);
        return tmpCoinPlans;
    }
}
