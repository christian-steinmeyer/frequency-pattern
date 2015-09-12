package de.uniba.mobi.frequencyPattern;

import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import de.uniba.mobi.jdbc.DBConnection;

public class Application {

	private static int index = 0;
	private static int numberOfElements = 0;

	public static void main(String[] args) {
		try {
			DBConnection.connect();
			createNodesFile("nodes.ser");
			DBConnection.disconnect();
			ArrayList<Node> nodes = readNodesFromFile("nodes.ser");
			writeCSVFromNodes(nodes);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void createNodesFile(String filename) throws IOException {
		System.out.println("Getting a lot of information from database. "
				+ "This will take a while.\n"
				+ "Just kidding. This is only the beginning.");
		List<String> hashmacs = DBConnection.getAllHashMacs();
		System.out.println("finished");

		// ArrayList<String> shortmacs = new ArrayList<String>();
		// for (int i = 0; i < 100; i++) {
		// shortmacs.add(hashmacs.get(i));
		// }

		numberOfElements = hashmacs.size();
		// numberOfElements = shortmacs.size();

		System.out.println("Creating nodes file from " + numberOfElements
				+ " hashmacs: ");
		JsonFileWriter file = new JsonFileWriter(filename);
		for (String each : hashmacs) {
			// for (String each : shortmacs) {
			Node node = new Node(each);
			node.setTimeline(DBConnection.getTimeline(each));
			file.writeDataToFile(node);
			System.out.print(".");
			if (index++ % 75 == 74)
				System.out.print("\n");

		}
		index = 0;
		file.finish();
		System.out.println("finished");
	}

	private static ArrayList<Node> readNodesFromFile(String filename)
			throws IOException, ClassNotFoundException {
		System.out.println("Reading nodes from file: ");
		ArrayList<Node> result = new ArrayList<>();
		JsonFileReader file = new JsonFileReader(filename);
		for (int i = 0; i < numberOfElements; i++) {
			result.add(file.readDataPointFromFile());
		}
		file.finish();
		System.out.println("finished");
		return result;
	}

	private static void writeCSVFromNodes(ArrayList<Node> nodes)
			throws ClassNotFoundException, IOException {
		System.out.println("Calculating values for " + nodes.size()
				+ " values:");
		LocalTime eventBeginOfDay = LocalTime.of(12, 0);
		LocalTime eventEndOfDay = LocalTime.of(18, 0);
		FrequencyPattern frequencyPattern = new FrequencyPattern(
				eventBeginOfDay, eventEndOfDay);
		CsvGenerator csv = new CsvGenerator("test.csv");
		csv.addCell("mac hashes");

		// header row
		for (Node each : nodes) {
			csv.addCell(each.getHashmac());
		}
		csv.newRow();

		// data rows
		for (int rowIndex = 0; rowIndex < nodes.size(); rowIndex++) {
			System.out.print(".");
			if (rowIndex % 75 == 74)
				System.out.print("\n");
			// header column
			csv.addCell(nodes.get(rowIndex).getHashmac());

			// data columns
			for (int cellIndex = 0; cellIndex < nodes.size(); cellIndex++) {
				float value = rowIndex == cellIndex ? 0f : frequencyPattern
						.frequencyGenerator(nodes.get(rowIndex),
								nodes.get(cellIndex));
				csv.addCell(String.valueOf(value));
			}
			csv.newRow();
		}
		csv.close();
		System.out.println("finished");
	}
}