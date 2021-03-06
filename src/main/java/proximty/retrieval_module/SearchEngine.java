package proximty.retrieval_module;

import proximty.POJO.QueryTermMap;
import proximty.POJO.Querys;
import proximty.POJO.RankedDocuments;
import proximty.resource.Constants;
import proximty.resource.RetrievalModel;
import proximty.tools.Soundex;
import proximty.tools.TextFile;

import java.util.*;


public class SearchEngine {

	// Represents the retrieval model for this search engine.
	private final RetrievalModel retrievalModel;

	// QueryMap generated from the given querys
	private LinkedHashMap<String, QueryTermMap> queryMap;

	// Indexer for this search engine.
	private Indexer indexer = null;

	// Variable representing the object for BM25 retrieval model
	private BM25 bm25Model;

	// Variable representing the object for ProximityScore retrieval model
	private ProximityScoreModel proximityScoreModel;

	// Ranked documents of the search results.
	private RankedDocuments rankedDocuments;

	// Text file handler for the results of this search engine.
	private TextFile resultFile;

	private boolean displayResults = true;
	private boolean performQueryCorrection = false;


	public boolean isPerformQueryCorrection() {
		return performQueryCorrection;
	}

	public void setPerformQueryCorrection(boolean performQueryCorrection) {
		this.performQueryCorrection = performQueryCorrection;
	}

	public boolean isDisplayResults() {
		return displayResults;
	}

	public void setDisplayResults(boolean displayResults) {
		this.displayResults = displayResults;
	}

	/**
	 * Getter and setter methods for Indexer.
	 */
	public Indexer getIndexer() {
		return indexer;
	}

	public void setIndexer(Indexer indexer) {
		this.indexer = indexer;
	}

	/**
	 * Constructor for this Search engine.
	 */
	public SearchEngine(RetrievalModel retrievalModel,String[] term_list,String metadata) {
		this.retrievalModel = retrievalModel;
		this.indexer = new Indexer();
		this.indexer.generate(1, true,term_list,metadata);
	}

	/**
	 * Search the documents and order them according to their BM25 score, for the given querys. Save the result in
	 * Results directory.
	 * 
	 */
	public RankedDocuments search(Querys querys,int top,String term) {
        RankedDocuments result = null;
		 
		if (isDisplayResults())
			System.out.println("\n*** Top 5 Search Results ***");

		if (getIndexer().size() != 0) {

			queryMap = querys.generate(getIndexer().getInvertedIndex(),term);
			bm25Model = new BM25(getIndexer());
			proximityScoreModel = new ProximityScoreModel(getIndexer());

			int queryCount = 0;

			Set<String> querySet = queryMap.keySet();
			Iterator<String> iterator = querySet.iterator();

			// for each query
			while (iterator.hasNext()) {
				queryCount++;
				String queryText = iterator.next();
				String correctQuery;

				if (isPerformQueryCorrection()) {
					HashMap<String, ArrayList<String>> map = Soundex
							.generateMap(getIndexer().getInvertedIndex().keySet());
					correctQuery = Soundex.getCorrectQuery(queryText, map);
					queryText = correctQuery;
				}

				if (isDisplayResults()) {
					System.out.println();
					System.out.println("Query : " + queryText);
					System.out.println("-------------------------------");
				}

				if (queryMap.containsKey(queryText)) {
					if (this.retrievalModel.equals(RetrievalModel.BM25))
						rankedDocuments = bm25Model.getRankedDocuments(this.queryMap.get(queryText));
					else if (this.retrievalModel.equals(RetrievalModel.PROXIMITY_SCORE))
						rankedDocuments = proximityScoreModel.getRankedDocuments(queryText, queryMap.get(queryText));

					// Display the top 5 results.
					if (isDisplayResults())
						result=rankedDocuments.getTop(top);
						//displayRankedDocuments(rankedDocuments.getTop(5));

					// Save the results.
					if (this.retrievalModel.equals(RetrievalModel.BM25)) {
						resultFile = new TextFile(String.valueOf(queryCount), Constants.BM25_FOLDER,
								Constants.EXTENSION_TXT);
					} else if (this.retrievalModel.equals(RetrievalModel.PROXIMITY_SCORE)) {
						resultFile = new TextFile(String.valueOf(queryCount), Constants.PROXIMITY_SCORE_FOLDER,
								Constants.EXTENSION_TXT);
					}

					resultFile.saveResults(rankedDocuments.getTop(100), this.retrievalModel, queryCount);
					rankedDocuments.clear();

				}

			}

			if (!isDisplayResults())
				System.out.println(this.retrievalModel + " results generated.");

		} else {
			System.out.println("Inverted index not present.");
		}

		/*
		changed by senwang
		 */
		return result;
}

	/**
	 * Display the details of the ranked documents.
	 */
	private void displayRankedDocuments(RankedDocuments documents) {
		Set<String> keys = documents.keySet();
		Iterator<String> iterator = keys.iterator();
		int count = 0;
		// System.out.println(keys.size());
		while (iterator.hasNext()) {
			count++;
			String docID = iterator.next();
			System.out.println(count + ". " + docID + " - " + documents.get(docID));
		}
	}

}
