package net.mslivo.example;


import net.mslivo.core.engine.tools.Tools;

public class ExampleLauncherMain {

    public static void main(String[] args) {

        /*int AMOUNT = 63;
        List<String> list = new ArrayList<>();
        for (int i = 0; i < AMOUNT; i++) list.add("String" + i);

         AtomicInteger errors = new AtomicInteger();

        ThreadedListUpdater<String> updater = new ThreadedListUpdater<>(list, new ThreadedListUpdater.ItemUpdater() {
            private AtomicInteger count = new AtomicInteger();

            @Override
            public void afterUpdate() {
                if (count.intValue() < AMOUNT) {
                    errors.addAndGet(1);
                    System.out.println("error:"+count);
                }
            }

            @Override
            public void beforeUpdate() {
                count.set(0);
            }

            @Override
            public void updateFromThread(Object object, int index) {
                count.addAndGet(1);
            }
        });

        int run = 0;
        long time = System.currentTimeMillis();
        for (int i = 0; i < 20000; i++) {
            System.out.println("Runs " + run);
            updater.update();
            run++;
        }
        System.out.println("100k runs: " + (System.currentTimeMillis() - time) + "ms, errors; "+errors);
        */

        Tools.App.launch(
                new ExampleMain(),
                ExampleMainConstants.APP_TITLE,
                ExampleMainConstants.INTERNAL_RESOLUTION_WIDTH,
                ExampleMainConstants.INTERNAL_RESOLUTION_HEIGHT, 60, null, true);


    }
}
