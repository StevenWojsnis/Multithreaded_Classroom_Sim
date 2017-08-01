import java.util.*;

/*
 * This program simulates an "Exam day" in which four exams are given throughout the day. It should 
 * be noted that the program moves in units of 15 minutes, as that is the smallest time specified
 * in this instructions (aside from random times of sleeping for grading/checking notes). Each time
 * unit is simulated by 1000ms. The specifications numStudents, capacity, and numSeats can be overridden
 * in that order with command-line arguments.
 * 
 * Various Objects, including a custom WaitingForGrades object (used to keep track of individual
 * students' grades), are used as locks for various synchronized blocks and methods. This is used
 * to create a monitor and synchronize the various threads.
 * 
 * It should also be noted that given the current program, students first arrive at school anywhere from
 * immediately to 25000ms into execution. This is done to demonstrate what would happen if there were
 * late students who end up missing exams. To simulate what would happen if all students arrived at
 * school before the first exam, the random sleep time of the students (that simulates them travelling 
 * to school) can be foregone.
 * 
 * Project By: Steven Wojsnis
 * CS344, Project 1
 */

public class Main {
	static int numStudents = 16;
	static int capacity = 12;
	static int numSeats = 3;
	static int seatOrdering = 0; //Keeps track of students who have entered the classroom
	static int studentsTakingExam; //Keeps track of students who are taking the exam
	static int examsAdministered = 0;
	
	//A flag to determine when the final exam has been given and graded
	static boolean examsOver = false;
	
	//A queue on which students will line up in when waiting for their test grade, to facilitate FCFS order
	static Queue<WaitingForGrade> waitingForGrade = new LinkedList<WaitingForGrade>();
	
	public static void main(String args[]){
		
		//Gives user ability to override project specs
		if(args.length == 3){
			if(Integer.parseInt(args[0]) == 0 || Integer.parseInt(args[1]) == 0 
					|| Integer.parseInt(args[2]) == 0 
					|| Integer.parseInt(args[3]) > Integer.parseInt(args[2]))
			{
				System.out.println("Cannot have 0 students, capacity, or numSeats.");
				System.out.println("Also cannot have more seats than capacity." );
				System.out.println("Default values being used");
			}
			else{
				numStudents = Integer.parseInt(args[0]);
				capacity = Integer.parseInt(args[1]);
				numSeats = Integer.parseInt(args[2]);
			}
		}
		
		//Objects that different tasks are synchronized on
		Object classRoomLine = new Object(); //Students will wait on this object outside of classroom
		Object letStudentsIn = new Object();
		Object[] atTable = new Object[(capacity/numSeats)+1];
		Object handOutExams = new Object();
		Object examToFinish = new Object();
		Object readyToGrade = new Object();
		WaitingForGrade[] waitingForGrades = new WaitingForGrade[numStudents];
		
		//Objects created to act as a lock for each table
		for(int i = 0; i<=(capacity/numSeats); i++){
			atTable[i] = new Object();
		}
		
		//The timer is initiated and started with the necessary locks
		Timer timer = new Timer(0, letStudentsIn, handOutExams, examToFinish);
		timer.start();
		
		//The instructor is initiated and started with the necessary locks
		Instructor instructor = new Instructor(0, timer, classRoomLine, letStudentsIn, atTable, 
				handOutExams, examToFinish, readyToGrade, waitingForGrades);
		instructor.start();
		
		//Each passenger is initiated and started with the necessary locks
		for(int i = 1; i<=numStudents; i++){
			waitingForGrades[i-1] = new WaitingForGrade();
			//Object waitingForGradeStudent = new Object();
			Student passenger = new Student(i, numSeats, classRoomLine, atTable, examToFinish,
					readyToGrade, waitingForGrades[i-1]);
			passenger.start();
		}
		
	}
}
