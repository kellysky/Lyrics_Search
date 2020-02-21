package run;

import proximty.POJO.Querys;
import proximty.POJO.RankedDocuments;
import proximty.resource.Constants;
import proximty.resource.RetrievalModel;
import proximty.retrieval_module.SearchEngine;

import java.util.Iterator;
import java.util.Set;


public class App {

	// Start time of the execution
	private static long startTime = System.currentTimeMillis();
	public static void main(String[] args) {

		// Querys containing all the query present in the file at the location
		// specified by the Query_path constant.
		Querys querys = new Querys(Constants.QUERY_PATH);


		System.out.println("***************************************");
		System.out.println("  SEARCH ENGINE, PROXIMITY SCORE RETRIEVAL MODEL  ");
		System.out.println("***************************************");
		runSearchEngine(querys, RetrievalModel.PROXIMITY_SCORE);

	}

	private static void runSearchEngine(Querys querys, RetrievalModel model) {

		SearchEngine searchEngine = new SearchEngine(model);
		searchEngine.setDisplayResults(true);
		 RankedDocuments rankedDocuments=searchEngine.search(querys,10,"隔壁 国术 馆");

		Set<String> keys = rankedDocuments.keySet();
		Iterator<String> iterator = keys.iterator();
		int count = 0;
		// System.out.println(keys.size());
		while (iterator.hasNext()) {
			count++;
			String docID = iterator.next();
			System.out.println(count + ". " + docID + " - " + rankedDocuments.get(docID));
		}
		displayTime();
	}

	/**
	 * Display the execution time till present.
	 */
	private static void displayTime() {
		System.out.println();
		System.out.println("Execution time: " + ((long) System.currentTimeMillis() - startTime) / 1000f + " sec");
	}
}
