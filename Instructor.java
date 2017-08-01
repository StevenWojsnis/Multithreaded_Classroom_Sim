import java.util.Random;

/*
Class represents an Instructor. A thread created from this class will block on various events and wait
to be signaled by a timer thread. Upon being signaled, this thread will, in turn, signal various
objects that students are blocked on, allowing the students to move forward at specific times.
*/

public class Instructor extends Thread {
	
	Timer timer;
	Random random = new Random();
	
	//Objects that different tasks are synchronzied on
	Object classRoomLine;
	Object letStudentsIn;
	Object[] atTable;
	Object handOutExams;
	Object examToFinish;
	Object readyToGrade;
	WaitingForGrade[] waitingForGrade;
	

	public static long time = System.currentTimeMillis();
	public void msg(String m) {
		System.out.println("["+(System.currentTimeMillis()-time)+"] "+getName()+":"+m);
	}
	
	// Default constructor
	public Instructor(int id, Timer timer, Object classRoomLine, Object letStudentsIn, Object[] atTable,
			Object handOutExams, Object examToFinish, Object readyToGrade, WaitingForGrade[] waitingForGrade) 
	{
		setName("Instructor-" + id);
		this.timer = timer;
		this.classRoomLine = classRoomLine;
		this.letStudentsIn = letStudentsIn;
		this.atTable = atTable;
		this.handOutExams = handOutExams;
		this.examToFinish = examToFinish;
		this.readyToGrade = readyToGrade;
		this.waitingForGrade = waitingForGrade;
	}
	
	public void run(){
		while(Main.examsAdministered < 4){
			letStudentsIn();
			giveExam();
			waitForExamToFinish();
			gradeExams();
			Main.examsAdministered++;
		}
		//Tell any students remaining outside of classroom to go home
		Main.examsOver = true;
		synchronized(classRoomLine){
			classRoomLine.notifyAll();
		}
		
		msg("is leaving.");
		
		for(int i = 0; i < waitingForGrade.length; i++){
			System.out.print("Student_"+i+" received scores of: ");
			for(int j = 0; j< waitingForGrade[i].grades.length; j++){
				if(j == waitingForGrade[i].grades.length -1)
					System.out.print(waitingForGrade[i].getGrade(j));
				else
					System.out.print(waitingForGrade[i].getGrade(j)+ ", ");
			}
			System.out.println("");
		}
	}
	
	//Let students into the classroom 15 minutes early
	private void letStudentsIn(){
		synchronized(letStudentsIn){
			
			fifteenMinutesBefore();
			msg("Exam number: " + Main.examsAdministered);
		}
		synchronized(classRoomLine){
			classRoomLine.notifyAll(); //Signal students to let them know they can enter classroom
		}
	}
	
	//Used to let students into classroom 15 minutes early
	private synchronized void fifteenMinutesBefore(){
		try {
			letStudentsIn.wait(); //Wait till it's time to let students in (will be signaled by timer)
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	//Give the exam to the students
	private void giveExam(){
		synchronized(handOutExams){
			try {
				handOutExams.wait(); //Wait until it's time for the exam to begin
				Main.studentsTakingExam = Main.seatOrdering; //Keeps track of how many students are taking current exam
				Main.seatOrdering = 0; //Resets count of students in Main for the next exam
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		//Signal each table, informing the students at that table that they can begin the exam.
		for(Object table : atTable){
			synchronized(table){
				table.notifyAll();
			}
		}
	}
	
	//Wait for exam to finish, and wait until the last student is done checking their notes to begin grading
	private void waitForExamToFinish(){
		synchronized(examToFinish){
			try {
				examToFinish.wait(); //Wait for the exam to finish
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		//In case no students made it in time to the exam, prevent deadlock.
		if(Main.studentsTakingExam != 0){
			synchronized(readyToGrade){
				try {
					readyToGrade.wait(); //Wait for students to finish looking over their notes
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	//Grade the exams
	private void gradeExams(){
		while(!Main.waitingForGrade.isEmpty()){
			WaitingForGrade grade = Main.waitingForGrade.poll(); //Grade paper. Queue is used to ensure FCFS order of when students finished looking over notes.
			synchronized(grade){
				try {
					sleep(random.nextInt(133)); //133ms is equivalent to 2 units of time for the given time units
					grade.setGrade(random.nextInt(100)+1); //Student gets assigned grade. Can't be assigned 0 if they took the exam.
					grade.notify();
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
}
