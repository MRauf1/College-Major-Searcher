College Major Searcher
By Rauf Makharov
----------
To execute, run the CollegeResearch.java file via a terminal.
javac CollegeResearch.java
java CollegeResearch

Or if you have an IDE, run CollegeResearch.java using that.
----------
Java program that searches through College Board's websites to find out which colleges offer certain majors and which ones do not.
Was developed to help out a friend.
Current version only supports master's degree majors. Plan on allowing the user to choose between master's/doctoral/bachelor's/associates and maybe other degrees.
results.xlsx will store the results in an excel file.
The initial results.xlsx file is a testing file that contains colleges and their respective values on whether they have a Computer Science Master's degree and Mathematics Master's degree.
Unfortunately, due to some names not matching in the provided JSON file of this program with College Board's names, some of the colleges are missing. For example, College Board name: Harvard College; program's JSON file: Harvard University, so the program could not find it.