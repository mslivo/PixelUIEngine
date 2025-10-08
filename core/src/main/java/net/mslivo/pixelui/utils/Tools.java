package net.mslivo.pixelui.utils;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.LongArray;
import com.github.dgzt.gdx.lwjgl3.Lwjgl3VulkanApplication;
import net.mslivo.pixelui.media.CMedia;
import net.mslivo.pixelui.media.CMediaFontArraySymbol;
import net.mslivo.pixelui.media.CMediaFontSingleSymbol;
import net.mslivo.pixelui.media.MediaManager;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Utility Class
 */
public class Tools {

    public static class App {
        private static float skipFrameAccumulator = 0f;
        private static int maxUpdatesPerSecond = 60;
        private static float timeStep;
        private static float timeStepX2;
        private static float timeBetweenUpdates;
        private static final Path ERROR_LOG_FILE = Path.of("error.log");

        private static final int MAX_TASKS = Runtime.getRuntime().availableProcessors();
        private static final IntArrayTask[] intTasks = new IntArrayTask[MAX_TASKS];
        @SuppressWarnings("rawtypes")
        private static final ArrayTask[] ARRAY_TASKS = new ArrayTask[MAX_TASKS];

        static {
            for (int i = 0; i < MAX_TASKS; i++) {
                intTasks[i] = new IntArrayTask();
                ARRAY_TASKS[i] = new ArrayTask<>();
            }
        }

        private static final class IntArrayTask extends RecursiveAction {
            int[] array;
            int start, end;
            IntConsumer consumer;

            void setup(int[] array, int start, int end, IntConsumer consumer) {
                this.array = array;
                this.start = start;
                this.end = end;
                this.consumer = consumer;
                this.reinitialize(); // allow reuse after join()
            }

            @Override
            protected void compute() {
                for (int i = start; i < end; i++) {
                    consumer.accept(array[i]);
                }
            }
        }

        private static final class ArrayTask<T> extends RecursiveAction {
            Array<T> list;
            int start, end;
            Consumer<T> consumer;

            void setup(final Array<T> list, final int start, final int end, final Consumer<T> consumer) {
                this.list = list;
                this.start = start;
                this.end = end;
                this.consumer = consumer;
                this.reinitialize(); // allow reuse after join()
            }

            @Override
            protected void compute() {
                for (int i = start; i < end; i++) {
                    consumer.accept(list.get(i));
                }
            }
        }

        public static void runParallel(final int[] array, final IntConsumer consumer) {
            runParallel(array, consumer, array.length);
        }

        public static void runParallel(final int[] array, final IntConsumer consumer, final int size) {
            if (size == 0) return;

            final int taskCount = Math.min(MAX_TASKS, size);
            final int chunkSize = (size + taskCount - 1) / taskCount;

            for (int i = 0; i < taskCount; i++) {
                int start = i * chunkSize;
                int end = Math.min(start + chunkSize, size);
                IntArrayTask task = intTasks[i];
                task.setup(array, start, end, consumer);
                ForkJoinPool.commonPool().execute(task);
            }

            for (int i = 0; i < taskCount; i++)
                intTasks[i].join();
        }

        public static <T> void runParallel(final Array<T> array, final Consumer<T> consumer) {
            runParallel(array, consumer, array.size);
        }

        @SuppressWarnings("unchecked")
        public static <T> void runParallel(final Array<T> array, final Consumer<T> consumer, final int size) {
            if (size == 0) return;

            final int taskCount = Math.min(MAX_TASKS, size);
            final int chunkSize = (size + taskCount - 1) / taskCount;

            for (int i = 0; i < taskCount; i++) {
                int start = i * chunkSize;
                int end = Math.min(start + chunkSize, size);
                ArrayTask<T> task = (ArrayTask<T>) ARRAY_TASKS[i];
                task.setup(array, start, end, consumer);
                ForkJoinPool.commonPool().execute(task);
            }

            for (int i = 0; i < taskCount; i++)
                ARRAY_TASKS[i].join();

        }

        public static void handleException(Exception e) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(ERROR_LOG_FILE.toString(), true))) {
                pw.write("Exception \"" + (e.getClass().getSimpleName()) + "\" occured" + System.lineSeparator());
                e.printStackTrace(pw);
                e.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        public static void handleError(String error) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(ERROR_LOG_FILE.toString(), true))) {
                pw.write(error + System.lineSeparator());
                System.err.println(error);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        public static void showExceptionDialog(Exception e) {
            String stackTrace;
            try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
                try (PrintWriter printWriter = new PrintWriter(byteArrayOutputStream)) {
                    e.printStackTrace(printWriter);
                    printWriter.flush();
                    stackTrace = byteArrayOutputStream.toString();
                }
            } catch (IOException ex) {
                stackTrace = e.toString();
            }
            StringBuilder shownStackTrace = new StringBuilder(stackTrace);
            if (shownStackTrace.length() > 512) {
                shownStackTrace.setLength(512);
                shownStackTrace.append(System.lineSeparator()).append("...");
            }
            shownStackTrace.append(System.lineSeparator()).append("Press OK to copy to Clipboard");

            int option = JOptionPane.showConfirmDialog(null, shownStackTrace.toString(), "Exception", JOptionPane.PLAIN_MESSAGE);
            if (option == JOptionPane.OK_OPTION) {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(stackTrace), null);
            }
        }

        public static void setTargetUpdates(int updatesPerSecond) {
            App.maxUpdatesPerSecond = Math.max(updatesPerSecond, 1);
            timeStep = (1f / (float) App.maxUpdatesPerSecond);
            timeStepX2 = timeStep * 2f;
            skipFrameAccumulator = 0;
            timeBetweenUpdates = 1000f / (updatesPerSecond * 1000f);
        }

        public static boolean runUpdate() {
            // Accumulate 2 frames max
            skipFrameAccumulator = Math.min(skipFrameAccumulator + Gdx.graphics.getDeltaTime(), timeStepX2);
            if (skipFrameAccumulator < timeStep) {
                return false;
            } else {
                skipFrameAccumulator -= timeStep;
                return true;
            }
        }

        public static float timeBetweenUpdates() {
            return timeBetweenUpdates;
        }

        public static void launch(ApplicationAdapter applicationAdapter, PixelUILaunchConfig launchConfig) {
            // Determine glEmulation
            String osName = System.getProperty("os.name").toLowerCase();
            PixelUILaunchConfig.GLEmulation glEmulation;
            if (osName.contains("win")) {
                glEmulation = launchConfig.windowsGLEmulation;
            } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
                glEmulation = launchConfig.linuxGLEmulation;
            } else if (osName.contains("mac")) {
                glEmulation = launchConfig.macOSGLEmulation;
            } else {
                throw new RuntimeException("Operating System \"" + osName + "\n not supported");
            }

            switch (glEmulation) {
                case GL32_OPENGL -> {
                    Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
                    config.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.GL32, 3, 2);
                    config.setResizable(launchConfig.resizeAble);
                    config.setDecorated(launchConfig.decorated);
                    config.setMaximized(launchConfig.maximized);
                    config.setWindowPosition(-1, -1);
                    config.setWindowedMode(launchConfig.resolutionWidth, launchConfig.resolutionHeight);
                    config.setWindowSizeLimits(launchConfig.resolutionWidth, launchConfig.resolutionHeight, -1, -1);
                    config.setTitle(launchConfig.appTile);
                    config.setForegroundFPS(launchConfig.fps);
                    config.setIdleFPS(launchConfig.idleFPS);
                    config.useVsync(launchConfig.vSync);
                    config.setBackBufferConfig(launchConfig.r, launchConfig.g, launchConfig.b, launchConfig.a, launchConfig.depth, launchConfig.stencil, launchConfig.samples);
                    if (launchConfig.iconPath != null) config.setWindowIcon(launchConfig.iconPath);
                    try {
                        new Lwjgl3Application(applicationAdapter, config);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Tools.App.handleException(e);
                        Tools.App.showExceptionDialog(e);
                    }
                }
                case GL32_VULKAN -> {
                    com.github.dgzt.gdx.lwjgl3.Lwjgl3ApplicationConfiguration config = new com.github.dgzt.gdx.lwjgl3.Lwjgl3ApplicationConfiguration();
                    config.setOpenGLEmulation(com.github.dgzt.gdx.lwjgl3.Lwjgl3ApplicationConfiguration.GLEmulation.ANGLE_GLES32, 3, 2);
                    config.setResizable(launchConfig.resizeAble);
                    config.setDecorated(launchConfig.decorated);
                    config.setMaximized(launchConfig.maximized);
                    config.setWindowPosition(-1, -1);
                    config.setWindowedMode(launchConfig.resolutionWidth, launchConfig.resolutionHeight);
                    config.setWindowSizeLimits(launchConfig.resolutionWidth, launchConfig.resolutionHeight, -1, -1);
                    config.setTitle(launchConfig.appTile);
                    config.setForegroundFPS(launchConfig.fps);
                    config.setIdleFPS(launchConfig.idleFPS);
                    config.useVsync(launchConfig.vSync);
                    config.setBackBufferConfig(launchConfig.r, launchConfig.g, launchConfig.b, launchConfig.a, launchConfig.depth, launchConfig.stencil, launchConfig.samples);
                    if (launchConfig.iconPath != null) config.setWindowIcon(launchConfig.iconPath);
                    try {
                        new Lwjgl3VulkanApplication(applicationAdapter, config);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Tools.App.handleException(e);
                        Tools.App.showExceptionDialog(e);
                    }
                }
            }
        }

    }


    public static class Text {

        private static final StringBuilder builder = new StringBuilder();

        public static String benchmark(String... customValues) {
            builder.setLength(0);
            builder.append(String.format("%1$6s", Gdx.graphics.getFramesPerSecond()));
            builder.append(" FPS | ");
            builder.append(String.format("%1$6s", ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024))));
            builder.append("MB RAM | ");
            builder.append(String.format("%1$6s", (Thread.getAllStackTraces().keySet().size())));
            builder.append(" Threads");
            for (int i = 0; i < customValues.length; i++)
                builder.append(" | ").append(String.format("%1$10s", customValues[i]));
            return builder.toString();
        }


        public static String[] toArray(String text) {
            return toArray(text, true);
        }

        public static String[] toArray(String... text) {
            return text;
        }

        public static String[] toArray(String text, boolean splitLines) {
            return splitLines ? text.split(System.lineSeparator()) : new String[]{text};
        }

        public static String formatNumber(int number) {
            return formatNumber((long) number);
        }

        public static String formatNumber(long number) {
            builder.setLength(0);
            boolean minus = number < 0;
            if (minus) {
                builder.append("-");
                number = Math.abs(number);
            }

            String numberString = String.valueOf(number);
            int length = numberString.length();
            for (int i = 0; i < length; i++) {
                if (i > 0 && (length - i) % 3 == 0) builder.append(".");
                builder.append(numberString.charAt(i));
            }
            return builder.toString();
        }


        public static String formatPercent(float percent) {
            return String.format("%.0f%%", percent * 100f);

        }

        public static String formatPercentDecimal(float percentDecimal) {
            return String.format("%.2f%%", percentDecimal * 100f);
        }

        public static String fontSymbol(CMediaFontSingleSymbol cMediaFontSymbol, Color color) {
            return fontSymbol(cMediaFontSymbol.id, color);

        }

        public static String fontSymbol(CMediaFontSingleSymbol cMediaFontSymbol) {
            return fontSymbol(cMediaFontSymbol.id, Color.GRAY);
        }

        public static String fontSymbol(CMediaFontArraySymbol cMediaFontArraySymbol, Color color, int arrayIndex) {
            return fontSymbol(cMediaFontArraySymbol.ids[arrayIndex], color);
        }

        public static String fontSymbol(CMediaFontArraySymbol cMediaFontArraySymbol, int arrayIndex) {
            return fontSymbol(cMediaFontArraySymbol.ids[arrayIndex], Color.GRAY);
        }

        public static String fontSymbol(int id) {
            return fontSymbol(id, Color.GRAY);
        }

        public static String fontSymbol(int id, Color color) {
            String symbol = Character.toString((char) (MediaManager.FONT_CUSTOM_SYMBOL_OFFSET + id));
            if (color != null) {
                builder.setLength(0);
                builder.append("[#").append(color).append("]").append(symbol).append("[]");
                return builder.toString();
            } else {
                return symbol;
            }
        }

        public static String validString(String string) {
            return string == null ? "" : string;
        }

        public static String[] validStringArray(String[] string) {
            if (string == null) {
                return new String[]{};
            } else {
                String[] validString = new String[string.length];
                for (int i = 0; i < string.length; i++) {
                    if (string[i] != null) {
                        validString[i] = string[i];
                    } else {
                        validString[i] = "";
                    }
                }
                return string;
            }
        }

        public static String truncate(String input, int maxLength) {
            return input.length() <= maxLength ? input : input.substring(0, maxLength);
        }

    }

    public static class File {

        private static final String ZIP_ENTRY_NAME = "packed.data";

        public static void writeFrameBuffer(String fileName) {
            writeFrameBuffer(fileName, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }

        public static void writeFrameBuffer(String fileName, int width, int height) {
            Path path = Path.of(fileName);
            if (path.toFile().exists()) return;
            PixmapIO.writePNG(new FileHandle(path.toFile()), Pixmap.createFromFrameBuffer(0, 0, width, height));
        }

        public static String validFileName(String fileName) {
            if (fileName == null || fileName.trim().length() == 0) {
                fileName = "newFile";
            }
            fileName = fileName.replaceAll("[\\\\/:*?\"<>|]", "");
            fileName = fileName.trim();
            if (fileName.trim().length() == 0) {
                fileName = "newFile";
            }
            return fileName;
        }

        public static Path findNextValidFile(Path folder, String filename, String extension) {
            if (makeSureDirectoryExists(folder)) {
                filename = validFileName(filename);
                extension = extension == null || extension.length() == 0 ? "" : "." + extension;
                Path file;
                int count = 1;
                do {
                    String countExt = count == 1 ? "" : "_" + count;
                    file = Path.of(folder.toString(), filename + countExt + extension);
                    count++;
                } while (Files.exists(file));
                return file;
            } else {
                return null;
            }
        }

        public static boolean makeSureDirectoryExists(Path file) {
            try {
                if (Files.isRegularFile(file)) Files.delete(file);
                Files.createDirectories(file);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        public static void writeTextToFile(Path file, String content) {
            writeTextToFile(file, content, false);
        }

        public static void writeTextToFile(Path file, String content, boolean zipped) {
            try {
                Files.createDirectories(file.getParent());
                try (FileOutputStream fileOutputStream = new FileOutputStream(file.toFile())) {
                    if (zipped) {
                        ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
                        ZipEntry zipEntry = new ZipEntry(ZIP_ENTRY_NAME);
                        zipOutputStream.putNextEntry(zipEntry);
                        try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(zipOutputStream, StandardCharsets.UTF_8)) {
                            outputStreamWriter.write(content);
                            outputStreamWriter.flush();
                        }
                    } else {
                        try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8)) {
                            outputStreamWriter.write(content);
                            outputStreamWriter.flush();
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }

        public static String readTextFromFile(Path file, boolean zipped) {
            return readTextFromFile(file,zipped,0,Long.MAX_VALUE);
        }
        public static String readTextFromFile(Path file, boolean zipped, long skip, long limit) {
            try (FileInputStream fileInputStream = new FileInputStream(file.toFile())) {
                if (zipped) {
                    try (ZipInputStream zipInputStream = new ZipInputStream(fileInputStream)) {
                        ZipEntry zipEntry = zipInputStream.getNextEntry();
                        if (zipEntry != null && zipEntry.getName().equals(ZIP_ENTRY_NAME)) {
                            try (InputStreamReader inputStreamReader = new InputStreamReader(zipInputStream, StandardCharsets.UTF_8)) {
                                StringBuilder builder = new StringBuilder();
                                int ch;
                                int count = 0;
                                inputStreamReader.skip(skip);
                                while ((ch = inputStreamReader.read()) != -1) {
                                    builder.append((char) ch);
                                    if(count++ > limit)
                                        break;
                                }
                                return builder.toString();
                            }
                        }
                    }
                } else {
                    try (InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8)) {
                        StringBuilder builder = new StringBuilder();
                        int ch;
                        int count = 0;
                        inputStreamReader.skip(skip);
                        while ((ch = inputStreamReader.read()) != -1) {
                            builder.append((char) ch);
                            if(count++ > limit)
                                break;
                        }
                        return builder.toString();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            return null;
        }

        public static FileHandle findResource(String path) {
            return Gdx.files.internal(path);
        }

    }

    public static class Calc {

        public static float maxOfValues(float... values) {
            float sum = 0;
            for (float f : values) if (f > sum) sum = f;

            return sum;
        }

        public static float minOfValues(float... values) {
            float sum = Float.MAX_VALUE;
            for (float f : values) if (f < sum) sum = f;
            return sum;
        }

        public static int average(int... values) {
            float sum = 0;
            for (float n : values) sum = sum + n;
            return MathUtils.round(sum / values.length);
        }

        public static int average(float[] weights, int... values) {
            int sum = 0;
            for (int i = 0; i < values.length; i++) {
                sum = sum + MathUtils.round(values[i] * weights[i]);
            }
            return sum;
        }

        public static float average(float... values) {
            float sum = 0;
            for (float n : values) sum = sum + n;
            return sum / values.length;
        }

        public static float average(float[] weights, float... values) {
            float sum = 0;
            for (int i = 0; i < values.length; i++) {
                sum = sum + (values[i] * weights[i]);
            }
            return sum;
        }

        public static float normalizeDegree(float degree) {
            degree = degree % MathUtils.PI2;
            if (degree < 0) degree += MathUtils.PI2;
            return degree;
        }

        public static float percentAboveThreshold(long value, long max, int threshold) {
            value = Math.min(value, max);
            if (value > threshold) {
                long above = value - threshold;
                float divisor = (max - threshold);
                return divisor > 0 ? (above / divisor) : 0f;
            } else {
                return 0f;
            }
        }

        public static float percentBelowThreshold(long value, long min, int threshold) {
            value = Math.max(value, min);
            if (value < threshold) {
                long below = threshold - value;
                float divisor = (threshold - min);
                return divisor > 0 ? (below / divisor) : 0f;
            } else {
                return 0f;
            }
        }

        public static float percentAboveThreshold(float value, float max, float threshold) {
            value = Math.min(value, max);
            if (value > threshold) {
                float above = value - threshold;
                float divisor = (max - threshold);
                return divisor > 0 ? (above / divisor) : 0f;
            } else {
                return 0f;
            }
        }

        public static float percentBelowThreshold(float value, float min, float threshold) {
            value = Math.max(value, min);
            if (value < threshold) {
                float below = threshold - value;
                float divisor = (threshold - min);
                return divisor > 0 ? (below / divisor) : 0f;
            } else {
                return 0f;
            }
        }

        public static int randomApply(int value, float randomness) {
            if (randomness == 0) return value;
            randomness = Math.clamp(randomness, 0f, 1f);
            return MathUtils.round(value * MathUtils.random((1 - randomness), (1 + randomness)));
        }

        public static long randomApply(long value, float randomness) {
            if (randomness == 0) return value;
            randomness = Math.clamp(randomness, 0f, 1f);
            return MathUtils.round(value * MathUtils.random((1 - randomness), (1 + randomness)));
        }

        public static float randomApply(float value, float randomness) {
            if (randomness == 0) return value;
            randomness = Math.clamp(randomness, 0f, 1f);
            return value * MathUtils.random((1 - randomness), (1 + randomness));
        }

        public static long applyPercent(long value, float percent) {
            if (percent == 0) return value;
            value += MathUtils.round(value * percent);
            return value;
        }

        public static int applyPercent(int value, float percent) {
            if (percent == 0) return value;
            value += MathUtils.round(value * percent);
            return value;
        }

        public static float applyPercent(float value, float percent) {
            if (percent == 0) return value;
            value += (value * percent);
            return value;
        }

        public static int randomSelectProbabilities(int[] probabilities) {
            if (probabilities.length == 0) return -1;
            int sum = 0;
            for (int i = 0; i < probabilities.length; i++) sum += probabilities[i];

            if (sum <= 0f) return MathUtils.random(0,probabilities.length-1);

            int random = MathUtils.random(0, sum);
            int cumulative = 0;
            for (int i = 0; i < probabilities.length; i++) {
                if (probabilities[i] <= 0) continue;
                cumulative += probabilities[i];
                if (random <= cumulative) return i;
            }
            return -1;
        }

        public static int randomSelectProbabilities(float[] probabilities) {
            if (probabilities.length == 0) return -1;
            float sum = 0;
            for (int i = 0; i < probabilities.length; i++) sum += probabilities[i];

            if (sum <= 0f) return MathUtils.random(0,probabilities.length-1);

            float random = MathUtils.random(0f, sum);
            float cumulative = 0f;
            for (int i = 0; i < probabilities.length; i++) {
                if (probabilities[i] <= 0f) continue;
                cumulative += probabilities[i];
                if (random <= cumulative) return i;
            }
            return -1;
        }

        public static float randomRange(float[] range) {
            return MathUtils.random(range[0], range[1]);
        }

        public static int randomRange(int[] range) {
            return MathUtils.random(range[0], range[1]);
        }

        public static boolean randomChance(float probability) {
            return MathUtils.random(0f, 1f) < probability;
        }

        public static boolean randomChance(double probability) {
            return MathUtils.random(0f, 1f) < probability;
        }

        public static boolean randomChance(int oneIn) {
            if (oneIn <= 0) return false;
            return MathUtils.random(1, oneIn) == 1;
        }

        public static float lerpRange(float[] range, float pct) {
            return MathUtils.lerp(range[0],range[1],pct);
        }

        public static int lerpRange(int[] range, float pct) {
            return MathUtils.round(MathUtils.lerp(range[0],range[1],pct));
        }

        public static boolean randomChance(long oneIn) {
            if (oneIn <= 0) return false;
            return MathUtils.random(1, oneIn) == 1;
        }

        public static boolean randomChance() {
            return MathUtils.random(1, 2) == 1;
        }

        public static <T> T randomSelect(T[] array) {
            if (array == null || array.length == 0) return null;
            return array[MathUtils.random(0, array.length - 1)];
        }

        public static <T> T randomSelect(Array<T> array) {
            if (array == null || array.size == 0) return null;
            return array.get(MathUtils.random(0, array.size - 1));
        }

        public static long quadraticGrowth(long baseValue, int times, float divisor){
            final int timesMinus1 = times-1;
            long factor = timesMinus1 * timesMinus1;
            long addValue = (long) ((baseValue*factor)/divisor);
            return baseValue+addValue;
        }

        public static int quadraticGrowth(int baseValue, int times, float divisor){
            final int timesMinus1 = times-1;
            int factor = timesMinus1 * timesMinus1;
            int addValue = (int) ((baseValue*factor)/divisor);
            return baseValue+addValue;
        }

        public static int exponentialGrowth(int baseValue, float exp, int times) {
            if (times <= 1) return MathUtils.round(baseValue);
            return MathUtils.round(baseValue * (float) Math.pow(exp, times - 1));
        }

        public static long exponentialGrowth(long baseValue, float exp, int times) {
            if (times <= 1) return MathUtils.round(baseValue);
            return MathUtils.round(baseValue * (float) Math.pow(exp, times - 1));
        }

        public static float exponentialGrowth(float baseValue, float exp, int times) {
            if (times <= 1) return baseValue;
            return baseValue * (float) Math.pow(exp, times - 1);
        }

        public static int exponentialDecay(int baseValue, float exp, int times) {
            return MathUtils.round(baseValue * (float)Math.exp(-exp * (times-1)));
        }

        public static long exponentialDecay(long baseValue, float exp, int times) {
            return MathUtils.round(baseValue * (float)Math.exp(-exp * (times-1)));
        }

        public static float exponentialDecay(float baseValue, float exp, int times) {
            return (baseValue * (float) Math.exp(-exp * (times-1)));
        }

        public static int randomCountHits(float baseChance) {
            return randomCountHits(baseChance, 1f);
        }

        public static int randomCountHits(float baseChance, float chanceReduce) {
            int hits = 0;
            while (Tools.Calc.randomChance(baseChance)) {
                hits++;
                baseChance *= chanceReduce;
            }
            return hits;
        }


        private static final IntMap<LongArray> doInRadiusCache = new IntMap<>();

        public interface DoInRadiusFunction<O> {
            default boolean doInRadiusContinue(int x_center, int y_center, int x, int y, O data) {
                return false;
            }

            default boolean doInRadiusContinue(int x_center, int y_center, int x, int y) {
                return false;
            }
        }

        private static void doInRadiusInternal(int x, int y, int radius, DoInRadiusFunction radiusFunction) {
            for (int iy = -radius; iy <= radius; iy++) {
                for (int ix = -radius; ix <= radius; ix++) {
                    if ((ix * ix) + (iy * iy) <= (radius * radius)) {
                        if (!radiusFunction.doInRadiusContinue(x, y, x + ix, y + iy)) {
                            return;
                        }
                    }
                }
            }
        }

        public static void doInRadius(int x, int y, int radius, DoInRadiusFunction radiusFunction) {
            doInRadius(x, y, radius, radiusFunction, null);
        }

        public static <O> void doInRadius(int x, int y, int radius, DoInRadiusFunction<O> radiusFunction, O data) {
            LongArray cached = doInRadiusCache.get(radius);
            if (cached == null) {
                cached = new LongArray();
                // map from inside out
                for (int r = 0; r <= radius; r++) {
                    for (int iy = -r; iy <= r; iy++) {
                        for (int ix = -r; ix <= r; ix++) {
                            if ((ix * ix) + (iy * iy) <= (r * r)) {
                                int xr = ix;
                                int yr = iy;
                                cached.add((((long) xr) << 32) | (yr & 0xffffffffL));
                            }
                        }
                    }
                }
                doInRadiusCache.put(radius, cached);
            }

            if (data != null) {
                for (int i = 0; i < cached.size; i++) {
                    long positions = cached.get(i);
                    if (!radiusFunction.doInRadiusContinue(x, y, x + ((int) (positions >> 32)), y + ((int) positions), data))
                        return;
                }
            } else {
                for (int i = 0; i < cached.size; i++) {
                    long positions = cached.get(i);
                    if (!radiusFunction.doInRadiusContinue(x, y, x + ((int) (positions >> 32)), y + ((int) positions)))
                        return;
                }
            }

        }

        public static boolean isAdjacent(int x1, int y1, int x2, int y2, int map_size, boolean diagonal) {
            for (int x = x1 - 1; x <= x1 + 1; x++) {
                yloop:
                for (int y = y1 - 1; y <= y1 + 1; y++) {
                    if (x == x1 && y == y1) continue yloop; // middle
                    if (!diagonal) {
                        if (x == (x1 - 1) && y == (y1 - 1)) continue yloop;
                        if (x == (x1 + 1) && y == (y1 + 1)) continue yloop;
                        if (x == (x1 - 1) && y == (y1 + 1)) continue yloop;
                        if (x == (x1 + 1) && y == (y1 - 1)) continue yloop;
                    }
                    if (x >= 0 && y >= 0 && x < map_size && y < map_size) {
                        if (x == x2 && y == y2) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        public static int distance(int x1, int y1, int x2, int y2) {
            return MathUtils.floor((float) (Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2))));
        }

        public static float distance(float x1, float y1, float x2, float y2) {
            return (float) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
        }

        public static boolean isWithinDistance(int x1, int y1, int x2, int y2, int distance) {
            return distance(x1, y1, x2, y2) <= distance;
        }

        public static boolean isWithinDistance(float x1, float y1, float x2, float y2, float distance) {
            return distance(x1, y1, x2, y2) <= distance;
        }

        public static float degreeBetweenPoints(float x1, float y1, float x2, float y2) {
            return (MathUtils.atan2((y1 - y2), (x1 - x2))) + MathUtils.PI;
        }

        public static boolean rectsCollide(int Ax, int Ay, int Aw, int Ah, int Bx, int By, int Bw, int Bh) {
            return Ax < Bx + Bw && Ax + Aw > Bx &&
                    Ay < By + Bh && Ay + Ah > By;
        }

        public static boolean rectsCollide(float Ax, float Ay, float Aw, float Ah, float Bx, float By, float Bw, float Bh) {
            return Ax < Bx + Bw && Ax + Aw > Bx &&
                    Ay < By + Bh && Ay + Ah > By;
        }

        public static boolean pointRectsCollide(int pointX, int pointY, int Bx, int By, int Bw, int Bh) {
            return pointX >= Bx && pointX < Bx + Bw &&
                    pointY >= By && pointY < By + Bh;
        }

        public static boolean pointRectsCollide(float pointX, float pointY, float Bx, float By, float Bw, float Bh) {
            return pointX >= Bx && pointX < Bx + Bw &&
                    pointY >= By && pointY < By + Bh;
        }

        public static boolean circlesCollide(float x1, float y1, float r1, float x2, float y2, float r2) {
            return distance(x1, y1, x2, y2) <= (r1 + r2);
        }

    }

    public static class Reflection {
        /* Dont use these if you target HTML */
        public static boolean checkEngineDataValidity(Class checkClass) {
            if (Collection.class.isAssignableFrom(checkClass)) return false;
            if (!String.class.isAssignableFrom(checkClass)) return false;
            if (!Serializable.class.isAssignableFrom(checkClass)) return true;
            if (checkClass.getDeclaredMethods().length != 0) return true;
            Field[] fields = checkClass.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                if (!Modifier.isPublic(fields[i].getModifiers())) {
                    return true;
                } else {
                    if (!fields[i].getType().isPrimitive()) {
                        if (checkEngineDataValidity(fields[i].getType())) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        public static CMedia[] scanStaticClassForCMedia(Class loadFromClass) {
            Array<CMedia> prepareList = new Array<>();
            Field[] fields = loadFromClass.getFields();
            for (int i = 0; i < fields.length; i++) {
                CMedia cMedia;
                try {
                    if (fields[i].getType().isArray()) {
                        CMedia[] medias = (CMedia[]) fields[i].get(null);
                        prepareList.addAll(medias);
                    } else {
                        cMedia = (CMedia) fields[i].get(null);
                        prepareList.add(cMedia);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    return prepareList.toArray(CMedia[]::new);
                }
            }
            return prepareList.toArray(CMedia[]::new);
        }

        public static CMedia[] scanObjectForCMedia(Object object) {
            return scanObjectForCMedia(object, 3);
        }

        public static CMedia[] scanObjectForCMedia(Object object, int scanDepthMax) {
            Array<CMedia> prepareList = new Array<>();
            try {
                scanObjectForCMedia(object, scanDepthMax, 1, prepareList);
            } catch (Exception e) {
                e.printStackTrace();
                return new CMedia[]{};
            }
            return prepareList.toArray(CMedia[]::new);
        }

        private static void scanObjectForCMedia(Object object, int scanDepthMax, int currentDepth, Array<CMedia> prepareList) {
            if (object == null) return;
            if (object.getClass().getPackageName().startsWith("java")) return;
            if (currentDepth > scanDepthMax) return;
            if (CMedia.class.isAssignableFrom(object.getClass())) {
                CMedia cMedia = (CMedia) object;
                prepareList.add(cMedia);
                return;
            }

            Field[] fields = object.getClass().getFields();
            for (int i = 0; i < fields.length; i++) {
                Object fieldObject;
                try {
                    fieldObject = fields[i].get(object);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    return;
                }
                if (fieldObject != null) {
                    if (CMedia.class.isAssignableFrom(fieldObject.getClass())) {
                        CMedia cMedia = (CMedia) fieldObject;
                        prepareList.add(cMedia);
                    } else if (fieldObject.getClass() == Array.class) {
                        Array array = (Array) fieldObject;
                        for (int i2 = 0; i2 < array.size; i2++) {
                            scanObjectForCMedia(array.get(i2), scanDepthMax, currentDepth + 1, prepareList);
                        }
                    } else if (fields[i].getType().isArray()) {
                        if (fields[i].getType().getName().matches("\\[+L")) {
                            Object[] arrayObjects = (Object[]) fieldObject;
                            for (int i2 = 0; i2 < arrayObjects.length; i2++) {
                                scanObjectForCMedia(arrayObjects[i2], scanDepthMax, currentDepth + 1, prepareList);
                            }
                        }
                    } else {
                        scanObjectForCMedia(fieldObject, scanDepthMax, currentDepth + 1, prepareList);
                    }
                }
            }
        }
    }
}
