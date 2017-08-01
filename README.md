# Multithreaded_Classroom_Sim
A class project for distributed systems. Uses Java's multi-threading synchronization capabilities.

<b>Please refer to the in-code documentation for a full understanding of the project and thread interactions, brief overview explained below</b>


This project simulates an "exam day" in a classroom, where students and an instructor (threads) must coordinate according to various
specifications. 

The general rules are as follows: Any amount of students take a random time to come to school (some may come late - and consequently
miss an exam). Once at school, students wait outside the classroom before shuffling in - only a fixed amount of students are allowed
in at any given time, any leftovers will have to wait until the next exam. The students then join a table with another restriction on
the number of students allowed per table. The students aren't allowed to start taking their exam until their table is full (or as
full as possible given the number of students in the class). Finally, the students take their exam and wait for it to be graded. Depending
on how many exams they have currently taken, the students leave the classroom and decide whether or not to wait outisde the classroom again
for another exam.

Within this story are several thread synchronization interactions. For instance, the students must wait for other students to join their
table (This involves synchronizing student threads onto a "table" object). 

There are several examples of these kinds of interactions. The code is well documented and explains all of the interactions between the
students, instructor, and timer threads (timer is a separate thread that acts as a clock, used for orchestrating the timing restrictions
given by the project description).
