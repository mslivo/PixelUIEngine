package net.mslivo.performancetest;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import net.mslivo.pixelui.utils.Tools;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class ThreadTest {

    private Array<Integer> list = new Array<>();
    private long startTime;
    private AtomicInteger sum = new AtomicInteger();
    private TestConsumer testConsumer = new TestConsumer();
    private int expected;
    private String test;

    class TestConsumer implements Consumer<Integer> {

        @Override
        public void accept(Integer value) {
            synchronized (sum){
                sum.set(sum.get()+value);
            }
        }
    }

    public static void main(String[]args){
        ThreadTest threadTest = new ThreadTest();


        for(int i=0;i<10;i++) {
            threadTest.start("parallelRunner.run()");
            threadTest.runTestParallelRunner();
            threadTest.end();

        }

        System.out.println(String.format("%1$6s", ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024)))+"MB");
    }

    public void start(String test){
        final int COUNT = 10_000_000;
        this.test = test;
        this.sum.set(0);
        this.expected = 0;
        this.list.clear();
        for(int i=0;i<COUNT;i++){
            int val = MathUtils.random(0,100);
            this.list.add(Integer.valueOf(val));
            this.expected+=val;
        }
        this.testConsumer = new TestConsumer();
        this.startTime = System.nanoTime();
    }

    public void end(){
        System.out.println("===== "+this.test+" =====");
        System.out.println("excected: "+this.expected+", actual: "+this.sum.get()+" - error:"+(this.expected-this.sum.get()));
        System.out.println("Time: "+((System.nanoTime()-startTime)/1000));
        System.out.println("====================================");
    }

    public void runTestParallelRunner(){
        Tools.App.runParallel(this.list, this.testConsumer);
    }




}
