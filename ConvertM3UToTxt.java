package Mp3File;

import java.util.*;
//import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileNotFoundException;
import java.nio.file.*;
//import java.lang.Runtime.*;

public class ConvertM3UToTxt {
	String logText = "";
	String execProgram = "Notepad";
	BufferedReader br = null;
	PrintWriter log = null;
	PrintWriter out = null;

	private ConvertM3UToTxt() {
		Path oneDrivePath = getOneDrivePath();
		Path playListPath = getPlayListPath(oneDrivePath);
		Path latestFile = getRecentFile(playListPath);
		Path outputFile = getOutputFile(latestFile);
		Path logFile = getLogFile(latestFile);
		getPlayListData(latestFile, outputFile, logFile);
		execCommand(execProgram, latestFile.toString());
		execCommand(execProgram, outputFile.toString());
		execCommand(execProgram, logFile.toString());
	}

	private void execCommand(String program, String params) {
		try {
			java.lang.Runtime.getRuntime().exec(program + " " + params);
		} catch (SecurityException e) {
//			If a security manager exists and its checkExec method doesn't allow creation of the subprocess
		} catch (IOException e) {
//			If an I/O error occurs
		} catch (NullPointerException e) {
//			If command is null
		} catch (IllegalArgumentException e) {
//			If command is empty)
		}
	}

	private Path getFile(Path fileName, String extent) {
		this.logText += "getFile: " + fileName.toString() + "\n";
		String newFileName = fileName.toString().substring(0, fileName.toString().length() - 3).concat(extent);
		this.logText += "outFile: " + newFileName + "\n";
		return Path.of(newFileName);
	}

	private Path getLogFile(Path latestFile) {
		return getFile(latestFile, "log");
	}

	private String getMp3Tag(Path filePath) throws Exception {
		String returnString = "";
		Mp3File f      = new Mp3File(filePath.toFile());
//	    System.out.println(f.getLengthInSeconds());
//	    }// else {
		if (f.hasId3v1Tag()) {
			ID3v1 id3v1Tag = f.getId3v1Tag();
			returnString = id3v1Tag.getArtist() + " - " + id3v1Tag.getTitle() + " ("
					+ id3v1Tag.getGenreDescription() + ")";
			log.println("Output: " + "V1 Tag " +  returnString);
		} else {
			log.println("Error Mp3 Tag: ");
			// return "Error Mp3 Tag: ";
		}
		if (f.hasId3v2Tag()) {
			ID3v2 id3v2Tag = f.getId3v2Tag();
			returnString = id3v2Tag.getArtist() + " - " + id3v2Tag.getTitle() + " (" + id3v2Tag.getGenreDescription()
					+ ")";
			log.println("Output: " + "V2 Tag " + returnString);			
		}
		return returnString;
	}

	private Path getOneDrivePath() {
		String key = "OneDriveConsumer";
//		Map<String, String> env = new HashMap<String, String>();
		String env = System.getenv(key);
//		System.out.println("Total Entries: " + env.size());
//        for (String key : env.keySet())
		this.logText += key + " - " + env + "\n";
		return Path.of(env);
	}

	private Path getOutputFile(Path latestFile) {
		return getFile(latestFile, "txt");
	}

	private void getPlayListData(Path playList, Path outFile, Path logFile) {
		try {
			br = new BufferedReader(new FileReader(playList.toFile()));
			log = new PrintWriter(new FileWriter(logFile.toString()));
			out = new PrintWriter(new FileWriter(outFile.toString()));
			log.println(this.logText);
			this.logText = "";
//		    StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			while (line != null) {
				log.println("Input: " + line);
				if (!(line.startsWith("F:\\Radio\\3WBC") || line.startsWith("F:\\Radio\\Spoken"))) {
					log.println("Accept: " + line);
					try {
						String tag = getMp3Tag(Path.of(line));
//						log.println("Output: " + tag);
						out.println(tag);
					} catch (Exception e) {
						System.out.println("MP3 Error: " + e.getMessage());
						log.println("Mp3 Error: " + e.getMessage());
					}
//					MP3File mp3file = new MP3File(sourceFile);
				}
//		        sb.append(System.lineSeparator());
				line = br.readLine();
			}
//		    String everything = sb.toString();
			br.close();
		} catch (FileNotFoundException e) {
			System.out.println("File Error: " + playList + ": " + e.getMessage());
		} catch (IOException e) {
			System.out.println("File Error: " + playList + ": " + e.getMessage());
		}
		log.println(this.logText);
		log.close();
		out.close();
	}

	private Path getPlayListPath(Path oneDrivePath) {
		Path path = Paths.get(oneDrivePath.toString(), "\\Music\\Playlists");
		this.logText += "Combined path: " + path.toString() + "\n";
		return path;
	}

	// if you have a huge number of files in deeply nested directories
	// this might need some further tuning
	private Path getRecentFile(Path directory) {
		// here we get the stream with full directory listing
		Optional<Path> lastFilePath = null;
		try {
			lastFilePath = Files.list(directory)
					// exclude subdirectories from listing
					.filter(f -> f.getFileName().toString().endsWith(".m3u"))
					// finally get the last file using simple comparator by lastModified field
					.max(Comparator.comparingLong(f -> f.toFile().lastModified()));
//				System.out.println("Latest File: " + lastFilePath.toString());
		} catch (IOException e) {
			System.out.println("IO Exception; " + e.getMessage());
		}
		// your folder may be empty
		if (!lastFilePath.isPresent()) {
			this.logText += "Latest File: not found" + "\n";
			return null;
		}

		this.logText += "Latest File: " + lastFilePath.get() + "\n";
		return lastFilePath.get();
	}

	public static void main(String[] args) {

		ConvertM3UToTxt convertM3UToTxt = new ConvertM3UToTxt();

	}

}
