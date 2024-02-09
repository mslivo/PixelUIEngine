package org.mslivo.core.engine.tools;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.badlogic.gdx.utils.ObjectMap;
import org.mslivo.core.engine.media_manager.media.CMedia;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Utility Class
 */
public class Tools {

    private static final DecimalFormat decimalFormat_2decimal = new DecimalFormat("#.##");

    private static final DecimalFormat decimalFormat_3decimal = new DecimalFormat("#.###");

    private static final DecimalFormat decimalFormat_4decimal = new DecimalFormat("#.####");

    private static final DecimalFormat decimalFormat_5decimal = new DecimalFormat("#.#####");

    private static final DecimalFormat decimalFormat_6decimal = new DecimalFormat("#.######");

    private static final SimpleDateFormat sdf = new SimpleDateFormat("[dd.MM.yy][HH:mm:ss] ");

    public static class Update {
        private static float skipFrameAccumulator = 0f;
        private static int maxUpdatesPerSecond = 60;
        private static float timeStep;
        private static float timeStepX2;

        public static void setUpdatesPerSecond(int maxUpdatesPerSecond){
            Update.maxUpdatesPerSecond = Tools.Calc.lowerBounds(maxUpdatesPerSecond,1);
            timeStep = (1f/(float) Update.maxUpdatesPerSecond);
            timeStepX2 = timeStep*2f;
            skipFrameAccumulator = 0;
        }

        public static boolean runUpdate() {
            // Accumulate 2 frames max
            skipFrameAccumulator = Tools.Calc.upperBounds(skipFrameAccumulator+Gdx.graphics.getDeltaTime(), timeStepX2);
            if (skipFrameAccumulator < timeStep) {
                return false;
            } else {
                skipFrameAccumulator -= timeStep;
                return true;
            }
        }

    }

    public static class Log {
        private static StringBuilder logMessageBuilder = new StringBuilder();
        private static String timestamp() {
            return sdf.format(new Date());
        }

        public static void benchmark(String... customValues) {
            StringBuilder custom = new StringBuilder();
            for (int i = 0; i < customValues.length; i++)
                custom.append(" | ").append(String.format("%1$10s", customValues[i]));
            Tools.Log.message(String.format("%1$3s", Gdx.graphics.getFramesPerSecond()) + " FPS | " +
                    String.format("%1$6s", ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024))) + "MB RAM" + custom);
        }

        public static void message(String msg) {
            logMessageBuilder.setLength(0);
            logMessageBuilder.append(Text.ANSI_BLUE).append(timestamp()).append(Text.ANSI_RESET).append(msg);
            System.out.println(logMessageBuilder.toString());
        }

        public static void message(String msg, Object values) {
            logMessageBuilder.setLength(0);
            logMessageBuilder.append(Text.ANSI_BLUE).append(timestamp()).append(Text.ANSI_RESET).append(String.format(msg, values));
            System.out.println(logMessageBuilder.toString());
        }

        public static void error(String msg) {
            logMessageBuilder.setLength(0);
            logMessageBuilder.append(Text.ANSI_RED).append(timestamp()).append( "error:").append(msg).append(Text.ANSI_RESET);
            System.err.println(logMessageBuilder.toString());
        }

        public static void error(Exception e) {
            logMessageBuilder.setLength(0);
            logMessageBuilder.append(Text.ANSI_RED).append(timestamp()).append(e.getClass().getSimpleName()).append(Text.ANSI_RESET);
            System.err.println(logMessageBuilder.toString());
            e.printStackTrace();
        }

        public static void inProgress(String what) {
            logMessageBuilder.setLength(0);
            logMessageBuilder.append(Text.ANSI_BLUE).append(timestamp()).append(Text.ANSI_RESET).append(what).append("...");
            System.out.println(logMessageBuilder);
        }

        public static void done() {
            logMessageBuilder.setLength(0);
            logMessageBuilder.append(Text.ANSI_BLUE).append(timestamp()).append(Text.ANSI_RESET).append("Done.");
            System.out.println(logMessageBuilder);
        }

        public static void toFile(String message, Path file) {
            try {
                FileWriter fileWriter = new FileWriter(file.toString(), true);
                PrintWriter printWriter = new PrintWriter(fileWriter);
                printWriter.write(timestamp() + message);
                printWriter.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        public static void toFile(Exception e, Path file) {
            try {
                FileWriter fileWriter = new FileWriter(file.toString(), true);
                PrintWriter printWriter = new PrintWriter(fileWriter);
                printWriter.write(timestamp());
                e.printStackTrace(printWriter);
                printWriter.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }

    public static class Text {

        public static final String ANSI_RESET = "\u001B[0m";
        public static final String ANSI_BLACK = "\u001B[30m";
        public static final String ANSI_RED = "\u001B[31m";
        public static final String ANSI_GREEN = "\u001B[32m";
        public static final String ANSI_YELLOW = "\u001B[33m";
        public static final String ANSI_BLUE = "\u001B[34m";
        public static final String ANSI_PURPLE = "\u001B[35m";
        public static final String ANSI_CYAN = "\u001B[36m";
        public static final String ANSI_WHITE = "\u001B[37m";
        public static final String ANSI_BACK_BLACK = "\u001B[40m";
        public static final String ANSI_BACK_RED = "\u001B[41m";
        public static final String ANSI_BACK_GREEN = "\u001B[42m";
        public static final String ANSI_BACK_YELLOW = "\u001B[43m";
        public static final String ANSI_BACK_BLUE = "\u001B[44m";
        public static final String ANSI_BACK_PURPLE = "\u001B[45m";
        public static final String ANSI_BACK_CYAN = "\u001B[46m";
        public static final String ANSI_BACK_WHITE = "\u001B[47m";

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
            StringBuilder formattedNumber = new StringBuilder();
            String numberString = String.valueOf(number);
            int length = numberString.length();

            for (int i = 0; i < length; i++) {
                if (i > 0 && (length - i) % 3 == 0) {
                    formattedNumber.append(".");
                }
                formattedNumber.append(numberString.charAt(i));
            }

            return formattedNumber.toString();
        }

        public static String format2Decimal(float decimal) {
            return decimalFormat_2decimal.format(decimal);
        }

        public static String format3Decimal(float decimal) {
            return decimalFormat_3decimal.format(decimal);
        }

        public static String format4Decimal(float decimal) {
            return decimalFormat_4decimal.format(decimal);
        }

        public static String format5Decimal(float decimal) {
            return decimalFormat_5decimal.format(decimal);
        }

        public static String format6Decimal(float decimal) {
            return decimalFormat_6decimal.format(decimal);
        }

        public static String formatPercent2Decimal(float percentF) {
            return format2Decimal(percentF * 100) + "%";
        }

        public static String formatPercent3Decimal(float percentF) {
            return format3Decimal(percentF * 100) + "%";
        }

        public static String formatPercent4Decimal(float percentF) {
            return format4Decimal(percentF * 100) + "%";
        }

        public static String formatPercent5Decimal(float percentF) {
            return format5Decimal(percentF * 100) + "%";
        }

        public static String formatPercent6Decimal(float percentF) {
            return format6Decimal(percentF * 100) + "%";
        }

        public static String formatPercent(float percentF) {
            return MathUtils.round(percentF * 100) + "%";
        }

        public static String customChar(int number) {
            return (Character.toString((char) (500 + number)));
        }

        public static String validString(String string) {
            return string == null ? "" : string;
        }

        public static String[] validString(String[] string) {
            return string == null ? new String[]{} : string;
        }

        public static String truncateString(String input, int maxLength) {
            if (input.length() <= maxLength) {
                return input;
            } else {
                return input.substring(0, maxLength);
            }
        }

    }

    public static class File {

        public static String makeFileNameValid(String fileName) {
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
                filename = makeFileNameValid(filename);
                extension = extension == null || extension.length() == 0 ? "" : "." + extension;
                Path file;
                int count = 1;
                do {
                    String countExt = count == 1 ? "" : "_" + count;
                    file = Path.of(folder.toString(), filename
                            + countExt
                            + extension);
                    count++;
                } while (Files.exists(file));
                return file;
            } else {
                return null;
            }
        }


        public static boolean makeSureDirectoryExists(Path file) {
            try {
                if (Files.isRegularFile(file)) {
                    Files.delete(file);
                }
                Files.createDirectories(file);
                return true;
            } catch (IOException e) {
                Tools.Log.error(e);
                return false;
            }
        }

        public static void writeObjectToFile(Object data, Path file) throws Exception {
            Files.createDirectories(file.getParent());
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(Files.newOutputStream(file));
            objectOutputStream.writeObject(data);
            objectOutputStream.flush();
            objectOutputStream.close();
        }

        public static Object readObjectFromFile(Path file) throws Exception {
            return readObjectFromFile(file, null);
        }


        public static Object readObjectFromFile(Path file, HashMap<String, String> classReplacements) throws Exception {
            try (HackedObjectInputStream objectInputStream = new HackedObjectInputStream(Files.newInputStream(file), classReplacements)) {
                Object ret = objectInputStream.readObject();
                objectInputStream.close();
                return ret;
            } catch (Exception e) {
                throw e;
            }
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

        public static long intsToLong(int int1, int int2) {
            return (((long) int1) << 32) | (int2 & 0xffffffffL);
        }

        public static int[] longToInts(long longValue) {
            int[] ret = new int[2];
            ret[0] = ((int) (longValue >> 32));
            ret[1] = (int) longValue;
            return ret;
        }

        public static float percentAboveThreshold(long value, long max, int threshold) {
            value = Tools.Calc.upperBounds(value, max);
            if (value > threshold) {
                long above = value - threshold;
                float divisor = (max - threshold);
                return divisor > 0 ? (above / divisor) : 0f;
            } else {
                return 0f;
            }
        }

        public static float percentBelowThreshold(long value, long min, int threshold) {
            value = Tools.Calc.lowerBounds(value, min);
            if (value < threshold) {
                long below = threshold - value;
                float divisor = (threshold - min);
                return divisor > 0 ? (below / divisor) : 0f;
            } else {
                return 0f;
            }
        }

        public static float percentAboveThreshold(float value, float max, float threshold) {
            value = Tools.Calc.upperBounds(value, max);
            if (value > threshold) {
                float above = value - threshold;
                float divisor = (max - threshold);
                return divisor > 0 ? (above / divisor) : 0f;
            } else {
                return 0f;
            }
        }

        public static float percentBelowThreshold(float value, float min, float threshold) {
            value = Tools.Calc.lowerBounds(value, min);
            if (value < threshold) {
                float below = threshold - value;
                float divisor = (threshold - min);
                return divisor > 0 ? (below / divisor) : 0f;
            } else {
                return 0f;
            }
        }

        public static int applyRandomness(int value, float randomness) {
            if (randomness == 0) return value;
            randomness = Tools.Calc.inBounds(randomness, 0f, 1f);
            return MathUtils.round(value * MathUtils.random((1 - randomness), (1 + randomness)));
        }

        public static long applyRandomness(long value, float randomness) {
            if (randomness == 0) return value;
            randomness = Tools.Calc.inBounds(randomness, 0f, 1f);
            return MathUtils.round(value * MathUtils.random((1 - randomness), (1 + randomness)));
        }

        public static float applyRandomness(float value, float randomness) {
            if (randomness == 0) return value;
            randomness = Tools.Calc.inBounds(randomness, 0f, 1f);
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

        public static float inBounds(float value, float lower, float upper) {
            return value < lower ? lower : (value > upper ? upper : value);
        }

        public static double inBounds(double value, double lower, double upper) {
            return value < lower ? lower : (value > upper ? upper : value);
        }

        public static long inBounds(long value, long lower, long upper) {
            return value < lower ? lower : (value > upper ? upper : value);
        }

        public static int inBounds(int value, int lower, int upper) {
            return value < lower ? lower : (value > upper ? upper : value);
        }

        public static float inBounds01(float value) {
            return Tools.Calc.inBounds(value, 0f, 1f);
        }

        public static double upperBounds(double value, double upper) {
            return value > upper ? upper : value;
        }

        public static long upperBounds(long value, long upper) {
            return value > upper ? upper : value;
        }

        public static float upperBounds(float value, float upper) {
            return value > upper ? upper : value;
        }

        public static int upperBounds(int value, int upper) {
            return value > upper ? upper : value;
        }

        public static double lowerBounds(double value, double lower) {
            return value < lower ? lower : value;
        }

        public static int lowerBounds(int value, int lower) {
            return value < lower ? lower : value;
        }

        public static float lowerBounds(float value, float lower) {
            return value < lower ? lower : value;
        }

        public static long lowerBounds(long value, long lower) {
            return value < lower ? lower : value;
        }

        public static Object selectRandom(List list) {
            return list.get(MathUtils.random(0, list.size() - 1));
        }

        public static int selectRandom(int... probabilities) {
            int sum = 0;
            for (int i = 0; i < probabilities.length; i++) sum += probabilities[i];
            float[] probabilitiesF = new float[probabilities.length];
            for (int i = 0; i < probabilitiesF.length; i++) {
                probabilitiesF[i] = sum == 0 ? (1f / (float) probabilitiesF.length) : (probabilities[i] / (float) sum);
            }

            return selectRandom(probabilitiesF);
        }

        public static int selectRandom(float... probabilities) {
            if (probabilities.length == 0) return -1;
            float random = MathUtils.random(0f, 1f);
            float cumulativeProbability = 0f;
            for (int i = 0; i < probabilities.length; i++) {
                cumulativeProbability += probabilities[i];
                if (random <= cumulativeProbability) return i;
            }
            return -1; // probabilities must add up to 1f!
        }

        public static boolean chance(float probability) {
            return MathUtils.random(0f, 1f) < probability;
        }

        public static boolean chance(double probability) {
            return MathUtils.random(0f, 1f) < probability;
        }

        public static boolean chance(int oneIn) {
            oneIn = lowerBounds(oneIn, 1);
            return MathUtils.random(1, oneIn) == 1;
        }

        public static boolean chance(long oneIn) {
            oneIn = lowerBounds(oneIn, 1);
            return MathUtils.random(1, oneIn) == 1;
        }

        public static <T> T chooseRandom(T[] array) {
            if (array == null || array.length == 0) return null;
            return array[MathUtils.random(0, array.length - 1)];
        }

        public static <T> T chooseRandom(List<T> list) {
            if (list == null || list.size() == 0) return null;
            return list.get(MathUtils.random(0, list.size() - 1));
        }

        private static final IntMap<ArrayList<Long>> doInRadiusCache = new IntMap<>();

        public interface DoInRadiusFunction {
            boolean apply(int x, int y);
        }

        private static void doInRadiusInternal(int x, int y, int radius, DoInRadiusFunction radiusFunction) {
            for (int iy = -radius; iy <= radius; iy++) {
                for (int ix = -radius; ix <= radius; ix++) {
                    if ((ix * ix) + (iy * iy) <= (radius * radius)) {
                        if (!radiusFunction.apply(x + ix, y + iy)) {
                            return;
                        }
                    }
                }
            }
        }

        public static void doInRadius(int x, int y, int radius, DoInRadiusFunction radiusFunction) {
            ArrayList<Long> cached = doInRadiusCache.get(radius);
            if (cached == null) {
                cached = new ArrayList<>();
                ArrayList<Long> finalCached = cached;
                doInRadiusInternal(0, 0, radius, (x1, y1) -> {
                    finalCached.add(
                            (((long) x1) << 32) | (y1 & 0xffffffffL));
                    return true;
                });
                doInRadiusCache.put(radius, cached);
            }

            for (int i = 0; i < cached.size(); i++) {
                Long positions = cached.get(i);
                if (!radiusFunction.apply(
                        x + ((int) (positions >> 32)),
                        y + positions.intValue())
                ) {
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

        public static boolean isInDistance(int x1, int y1, int x2, int y2, int radius) {
            return distance(x1, y1, x2, y2) <= radius;
        }

        public static boolean isInDistance(float x1, float y1, float x2, float y2, float radius) {
            return distance(x1, y1, x2, y2) <= radius;
        }

        public static boolean isInDistanceFast(float x1, float y1, float x2, float y2, float radius) {
            return distanceFast(x1, y1, x2, y2) <= radius;
        }

        public static boolean isInDistanceFast(int x1, int y1, int x2, int y2, int radius) {
            return distanceFast(x1, y1, x2, y2) <= radius;
        }

        public static float degreeBetweenPoints(float x1, float y1, float x2, float y2) {
            return (MathUtils.atan2((y1 - y2), (x1 - x2))) + MathUtils.PI;
        }

        public static boolean rectsCollide(int Ax, int Ay, int Aw, int Ah, int Bx, int By, int Bw, int Bh) {
            return Bx + Bw > Ax &&
                    By + Bh > Ay &&
                    Ax + Aw > Bx &&
                    Ay + Ah > By;
        }

        public static boolean rectsCollide(float Ax, float Ay, float Aw, float Ah, float Bx, float By, float Bw, float Bh) {
            return Bx + Bw > Ax &&
                    By + Bh > Ay &&
                    Ax + Aw > Bx &&
                    Ay + Ah > By;
        }

        public static boolean pointRectsCollide(int pointX, int pointY, int Bx, int By, int Bw, int Bh) {
            return rectsCollide(pointX, pointY, 1, 1, Bx, By, Bw, Bh);
        }

        public static boolean pointRectsCollide(float pointX, float pointY, float Bx, float By, float Bw, float Bh) {
            return rectsCollide(pointX, pointY, 1, 1, Bx, By, Bw, Bh);
        }

        public static float toIsoX(float cart_X, float cart_Y) {
            return cart_X - cart_Y;
        }

        public static float toIsoY(float cart_X, float cart_Y) {
            return (cart_X + cart_Y) / 2;
        }

        public static int toIsoX(int cart_X, int cart_Y) {
            return cart_X - cart_Y;
        }

        public static int toIsoY(int cart_X, int cart_Y) {
            return (cart_X + cart_Y) / 2;
        }

        public static int toCartX(int iso_X, int iso_Y) {
            return (2 * iso_Y + iso_X) / 2;
        }

        public static float toCartX(float iso_X, float iso_Y) {
            return (2 * iso_Y + iso_X) / 2;
        }

        public static float toCartY(float iso_X, float iso_Y) {
            return (2 * iso_Y - iso_X) / 2;
        }

        public static int toCartY(int iso_X, int iso_Y) {
            return (2 * iso_Y - iso_X) / 2;
        }
    }

    public static class Reflection {
        /* Dont use these if you target HTML */
        public static boolean checkDataObjectGuidelines(Class checkClass) {
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
                        if (checkDataObjectGuidelines(fields[i].getType())) {
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
                CMedia cMedia = null;
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
                Object fieldObject = null;
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
