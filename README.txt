CMSC 12200 Final Project by Andrew Christoforakis and Arin Schwartz

Instructions:
Assuming you have all our files in the same directory, just run Scraper.py
(should take a few seconds) then Internet.java. (It may have to be 
compiled first: javac Internet.java).

Running Scraper.py will give you the most up-to-date data to work with,
but the program will work perfectly fine with the 3/1/2012 data.txt.

The simulation should print a series of Website data for an
up-and-coming new site I<3Beiber.com, adjusting its ad placements and
sponsors every round. All the class variables in Internet.java can
be altered to make the simulation incorporate more sites, go on for 
longer, run more Users through the simulation, etc.

Also, in the case of a File input error, go to DataTable.java
and, right near the top, alter the String sourceFile from 
"src/data.txt" to "data.txt". This should have already been done.

List of files:

User.java: Defines users as set of demographics (age, gender, education)
and ranked list of interests (sports, blogging, gossip, etc.)

Website.java: Defines sites as linked list of nodes, each with
a particular interest that it caters to as well as a cost for new
sites that want to connect to it.

Internet.java: Defines the web as a list of interconnected sites,
simulates web-browsing by letting users wander between adjacent
nodes and tabulates traffic figures

DataTable.java: Used for holding all statistical constants like age and interests distributions. 
Reads data from file rather than store it as class constants. Keeps all tables,
file i/o, and random number generation under the hood.

data.txt: Data tables for age/education distribution and
interests among demographics. The Python component will write
data to here.

Scraper.py: Uses BeautifulSoup module to scrape all the relevant data
from quantcast.com and saves it to data.txt

Assumptions:

-Web sites can only show up to three different advertisements (links) on their web page.

-Sponsors can only place one ad on each site; no multiple placements.

-Web sites can place a maximum of four ads on other sites.

-Web sites only cater to one type of user (gender, age group, education level).

-Users have five ranked interests that determine how they'll move around the web.

-Every "round", all users visit a predetermined number of web sites. We're not concerned with how long
they spend on each site, only the sites they visit.

-A "round" consists of a run-through (a predetermined number of "steps" in which each user moves to a different site),
a tabulation phase where each site determines its WTP for every other site, and a negotiation phase
where sites establish links based on their costs and WTP's.

-Every "step", users have a fixed probability to jump to a completely random site. This is to prevent pooling among
the several most popular sites.

-Traffic figures from the previous round are public information. All sites will use these to project
their own user traffic based on possible ad placements.

-When reviewing traffic figures, sites place more importance on users who fit their target demographic 
perfectly (three out of three characteristics) than those who have only two characteristics. Users who
exhibit less than two characteristics aren't taken into account.

-Current web sites re-optimize their links every round. This means they could potentially drop ads or sponsors 
in response to previous rounds' traffic figures.

-Each web site looks at each others' traffic figures and calculates its willingness to pay (WTP) for
an ad on that site. Sites will allow the sites with the three highest WTP's to take out ads on it at the price
of the third highest WTP (think a third-price auction).

-Each site's WTP for an ad on itself will always be 0 (there will be no advertising on oneself).

-If a site's link-cost (the amount it charges other sites to advertise on it) is below a certain threshold, other web sites,
when calculating their WTP's, will increase their WTP for that site by 20. This keeps them from discriminating against new 
sites that will have no traffic when they're introduced.
