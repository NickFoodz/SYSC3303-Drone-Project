SYSC3303 Project for Team 3
*******************************
Iteration 1

Files:

    1. DroneSubsystem.java
        - Name of file is class that models the drone subsystem that checks the scheduler for tasks
        - Simulates performance of tasks (Flying to a zone)
        - If an event is stored in the scheduler, it will perform the tasks for it
        - Reports task completion
        - Otherwise, the thread sleeps and checks again after 500 ms

    2. FireIncidentSubsystem.java
        - Name of file is class that models the subsystem to monitor fire incidents
        - Reads information from event csv file and records it as a FireEvent object
        - Adds FireEvent to scheduler for Drone subsystem to check

    3. Scheduler.java
        - Name of file is a class that models a scheduler for drones to deal with fire events
        - Holds information from the FireIncidentSubsystem to pass-through to DroneSubsystem
        - Reports when events are complete, signals that it is ready to schedule another

    4. FireEvent.java
        - Name represents the class, which is an object used to store information from the event csv
        - Holds Time of event, ZoneID, Type of event, and Severity of the event
        - Created by FireIncidentSubsystem to send to the scheduler

    5. DroneSubsystemTest.java
            - Test class for DroneSubsystem
            - Run to test the fightFire() method in DroneSubsystem

    6. FireIncidentSubsystemTest.java
            - Test class for FireIncidentSubsystem
            - Run to test the getData() method in FireIncidentSubsystem

    7. SchedulerTest.java
            - Test class for Scheduler
            - Run to test that the Scheduler stores data correctly from FireIncidentSubsystem


Iteration 2:
    1. DroneSubsystem.java
            - Implemented state machine and helper class for drones to support subsystem
            - List stores drones if available
            - Moved some methods to drone helper class and added new methods to deal with list

    2. FireIncidentSubsystem.java
            - Reads in all the events, and sorts them via timestamp
            - Forwards events to the scheduler at proper times with help of the simulator

    3. Scheduler.java
            - Maintains an event queue that is works via FIFO unless theres a HIGH severity event, which gets pushed to the front

    4. FireEvent.java
            - Time is now stored as a LocalTime Object

    5. DroneSubsystemTest.java
            - Test class for DroneSubsystem
            - Added tests for new drone helper class and changed methods in DroneSubsystem.java

    6. FireIncidentSubsystemTest.java
            - No changes from iteration 1

    7. SchedulerTest.java
            - Test class for Scheduler
            - Run to test that the Scheduler stores data correctly from FireIncidentSubsystem

    8. Simulation.java
            - Basic time based simulation
            - Increments the time up in seconds. If an event happens to be at any of those times, send the event to the scheduler queue


Iteration 3:
    1. DroneSubsystem.java
            - UDP for communicating between the drone subsystem and the scheduler, the drone subsystem and the drones, and the drones and the scheduler

    2. FireIncidentSubsystem.java
            - Fixed simulator adding events at proper times
            - UDP for communicating between scheduler

    3. Scheduler.java
            - UDP for communicating with FireIncidentSubsystem and DroneSubsystem, as well as status messages of the drones

    4. FireEvent.java
            - No Changes

    5. DroneSubsystemTest.java
            - Test class for DroneSubsystem

    6. FireIncidentSubsystemTest.java
            - Test class for FireIncidentSubsystemTest

    7. SchedulerTest.java
            - Test class for Scheduler

    8. Simulation.java
            - Added a constructor with the new UDP FireIncidentSystem



Instructions:

  - Make sure all the files are in the same folder. In IntelliJ, this means the class files are located in the
  src folder.

  - Replace the assignment to the field eventFilePath in FireIncidentSubsystem.java to the filepath of the
  csv file you wish to simulate.

  - Run the processes in this order: Drone Subsystem, Scheduler, Fire Incident Subsystem

  - If performing correctly, the console log will show each thread acting and printing a description of the
  current tasks as it runs. Each process will print its own log of its process
