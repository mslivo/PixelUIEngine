package net.mslivo.core.engine.tools;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.LongArray;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import net.mslivo.core.engine.media_manager.*;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
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
        private static ArrayDeque<ForkJoinTask> parallelTaskList = new ArrayDeque<>();

        public static void runParallel(int[] array, IntConsumer consumer) {
            runParallel(array, consumer, array.length);
        }

        public static void runParallel(int[] array, IntConsumer consumer, int size) {
            if (array.length == 0) return;
            final int parallelism = ForkJoinPool.commonPool().getParallelism();
            final int listSize = size;

            int taskCount = Math.min(parallelism, listSize);
            int chunkSize = MathUtils.ceil(listSize / (float) taskCount);

            for (int i = 0; i < taskCount; i++) {
                int start = i * chunkSize;
                int end = Math.min(start + chunkSize, listSize);

                ForkJoinTask forkJoinTask = ForkJoinPool.commonPool().submit(() -> {
                    for (int i2 = start; i2 < end; i2++) {
                        consumer.accept(array[i2]);
                    }
                });
                parallelTaskList.add(forkJoinTask);
            }

            while (!parallelTaskList.isEmpty())
                parallelTaskList.poll().join();
        }

        public static <T> void runParallel(List<T> list, Consumer<T> consumer) {
            runParallel(list, consumer, list.size());
        }

        public static <T> void runParallel(List<T> list, Consumer<T> consumer, int size) {
            if (list.size() == 0) return;
            final int parallelism = ForkJoinPool.commonPool().getParallelism();
            final int listSize = size;

            int taskCount = Math.min(parallelism, listSize);
            int chunkSize = MathUtils.ceil(listSize / (float) taskCount);

            for (int i = 0; i < taskCount; i++) {
                int start = i * chunkSize;
                int end = Math.min(start + chunkSize, listSize);

                ForkJoinTask forkJoinTask = ForkJoinPool.commonPool().submit(() -> {
                    for (int i2 = start; i2 < end; i2++) {
                        consumer.accept(list.get(i2));
                    }
                });
                parallelTaskList.add(forkJoinTask);
            }

            while (!parallelTaskList.isEmpty())
                parallelTaskList.poll().join();
        }

        public static void exceptionToErrorLogFile(Exception e) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(ERROR_LOG_FILE.toString(), true))) {
                pw.write("Exception \"" + (e.getClass().getSimpleName()) + "\" occured" + System.lineSeparator());
                e.printStackTrace(pw);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        public static void exceptionToDialog(Exception e){
            String stackTrace;
            try(ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()){
                try(PrintWriter printWriter = new PrintWriter(byteArrayOutputStream)){
                    e.printStackTrace(printWriter);
                    printWriter.flush();
                    stackTrace = byteArrayOutputStream.toString();
                }
            } catch (IOException ex) {
                stackTrace = e.toString();
            }
            StringBuilder shownStackTrace = new StringBuilder(stackTrace);
            if(shownStackTrace.length() > 512){
                shownStackTrace.setLength(512);
                shownStackTrace.append(System.lineSeparator()).append("...");
            }
            shownStackTrace.append(System.lineSeparator()).append("Press OK to copy to Clipboard");

            int option = JOptionPane.showConfirmDialog(null, shownStackTrace.toString(), "Exception", JOptionPane.PLAIN_MESSAGE);
            if(option == JOptionPane.OK_OPTION){
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(stackTrace),null);
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

        public static void launch(ApplicationAdapter applicationAdapter, String appTile, int resolutionWidth, int resolutionHeight, String iconPath, int fps, boolean vSync) {
            Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
            config.setResizable(true);
            boolean linux32Bit = UIUtils.isLinux && !SharedLibraryLoader.is64Bit;

            config.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.GL32, 3, 2);

            config.setWindowedMode(resolutionWidth, resolutionHeight);
            config.setWindowSizeLimits(resolutionWidth, resolutionHeight, -1, -1);
            config.setTitle(appTile);
            config.setDecorated(true);
            config.setMaximized(true);
            config.setForegroundFPS(fps);
            config.setIdleFPS(fps);
            config.useVsync(vSync);
            config.setWindowPosition(-1, -1);
            config.setBackBufferConfig(8, 8, 8, 8, 16, 0, 0);
            if (iconPath != null) config.setWindowIcon(iconPath);
            try {
                new Lwjgl3Application(applicationAdapter, config);
            } catch (Exception e) {
                e.printStackTrace();
                Tools.App.exceptionToErrorLogFile(e);
                Tools.App.exceptionToDialog(e);
            }
        }
    }


    public static class Text {

        private static final StringBuilder builder = new StringBuilder();

        public static String benchmark(String... customValues) {
            builder.setLength(0);
            StringBuilder custom = new StringBuilder();
            for (int i = 0; i < customValues.length; i++)
                custom.append(" | ").append(String.format("%1$10s", customValues[i]));
            builder.append(String.format("%1$6s", Gdx.graphics.getFramesPerSecond()));
            builder.append(" FPS | ");
            builder.append(String.format("%1$6s", ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024))));
            builder.append("MB RAM | ");
            builder.append(String.format("%1$6s", (Thread.getAllStackTraces().keySet().size())));
            builder.append(" Threads");
            builder.append(custom);
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

        public static String fontSymbol(CMediaFontSingleSymbol cMediaFontSymbol, boolean neutralColor) {
            return fontSymbol(cMediaFontSymbol.id, neutralColor);
        }

        public static String fontSymbol(CMediaFontSingleSymbol cMediaFontSymbol) {
            return fontSymbol(cMediaFontSymbol.id, true);
        }

        public static String fontSymbol(CMediaFontArraySymbol cMediaFontArraySymbol, boolean neutralColor, int arrayIndex) {
            return fontSymbol(cMediaFontArraySymbol.ids[arrayIndex], neutralColor);
        }

        public static String fontSymbol(CMediaFontArraySymbol cMediaFontArraySymbol, int arrayIndex) {
            return fontSymbol(cMediaFontArraySymbol.ids[arrayIndex], true);
        }

        public static String fontSymbol(int id) {
            return fontSymbol(id, true);
        }

        public static String fontSymbol(int id, boolean neutralColor) {
            String symbol = Character.toString((char) (MediaManager.FONT_CUSTOM_SYMBOL_OFFSET + id));
            if(neutralColor){
                builder.setLength(0);
                builder.append("[#7F7F7F]").append(symbol).append("[]");
                return builder.toString();
            }else{
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
            Path path = Path.of(fileName);
            if (path.toFile().exists()) return;
            PixmapIO.writePNG(new FileHandle(path.toFile()), Pixmap.createFromFrameBuffer(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        }

        public static String validFileName(String fileName) {
            if (fileName == null || fileName.trim().length() == 0) {
                fileName = "new_file";
            }
            fileName = fileName.replaceAll("[\\\\/:*?\"<>|]", "");
            fileName = fileName.trim();
            if (fileName.trim().length() == 0) {
                fileName = "new_file";
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


        public static Object readObjectFromFile(Path file) {
            return readObjectFromFile(file, false, null);
        }

        public static Object readObjectFromFile(Path file, boolean zipped) {
            return readObjectFromFile(file, zipped, null);
        }

        public static Object readObjectFromFile(Path file, boolean zipped, HashMap<String, String> classReplacements)  {
            try (FileInputStream fileInputStream = new FileInputStream(file.toFile())) {
                if (zipped) {
                    try (ZipInputStream zipInputStream = new ZipInputStream(fileInputStream)) {
                        ZipEntry zipEntry = zipInputStream.getNextEntry();
                        if (zipEntry != null && zipEntry.getName().equals(ZIP_ENTRY_NAME)) {
                            try (HackedObjectInputStream objectInputStream = new HackedObjectInputStream(zipInputStream, classReplacements)) {
                                Object readObject = objectInputStream.readObject();
                                return readObject;
                            }
                        }
                    }
                } else {
                    try (HackedObjectInputStream objectInputStream = new HackedObjectInputStream(fileInputStream, classReplacements)) {
                        Object readObject = objectInputStream.readObject();
                        return readObject;
                    }
                }
                return null;
            }catch (Exception e){
                throw new RuntimeException(e);
            }
        }

        public static void writeObjectToFile(Path file, Object data)  {
            writeObjectToFile(file, data, false);
        }

        public static void writeObjectToFile(Path file, Object data, boolean zipped)  {
            try {
                Files.createDirectories(file.getParent());
                try (FileOutputStream fileOutputStream = new FileOutputStream(file.toFile())) {
                    if (zipped) {
                        ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
                        ZipEntry zipEntry = new ZipEntry(ZIP_ENTRY_NAME);
                        zipOutputStream.putNextEntry(zipEntry);
                        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(zipOutputStream)) {
                            objectOutputStream.writeObject(data);
                            objectOutputStream.flush();
                        }
                        zipOutputStream.close();
                    } else {
                        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {
                            objectOutputStream.writeObject(data);
                            objectOutputStream.flush();
                        }
                    }
                }
            }catch (Exception e){
                throw new RuntimeException(e);
            }
        }

        public static void writeTextToFile(Path file, String text)  {
            writeTextToFile(file, text, false);
        }

        public static void writeTextToFile(Path file, String text, boolean zipped)  {
            try {
                Files.createDirectories(file.getParent());
                try (FileOutputStream fileOutputStream = new FileOutputStream(file.toFile())) {
                    if (zipped) {
                        ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
                        ZipEntry zipEntry = new ZipEntry(ZIP_ENTRY_NAME);
                        zipOutputStream.putNextEntry(zipEntry);
                        try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(zipOutputStream, StandardCharsets.UTF_8)) {
                            outputStreamWriter.write(text);
                            outputStreamWriter.flush();
                        }
                    } else {
                        try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8)) {
                            outputStreamWriter.write(text);
                            outputStreamWriter.flush();
                        }
                    }
                }
            }catch (Exception e) {
                throw new RuntimeException(e);
            }

        }

        public static String readTextFromFile(Path file, boolean zipped) {
            try (FileInputStream fileInputStream = new FileInputStream(file.toFile())) {
                if (zipped) {
                    try (ZipInputStream zipInputStream = new ZipInputStream(fileInputStream)) {
                        ZipEntry zipEntry = zipInputStream.getNextEntry();
                        if (zipEntry != null && zipEntry.getName().equals(ZIP_ENTRY_NAME)) {
                            try (InputStreamReader inputStreamReader = new InputStreamReader(zipInputStream, StandardCharsets.UTF_8)) {
                                StringBuilder builder = new StringBuilder();
                                int ch;
                                while ((ch = inputStreamReader.read()) != -1) {
                                    builder.append((char) ch);
                                }
                                return builder.toString();
                            }
                        }
                    }
                } else {
                    try (InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8)) {
                        StringBuilder builder = new StringBuilder();
                        int ch;
                        while ((ch = inputStreamReader.read()) != -1) {
                            builder.append((char) ch);
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

        private static class HackedObjectInputStream extends ObjectInputStream {

            private final HashMap<String, String> classReplacements;

            public HackedObjectInputStream(final InputStream stream, HashMap<String, String> classReplacements) throws IOException {
                super(stream);
                this.classReplacements = classReplacements;
            }

            @Override
            protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
                ObjectStreamClass resultClassDescriptor = super.readClassDescriptor();
                if (classReplacements == null) return resultClassDescriptor;

                // Replace Class
                String cName = resultClassDescriptor.getName();
                if (classReplacements.get(cName) != null) {
                    resultClassDescriptor = ObjectStreamClass.lookup(Class.forName(classReplacements.get(cName)));
                } else if (cName.startsWith("[")) {
                    // Replace Arrays of Class
                    int arraySizeI = 0;
                    while (arraySizeI < cName.length() && cName.charAt(arraySizeI) == '[') arraySizeI++;
                    String realCName = cName.substring(arraySizeI + 1, cName.length() - 1);
                    if (classReplacements.get(realCName) != null) {
                        Class newClass = Class.forName(classReplacements.get(realCName));
                        for (int i = 0; i < arraySizeI; i++) newClass = newClass.arrayType();
                        resultClassDescriptor = ObjectStreamClass.lookup(newClass);
                    }
                }
                return resultClassDescriptor;
            }

        }

    }

    public static class Calc {

        public static float maxOfValues(float[] values) {
            float sum = 0;
            for (float f : values) if (f > sum) sum = f;
            return sum;
        }

        public static float minOfValues(float[] values) {
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

            int random = MathUtils.random(0, sum);
            int cumulative = 0;
            for (int i = 0; i < probabilities.length; i++) {
                cumulative += probabilities[i];
                if (random <= cumulative) return i;
            }
            return -1;
        }

        public static int randomSelectProbabilities(float[] probabilities) {
            if (probabilities.length == 0) return -1;
            float sum = 0;
            for (int i = 0; i < probabilities.length; i++) sum += probabilities[i];

            float random = MathUtils.random(0f, sum);
            float cumulative = 0f;
            for (int i = 0; i < probabilities.length; i++) {
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

        public static <T> T randomSelect(List<T> list) {
            if (list == null || list.size() == 0) return null;
            return list.get(MathUtils.random(0, list.size() - 1));
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

        public static float distanceFast(float x1, float y1, float x2, float y2) {
            float dx = x2 - x1;
            float dy = y2 - y1;
            float x = dx * dx + dy * dy;
            float xhalf = 0.5f * x;
            int i = Float.floatToIntBits(x);
            i = 0x5f3759df - (i >> 1);
            x = Float.intBitsToFloat(i);
            x = x * (1.5f - xhalf * x * x);
            return 1.0f / x;
        }

        public static float distanceFast(int x1, int y1, int x2, int y2) {
            return distanceFast((float) x1, (float) y1, (float) x2, (float) y2);
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

        public static boolean isWithinDistanceFast(float x1, float y1, float x2, float y2, float distance) {
            return distanceFast(x1, y1, x2, y2) <= distance;
        }

        public static boolean isWithinDistanceFast(int x1, int y1, int x2, int y2, int radius) {
            return distanceFast(x1, y1, x2, y2) <= radius;
        }

        public static float degreeBetweenPoints(float x1, float y1, float x2, float y2) {
            return (MathUtils.atan2((y1 - y2), (x1 - x2))) + MathUtils.PI;
        }

        public static boolean rectsCollide(int Ax, int Ay, int Aw, int Ah, int Bx, int By, int Bw, int Bh) {
            return Bx + Bw >= Ax && By + Bh >= Ay && Ax + Aw >= Bx && Ay + Ah >= By;
        }

        public static boolean rectsCollide(float Ax, float Ay, float Aw, float Ah, float Bx, float By, float Bw, float Bh) {
            return Bx + Bw >= Ax && By + Bh >= Ay && Ax + Aw >= Bx && Ay + Ah >= By;
        }

        public static boolean pointRectsCollide(int pointX, int pointY, int Bx, int By, int Bw, int Bh) {
            return Bx + Bw >= pointX && By + Bh >= pointY && (pointX+1) >= Bx && (pointY+1) >= By;
        }

        public static boolean pointRectsCollide(float pointX, float pointY, float Bx, float By, float Bw, float Bh) {
            return Bx + Bw >= pointX && By + Bh >= pointY && (pointX + 1) >= Bx && (pointY + 1) >= By;
        }

        public static boolean circlesCollide(float x1, float y1, float r1, float x2, float y2, float r2) {
            return distance(x1, y1, x2, y2) <= (r1 + r2);
        }

        public static boolean circleCollideFast(float x1, float y1, float r1, float x2, float y2, float r2) {
            return distanceFast(x1, y1, x2, y2) <= (r1 + r2);
        }

        public static boolean circlesCollide(int x1, int y1, int r1, int x2, int y2, int r2) {
            return distance(x1, y1, x2, y2) <= (r1 + r2);
        }

        public static boolean circleCollideFast(int x1, int y1, int r1, int x2, int y2, int r2) {
            return distanceFast(x1, y1, x2, y2) <= (r1 + r2);
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
            ArrayList<CMedia> prepareList = new ArrayList<>();
            Field[] fields = loadFromClass.getFields();
            for (int i = 0; i < fields.length; i++) {
                CMedia cMedia;
                try {
                    if (fields[i].getType().isArray()) {
                        CMedia[] medias = (CMedia[]) fields[i].get(null);
                        prepareList.addAll(Arrays.asList(medias));
                    } else {
                        cMedia = (CMedia) fields[i].get(null);
                        prepareList.add(cMedia);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    return prepareList.toArray(new CMedia[]{});
                }
            }
            return prepareList.toArray(new CMedia[]{});
        }

        public static CMedia[] scanObjectForCMedia(Object object) {
            return scanObjectForCMedia(object, 3);
        }

        public static CMedia[] scanObjectForCMedia(Object object, int scanDepthMax) {
            ArrayList<CMedia> prepareList = new ArrayList<>();
            try {
                scanObjectForCMedia(object, scanDepthMax, 1, prepareList);
            } catch (Exception e) {
                e.printStackTrace();
                return new CMedia[]{};
            }
            return prepareList.toArray(new CMedia[]{});
        }

        private static void scanObjectForCMedia(Object object, int scanDepthMax, int currentDepth, ArrayList<CMedia> prepareList) {
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
                    } else if (fieldObject.getClass() == ArrayList.class) {
                        ArrayList arrayList = (ArrayList) fieldObject;
                        for (int i2 = 0; i2 < arrayList.size(); i2++) {
                            scanObjectForCMedia(arrayList.get(i2), scanDepthMax, currentDepth + 1, prepareList);
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
