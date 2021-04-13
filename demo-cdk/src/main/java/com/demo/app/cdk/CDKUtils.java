package com.demo.app.cdk;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.demo.app.annotation.LambdaFunctionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CDKUtils {

    private static Logger LOG = LoggerFactory.getLogger(CDKUtils.class);

    private static String apiPath = "demo-api/src/main/java/com/demo/app";

    private static final List<Class> classes;

    static {
        classes = findClasses(new File(apiPath), "com.demo.app");
    }

    public static List<LambdaFunctionConfig> getFunctions() {
        return  classes.stream()
                .filter(aClass -> aClass.isAnnotationPresent(LambdaFunctionConfig.class))
                .map(aClass ->  ((LambdaFunctionConfig)aClass.getAnnotation(LambdaFunctionConfig.class)))
                .collect(Collectors.toList());
    }

    public static List<String> getTableNames() {
        return  classes.stream()
                .filter(aClass -> aClass.isAnnotationPresent(DynamoDBTable.class))
                .map(aClass -> ((DynamoDBTable)aClass.getAnnotation(DynamoDBTable.class)).tableName())
                .collect(Collectors.toList());
    }



    /**
     * Recursive method used to find all classes in a given directory and
     * subdirs.
     *
     * @param directory
     *            The base directory
     * @param packageName
     *            The package name for classes found inside the base directory
     * @return The classes
     * @throws ClassNotFoundException
     */
    private static List<Class> findClasses(File directory, String packageName)
    {
        List<Class> classes = new ArrayList<Class>();
        try {
            if (!directory.exists()) {
                return classes;
            }
            File[] files = directory.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    classes.addAll(findClasses(file, packageName + "." + file.getName()));
                } else if (file.getName().endsWith(".java")) {
                    classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 5)));
                }
            }
        } catch (ClassNotFoundException ex) {
            LOG.error("Class not found ", ex.getMessage());
        }
        return classes;
    }
}
