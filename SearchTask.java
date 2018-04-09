/**
 * Implements a non-multithreaded search mechanism in order to find a pattern in the list of files
 * @author Hussein Al Osman
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class SearchTask extends Thread {
	
	protected static ThreadGroup tg = new ThreadGroup("Threads");
	protected static Thread[] threads = new Thread[4];
	private SearchJob [] searchJobs;
	private String pattern;
	private KMP kmp;
	int currIndex = 0;
	int finalIndex = 3;
	int j = 0;
	/**
	 * Constructor accepting an array of search jobs
	 * @param searchJobs an array of search jobs
	 * @param pattern you are looking for
	 */
	public SearchTask (SearchJob [] searchJobs, String pattern){
		
		this.searchJobs = searchJobs;
		this.pattern = pattern;
		kmp = new KMP();
	}
	
	/**
	 * Constructor accepting a single search job
	 * @param searchJob a single search job
	 * @param pattern you are looking for
	 */
	public SearchTask (SearchJob searchJob, String pattern){
		
		this.searchJobs = new SearchJob [1];
		searchJobs[0] = searchJob;
		
		this.pattern = pattern;
		
		kmp = new KMP();
	}
	
	//Another constructor taking a TaskGroup as argument.
	// Basically same as other constructors
	public SearchTask(ThreadGroup group, SearchJob searchJob, String pattern){
		super(tg, "Thread"); // Need to add the thread to its parent group
		this.searchJobs = new SearchJob [1];
		searchJobs[0] = searchJob;
		this.pattern = pattern;
		kmp = new KMP();
	}
	
	public void run(){
		this.runSearchPart2();
	}
	
	public void runSearchPart2(){
		for (SearchJob job: searchJobs){
			try {
				FileReader fr = new FileReader(job.getFile());
				BufferedReader br = new BufferedReader(fr);
				
				String line;
				int counter = 0;
			
				while((line=br.readLine()) != null){
					counter++;
					int pos= kmp.search(line, pattern);

					if (pos > 0)
						foundPattern(job.getName(), counter);
				}
				br.close();
				throw new InterruptedException();

			} catch (IOException e) {
				e.printStackTrace();
			}
			catch (InterruptedException e){
				this.interrupt();
				return;
			}

		}
	}
	
	
	/**
	 * Run the search by going through all the files one line at a time and looking for the pattern
	 */
	public void runSearch(){
		
		// populating array with threads
		for (int i = currIndex; i < finalIndex; i++) {
			threads[i] = new Thread(tg, "Worker" + Integer.toString(i));
			threads[i].setDaemon(true);
		}
		
		for (j = currIndex; j<finalIndex; j++){ // Go through the list of files
			
			//for each job in the array, create a thread
			Thread thread = new Thread(new Runnable() {
					public void run() {
						try {
							FileReader fr = new FileReader(searchJobs[j].getFile()); 
							BufferedReader br = new BufferedReader(fr); 
				
							String line; 
							int counter = 0;
				
							// Read each file one line at a time
							while((line=br.readLine()) != null){
								counter++; // keep track of the line number
										
					 		   // Perform the search on that line
								int pos= kmp.search(line, pattern);
						
								if (pos > 0) { //if the pattern is found, print it
									foundPattern(searchJobs[j].getName(), counter);
								}
							}
				
				
						// Close the buffered reader resource
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}	
				}
			});
			
			thread.start();
				
				
		}	
		
	}
	
	/**
	 * In the current implementation, this method only prints a message to the console whenever the pattern
	 * is found in a file
	 * @param fileName name of the file where the pattern was found
	 * @param lineNumber number of the line where the pattern was found
	 */
	private void foundPattern (String fileName, int lineNumber){
		System.out.println("Sequence found in: "+fileName+" at line: "+lineNumber);
	}

}
