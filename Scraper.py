import urllib
from BeautifulSoup import BeautifulSoup

    
"""One site for each type of interest."""


sites = ['blogspot.com', 'facebook.com', 'reddit.com', 'chase.com', 'wsj.com', 'monster.com', 'kotaku.com', 'espn.com', 'pandora.com', 'imdb.com', 'cnn.com', 'perezhilton.com', 'foxnews.com', 'yelp.com', 'jstor.com', 'ehow.com', 'macys.com', 'bestbuy.com', 'ebay.com']



def getData(URL):
    source = urllib.urlopen("http://www.quantcast.com/www.%s" % URL)
    sourceString = source.read()
    soup = BeautifulSoup(sourceString)
    return soup
    
    
def getDemographicsBlock(soup):
    for element in soup.findAll('h4'):
        for string in element.contents:
            if 'US Demographics' in string:
                demographics = element.parent
                return demographics

"""This takes the block of demographic code and a demographic characteristic, and returns the block of code pertaining to that characteristic."""

def getInfoBlocks(demographics, characteristic):
    infoBlocks = []
    for element in demographics.findAll('tr'):
        if element.has_key('class'):
            if characteristic in element['class']:
                infoBlocks.append(element)
    return infoBlocks

"""Assembles integer list of traffic stats for a demographic characteristic. List is based on the information in the block passed to the function."""
def getStats(infoBlocks):
    stats = []
    for tr in infoBlocks:
        for td in tr.findAll('td'):
            if td.has_key('class'):
                if 'digit' in td['class']:
                    stats.append(td.string.strip())
    return [int(x) for x in stats]
   
"""Scraping/helper functions end here."""   


"""This is the primary gender information fucntion. It uses the ones above as helpers to make a list of gender statistics from a URL."""    
def genderTraffic(URL):
    soup = getData(URL)
    return getStats(getInfoBlocks(getDemographicsBlock(soup), 'GENDER'))

"""This is the primary age traffic function."""
def ageTraffic(URL):
    soup = getData(URL)
    return getStats(getInfoBlocks(getDemographicsBlock(soup), 'AGE'))

"""Guess what this one is."""
def educationTraffic(URL):
    soup = getData(URL)
    return getStats(getInfoBlocks(getDemographicsBlock(soup), 'EDUCATION'))

"""Assembles list-of-lists containing all relevant demographic information for a website. Don't know if we're gonna need this one. Dealing with it is inefficient."""
def siteTraffic(URL):
    return [genderTraffic(URL), ageTraffic(URL), educationTraffic(URL)]
    





"""Makes a list-of-lists profiling one characteristic eg. age for every type of interest."""
def byCharacteristic(sites, characteristic):
    characteristics = ('AGE', 'GENDER', 'EDUCATION')
    if characteristic not in characteristics:
        print "ERROR: WRONG CHARACTERISTIC"
        return
    if characteristic == 'GENDER':
        male = []
        female = []
        for x in sites:
            traffic = genderTraffic(x)
            male.append(traffic[0])
            female.append(traffic[1])
        genderInterests = [male, female]
        return genderInterests
    if characteristic == 'AGE':
        TEEN = []
        YOUNG_ADULT = []
        ADULT = []
        MIDDLE_AGED = []
        OLDER = []
        OLDER_STILL = []
        RETIRED = []
        for x in sites:
            traffic = ageTraffic(x)
            TEEN.append(traffic[0])
            YOUNG_ADULT.append(traffic[1])
            ADULT.append(traffic[2])
            MIDDLE_AGED.append(traffic[3])
            OLDER.append(traffic[4])
            OLDER_STILL.append(traffic[5])
            RETIRED.append(traffic[6])
        ageInterests = [TEEN, YOUNG_ADULT, ADULT, MIDDLE_AGED, OLDER, OLDER_STILL, RETIRED]
        return ageInterests
    if characteristic == 'EDUCATION':
        NO_COLLEGE = []
        COLLEGE = []
        GRAD_STUDENT = []
        for x in sites:
            traffic = educationTraffic(x)
            NO_COLLEGE.append(traffic[0])
            COLLEGE.append(traffic[1])
            GRAD_STUDENT.append(traffic[2])
        educationInterests = [NO_COLLEGE, COLLEGE, GRAD_STUDENT]
        return educationInterests

"""Takes a file name and a list of websites to scrape. Constructs a text file with one block each for gender, age, and education, headed by the dimensions of each matrix in the first two rows. Columns represent categories of websites, and entries in the columns are that bucket's representation for that category. Our data."""
def scrapeItUp(filename, sites):
    f = open(filename, 'w')
    genderInterests = byCharacteristic(sites, 'GENDER')
    genRows = str(len(genderInterests))
    genCols = str(len(genderInterests[0]))
    
    ageInterests = byCharacteristic(sites, 'AGE')
    ageRows = str(len(ageInterests))
    ageCols = str(len(ageInterests[0]))
    
    educationInterests = byCharacteristic(sites, 'EDUCATION')
    eduRows = str(len(educationInterests))
    eduCols = str(len(educationInterests[0]))
    
    """Data file written to specifications. Forst two write commands are CONSTNAT cumulative distributions that we just like to have at the top of every data file."""
    f.write("1\n7\n.214, .355, .489, .638, .811, .913, 1.0" +"\n"*2)
    
    f.write("7\n4\n.98,1.0,1.0,1.0\n.302,.664,.956,1.0\n.395,.68,.905,1.0\n.35,.615,.83,1.0\n.42,.67,.98,1.0\n.41,.68,.865,1.0\n.545,.76,.895,1.0" + "\n"*3)
    
    f.write(genRows + '\n')
    f.write(genCols + '\n')
    for bucket in genderInterests:
        for interest in bucket:
            x = str(interest)
            f.write(x + ',' + ' ')
        f.write('\n')
    f.write('\n'*3)
    
    f.write(ageRows + '\n')
    f.write(ageCols + '\n')
    for bucket in ageInterests:
        for interest in bucket:
            x = str(interest)
            f.write(x + ',' + ' ')
        f.write('\n')
    f.write('\n'*3)
    
    f.write(eduRows + '\n')
    f.write(eduCols + '\n')
    for bucket in educationInterests:
        for interest in bucket:
            x = str(interest)
            f.write(x + ',' + ' ')
        f.write('\n')
    f.close()

            

scrapeItUp('data.txt', sites)


