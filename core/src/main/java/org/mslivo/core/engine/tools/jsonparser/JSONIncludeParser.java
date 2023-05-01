package org.mslivo.core.engine.tools.jsonparser;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;


public class JSONIncludeParser {

    private static final String INCLUDE = "//INCLUDE ";

    private static final String INCLUDE_TRIM = "//INCLUDE_TRIM ";

    record IncludeInfo(String includeFile, boolean trim) {

    }

    private static IncludeInfo findIncludeInfo(String line){
        String prepare = line.trim();
        if(prepare.startsWith(INCLUDE)){
            return new IncludeInfo(line.substring(INCLUDE.length()), false);
        }else if(prepare.startsWith(INCLUDE_TRIM)){
            return new IncludeInfo(line.substring(INCLUDE_TRIM.length()),  true);
        }else{
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
            fileContent =  Files.readString(Path.of(basePath + fileName));
        }
        String[] fileContentSplit = fileContent.split(System.lineSeparator());
        for(String line : fileContentSplit){
            line = line.trim();
            if(!line.isEmpty()){
                lines.add(line);
            }
        }

        if(include) {
            // Remove braces
            if(trimInclude) {
                lines.remove(0);
                lines.remove(lines.size() - 1);
            }
            // update of sub includes path
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                IncludeInfo includeInfo = findIncludeInfo(line);
                if(includeInfo != null){
                    String incFile = includeInfo.includeFile;
                    String currentdir = new File(fileName).getParent();
                    String incSyntax = includeInfo.trim ? INCLUDE_TRIM : INCLUDE;
                    lines.set(i,incSyntax+currentdir+"\\"+incFile);
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
        ArrayList<String> lines = getFileContent(basePath, fileName, false,false, inputFileMode);


        // Parse includes
        ArrayDeque<Integer> removeIndexes = new ArrayDeque<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            IncludeInfo includeInfo = findIncludeInfo(line);
            if(includeInfo != null){
                lines.set(i,"");
                removeIndexes.add(i);
                ArrayList<String> includeLines = getFileContent(basePath,includeInfo.includeFile, true, includeInfo.trim, inputFileMode);
                if(includeLines.size() > 0) {
                    for (int i2 = includeLines.size() - 1; i2 >= 0; i2--) {
                        String incLine = includeLines.get(i2);
                        lines.add(i + 1, incLine);
                    }
                }
            }

        }

        // remove empty
        Integer index;
        while ((index = removeIndexes.pollLast()) != null){
            lines.remove((int)index);
        }

        for (String line : lines) {
            result.append(line).append(System.lineSeparator());
        }

        return result.toString();
    }



}
