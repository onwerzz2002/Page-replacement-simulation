import java.io.*;
import java.util.*;

public class Application {
    public static void main(String[] args) throws IOException{
    	
    	int num_pf;                                 // number of physical frames
    	
    	// this is to read input.txt
    	File temp=new File("FIFO_INPUT_16.txt");      // input file: FIFO_INPUT_16.txt/LRU_INPUT_16.txt/CLOCK_INPUT_16.txt
    	                                          // FIFO_INPUT_256.txt/LRU_INPUT_256.txt/CLOCK_INPUT_256, please change accordingly ^_^
		Scanner file=new Scanner(temp);           // the last number 16 or 256 means the frame size in that test case
    	
		while(file.hasNextLine()){
			// to declare number of physical frames
	    	for(int i=0;i<4;i++){
	    		String Number_of_page_frames=file.next();  
	    	}                                            
	    	num_pf=file.nextInt();                                     // to read Number_of_page_frames
	    	
	    	// to read which algorithm we will use
	    	for(int i=0;i<3;i++){
	    		String Page_replacement_algorithm=file.next();
	    	}
	    	String pr_algo=file.next().toUpperCase();                  // read algorithm
	    	
	    	//to create an inverted page table
	    	int[][] inverted_page_table=new int[num_pf][4];
	    	//to initialize inverted_page_tabel for the first use
	    	create_inverted_page_table(inverted_page_table);
	    	
	    	// to read <oper_k>/START/END/CLOCK_TICK,<vpn_k>
	    	ArrayList<String> oper=new ArrayList<String>();            // to read <oper_k>/START/END/CLOCK_TICK
	    	ArrayList<Integer> vpn=new ArrayList<Integer>();           // to read <vpn_k>
	    	
	    	String START=file.next();                                  // to read START
	    	oper.add(file.next());                                     // start reading <oper_k>/START/END/CLOCK_TICK
	    	vpn.add(file.nextInt());                                   // start reading <vpn_k>
	    	while(!oper.get(oper.size()-1).equals("END")){             // when operation is END, the reading from input file ends.
	    		oper.add(file.next());
	    		
	    		if(oper.get(oper.size()-1).equals("END"))
	    			break;
	    		if(oper.get(oper.size()-1).equals("CLOCK_TICK"))
	    			continue;
	        	vpn.add(file.nextInt());
	        	
	    	}
	    	
	    	
	    	if(pr_algo.equals("FIFO")){                                // run FIFO algorithm
	    		FIFO(inverted_page_table,oper,vpn);
	    	}
	    	else if(pr_algo.equals("LRU")){                            // run LRU algorithm
	    		LRU(inverted_page_table,oper,vpn);
	    	}
	    	else if(pr_algo.equals("CLOCK")){                          // run CLOCK algorithm
	    		CLOCK(inverted_page_table,oper,vpn);
	    	}
	    	else
	    		System.out.println("You typed in wrong format of algorithm.");
		}
 
    }
    
    public static void FIFO(int[][] inverted_page_table,ArrayList<String> oper,ArrayList<Integer> vpn){
    	
    	int number_of_page_hit=0;
    	int number_of_page_fault=0;
    	int number_of_page_evect=0;
    	int global_timer=0;
    	int clock_tick=0;
    	String operation;                    // to catch operation
    	int virtual_page_number;             // to catch virtual_page_number
    	int i=0; // index of ArryaList oper
		int j=0; // index of ArrayList vpn
		int isMemoryFULL=0;    //by default, the memory is not full,actually it's empty, therefore assigning isMemoryFull==0,the first place
		
		// to implement FIFO algorithm by suing LinkedList<ArrayList<Integer>()>
		LinkedList<ArrayList<Integer>> FIFO_IPT=new LinkedList<ArrayList<Integer>>();
		initialize_FIFO_IPT(inverted_page_table,FIFO_IPT);                      //to create an inverted page table by using LinkedList
		
        while(!oper.get(i).equals("END")){
    		
    		global_timer++;    //increment the global_timer for every loop
    		
    		// to read the <oper_k>/END/CLOCK_TICK, <vpn_k>
    		operation=oper.get(i);
    		if(operation.equals("END"))
    			break;
 
    		// to check if the input is "CLOCK_TICK"
    		if(operation.equals("CLOCK_TICK")){
    			clock_tick=global_timer;
    			i++;            // only ArrayList oper gets increment when input is "CLOCK_TICK"
    			continue;       // continue to next operation when present operation is "CLOCK_TICK"
    		}
    		else{
    			virtual_page_number=vpn.get(j);                   //to continue catching vpn when previous operation is clock_tick
    			i++;             // ArrayList oper index gets increment
    			j++;             // ArrayList vpn index gets incement
    		}
    		
    	    //  to check if vpn is in main memory, it will return where the entry it's found, otherwise, -1 returned and page fault happens
    		int return_index=contain_vpn(FIFO_IPT,virtual_page_number);
    		//  PAGE_HIT
    		if(return_index!=-1){	
    			System.out.printf("%-5d%-6s%-11s\n\n",virtual_page_number,operation,"PAGE_HIT");   //print out <vpn_k> <oper_k> PAGE_HIT
    			if(operation.equals("READ")){                               // operation is "READ"
    				FIFO_IPT.get(return_index).set(2, global_timer);        // update referenced(R) timestamp
    			}
    			else{                                                       // operation is "WRITE"
    				FIFO_IPT.get(return_index).set(2, global_timer);        // update referenced(R) timestamp
    				FIFO_IPT.get(return_index).set(3, global_timer);        // update modified(M) timestamp
    			}
    			number_of_page_hit++;                                       //number_of_page_hit gets increment
    		}
    		// PAGE_FAULT occurs!! ---> FIFO replacement algorithm
    		else{
    			
    			// get the empty index in memory, and load the new one from disk to that index place
    			if(isMemoryFULL!=-1){
    				int loaded_index=isMemoryFULL;         //to make parameter make sense, pass the empty index in memory to "loaded_index"
    				System.out.printf("%-5d%-6s%-11s\n",virtual_page_number,operation,"PAGE_FAULT");
      			      FIFO_IPT.get(loaded_index).set(1, virtual_page_number);    // update the new virtual_page_number loaded
      			      FIFO_IPT.get(loaded_index).set(2, global_timer);           // update referenced(R) timestamp
      			    if(operation.equals("WRITE"))
      			      FIFO_IPT.get(loaded_index).set(3, global_timer);           // update modified(M) timestamp
      				   
      			System.out.printf("%-5d%-6s%-11s\n\n",FIFO_IPT.get(loaded_index).get(1),FIFO_IPT.get(loaded_index).get(0),"PAGE_LOAD");
    				
    				isMemoryFULL=check_if_memory_full(FIFO_IPT);
    			}
    			
    			// the memory is whole occupied by entry,let's officially start the "FIFO" algorithm
    			else{
    				
    				System.out.printf("%-5d%-6s%-11s\n",virtual_page_number,operation,"PAGE_FAULT");
    				  if(FIFO_IPT.get(0).get(3)!=0){                                             // when M bit =1, it needs to be evicted
    					  System.out.printf("%-5d%-6s%-11s\n",FIFO_IPT.get(0).get(1),FIFO_IPT.get(0).get(0),"PAGE_EVICT");
          			      number_of_page_evect++;                                                // number_of_page_evect gets increment by one
    				  }  			
        			  int frame_number=FIFO_IPT.get(0).get(0);                                   // keep the frame number for later use for newly loaded entry
        			  FIFO_IPT.remove(0);                                                        // to remove the oldest one
        			  FIFO_IPT.add(new ArrayList<Integer>());                                    // add the most recent arrival one at the tail
        			  
        			  //to update four elements in the newly loaded entry
        			  FIFO_IPT.get(FIFO_IPT.size()-1).add(frame_number);                         // set the frame number in the newly loaded entry at the tail
        			  FIFO_IPT.get(FIFO_IPT.size()-1).add(virtual_page_number);                  // update the new virtual_page_number loaded
        			  FIFO_IPT.get(FIFO_IPT.size()-1).add(global_timer);                         // update referenced(R) timestamp
        			  FIFO_IPT.get(FIFO_IPT.size()-1).add(0);                                    // update modified(M) timestamp when READ
        			 
        			  if(operation.equals("WRITE"))
        				  FIFO_IPT.get(FIFO_IPT.size()-1).set(3, global_timer);                  // update modified(M) timestamp when WRITE
        				   
        			System.out.printf("%-5d%-6d%-11s\n\n",FIFO_IPT.get(FIFO_IPT.size()-1).get(1),FIFO_IPT.get(FIFO_IPT.size()-1).get(0),"PAGE_LOAD");
    			}
    			number_of_page_fault++;
    		}
    	}
    	
        // print out how many the total number of PAGE_HIT, PAGE_FAULT, and PAGE_EVICT are. 
    	System.out.println("Number of PAGE_HIT: "+number_of_page_hit);
    	System.out.println("Number of PAGE_FAULT: "+number_of_page_fault);
    	System.out.println("Number of PAGE_EVICT: "+number_of_page_evect);
    	
    	// print out the inverted page table
    	//for(int k=0;k<FIFO_IPT.size();k++){
    	//	System.out.printf("%-3d%-5d%-4d%-4d\n",FIFO_IPT.get(k).get(0),FIFO_IPT.get(k).get(1),FIFO_IPT.get(k).get(2),FIFO_IPT.get(k).get(3));
    	//}
		
    }
    
    public static void LRU(int[][] inverted_page_table,ArrayList<String> oper,ArrayList<Integer> vpn){
    	
    	int number_of_page_hit=0;
    	int number_of_page_fault=0;
    	int number_of_page_evect=0;
    	int global_timer=0;
    	int clock_tick=0;
    	String operation;                    // to catch operation
    	int virtual_page_number;             // to catch virtual_page_number
    	int i=0;                             // index of ArryaList oper
		int j=0;                             // index of ArrayList vpn
		int isMemoryFULL=0;    //by default, the memory is not full,actually it's empty, therefore assigning isMemoryFull==0,the first place
    	
    	while(!oper.get(i).equals("END")){
    		
    		global_timer++;    //increment the global_timer for every loop
    		
    		// to read the <oper_k>/END/CLOCK_TICK, <vpn_k>
    		operation=oper.get(i);
    		if(operation.equals("END"))
    			break;
    	
    		// to check if the input is "CLOCK_TICK"
    		if(operation.equals("CLOCK_TICK")){
    			clock_tick=global_timer;
    			i++;               // only ArrayList oper gets increment when input is "CLOCK_TICK"
    			continue;          // continue to next operation when present operation is "CLOCK_TICK"
    		}
    		else{
    			virtual_page_number=vpn.get(j);                  //to continue catching vpn when previous operation is clock_tick
    			i++;             // ArrayList oper index gets increment
    			j++;             // ArrayList vpn index gets incement
    		}
    		
    	    // to check if vpn is in main memory, it will return where the entry it's found, otherwise, -1 returned and page fault happens
    		int return_index=contain_vpn(inverted_page_table,virtual_page_number);
    		//  PAGE_HIT 
    		if(return_index!=-1){	
    			System.out.printf("%-5d%-6s%-11s\n\n",virtual_page_number,operation,"PAGE_HIT");   //print out <vpn_k> <oper_k> PAGE_HIT
    			if(operation.equals("READ")){                             // operation is "READ"
    				inverted_page_table[return_index][2]=global_timer;    // update referenced(R) timestamp
    			}
    			else{                                                     // operation is "WRITE"
    				inverted_page_table[return_index][2]=global_timer;    // update referenced(R) timestamp
    				inverted_page_table[return_index][3]=global_timer;    // update modified(M) timestamp
    			}
    			number_of_page_hit++;                                     //number_of_page_hit gets increment
    		}
    		// PAGE_FAULT occurs!! ---> LRU replacement algorithm
    		else{
    			
    			// get the empty index in memory, and load the new one from disk to that index place
    			if(isMemoryFULL!=-1){
    				int loaded_index=isMemoryFULL;         //to make parameter make sense, pass the empty index in memory to "loaded_index"
    				System.out.printf("%-5d%-6s%-11s\n",virtual_page_number,operation,"PAGE_FAULT");
      			      inverted_page_table[loaded_index][1]=virtual_page_number;                      // update the new virtual_page_number loaded
      			      inverted_page_table[loaded_index][2]=virtual_page_number=global_timer;         // update referenced(R) timestamp
      			    if(operation.equals("WRITE"))
      			      inverted_page_table[loaded_index][3]=global_timer;                             // update modified(M) timestamp
      				   
      			System.out.printf("%-5d%-6d%-11s\n\n",inverted_page_table[loaded_index][1],inverted_page_table[loaded_index][0],"PAGE_LOAD");
    				
    				isMemoryFULL=check_if_memory_full(inverted_page_table);
    			}
    			
    			// the memory is whole occupied by entry,let's officially start the "LRU" algorithm
    			else{
    			// get the least_recent_arrival index, and evict it the load the new one into that place from disk
    			int least_recent_arrival_index=lra_index(inverted_page_table);
    			
    			System.out.printf("%-5d%-6s%-11s\n",virtual_page_number,operation,"PAGE_FAULT");
    			if(inverted_page_table[least_recent_arrival_index][3]!=0){                   // when M bit =1, it needs to be evicted  
    				System.out.printf("%-5d%-6s%-11s\n",inverted_page_table[least_recent_arrival_index][1],inverted_page_table[least_recent_arrival_index][0],"PAGE_EVICT");
      			    number_of_page_evect++;                                                  // number_of_page_evect gets increment by one
    			}
    			  inverted_page_table[least_recent_arrival_index][1]=virtual_page_number;    // update the new virtual_page_number loaded
    			  inverted_page_table[least_recent_arrival_index][2]=global_timer;           // update referenced(R) timestamp
    			  inverted_page_table[least_recent_arrival_index][3]=0;                      // update referenced(R) timestamp when READ
    			  if(operation.equals("WRITE"))
    				  inverted_page_table[least_recent_arrival_index][3]=global_timer;       // update modified(M) timestamp when WRITE
    			
    			  // we can use least_recent_arrival_index as the vpn brought in because it just got updated	   
    			System.out.printf("%-5d%-6s%-11s\n\n",inverted_page_table[least_recent_arrival_index][1],inverted_page_table[least_recent_arrival_index][0],"PAGE_LOAD");
    			}
    			number_of_page_fault++;
    		}
    	}
    	
    	// print out how many the total number of PAGE_HIT, PAGE_FAULT, and PAGE_EVICT are. 
    	System.out.println("Number of PAGE_HIT: "+number_of_page_hit);
    	System.out.println("Number of PAGE_FAULT: "+number_of_page_fault);
    	System.out.println("Number of PAGE_EVICT: "+number_of_page_evect);
    	
    	// print out the inverted page table
    	//for(int k=0;k<inverted_page_table.length;k++){
    	//			System.out.printf("%-3d%-5d%-4d%-4d\n",inverted_page_table[k][0],inverted_page_table[k][1],inverted_page_table[k][2],inverted_page_table[k][3]);
    	//		}
    	
    	
    }
    
    public static void CLOCK(int[][] inverted_page_table,ArrayList<String> oper,ArrayList<Integer> vpn){
    	
    	int number_of_page_hit=0;
    	int number_of_page_fault=0;
    	int number_of_page_evect=0;
    	int global_timer=0;
    	int clock_tick=0;
    	String operation;                    // to catch operation
    	int virtual_page_number;             // to catch virtual_page_number
    	int i=0;                             // index of ArryaList oper
		int j=0;                             // index of ArrayList vpn
		int isMemoryFULL=0;    //by default, the memory is not full,actually it's empty, therefore assigning isMemoryFull==0,the first place
		int clock_index=0;     // this index is for clock algorithm tracking, it is by default 0, meaning inspecting from the first place
		
    	while(!oper.get(i).equals("END")){
    		
    		global_timer++;    //increment the global_timer for every loop
    		
    		// to read the <oper_k>/END/CLOCK_TICK, <vpn_k>
    		operation=oper.get(i);
    		if(operation.equals("END"))
    			break;
    	
    		// to check if the input is "CLOCK_TICK"
    		if(operation.equals("CLOCK_TICK")){
    			clock_tick=global_timer;
    			i++;               // only ArrayList oper gets increment when input is "CLOCK_TICK"
    			continue;          // continue to next operation when present operation is clock_tick
    		}
    		else{
    			virtual_page_number=vpn.get(j);                  //to continue catch vpn when previous operation is clock_tick
    			i++;             // ArrayList oper index gets increment
    			j++;             // ArrayList vpn  index gets increment
    		}
    		
    		// to check if vpn is in main memory, it will return where the entry it's found, otherwise, -1 returned and page fault happens
    		int return_index=contain_vpn(inverted_page_table,virtual_page_number);
    		//  PAGE_HIT
    		if(return_index!=-1){	
    			System.out.printf("%-5d%-6s%-11s\n\n",virtual_page_number,operation,"PAGE_HIT");   //print out <vpn_k> <oper_k> PAGE_HIT
    			if(operation.equals("READ")){                             // operation is "READ"
    				inverted_page_table[return_index][2]=global_timer;    // update referenced(R) timestamp
    			}
    			else{                                                     // operation is "WRITE"
    				inverted_page_table[return_index][2]=global_timer;    // update referenced(R) timestamp
    				inverted_page_table[return_index][3]=global_timer;    // update modified(M) timestamp
    			}
    			number_of_page_hit++;                                     //number_of_page_hit gets increment
    		}
    		// PAGE_FAULT occurs!! ---> CLOCK replacement algorithm
    		else{
    			
    			// get the empty index in memory, and load the new one from disk to that index place
    			if(isMemoryFULL!=-1){
    				int loaded_index=isMemoryFULL;         //to make parameter make sense, pass the empty index in memory to "loaded_index"
    				System.out.printf("%-5d%-6s%-11s\n",virtual_page_number,operation,"PAGE_FAULT");
      			      inverted_page_table[loaded_index][1]=virtual_page_number;                      // update the new virtual_page_number loaded
      			      inverted_page_table[loaded_index][2]=virtual_page_number=global_timer;         // update referenced(R) timestamp
      			    if(operation.equals("WRITE"))
      			      inverted_page_table[loaded_index][3]=global_timer;                             // update modified(M) timestamp
      				   
      			System.out.printf("%-5d%-6d%-11s\n\n",inverted_page_table[loaded_index][1],inverted_page_table[loaded_index][0],"PAGE_LOAD");
    				
    				isMemoryFULL=check_if_memory_full(inverted_page_table);
    			}
    			
    			// the memory is whole occupied by entry,let's officially start the "CLOCK" algorithm
    			else{
    			
    			System.out.printf("%-5d%-6s%-11s\n",virtual_page_number,operation,"PAGE_FAULT");
    				
    			boolean evict_found=false;
    			while(!evict_found){
    				
    				//  this means R=0
    				if(clock_tick>inverted_page_table[clock_index][2]){
    					if(inverted_page_table[clock_index][3]!=0){                   // when M bit =1, it needs to be evicted
    					  System.out.printf("%-5d%-6d%-11s\n",inverted_page_table[clock_index][1],inverted_page_table[clock_index][0],"PAGE_EVICT");
      					  number_of_page_evect++;                                     // number_of_page_evect gets increment by one
    					}				
    	    			  inverted_page_table[clock_index][1]=virtual_page_number;    // update the new virtual_page_number loaded
    	    			  inverted_page_table[clock_index][2]=global_timer;           // update referenced(R) timestamp
    	    			  inverted_page_table[clock_index][3]=0;                      // update referenced(R) timestamp when READ
    	    			  if(operation.equals("WRITE"))
    	    				  inverted_page_table[clock_index][3]=global_timer;       // update modified(M) timestamp when WRITE
    	    				   
    	    			System.out.printf("%-5d%-6s%-11s\n\n",inverted_page_table[clock_index][1],inverted_page_table[clock_index][0],"PAGE_LOAD");
    	    			
    	    			evict_found=true;      // page evicted found!
    	    			clock_index++;         //the hand is advanced one position
    	    			// clock_index goes to index 0 when it's greater than the maximum index
    	    			if(clock_index==inverted_page_table.length)
    	    				clock_index=0;
    				}
    				
    				//  this means R=1
    				else{
    					  inverted_page_table[clock_index][2]=clock_tick-5;        // this will clear R as 0
    	    			  clock_index++;                                           // hand is advanced to the next page
    	    			  // clock_index goes to index 0 when it's greater than the maximum index
      	    			  if(clock_index==inverted_page_table.length)
      	    				  clock_index=0;
    				}
    			  }
    		   }
    			number_of_page_fault++;
    		}
    	}
    	
    	// print out how many the total number of PAGE_HIT, PAGE_FAULT, and PAGE_EVICT are. 
    	System.out.println("Number of PAGE_HIT: "+number_of_page_hit);
    	System.out.println("Number of PAGE_FAULT: "+number_of_page_fault);
    	System.out.println("Number of PAGE_EVICT: "+number_of_page_evect);
    	
    	
    	// print out the inverted page table
    	//for(int k=0;k<inverted_page_table.length;k++){
    	//			System.out.printf("%-3d%-5d%-4d%-4d\n",inverted_page_table[k][0],inverted_page_table[k][1],inverted_page_table[k][2],inverted_page_table[k][3]);
    	//}  	
    }
     
    public static void create_inverted_page_table(int[][] inverted_page_table){
    	
    	// to declare number of pages
    	final int num_page=1024;
    	int[] vpn=new int[num_page];
    	for(int i=0;i<vpn.length;i++){
    		vpn[i]=i;
    		//System.out.println(vpn[i]);
    	}
    	
    	for(int i=0;i<inverted_page_table.length;i++){
    		inverted_page_table[i][0]=i+1;                  //frame number
    		inverted_page_table[i][1]=0;                 //virtual page number by default is 0, meaning it's invalid!!!
    		inverted_page_table[i][2]=0;                  //R timestamp
    		inverted_page_table[i][3]=0;                  //M timestamp
    	}  	  	
    }
    
    //for LRU and CLOCK use
    public static int contain_vpn(int[][] inverted_page_table, int virtual_page_number){
    	
    	for(int i=0;i<inverted_page_table.length;i++){
    		if(virtual_page_number==inverted_page_table[i][1])
    			return i;        // return index in which virtual_page_number is found
    	}
    	return -1;
    }
  
    //for FIFO use
    public static int contain_vpn(LinkedList<ArrayList<Integer>> FIFO_IPT, int virtual_page_number){
    	
    	for(int i=0;i<FIFO_IPT.size();i++){
    		if(virtual_page_number==FIFO_IPT.get(i).get(1))
    			return i;        // return true if virtual_page_number is found
    	}
    	return -1;
    }
    
    //for LRU use
    public static int lra_index(int[][] inverted_page_table){
    	
    	// to find the least recent used one
    	int smallest_timestamp=Integer.MAX_VALUE;
    	int index=0;;
    	for(int i=0;i<inverted_page_table.length;i++){
    		if(smallest_timestamp>inverted_page_table[i][2]){
    			smallest_timestamp=inverted_page_table[i][2];
    			index=i;
    		}
    	}
    	return index;
    }
         
    // for FIFO use
    public static int check_if_memory_full(LinkedList<ArrayList<Integer>> FIFO_IPT){
    	
    	for(int i=0;i<FIFO_IPT.size();i++){
    		if(FIFO_IPT.get(i).get(1)==0)
    			return i;
    	}
    	return -1;       //memory is occupied, so it returns -1
    }
    
    // for LRU and CLOCK use
    public static int check_if_memory_full(int[][] inverted_page_table){
    	
    	for(int i=0;i<inverted_page_table.length;i++){
    		if(inverted_page_table[i][1]==0)
    			return i;
    	}
    	return -1;       //memory is occupied, so it returns -1
    }
    
    // for FIFO use
    public static void initialize_FIFO_IPT(int[][] inverted_page_table,LinkedList<ArrayList<Integer>> FIFO_IPT){
    	
    	// to build up LinKedList FIFO_IPT which has the same information contained in inverted_page_table
    	for(int i=0;i<inverted_page_table.length;i++){
    		FIFO_IPT.add(new ArrayList<Integer>());
    		
    		FIFO_IPT.get(i).add(inverted_page_table[i][0]);
    		FIFO_IPT.get(i).add(inverted_page_table[i][1]);
    		FIFO_IPT.get(i).add(inverted_page_table[i][2]);
    		FIFO_IPT.get(i).add(inverted_page_table[i][3]);
    	}
    }
}
