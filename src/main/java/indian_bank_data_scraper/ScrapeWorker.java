package indian_bank_data_scraper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ScrapeWorker implements Runnable {

	public static final String BASEURL = "https://www.bankbazaar.com";
	public static final String BANKLIST ="https://www.bankbazaar.com/ifsc-code.html"; 
	public static final String BASEIFSC = "https://www.bankbazaar.com/ifsc-code/"; 
	
	public static int totalDistricts = 0;
	Semaphore sem = new Semaphore(1);
	String threadName;
	List<String> bankList = new ArrayList<String>();
	int districtCount = 0;
	String dataString = "";
	StringBuilder sb = new StringBuilder();
		
	public ScrapeWorker(Semaphore sem, String name) {
		this.sem = sem;
		this.threadName = name;
	}
	
	public void run() {
		// TODO Auto-generated method stub
		System.out.println(this.threadName + ": started scraping data!");
		Document doc = null;
		List<String> stateUrls = null;
		List<String> districtUrls = null;
		List<String> branchUrls = null;
		for (String bankUrl : bankList) { // iterate for number of banks
			try {
				doc = Jsoup.connect(bankUrl).timeout(50000).get();
				Elements elements = doc.select("div.ifsc-list > ul > li > a[href]");
				stateUrls = new ArrayList<String>();
				for (Element element : elements) {
					if(element.toString().contains("/ifsc-code/")) {
						stateUrls.add(BASEURL + element.attr("href"));
					}
				}
			}catch(Exception e) {
				System.out.println("Exception at: " + bankUrl);
			}
			
			for (String stateUrl : stateUrls) {
				try {
					doc = Jsoup.connect(stateUrl).timeout(10000).get();
					Elements elements = doc.select("div.ifsc-list > ul > li > a[href]");
					districtUrls = new ArrayList<String>();
					for (Element element : elements) {
						if(element.toString().contains("/ifsc-code/")) {
							districtUrls.add(BASEURL + element.attr("href"));
						}
					}
				}catch(Exception e) {
					System.out.println("Exception at: " + stateUrl);
				}
			}
			
			String temp = "";

			for (String districtUrl : districtUrls) {
				try {
					doc = Jsoup.connect(districtUrl).timeout(50000).get();
					Elements elements = doc.select("div.ifsc-list > ul > li > a[href]");
					branchUrls = new ArrayList<String>();
					for (Element element : elements) {
						if(element.toString().contains("/ifsc-code/")) {
							String branchUrl = BASEURL + element.attr("href");
							try {
								doc = Jsoup.connect(branchUrl).timeout(50000).get();
								Elements detailsElems = doc.select("table.tabdetails > tbody > tr");
								for(Element detailElement : detailsElems) { // writing details of one branch
									Elements tds = detailElement.select("td");
									temp = tds.get(1).text();
									if(temp.isEmpty() || temp == null) {
										dataString = dataString + "N/A" + "|";
									}else {
										dataString = dataString + temp + "|";
									}
								}	
								dataString += "\n";
							}catch(Exception e) {
								System.out.println("Exception at: " + branchUrl);
							}
						}
					}
				}catch(Exception e) {
					System.out.println("Exception at: " + districtUrl);
				}
			}
			districtCount++;
			
			if(districtCount % 3 == 0) {
				try {
					sem.acquire(); // acquire lock before writing the temporary output to the file
					ScrapeMaster.bw.write(dataString.replace("(used for RTGS and NEFT transactions)", "").replace(" |", "|").trim());
					ScrapeMaster.bw.flush();
					dataString = "";
					this.totalDistricts += 3;
					System.out.println(this.threadName + " wrote: " + this.districtCount + " districts to file!");
					System.out.println("total districts written: " + totalDistricts);
					sem.release(); // release lock after writing the output to the file
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}