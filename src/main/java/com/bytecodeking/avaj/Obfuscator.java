package com.bytecodeking.avaj;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.bytecodeking.avaj.transformers.ClassTransformer;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.printer.DefaultPrettyPrinter;
import com.github.javaparser.printer.Printer;
import com.github.javaparser.printer.configuration.DefaultConfigurationOption;
import com.github.javaparser.printer.configuration.DefaultPrinterConfiguration;
import org.tinylog.Logger;

public class Obfuscator {
    private Path                   inputFile;
    private List<ClassTransformer> transformers;

    public Obfuscator(Path inputFile) {
        this(inputFile, new ArrayList<>());
    }

    public Obfuscator(Path inputFile, List<ClassTransformer> transformers) {
        this.inputFile = inputFile;
        this.transformers = transformers;
    }

    public String run() throws ObfuscateException {
        try {
            StaticJavaParser.getConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_16);
            CompilationUnit                   root    = StaticJavaParser.parse(inputFile);
            List<ClassOrInterfaceDeclaration> classes = root.findAll(ClassOrInterfaceDeclaration.class);
            Logger.debug("Found {} classes", classes.size());
            for (ClassOrInterfaceDeclaration clazz : classes) {
                Logger.info("Transforming class: {}", clazz.getName());
                transform(clazz);
            }
            Printer printer = new DefaultPrettyPrinter();
            // Remove orphan comment
            printer.getConfiguration().removeOption(new DefaultConfigurationOption(DefaultPrinterConfiguration.ConfigOption.PRINT_COMMENTS));

            return printer.print(root);
        } catch (ParseProblemException parseEx) {
            Optional<TokenRange> location = parseEx.getProblems().get(0).getLocation();
            String position = location.map((l) -> l.getBegin().getRange().map(
                                                   (r) -> r.begin.toString()).orElse("unknown line")
                                          ).orElse("unknown line");
            Logger.error(parseEx);
            throw new ObfuscateException("[Syntax error] Invalid syntax at " + position);
        } catch (Throwable t) {
            Logger.error(t);
            throw new ObfuscateException("[Internal error] Unknown error");
        }
    }

    public void transform(ClassOrInterfaceDeclaration clazz) {
        for (ClassTransformer transformer : transformers) {
            Logger.info("Running {}", transformer.getName());
            transformer.transform(clazz);
        }
    }

    public void registerTransformer(ClassTransformer... transformer) {
        transformers.addAll(Arrays.asList(transformer));
    }

    public void unRegisterTransformer(Class<? extends ClassTransformer> transformerType) {
        transformers.removeIf(o -> o.getClass().isAssignableFrom(transformerType));
    }
}
