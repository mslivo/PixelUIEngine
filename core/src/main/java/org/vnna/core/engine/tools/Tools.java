package org.vnna.core.engine.tools;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ObjectMap;
import org.vnna.core.engine.media_manager.color.CColor;
import org.vnna.core.engine.media_manager.color.FColor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.function.BiFunction;

public class Tools {

    private static final DecimalFormat decimalFormat_2decimal = new DecimalFormat("#.##");

    private static final DecimalFormat decimalFormat_3decimal = new DecimalFormat("#.###");

    private static final DecimalFormat decimalFormat_4decimal = new DecimalFormat("#.####");

    private static final DecimalFormat decimalFormat_5decimal = new DecimalFormat("#.#####");

    private static final DecimalFormat decimalFormat_6decimal = new DecimalFormat("#.######");

    private static final SimpleDateFormat sdf = new SimpleDateFormat("[dd.MM.yy][HH:mm:ss] ");

    private static boolean debugEnabled = false;

    private static String timestamp() {
        return sdf.format(new Timestamp(new Date().getTime()));
    }

    public static void log(String msg) {
        System.out.println(Text.ANSI_BLUE + timestamp() + Text.ANSI_RESET + msg);
    }

    public static void log(String msg, Object values) {
        System.out.println(Text.ANSI_BLUE + timestamp() + Text.ANSI_RESET + String.format(msg, values));
    }

    public static void logError(String msg) {
        System.err.println(Text.ANSI_RED + timestamp() + Text.ANSI_RESET + msg);
    }

    public static void logError(Exception e) {
        System.err.println(Text.ANSI_RED + timestamp() + Text.ANSI_RESET + e.getLocalizedMessage());
    }

    public static void enableDebugLog(boolean enabled) {
        debugEnabled = enabled;
    }

    public static void logDebug(String msg) {
        if (!debugEnabled) return;
        System.out.println(Text.ANSI_PURPLE + timestamp() + Text.ANSI_RESET + msg);
    }

    public static void logInProgress(String what) {
        System.out.println(Text.ANSI_BLUE + timestamp() + Text.ANSI_RESET + what);
    }

    public static void logDone() {
        System.out.println(Text.ANSI_BLUE + timestamp() + Text.ANSI_RESET + "Done.");
    }

    public static void logBenchmark(String... customValues) {
        String custom = "";
        if (customValues.length > 0) {
            for (String customValue : customValues) {
                custom = custom + " | " + String.format("%1$10s", customValue);
            }
        }

        Tools.log(String.format("%1$3s", Gdx.graphics.getFramesPerSecond()) + " FPS | " +
                String.format("%1$6s", (Runtime.getRuntime().totalMemory() / (1024 * 1024))) + "MB RAM" +
                " | " +
                String.format("%1$4s", Thread.getAllStackTraces().keySet().size()) + " Threads" + custom);
    }

    public static class Colors {
        public static final FColor WHITE = new FColor(1, 1, 1, 1);
        public static final FColor BLACK = new FColor(0, 0, 0, 1);
        public static final FColor TRANSPARENT = new FColor(0, 0, 0, 0);
        public static final FColor GREEN_BRIGHT = new FColor(0.18039216f, 0.8f, 0.44313726f, 1f);
        public static final FColor GREEN_DARK = new FColor(0.15294118f, 0.68235296f, 0.3764706f, 1f);
        public static final FColor BLUE_BRIGHT = new FColor(0.20392157f, 0.59607846f, 0.85882354f, 1f);
        public static final FColor BLUE_DARK = new FColor(0.16078432f, 0.5019608f, 0.7254902f, 1f);
        public static final FColor ORANGE_BRIGHT = new FColor(0.9019608f, 0.49411765f, 0.13333334f, 1f);
        public static final FColor ORANGE_DARK = new FColor(0.827451f, 0.32941177f, 0.0f, 1f);
        public static final FColor RED_BRIGHT = new FColor(0.90588236f, 0.29803923f, 0.23529412f, 1f);
        public static final FColor RED_DARK = new FColor(0.7529412f, 0.22352941f, 0.16862746f, 1f);
        public static final FColor GRAY_BRIGHT = new FColor(0.58431375f, 0.64705884f, 0.6509804f, 1f);
        public static final FColor GRAY_DARK = new FColor(0.49803922f, 0.54901963f, 0.5529412f, 1f);
        public static final FColor SILVER_BRIGHT = new FColor(0.9254902f, 0.9411765f, 0.94509804f, 1f);
        public static final FColor SILVER_DARK = new FColor(0.7411765f, 0.7647059f, 0.78039217f, 1f);
        public static final FColor YELLOW_BRIGHT = new FColor(0.94509804f, 0.76862746f, 0.05882353f, 1f);
        public static final FColor YELLOW_DARK = new FColor(0.9529412f, 0.6117647f, 0.07058824f, 1f);
        public static final FColor TURQUOISE_BRIGHT = new FColor(0.101960786f, 0.7372549f, 0.6117647f, 1f);
        public static final FColor TURQUOISE_DARK = new FColor(0.08627451f, 0.627451f, 0.52156866f, 1f);
        public static final FColor PURPLE_BRIGHT = new FColor(0.60784316f, 0.34901962f, 0.7137255f, 1f);
        public static final FColor PURPLE_DARK = new FColor(0.5568628f, 0.26666668f, 0.6784314f, 1f);
        public static final FColor NAVY_BLUE_BRIGHT = new FColor(0.20392157f, 0.28627452f, 0.36862746f, 1);
        public static final FColor NAVY_BLUE_DARK = new FColor(0.17254902f, 0.24313726f, 0.3137255f, 1f);
        public static final FColor BROWN_BRIGHT = new FColor(0.6509804f, 0.48235294f, 0.31764707f, 1f);
        public static final FColor BROWN_DARK = new FColor(0.38039216f, 0.23137255f, 0.08627451f, 1f);

        public static final FColor[] ALL_COLORS = new FColor[]{
                WHITE, BLACK, GREEN_BRIGHT,
                GREEN_DARK, BLUE_BRIGHT, BLUE_DARK, ORANGE_BRIGHT,
                ORANGE_DARK, RED_BRIGHT, RED_DARK, GRAY_BRIGHT,
                GRAY_DARK, SILVER_BRIGHT, SILVER_DARK, YELLOW_BRIGHT,
                YELLOW_DARK, TURQUOISE_BRIGHT, TURQUOISE_DARK, PURPLE_BRIGHT,
                PURPLE_DARK, NAVY_BLUE_BRIGHT, NAVY_BLUE_DARK, BROWN_BRIGHT, BROWN_DARK
        };

        public static CColor create(CColor color) {
            return Colors.create(color.r, color.g, color.b, color.a);
        }

        public static CColor create(FColor color) {
            return Colors.create(color.r, color.g, color.b, color.a);
        }

        public static CColor create(float r, float g, float b) {
            return Colors.create(r, g, b, 1);
        }

        public static CColor create(float r, float g, float b, float a) {
            CColor color = new CColor();
            setRGBA(color, r, g, b, a);
            return color;
        }

        public static CColor createDarker(CColor color, float amount) {
            CColor darker = create(color);
            amount = 1f - Tools.Calc.inBounds(amount, 0f, 1f);
            darker.r = Tools.Calc.inBounds(darker.r*amount,0f,1f);
            darker.g = Tools.Calc.inBounds(darker.g*amount,0f,1f);
            darker.b = Tools.Calc.inBounds(darker.b*amount,0f,1f);
            return darker;
        }

        public static CColor createBrighter(CColor color, float amount) {
            CColor brighter = create(color);
            amount = 1f + Tools.Calc.lowerBounds(amount, 1f);
            brighter.r = Tools.Calc.inBounds(brighter.r*amount,0f,1f);
            brighter.g = Tools.Calc.inBounds(brighter.g*amount,0f,1f);
            brighter.b = Tools.Calc.inBounds(brighter.b*amount,0f,1f);
            return brighter;
        }

        public static CColor createFromString(String colorString) {
            String[] colors = colorString.split(",");
            if (colors.length != 4) return Colors.create(Colors.WHITE);
            try {
                return create(
                        Float.parseFloat(colors[0]),
                        Float.parseFloat(colors[1]),
                        Float.parseFloat(colors[2]),
                        Float.parseFloat(colors[3]));
            } catch (Exception e) {
                return Colors.create(Colors.WHITE);
            }
        }

        public static CColor createFromInt(int rgb8888) {
            return create((float) ((rgb8888 & -16777216) >>> 24) / 255.0F,
                    (float) ((rgb8888 & 16711680) >>> 16) / 255.0F,
                    (float) ((rgb8888 & '\uff00') >>> 8) / 255.0F,
                    (float) (rgb8888 & 255) / 255.0F
            );
        }

        public static String getAsString(CColor cColor) {
            return cColor.r + "," + cColor.g + "," + cColor.b + "," + cColor.a;
        }

        public static float getBrightness(CColor cColor) {
            return (0.299f * cColor.r) + (0.587f * cColor.g) + (0.114f * cColor.b);
        }

        public static float getBrightness(FColor fColor) {
            return (0.299f * fColor.r) + (0.587f * fColor.g) + (0.114f * fColor.b);
        }

        public static FColor createFixed(float r, float g, float b, float a) {
            return new FColor(
                    Calc.inBounds(r, 0f, 1f),
                    Calc.inBounds(g, 0f, 1f),
                    Calc.inBounds(b, 0f, 1f),
                    Calc.inBounds(a, 0f, 1f));
        }

        public static FColor createFixed(CColor color) {
            return Colors.createFixed(color.r, color.g, color.b, color.a);
        }

        public static FColor createFixed(FColor fColor) {
            return Colors.createFixed(fColor.r, fColor.g, fColor.b, fColor.a);
        }

        public static FColor createFixed(float r, float g, float b) {
            return Colors.createFixed(r, g, b, 1f);
        }

        public static FColor createFixedFromInt(int rgb8888) {
            return createFixed((float) ((rgb8888 & -16777216) >>> 24) / 255.0F,
                    (float) ((rgb8888 & 16711680) >>> 16) / 255.0F,
                    (float) ((rgb8888 & '\uff00') >>> 8) / 255.0F,
                    (float) (rgb8888 & 255) / 255.0F
            );
        }


        public static FColor createFixedFromString(String colorString) {
            String[] colors = colorString.split(",");
            if (colors.length != 4) return Colors.createFixed(Colors.WHITE);
            try {
                return createFixed(
                        Float.parseFloat(colors[0]),
                        Float.parseFloat(colors[1]),
                        Float.parseFloat(colors[2]),
                        Float.parseFloat(colors[3]));
            } catch (Exception e) {
                return Colors.createFixed(Colors.WHITE);
            }
        }

        public static String getAsString(FColor fColor) {
            return fColor.r + "," + fColor.g + "," + fColor.b + "," + fColor.a;
        }

        public static String getAsHex(FColor fColor) {
            return getAsHex(fColor.r, fColor.g, fColor.b);
        }

        public static String getAsHex(CColor color) {
            return getAsHex(color.r, color.g, color.b);
        }

        private static String getAsHex(float r, float g, float b) {
            return String.format("#%02x%02x%02x", MathUtils.round(r * 255), MathUtils.round(g * 255), MathUtils.round(b * 255));
        }

        public static void setAlpha(CColor color, float a) {
            color.a = Calc.inBounds(a, 0f, 1f);
        }

        public static void setRed(CColor color, float r) {
            color.r = Calc.inBounds(r, 0f, 1f);
        }

        public static void setGreen(CColor color, float g) {
            color.g = Calc.inBounds(g, 0f, 1f);
        }

        public static void setBlue(CColor color, float b) {
            color.b = Calc.inBounds(b, 0f, 1f);
        }

        public static void setRGBAFrom(CColor color, CColor colorFrom) {
            setRGBA(color, colorFrom.r, colorFrom.g, colorFrom.b, colorFrom.a);
        }

        public static void setRGB(CColor color, float r, float g, float b) {
            setRed(color, r);
            setGreen(color, g);
            setBlue(color, b);
        }

        public static void setRGBA(CColor color, float r, float g, float b, float a) {
            setRed(color, r);
            setGreen(color, g);
            setBlue(color, b);
            setAlpha(color, a);
        }

        public static void setRGBAFrom(CColor color, FColor fColor) {
            setRGBA(color, fColor.r, fColor.g, fColor.b, fColor.a);
        }

        public static void setRGBFrom(CColor color, FColor fColor) {
            setRGB(color, fColor.r, fColor.g, fColor.b);
        }

        public static void setRGBFrom(CColor color, CColor cColor) {
            setRGB(color, cColor.r, cColor.g, cColor.b);
        }

        public static boolean matches(CColor color1, FColor color2) {
            return (color1.r == color2.r &&
                    color1.g == color2.g &&
                    color1.b == color2.b &&
                    color1.a == color2.a);
        }

        public static boolean matches(FColor color1, CColor color2) {
            return (color1.r == color2.r &&
                    color1.g == color2.g &&
                    color1.b == color2.b &&
                    color1.a == color2.a);
        }

        public static boolean matches(CColor color1, CColor color2) {
            return (color1.r == color2.r &&
                    color1.g == color2.g &&
                    color1.b == color2.b &&
                    color1.a == color2.a);
        }

        public static boolean matches(FColor color1, FColor color2) {
            return (color1.r == color2.r &&
                    color1.g == color2.g &&
                    color1.b == color2.b &&
                    color1.a == color2.a);
        }


        public static CColor createRandom() {
            return Colors.create(MathUtils.random(0f, 1f), MathUtils.random(0f, 1f), MathUtils.random(0f, 1f));
        }

        public static FColor createRandomFixed() {
            return Colors.createFixed(MathUtils.random(0f, 1f), MathUtils.random(0f, 1f), MathUtils.random(0f, 1f));
        }

        public static FColor createFixedFromHex(String hex) {
            if (hex.startsWith("#")) hex = hex.substring(1);
            return Colors.createFixed(
                    Integer.parseInt(hex.substring(0, 2), 16) / 255f,
                    Integer.parseInt(hex.substring(2, 4), 16) / 255f,
                    Integer.parseInt(hex.substring(4, 6), 16) / 255f);
        }

        public static CColor createFromHex(String hex) {
            if (hex.startsWith("#")) hex = hex.substring(1);
            return Tools.Colors.create(
                    Integer.parseInt(hex.substring(0, 2), 16) / 255f,
                    Integer.parseInt(hex.substring(2, 4), 16) / 255f,
                    Integer.parseInt(hex.substring(4, 6), 16) / 255f);
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
            return text.split("\n");
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
            return (Character.toString((char) (127 + number)));
        }

        public static String validString(String string) {
            return string == null ? "" : string;
        }

        public static String[] validString(String[] string) {
            return string == null ? new String[]{} : string;
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

        public static Path findNextValidFile(String folder, String filename, String extension) {
            Path folderF = Path.of(folder);
            makeSureDirectoryExists(folderF);
            filename = makeFileNameValid(filename);
            extension = extension == null || extension.length() == 0 ? "" : "." + extension;
            Path file;
            int count = 1;
            do {
                String countExt = count == 1 ? "" : "_" + count;
                file = Path.of(folder, filename
                        + countExt
                        + extension);
                count++;
            } while (Files.exists(file));
            return file;
        }


        public static boolean makeSureDirectoryExists(Path file) {
            try {
                if (Files.isRegularFile(file)) {
                    Files.delete(file);
                }
                Files.createDirectories(file);
                return true;
            } catch (IOException e) {
                Tools.logError(e);
                return false;
            }
        }

        public static String findAppDataDirectory() {
            String os = System.getProperty("os.name", "generic").toLowerCase();
            if (os.indexOf("win") >= 0) {
                return System.getenv("APPDATA").replace("\\", "/");
            } else if ((os.indexOf("mac") >= 0) || (os.indexOf("darwin") >= 0)) {
                return "~/Library/Preferences/";
            } else if (os.indexOf("nux") >= 0) {
                if (System.getenv("XDG_CONFIG_HOME") != null) {
                    return System.getenv("XDG_CONFIG_HOME") + "/";
                } else {
                    return "~/.config/";
                }
            } else {
                return System.getProperty("user.home") + "/";
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
            ObjectInputStream objectInputStream = new ObjectInputStream(Files.newInputStream(file));
            try {
                Object ret = objectInputStream.readObject();
                objectInputStream.close();
                return ret;
            } catch (Exception e) {
                throw e;
            } finally {
                objectInputStream.close();
            }
        }

        public static FileHandle findResource(String path) {
            return Gdx.files.internal(path);
        }


    }

    public static class Calc {

        public static Object selectRandom(ArrayList arrayList) {
            if (arrayList.size() > 0) {
                return arrayList.get(MathUtils.random(0, arrayList.size() - 1));
            } else {
                return null;
            }
        }

        public static int selectRandomProbabilities(int... values) {
            int sum = 0;
            for (int i = 0; i < values.length; i++) {
                sum += values[i];
            }
            float[] probabilities = new float[values.length];

            for (int i = 0; i < values.length; i++) {
                if (sum == 0) {
                    probabilities[i] = 1f / (float) values.length;
                } else {
                    probabilities[i] = values[i] / (float) sum;
                }
            }

            return selectRandomProbabilitiesPercent(probabilities);
        }

        public static int selectRandomProbabilitiesPercent(float... probabilities) {
            if (probabilities.length == 0) return -1;
            float random = MathUtils.random();
            float cumulativeProbability = 0f;
            for (int i = 0; i < probabilities.length; i++) {
                cumulativeProbability += probabilities[i];
                if (random <= cumulativeProbability) {
                    return i;
                }
            }
            return -1; // probabilities must add up to 1f!
        }


        public static boolean chance(float probability) {
            return MathUtils.random(0f, 1f) < probability;
        }

        public static boolean chance(double probability) {
            return MathUtils.random(0f, 1f) < probability;
        }


        public static boolean shouldRun(int ticksPerSecond, long lastRun) {
            if ((System.currentTimeMillis() - lastRun) > (1000 / ticksPerSecond)) {
                return true;
            } else {
                return false;
            }
        }

        public static float min(float... values) {
            float sum = Float.MAX_VALUE;
            for (float f : values) {
                if (f < sum) sum = f;
            }
            return sum;
        }

        public static float sinPos(float radians) {
            return MathUtils.sin(radians) * 0.5f + 0.5f;
        }

        public static float cosPos(float radians) {
            return MathUtils.cos(radians) * 0.5f + 0.5f;
        }


        public static float max(float... values) {
            float sum = 0;
            for (float f : values) {
                if (f > sum) sum = f;
            }
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

        public static boolean inBoundsX(int x, int map_width) {
            if (x < 0) return false;
            if (x > (map_width - 1)) return false;
            return true;
        }

        public static boolean inBoundsY(int y, int map_height) {
            if (y < 0) return false;
            if (y > (map_height - 1)) return false;
            return true;
        }

        public static boolean inBoundsXY(int x, int y, int map_width, int map_height) {
            return inBoundsX(x, map_width) && inBoundsY(y, map_height);
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

        private static ObjectMap<Integer, ArrayList<Long>> doInRadiusCache = new ObjectMap<>();

        public static void doInRadiusCached(int x, int y, int radius, BiFunction<Integer, Integer, Boolean> tileFunction) {
            ArrayList<Long> cached = doInRadiusCache.get(radius);
            if (cached == null) {
                cached = new ArrayList<>();
                ArrayList<Long> finalCached = cached;
                doInRadius(0, 0, radius, new BiFunction<Integer, Integer, Boolean>() {
                    @Override
                    public Boolean apply(Integer x, Integer y) {
                        finalCached.add(
                                (((long) x) << 32) | (y & 0xffffffffL));
                        return true;
                    }
                });
                doInRadiusCache.put(radius, cached);
            }

            for (Long positions : cached) {
                if (!tileFunction.apply(
                        x + ((int) (positions >> 32)),
                        y + positions.intValue())
                ) {
                    return;
                }
            }

        }

        public static void doInRadius(int x, int y, int radius, BiFunction<Integer, Integer, Boolean> tileFunction) {
            for (int iy = -radius; iy <= radius; iy++) {
                for (int ix = -radius; ix <= radius; ix++) {
                    if ((ix * ix) + (iy * iy) <= (radius * radius)) {
                        if (!tileFunction.apply(x + ix, y + iy)) {
                            return;
                        }
                    }
                }
            }
        }

        public static long applyRandomness(long value, float randomness) {
            if (randomness == 0) return value;
            randomness = Tools.Calc.inBounds(randomness, 0f, 1f);
            return MathUtils.round(value * MathUtils.random((1 - randomness), (1 + randomness)));
        }

        public static int applyRandomness(int value, float randomness) {
            return (int) applyRandomness((long) value, randomness);
        }

        public static double applyRandomness(double value, float randomness) {
            if (randomness == 0) return value;
            randomness = Tools.Calc.inBounds(randomness, 0f, 1f);
            return value * MathUtils.random((1 - randomness), (1 + randomness));
        }

        public static float applyRandomness(float value, float randomness) {
            return (float) applyRandomness((double) value, randomness);
        }

        public static long applyPercent(long value, float percent) {
            if (percent == 0) return value;
            value += MathUtils.round(value * percent);
            return value;
        }

        public static int applyPercent(int value, float percent) {
            return (int) applyPercent((long) value, percent);
        }


        public static double applyPercent(double value, float percent) {
            if (percent == 0) return value;
            value += (value * percent);
            return value;
        }

        public static float applyPercent(float value, float percent) {
            return (float) applyPercent((double) value, percent);
        }

        public static boolean pointRectsCollide(int pointX, int pointY, int Bx, int By, int Bw, int Bh) {
            return rectsCollide(pointX, pointY, 1, 1, Bx, By, Bw, Bh);
        }

        public static int distance(int x1, int y1, int x2, int y2) {
            return MathUtils.floor((float) (Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2))));
        }

        public static float distancef(float x1, float y1, float x2, float y2) {
            return (float) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
        }

        public static boolean isInRadius(int x1, int y1, int x2, int y2, int radius) {
            return distance(x1, y1, x2, y2) <= radius;
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

        public static float inBounds(float value, float lower, float upper) {
            if (value < lower) value = lower;
            if (value > upper) value = upper;
            return value;
        }

        public static double inBounds(double value, double lower, double upper) {
            if (value < lower) value = lower;
            if (value > upper) value = upper;
            return value;
        }


        public static double upperBounds(double value, double upper) {
            if (value > upper) value = upper;
            return value;
        }


        public static double lowerBounds(double value, double lower) {
            if (value < lower) value = lower;
            return value;
        }

        public static int lowerBounds(int value, int lower) {
            if (value < lower) value = lower;
            return value;
        }

        public static float lowerBounds(float value, float lower) {
            if (value < lower) value = lower;
            return value;
        }

        public static long lowerBounds(long value, long lower) {
            if (value < lower) value = lower;
            return value;
        }

        public static long upperBounds(long value, long upper) {
            if (value > upper) value = upper;
            return value;
        }

        public static float upperBounds(float value, float upper) {
            if (value > upper) value = upper;
            return value;
        }

        public static int upperBounds(int value, int upper) {
            if (value > upper) value = upper;
            return value;
        }

        public static long inBounds(long value, long lower, long upper) {
            if (value < lower) value = lower;
            if (value > upper) value = upper;
            return value;
        }

        public static int inBounds(int value, int lower, int upper) {
            if (value < lower) value = lower;
            if (value > upper) value = upper;
            return value;
        }
    }

}
