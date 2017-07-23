package com.example;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.OptionHandlerFilter;
import org.kohsuke.args4j.ParserProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DatamaskingCommandLineRunner implements CommandLineRunner {

	private final static Logger logger = LoggerFactory.getLogger(DatamaskingCommandLineRunner.class);

	@Option(required = true, name = "-i", usage = "input from this file", metaVar = "INPUT")
	private File in = new File(".");

	@Option(required = true, name = "-o", usage = "output to this file", metaVar = "OUTPUT")
	private File out = new File(".");

	@Option(name = "-F", usage = "set masking fields. require multi field camma separeated", metaVar = "Mask Fields")
	private String fields = "";

	@Argument
	private List<String> arguments = new ArrayList<String>();

	@Override
	public void run(String... args) throws Exception {
		logger.info(ToStringBuilder.reflectionToString(args));
		logger.info(
				Arrays.asList(args).stream().collect(Collectors.joining(",", getClass().getSimpleName() + "[", "]")));

		CmdLineParser parser = new CmdLineParser(this);
		parser.getProperties();
		ParserProperties props = ParserProperties.defaults();
		props.withUsageWidth(80);

		try {
			List<String> argList = Arrays.asList(args).stream().filter(s -> !StringUtils.startsWith(s, "--spring."))
					.collect(Collectors.toList());
			argList.stream().forEach(System.out::println);
			parser.parseArgument();

			if (arguments.isEmpty())
				throw new CmdLineException(parser, "No argument is given");

		} catch (CmdLineException e) {
			logger.error(e.getMessage(), e);
			// if there's a problem in the command line,
			// you'll get this exception. this will report
			// an error message.
			System.err.println(e.getMessage());
			System.err.println("java datamask [options...] arguments...");
			// print the list of available options
			parser.printUsage(System.err);
			System.err.println();

			// print option sample. This is useful some time
			System.err.println("  Example: java datamasking" + parser.printExample(OptionHandlerFilter.ALL));

			return;
		}

	}

}
