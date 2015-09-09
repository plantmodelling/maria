package leafJ;
import java.util.Comparator;


public class Sort2D implements Comparator<int[]> {
		private int column;
		public Sort2D(int column) {
			this.column = column;
		}
			
		public int compare(int[] one, int[] two) {
			return one[column]-two[column];
		}
	}
