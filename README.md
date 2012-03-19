# Dictionary Extensions Plugin #

This document describes the functionality provided by the dictionary extensions plugin.

See the **Deployit Reference Manual** for background information on Deployit and deployment concepts.

# Overview #

The dictionary-extensions-plugin is a Deployit plugin that extends the udm.Dictionary.

##Features##

* ext.HierarchicalDictionary

# Requirements #

* **Deployit requirements**
	* **Deployit**: version 3.7+
	* **Other Deployit Plugins**: None

# Usage #

##  HierarchicalDictionary
This dictionary contains not only entries, but also other dictionaries. If an entry is not found in the entries, it will be searched in the first dictionaires.
if the entry is still not found, the entry will be search the next in the list.. until the end of the list.

entries > dictionaries[0] >  dictionaries[1] > ......  > dictionaries[n]


