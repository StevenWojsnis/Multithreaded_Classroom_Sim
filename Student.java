import java.util.Random;

/*
This class represents Students. A cycle of waiting outside the classroom, waiting at a table,
taking the exam, and waiting to receive the exam grade is repeated until the student has completed
all their tests, or the last test is given.
*/

public class Student extends Thread{

	Random random = new Random();
	
	//Objects that different tasks are synchronzied on
	Object classRoomLine;
	Object table;
	Object[] atTable;
	Object examToFinish;
	WaitingForGrade waitingForGrade;
	Object readyToGrade;
	
	int numSeats;
	int tableNumber;
	int examsTaken = 0;
	
	static int numberFinished = 0;
	
	public static long time = System.currentTimeMillis();
		public void msg(String m) {
			System.out.println("["+(System.currentTimeMillis()-time)+"] "+getName()+":"+m);
		}
		
		public Student(int id, int numSeats, Object classRoomLine, Object[] atTable, Object examToFinish,
				Object readyToGrade, WaitingForGrade waitingForGrade) 
		{
			setName("Student-" + id);
			this.numSeats = numSeats;
			this.classRoomLine = classRoomLine;
			this.atTable = atTable;
			this.examToFinish = examToFinish;
			this.waitingForGrade = waitingForGrade;
			this.readyToGrade = readyToGrade;
		}
		
		public void run(){
			
			try {
				//Students arrive at school at a random time from immediately to 50000ms
				sleep((int)(random.nextInt(25000))); //Going to school

				
				
				while(examsTaken < 3 && Main.examsAdministered < 4)
					examLoop();
			
				msg(" is leaving school");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
		
		private void examLoop(){
			synchronized(classRoomLine){
				
				if(Main.examsOver) return; //To catch students who arrive to school after exams ended
				
				while(true){ //Wait to be notified not interrupted
					try {
						msg(" is waiting outside classroom");
						classRoomLine.wait(); //Wait to be let into classroom
						
						//Only enter if the classroom hasn't reached its capacity. Else wait again
						if(Main.seatOrdering < Main.capacity){
						
							//Assign a student to a table depending on when they entered the room
							table = atTable[(Main.seatOrdering/numSeats) ];
							tableNumber = (Main.seatOrdering/numSeats);
							Main.seatOrdering++;
							break;
						}
					} catch (InterruptedException e) {
						continue;
					}
				}
			}
			
			if(Main.examsOver) return; //To catch any students who were waiting outside the classroom when the instructor goes home
			
			synchronized(table){
				try {
					table.wait(); //Wait for the exam on their current table
					msg(" is starting the exam at table "+tableNumber);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			synchronized(examToFinish){
				try {
					examToFinish.wait(); //Wait for the exam to finish / simulates student taking exam
					msg(" finished the exam");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			//Check over notes
			try {
				sleep(random.nextInt(333)); //333ms is equivalent to 5 units of time for the given time units
				msg("is submitting the exam");
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			
			//Each student gets on waitingForGrade object. Add it to a queue to preserve the FCFS order of graded exam return.
			Main.waitingForGrade.add(waitingForGrade);
			
			//When the last student finishes checking notes, they signal the instructor to start grading
			if(Main.waitingForGrade.size() == Main.studentsTakingExam){
				synchronized(readyToGrade){
						readyToGrade.notify();
				}
					
			}
			
			synchronized(waitingForGrade){
				try {
					waitingForGrade.wait(); //Wait to receive a grade
					examsTaken++;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
}
