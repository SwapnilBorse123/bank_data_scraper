/**
@author imcoolswap
@date 4/11/2017
*/

package indian_bank_data_scraper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ScrapeMaster {
	
	static Semaphore sem = new Semaphore(1);
	
	public static final String BASEURL = "https://www.bankbazaar.com";
	public static final String BANKLIST ="https://www.bankbazaar.com/ifsc-code.html"; 
	public static final String BASEIFSC = "https://www.bankbazaar.com/ifsc-code/"; 
	
	public static int ASSIGNURL = 0;
	
	static BufferedWriter bw = null;
	static FileWriter fw = null;
	static File f = null;
	
	public static void main(String []args) throws IOException {
		
		f = new File("./src/main/resources/bank_ifsc_data.csv");
		fw = new FileWriter(f);
		bw = new BufferedWriter(fw);
		
		
		String url = "https://www.bankbazaar.com/ifsc-code.html";
		Document doc = Jsoup.connect(url).timeout(20000).get();
		Elements elements = doc.select("div.ifsc-list > ul > li > a[href]");
		List<String> bankUrls = new ArrayList<String>();
		for (Element element : elements) {
			if(element.toString().contains("/ifsc-code/")) {
				bankUrls.add(BASEURL + element.attr("href"));
			}
		}
		System.out.println("total: " + bankUrls.size());
		
		ScrapeWorker worker1 = new ScrapeWorker(sem, "worker1");
		worker1.bankList = bankUrls.subList(0, 100);
		
		ScrapeWorker worker2 = new ScrapeWorker(sem, "worker2");
		worker2.bankList = bankUrls.subList(100, 193);
		
		/*ScrapeWorker worker3 = new ScrapeWorker(sem, "worker3");
		worker3.bankList = bankUrls.subList(40, 60);
		
		ScrapeWorker worker4 = new ScrapeWorker(sem, "worker4");
		worker4.bankList = bankUrls.subList(60, 80);
		
		ScrapeWorker worker5 = new ScrapeWorker(sem, "worker5");
		worker5.bankList = bankUrls.subList(80, 100);
		
		ScrapeWorker worker6 = new ScrapeWorker(sem, "worker6");
		worker6.bankList = bankUrls.subList(100, 140);
		
		ScrapeWorker worker7 = new ScrapeWorker(sem, "worker7");
		worker7.bankList = bankUrls.subList(140, 170);
		
		ScrapeWorker worker8 = new ScrapeWorker(sem, "worker8");
		worker8.bankList = bankUrls.subList(170, 193);*/
		
		Thread t1 = new Thread(worker1);
		t1.start();
		
		Thread t2 = new Thread(worker2);
		t2.start();
		
		/*Thread t3 = new Thread(worker3);
		t3.start();
		
		Thread t4 = new Thread(worker4);
		t4.start();
		
		Thread t5 = new Thread(worker5);
		t5.start();
		
		Thread t6 = new Thread(worker6);
		t6.start();
		
		Thread t7 = new Thread(worker7);
		t7.start();
		
		Thread t8 = new Thread(worker8);
		t8.start();*/
		
		try{
			t1.join();
			t2.join();
			/*t3.join();
			t4.join();
			t5.join();
			t6.join();
			t7.join();
			t8.join();*/
		}catch(Exception e){
			System.out.println("Exception in wait.");
		}		
	}
}