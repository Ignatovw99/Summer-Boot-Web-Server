package summer.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PathFormatter {

    private static final String PATH_PARAMETER_PATTERN = "\\{([a-zA-Z]+)\\}";

    private static final String PATH_PARSED_PARAMETER_PATTERN = "(?<group-name>[a-zA-Z0-9_-]+)";

    public static String formatPath(String path) {
        Pattern pathParameterPattern = Pattern.compile(PATH_PARAMETER_PATTERN);
        Matcher pathParameterMatcher = pathParameterPattern.matcher(path);

        String formattedPath = path;

        while (pathParameterMatcher.find()) {
            System.out.println("Vlixa v path formatter");
            String parameterName = pathParameterMatcher.group(1);

            System.out.println("add: " + parameterName);

            String formattedParameterPattern = PATH_PARSED_PARAMETER_PATTERN.replace("group-name", parameterName);

            System.out.println(formattedPath);
            formattedPath = formattedPath.replaceFirst(PATH_PARAMETER_PATTERN, formattedParameterPattern);
            System.out.println(formattedPath);
        }

        return formattedPath;
    }
}
