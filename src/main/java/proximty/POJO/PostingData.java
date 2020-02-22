package proximty.POJO;

import java.util.LinkedHashSet;

/**
 * @Date : Feb 23, 2020
 *
 * @Author: Senhao WANG
 */

public class PostingData {
	
	private int termFrequency;
	private int noOfPositions;
	private final LinkedHashSet<Integer> positions;
	
	
	public int getTermFrequency() {
		return termFrequency;
	}
	public void setTermFrequency(int termFrequency) {
		this.termFrequency = termFrequency;
	}
	public int getNoOfPositions() {
		return noOfPositions;
	}
	public void setNoOfPositions(int noOfPositions) {
		this.noOfPositions = noOfPositions;
	}
	public LinkedHashSet<Integer> getPositions() {
		return positions;
	}

	
	public PostingData() {
		this.positions = new LinkedHashSet<Integer>();
	}

}
