package edu.wit.cs.comp3370;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/* Provides a solution to the 0-1 knapsack problem 
 * 
 * Wentworth Institute of Technology
 * COMP 3370
 * Lab Assignment 8
 * Rachel Palmer
 */

public class LAB8 {

	/**
	 * FindDynamic uses two matrices of m by n size. P keeps track of the best value for the 
	 * current index. bools keeps track of whether or not to include a weight when figuring out
	 * which indices make up the best value. If the weight of the current Item is less than the
	 * curr w value (meaning more can fit in the knapsack), then P[i][w] is calculated by finding
	 * the max of the last value, and value at the w val corresponding to w-w[i] (which represents
	 * the exact amount of weight that can still fit in the knapsack). otherwise, P[i][w] is equal
	 * to P at the previous i value. best_value is equal to the final index in P. 
	 * findItems uses a recursive method to find the exact sequence of items that made up the 
	 * best value. Starting from the last index, it looks to see whether that index is true.
	 * If it is, then the Item from table at that index is added to the result array and 
	 * findItems is called for i-1 and w-wi (which was the last weight value used in 
	 * calculations. Otherwise, it ignore the value and checks the b value at i-1. Once i or w
	 * reach 0, this means there are no more items that can be added to the sack, so the result
	 * array is finalized. 
	 *
	 * @param table			table of Items with a weight and value
	 * @param weight		the max weight allowed in the knapsack
	 * @return				array of indices of items that make the best value
	 */
	public static Item[] FindDynamic(Item[] table, int weight) {
		int m = table.length+1; //+1 to include all values in table (since theyre accessed by i-1 below)
		int n = weight+1;
		int[][] P = new int[m][n];
		Boolean[][] bools = new Boolean[m][n]; //keeps true/false vals when number at index updates
		for(int i=0;i<m;i++){
			P[i][0] = 0;
		}
		for(int j=0;j<n;j++){
			P[0][j] = 0;
		}
		for(int i=1;i<m;i++){
			for(int w=1;w<n;w++){
				if(table[i-1].weight <= w){
					P[i][w] = Math.max(P[i-1][w], table[i-1].value + P[i-1][w-table[i-1].weight]);
					if(P[i-1][w] < P[i][w]) //if the 2nd max arg was the one accepted
						bools[i][w] = true;
					else //if prev val was max or equal to (no val update)
						bools[i][w] = false;
				}
				else{
					P[i][w] = P[i-1][w];
					bools[i][w] = false;
				}
			}
		}
		best_value = P[m-1][n-1]; //last index of P
		ArrayList<Item> result = new ArrayList<Item>(); //ArrayList used so indices arent needed in findItems
		findItems(table, P, bools, result, m-1, n-1); //start with final index
		Item[] r = result.toArray(new Item[result.size()]);
		return r;
	}
	
	public static void findItems(Item[] table, int[][] P, Boolean[][] b, ArrayList<Item> result, int i, int w){
		if(i==0 || w == 0) //no more items can be added
			return;
		if(b[i][w]){
			result.add(table[i-1]);
			findItems(table, P, b, result, i-1, w - table[i-1].weight); //go to index of last best value to go into knapsack with current index
		}
		else{
			findItems(table, P, b, result, i-1, w); 
		}
		
	}

	/********************************************
	 * 
	 * You shouldn't modify anything past here
	 * 
	 ********************************************/

	// set by calls to Find* methods
	private static int best_value = 0;
	
	public static class Item {
		public int weight;
		public int value;
		public int index;

		public Item(int w, int v, int i) {
			weight = w;
			value = v;
			index = i;
		}

		public String toString() {
			return "(" + weight + "#, $" + value + ")"; 
		}
	}

	// enumerates all subsets of items to find maximum value that fits in knapsack
	public static Item[] FindEnumerate(Item[] table, int weight) {

		if (table.length > 31) {	// bitshift fails for larger sizes
			System.err.println("Problem size too large. Exiting");
			System.exit(0);
		}
		
		int nCr = 1 << table.length; // bitmask for included items
		int bestSum = -1;
		boolean[] bestUsed = {}; 
		boolean[] used = new boolean[table.length];
		
		for (int i = 0; i < nCr; i++) {	// test all combinations
			int temp = i;

			for (int j = 0; j < table.length; j++) {
				used[j] = (temp % 2 == 1);
				temp = temp >> 1;
			}

			if (TotalWeight(table, used) <= weight) {
				if (TotalValue(table, used) > bestSum) {
					bestUsed = Arrays.copyOf(used, used.length);
					bestSum = TotalValue(table, used);
				}
			}
		}

		int itemCount = 0;	// count number of items in best result
		for (int i = 0; i < bestUsed.length; i++)
			if (bestUsed[i])
				itemCount++;

		Item[] ret = new Item[itemCount];
		int retIndex = 0;

		for (int i = 0; i < bestUsed.length; i++) {	// construct item list
			if (bestUsed[i]) {
				ret[retIndex] = table[i];
				retIndex++;
			}
		}
		best_value = bestSum;
		return ret;

	}

	// returns total value of all items that are marked true in used array
	private static int TotalValue(Item[] table, boolean[] used) {
		int ret = 0;
		for (int i = 0; i < table.length; i++)
			if (used[i])
				ret += table[i].value;

		return ret;
	}

	// returns total weight of all items that are marked true in used array
	private static int TotalWeight(Item[] table, boolean[] used) {
		int ret = 0;
		for (int i = 0; i < table.length; i++) {
			if (used[i])
				ret += table[i].weight;
		}

		return ret;
	}

	// adds items to the knapsack by picking the next item with the highest
	// value:weight ratio. This could use a max-heap of ratios to run faster, but
	// it runs in n^2 time wrt items because it has to scan every item each time
	// an item is added
	public static Item[] FindGreedy(Item[] table, int weight) {
		boolean[] used = new boolean[table.length];
		int itemCount = 0;

		while (weight > 0) {	// while the knapsack has space
			int bestIndex = GetGreedyBest(table, used, weight);
			if (bestIndex < 0)
				break;
			weight -= table[bestIndex].weight;
			best_value += table[bestIndex].value;
			used[bestIndex] = true;
			itemCount++;
		}

		Item[] ret = new Item[itemCount];
		int retIndex = 0;

		for (int i = 0; i < used.length; i++) { // construct item list
			if (used[i]) {
				ret[retIndex] = table[i];
				retIndex++;
			}
		}

		return ret;
	}
	
	// finds the available item with the best value:weight ratio that fits in
	// the knapsack
	private static int GetGreedyBest(Item[] table, boolean[] used, int weight) {

		double bestVal = -1;
		int bestIndex = -1;
		for (int i = 0; i < table.length; i++) {
			double ratio = (table[i].value*1.0)/table[i].weight;
			if (!used[i] && (ratio > bestVal) && (weight >= table[i].weight)) {
				bestVal = ratio;
				bestIndex = i;
			}
		}

		return bestIndex;
	}

	public static int getBest() {
		return best_value;
	}
	
	public static void main(String[] args) {
		Scanner s = new Scanner(System.in);
		String file1;
		int weight = 0;
		System.out.printf("Enter <objects file> <knapsack weight> <algorithm>, ([d]ynamic programming, [e]numerate, [g]reedy).\n");
		System.out.printf("(e.g: objects/small 10 g)\n");
		file1 = s.next();
		weight = s.nextInt();

		ArrayList<Item> tableList = new ArrayList<Item>();

		try (Scanner f = new Scanner(new File(file1))) {
			int i = 0;
			while(f.hasNextInt())
				tableList.add(new Item(f.nextInt(), f.nextInt(), i++));
		} catch (IOException e) {
			System.err.println("Cannot open file " + file1 + ". Exiting.");
			System.exit(0);
		}

		Item[] table = new Item[tableList.size()];
		for (int i = 0; i < tableList.size(); i++)
			table[i] = tableList.get(i);

		String algo = s.next();
		Item[] result = {};

		switch (algo.charAt(0)) {
		case 'd':
			result = FindDynamic(table, weight);
			break;
		case 'e':
			result = FindEnumerate(table, weight);
			break;
		case 'g':
			result = FindGreedy(table, weight);
			break;
		default:
			System.out.println("Invalid algorithm");
			System.exit(0);
			break;
		}

		s.close();

		System.out.printf("Index of included items: ");
		for (int i = 0; i < result.length; i++)
			System.out.printf("%d ", result[i].index);
		System.out.printf("\nBest value: %d\n", best_value);	
	}

}
