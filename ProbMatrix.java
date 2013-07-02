import java.util.*;

public class ProbMatrix {
	public static final Random gen = new Random();
	
	public static final double[] AgeDist = new double[] {.214, .355, .489, .638, .811, .913, 1.0};
	public static final double[][] EduDist = new double[][] {{.98, 1.0, 1.0, 1.0},
								 {.302, .664, .956, 1.0},
								 {.395, .68, .905, 1.0},
								 {.35, .615, .83, 1.0},
								 {.42, .67, .98, 1.0},
								 {.41, .68, .865, 1.0},
								 {.545, .76, .895, 1.0}};
	
    //THESE ARE PLACE HOLDER PROBABILITIES UNTIL WE HAVE BETTER DATA

    //probabilities that various demographics have particular interests
    public static final double[] MaleInterests = new double[] {.40, .50, .50, .40, .70, .35, .85, .70, .60, .50, .50, .25, .24, .25, .25, .25, .25, .25, .25};
    //this should be the the complement of the MaleInterests array
    public static final double[] FemaleInterests = new double[] {.60, .50, .50, .60, .30, .65, .15, .30, .40, .50, .50, .75, .76, .75, .75, .75, .75, .75, .75};
	public static final double[][] AgeInterests = new double[][] {{.50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50},
								      {.50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50},
								      {.50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50},
								      {.50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50},
								      {.50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50},
								      {.50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50},
								      {.50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50}};
	
	 public static final double[][] EducInterests = new double[][] {{.50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50},
		 							{.50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50},
		 							{.50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50},
		 							{.50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50, .50}};
	
	//helper function to print individual rows
	public static void printArray(double[] array){
		for (int i=0; i < array.length; i++)
			System.out.println(array[i]);
	}
	
	//given array of doubles, returns column number based on cdf
	//used for assigning random age group and education
	public static int randColumn(double[] array){
		double r = gen.nextDouble();
		for (int i=0; i < array.length; i++){
			if (r < array[i])
				return i;
		}
		System.out.println("ERROR: INVALID COLUMN NUMBER RETURNED");
		return -1;
	}
	
	public static int unsortedArrayIndex(double[] array, double value){
		for (int i=0; i < array.length; i++)
			if (array[i] == value)
				return i;
		System.out.println("ERROR, VALUE NOT IN ARRAY");
		return -1;
	}
	
	//generates random age group based on given age distribution
	public static User.AgeGroup randAgeGroup(){
		return User.AgeGroup.values()[randColumn(AgeDist)];
	}
	
    //generates random "gender" based on coin flip
    public static boolean randGender(){
            double value = gen.nextDouble();
            return value > 0.50;
    }

    //generates random education level given age group
    public static User.Education randEduc(User.AgeGroup age){
            //find integer corresponding to age group
            int index = -1;
            for (int i=0; i < User.AgeGroup.values().length; i++){
                    if (User.AgeGroup.values()[i] == age)
                            index = i;
            }
            if (index < 0)
                    System.out.println("ERROR, INDEX NOT ASSIGNED.");

            return User.Education.values()[randColumn(EduDist[index])];
    }

    //increases scores by random function of interest values
    public static void rankInterests(double[] scores, double[] values){
            for (int i=0; i < User.Interests.values().length; i++){
                    int random = Math.abs(gen.nextInt()) % 100 + 1;
                    scores[i] += random*values[i];
            }
    }
	
    //EDIT THIS LATER TO MAKE IT MORE SOPHISTICATED
    //generates random interests given user demographics
    public static User.Interests[] randInterests(User.AgeGroup age, boolean isMale, 
User.Education edu){
    	//rankings initialized to zero
    	double[] rankings = new double[User.Interests.values().length];
    	
    	//need indices for age group and education level
    	int ageIndex = -1, eduIndex = -1;
    	for (int i=0; i < User.AgeGroup.values().length; i++)
    		if (User.AgeGroup.values()[i] == age)
    			ageIndex = i;
    	for (int i=0; i < User.Education.values().length; i++)
    		if (User.Education.values()[i] == edu)
    			eduIndex = i;
    	double[] genderInterests = isMale ? MaleInterests : FemaleInterests;
    	
    	rankInterests(rankings, genderInterests);
    	rankInterests(rankings, AgeInterests[ageIndex]);
    	rankInterests(rankings, EducInterests[eduIndex]);
    	
    	return topFive(rankings);
    }
    
    //given list of rankings, returns top five interests
    public static User.Interests[] topFive(double[] rankings){
    	int numInterests = User.Interests.values().length;
    	double[] copy = new double[numInterests];
    	for (int i=0; i < numInterests; i++)
    		copy[i] = rankings[i];
    	Arrays.sort(copy);
    	User.Interests topFive[] = new User.Interests[5];
    	for (int i = numInterests-1; i > numInterests - 6; i--)
    		topFive[numInterests-i-1] = 
User.Interests.values()[unsortedArrayIndex(rankings, copy[i])];
    	
    	return topFive;
    }
    
	public static void main(String[] args){
	
	}
}
