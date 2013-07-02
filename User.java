/** 
 * @author Andrew Christoforakis, Arin Schwartz
 * 
 * A typical person browsing the Internet. Each User consists of a set of demographics:
 * age, education level, and gender. Users will also have ranked lists
 * of interests that help determine what web sites they'll gravitate
 * towards when roaming the net. This class also holds the enumerations
 * we use to deliniate our demographics.
 *
 */
public class User {

	public AgeGroup age;
	public boolean isMale;
	public Education edu;
	public Interests[] interests = new Interests[5];
	public Website site; /* what web site the User is currently on, important when traversing the web*/
	
	public static final int cookies = 3; //may or may not implement
	
	public enum AgeGroup{
		TEEN, YOUNG_ADULT, ADULT, MIDDLE_AGED, OLDER, OLDER_STILL, RETIRED
	}
	
	public enum Education{
		NO_COLLEGE, COLLEGE, GRAD_SCHOOL
	}
	
	public enum Interests{
		BLOGGING, SOCIAL_NETWORKS, SUBMITTED_CONTENT, BUSINESS_AND_FINANCE, BANKING, EMPLOYMENT, GAMING, SPORTS, MUSIC, MOVIES_AND_TV, WORLD_NEWS, GOSSIP, US_POLITICS, REVIEWS, ACADEMIC, PRACTICAL_INFO, CLOTHING, ELECTRONICS, TRADE
	}		
	
	/**
	 * Constructor asks for the three demographic components,
	 * web site and interests determined later
	 * 
	 * @param age		User's age group
	 * @param isMale	whether or not User is male
	 * @param edu		User's education level
	 */
	public User(AgeGroup age, boolean isMale, Education edu){
		this.age = age;
		this.isMale = isMale;
		this.edu = edu;
		this.interests = null;
		this.site = null;
	}
	
	/**
	 * Generates random User whose demographic components
	 * and interests are determined by probabilty tables
	 * in DataTable.java (stored in data.txt)
	 * 
	 * @return randomly generated User
	 */
	public static User randUser(){
		DataTable.makeTables();
		AgeGroup age = AgeGroup.values()[DataTable.AgeDist.randValue(0)];
		double coinFlip = DataTable.gen.nextDouble();
		boolean gender = (coinFlip > 0.5);
		int AgeIndex = DataTable.arrayIndex(AgeGroup.values(), age.toString());
		Education edu = Education.values()[DataTable.EduDist.randValue(AgeIndex)];
		User user = new User(age, gender, edu);
		user.interests = DataTable.randInterests(user);
		return user;
	}
	
	/* places User on web site, called in Internet.java and User.hopSite() */
	public void setSite(Website site){
		this.site = site;
	}
	
	/**
	 * User moves to one of the sites linked to by its current site
	 * based on User's interests and other sites' content.
	 * To prevent pooling among the most popular sites, User jumps to 
	 * random site with a constant probabilty. 
	 * 
	 * @param WWW	The Internet; only comes into play if User jumps to random site
	 */
	public void hopSite(Internet WWW){
		double p = DataTable.gen.nextDouble();
		if (p < 0.15 || this.site.sponsors.isEmpty()){
			randJump(WWW);
			return;
		}	
		/* User to hop to web site with highest score */
		int scores[] = new int[this.site.sponsors.size()];
		/* for each site connected to this one, calculate that site's appeal to User */
		for (int i=0; i < scores.length; i++)
			scores[i] = this.site.sponsors.get(i).site.scoreInterest(this.interests);
		/* find index of site with max interest score */
		int maxScore = Integer.MIN_VALUE, index = -1;
		for (int i=0; i < scores.length; i++){
			if (scores[i] > maxScore){
				maxScore = scores[i];
				index = i;
			}	
		}
		/* move User to site with max score */
		this.site = this.site.sponsors.get(index).site;
	}
	
	/**
	 * User jumps to random site on Internet
	 * 
	 * @param WWW	Internet containing all sites the user can jump to
	 */
	public void randJump(Internet WWW){
		Website newSite = WWW.randSite();
		this.site = newSite;
		newSite.tabulate(this);
	}
	
	/* print User's interests, for testing purposes */
	public void printInterests(){
		for (int i=0; i < 5; i++)
			System.out.println((i+1) + ": " + interests[i].toString());
	}
	
	/* print all of User's information, for testing purposes */
	public void printUser(){
		String gender = isMale ? "Male" : "Female";
		System.out.println(age.toString() + '\t' + gender + '\t' + edu.toString());
		printInterests();
	}
	
	public static void main(String[] args) {
		for (int i=0; i < 100; i++){
			System.out.println("User #" + (i+1));
			User test = randUser();
			test.printUser();
			System.out.println();
		}
	}

}
