package net.mslivo.example;

import com.badlogic.gdx.math.MathUtils;
import net.mslivo.core.engine.tools.threads.ForkJoinListUpdater;
import net.mslivo.core.engine.tools.threads.ThreadedListUpdater;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadTest {

    private static final int AMOUNT = 5000;
    private static final int RUNS = 60 * 60;

    public static void main(String[] args) throws Exception {
        System.in.read();
        for (int i = 0; i < 10; i++) {
            System.out.print("testForkJoinUpdater:");
            testForkJoinUpdater();
            System.out.print("ThreadedListUpdater:");
            testThreadedListUpdater();
            System.out.print("Single Thread      :");
            testExecutorService();

            System.out.println("_________________");
        }
        System.in.read();

    }


    private static void testExecutorService() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < AMOUNT; i++) list.add(i);
        AtomicInteger sum = new AtomicInteger();

        long time = System.nanoTime();

        for (int i = 0; i < RUNS; i++) {
            for (int i2 = 0; i2 < AMOUNT; i2++) {
                sum.addAndGet(compute(list.get(i2)));
            }
        }

        int timeresult = MathUtils.round((System.nanoTime() - time) / 1000);
        System.out.println(timeresult + "ns, result: " + sum.get());
    }

    private static void testThreadedListUpdater() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < AMOUNT; i++) list.add(i);
        AtomicInteger sum = new AtomicInteger();


        ThreadedListUpdater<Integer> updater = new ThreadedListUpdater<>(list, new ThreadedListUpdater.ItemUpdater<Integer>() {
            @Override
            public void updateFromThread(Integer object, int index) {
                sum.addAndGet(compute(object));
            }
        });

        long time = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            updater.update();
        }

        int timeresult = MathUtils.round((System.nanoTime() - time) / 1000);
        System.out.println(timeresult + "ns, result: " + sum.get());

    }

    private static void testForkJoinUpdater() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < AMOUNT; i++) list.add(i);
        AtomicInteger sum = new AtomicInteger();


        ForkJoinListUpdater<Integer> updater = new ForkJoinListUpdater<>(list, new ForkJoinListUpdater.ItemUpdater<Integer>() {
            @Override
            public void updateFromThread(Integer object, int index) {
                sum.addAndGet(compute(object));
            }
        });

        int run = 0;
        long time = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            updater.update();
            run++;
        }

        int timeresult = MathUtils.round((System.nanoTime() - time) / 1000);
        System.out.println(timeresult + "ns, result: " + sum.get());

    }

    private static ArrayList<Integer> testList = new ArrayList<>();

    static {
        testList.add(0);
        testList.add(4);
        testList.add(10);
        testList.add(8);
    }

    private static int compute(Integer number) {
        number = number / 2;
        number++;
        synchronized (number) {
            number = number * 100;
        }
        synchronized (number) {
            number = number * -100;
        }
        if (number < 100) {
            number--;
        } else {
            if (number > 150) {
                number = number / number;
            } else {
                number -= 10;
            }
            if (number > 500) {
                number -= 10;
                number += testList.get(1);

            } else if (number > 600) {
                number = 0;
            }
        }
        number += testList.get(0);
        number *= testList.get(2);
        return number;
    }

}
