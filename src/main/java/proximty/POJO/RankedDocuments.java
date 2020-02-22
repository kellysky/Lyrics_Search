package proximty.POJO;

import java.util.*;

/**
 * 
 * @author Senhao WANG
 *
 */
public class RankedDocuments extends LinkedHashMap<String, Float> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6546847457982434622L;
	
	// a variable for RankedDocuments.
	private RankedDocuments rankedDocuments;

	/**
	 * Return top n ranked documents.
	 */
	public RankedDocuments getTop(int n) {
		this.rankedDocuments = new RankedDocuments();
		
		int count = 0;
		Set<String> keys = this.keySet();
		Iterator<String> iterator = keys.iterator();
		
		while(count<n && iterator.hasNext()) {
			String key = iterator.next();
			this.rankedDocuments.put(key, this.get(key));
			count++;
		}
		
		return rankedDocuments;
	}

	/**
	 * Sort this ranked documents based on the value
	 */
	public static RankedDocuments sort(RankedDocuments rankedDocuments) {

		List<Map.Entry<String, Float>> entries = new ArrayList<Map.Entry<String, Float>>(
				rankedDocuments.entrySet());
		Collections.sort(entries, new Comparator<Map.Entry<String, Float>>() {
			public int compare(Map.Entry<String, Float> a,
					Map.Entry<String, Float> b) {
				return (a.getValue().compareTo(b.getValue()) * -1);
			}
		});

		RankedDocuments sortedMap = new RankedDocuments();
		for (Map.Entry<String, Float> entry : entries) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}

		return sortedMap;
	}

}
