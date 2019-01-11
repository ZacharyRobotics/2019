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
	//private String driveStyle;
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
		driveSelector.addObject("Z-Axis", "Z-Axis");
		SmartDashboard.putData("Driving styles", driveSelector);
		
		leftSide = new Spark(0);
		rightSide = new Spark(1);
		
		//timer = new Timer();
	}

	@Override
	public void autonomousInit() {
		/*m_autoSelected = driveSelector.getSelected();
		// autoSelected = SmartDashboard.getString("Auto Selector",
		// defaultAuto);
		System.out.println("Auto selected: " + m_autoSelected);
		
		timer.start();*/
	}

	@Override
	public void autonomousPeriodic() {
		/*if (timer.get() < 3.0) {
			leftSide.set(-.3);
			rightSide.set(.3);
		} else if (timer.get() < 6.0) {
			leftSide.set(.3);
			rightSide.set(-.3);
		} else {
			leftSide.set(0);
			rightSide.set(0);
		}*/
		
		
		/*switch (m_autoSelected) {
			case kCustomAuto:
				// Put custom auto code here
				break;
			case kDefaultAuto:
			default:
				// Put default auto code here
				break;
		}*/
	}

	@Override
	public void teleopPeriodic() {
		String driveStyle = driveSelector.getSelected();
		
		if (driveStyle == "Point") {
			// This makes the joystick work where the robot just goes where you point the stick
			// PLAYERS: Payton, Jude
			
			leftSide.set(joystick.getX() + joystick.getY());
			rightSide.set(joystick.getX() - joystick.getY());
		} else if (driveStyle == "Z-Axis") {
			// Slider determines speed
			// PLAYERS: Parker
			
			double speed = (joystick.getThrottle() / 2) - .7;
			if (joystick.getY() < -.3 || joystick.getZ() > .2 || joystick.getZ() < -.2) { // Forwards?
				if (joystick.getZ() > .2 || joystick.getZ() < -.2) {
					leftSide.set((joystick.getZ() + speed) * .5);
					rightSide.set((joystick.getZ() - speed) * .5);
				} else {
					leftSide.set(speed);
					rightSide.set(-speed);
				}
			} else if (joystick.getY() > .3) { // Backwards?
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
		
		/*value = joystick.getZ();
		value = joystick.getThrottle();
		value = joystick.getTwist();

		boolean buttonValue;
		buttonValue = joystick.getTop();
		buttonValue = joystick.getTrigger();*/
	}
}
