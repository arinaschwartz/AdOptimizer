/**
 * @author Andrew Christoforakis, Arin Schwartz
 * 
 * Represents the entire Internet as a collection of interconnected
 * Websites. The initial state of the Internet will be somewhat 
 * arbitrary. Only after randomly generated Users have moved around 
 * in it can Websites compute traffic figures and make offers. Functions
 * break down each "round" into separate phases.
 */

import java.util.*;

public class Internet {

	/* constants used for the runThrough function */
	public static final int numUsers = 75;
	public static final int numSteps = 20;
	
	/* how many websites the Internet will start with */
	public static final int initSize = 8;
	/* how large we'll allow the Internet to become for purpose of simulation */
	public static final int maxSize = 50;
	/* how many new sites will be added each round */
	public static final int startups = 2;
	/* how much of a boost new sites are given in bidding for ad space*/
	public static final int handicap = 15;
	/* collection of Websites */
	public ArrayList<Website> sites;
	
	/* useful for tracking Internet size, incremented when Website is created */
	public static int siteCount;
	
	/* Default constructor makes empty list of Websites */
	public Internet(){
		this.sites = new ArrayList<Website>();
	}
	
	/* initializes Internet as collection of random unlinked websites */
	public static Internet randInternet(){
		Internet WWW = new Internet();
		for (int i=0; i < initSize; i++){
			Website site = Website.randSite();
			WWW.addSite(site);
		}
		return WWW;
	}
	
	/* site added to Internet, site ID will correspond to its index in ArrayList */
	public void addSite(Website site){
		this.sites.add(site);
	}
	
	/* returns random site from the Internet, called in User.randJump() */
	public Website randSite(){
		Random gen = new Random();
		Website newSite = this.sites.get(gen.nextInt(this.sites.size()));
		return newSite;
	}
	
	/* for testing purposes */
	public void printInternet(){
		for (Website site : sites){
			site.printSite(this);
			System.out.println();
		}	
	}
	
	/* resets all ad placements for new round */
	public void reset(){
		for (Website site : sites){
			site.WTPs.clear();
			site.clearTraffic();
		}
	}
	
	/* simulates run through in which random users are allowed to roam through Internet */
	public void runThrough(){
		for (int i=0; i < numUsers; i++){
			User user = User.randUser();
			user.setSite(randSite());
			for (int j=0; j < numSteps; j++)
				user.hopSite(this);
		}
	}
	
	/* lets all Web sites calculate their WTP's for other sites */
	public void tabulationPhase(){
		for (Website site : this.sites)
			site.shopAround(this);
	}
	
	/* lets all sites examine other sites and establish links based on their WTP's and link costs */
	public void offerPhase(){
		for (Website site : this.sites)
			site.makeOffers(this);
		for (Website site : this.sites){
			if (!site.sponsors.isEmpty())
				site.setCost(site.sponsors.get(0).amount);
			else
				site.setCost(0);
		}	
	}
	
	/* lets new sites enter in and establish links */
	public void newBusinesses(){
		for (int i=0; i < startups; i++){
			Website newSite = Website.randSite();
			ArrayList<Website.Offer> bids = new ArrayList<Website.Offer>();
			/* give new sites handicap in bidding for ads*/
			for (Website site : this.sites){
				bids.add(new Website.Offer(site, newSite.WTP(site) + handicap));
			}
			/* sort offers by amount (since Offer class implements Comparable interface) */
			Collections.sort(bids);
			Website thirdBest = bids.get(bids.size()-3).site;
			Website fourthBest = bids.get(bids.size()-4).site;
			/* give new sites ads on their 3rd and 4th best picks. The amounts they pay are set to zero,
			 * meaning they'll likely be bumped off next round. This means they have one round to attract
			 * traffic and receive offers from other sites*/
			newSite.placeAd(thirdBest);
			newSite.placeAd(fourthBest);
			/* new sites have no sponsors, but at most two placements */
			this.addSite(newSite);
		}
	}
	
	/* conducts entire round */
	public void round(){
		/* initialize all sites' traffic and WTP's to zero */
		this.reset();
		/* let some randomly generated Users run through Internet */
		this.runThrough();
		/* sites look at other sites' traffic and determine their WTP's */
		this.tabulationPhase();
		/* sites systematically begin to make offers and place ads based on their WTP's */
		this.offerPhase();
		/* create news sites and let them determine where they set up */
		this.newBusinesses();
	}
	
	/* make a custom Website and monitor its progress */
	public static void journey(){
		Internet WWW = randInternet();
		WWW.sites.get(0).alter("I<3Beiber.com", User.Interests.MUSIC, false, User.AgeGroup.TEEN, User.Education.NO_COLLEGE);
		int round = 0;
		while (WWW.sites.size() < maxSize){
			round++;
			System.out.println("Round # " + round);
			WWW.round();
			WWW.sites.get(0).printStatus();
			System.out.println();
		}
	}
	
	public static void main(String[] args) {	
		journey();
	}
}

