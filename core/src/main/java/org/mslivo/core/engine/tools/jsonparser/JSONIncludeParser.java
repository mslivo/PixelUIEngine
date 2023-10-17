package org.mslivo.core.engine.tools.jsonparser;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Objects;


public class JSONIncludeParser {

    private static final String INCLUDE = "//INCLUDE ";

    private static final String INCLUDE_TRIM = "//INCLUDE_TRIM ";

    static final class IncludeInfo {
        private final String includeFile;
        private final boolean trim;

        IncludeInfo(String includeFile, boolean trim) {
            this.includeFile = includeFile;
            this.trim = trim;
        }

        public String includeFile() {
            return includeFile;
        }

        public boolean trim() {
            return trim;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (IncludeInfo) obj;
            return Objects.equals(this.includeFile, that.includeFile) &&
                    this.trim == that.trim;
        }

        @Override
        public int hashCode() {
            return Objects.hash(includeFile, trim);
        }

        @Override
        public String toString() {
            return "IncludeInfo[" +
                    "includeFile=" + includeFile + ", " +
                    "trim=" + trim + ']';
        }


    }

    private static IncludeInfo findIncludeInfo(String line) {
        String prepare = line.trim();
        if (prepare.startsWith(INCLUDE)) {
            return new IncludeInfo(line.substring(INCLUDE.length()), false);
        } else if (prepare.startsWith(INCLUDE_TRIM)) {
            return new IncludeInfo(line.substring(INCLUDE_TRIM.length()), true);
        } else {
            return null;
        }
    }


    private static ArrayList<String> getFileContent(String basePath, String fileName, boolean include, boolean trimInclude, InputFileMode inputFileMode) throws IOException {
        ArrayList<String> lines = new ArrayList<>();
        String fileContent = "";
        if (inputFileMode == InputFileMode.CLASSPATH) {
            FileHandle fileHandle = Gdx.files.internal(basePath + fileName);
            fileContent = fileHandle.readString();
        } else if (inputFileMode == InputFileMode.EXTERNAL) {
            fileContent = Files.readString(Path.of(basePath + fileName));
        }
        String[] fileContentSplit = fileContent.split(System.lineSeparator());
        for (int i = 0; i < fileContentSplit.length; i++) {
            String line = fileContentSplit[i].trim();
            if (!line.isEmpty()) lines.add(line);
        }

        if (include) {
            // Remove braces
            if (trimInclude) {
                lines.remove(0);
                lines.remove(lines.size() - 1);
            }
            // update of sub includes path
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                IncludeInfo includeInfo = findIncludeInfo(line);
                if (includeInfo != null) {
                    String incFile = includeInfo.includeFile;
                    String currentdir = new File(fileName).getParent();
                    String incSyntax = includeInfo.trim ? INCLUDE_TRIM : INCLUDE;
                    lines.set(i, incSyntax + currentdir + "\\" + incFile);
                }

            }

        }
        return lines;
    }


    public static String parseJSON(String jsonPath, InputFileMode inputFileMode) throws IOException {
        StringBuilder result = new StringBuilder();

        // Read File
        String basePath = "", fileName;
        File jsonFile = new File(jsonPath);
        if (jsonFile.getParent() != null) {
            basePath = jsonFile.getParent() + "\\";
        }
        fileName = jsonFile.getName();

        // Read Base File
        ArrayList<String> lines = getFileContent(basePath, fileName, false, false, inputFileMode);


        // Parse includes
        ArrayDeque<Integer> removeIndexes = new ArrayDeque<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            IncludeInfo includeInfo = findIncludeInfo(line);
            if (includeInfo != null) {
                lines.set(i, "");
                removeIndexes.add(i);
                ArrayList<String> includeLines = getFileContent(basePath, includeInfo.includeFile, true, includeInfo.trim, inputFileMode);
                if (includeLines.size() > 0) {
                    for (int i2 = includeLines.size() - 1; i2 >= 0; i2--) {
                        String incLine = includeLines.get(i2);
                        lines.add(i + 1, incLine);
                    }
                }
            }

        }

        // remove empty
        Integer index;
        while ((index = removeIndexes.pollLast()) != null) {
            lines.remove((int) index);
        }

        for (int i = 0; i < lines.size(); i++) result.append(lines.get(i)).append(System.lineSeparator());
        return result.toString();
    }


}
