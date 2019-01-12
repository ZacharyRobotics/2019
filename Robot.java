/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.usfirst.frc.team6489.robot;

import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Robot extends IterativeRobot {
	private SendableChooser<String> driveSelector = new SendableChooser<>();
	
	Joystick joystick = new Joystick(0);
	
	/** Negative is forward **/
	Spark leftSide;
	/** Positive is forward **/
	Spark rightSide;
	
	Timer timer;

	@Override
	public void robotInit() {
		CameraServer.getInstance().startAutomaticCapture();
		
		driveSelector.addDefault("Point", "Point");
		driveSelector.addObject("Point w/ speed control", "Point w/ speed control");
		driveSelector.addObject("Z-Axis", "Z-Axis");
		SmartDashboard.putData("Driving styles", driveSelector);
		
		leftSide = new Spark(0);
		rightSide = new Spark(1);
	}

	@Override
	public void teleopPeriodic() {
		String driveStyle = driveSelector.getSelected();
		
		if (driveStyle == "Point") {
			// This makes the joystick work where the robot just goes where you point the stick
			// PLAYERS: Payton, Jude
			
			leftSide.set(joystick.getX() + joystick.getY());
			rightSide.set(joystick.getX() - joystick.getY());
		} else if (driveStyle == "Point w/ speed control") {
			// Slider determines speed with point to drive
			// PLAYERS: Parker
			
			double speed = (-joystick.getThrottle() + 1) / 2; // Negative because the controls are reversed
			if (speed < .2) {
				speed = .2;
			}
			
			Boolean inXDeadzone = joystick.getX() > -.15 || joystick.getX() < .15;
			double yValue = -joystick.getY();
			if (inXDeadzone) {
				if (yValue > .2) { // Forwards
					leftSide.set(-speed);
					rightSide.set(speed);
				} else if (yValue < -.2) { // Backwards
					leftSide.set(speed);
					rightSide.set(-speed);
				} else { // Stationary
					leftSide.set(0);
					rightSide.set(0);
				}
			} else {
				double xValue = joystick.getX();
				if (yValue > .2) { // Forwards
					if (xValue > .15) {
						leftSide.set(-speed);
						rightSide.set(speed);
					}
					
				} else if (yValue < -.2) { // Backwards
					leftSide.set(speed);
					rightSide.set(-speed);
				} else { // Stationary
					leftSide.set(0);
					rightSide.set(0);
				}
			}
		} else if (driveStyle == "Z-Axis") {
			// Slider determines speed with z-axis turning
			// PLAYERS: 
			
			double speed = (joystick.getThrottle() / 2) - .7;
			if (joystick.getY() < -.3 || joystick.getZ() > .2 || joystick.getZ() < -.2) { // Forwards
				if (joystick.getZ() > .2 || joystick.getZ() < -.2) {
					leftSide.set((joystick.getZ() + speed) * .5);
					rightSide.set((joystick.getZ() - speed) * .5);
				} else {
					leftSide.set(speed);
					rightSide.set(-speed);
				}
			} else if (joystick.getY() > .3) { // Backwards
				// This math sets the speed to zero when the throttle is -1.
				if (joystick.getZ() > .2 || joystick.getZ() < -.2) {
					leftSide.set((joystick.getZ() - speed) * .5);
					rightSide.set((joystick.getZ() + speed) * .5);
				} else {
					leftSide.set(-speed);
					rightSide.set(speed);
				}
			} else {
				leftSide.set(0);
				rightSide.set(0);
			}
		}
		
		// Makes forward/backward use Y axis and turning use Z axis
		//leftSide.set((joystick.getZ() + joystick.getY()) * .5);
		//rightSide.set((joystick.getZ() - joystick.getY()) * .5);
	}
}


/*
int direction;
if (joystick.getY() < -.2) {
	// Forwards because reversed joystick
	direction = 1;
} else if (joystick.getY() > .2) {
	// Backwards
	direction = -1;
} else {
	// Stationary
	direction = 0;
}

double speed = joystick.getThrottle();
Boolean inXDeadzone = joystick.getX() > -.15 || joystick.getX() < .15;
if (inXDeadzone) { // X-axis dead zone to make straight navigation easier
	leftSide.set(-speed * direction);
	rightSide.set(speed * direction);
} else {
	if (direction > 0) {
		// Forwards
		leftSide.set((joystick.getX() + speed + 1) * .65);
		rightSide.set(-(joystick.getX() + speed + 1) * .65);
	} else {
		leftSide.set(-(joystick.getX() + speed + 1) * .65 * direction);
		rightSide.set((joystick.getX() + speed + 1) * .65 * direction);
	}
}
*/