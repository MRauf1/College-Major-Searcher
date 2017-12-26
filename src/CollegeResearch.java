import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.gson.Gson;

public class CollegeResearch {

	public static void main(String[] args) throws IOException {
		
		//Fetch the JSON file with the list of colleges
		Gson gson = new Gson();
		College[] colleges = gson.fromJson(new FileReader("collegeList/us_institutions.json"), College[].class);
		
		//Retrieve the map with all the majors and print the list
		Map<Integer, String> availableMajors = getAvailableMajors(); 
		System.out.println(availableMajors.toString());
		
		//Initializing the necessary variables
		Scanner scan = new Scanner(System.in);
		int majorIndex;
		List<String> selectedMajors = new ArrayList<String>();
		
		System.out.println("Select the index of any of these majors. Once you're done selecting the ones you need, enter -1 to continue.");
		
		//Add all the selected majors to the list. Break the loop once -1 is entered.
		do {
			majorIndex = scan.nextInt();
			if(majorIndex == -1) {
				break;
			} else {
				selectedMajors.add(availableMajors.get(majorIndex));
			}
		} while(true);
		
		//Close the scanner
		scan.close();
		
		//Map containing the data about colleges and majors
		//Format example: {Harvard : [{Mathematics : true}, {Comp. Sci. : true}]}
		Map<String, List<Map<String, Boolean>>> results = new HashMap<String, List<Map<String, Boolean>>>();
		
		//Length of the array of colleges
		int collegesLength = colleges.length;
		
		//Set up the web driver
		System.setProperty("webdriver.chrome.driver", "C:\\Users\\m_rau\\Downloads\\chromedriver_win32\\chromedriver.exe");
		WebDriver driver = new ChromeDriver();
		
		//Loop through all the colleges in the JSON file and execute the following
		for (int i = 0; i < collegesLength; i++) {
			
			//Select and format a college name
			String collegeName =  colleges[i].toString().toLowerCase().replace(" ", "-");
			
			//Connect to the web page
			driver.get("https://bigfuture.collegeboard.org/college-university-search/" + collegeName);
			
			boolean elementExists;
			
			//Wait until the page loaded
			WebDriverWait wait = new WebDriverWait(driver, 10);
			wait.until(webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));
			
			elementExists = doesElementExist(driver, "cpProfile_tabs_majorsAndLearning_anchor");
			
			if(elementExists) {
				//Click on the links to get to the required part of the web page
				((JavascriptExecutor) driver).executeScript(
						"document.getElementById('cpProfile_tabs_majorsAndLearning_anchor').children[0].click();");
			} else {
				continue;
			}
			
			
			
			wait.until(webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));
			
			elementExists = doesElementExist(driver, "cpProfile_tabs_majorsAndLearning_allMajors_anchor");
			
			if(elementExists) {
				//Click on the links to get to the required part of the web page
				((JavascriptExecutor) driver).executeScript(
						"document.getElementById('cpProfile_tabs_majorsAndLearning_allMajors_anchor').children[0].click();");
			} else {
				driver.close();
				continue;
			}
			
			//Fetch all the tables
			@SuppressWarnings("unchecked")
			List<WebElement> tables = (List<WebElement>) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('majorsOfferedTable filterTbl hasStripes addBottom20');");
			
			//Index
			int j = 0;
			
			//List containing all the major data about the current college
			List<Map<String, Boolean>> tempList = new ArrayList<Map<String, Boolean>>();
			
			//Loop through each table
			for(String selectedMajor : selectedMajors) {
				
				//Boolean representing whether the selected major has been found or not
				boolean majorFound = false;
				
				//Loop through each selected major
				for(WebElement element : tables) {
					
					//The text inside the table
					String text = element.getText();
					
					//Create a temporary map with data about whether a major is present or not
					Map<String, Boolean> tempMap = new HashMap<String, Boolean>();
					
					//If there is a table with the selected major, execute the following
					if(text.contains(selectedMajor)) {
						
						majorFound = true;
						
						int index = j;
						
						//Go through the rows of the current table and see whether the selected major is offered or not
						Boolean hasMasters =  (Boolean) ((JavascriptExecutor) driver).executeScript(
								"var tables = document.getElementsByClassName('majorsOfferedTable filterTbl hasStripes addBottom20');"
								+ "var table = tables[" + index + "].children[1];"
								+ "var tableLength = table.rows.length;"
								+ "for(var i = 0; i < tableLength; i++) {"
									+ "var element = table.children[i].children[4];"
									+ "if(element.className == 'offered') {"
										+ "return true;"
									+ "}"
								+ "}"
								+ "return false;");
						
						//Put the value inside the map
						tempMap.put(selectedMajor, hasMasters);
						
						//Add the temporary map to the temporary list containing info about all the majors of the current college
						tempList.add(tempMap);
						
					} else if(element.equals(tables.get(tables.size() - 1)) && majorFound == false) {
						
						//If the selected major doesn't exist on the webpage, put false in for it
						
						//Put the value inside the map
						tempMap.put(selectedMajor, false);
						
						//Add the temporary map to the temporary list containing info about all the majors of the current college
						tempList.add(tempMap);
						
					}
					
				}
				
				j++;
				
			}
			
			//Add the data about the current college to the full map of all colleges
			results.put(collegeName, tempList);
			
			//Show progress
			System.out.println(i + "/" + collegesLength);
			System.out.println(results.toString());
			
		}
		
		//Exit the current window
		driver.close();
		
		//Save the results with all the data in a .xlsx file
		saveResults(results);
		
	}
	
	//Check whether an entered element exists on the web page
	public static boolean doesElementExist(WebDriver driver, String id) {
		
		//Execute the following JavaScript which checks whether the element exists
		boolean elementExists = (boolean) ((JavascriptExecutor) driver).executeScript(
				"if(document.getElementById('" + id + "') == null) {"
					+ "return false;"
				+ "} else {"
					+ "return true;"
				+ "}");
		
		return elementExists;
		
	}
	
	//Creates a map with all the present majors on the CollegeBoard website
	public static Map<Integer, String> getAvailableMajors() {
		
		Map<Integer, String> availableMajors = new HashMap<Integer, String>();
		availableMajors.put(0, "Architecture & Related Programs");
		availableMajors.put(1, "Area, Ethnic, Cultural, & Gender Studies");
		availableMajors.put(2, "Biological & Biomedical Sciences");
		availableMajors.put(3, "Business, Management, Marketing, & Related Support");
		availableMajors.put(4, "Communication, Journalism & Related Programs");
		availableMajors.put(5, "Computer & Information Sciences, Support Services");
		availableMajors.put(6, "Education");
		availableMajors.put(7, "Engineering");
		availableMajors.put(8, "Engineering Technologies/Technicians");
		availableMajors.put(9, "English Language, Literature & Letters");
		availableMajors.put(10, "Foreign Language, Literatures & Linguistics");
		availableMajors.put(11, "Health Professions & Related Clinical Sciences");
		availableMajors.put(12, "History");
		availableMajors.put(13, "Legal Professions & Studies");
		availableMajors.put(14, "Liberal Arts & Sciences, Gen Studies & Humanities");
		availableMajors.put(15, "Mathematics & Statistics");
		availableMajors.put(16, "Multi & Interdisciplinary Studies");
		availableMajors.put(17, "Natural Resources & Conservation");
		availableMajors.put(18, "Philosophy & Religious Studies");
		availableMajors.put(19, "Physical Sciences");
		availableMajors.put(20, "Psychology");
		availableMajors.put(21, "Public Administration & Social Services");
		availableMajors.put(22, "Social Sciences");
		availableMajors.put(23, "Theology & Religious Vocations");
		availableMajors.put(24, "Visual & Performing Arts");
		
		return availableMajors;
		
	}
	
	//Write all the results into a .xlsx file
	public static void saveResults(Map<String, List<Map<String, Boolean>>> results) throws IOException {
		
		//Create the workbook and the sheet
		XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("College Master's Majors");
        
        //Initialize counters for the row and column
        int rowNumber = 0;
        int columnNumber = 0;
        
        //Loop through all the colleges
        for(String collegeName : results.keySet()) {
        	
        	//Create the row
        	Row row = sheet.createRow(rowNumber);
        	
        	//Write the college name
        	populateCell(row, columnNumber, collegeName);
        	columnNumber += 2;
        	
        	//Length of the list containing majors of the current college
        	int collegeMajorsSize = results.get(collegeName).size();
        	
        	//Loop through the list with the majors
        	for(int i = 0; i < collegeMajorsSize; i++) {
        		
        		//Get the map inside the list at index i
        		Map<String, Boolean> collegeMajors = results.get(collegeName).get(i);
        		
        		//Fill in the cell with the college major name and the boolean of whether it offers it or not
        		for(String major : collegeMajors.keySet()) {
        			
        			populateCell(row, columnNumber, major);
        			columnNumber++;
        			populateCell(row, columnNumber, collegeMajors.get(major).toString());
        			columnNumber += 2;
        			
        		}
        		
        	}
        	
        	rowNumber++;
        	columnNumber = 0;
        	
        }
        
        //Write the data into the file and close the writer
        FileOutputStream outputStream = new FileOutputStream("results.xlsx");
        workbook.write(outputStream);
        workbook.close();
		
	}
	
	//Populate a single cell in the given row and column with the given value
	public static void populateCell(Row row, int columnNumber, String value) {
		
		Cell cell = row.createCell(columnNumber);
    	cell.setCellValue(value);
		
	}
	
}
