/*
This class is used to represent a timer thread. Basically, a thread created from this class will
continuously run for 4 cycles (because 4 exams are scheduled). During each cycle, at specified time
intervals, a thread created from this class will notify certain objects (the instructor will be 
waiting on said objects) to put various events into motion.
*/

public class Timer extends Thread {

	int everyTwoHours = 0;
	int everyHour;
	int timeUnit = 0;
	
	//Objects that different tasks are synchronzied on
	Object letStudentsIn;
	Object handOutExams;
	Object examToFinish;
	
	public static long time = System.currentTimeMillis();
	public void msg(String m) {
		System.out.println("["+(System.currentTimeMillis()-time)+"] "+getName()+":"+m);
	}
	
	// Default constructor
	public Timer(int id, Object letStudentsIn, Object handOutExams, Object examToFinish) {
		setName("Timer-" + id);
		this.letStudentsIn = letStudentsIn;
		this.handOutExams = handOutExams;
		this.examToFinish = examToFinish;
	}
	
	public void run(){
		while(Main.examsAdministered < 4)
			processTimeUnit();
	}
	

	
	public int getTime(){
		return timeUnit;
	}
	
	//Method that's used to advance the current time in increments of 15 minutes
	public void processTimeUnit(){
		try {
			//15 minutes is equivalent to 1000 ms
			sleep(1000);
			timeUnit++;
			if(timeUnit > 0 && timeUnit%7 == 0){ //Every 1:45 hours, let students into classroom
				synchronized(letStudentsIn){
					letStudentsIn.notify();
				}
			}
			else if(timeUnit > 0 && timeUnit%8 == 0){ //Every 2:00 hours, begin exam
				synchronized(handOutExams){
					handOutExams.notify();
				}
			}
			else if(timeUnit > 0 && (timeUnit+4) % 8 == 0){ //Every 2:00 hours after the first hour, end an exam
				synchronized(examToFinish){
					examToFinish.notifyAll();
				}
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
