package com.gt.jdbcbackup;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.gt.jdbcbackup.db.Backup;
import com.gt.jdbcbackup.db.ScriptExecutor;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class MainClass {

	public static void main(String[] args)
			throws SQLException, ClassNotFoundException, FileNotFoundException, IOException {

		CommandLineParser parser = new DefaultParser();
		CommandLine commandLine = null;
		try {
			commandLine = parser.parse(MainClass.buildOptions(), args);
		} catch (ParseException ex) {
			Logger.getLogger(MainClass.class.getName()).log(Level.SEVERE, "Error al leer las opciones", ex);
			return;
		}

		if (commandLine.hasOption("help")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("Ayuda de jdbcbackup", MainClass.buildOptions());
		} else if (commandLine.hasOption("exec")) {
			ScriptExecutor.runFile(commandLine.getOptionValue("d"), commandLine.getOptionValue("j"),
					commandLine.getOptionValue("u"), commandLine.getOptionValue("p"),
					commandLine.getOptionValue("file"));
		} else {

			Backup.toZipFile(commandLine.getOptionValue("d"), commandLine.getOptionValue("j"),
					commandLine.getOptionValue("u"), commandLine.getOptionValue("p"),
					commandLine.getOptionValue("file"), commandLine.hasOption("ddl"),
					commandLine.getOptionValues("catalog"), commandLine.getOptionValues("schema"),
					commandLine.getOptionValue("dialect"));
		}
	}

	public static Options buildOptions() {

		Options options = new Options();

		options.addOption(Option.builder("h").longOpt("help").argName("comando").build());

		options.addOption(
				Option.builder("d").longOpt("driverclass").hasArg().desc("Clase java del driver jdbc").build());
		options.addOption(Option.builder("j").longOpt("jdbc").hasArg().desc("jdbc connection string").build());
		options.addOption(
				Option.builder("l").longOpt("ddl").optionalArg(true).desc("indica que solo se creará el DDL").build());
		options.addOption(Option.builder("u").longOpt("uid").hasArg().desc("Nombre de usuario").build());
		options.addOption(Option.builder("s").longOpt("schema").hasArg().desc("lista de schemas a copiar").numberOfArgs(Option.UNLIMITED_VALUES).build());
		options.addOption(Option.builder("c").longOpt("catalog").hasArg().desc("lista de catalogos a copiar").numberOfArgs(Option.UNLIMITED_VALUES).build());
		
		options.addOption(Option.builder("p").longOpt("pass").hasArg().desc("contraseña").build());
		
		
		options.addOption(Option.builder("f").longOpt("file").hasArg().desc("base de datos").build());
		options.addOption(Option.builder("a").longOpt("dialect").hasArg()
				.desc("Dialecto del script generado, puede ser postgre, mysql, hsql o sqlserver").build());
		options.addOption(Option.builder("e").longOpt("exec").optionalArg(true).desc(
				"indica que se debe ejecutar el script SQL indicado en file en vez de realizar un backup, cada línea se separa con ;\n")
				.build());

		return options;
	}

	public static String getJarPath() {
		CodeSource codeSource = MainClass.class.getProtectionDomain().getCodeSource();

		String jarDir = "";
		try {
			File jarFile = new File(codeSource.getLocation().toURI().getPath());
			jarDir = jarFile.getParentFile().getPath();
		} catch (URISyntaxException ex) {
			java.util.logging.Logger.getLogger(MainClass.class.getName()).log(java.util.logging.Level.SEVERE,
					"Error buscando jar path", ex);
		}
		return jarDir;
	}

}