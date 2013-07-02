/**
 * by Andrew Christoforakis and Arin Schwartz
 * 
 * A potential web site, characterized by the User demographics it
 * hopes to appeal to, as well as a User interest to describe its content.
 * 
 * Web sites link to one another by placing advertisements on other sites.
 * Every round, they dynamically alter their lists of ad placements and
 * sponsors in hopes of attracting their target demographic. They bid 
 * for ads on popular sites by submitting their willingness to pay (WTP),
 * which is based on that site's traffic figures. The offers a web site
 * receives determine the cost of advertising on the site.
 * 
 * See README.txt for a more complete list of our assumptions for this model. 
 */

import java.util.*;

public class Website {

	/* for the purpose of identifying the site, both to the client and to other sites */
	public String domainName;
	public int ID;
	
	/* what this site charges others to link (place ads) to it. Determined in third-price auction format */
	public int linkCost;
	
	/* list of site's WTP for ad on every other site on net. List indices correspond to Internet's. */
	public ArrayList<Integer> WTPs;
	
	/* lists of contracts this site has with other sites, both ones that it adverstises on and those that advertise on it */
	public ArrayList<Offer> placements; 
	public ArrayList<Offer> sponsors;
	
	/* 3-Dimensional array that counts the number of each User permutation that passes through this site */
	public int[][][] traffic;
	
	/* these describe the demographics this site wants to attract, and the sort of content it hopes to attract them with */
	public User.Interests content;
	public boolean gender;
	public User.AgeGroup ageGroup;
	public User.Education eduLevel;
	
	/* arbitrary limits on the number of ads and sponsors a web site can have, for simplicity's sake */
	public static final int maxSponsors = 3;
	public static final int maxPlacements = 4;
	
	/* running counter of randomly generated sites */
	public static int newSites = 0;
	
	public static void main(String[] args) {

	}
	
	/**
	 * Constructor sets webs site's target demographics and content. 
	 * The other memeber variables are set to the equivalent null value.
	 * 
	 * @param domain
	 * @param content
	 * @param gender
	 * @param ageGroup
	 * @param eduLevel
	 */
	public Website(String domain, User.Interests content, boolean gender, User.AgeGroup ageGroup, User.Education eduLevel){
		this.domainName = domain;
		
		/* Internet class tracks total number of web sites for sake of assigning ID's */
		this.ID = Internet.siteCount;
		Internet.siteCount++;
		
		this.WTPs = new ArrayList<Integer>();
		this.linkCost = 0;
		this.placements = new ArrayList<Offer>();
		this.sponsors = new ArrayList<Offer>();
		
		/* number of genders, number of age groups, number of education levels*/
		this.traffic = new int[2][7][3];
		
		this.content = content;
		this.gender = gender;
		this.ageGroup = ageGroup;
		this.eduLevel = eduLevel;
	}
	
	/**
	 * Generates random User and takes User's demographics
	 * and top interests to make new site. Since randUser()
	 * already creates statistically probable demographics 
	 * and interests, web site can expect close correlation
	 * between target demographic and content
	 * 
	 * @return	randomly generated Website
	 */
	public static Website randSite(){
		User user = User.randUser();
		newSites++;
		String domain = "New Site " + newSites;
		return new Website(domain, user.interests[0], user.isMale, user.age, user.edu);
	}
	
	/* alters all of site's attributes without disrupting ID system */
	public void alter(String domain, User.Interests content, boolean gender, User.AgeGroup age, User.Education edu){
		this.domainName = domain;
		this.content = content;
		this.gender = gender;
		this.ageGroup = age;
		this.eduLevel = edu;
	}
	
	/* mutator function called in Website.makeOffers() */
	public void setCost(int thisCost){
		this.linkCost = thisCost;
	}
	
	/* connect sites after acceptable offer has been made */
	public void placeAd(Website other){
		if (this != other && !this.hasPlacedAd(other)){
			if (other.sponsors.size() == maxSponsors)
				other.dropSponsor();
			this.placements.add(this.offerFor(other));
			other.sponsors.add(other.offerFrom(this));
		}
		Collections.sort(this.placements);
		Collections.sort(other.sponsors);
	}
	
	/* drops ad from web site, presumably because a better one has been found */
	public void dropAd(){
		Offer dropped = this.placements.get(0);
		Website other = dropped.site;
		this.placements.remove(0);
		/* need to find the orginal offer*/
		Offer removed = null;
		for (Offer offer : other.sponsors){
			if (offer.site == this)
				removed = offer;
		}
		other.sponsors.remove(removed);
		Collections.sort(this.placements);
		Collections.sort(other.sponsors);
	}
	
	/* reverse of dropAd, clears Websites lowest paying sponsor */
	public void dropSponsor(){
		Offer dropped = this.sponsors.get(0);
		Website other = dropped.site;
		this.sponsors.remove(0);
		/* need to find original offer */
		Offer removed = null;
		for (Offer offer : other.placements){
			if (offer.site == this)
				removed = offer;
		}
		other.placements.remove(removed);
		Collections.sort(this.sponsors);
		Collections.sort(other.placements);
	}
	
	/* transforms visiting User's demographics into coordinates, updates traffic table */
	public void tabulate(User user){
		int gender = user.isMale ? 0 : 1;
		int age = DataTable.arrayIndex(User.AgeGroup.values(), user.age.toString());
		int edu = DataTable.arrayIndex(User.Education.values(), user.edu.toString());
		this.traffic[gender][age][edu]++;
	}
	
	/* returns all traffic figures to zero, called in Internet.reset */
	public void clearTraffic(){
		for (int i=0; i < 2; i++)
			for (int j=0; j < 7; j++)
				for (int k=0; k < 3; k++)
					this.traffic[i][j][k] = 0;
	}
	
	/* returns quasi-random integer based on how closely site matches User's interests */
	public int scoreInterest(User.Interests[] interests){
		/* random integer makes it so that same user won't always go to same site */
		int rand = DataTable.gen.nextInt(3);
		int score = rand;
		/* the farther this site's content is up the User's ranked list of interests, the higher its score */
		for (int i=0; i < 5; i++)
			if (interests[i] == this.content)
				score += (5-i);
		return score;
	}

	/**
	 * Returns integer representing willingness to pay for ad on other site based on that site's traffic.
	 * Note that this method assigns 1 point for matching two out of three categories, and 3 points for
	 * matching all three (by virtue of triple counting).
	 * 
	 * @param other	Website this site is considering placing an ad on.
	 * @return		integer expressing site's WTP for ad on other site
	 */
	public int WTP(Website other){
		int WTP = 0;
		int genderID = this.gender ? 0 : 1;
		int age = DataTable.arrayIndex(User.AgeGroup.values(), this.ageGroup.toString());
		int edu = DataTable.arrayIndex(User.Education.values(), this.eduLevel.toString());
		for (int i=0; i < 2; i++)
			WTP += other.traffic[i][age][edu];
		for (int i=0 ; i < 7; i++)
			WTP += other.traffic[genderID][i][edu];
		for (int i=0; i < 3; i++)
			WTP += other.traffic[genderID][age][i];
		return WTP;
	}
	
	/* site determines its WTP for ad on every site on the Internet, WTP for ad on self is always zero */
	public void shopAround(Internet WWW){
		this.WTPs.clear();
		for (int i=0; i < WWW.sites.size(); i++){
			if (WWW.sites.get(i) == this)
				this.WTPs.add(0);
			else
				this.WTPs.add(this.WTP(WWW.sites.get(i)));
		}
	}
	
	/* site looks at all other sites in Internet and places ad if seen as profitable */
	public void makeOffers(Internet WWW){
		for (int i=0; i < WWW.sites.size(); i++){
			Website other = WWW.sites.get(i);
			if (this != other && this.profitable(other) && this.acceptable(other)){ /* debug mode says this is failing when it shouldn't be */
				if (this.placements.size() == maxPlacements)
					this.dropAd();
				if (other.sponsors.size() == maxSponsors)
					other.dropSponsor();
				this.placeAd(other);
			}
		}
	}
	
	/* whether or not site has already placed ad on other */
	public boolean hasPlacedAd(Website other){
		for (Offer offer : this.placements){
			if (offer.site == other)
				return true;
		}
		return false;
	}
	
	/* whether or not site can gain from placing an ad on given site, default answer is yes */
	public boolean profitable(Website other){
		if (this.placements.isEmpty() || this.numPlacements() < maxPlacements)
			return true;
		int amount = this.WTPs.get(other.ID);
		return (amount > this.placements.get(0).amount) && this.numPlacements() == maxPlacements;
	}
	
	/* whether or not other site is willing to sell ad space to this site, default answer is yes */
	public boolean acceptable(Website other){
		if (other.sponsors.isEmpty() || other.sponsors.size() < maxSponsors)
			return true;
		return this.WTPs.get(other.ID) > other.sponsors.get(0).amount;
	}
	
	/* generates offer this site makes to site it wishes to advertise on */
	public Offer offerFor(Website other){
		/* if site's list of WTP's is empty, make offer of zero. This will only happen when initializing Internet */
		int amount = (this.WTPs.isEmpty()) ? 0 : this.WTPs.get(other.ID);
		return new Offer(other, amount);
	}
	
	public Offer offerFrom(Website other){
		/* if other site's list of WTP's is empty, accept offer of zero. This will only happen when initializing Internet or adding new sites between rounds */
		int amount = (other.WTPs.isEmpty()) ? 0 : other.WTPs.get(this.ID);
		return new Offer(other, amount);
	}
	
	/* print contents of Website, for testing purposes */
	public void printSite(Internet WWW){
		System.out.println("Name: " + this.domainName + "\tCategory: " + this.content.toString());
		String genderString = gender ? "Male" : "Female";
		System.out.println("Caters to " + genderString + " " + ageGroup.toString() + " " + eduLevel.toString());
		//printTraffic();
		//this.printWTPs(WWW);
		System.out.println("Number of placements: " + this.numPlacements());
		System.out.println("Number of sponsors: " + this.numSponsors());
	}
	
	public void printStatus(){
		System.out.println("Name: " + this.domainName + "\tCategory: " + this.content.toString());
		String genderString = gender ? "Male" : "Female";
		System.out.println("Caters to " + genderString + " " + ageGroup.toString() + " " + eduLevel.toString());
		System.out.println("Link Cost: " + this.linkCost);
		System.out.println("Traffic total: " + this.trafficTotal());
		System.out.println();
		this.printPlacements();
		System.out.println();
		this.printSponsors();
	}
	
	/* prints site's WTP for each other site on Internet, for testing purposes */
	public void printWTPs(Internet WWW){
		System.out.println(this.domainName + "'s WTPs:");
		for (int i=0; i < WWW.sites.size(); i++){
			System.out.println(WWW.sites.get(i).domainName + ": " + this.WTPs.get(i));
		}
	}
	
	/* prints all the sites this site has placed ads on and how much it paid them, for testing purposes */
	public void printPlacements(){
		System.out.println(this.domainName + "'s placements:");
		for (Offer offer : this.placements)
			System.out.println(offer);
	}
	
	/* prints all sites that have placed on this site and how they paid, for testing purposes */
	public void printSponsors(){
		System.out.println(this.domainName + "'s sponsors:");
		for (Offer offer : this.sponsors)
			System.out.println(offer);
	}
	
	/* iterates over site's traffic table, transforms coordinates into demographics, prints count if non zero. For testing purposes */
	public void printTraffic(){
		for (int i=0; i < 2; i++)
			for (int j=0; j < 7; j++)
				for (int k=0; k < 3; k++)
					if (traffic[i][j][k] != 0){
						String gender = i==0 ? "Male" : "Female";
						String age = User.AgeGroup.values()[j].toString();
						String edu = User.Education.values()[k].toString();
						System.out.println(gender + ' ' + age + ' ' + edu + ": " + traffic[i][j][k]);
					}	
	}
	
	/* prints total number of visits to website, for testing purposes */
	public int trafficTotal(){
		int total = 0;
		for (int i=0; i < 2; i++)
			for (int j=0; j < 7; j++)
				for (int k=0; k < 3; k++)
					total += this.traffic[i][j][k];
		return total;
	}
	
	/* returns the number of site's sponsors, for testing purposes */
	public int numSponsors(){
		return this.sponsors.size();
	}
	
	/* returns the number of site's placements, for testing purposes */
	public int numPlacements(){
		return this.placements.size();
	}
	
	/**
	 * Offer class represents contracts made between sites. 
	 * Each offer consists of a Website that the site is doing 
	 * business with and the amount that is being paid/received.
	 * Whether the offer is listed in the site's placements
	 * or sponsors list determines what end of the transaction the
	 * Website is on.
	 *
	 * Implements Comparable interface to sort offers and 
	 * allow sites to dynamically update sponsors and ad placements.
	 *
	 */
	public static class Offer implements Comparable<Offer>{
		public Website site;
		public int amount;
		
		public Offer(Website site, int profit){
			this.site = site;
			this.amount = profit;
		}
		
		/* allows sites to rank offers by profit margin */
		public int compareTo(Offer other){
			if (this.amount == other.amount)
				return 0;
			else if (this.amount < other.amount)
				return -1;
			else
				return 1;
		}
		
		/* for testing purposes */
		public String toString(){
			return this.site.domainName + " Amount: " + this.amount;
		}
	}
	
}
