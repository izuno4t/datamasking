package com.example;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.lang.UProperty;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

@Component
public class DatamaskingCommandLineRunner implements CommandLineRunner {

    private final static Logger logger = LoggerFactory.getLogger(DatamaskingCommandLineRunner.class);

    @Option(name = "--spring.output.ansi.enabled", hidden = true, usage = "SpringBoot argument")
    private final String springOutputAnsi = "";

    @Option(name = "-h", aliases = "--help", usage = "print usage message and exit")
    private boolean usageFlag;

    @Option(name = "-i", metaVar = "input file name", aliases = "--input", required = true, usage = "入力ファイル")
    private final String input = "";

    @Option(name = "-ie", metaVar = "input file encode", aliases = "--input-encode", usage = "入力ファイルの文字コード（未指定の場合はUTF-8）")
    private final String inputEncode = "";

    @Option(name = "-o", metaVar = "output file name", aliases = "--output", required = true, usage = "出力ファイル")
    private final String output = "";

    @Option(name = "-oe", metaVar = "output file encode", aliases = "--output-encode", usage = "入力ファイルの文字コード（未指定の場合はUTF-8）")
    private final String outputEncode = "";

    @Option(name = "-f", metaVar = "fields", aliases = "--fields", usage = "マスク対象列番号")
    private final String fields = "";

    @Override
    public void run(String... args) throws Exception {
        logger.info(ToStringBuilder.reflectionToString(args));
        logger.info(
                Arrays.stream(args).collect(Collectors.joining(",", getClass().getSimpleName() + "[", "]")));

        CmdLineParser parser = new CmdLineParser(this);
        try {
            parser.parseArgument(args);
            if (usageFlag) {
                System.out.println("Usage:");
                parser.printUsage(System.out);
                return;
            }
        } catch (CmdLineException e) {
            parser.printUsage(System.err);
            throw new IllegalArgumentException(e);
        }

        System.out.println("データのマスク化を開始します。");

        Path inputPath = Paths.get("", input);
        Charset inputCharset = Charset.forName(StringUtils.defaultIfBlank(inputEncode, "UTF-8"));
        Path outputPath = Paths.get("", output);
        Charset outputCharset = Charset.forName(StringUtils.defaultIfBlank(outputEncode, "UTF-8"));
        List<Integer> maskFields = Arrays.stream(StringUtils.split(fields, ",")).map(Integer::valueOf)
                .collect(Collectors.toList());

        logger.info("input file={}", inputPath.toAbsolutePath());
        logger.info("output file={}", outputPath.toAbsolutePath());

        // 読込ファイルはUTF-8に変換しておくこと。
        try (CSVReader reader = new CSVReader(new BufferedReader(
                new InputStreamReader(new BufferedInputStream(new FileInputStream(inputPath.toFile())), inputCharset)));
             CSVWriter writer = new CSVWriter(
                     new BufferedWriter(new OutputStreamWriter(
                             new BufferedOutputStream(new FileOutputStream(outputPath.toFile())), outputCharset)),
                     CSVWriter.DEFAULT_SEPARATOR, CSVWriter.DEFAULT_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.RFC4180_LINE_END)) {

            String[] columns;
            while ((columns = reader.readNext()) != null) {
                mask(columns, maskFields);
                writer.writeNext(columns, false);
            }
            writer.flushQuietly();
        }
        System.out.println("データのマスク化が完了しました。");
    }

    private void mask(String[] columns, List<Integer> maskFields) {
        for (Integer field : maskFields) {
            int idx = field - 1;
            columns[idx] = columns[idx].chars().mapToObj(c -> {
                if (StringUtils.isBlank(String.valueOf(c))) {
                    return String.valueOf(c);
                } else if (StringUtils.equals("　", String.valueOf(c))) {
                    return "　";
                } else {
                    if (1 == getCharacterWidth(c)) {
                        return "*";
                    } else {
                        return "＊";
                    }
                }
            }).collect(Collectors.joining());
        }
    }

    private static int getCharacterWidth(int codePoint) {
        int value = UCharacter.getIntPropertyValue(codePoint, UProperty.EAST_ASIAN_WIDTH);
        switch (value) {
            case UCharacter.EastAsianWidth.FULLWIDTH:
            case UCharacter.EastAsianWidth.WIDE:
                return 2;
            case UCharacter.EastAsianWidth.AMBIGUOUS:
                return (StringUtils.containsAny(Locale.getDefault().getLanguage(), "ja", "vi", "kr", "zh")) ? 2 : 1;
            default:
                return 1;
        }
    }

}
