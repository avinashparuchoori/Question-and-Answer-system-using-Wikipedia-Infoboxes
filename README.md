Question-and-Answer-system-using-Wikipedia-Infoboxes
====================================================

This application uses Wikipedia dump to parse and extract infoboxes and use that data as input for Solr and Stanford NLP to build a question answer system
relation_extraction --- extracts infoboxes and categories from main wikipedia dump
DataExtractor --- extracts indexable xml documents from processed dump and schema for it
Infobox Parser ---- processes infobox to create indexable xml document
relationDocCreator ---- creates indexable relations document
relationsExtractor ---- generates a hashmap for all the word synonyms of relations
rest of the classes support these operations

Place raw dump in the project folder the name by default points to "Wikidump_1600.xml". This has to be changed if any other file is being given as input. relation_extraction.java line number 70

First process the raw dump using relation_extraction. This will create a parsed dump that has only infoboxes, categories, page titles and page ids for pages with infobox type "organizations", "film", "musical artist". File Name created will be "TestInfoboxDump.xml" this would be taken as input by DataExtractor.java

Run the Data Extractor this will create an indexable xml file with file name "InputDataDocumentFormat.xml" and a schema for this "InputSchema.xml" and a data config file "InputDataConfig.xml" if we choose to use a data import handler which is the indexable with the given schema. It also creates a properties file named "relations.properties" which is later used as a hash map to extract all the key words pointing to these relations using wordnet

Run Question Parser to create the probability of all the question answer types using a TREC collection of 5500 questions. Input filename is "questionlist" and the output file is "questionTypeProbability.properties" which can be loaded and used as a hashmap.

List of all questions can be found at
http://cogcomp.cs.illinois.edu/Data/QA/QC/train_5500.label

Add the following fields to your schema after this to index this document

<field name="fieldid" type="text_general_custom" indexed="true" stored="true" multiValued="false"/>
<field name="relations" type="text_general_custom_2" indexed="true" stored="true" multiValued="false"/>	
<field name="entity" type="text_general" indexed="true" stored="true" multiValued="false"/>
<field name="count" type="int" indexed="true" stored="true" multiValued="false"/>

Run the relationsExtractor to create a file called expandedRelations.properties which contains all the expanded relations that can be loaded as a hash map in the search request handler of solr.

For this step we need to map all the desired fields with respective Mappings in a file called "Mappings To Top 150 Fields.properties". We have included it for reference.

Run the relationDocCreator which creates the indexable relations document that contains the expanded key words pointing to all the fields we have and indexable with solr.

PS: All the files will be created in the project folder and are currently mapped to the filenames in the way they are supposed to be created. Any change to a particular filename should be reflected in the code that uses this file.

All the files for Wikidump_1600.xml are included for reference.


