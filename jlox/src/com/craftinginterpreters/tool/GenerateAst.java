package com.craftinginterpreters.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

/**
 * Auto-generate AST classes
 */
public class GenerateAst {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);
        }

        String outputDir = args[0];

        defineAst(outputDir, "Expression",
                Arrays.asList("Binary   : Expression left, Token operator, Expression right",
                        "Grouping : Expression expression", "Literal  : Object value",
                        "Unary    : Token operator, Expression right"));
    }

    private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException {
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, "UTF-8");

        writer.println("package com.craftinginterpreters.lox;");
        writer.println();
        writer.println("// import java.util.List;"); // FIXME used later
        writer.println();
        writer.println("abstract class " + baseName + " {");

        int typesLength = types.size();

        for (int i = 0; i < typesLength; i++) {
            String type = types.get(i);
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();

            defineType(writer, baseName, className, fields);

            if (i < typesLength - 1) {
                writer.println();
            }
        }

        writer.println("}");
        writer.close();
    }

    private static void defineType(PrintWriter writer, String baseName, String className, String fieldList) {
        writer.println("    static class " + className + " extends " + baseName + " {");

        // Constructor
        writer.println("        " + className + "(" + fieldList + ") {");

        // Store parameters in fields
        String[] fields = fieldList.split(", ");

        for (String field : fields) {
            String name = field.split(" ")[1];
            writer.println("            this." + name + " = " + name + ";");
        }

        writer.println("        }");

        // Fields
        writer.println();

        for (String field : fields) {
            writer.println("        final " + field + ";");
        }

        writer.println("    }");
    }
}
