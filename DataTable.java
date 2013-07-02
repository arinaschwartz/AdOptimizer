/**
 * @author Andrew Christoforakis, Arin Schwartz
 * 
 * Class for holding statistical constants like age and interest distributions.
 * Reads data from text file and stores it as class constants. Keeps file I/O
 * and random number generation under the hood.
 */

import java.io.*;
import java.util.*;

public class DataTable {
	
	/* name of text file we're reading from, may need to change address once out of Eclipse */
	public static final String sourceFile = "data.txt";
	
	/* class constants for our data tables */
	public static Table AgeDist;
	public static Table EduDist;
	public static Table GenderInterests;
	public static Table AgeInterests;
	public static Table EduInterests;
	
	/* handles all Random functions (randUser, randSite, etc.) */
	public static Random gen = new Random();
	
	public static void main(String[] args) {
		/* test that all our functions work */
		makeTables();
		AgeDist.printTable();
		System.out.println();
		EduDist.printTable();
		System.out.println();
		AgeInterests.printTable();
		System.out.println();
		GenderInterests.printTable();
		System.out.println();
		EduInterests.printTable();
	}
	
	/* for testing purposes */
	public static void printArray(double[] array){
		for (int i=0; i < array.length; i++)
			System.out.print(array[i] + " ");
		System.out.println();
	}
	
	/* given String representing enum value (COLLEGE, SOCIAL_NETWORK, etc.),return index from respective values array */
	public static int arrayIndex(Object[] array, String name){
		for (int i=0; i < array.length; i++)
			if (array[i].toString().equals(name))
				return i;
		/* else not in array */
		System.out.println("ERROR, VALUE NOT IN ARRAY.");
		return -1;
	}
	
	/* takes array of doubles and normalizes values to add up to one */
	public static void normalize(double[] array){
		double total = 0.0;
		for (int i=0; i < array.length; i++)
			total += array[i];
		for (int i=0; i < array.length; i++)
			array[i] /= total;
	}
	
	/* takes normalized array of doubles and makes cumulative distribution */
	public static void cumulate(double[] array){
		for (int i=1; i < array.length; i++)
			array[i] += array[i-1];
	}
	
	/**
	 * Takes empty set and array of values, fills set with five likely interests
	 * based on cumulative distribution function for one demographic category.
	 * randUser function calls this three times (for age, gender, and education).
	 * 
	 * @param set		Empty set representing user interests
	 * @param values	Array with weighted interest scores
	 */
	public static void fillSet(Set<User.Interests> set, double[] values){
		normalize(values);
		cumulate(values);
		while (set.size() != 5){
			int index = -1;
			double r = gen.nextDouble();
			for (int i=0; i < values.length; i++){
				if (r < values[i]){
					index = i;
					break;
				}	
			}
			set.add(User.Interests.values()[index]);
		}
	}
	
	/* assigns random interests based on User demographics */
	public static User.Interests[] randInterests(User user){
		/* initialize proper interests values based on demographic */
		double[] genderValues = user.isMale ? GenderInterests.data[0] : GenderInterests.data[1];
		double[] ageValues = AgeInterests.data[arrayIndex(User.AgeGroup.values(),user.age.toString())];
		double[] eduValues = EduInterests.data[arrayIndex(User.Education.values(),user.edu.toString())];
		/* initialize sets */
		Set<User.Interests> genderSet = new HashSet<User.Interests>();
		Set<User.Interests> ageSet = new HashSet<User.Interests>();
		Set<User.Interests> eduSet = new HashSet<User.Interests>();
		/* get sample interests */
		fillSet(genderSet, genderValues);
		fillSet(ageSet, ageValues);
		fillSet(eduSet, eduValues);
		/* add all interests into one pool */
		Set<User.Interests> union = new HashSet<User.Interests>();
		union.addAll(genderSet);
		union.addAll(ageSet);
		union.addAll(eduSet);
		/* select five interests from pool at random */
		User.Interests[] interests = new User.Interests[5];
		for (int i=0; i < 5; i++){
			User.Interests sample = (User.Interests) union.toArray()[gen.nextInt(union.size())];
			interests[i] = sample;
			union.remove(sample);
		}
		return interests;
	}
	 
	/* returns buffered reader for source file, function taken from Java For Dummies by Doug Lowe */
	public static BufferedReader getReader(String name){
		BufferedReader in = null;
		try {
			File f = new File(name);
			in = new BufferedReader(new FileReader(f));
		} catch (FileNotFoundException e){
			System.out.println(" File " + name + " doesn't exist.");
			System.exit(0);
		}	
		return in;
	}
	
	/* helps with line-by-line file input, also modified from Java For Dummies by Doug Lowe */
	public static String nextLine(BufferedReader in){
		String line = null;
		try{
			line = in.readLine();
		} catch(IOException e){
			System.out.println("IO Exception");
			System.exit(0);
		}
		return line;
	}	
	
	/* retrieves Table data from text file */
	public static Table readTable(BufferedReader in){
		String intLine = nextLine(in);
		while (intLine.equals(""))
			intLine = nextLine(in);
		int numRows = Integer.parseInt(intLine);
		int numColumns = Integer.parseInt(nextLine(in));
		double[][] data = new double[numRows][numColumns];
		for (int i=0; i < numRows; i++)
			data[i] = getValues(nextLine(in));
		return new Table(numRows, numColumns, data);
	}
	
	/* takes comma-separated string of doubles and returns them as an array */
	public static double[] getValues(String info){
		String[] array = info.split(",");
		double[] data = new double[array.length];
		for (int i=0; i < array.length; i++)
			data[i] = Double.parseDouble(array[i]);
		return data;
	}	
	
	/* reads all tables from data file and sets class constants */
	public static void makeTables(){
		BufferedReader in = getReader(sourceFile);
		AgeDist = readTable(in);
		EduDist = readTable(in);
		GenderInterests = readTable(in);	
		AgeInterests = readTable(in);
		EduInterests = readTable(in);
	}
	
	
	/**
	 * Table class meant to store data that was scraped from
	 * the Internet by a Python program and saved to a
	 * source file. By saving these as class constants,
	 * we only need to scrape the Internet and read the
	 * source file once.
	 *
	 */
	public static class Table{
		public int numRows, numColumns;
		public double[][] data;
		
		/* constructor needs dimensions of table and matching 2D array */
		public Table(int numRows, int numColumns, double[][] data){
			this.numRows = numRows;
			this.numColumns = numColumns;
			this.data = data;
		}
		
		/* for testing purposes */
		public void printTable(){
			System.out.println(numRows + " by " + numColumns + " Table:");
			for (int i=0; i < numRows; i++)
				printArray(data[i]);
		}
		
		/* returns random index from given row */
		public int randValue(int rowNum){
			double r = gen.nextDouble();
			for (int i=0; i < numColumns; i++){
				if (r < data[rowNum][i])
					return i;
			}
			System.out.println("ERROR: INVALID COLUMN NUMBER RETURNED");
			return -1;
		}
	}

}
